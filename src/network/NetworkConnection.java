package network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by nickolay on 10.02.16.
 */
public interface NetworkConnection {
    OutputStream getOutputStream() throws IOException;
    InputStream getInputStream() throws IOException;
    void close();
}
