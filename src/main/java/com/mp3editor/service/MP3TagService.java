package com.mp3editor.service;

import com.mp3editor.model.MP3Metadata;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v1Tag;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;

import java.io.File;

/**
 * Сервис для чтения и записи тегов mp3 файлов с использованием библиотеки mp3agic.
 */
public class MP3TagService {

    /**
     * Читает метаданные из mp3 файла.
     *
     * @param file mp3 файл
     * @return MP3Metadata с заполненными полями
     * @throws Exception если файл нельзя прочитать или это не mp3
     */
    public MP3Metadata readMetadata(File file) throws Exception {
        Mp3File mp3File = new Mp3File(file.getAbsolutePath());

        // Сначала пробуем ID3v2 (более современный и полный)
        if (mp3File.hasId3v2Tag()) {
            ID3v2 id3v2 = mp3File.getId3v2Tag();
            MP3Metadata metadata = new MP3Metadata();
            metadata.setTitle(id3v2.getTitle());
            metadata.setArtist(id3v2.getArtist());
            metadata.setAlbum(id3v2.getAlbum());
            metadata.setYear(id3v2.getYear());
            metadata.setGenre(id3v2.getGenreDescription()); // строковое описание жанра
            // BPM в ID3v2 обычно хранится в T*** кадре; mp3agic даёт getBPM только в некоторых версиях
            try {
                // В mp3agic нет отдельного метода, поэтому оставим BPM null в простом варианте
                metadata.setBpm(null);
            } catch (Exception ignored) {
                metadata.setBpm(null);
            }
            byte[] coverImageData = id3v2.getAlbumImage();
            metadata.setCoverArt(coverImageData);
            return metadata;
        }

        // Если ID3v2 нет, пробуем ID3v1
        if (mp3File.hasId3v1Tag()) {
            ID3v1 id3v1 = mp3File.getId3v1Tag();
            MP3Metadata metadata = new MP3Metadata();
            metadata.setTitle(id3v1.getTitle());
            metadata.setArtist(id3v1.getArtist());
            metadata.setAlbum(id3v1.getAlbum());
            metadata.setYear(id3v1.getYear());
            metadata.setGenre(id3v1.getGenreDescription());
            metadata.setBpm(null); // ID3v1 не поддерживает BPM
            return metadata;
        }

        // Если тегов нет вообще — возвращаем пустой объект
        return new MP3Metadata();
    }

    /**
     * Записывает метаданные в mp3 файл.
     * Важно: mp3agic при сохранении создаёт новый файл, старый остаётся.
     *
     * @param inputFile  исходный mp3
     * @param outputFile выходной mp3 с обновлёнными тегами
     * @param metadata   метаданные для записи
     * @throws Exception при ошибке записи/чтения
     */
    public void writeMetadata(File inputFile, File outputFile, MP3Metadata metadata) throws Exception {
        Mp3File mp3File = new Mp3File(inputFile.getAbsolutePath());

        ID3v2 id3v2Tag;
        if (mp3File.hasId3v2Tag()) {
            id3v2Tag = mp3File.getId3v2Tag();
        } else {
            // Если ID3v2 ещё нет — создаём новый тег
            id3v2Tag = new ID3v24Tag();
            mp3File.setId3v2Tag(id3v2Tag);
        }

        // Заполняем основные поля
        id3v2Tag.setTitle(metadata.getTitle());
        id3v2Tag.setArtist(metadata.getArtist());
        id3v2Tag.setAlbum(metadata.getAlbum());
        id3v2Tag.setYear(metadata.getYear());

        // Жанр: у mp3agic жанр как номер + описание; тут проще записать description как "custom" жанр
        if (metadata.getGenre() != null) {
            // Библиотека ожидает индекс жанра, поэтому безопаснее не трогать числовой жанр,
            // а положить строку в комментарий или TCON. Для простоты пропустим установку числового жанра.
            id3v2Tag.setGenreDescription(metadata.getGenre());
        }

        // BPM: в mp3agic нет прямого сеттера BPM для всех версий, поэтому для MVP можно пропустить
        // или добавить в комментарий.
        if (metadata.getBpm() != null) {
            String comment = id3v2Tag.getComment();
            if (comment == null) {
                comment = "";
            }
            comment = comment + " BPM=" + metadata.getBpm();
            id3v2Tag.setComment(comment.trim());
        }

        // В методе writeMetadata() добавь перед сохранением:
        if (metadata.getCoverArt() != null && metadata.getCoverArt().length > 0) {
            String mimeType = detectMimeType(metadata.getCoverArt());
            id3v2Tag.setAlbumImage(metadata.getCoverArt(), mimeType);
        } else {
            // Удаляем обложку если её нет
            id3v2Tag.clearAlbumImage();
        }

        // Дополнительно синхронизируем ID3v1 (если нужен)
        syncId3v1Tag(mp3File, metadata);

        // Сохраняем в новый файл
        mp3File.save(outputFile.getAbsolutePath());
    }

    private String detectMimeType(byte[] imageData) {
        if (imageData.length < 8) return "image/jpeg";

        // Простое определение по сигнатуре
        if (imageData[0] == (byte) 0xFF && imageData[1] == (byte) 0xD8) {
            return "image/jpeg";
        } else if (imageData[0] == (byte) 0x89 && imageData[1] == 'P') {
            return "image/png";
        }
        return "image/jpeg"; // по умолчанию
    }


    /**
     * Создаёт или обновляет ID3v1 тег, чтобы базовая информация была и в старом формате.
     */
    private void syncId3v1Tag(Mp3File mp3File, MP3Metadata metadata) {
        ID3v1 id3v1Tag;
        if (mp3File.hasId3v1Tag()) {
            id3v1Tag = mp3File.getId3v1Tag();
        } else {
            id3v1Tag = new ID3v1Tag();
            mp3File.setId3v1Tag(id3v1Tag);
        }

        id3v1Tag.setTitle(metadata.getTitle());
        id3v1Tag.setArtist(metadata.getArtist());
        id3v1Tag.setAlbum(metadata.getAlbum());
        id3v1Tag.setYear(metadata.getYear());
        // Жанр в ID3v1 — только предопределённый список, поэтому оставим как есть
    }
}
