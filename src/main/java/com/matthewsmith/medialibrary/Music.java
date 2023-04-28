// **********************************************************************************
// Title: Music
// Author: Matthew Smith
// Course Section: CMIS202-ONL1 (Seidel) Spring 2023
// File: Music.java
// Description: Represents a song or album
// **********************************************************************************

package com.matthewsmith.medialibrary;

import java.io.Serializable;

public class Music extends Media implements Serializable {
    private String artist;

    public Music(String name) {
        super(name);
        this.artist = "";
    }

    public Music(String name, String genre, String description, String format,
                 int year, int yearConsumed, double rating, double[] color, String artist) {
        super(name, genre, description, format, year, yearConsumed, rating, color);
        this.artist = artist;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    @Override
    public double getSimilarity(Media m) {
        double initialValue = super.getSimilarity(m) * 0.85; // initial score values are worth 85%
        double artistValue = compareStrings(artist.toLowerCase(),
                ((Music) m).getArtist().toLowerCase()) * 0.15; // artist is worth 15%

        return initialValue + artistValue;
    }

    @Override
    public String toString() {
        return "Music{" + super.toString() +
                ", artist='" + artist + '\'' +
                '}';
    }
}
