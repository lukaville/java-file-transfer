package protocol;

import client.model.FileItem;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Created by nickolay on 12.02.16.
 */
public class FrameEncoder {
    public static Frame encodeFileList(int status, List<FileItem> files, String path) {
        if (status != 0x00) {
            return new Frame(Frame.TYPE_LIST_DIRECTORY, new byte[] {(byte) status});
        }

        // TODO: optimize array allocation
        int frameDataLength = 1;
        frameDataLength += path.getBytes(StandardCharsets.UTF_8).length + 2;
        for (FileItem file : files) {
            frameDataLength += file.getName().getBytes(StandardCharsets.UTF_8).length + 3;
        }
        byte[] frameData = new byte[frameDataLength];
        frameData[0] = (byte) 0x00; // status

        int currentIndex = 1;
        currentIndex += writeString(frameData, path, currentIndex);

        for(FileItem file : files) {
            frameData[currentIndex] = (byte) (file.isDirectory() ? 0x01 : 0x00); // flags
            currentIndex++;
            currentIndex += writeString(frameData, file.getName(), currentIndex);
        }

        return new Frame(Frame.TYPE_LIST_DIRECTORY, frameData);
    }

    public static int writeString(byte[] arr, String str, int offset) {
        byte[] nameBytes = str.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(nameBytes, 0, arr, offset, nameBytes.length);
        arr[offset + nameBytes.length] = (byte) 0x00; // zero-symbol
        arr[offset + nameBytes.length + 1] = (byte) 0x00; // zero-symbol
        return nameBytes.length + 2;
    }
}
