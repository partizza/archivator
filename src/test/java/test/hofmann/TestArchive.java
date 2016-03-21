package test.hofmann;

import org.junit.Test;
import ua.agwebs.compresion.hofmann.ArchiveServiceProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.assertEquals;


public class TestArchive {

    @Test
    public void testUnpack() throws IOException {
        ArchiveServiceProvider archiveServiceProvider = new ArchiveServiceProvider();
        File source = new File("HungerGames.txt");
        File archive = new File("HungerGames.ag");
        File unpackedSource = new File("result.txt");
        archiveServiceProvider.pack(source, archive);
        archiveServiceProvider.unpack(archive, unpackedSource);

        try (BufferedReader original = new BufferedReader(new FileReader(source));
            BufferedReader unpacked = new BufferedReader(new FileReader(unpackedSource));){
            String expected = original.readLine();
            String resulted = unpacked.readLine();
            while (expected != null || resulted != null) {
                assertEquals("Files are not equals.", expected, resulted);
                expected = original.readLine();
                resulted = unpacked.readLine();
            }
        }
    }
}
