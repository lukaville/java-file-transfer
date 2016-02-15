package main;

import client.FileTransferClient;
import client.FileTransferClientListener;
import client.model.FileItem;
import gnu.io.CommPortIdentifier;
import network.NetworkConnection;
import network.TcpNetworkConnection;
import ui.MainForm;
import ui.UiListener;

import java.io.IOException;
import java.util.List;

/**
 * Created by nickolay on 10.02.16.
 */
public class Application implements UiListener, FileTransferClientListener {
    private final MainForm mainForm;
    private FileTransferClient client;

    public Application() {
        mainForm = new MainForm(this);
    }

    public void start() {
        mainForm.show();
    }

    @Override
    public void onDisconnectButton() {
        client.requestList(".");
    }

    @Override
    public void onList(List<FileItem> files) {
        mainForm.updateFileList(files);
    }

    @Override
    public void onConnectButton(CommPortIdentifier port, int baudRate, int dataBits, int stopBits, int parity) {
        // TODO: serial port
        NetworkConnection connection = connect(baudRate == 0);
        client = new FileTransferClient(connection, this);
    }

    private NetworkConnection connect(boolean isServer) {
        try {
            NetworkConnection connection;
            if (isServer) {
                connection = new TcpNetworkConnection(3333);
            } else {
                connection = new TcpNetworkConnection("127.0.0.1", 3333);
            }

            return connection;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
