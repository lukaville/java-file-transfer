import network.NetworkConnection;

import java.io.*;

/**
 * Created by nickolay on 15.02.16.
 */
public class FakeConnection extends NetworkConnection {

    private PipedInputStream in;
    private PipedOutputStream out;

    public FakeConnection() {
        try {
            in = new PipedInputStream();
            out = new PipedOutputStream(in);

            new Thread(() -> {
                while (true) {
                    try {
                        out.write(in.read());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return out;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return in;
    }

    @Override
    public void close() {
        try {
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
