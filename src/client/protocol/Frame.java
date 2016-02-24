package client.protocol;

import util.ByteUtils;

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
    public static final byte TYPE_GET_FILE_END = (byte) 0x0A;
    public static final byte TYPE_DISCONNECT = (byte) 0x0B;

    public static final Frame FRAME_CONNECT = new Frame(TYPE_CONNECT);

    private static final String[] FRAME_TYPE_DESCRIPTIONS = {
            "connect",
            "set_speed",
            "get_list_directory",
            "list_directory",
            "get_file",
            "get_file_response",
            "file_data",
            "file_data_success",
            "file_data_retry",
            "file_data_cancel",
            "file_data_end",
            "file_data_disconnect"
    };

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
            ByteUtils.intToBytes(result, dataSize, 2);
            System.arraycopy(data, 0, result, 6, dataSize);
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Frame type: ").append(FRAME_TYPE_DESCRIPTIONS[type]).append("\n");
        sb.append("Length: ").append(data == null ? 0 : data.length).append(".");

        return sb.toString();
    }
}
