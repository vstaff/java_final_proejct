package com.mp3editor.service;

import com.mp3editor.model.MP3Metadata;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v1Tag;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;

import java.io.File;

public class MP3TagService {

    // основной публичный метод: читает все доступные теги из mp3 файла
    public MP3Metadata readMetadata(File file) throws Exception {
        // открываем mp3 файл через библиотеку mp3agic
        Mp3File mp3File = new Mp3File(file.getAbsolutePath());

        // приоритет id3v2 (современный формат, содержит обложку и больше информации)
        if (mp3File.hasId3v2Tag()) {
            // получаем теги id3v2
            ID3v2 id3v2 = mp3File.getId3v2Tag();
            // создаем объект с метаданными
            MP3Metadata metadata = new MP3Metadata();

            // извлекаем текстовые теги
            metadata.setTitle(id3v2.getTitle());
            metadata.setArtist(id3v2.getArtist());
            metadata.setAlbum(id3v2.getAlbum());
            metadata.setYear(id3v2.getYear());
            metadata.setGenre(id3v2.getGenreDescription());

            // bpm пока не поддерживается библиотекой напрямую
            metadata.setBpm(null);

            // извлекаем обложку как массив байтов
            byte[] coverImageData = id3v2.getAlbumImage();
            metadata.setCoverArt(coverImageData);
            return metadata;
        }

        // fallback на id3v1 если id3v2 отсутствует (для старых mp3 файлов)
        if (mp3File.hasId3v1Tag()) {
            // получаем теги id3v1
            ID3v1 id3v1 = mp3File.getId3v1Tag();
            MP3Metadata metadata = new MP3Metadata();

            // извлекаем базовые текстовые теги (id3v1 не поддерживает обложку)
            metadata.setTitle(id3v1.getTitle());
            metadata.setArtist(id3v1.getArtist());
            metadata.setAlbum(id3v1.getAlbum());
            metadata.setYear(id3v1.getYear());
            metadata.setGenre(id3v1.getGenreDescription());
            // id3v1 не поддерживает bpm и обложку
            metadata.setBpm(null);
            return metadata;
        }

        // если тегов нет вообще - возвращаем пустой объект
        return new MP3Metadata();
    }

    // основной публичный метод: записывает метаданные в новый mp3 файл
    public void writeMetadata(File inputFile, File outputFile, MP3Metadata metadata) throws Exception {
        // открываем исходный mp3 файл для чтения аудиоданных
        Mp3File mp3File = new Mp3File(inputFile.getAbsolutePath());

        // получаем или создаем теги id3v2 (приоритетный современный формат)
        ID3v2 id3v2Tag;
        if (mp3File.hasId3v2Tag()) {
            // используем существующие теги
            id3v2Tag = mp3File.getId3v2Tag();
        } else {
            // создаем новые теги id3v2.4
            id3v2Tag = new ID3v24Tag();
            mp3File.setId3v2Tag(id3v2Tag);
        }

        // записываем основные текстовые теги
        id3v2Tag.setTitle(metadata.getTitle());
        id3v2Tag.setArtist(metadata.getArtist());
        id3v2Tag.setAlbum(metadata.getAlbum());
        id3v2Tag.setYear(metadata.getYear());

        // жанр записываем только если указан
        if (metadata.getGenre() != null) {
            id3v2Tag.setGenreDescription(metadata.getGenre());
        }

        // bpm сохраняем в комментарий (библиотека не поддерживает прямую запись bpm)
        if (metadata.getBpm() != null) {
            String comment = id3v2Tag.getComment();
            if (comment == null) {
                comment = "";
            }
            // добавляем bpm к существующему комментарию
            comment = comment + " BPM=" + metadata.getBpm();
            id3v2Tag.setComment(comment.trim());
        }

        // обработка обложки
        if (metadata.getCoverArt() != null && metadata.getCoverArt().length > 0) {
            // определяем mime тип изображения
            String mimeType = detectMimeType(metadata.getCoverArt());
            // сохраняем обложку в тег
            id3v2Tag.setAlbumImage(metadata.getCoverArt(), mimeType);
        } else {
            // удаляем обложку если её не нужно сохранять
            id3v2Tag.clearAlbumImage();
        }

        // синхронизируем базовые теги в id3v1 для совместимости
        syncId3v1Tag(mp3File, metadata);

        // сохраняем результат в новый файл (исходный файл остается неизменным)
        mp3File.save(outputFile.getAbsolutePath());
    }

    // определяет mime тип изображения по первым байтам файла
    private String detectMimeType(byte[] imageData) {
        // слишком короткие данные считаем jpeg
        if (imageData.length < 8) return "image/jpeg";

        // jpeg начинается с ff d8
        if (imageData[0] == (byte) 0xFF && imageData[1] == (byte) 0xD8) {
            return "image/jpeg";
        }
        // png начинается с 89 50 4e 47
        else if (imageData[0] == (byte) 0x89 && imageData[1] == 'P') {
            return "image/png";
        }
        // по умолчанию jpeg
        return "image/jpeg";
    }

    // синхронизирует базовые теги id3v1 для совместимости со старыми плеерами
    private void syncId3v1Tag(Mp3File mp3File, MP3Metadata metadata) {
        ID3v1 id3v1Tag;
        if (mp3File.hasId3v1Tag()) {
            // используем существующий тег id3v1
            id3v1Tag = mp3File.getId3v1Tag();
        } else {
            // создаем новый тег id3v1
            id3v1Tag = new ID3v1Tag();
            mp3File.setId3v1Tag(id3v1Tag);
        }

        // копируем только базовые поля (id3v1 не поддерживает жанр, bpm, обложку)
        id3v1Tag.setTitle(metadata.getTitle());
        id3v1Tag.setArtist(metadata.getArtist());
        id3v1Tag.setAlbum(metadata.getAlbum());
        id3v1Tag.setYear(metadata.getYear());
    }
}
