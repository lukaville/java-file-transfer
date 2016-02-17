package protocol;

import network.NetworkConnection;

import java.io.IOException;

/**
 * Created by nickolay on 11.02.16.
 */
public class FileTransferConnection extends Thread {
    public static final int NONE = -1;
    public static final int BYTE_START_INDEX = 0;
    public static final int BYTE_TYPE_INDEX = 1;
    public static final int BYTE_SIZE_INDEX_START = 2;
    public static final int BYTE_SIZE_INDEX_STOP = 5;

    private int currentByte = NONE;

    // Current frame
    private int dataSize;
    private byte[] dataSizeByteArray = new byte[4];
    private byte type;
    private byte[] data;

    private FrameListener listener;

    private NetworkConnection connection;

    public FileTransferConnection(NetworkConnection connection, FrameListener listener) {
        this.connection = connection;
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            while (!interrupted()) {
                if (connection.available() > 0) {
                    byte data = connection.read();
                    handleByte(data);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleByte(byte receivedByte) {
        if (currentByte == NONE) {
            if (receivedByte == Frame.START_BYTE) {
                currentByte = BYTE_START_INDEX;
            }
            // else - error occurred
            return;
        }

        currentByte++;

        if (currentByte == BYTE_TYPE_INDEX) {
            type = receivedByte;
            return;
        }

        if (currentByte >= BYTE_SIZE_INDEX_START && currentByte <= BYTE_SIZE_INDEX_STOP) {
            dataSizeByteArray[currentByte - BYTE_SIZE_INDEX_START] = receivedByte;

            if (currentByte == BYTE_SIZE_INDEX_STOP) {
                //big-endian
                dataSize = ((dataSizeByteArray[0] & 0xFF) << 24) | ((dataSizeByteArray[1] & 0xFF) << 16)
                        | ((dataSizeByteArray[2] & 0xFF) << 8) | (dataSizeByteArray[3] & 0xFF);


                if (dataSize <= 0) {
                    currentByte = NONE;
                    onFrameReceived();
                } else {
                    this.data = new byte[dataSize];
                }
            }
            return;
        }

        // End byte
        if (currentByte > (BYTE_SIZE_INDEX_STOP + dataSize)) {
            if (receivedByte == Frame.STOP_BYTE) {
                currentByte = NONE;
                onFrameReceived();
            } else {
                // error occurred
                currentByte = NONE;
            }
            return;
        }

        // Data bytes
        if (currentByte > BYTE_SIZE_INDEX_STOP) {
            data[currentByte - BYTE_SIZE_INDEX_STOP - 1] = receivedByte;
        }
    }

    private void onFrameReceived() {
        Frame frame;
        if (data == null || data.length == 0) {
            frame = new Frame(type);
        } else {
            frame = new Frame(type, data);
        }

        if (listener != null) {
            listener.onFrameReceived(frame);
        }
    }

    public void sendFrame(Frame frame) {
        System.out.println("Frame sent: " + frame.getType() + '\n' + frame.toString() + "\n\n");

        try {
            connection.getOutputStream().write(frame.build());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        connection.close();
        interrupt();
    }
}
