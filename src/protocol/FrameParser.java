package protocol;

import client.ClientCallbacks;
import client.model.FileItem;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nickolay on 12.02.16.
 */
public class FrameParser {
    public static void parseFrame(Frame frame, ClientCallbacks listener) {
        byte type = frame.getType();
        switch (type) {
            case Frame.TYPE_CONNECT:
                listener.onConnect();
                return;
            case Frame.TYPE_GET_LIST_DIRECTORY:
                String directory = new String(frame.getData(), StandardCharsets.UTF_8);
                listener.onGetList(directory);
                return;
            case Frame.TYPE_LIST_DIRECTORY:
                List<FileItem> files = parseFileList(frame);
                listener.onList(files);
                return;
            case Frame.TYPE_GET_FILE:
                String path = new String(frame.getData(), StandardCharsets.UTF_8);
                listener.onGetFile(path);
                return;
            case Frame.TYPE_GET_FILE_RESPONSE:
                int status = frame.getData()[0];
                //int fileLength = frame.getData()[1];
                //listener.onFile();
                return;
            case Frame.TYPE_FILE_DATA:
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
                return;
        }
    }

    private static List<FileItem> parseFileList(Frame frame) {
        byte[] frameData = frame.getData();

        List<FileItem> files = new ArrayList<>();
        byte status = frameData[0];

        if (status != 0x00) {
            return null;
        }

        boolean isName = false;
        byte flags = 0;
        int nameStart = 0;
        for (int i = 1; i < frameData.length; ++i) {
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

        return files;
    }
}
