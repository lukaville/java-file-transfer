import client.ClientCallbacks;
import client.model.FileItem;
import org.junit.BeforeClass;
import org.junit.Test;
import protocol.*;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Created by nickolay on 15.02.16.
 */
public class FrameParserTest {
    private static List<FileItem> fileItemList;

    @BeforeClass
    public static void setUp() {
        fileItemList = new ArrayList<>();
        fileItemList.add(new FileItem("test", true));
        fileItemList.add(new FileItem("thedirectory", true));
        fileItemList.add(new FileItem("123", false));
        fileItemList.add(new FileItem("one.pdf", false));
    }

    @Test
    public void testFileList() {
        String path = "/user/rdtfyguhijmk/hbunj/ouytrf";
        Frame fileListFrame = FrameEncoder.encodeFileList(0x00, fileItemList, path);
        FakeConnection connection = new FakeConnection(fileListFrame.build());

       new FileTransferConnection(connection, frame -> {
            ClientCallbacks callbacks = mock(ClientCallbacks.class);
            FrameParser.parseFrame(fileListFrame, callbacks);

            verify(callbacks, times(1)).onList(fileItemList, path);
        }).run();
    }
}