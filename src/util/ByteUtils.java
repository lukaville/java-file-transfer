package util;

/**
 * alex on 12.02.16.
 */
public class ByteUtils {
    public static int byteArrayToInt(byte[] array) {
        int result = 0;
        for (int i = 0; i < array.length; ++i) {
            result += array[array.length - i - 1] * Math.pow(2, i);
        }
        return result;
    }
}
