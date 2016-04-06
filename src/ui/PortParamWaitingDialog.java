package ui;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import util.SerialUtils;

import javax.swing.*;
import java.awt.event.*;

public class PortParamWaitingDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonWaitConnect;
    private JButton buttonCancel;
    private JComboBox<String> comPort;

    private UiListener uiListener;

    public PortParamWaitingDialog(UiListener uiListener) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonWaitConnect);

        buttonWaitConnect.addActionListener(e -> onWaitConnect());

        buttonCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        updateComPortList();

        this.uiListener = uiListener;
    }

    private void updateComPortList() {
        comPort.removeAllItems();
        for (String port : SerialUtils.getAvailablePorts()) {
            comPort.addItem(port);
        }
    }

    private void onWaitConnect() {
        comPort.setEnabled(false);
        buttonWaitConnect.setEnabled(false);
        try {
            uiListener.onWaitConnectButton(CommPortIdentifier.getPortIdentifier((String) comPort.getSelectedItem()));
        } catch (NoSuchPortException e) {
            e.printStackTrace();
        }
    }

    private void onCancel() {
        uiListener.onDisconnectButton();
        dispose();
    }
}
