package com.mp3editor.model;

/**
 * Простая модель для хранения метаданных трека.
 * Этот класс ничего не знает о mp3agic и файлах.
 */
public class MP3Metadata {

    private String title;
    private String artist;
    private String album;
    private String genre;
    private String year;
    private Integer bpm;

    public MP3Metadata() {
    }

    public MP3Metadata(String title,
                       String artist,
                       String album,
                       String genre,
                       String year,
                       Integer bpm) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.year = year;
        this.bpm = bpm;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public Integer getBpm() {
        return bpm;
    }

    public void setBpm(Integer bpm) {
        this.bpm = bpm;
    }

    @Override
    public String toString() {
        return "MP3Metadata{" +
                "title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", genre='" + genre + '\'' +
                ", year='" + year + '\'' +
                ", bpm=" + bpm +
                '}';
    }
}
