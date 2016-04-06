package ui;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.SerialPort;
import util.SerialUtils;

import javax.swing.*;
import java.awt.event.*;

public class ConnectDialog extends JDialog {
    public static final String PARITY_NONE = "отсутствует";
    public static final String PARITY_ODD = "дополнение до нечетности";
    public static final String PARITY_EVEN = "дополнение до четности";
    public static final String PARITY_MARK = "всегда 1";
    public static final String PARITY_SPACE = "всегда 0";

    public static final String STOP_BITS_1 = "один";
    public static final String STOP_BITS_1_5 = "полтора";
    public static final String STOP_BITS_2 = "два";

    private JPanel contentPane;
    private JButton buttonConnect;
    private JButton buttonCancel;
    private JComboBox<Integer> baudRate;
    private JComboBox<Integer> dataBits;
    private JComboBox<String> stopBits;
    private JComboBox<String> parity;
    private JComboBox<String> comPort;
    private JButton buttonSendParams;
    private UiListener uiListener;

    public ConnectDialog(UiListener uiListener) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonConnect);

        buttonConnect.addActionListener(e -> onConnect());
        buttonSendParams.addActionListener(e -> onSendParams());
        buttonCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        updateComPortList();
        setDataBits();
        setBaudRate();
        setStopBits();
        setParity();

        this.uiListener = uiListener;
    }

    private void onSendParams() {
        try {
            uiListener.onWaitConnectButton(CommPortIdentifier.getPortIdentifier((String) comPort.getSelectedItem()));
        } catch (NoSuchPortException e) {
            e.printStackTrace();
        }
        uiListener.onDisconnectButton();
        onConnect();
    }

    private void updateComPortList() {
        comPort.removeAllItems();
        for (String port : SerialUtils.getAvailablePorts()) {
            comPort.addItem(port);
        }
    }

    private void setBaudRate() {
        baudRate.addItem(50);
        baudRate.addItem(75);
        baudRate.addItem(150);
        baudRate.addItem(300);
        baudRate.addItem(600);
        baudRate.addItem(1200);
        baudRate.addItem(2400);
        baudRate.addItem(4800);
        baudRate.addItem(9600);
        baudRate.addItem(19200);
        baudRate.addItem(38400);
        baudRate.addItem(57600);
        baudRate.addItem(115200);
    }

    private void setDataBits() {
        dataBits.addItem(SerialPort.DATABITS_5);
        dataBits.addItem(SerialPort.DATABITS_6);
        dataBits.addItem(SerialPort.DATABITS_7);
        dataBits.addItem(SerialPort.DATABITS_8);
    }

    private void setStopBits() {
        stopBits.addItem(STOP_BITS_1);
        stopBits.addItem(STOP_BITS_1_5);
        stopBits.addItem(STOP_BITS_2);
    }

    private void setParity() {
        parity.addItem(PARITY_NONE);
        parity.addItem(PARITY_EVEN);
        parity.addItem(PARITY_ODD);
        parity.addItem(PARITY_MARK);
        parity.addItem(PARITY_SPACE);
    }

    private void onConnect() {
        try {
            uiListener.onConnectButton(
                    CommPortIdentifier.getPortIdentifier((String) comPort.getSelectedItem()),
                    (Integer) baudRate.getSelectedItem(),
                    (Integer) dataBits.getSelectedItem(),
                    getStopBits((String) stopBits.getSelectedItem()),
                    getParity((String) parity.getSelectedItem())
            );
            dispose();
        } catch (NoSuchPortException e) {
            e.printStackTrace();
        }
    }

    private void onCancel() {
        dispose();
    }

    private int getStopBits(String stopBits) {
        switch (stopBits) {
            case STOP_BITS_1:
                return SerialPort.STOPBITS_1;
            case STOP_BITS_1_5:
                return SerialPort.STOPBITS_1_5;
            case STOP_BITS_2:
                return SerialPort.STOPBITS_2;
            default:
                return 0;
        }
    }

    private int getParity(String parity) {
        switch (parity) {
            case PARITY_EVEN:
                return SerialPort.PARITY_EVEN;
            case PARITY_MARK:
                return SerialPort.PARITY_MARK;
            case PARITY_NONE:
                return SerialPort.PARITY_NONE;
            case PARITY_ODD:
                return SerialPort.PARITY_ODD;
            case PARITY_SPACE:
                return SerialPort.PARITY_SPACE;
            default:
                return 0;
        }
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
