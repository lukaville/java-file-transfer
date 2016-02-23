package client;

import client.model.FileItem;
import client.protocol.*;
import network.NetworkConnection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * Created by nickolay on 12.02.16.
 */
public class FileTransferClient implements FrameListener, ClientCallbacks {
    public static final int DEFAULT_BLOCK_SIZE = 1024;

    private ClientCallbacks callbacks = this;
    private DataLink connection;
    private FileTransferClientListener listener;

    // Current file for write
    private int currentWriteFileLength;
    private int currentWriteBlockSize;
    private Path currentWriteFile;
    private OpenOption[] FILE_WRITE_OPTIONS = new OpenOption[] { APPEND, CREATE };

    // Current file for read
    private FileInputStream currentReadFileStream;
    private byte[] currentReadFileBuffer;
    private int currentReadFileBlock;
    private int currentReadBlockSize;
    private boolean isEndFile = false;

    public FileTransferClient(NetworkConnection connection, FileTransferClientListener listener) {
        this.connection = new DataLink(connection, this);
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

    public void connect() {
        connection.sendFrame(new Frame(Frame.TYPE_CONNECT));
    }

    public void requestFile(String remotePath, String localPath) {
        currentWriteFile = Paths.get(localPath);
        connection.sendFrame(FrameEncoder.encodeGetFile(remotePath));
    }

    @Override
    public void onFrameReceived(Frame frame) {
        System.out.println("\n\nFRAME RECEIVED\n" + frame.toString());
        FrameDecoder.parseFrame(frame, callbacks);
    }

    @Override
    public void onConnect() {
        listener.onConnect();
    }

    @Override
    public void onGetList(String path) {
        List<FileItem> fileItems = new ArrayList<>();

        path = Paths.get(path).toAbsolutePath().normalize().toString();

        File directory = new File(path);
        File[] listOfFiles = directory.listFiles();

        if (listOfFiles == null) {
            connection.sendFrame(FrameEncoder.encodeFileList(0x01, null, path));
        } else {
            for (File file : listOfFiles) {
                fileItems.add(new FileItem(file));
            }
            connection.sendFrame(FrameEncoder.encodeFileList(0x00, fileItems, path));
        }
    }

    @Override
    public void onList(List<FileItem> files, String path) {
        listener.onList(files, path);
    }

    @Override
    public void onGetFile(String localPath) {
        File file = new File(localPath);

        if (!file.isFile()) {
            connection.sendFrame(FrameEncoder.encodeGetFileResponse(0x01, 0, 0));
            return;
        }

        // TODO: file length int bytes must be < MAX_INT
        int fileLength = (int) file.length();
        int blockSize = DEFAULT_BLOCK_SIZE;
        currentReadBlockSize = blockSize;
        isEndFile = false;

        try {
            currentReadFileStream = new FileInputStream(localPath);
            currentReadFileBuffer = new byte[blockSize];
            currentReadFileBlock = 0;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        connection.sendFrame(FrameEncoder.encodeGetFileResponse(0x00, fileLength, blockSize));
    }

    @Override
    public void onFile(int status, int lengthBytes, int blockSize) {
        if (status != 0x00) {
            listener.onFileError(status);
            return;
        }

        currentWriteFileLength = lengthBytes;
        currentWriteBlockSize = blockSize;

        connection.sendFrame(new Frame(Frame.TYPE_FILE_DATA_SUCCESS));
    }

    @Override
    public void onFileBlock(int blockNumber, byte[] data) {
        if (data == null) {
            connection.sendFrame(new Frame(Frame.TYPE_FILE_DATA_RETRY));
            return;
        }

        if (blockNumber * currentWriteBlockSize > currentWriteFileLength) {
            int trailingDataLength = currentWriteFileLength - currentWriteBlockSize * (blockNumber - 1);
            byte[] truncatedData = new byte[trailingDataLength];
            System.arraycopy(data, 0, truncatedData, 0, trailingDataLength);
            data = truncatedData;
        }

        try {
            Files.write(currentWriteFile, data, FILE_WRITE_OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
        }

        connection.sendFrame(new Frame(Frame.TYPE_FILE_DATA_SUCCESS));
    }

    @Override
    public void onFileBlockReceiveSuccess() {
        if (isEndFile) {
            connection.sendFrame(new Frame(Frame.TYPE_GET_FILE_END));
            return;
        }

        try {
            int currentByte = -1;
            int currentByteIndex = 0;
            while(currentByteIndex < currentReadBlockSize && (currentByte = currentReadFileStream.read()) != -1) {
                currentReadFileBuffer[currentByteIndex++] = (byte) currentByte;
            }

            connection.sendFrame(FrameEncoder.encodeFilePart(currentReadFileBuffer, currentReadFileBlock));
            currentReadFileBlock++;

            // End file
            if (currentByte == -1) {
                isEndFile = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        listener.onDisconnect();
    }
}
