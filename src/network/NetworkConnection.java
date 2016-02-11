package network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by nickolay on 10.02.16.
 */
public abstract class NetworkConnection {
    public abstract OutputStream getOutputStream() throws IOException;
    public abstract InputStream getInputStream() throws IOException;
    public abstract void close();
}
