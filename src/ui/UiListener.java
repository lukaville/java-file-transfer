package ui;

import gnu.io.CommPortIdentifier;

public interface UiListener {
    void onDisconnectButton();
    void onConnectButton(CommPortIdentifier port, int baudRate, int dataBits, int stopBits, int parity);
}