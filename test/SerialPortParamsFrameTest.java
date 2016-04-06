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
public class SerialPortParamsFrameTest {
    @Test
    public void testEncodeDecode() {
        Frame frame = FrameEncoder.encodeSerialPortSettings(500, 8, 1, 2);

        ClientCallbacks callbacks = mock(ClientCallbacks.class);
        FrameDecoder.parseFrame(frame, callbacks);

        verify(callbacks, times(1)).onSetSerialPort(500, 8, 1, 2);
    }
}