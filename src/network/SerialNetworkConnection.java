package network;

import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by nickolay on 10.02.16.
 */
public class SerialNetworkConnection implements NetworkConnection {
    public static final String APP_NAME = "FileTransfer";
    private SerialPort port;

    public SerialNetworkConnection(CommPortIdentifier id, int timeout, int baudrate, int dataBits, int stopBits, int parity) throws PortInUseException, UnsupportedCommOperationException {
        port = (SerialPort) id.open(APP_NAME, timeout);
        port.setSerialPortParams(baudrate, dataBits, stopBits, parity);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return port.getOutputStream();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return port.getInputStream();
    }

    @Override
    public void close() {
        port.close();
    }
}
