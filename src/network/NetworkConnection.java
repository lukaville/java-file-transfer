package network;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by nickolay on 10.02.16.
 */
public abstract class NetworkConnection {
    public abstract OutputStream getOutputStream() throws IOException;
    public abstract int available() throws IOException;
    public abstract byte read() throws IOException;
    public abstract void close();
}
