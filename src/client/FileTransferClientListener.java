package client;

import client.model.FileItem;

import java.util.List;

/**
 * Created by nickolay on 12.02.16.
 */
public interface FileTransferClientListener {
    void onList(List<FileItem> files, String path);
    void onFileError(int status);
    void onConnect();
    void onDisconnect();
}
