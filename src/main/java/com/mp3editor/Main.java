package com.mp3editor;

import com.mp3editor.model.MP3Metadata;
import com.mp3editor.service.MP3TagService;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("MP3 Tag Editor test start");

        MP3TagService service = new MP3TagService();

        // Укажи путь к тестовому mp3 файлу
        File input = new File("C:\\Users\\vstaffcheck\\Desktop\\garden mansion - IИTERNAT10NAL.mp3");
        File output = new File("C:\\Users\\vstaffcheck\\Desktop\\garden mansion - IИTERNAT10NAL_output.mp3");

        // Чтение
        MP3Metadata metadata = service.readMetadata(input);
        System.out.println("Before: " + metadata);

        // Изменение (для теста)
        metadata.setTitle("аристис");
        metadata.setArtist("новое название");

        // Запись
        service.writeMetadata(input, output, metadata);

        // Проверяем что в новом файле теги поменялись
        MP3Metadata metadataAfter = service.readMetadata(output);
        System.out.println("After: " + metadataAfter);
    }
}
