import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class RLE {

    // RLE compression
    public static byte[] compress(byte[] input) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (int i = 0; i < input.length;) {
            byte b = input[i];
            int run = 1;
            while (i + run < input.length && input[i + run] == b && run < 255) run++;
            bos.write(b);
            bos.write(run); // store run length
            i += run;
        }
        return bos.toByteArray();
    }

    // RLE decompression
    public static byte[] decompress(byte[] input) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (int i = 0; i < input.length; i += 2) {
            byte b = input[i];
            int count = input[i + 1] & 0xFF; // ensures non-negative
            for (int j = 0; j < count; j++) bos.write(b);
        }
        return bos.toByteArray();
    }
}



