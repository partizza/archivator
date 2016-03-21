package ua.agwebs.compresion.hofmann;


import org.apache.log4j.Logger;

public class Node implements Comparable<Node> {

    static private Logger logger = Logger.getLogger(Node.class);

    // key
    private int frequency;
    //value
    private String value;
    //children
    private Node leftChild;
    private Node rightChild;

    public Node(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value can't be null.");
        }
        this.value = value;
        this.frequency = 1;
        logger.debug("Node was created: " + this.toString());
    }

    public Node(Node first, Node second) {

        if (first == null || second == null) {
            throw new IllegalArgumentException("Parameter can't be null.");
        }

        this.value = first.getValue() + second.getValue();
        this.frequency = first.getFrequency()+ second.getFrequency();
        this.leftChild = first;
        this.rightChild = second;

        logger.debug("Node was created: " + this.toString());
    }

    public String getValue() {
        return value;
    }

    public void incFrequency() {
        frequency++;
    }

    public int getFrequency() {
        return frequency;
    }

    public Node getLeftChild() {
        return leftChild;
    }

    public void setLeftChild(Node leftChild) {
        this.leftChild = leftChild;
    }

    public Node getRightChild() {
        return rightChild;
    }

    public void setRightChild(Node rightChild) {
        this.rightChild = rightChild;
    }

    @Override
    public String toString() {
        return "Node{" +
                "frequency=" + frequency +
                ", value=" + value +
                ", leftChild=" + leftChild +
                ", rightChild=" + rightChild +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        if (frequency != node.frequency) return false;
        if (!value.equals(node.getValue())) return false;
        if (leftChild != null ? !leftChild.equals(node.leftChild) : node.leftChild != null) return false;
        return !(rightChild != null ? !rightChild.equals(node.rightChild) : node.rightChild != null);

    }

    @Override
    public int hashCode() {
        return 31 * frequency + value.hashCode();
    }

    public int compareTo(Node another) {

        int result = this.frequency - another.frequency;
        if (result == 0) {
            result = this.value.compareTo(another.value);
        }

        return result;
    }
}
