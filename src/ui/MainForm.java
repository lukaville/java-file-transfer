package ui;

import javax.swing.*;

/**
 * Created by nickolay on 10.02.16.
 */
public class MainForm {
    private JButton connectButton;
    private JButton disconnectButton;
    private JList fileList;
    private JPanel panel;

    public MainForm() {
        connectButton.addActionListener(e -> {
            openConnectDialog();
        });
    }

    private void openConnectDialog() {
        ConnectDialog dialog = new ConnectDialog();
        dialog.pack();
        dialog.setVisible(true);
    }

    public JPanel getPanel() {
        return panel;
    }
}
