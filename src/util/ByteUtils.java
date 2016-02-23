package util;

/**
 * alex on 12.02.16.
 */
public class ByteUtils {
    public static int bytesToInt(byte[] arr, int offset) {
        return ((arr[3 + offset] & 0xFF) << 24) |
                ((arr[2 + offset] & 0xFF) << 16) |
                ((arr[1 + offset] & 0xFF) << 8) |
                (arr[offset] & 0xFF);
    }

    public static void intToBytes(byte[] arr, int value, int offset) {
        arr[3 + offset] = (byte) ((value >> 24) & 0xFF);
        arr[2 + offset] = (byte) ((value >> 16) & 0xFF);
        arr[1 + offset] = (byte) ((value >> 8) & 0xFF);
        arr[offset] = (byte) (value & 0xFF);
    }
}
