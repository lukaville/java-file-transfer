package ui;

import client.model.FileItem;
import main.Application;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Collections;
import java.util.List;


/**
 * Created by nickolay on 10.02.16.
 */
public class MainForm {
    private static final String WINDOW_TITLE = "File transfer";

    private Icon fileIcon = new ImageIcon("res/file.png");
    private Icon folderIcon = new ImageIcon("res/folder.png");
    private Icon warningIcon = new ImageIcon("res/status_warning.png");
    private Icon okIcon = new ImageIcon("res/status_ok.png");

    private JButton connectButton;
    private JButton disconnectButton;
    private JList<FileItem> fileList;
    private JPanel panel;
    private JTextField pathTextField;
    private JButton getListButton;
    private JLabel statusLabel;
    private JProgressBar progressBar;
    private JButton waitConnectButton;
    private final Application application;

    PortParamWaitingDialog portParamWaitingDialog;

    private final DefaultListModel<FileItem> fileListModel;

    public MainForm(Application application) {
        this.application = application;

        fileList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                if (evt.getClickCount() == 2) {
                    int index = list.locationToIndex(evt.getPoint());
                    application.onFileItemClick(fileListModel.get(index), pathTextField.getText());
                }
            }
        });
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
        waitConnectButton.addActionListener(e -> openPortParamWaitingDialog());
        disconnectButton.addActionListener(e -> application.onDisconnectButton());
        getListButton.addActionListener(e -> application.onGetListButton(pathTextField.getText()));

        statusLabel.setIcon(warningIcon);
    }

    private void openPortParamWaitingDialog() {
        portParamWaitingDialog = new PortParamWaitingDialog(application);
        portParamWaitingDialog.pack();
        portParamWaitingDialog.setVisible(true);
    }

    private void openConnectDialog() {
        ConnectDialog dialog = new ConnectDialog(application);
        dialog.pack();
        dialog.setVisible(true);
    }

    public void show() {
        JFrame frame = new JFrame(WINDOW_TITLE);
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void updateFileList(List<FileItem> files, String path) {
        if (path != null) {
            pathTextField.setText(path);
        }
        Collections.sort(files);
        files.add(0, new FileItem("..", true));
        fileListModel.removeAllElements();
        files.forEach(fileListModel::addElement);
    }

    public void setStatus(boolean isOk) {
        statusLabel.setIcon(isOk ? okIcon : warningIcon);
    }

    public void openSaveFileDialog(String name, OnFileSaveDialogListener listener) {
        final JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Сохранение " + name);
        fc.setSelectedFile(new File(name));
        int returnVal = fc.showSaveDialog(panel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            listener.onFileSave(file.getAbsolutePath());
        }
    }

    public void clearProgress() {
        progressBar.setValue(0);
    }

    public void setFileTransferProgress(int current, int max) {
        progressBar.setValue(current);
        progressBar.setMaximum(max);
    }

    public void clear() {
        fileListModel.clear();
        pathTextField.setText(".");
        setFileTransferProgress(0, 0);
    }

    public void closePortParamWaitingDialog() {
        if (portParamWaitingDialog != null) {
            portParamWaitingDialog.dispose();
        }
    }

    public interface OnFileSaveDialogListener {
        void onFileSave(String localPath);
    }

    public void showAlert(String message, boolean isError) {
        if (isError) {
            JOptionPane.showMessageDialog(new Frame(), message, "Ошибка", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(new Frame(), message, "Информация", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
