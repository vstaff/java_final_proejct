package com.mp3editor;

import com.mp3editor.ui.MainFrame;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Запускаем GUI в EDT-потоке (рекомендованный способ для Swing)
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
