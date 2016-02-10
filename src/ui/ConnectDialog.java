package ui;

import util.SerialUtils;

import javax.swing.*;
import java.awt.event.*;

public class ConnectDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonConnect;
    private JButton buttonCancel;
    private JComboBox baudRate;
    private JComboBox dataBits;
    private JComboBox stopBits;
    private JComboBox parity;
    private JComboBox<String> comPort;

    public ConnectDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonConnect);

        buttonConnect.addActionListener(e -> onConnect());

        buttonCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        updateComPortList();
    }

    private void updateComPortList() {
        comPort.removeAllItems();
        for(String port : SerialUtils.getAvailablePorts()) {
            comPort.addItem(port);
        }
    }

    private void onConnect() {

        dispose();
    }

    private void onCancel() {
        dispose();
    }
}
