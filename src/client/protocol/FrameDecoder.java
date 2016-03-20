package client.protocol;

import client.ClientCallbacks;
import client.FileTransferClient;
import client.model.FileItem;
import util.ByteUtils;
import util.HammingUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nickolay on 12.02.16.
 */
public class FrameDecoder {
    public static void parseFrame(Frame frame, ClientCallbacks listener) {
        byte type = frame.getType();
        switch (type) {
            case Frame.TYPE_CONNECT:
                listener.onConnect();
                return;
            case Frame.TYPE_GET_LIST_DIRECTORY:
                String directory = new String(frame.getData(), StandardCharsets.UTF_8);
                System.out.println("Directory: " + directory);
                listener.onGetList(directory);
                return;
            case Frame.TYPE_LIST_DIRECTORY:
                parseFileList(frame, listener);
                return;
            case Frame.TYPE_GET_FILE:
                String path = new String(frame.getData(), StandardCharsets.UTF_8);
                System.out.println("Path: " + path);
                listener.onGetFile(path);
                return;
            case Frame.TYPE_GET_FILE_RESPONSE:
                int status = frame.getData()[0];
                int fileLength = ByteUtils.bytesToInt(frame.getData(), 1);
                int blockSize = ByteUtils.bytesToInt(frame.getData(), 5);
                System.out.printf("Status: %d. File length: %d. Block size: %d.%n", status, fileLength, blockSize);
                listener.onFile(status, fileLength, blockSize);
                return;
            case Frame.TYPE_FILE_DATA:
                int blockNumber = ByteUtils.bytesToInt(frame.getData(), 0);
                blockSize = frame.getData().length - 4;
                byte[] blockBytes = new byte[blockSize];
                System.arraycopy(frame.getData(), 4, blockBytes, 0, blockSize);
                byte[] decoded = HammingUtils.cycleDecode(blockBytes);
                System.out.printf("Block number: %d. Decoded data length: %d%n", blockNumber, decoded == null ? 0 : decoded.length);
                listener.onFileBlock(blockNumber, decoded);
                return;
            case Frame.TYPE_FILE_DATA_SUCCESS:
                listener.onFileBlockReceiveSuccess();
                return;
            case Frame.TYPE_FILE_DATA_RETRY:
                listener.onFileBlockReceiveFail();
                return;
            case Frame.TYPE_FILE_DATA_CANCEL:
                listener.onFileCancel();
                return;
            case Frame.TYPE_GET_FILE_END:
                listener.onFileReceived();
                return;
            case Frame.TYPE_DISCONNECT:
                listener.onDisconnect();
        }
    }

    private static void parseFileList(Frame frame, ClientCallbacks listener) {
        byte[] frameData = frame.getData();

        List<FileItem> files = new ArrayList<>();
        String path = "";
        byte status = frameData[0];

        if (status != FileTransferClient.STATUS_OK) {
            listener.onList(files, null);
            if (status == FileTransferClient.STATUS_DIRECTORY_NOT_EXIST) {
                listener.onError("Директория не существует");
            }
            return;
        }

        int i = 1;
        for (; i < 8192; ++i) {
            if (frameData[i] == 0x00 && frameData[i - 1] == 0x00) {
                path = new String(frameData, 1, i - 2, StandardCharsets.UTF_8);
                break;
            }
        }

        boolean isName = false;
        byte flags = 0;
        int nameStart = 0;
        for (++i; i < frameData.length; ++i) {
            if (!isName) {
                flags = frameData[i];
                isName = true;
                nameStart = i + 1;
            } else {
                if (frameData[i] == 0x00 && frameData[i - 1] == 0x00) {
                    String name = new String(frameData, nameStart, i - nameStart - 1, StandardCharsets.UTF_8);
                    files.add(new FileItem(name, flags == 0x01));
                    isName = false;
                }
            }
        }

        System.out.println("File list length: " + files.size());
        listener.onList(files, path);
    }
}
