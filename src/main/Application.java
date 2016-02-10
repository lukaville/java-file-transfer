package main;

import ui.MainForm;

import javax.swing.*;

/**
 * Created by nickolay on 10.02.16.
 */
public class Application {
    private static final String WINDOW_TITLE = "File transfer";
    private final MainForm mainForm;

    public Application() {
        mainForm = new MainForm();
    }

    public void start() {
        JFrame frame = new JFrame(WINDOW_TITLE);
        frame.setContentPane(mainForm.getPanel());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(400, 400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
