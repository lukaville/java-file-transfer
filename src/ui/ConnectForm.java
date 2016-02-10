package ui;

import gnu.io.CommPortIdentifier;

import javax.swing.*;

/**
 * Created by nickolay on 10.02.16.
 */
public class ConnectForm {
    private JPanel panel;
    private JComboBox baudRate;
    private JComboBox dataBits;
    private JComboBox stopBits;
    private JComboBox parity;
    private JComboBox<CommPortIdentifier> comPort;

    public JPanel getPanel() {
        return panel;
    }

    public JComboBox getBaudRate() {
        return baudRate;
    }

    public JComboBox getDataBits() {
        return dataBits;
    }

    public JComboBox getStopBits() {
        return stopBits;
    }

    public JComboBox getParity() {
        return parity;
    }

    public JComboBox<CommPortIdentifier> getComPort() {
        return comPort;
    }
}
