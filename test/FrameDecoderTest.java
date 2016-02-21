import client.ClientCallbacks;
import client.model.FileItem;
import client.protocol.DataLink;
import client.protocol.Frame;
import client.protocol.FrameDecoder;
import client.protocol.FrameEncoder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Created by nickolay on 15.02.16.
 */
public class FrameDecoderTest {
    private static List<FileItem> fileItemList;

    @BeforeClass
    public static void setUp() {
        fileItemList = new ArrayList<>();
        fileItemList.add(new FileItem("test", true));
        fileItemList.add(new FileItem("1234", false));
    }

    @Test
    public void testFileList() {
        String path = "dir";
        Frame fileListFrame = FrameEncoder.encodeFileList(0x00, fileItemList, path);
        FakeConnection connection = new FakeConnection(fileListFrame.build());

       new DataLink(connection, frame -> {
            ClientCallbacks callbacks = mock(ClientCallbacks.class);
            FrameDecoder.parseFrame(fileListFrame, callbacks);

            verify(callbacks, times(1)).onList(fileItemList, path);
        }).test();
    }
}