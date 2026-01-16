package com.mp3editor.ui;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import java.awt.BorderLayout;

public class MainFrame extends JFrame {

    // основная панель редактирования mp3 тегов
    private MP3EditorPanel editorPanel;

    public MainFrame() {
        // устанавливаем заголовок главного окна приложения
        super("MP3 Tag Editor");

        // при закрытии окна завершаем работу приложения
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // задаем стартовый размер окна: 900x600 пикселей
        setSize(900, 600);

        // центрируем окно на экране
        setLocationRelativeTo(null);

        // создаем основную панель с интерфейсом редактирования
        editorPanel = new MP3EditorPanel();

        // задаем layout для главного окна
        setLayout(new BorderLayout());

        // размещаем панель редактирования по центру окна
        add(editorPanel, BorderLayout.CENTER);

        // добавляем меню в верхнюю часть окна
        setJMenuBar(createMenuBar());
    }

    // создает и настраивает главное меню приложения
    private JMenuBar createMenuBar() {
        // создаем строку меню
        JMenuBar menuBar = new JMenuBar();

        // создаем меню "File" (файл)
        JMenu fileMenu = new JMenu("File");

        // пункт меню "Exit" (выход)
        JMenuItem exitItem = new JMenuItem("Exit");

        // при нажатии на "Exit" завершаем работу приложения
        exitItem.addActionListener(e -> System.exit(0));

        // добавляем пункт "Exit" в меню "File"
        fileMenu.add(exitItem);

        // добавляем меню "File" в строку меню
        menuBar.add(fileMenu);

        return menuBar;
    }
}
