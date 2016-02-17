package network;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by nickolay on 11.02.16.
 */
public class TcpNetworkConnection extends NetworkConnection {
    private Socket socket;

    public TcpNetworkConnection(String host, int port) throws IOException {
        socket = new Socket(host, port);
    }

    public TcpNetworkConnection(int serverPort) throws IOException {
        ServerSocket serverSocket = new ServerSocket(serverPort);
        socket = serverSocket.accept();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    @Override
    public int available() throws IOException {
        return socket.getInputStream().available();
    }

    @Override
    public byte read() throws IOException {
        return (byte) socket.getInputStream().read();
    }

    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
