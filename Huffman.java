import java.io.*;
import java.util.*;

public class Huffman {
    // Huffman tree node
    private static class Node implements Comparable<Node> {
        char ch;
        int freq;
        Node left, right;
        Node(char ch, int freq) {
            this.ch = ch;
            this.freq = freq;
        }
        Node(Node left, Node right) {
            this.ch = '\0';
            this.freq = left.freq + right.freq;
            this.left = left;
            this.right = right;
        }
        public boolean isLeaf() { return left == null && right == null; }
        @Override public int compareTo(Node other) { return this.freq - other.freq; }
    }

    // Bit Output Stream
    public static class BitOutputStream implements Closeable {
        private OutputStream out;
        private int currentByte;
        private int numBitsFilled;
        public BitOutputStream(OutputStream out) { this.out = out; }
        public void writeBit(int b) throws IOException {
            if (b != 0 && b != 1) throw new IllegalArgumentException();
            currentByte = (currentByte << 1) | b;
            numBitsFilled++;
            if (numBitsFilled == 8) {
                out.write(currentByte);
                numBitsFilled = 0;
            }
        }
        public void close() throws IOException {
            while (numBitsFilled != 0) writeBit(0);
            out.close();
        }
    }

    // Bit Input Stream
    public static class BitInputStream implements Closeable {
        private InputStream in;
        private int currentByte;
        private int numBitsRemaining;
        public BitInputStream(InputStream in) { this.in = in; numBitsRemaining = 0; }
        public int readBit() throws IOException {
            if (currentByte == -1) return -1;
            if (numBitsRemaining == 0) {
                currentByte = in.read();
                if (currentByte == -1) return -1;
                numBitsRemaining = 8;
            }
            numBitsRemaining--;
            return (currentByte >>> numBitsRemaining) & 1;
        }
        public void close() throws IOException { in.close(); }
    }

    // Build frequency table
    private static Map<Character, Integer> buildFreq(String text) {
        Map<Character, Integer> freq = new HashMap<>();
        for (char c : text.toCharArray())
            freq.put(c, freq.getOrDefault(c, 0) + 1);
        return freq;
    }

    // Build Huffman tree
    private static Node buildTree(Map<Character, Integer> freq) {
        PriorityQueue<Node> pq = new PriorityQueue<>();
        for (var e : freq.entrySet())
            pq.add(new Node(e.getKey(), e.getValue()));
        while (pq.size() > 1)
            pq.add(new Node(pq.poll(), pq.poll()));
        return pq.poll();
    }

    // Build code map
    private static void buildCode(Map<Character, String> map, Node n, String s) {
        if (n.isLeaf()) { map.put(n.ch, s); return; }
        buildCode(map, n.left, s + '0');
        buildCode(map, n.right, s + '1');
    }

    // Compress text -> bytes
    public static byte[] compress(String text) throws IOException {
        Map<Character, Integer> freq = buildFreq(text);
        Node root = buildTree(freq);
        Map<Character, String> code = new HashMap<>();
        buildCode(code, root, "");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitOutputStream bout = new BitOutputStream(baos);
        for (char c : text.toCharArray()) {
            String bits = code.get(c);
            for (char b : bits.toCharArray())
                bout.writeBit(b - '0');
        }
        bout.close();
        return baos.toByteArray();
    }

    // Decompress bytes -> text
    public static String decompress(byte[] data, Map<Character, Integer> freq) throws IOException {
        Node root = buildTree(freq);
        StringBuilder sb = new StringBuilder();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        BitInputStream bin = new BitInputStream(bais);
        Node node = root;
        int bit;
        while ((bit = bin.readBit()) != -1) {
            node = (bit == 0) ? node.left : node.right;
            if (node.isLeaf()) {
                sb.append(node.ch);
                node = root;
            }
        }
        bin.close();
        return sb.toString();
    }
}
