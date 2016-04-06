package client;

import client.model.FileItem;
import client.protocol.*;
import network.NetworkConnection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
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

    public static final byte STATUS_OK = (byte) 0x00;
    public static final byte STATUS_DIRECTORY_NOT_EXIST = (byte) 0x01;
    public static final byte STATUS_FILE_IS_NOT_FILE = (byte) 0x02;
    public static final byte STATUS_VERY_BIG_FILE = (byte) 0x03;
    public static final byte STATUS_UNKNOWN_ERROR = (byte) 0xFF;

    private ClientCallbacks callbacks = this;
    private final DataLink dataLink;
    private FileTransferClientListener listener;

    public static final int HEARTBEAT_PERIOD = 5000;
    private Thread connectionHeartBeat;
    private long lastFrame;

    // Current file for write
    private long lastFileBlockFrame;
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
    private boolean wasEndFileFrameSend = false;
    private int currentReadFileLength;

    public FileTransferClient(NetworkConnection connection, FileTransferClientListener listener) {
        this.dataLink = new DataLink(connection, this);
        this.dataLink.start();
        this.listener = listener;
    }

    public FileTransferClient(NetworkConnection connection, FileTransferClientListener listener, ClientCallbacks callbacks) {
        this(connection, listener);
        this.callbacks = callbacks;
    }

    public void requestList(String path) {
        dataLink.sendFrame(new Frame(Frame.TYPE_GET_LIST_DIRECTORY,
                path.getBytes()));
    }

    public void connect() {
        lastFrame = System.currentTimeMillis();
        lastFileBlockFrame = -1;

        connectionHeartBeat = new Thread(() -> {
            while (!Thread.interrupted()) {
                if (System.currentTimeMillis() - lastFrame > HEARTBEAT_PERIOD * 2) {
                    disconnect();
                    break;
                }

                dataLink.sendFrame(Frame.FRAME_CONNECT);

                if (lastFileBlockFrame > 0) {
                    if (System.currentTimeMillis() - lastFileBlockFrame > HEARTBEAT_PERIOD) {
                        dataLink.sendFrame(Frame.FRAME_FILE_DATA_RETRY);
                    }
                }

                try { Thread.sleep(HEARTBEAT_PERIOD); } catch (InterruptedException ignored) {}
            }
        });

        connectionHeartBeat.start();
    }

    public void disconnect() {
        connectionHeartBeat.interrupt();
        dataLink.disconnect();
        onDisconnect();
    }

    public void requestFile(String remotePath, String localPath) {
        currentWriteFile = Paths.get(localPath);
        dataLink.sendFrame(FrameEncoder.encodeGetFile(remotePath));
    }

    @Override
    public void onFrameReceived(Frame frame) {
        System.out.println("\n\nFRAME RECEIVED\n" + frame.toString());
        FrameDecoder.parseFrame(frame, callbacks);
        lastFrame = System.currentTimeMillis();
    }

    @Override
    public void onConnect() {
        listener.onConnect();
        lastFrame = System.currentTimeMillis();
    }

    @Override
    public void onSetSerialPort(int baudRate, int dataBits, int stopBits, int parity) {
        listener.onSetSerialPort(baudRate, dataBits, stopBits, parity);
    }

    public void sendSerialPortParams(int baudRate, int dataBits, int stopBits, int parity) {
        dataLink.sendFrame(FrameEncoder.encodeSerialPortSettings(baudRate, dataBits, stopBits, parity));
    }

    @Override
    public void onGetList(String path) {
        List<FileItem> fileItems = new ArrayList<>();

        path = Paths.get(path).toAbsolutePath().normalize().toString();

        File directory = new File(path);

        File[] listOfFiles = directory.listFiles();

        if (listOfFiles == null) {
            dataLink.sendFrame(FrameEncoder.encodeFileList(STATUS_DIRECTORY_NOT_EXIST, null, path));
        } else {
            for (File file : listOfFiles) {
                fileItems.add(new FileItem(file));
            }
            dataLink.sendFrame(FrameEncoder.encodeFileList(STATUS_OK, fileItems, path));
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
            dataLink.sendFrame(FrameEncoder.encodeGetFileResponse(STATUS_FILE_IS_NOT_FILE, 0, 0));
            return;
        }

        if (file.length() > Integer.MAX_VALUE) {
            dataLink.sendFrame(FrameEncoder.encodeGetFileResponse(STATUS_VERY_BIG_FILE, 0, 0));
            return;
        }

        int fileLength = (int) file.length();
        int blockSize = DEFAULT_BLOCK_SIZE;
        currentReadBlockSize = blockSize;
        currentReadFileLength = fileLength;
        isEndFile = false;
        wasEndFileFrameSend = false;

        try {
            currentReadFileStream = new FileInputStream(localPath);
            currentReadFileBuffer = new byte[blockSize];
            currentReadFileBlock = 0;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        dataLink.sendFrame(FrameEncoder.encodeGetFileResponse(STATUS_OK, fileLength, blockSize));
    }

    @Override
    public void onFile(int status, int lengthBytes, int blockSize) {
        if (status != STATUS_OK) {
            listener.onFileError(status);
            return;
        }

        currentWriteFileLength = lengthBytes;
        currentWriteBlockSize = blockSize;

        lastFileBlockFrame = System.currentTimeMillis();

        dataLink.sendFrame(Frame.FRAME_FILE_DATA_SUCCESS);

        listener.onStartFileTransfer();
    }

    @Override
    public void onFileBlock(int blockNumber, byte[] data) {
        if (data == null) {
            dataLink.sendFrame(Frame.FRAME_FILE_DATA_RETRY);
            return;
        }

        byte[] blockData = new byte[currentWriteBlockSize];
        System.arraycopy(data, 0, blockData, 0, currentWriteBlockSize);

        if ((blockNumber + 1) * currentWriteBlockSize > currentWriteFileLength) {
            int trailingDataLength = currentWriteFileLength - currentWriteBlockSize * blockNumber;
            byte[] truncatedData = new byte[trailingDataLength];
            System.arraycopy(blockData, 0, truncatedData, 0, trailingDataLength);
            blockData = truncatedData;
        }

        try {
            Files.write(currentWriteFile, blockData, FILE_WRITE_OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
        }

        lastFileBlockFrame = System.currentTimeMillis();

        dataLink.sendFrame(Frame.FRAME_FILE_DATA_SUCCESS);

        listener.onProgressFileTransfer(blockNumber * currentWriteBlockSize, currentWriteFileLength);
    }

    @Override
    public void onFileBlockReceiveSuccess() {
        if (isEndFile) {
            dataLink.sendFrame(new Frame(Frame.TYPE_GET_FILE_END));
            wasEndFileFrameSend = true;
            listener.onEndFileTransfer();
            return;
        }

        try {
            int currentByte = -1;
            int currentByteIndex = 0;
            while(currentByteIndex < currentReadBlockSize && (currentByte = currentReadFileStream.read()) != -1) {
                currentReadFileBuffer[currentByteIndex++] = (byte) currentByte;
            }

            dataLink.sendFrame(FrameEncoder.encodeFilePart(currentReadFileBuffer, currentReadFileBlock));
            currentReadFileBlock++;

            // End file
            if (currentByte == -1) {
                isEndFile = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        listener.onProgressFileTransfer(currentReadFileBlock * currentReadBlockSize, currentReadFileLength);
    }

    @Override
    public void onFileBlockReceiveFail() {
        if (wasEndFileFrameSend) {
            dataLink.sendFrame(new Frame(Frame.TYPE_GET_FILE_END));
            return;
        }

        try {
            int currentByte = -1;
            int currentByteIndex = 0;
            --currentReadFileBlock;
            isEndFile = false;

            long lastFilePosition = currentReadBlockSize * currentReadFileBlock;
            FileChannel fileChannel = currentReadFileStream.getChannel();
            fileChannel.position(lastFilePosition);

            while(currentByteIndex < currentReadBlockSize && (currentByte = currentReadFileStream.read()) != -1) {
                currentReadFileBuffer[currentByteIndex++] = (byte) currentByte;
            }

            dataLink.sendFrame(FrameEncoder.encodeFilePart(currentReadFileBuffer, currentReadFileBlock));
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
    public void onFileCancel() {
        listener.onEndFileTransfer();
        lastFileBlockFrame = -1;
    }

    @Override
    public void onFileReceived() {
        listener.onEndFileTransfer();
        lastFileBlockFrame = -1;
    }

    @Override
    public void onDisconnect() {
        listener.onDisconnect();
    }

    @Override
    public void onError(String description) {
        listener.onError(description);
    }
}
