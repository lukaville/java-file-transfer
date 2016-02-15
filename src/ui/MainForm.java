package ui;

import client.model.FileItem;

import javax.swing.*;
import java.util.Collections;
import java.util.List;


/**
 * Created by nickolay on 10.02.16.
 */
public class MainForm {
    private static final String WINDOW_TITLE = "File transfer";

    private Icon fileIcon = new ImageIcon("res/file.png");
    private Icon folderIcon = new ImageIcon("res/folder.png");

    private JButton connectButton;
    private JButton disconnectButton;
    private JList<FileItem> fileList;
    private JPanel panel;
    private final UiListener uiListener;

    private final DefaultListModel<FileItem> fileListModel;

    public MainForm(UiListener uiListener) {
        this.uiListener = uiListener;

        fileList.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        fileList.setCellRenderer(new IconListRenderer(item -> {
            if (((FileItem) item).isDirectory()) {
                return folderIcon;
            } else {
                return fileIcon;
            }
        }));

        fileListModel = new DefaultListModel<>();
        fileList.setModel(fileListModel);

        connectButton.addActionListener(e -> openConnectDialog());
        disconnectButton.addActionListener(e -> uiListener.onDisconnectButton());
    }

    private void openConnectDialog() {
        ConnectDialog dialog = new ConnectDialog(uiListener);
        dialog.pack();
        dialog.setVisible(true);
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

    public void updateFileList(List<FileItem> files) {
        Collections.sort(files);
        files.add(0, new FileItem("/", true));
        fileListModel.removeAllElements();
        files.forEach(fileListModel::addElement);
    }
}
