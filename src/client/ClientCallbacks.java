package client;

import client.model.FileItem;

import java.util.List;

/**
 * Created by nickolay on 12.02.16.
 */
public interface ClientCallbacks {
    void onConnect();
    void onSetSerialPort(int baudRate, int dataBits, int stopBits, int parity);
    void onGetList(String path);
    void onList(List<FileItem> files, String path);
    void onGetFile(String path);
    void onFile(int status, int lengthBytes, int blockSize);
    void onFileBlock(int blockNumber, byte[] data);
    void onFileBlockReceiveSuccess();
    void onFileBlockReceiveFail();
    void onFileCancel();
    void onFileReceived();
    void onDisconnect();
    void onError(String description);
}
