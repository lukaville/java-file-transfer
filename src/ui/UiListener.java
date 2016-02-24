package ui;

import client.model.FileItem;
import gnu.io.CommPortIdentifier;

public interface UiListener {
    void onDisconnectButton();
    void onConnectButton(CommPortIdentifier port, int baudRate, int dataBits, int stopBits, int parity);
    void onGetListButton(String path);
    void onFileItemClick(FileItem fileItem, String path);
    void onFileTransferCancel();
}