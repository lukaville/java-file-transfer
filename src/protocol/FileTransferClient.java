package protocol;

import network.NetworkConnection;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by nickolay on 11.02.16.
 */
public class FileTransferClient extends Thread {
    public static final int NONE = -1;
    public static final int BYTE_START_INDEX = 0;
    public static final int BYTE_TYPE_INDEX = 1;
    public static final int BYTE_SIZE_INDEX = 2;

    private int currentByte = NONE;

    // Current frame
    private int dataSize;
    private byte type;
    private byte[] data;

    private FrameListener listener;

    private NetworkConnection connection;

    public FileTransferClient(NetworkConnection connection) {
        this.connection = connection;
    }

    public void setListener(FrameListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = connection.getInputStream();

            while (!interrupted()) {
                if (inputStream.available() > 0) {
                    byte data = (byte) inputStream.read();
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

        if (currentByte == BYTE_SIZE_INDEX) {
            // No data in frame
            if (receivedByte == Frame.STOP_BYTE) {
                currentByte = NONE;
                onFrameReceived();
            } else {
                dataSize = receivedByte;
                this.data = new byte[dataSize];
            }
            return;
        }

        // End byte
        if (currentByte > (BYTE_SIZE_INDEX + dataSize)) {
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
        if (currentByte > BYTE_SIZE_INDEX) {
            data[currentByte - BYTE_SIZE_INDEX - 1] = receivedByte;
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
            listener.onFrameReceived(frame, this);
        }
    }

    public void sendFrame(Frame frame) throws IOException {
        connection.getOutputStream().write(frame.build());
    }

    public void disconnect() {
        connection.close();
        interrupt();
    }
}
