package com.example.music.service.model;

import java.util.List;
import java.util.Set;

public class Band {
    private String bandName;

    private String genre;

    private Set<String> albums;

    private Band(String band, String genre, Set<String> albums) {
        this.bandName = band;
        this.genre = genre;
        this.albums = albums;
    }

    public static Band of(String band, String genre, Set<String> albums) {
        return new Band(band,genre,albums);
    }

    @Override
    public String toString() {
        return "Band{" +
                "bandName='" + bandName + '\'' +
                ", genre='" + genre + '\'' +
                ", albums=" + albums +
                '}';
    }
}
