package main;

import client.FileTransferClient;
import client.FileTransferClientListener;
import client.model.FileItem;
import gnu.io.CommPortIdentifier;
import network.NetworkConnection;
import network.TcpNetworkConnection;
import ui.MainForm;
import ui.UiListener;

import java.io.File;
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

    }

    @Override
    public void onList(List<FileItem> files, String path) {
        mainForm.updateFileList(files, path);
    }

    @Override
    public void onFileError(int status) {
        System.out.println("Can't download file: error #" + status);
    }

    @Override
    public void onConnect() {
        mainForm.setStatus(true);
    }

    @Override
    public void onDisconnect() {
        mainForm.setStatus(false);
    }

    @Override
    public void onConnectButton(CommPortIdentifier port, int baudRate, int dataBits, int stopBits, int parity) {
        // TODO: serial port
        NetworkConnection connection = connect(baudRate == 0);
        client = new FileTransferClient(connection, this);
        client.connect();
    }

    @Override
    public void onGetListButton(String path) {
        client.requestList(path);
    }

    @Override
    public void onFileItemClick(FileItem fileItem, String remotePath) {
        String newRemotePath = remotePath + File.separator + fileItem.getName();

        if (fileItem.isDirectory()) {
            client.requestList(newRemotePath);
        } else {
            mainForm.openSaveFileDialog(fileItem.getName(), localPath -> {
                client.requestFile(newRemotePath, localPath);
            });
        }
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
