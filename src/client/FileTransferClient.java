package client;

import client.model.FileItem;
import network.NetworkConnection;
import protocol.*;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nickolay on 12.02.16.
 */
public class FileTransferClient implements FrameListener, ClientCallbacks {
    private ClientCallbacks callbacks = this;
    private FileTransferConnection connection;
    private FileTransferClientListener listener;

    public FileTransferClient(NetworkConnection connection, FileTransferClientListener listener) {
        this.connection = new FileTransferConnection(connection, this);
        this.connection.start();
        this.listener = listener;
    }

    public FileTransferClient(NetworkConnection connection, FileTransferClientListener listener, ClientCallbacks callbacks) {
        this(connection, listener);
        this.callbacks = callbacks;
    }

    public void requestList(String path) {
        connection.sendFrame(new Frame(Frame.TYPE_GET_LIST_DIRECTORY,
                path.getBytes()));
    }

    @Override
    public void onFrameReceived(Frame frame) {
        System.out.println("Frame received: " + frame.getType() + ". Length: " + frame.getData().length);
        FrameParser.parseFrame(frame, callbacks);
    }

    @Override
    public void onConnect() {

    }

    @Override
    public void onGetList(String path) {
        List<FileItem> fileItems = new ArrayList<>();

        if (path.equals(".")) {
            path = Paths.get(".").toAbsolutePath().normalize().toString();
        }

        File directory = new File(path);
        File[] listOfFiles = directory.listFiles();

        if (listOfFiles == null) {
            connection.sendFrame(FrameEncoder.encodeFileList(0x01, null));
        } else {
            for (File file : listOfFiles) {
                fileItems.add(new FileItem(file));
            }
            connection.sendFrame(FrameEncoder.encodeFileList(0x00, fileItems));
        }
    }

    @Override
    public void onList(List<FileItem> files) {
        listener.onList(files);
    }

    @Override
    public void onGetFile(String path) {

    }

    @Override
    public void onFile(int status, int lengthBytes) {

    }

    @Override
    public void onFileBlock(int bytesFrom, byte[] data) {

    }

    @Override
    public void onFileBlockReceiveSuccess() {

    }

    @Override
    public void onFileBlockReceiveFail() {

    }

    @Override
    public void onFileCancel() {

    }

    @Override
    public void onFileReceived() {

    }

    @Override
    public void onDisconnect() {

    }
}
