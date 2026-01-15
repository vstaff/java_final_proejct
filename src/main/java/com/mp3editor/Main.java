package com.mp3editor;

import com.mpatric.mp3agic.Mp3File;

public class Main {
    public static void main(String[] args) {
        System.out.println("✅ MP3 Tag Editor запущен!");
        System.out.println("✅ mp3agic подключена корректно");
        System.out.println("Версия: " + Mp3File.class.getPackage().getImplementationVersion());
    }
}
