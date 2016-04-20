package network;

import gnu.io.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.TooManyListenersException;

/**
 * Created by nickolay on 10.02.16.
 */
public class SerialNetworkConnection extends NetworkConnection implements SerialPortEventListener {
    public static final String APP_NAME = "FileTransfer";
    private SerialPort port;

    public SerialNetworkConnection(CommPortIdentifier id, int timeout, int baudrate, int dataBits, int stopBits, int parity) throws PortInUseException, UnsupportedCommOperationException {
        port = (SerialPort) id.open(APP_NAME, timeout);
        port.setSerialPortParams(baudrate, dataBits, stopBits, parity);

        port.notifyOnCTS(true);
        port.notifyOnDataAvailable(true);
        port.notifyOnDSR(true);

        try {
            port.addEventListener(this);
        } catch (TooManyListenersException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return port.getOutputStream();
    }

    @Override
    public int available() throws IOException {
        return port.getInputStream().available();
    }

    @Override
    public byte read() throws IOException {
        return (byte) port.getInputStream().read();
    }

    @Override
    public void close() {
        new Thread(){
            @Override
            public void run(){
                try {
                    port.getInputStream().close();
                    port.getOutputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println("Waiting...");

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("Removing listener...");
                port.removeEventListener();

                System.out.println("Closing serial port...");
                port.close();
            }
        }.start();
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        System.out.println("Received event. Type: " + event.getEventType() + ", old value: " + event.getOldValue() + ", new value: " + event.getNewValue());
        System.out.println("RTS: " + port.isRTS());
        System.out.println("CTS: " + port.isCTS());

        if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE && !event.getNewValue()) {
            port.close();
        }
    }
}
