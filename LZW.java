// LZW.java
import java.util.*;

public class LZW {
    public static List<Integer> compress(byte[] input) {
        Map<String, Integer> dict = new HashMap<>();
        for (int i = 0; i < 256; i++) dict.put("" + (char)i, i);

        String w = "";
        List<Integer> result = new ArrayList<>();
        int dictSize = 256;

        for (byte b : input) {
            String wc = w + (char)(b & 0xFF);
            if (dict.containsKey(wc)) w = wc;
            else {
                result.add(dict.get(w));
                dict.put(wc, dictSize++);
                w = "" + (char)(b & 0xFF);
            }
        }
        if (!w.equals("")) result.add(dict.get(w));
        return result;
    }

    public static byte[] decompress(List<Integer> compressed) {
        Map<Integer, String> dict = new HashMap<>();
        for (int i = 0; i < 256; i++) dict.put(i, "" + (char)i);

        String w = "" + (char)(int)compressed.remove(0);
        StringBuilder result = new StringBuilder(w);
        int dictSize = 256;

        for (int k : compressed) {
            String entry = dict.containsKey(k) ? dict.get(k) : w + w.charAt(0);
            result.append(entry);
            dict.put(dictSize++, w + entry.charAt(0));
            w = entry;
        }

        byte[] out = new byte[result.length()];
        for (int i = 0; i < out.length; i++) out[i] = (byte)result.charAt(i);
        return out;
    }
}


