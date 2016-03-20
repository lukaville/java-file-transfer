package client.protocol;

import client.FileTransferClient;
import client.model.FileItem;
import util.ByteUtils;
import util.HammingUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Created by nickolay on 12.02.16.
 */
public class FrameEncoder {
    public static Frame encodeFileList(int status, List<FileItem> files, String path) {
        if (status != FileTransferClient.STATUS_OK) {
            return new Frame(Frame.TYPE_LIST_DIRECTORY, new byte[] {(byte) status});
        }

        // TODO: optimize array allocation
        int frameDataLength = 1;
        frameDataLength += path.getBytes(StandardCharsets.UTF_8).length + 2;
        for (FileItem file : files) {
            frameDataLength += file.getName().getBytes(StandardCharsets.UTF_8).length + 3;
        }
        byte[] frameData = new byte[frameDataLength];
        frameData[0] = FileTransferClient.STATUS_OK;

        int currentIndex = 1;
        currentIndex += writeString(frameData, path, currentIndex);

        for(FileItem file : files) {
            frameData[currentIndex] = (byte) (file.isDirectory() ? 0x01 : 0x00); // flags
            currentIndex++;
            currentIndex += writeString(frameData, file.getName(), currentIndex);
        }

        return new Frame(Frame.TYPE_LIST_DIRECTORY, frameData);
    }

    public static Frame encodeGetFile(String remotePath) {
        return new Frame(Frame.TYPE_GET_FILE, remotePath.getBytes(StandardCharsets.UTF_8));
    }

    public static Frame encodeGetFileResponse(int status, int fileLength, int blockSize) {
        byte[] frameData = new byte[1 + 4 + 4];
        frameData[0] = (byte) status;

        ByteUtils.intToBytes(frameData, fileLength, 1);
        ByteUtils.intToBytes(frameData, blockSize, 5);

        return new Frame(Frame.TYPE_GET_FILE_RESPONSE, frameData);
    }
    
    public static Frame encodeFilePart(byte[] block, int blockNumber) {
        byte[] encodedBlock = HammingUtils.cycleEncode(block);
        byte[] frameData = new byte[encodedBlock.length + 4];
        ByteUtils.intToBytes(frameData, blockNumber, 0);
        System.arraycopy(encodedBlock, 0, frameData, 4, encodedBlock.length);
        return new Frame(Frame.TYPE_FILE_DATA, frameData);
    }

    private static int writeString(byte[] arr, String str, int offset) {
        byte[] nameBytes = str.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(nameBytes, 0, arr, offset, nameBytes.length);
        arr[offset + nameBytes.length] = (byte) 0x00; // zero-symbol
        arr[offset + nameBytes.length + 1] = (byte) 0x00; // zero-symbol
        return nameBytes.length + 2;
    }
}
