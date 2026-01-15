package com.mp3editor.ui;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import java.awt.BorderLayout;

public class MainFrame extends JFrame {

    private MP3EditorPanel editorPanel;

    public MainFrame() {
        super("MP3 Tag Editor");

        // базовые настройки окна
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null); // центрируем окно

        // создаём основную панель редактора
        editorPanel = new MP3EditorPanel();
        setLayout(new BorderLayout());
        add(editorPanel, BorderLayout.CENTER);

        // создаём простое меню (File -> Exit)
        setJMenuBar(createMenuBar());
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        return menuBar;
    }
}
