package main;

import network.ConnectListener;
import org.apache.commons.io.IOUtils;
import protocol.FileTransferClient;
import network.NetworkConnection;
import protocol.Frame;
import protocol.FrameListener;
import ui.MainForm;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Created by nickolay on 10.02.16.
 */
public class Application implements ConnectListener, FrameListener, MainForm.MainFormListener {
    private final MainForm mainForm;
    private FileTransferClient client;

    public Application() {
        mainForm = new MainForm(this, this);
    }

    public void start() {
        mainForm.show();
    }

    @Override
    public void onConnect(NetworkConnection connection) {
        if (client != null) {
            client.disconnect();
        }
        client = new FileTransferClient(connection);
        client.setListener(this);
        client.start();
    }

    @Override
    public void onFrameReceived(Frame frame, FileTransferClient client) {
        System.out.println("Frame received: " + new String(frame.getData(), StandardCharsets.UTF_8));
    }

    @Override
    public void onDisconnectButton() {
        String data = "hello";
        try {
            client.sendFrame(new Frame(Frame.TYPE_FILE_DATA, data.getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
