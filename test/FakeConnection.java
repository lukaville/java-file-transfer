import network.NetworkConnection;

import java.io.*;

/**
 * Created by nickolay on 15.02.16.
 */
public class FakeConnection extends NetworkConnection {
    private final byte[] bytes;
    private int currentByte = 0;

    public FakeConnection(byte[] fakeBytes) {
        this.bytes = fakeBytes;
        currentByte = fakeBytes.length;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public int available() throws IOException {
        return currentByte;
    }

    @Override
    public byte read() throws IOException {
        return bytes[bytes.length - currentByte--];
    }

    @Override
    public void close() {
    }
}
