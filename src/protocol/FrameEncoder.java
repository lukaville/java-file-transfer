package protocol;

import client.model.FileItem;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Created by nickolay on 12.02.16.
 */
public class FrameEncoder {
    public static Frame encodeFileList(int status, List<FileItem> files) {
        if (status != 0x00) {
            return new Frame(Frame.TYPE_LIST_DIRECTORY, new byte[] {(byte) status});
        }

        // TODO: optimize array allocation
        int frameDataLength = 1;
        for (FileItem file : files) {
            frameDataLength += file.getName().getBytes(StandardCharsets.UTF_8).length + 3;
        }
        byte[] frameData = new byte[frameDataLength];
        frameData[0] = (byte) 0x00; // status

        int currentIndex = 1;
        for(FileItem file : files) {
            frameData[currentIndex] = (byte) (file.isDirectory() ? 0x01 : 0x00); // flags
            currentIndex++;

            byte[] nameBytes = file.getName().getBytes(StandardCharsets.UTF_8);
            System.arraycopy(nameBytes, 0, frameData, currentIndex, nameBytes.length);
            currentIndex += nameBytes.length;
            frameData[currentIndex] = (byte) 0x00; // zero-symbol
            frameData[currentIndex + 1] = (byte) 0x00; // zero-symbol
            currentIndex += 2;
        }

        return new Frame(Frame.TYPE_LIST_DIRECTORY, frameData);
    }
}
