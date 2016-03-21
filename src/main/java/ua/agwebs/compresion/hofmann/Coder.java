package ua.agwebs.compresion.hofmann;

import org.apache.log4j.Logger;
import java.util.*;

public class Coder {

    static private Logger logger = Logger.getLogger(Coder.class);

    private Map<Character, Node> characters;

    public Coder() {
        characters = new HashMap<Character, Node>();
        logger.debug("Coder was created");
    }

    public void putCharacter(char chr) {
        if (characters.containsKey(chr)) {
            Node node = characters.get(chr);
            node.incFrequency();
            logger.debug("Frequency was increased: " + node.toString());
        } else {
            Node node = new Node(String.valueOf(chr));
            characters.put(chr, node);
            logger.debug("New node was added: " + node.toString());
        }
    }

    private Node getRootOfHofmannTree() {
        // sort by frequency
        SortedSet<Node> sortedNodes = new TreeSet<Node>();
        sortedNodes.addAll(characters.values());
        logger.debug("Sorted list of characters by frequency: " + sortedNodes);

        while (sortedNodes.size() > 1) {

            Node firstNode = sortedNodes.first();
            sortedNodes.remove(firstNode);

            Node secondNode = sortedNodes.first();
            sortedNodes.remove(secondNode);

            Node newNode = new Node(firstNode, secondNode);
            sortedNodes.add(newNode);
        }

        logger.debug("Hofmann tree: " + sortedNodes);
        return sortedNodes.first();
    }

    private void defineHofmannCode(Node currentNode, List<Boolean> currentCode, Map<Character, List<Boolean>> codeMap) {
        if (currentNode == null) {
            return;
        }
        // stop and write code if there are no children
        if (currentNode.getLeftChild() == null && currentNode.getRightChild() == null) {
            String strValue = currentNode.getValue();
            if (strValue.length() != 1) {
                throw new UnexpectedResult("Value of current node is not appropriate to be coded." + currentNode);
            }
            if (currentCode.size() < 1) {
                currentCode.add(true);
            }
            codeMap.put(strValue.charAt(0), currentCode);
            logger.debug("Hofmann code of byte {" + currentNode.getValue() + "} : " + currentCode);
        }
        // go deep if there are one or two children
        else {
            // go left
            List<Boolean> leftTurn = new ArrayList<Boolean>(currentCode);
            leftTurn.add(false);
            defineHofmannCode(currentNode.getLeftChild(), leftTurn, codeMap);

            // go right
            List<Boolean> rightTurn = new ArrayList<Boolean>(currentCode);
            rightTurn.add(true);
            defineHofmannCode(currentNode.getRightChild(), rightTurn, codeMap);
        }
    }

    public Map<Character, List<Boolean>> getHofmannTable() {
        Node root = getRootOfHofmannTree();
        Map<Character, List<Boolean>> codeMap = new HashMap<Character, List<Boolean>>();
        List<Boolean> startCode = new ArrayList<>();
        startCode.add(true);
        defineHofmannCode(root, startCode, codeMap);

        logger.debug("Elements and codeMap: " + codeMap);
        return codeMap;
    }

    @Override
    public String toString() {
        return "Coder{" +
                ", characters=" + characters +
                '}';
    }
}
