package protocol;

import java.util.Arrays;

/**
 * Created by nickolay on 11.02.16.
 */
public class Frame {
    public static final byte START_BYTE = (byte) 0x00;
    public static final byte STOP_BYTE = START_BYTE;

    public static final byte TYPE_CONNECT = (byte) 0x00;
    public static final byte TYPE_SET_SPEED = (byte) 0x01;
    public static final byte TYPE_GET_LIST_DIRECTORY = (byte) 0x02;
    public static final byte TYPE_LIST_DIRECTORY = (byte) 0x03;
    public static final byte TYPE_GET_FILE = (byte) 0x04;
    public static final byte TYPE_GET_FILE_RESPONSE = (byte) 0x05;
    public static final byte TYPE_FILE_DATA = (byte) 0x06;
    public static final byte TYPE_FILE_DATA_SUCCESS = (byte) 0x07;
    public static final byte TYPE_FILE_DATA_RETRY = (byte) 0x08;
    public static final byte TYPE_FILE_DATA_CANCEL = (byte) 0x09;
    public static final byte TYPE_GET_FILE_END = (byte) 0x10;
    public static final byte TYPE_DISCONNECT = (byte) 0x11;

    private byte type = TYPE_CONNECT;
    private byte[] data;

    public Frame(byte type) {
        this.type = type;
    }

    public Frame(byte type, byte[] data) {
        this.type = type;
        this.data = data;
    }

    public byte getType() {
        return type;
    }

    public byte[] getData() {
        return data;
    }

    public byte[] build() {
        int dataSize = data == null ? 0 : data.length;
        int frameSize = dataSize == 0 ? 6 : 7 + dataSize;

        byte[] result = new byte[frameSize];
        result[0] = START_BYTE;
        result[1] = type;
        result[frameSize - 1] = STOP_BYTE;

        if (dataSize != 0) {
            result[5] = (byte) ((dataSize >> 24) & 0xFF);
            result[4] = (byte) ((dataSize >> 16) & 0xFF);
            result[3] = (byte) ((dataSize >> 8) & 0xFF);
            result[2] = (byte) (dataSize & 0xFF);
            System.arraycopy(data, 0, result, 6, dataSize);
        }

        return result;
    }

    @Override
    public String toString() {
        if (data == null) {
            return "No data";
        }

        return "Length: " + data.length + ". Hash: " + Arrays.hashCode(data) + '\n' + bytesToHex(data);
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
