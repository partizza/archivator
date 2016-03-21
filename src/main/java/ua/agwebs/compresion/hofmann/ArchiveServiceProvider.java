package ua.agwebs.compresion.hofmann;


import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

public class ArchiveServiceProvider {

    static private final int MAX_BITS_BUFFER_SIZE = 32_000;
    static private Logger logger = Logger.getLogger(ArchiveServiceProvider.class);

     public void pack(File source, File target) throws IOException {
        if (!source.isFile()) {
            throw new IllegalArgumentException("Source file have to be non-directory file.");
        }
        Map<Character, List<Boolean>> codeMap = createHofmannTable(source);
        try (BufferedReader reader = new BufferedReader(new FileReader(source));
             FileOutputStream outputStream = new FileOutputStream(target);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
             BufferedOutputStream writer = new BufferedOutputStream(outputStream)) {

            objectOutputStream.writeObject(codeMap);
            BitSet bits = new BitSet();
            int bitCount = 0;
            char[] cbuff = new char[4_000];
            int charCount = 0;
            while ((charCount = reader.read(cbuff, 0, cbuff.length)) > 0) {
                for (int i = 0; i < charCount; i++) {
                    List<Boolean> code = codeMap.get(cbuff[i]);
                    for (Boolean b : code) {
                        bits.set(bitCount++, b);
                    }
                }
                //flush bits
                if (bits.length() > MAX_BITS_BUFFER_SIZE) {
                    BitSet bitBuff = bits.get(0, MAX_BITS_BUFFER_SIZE);
                    byte[] bytes = bitBuff.toByteArray();
                    writer.write(bytes, 0, bytes.length);
                    bits = bits.get(MAX_BITS_BUFFER_SIZE, bitCount);
                    bitCount = bitCount - MAX_BITS_BUFFER_SIZE;
                }
            }
            //flush bits if there are
            if (bits.length() > 0) {
                byte[] bytes = bits.toByteArray();
                writer.write(bytes, 0, bytes.length);
            }
            writer.flush();
        } catch (FileNotFoundException e) {
            logger.error("Pack operation: " + e.toString());
            throw e;
        } catch (IOException e) {
            logger.error("Pack operation: " + e.toString());
            throw e;
        }
        logger.debug("Source file " + source + " has been packed into " + target);
    }

    private Map<Character, List<Boolean>> createHofmannTable(File source) throws IOException {
        Coder coder = new Coder();
        try (BufferedReader reader = new BufferedReader(new FileReader(source))) {
            int cnt = 0;
            char[] cbuff = new char[4_000];
            while ((cnt = reader.read(cbuff, 0, cbuff.length)) > 0) {
                for (int i = 0; i < cnt; i++) {
                    coder.putCharacter(cbuff[i]);
                }
            }
        } catch (FileNotFoundException e) {
            logger.error("Creating Hofmann table: " + e.toString());
            throw e;
        } catch (IOException e) {
            logger.error("Creating Hofmann table: " + e.toString());
            throw e;
        }
        return coder.getHofmannTable();
    }

    public void unpack(File source, File target) throws IOException {
        if (!source.isFile()) {
            throw new IllegalArgumentException("Source file have to be non-directory file.");
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter(target));
             FileInputStream inputStream = new FileInputStream(source);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
             BufferedInputStream reader = new BufferedInputStream(inputStream)) {

            Map<Character, List<Boolean>> codeMap = (HashMap<Character, List<Boolean>>) objectInputStream.readObject();
            Map<List<Boolean>, Character> decodeMap = new HashMap<>();
            int maxDecodeLength = 0;
            for (Map.Entry<Character, List<Boolean>> entry : codeMap.entrySet()) {
                decodeMap.put(entry.getValue(), entry.getKey());
                if (maxDecodeLength < entry.getValue().size()) {
                    maxDecodeLength = entry.getValue().size();
                }
            }

            byte[] bbuff = new byte[4_000];
            List<Boolean> dcode = new ArrayList<>();
            int readCntBytes = 0;
            while ((readCntBytes = reader.read(bbuff, 0, bbuff.length)) > 0) {
                BitSet bits = BitSet.valueOf(bbuff);
                int indx = 0;
                while (indx < readCntBytes * 8) {
                    dcode.add(bits.get(indx++));
                    if (decodeMap.containsKey(dcode)) {
                        Character chr = decodeMap.get(dcode);
                        writer.write(String.valueOf(chr));
                        dcode.clear();
                    } else if (dcode.size() >= maxDecodeLength) {
                        dcode.clear();
                    }
                }
            }
            //flush dcode
            while (dcode.size() > 0) {
                dcode.add(false);
                if (decodeMap.containsKey(dcode)) {
                    Character chr = decodeMap.get(dcode);
                    writer.write(String.valueOf(chr));
                    dcode.clear();
                } else if (dcode.size() >= maxDecodeLength) {
                    dcode.clear();
                }
            }
            writer.flush();
        }
        catch (ClassNotFoundException e) {
            logger.error("Unpack operation: " + e.toString());
            throw new UnexpectedResult("Source file don't include information about Hofmann code " +
                    "in expected format for unpack operation");
        } catch (FileNotFoundException e) {
            logger.error("Unpack operation: " + e.toString());
            throw e;
        } catch (IOException e) {
            logger.error("Unpack operation: " + e.toString());
            throw e;
        }
        logger.debug("Source file " + source + " has been unpacked into " + target);
    }
}
