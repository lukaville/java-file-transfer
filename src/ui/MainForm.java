package ui;

import network.ConnectListener;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Created by nickolay on 10.02.16.
 */
public class MainForm {
    private static final String WINDOW_TITLE = "File transfer";

    private JButton connectButton;
    private JButton disconnectButton;
    private JList fileList;
    private JPanel panel;
    private final ConnectListener connectListener;
    private final MainFormListener mainFormListener;

    public interface MainFormListener {
        void onDisconnectButton();
    }

    public MainForm(ConnectListener connectListener, MainFormListener mainFormListener) {
        this.connectListener = connectListener;
        this.mainFormListener = mainFormListener;

        connectButton.addActionListener(e -> openConnectDialog());
        disconnectButton.addActionListener(e -> mainFormListener.onDisconnectButton());
    }

    private void openConnectDialog() {
        ConnectDialog dialog = new ConnectDialog(connectListener);
        dialog.pack();
        dialog.setVisible(true);
    }

    public JPanel getPanel() {
        return panel;
    }

    public void show() {
        JFrame frame = new JFrame(WINDOW_TITLE);
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(400, 400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
