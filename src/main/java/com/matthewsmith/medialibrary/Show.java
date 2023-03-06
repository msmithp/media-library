// **********************************************************************************
// Title: Show
// Author: Matthew Smith
// Course Section: CMIS202-ONL1 (Seidel) Spring 2023
// File: Show.java
// Description: Represents a television show
// **********************************************************************************

package com.matthewsmith.medialibrary;

import java.io.Serializable;

public class Show extends Media implements Serializable {
    private String creator;
    private int numSeasons;
    private int numEpisodes;

    public Show(String name) {
        super(name);
        this.creator = "";
        this.numSeasons = 0;
        this.numEpisodes = 0;
    }

    public Show(String name, String genre, String description, String format,
                int year, int yearConsumed, double rating, double[] color, String creator, int numSeasons, int numEpisodes) {
        super(name, genre, description, format, year, yearConsumed, rating, color);
        this.creator = creator;
        this.numSeasons = numSeasons;
        this.numEpisodes = numEpisodes;
    }

    public String getCreator() {
        return creator;
    }

    public int getNumSeasons() {
        return numSeasons;
    }

    public int getNumEpisodes() {
        return numEpisodes;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setNumSeasons(int numSeasons) {
        this.numSeasons = numSeasons;
    }

    public void setNumEpisodes(int numEpisodes) {
        this.numEpisodes = numEpisodes;
    }

    @Override
    public String toString() {
        return "Show{" + super.toString() +
                ", creator='" + creator + '\'' +
                ", numSeasons=" + numSeasons +
                ", numEpisodes=" + numEpisodes +
                '}';
    }
}
