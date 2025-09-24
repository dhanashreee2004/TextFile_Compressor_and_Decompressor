import java.io.*;
import java.util.*;

// ---------------------- Huffman Node ----------------------
class Node implements Comparable<Node> {
    int data, freq;
    Node left, right;
    Node(int data, int freq) { this.data = data; this.freq = freq; }
    public int compareTo(Node n) { return this.freq - n.freq; }
    boolean isLeaf() { return left == null && right == null; }
}

// ---------------------- Bit Streams ----------------------
class BitOutputStream implements Closeable {
    private OutputStream out;
    private int currentByte = 0, numBitsFilled = 0;

    BitOutputStream(OutputStream out) { this.out = out; }

    void writeBit(int bit) throws IOException {
        currentByte = (currentByte << 1) | bit;
        numBitsFilled++;
        if (numBitsFilled == 8) flushByte();
    }

    void writeBits(String bits) throws IOException {
        for (char c : bits.toCharArray()) writeBit(c - '0');
    }

    private void flushByte() throws IOException {
        out.write(currentByte);
        numBitsFilled = 0;
        currentByte = 0;
    }

    public void close() throws IOException {
        if (numBitsFilled > 0) {
            currentByte <<= (8 - numBitsFilled);
            flushByte();
        }
        out.close();
    }
}

class BitInputStream implements Closeable {
    private InputStream in;
    private int currentByte = 0, numBitsRemaining = 0;

    BitInputStream(InputStream in) { this.in = in; }

    int readBit() throws IOException {
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

// ---------------------- HuffmanCompressor ----------------------
public class HuffmanCompressor {

    // ---------------------- Huffman ----------------------
    private static Node buildTree(int[] freq) {
        PriorityQueue<Node> pq = new PriorityQueue<>();
        for (int i = 0; i < freq.length; i++)
            if (freq[i] > 0) pq.add(new Node(i, freq[i]));

        while (pq.size() > 1) pq.add(new Node(-1, pq.poll().freq + pq.poll().freq) {{
            left = pq.poll(); right = pq.poll();
        }});
        return pq.poll();
    }
    
    private static void buildCodeMap(Node node, String s, Map<Integer, String> map) {
    if (node == null) return; // safety check
    if (node.isLeaf()) {
        map.put(node.data, s.length() > 0 ? s : "0"); // guarantee a code
        return;
    }
    buildCodeMap(node.left, s + "0", map);
    buildCodeMap(node.right, s + "1", map);
}

private static void compressBlock(byte[] data, DataOutputStream out) throws IOException {
    int[] freq = new int[256];
    for (byte b : data) freq[b & 0xFF]++;

    Node root = buildTree(freq);
    Map<Integer, String> codeMap = new HashMap<>();
    buildCodeMap(root, "", codeMap);

    // write freq table
    for (int f : freq) out.writeInt(f);

    // encode
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try (BitOutputStream bout = new BitOutputStream(bos)) {
        for (byte b : data) {
            String code = codeMap.get(b & 0xFF);
            if (code == null) code = "0"; // fallback for safety
            bout.writeBits(code);
        }
    }

    byte[] encoded = bos.toByteArray();
    out.writeInt(encoded.length);
    out.write(encoded);
}

    
    private static byte[] decompressBlock(DataInputStream in) throws IOException {
        int[] freq = new int[256];
        for (int i = 0; i < 256; i++) freq[i] = in.readInt();
        Node root = buildTree(freq);

        int length = in.readInt();
        byte[] encoded = new byte[length];
        in.readFully(encoded);

        List<Byte> result = new ArrayList<>();
        try (BitInputStream bin = new BitInputStream(new ByteArrayInputStream(encoded))) {
            Node node = root;
            int bit;
            while ((bit = bin.readBit()) != -1) {
                if (node == null) throw new IOException("Invalid Huffman tree or corrupted file.");
                node = bit == 0 ? node.left : node.right;
                if (node == null) throw new IOException("Invalid traversal in Huffman tree; file may be corrupted.");
                if (node.isLeaf()) {
                   result.add((byte) node.data);
                   node = root; // reset to root for next symbol
                }
            }

        }
        byte[] outArr = new byte[result.size()];
        for (int i = 0; i < result.size(); i++) outArr[i] = result.get(i);
        return outArr;
    }

    // ---------------------- RLE ----------------------
    private static byte[] rleCompress(byte[] input) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (int i = 0; i < input.length;) {
            byte b = input[i]; int run = 1;
            while (i + run < input.length && input[i + run] == b && run < 255) run++;
            bos.write(b); bos.write(run); i += run;
        }
        return bos.toByteArray();
    }

    private static byte[] rleDecompress(byte[] input) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (int i = 0; i < input.length; i += 2) {
            byte b = input[i]; int count = input[i + 1] & 0xFF;
            for (int j = 0; j < count; j++) bos.write(b);
        }
        return bos.toByteArray();
    }

    // ---------------------- LZW Placeholder ----------------------
    private static byte[] lzwCompress(byte[] input) { return input; }
    private static byte[] lzwDecompress(byte[] input) { return input; }

    // ---------------------- Compress File ----------------------
    public static void compress(String inputFile, String outputFile, String level) throws IOException {
        int blockSize = level.equalsIgnoreCase("low") ? 16*1024 : 64*1024;
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(outputFile));
             FileInputStream fis = new FileInputStream(inputFile)) {
            out.writeUTF(level);
            byte[] buffer = new byte[blockSize];
            int read;
            while ((read = fis.read(buffer)) > 0) {
               byte[] block = Arrays.copyOf(buffer, read);
               if (block.length == 0) continue; // <<< prevents empty block crash

               if (level.equalsIgnoreCase("low")) compressBlock(block, out);
               else if (level.equalsIgnoreCase("medium")) {
                  byte[] rleData = rleCompress(block);
                  byte[] huffRLE = compressBlockToBytes(rleData);
                  byte[] huffPlain = compressBlockToBytes(block);
                  if (huffRLE.length < huffPlain.length) {
                      out.write(huffRLE); out.writeBoolean(true);
                  } else {
                      out.write(huffPlain); out.writeBoolean(false);
                  }
               } else {
                  byte[] lzwData = lzwCompress(block);
                  compressBlock(lzwData, out);
                }
            }
   
        }
    }

    private static byte[] compressBlockToBytes(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        compressBlock(data, new DataOutputStream(baos));
        return baos.toByteArray();
    }

    // ---------------------- Decompress File ----------------------
    public static void decompress(String inputFile, String outputFile) throws IOException {
        try (DataInputStream in = new DataInputStream(new FileInputStream(inputFile));
             FileOutputStream fos = new FileOutputStream(outputFile)) {
            String level = in.readUTF();
            while (in.available() > 0) {
                byte[] block = decompressBlock(in);
                if (level.equalsIgnoreCase("medium")) {
                    boolean rleUsed = in.readBoolean();
                    if (rleUsed) block = rleDecompress(block);
                } else if (level.equalsIgnoreCase("high")) {
                    block = lzwDecompress(block);
                }
                fos.write(block);
            }
        }
    }

    // ---------------------- Main ----------------------
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("Usage: java HuffmanCompressor <compress|decompress> inputFile outputFile [level]");
            return;
        }
        if (args[0].equalsIgnoreCase("compress")) {
            String level = args.length >= 4 ? args[3] : "medium";
            compress(args[1], args[2], level);
        } else decompress(args[1], args[2]);
    }
}



