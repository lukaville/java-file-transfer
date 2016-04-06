package main;

import client.FileTransferClient;
import client.FileTransferClientListener;
import client.model.FileItem;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import network.NetworkConnection;
import network.SerialNetworkConnection;
import ui.MainForm;
import ui.UiListener;

import java.io.File;
import java.util.List;

/**
 * Created by nickolay on 10.02.16.
 */
public class Application implements UiListener, FileTransferClientListener {
    private final MainForm mainForm;
    private FileTransferClient client;
    private CommPortIdentifier waitingPort;

    public Application() {
        mainForm = new MainForm(this);
    }

    public void start() {
        mainForm.show();
    }

    @Override
    public void onDisconnectButton() {
        if (client != null) {
            client.disconnect();
        }
    }

    @Override
    public void onList(List<FileItem> files, String path) {
        mainForm.updateFileList(files, path);
    }

    @Override
    public void onFileError(int status) {
        String msg;
        switch (status) {
            case FileTransferClient.STATUS_FILE_IS_NOT_FILE:
                msg = "Requested file is not file";
                break;
            case FileTransferClient.STATUS_VERY_BIG_FILE:
                msg = "This file size doesn't supported";
                break;
            default:
                msg = "Can't download file";
        }

        onError(msg);
        System.out.println("Can't download file: error #" + status);
    }

    @Override
    public void onStartFileTransfer() {
        mainForm.clearProgress();
    }

    @Override
    public void onProgressFileTransfer(int current, int max) {
        mainForm.setFileTransferProgress(current, max);
    }

    @Override
    public void onError(String description) {
        mainForm.showAlert(description, true);
    }

    @Override
    public void onEndFileTransfer() {
        mainForm.showAlert("Передача файла завершена", false);
        mainForm.clearProgress();
    }

    @Override
    public void onConnect() {
        mainForm.setStatus(true);
    }

    @Override
    public void onDisconnect() {
        mainForm.setStatus(false);
        mainForm.clear();
    }

    @Override
    public void onSetSerialPort(int baudRate, int dataBits, int stopBits, int parity) {
        onDisconnectButton();
        if (waitingPort != null) {
            onConnectButton(waitingPort, baudRate, dataBits, stopBits, parity);
        }
        mainForm.closePortParamWaitingDialog();
    }

    @Override
    public void onConnectButton(CommPortIdentifier port, int baudRate, int dataBits, int stopBits, int parity) {
        // TODO: send typeSetSpeed frame with parameters
        try {
            NetworkConnection connection = new SerialNetworkConnection(port, 3000, baudRate, dataBits, stopBits, parity);
            client = new FileTransferClient(connection, this);
            client.connect();
        } catch (PortInUseException | UnsupportedCommOperationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWaitConnectButton(CommPortIdentifier port) {
        try {
            NetworkConnection connection = new SerialNetworkConnection(port, 3000, 115200, 8, 1, 2);
            client = new FileTransferClient(connection, this);
            client.connect();
            waitingPort = port;
        } catch (PortInUseException | UnsupportedCommOperationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGetListButton(String path) {
        if (client != null) {
            client.requestList(path);
        }
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

    @Override
    public void onFileTransferCancel() {

    }

    public FileTransferClient getFileTransferClient() {
        return client;
    }
}
