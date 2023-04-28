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
    public double getSimilarity(Media m) {
        double initialValue = super.getSimilarity(m) * 0.7; // initial score values are worth 70% of score
        double creatorValue = compareStrings(creator.toLowerCase(),
                ((Show) m).getCreator().toLowerCase()) * 0.2; // creator is worth 20% of score

        // if numSeasons difference is >3, the numSeasons value is 0
        int numSeasonsDifference = Math.abs(numSeasons - ((Show) m).getNumSeasons());
        double numSeasonsValue = numSeasonsDifference > 3 ?
                0 : (1 - (numSeasonsDifference / 3.0)) * 0.05; // number of seasons is worth 5% of score

        // if numEpisodes difference is >20, numEpisodes value is 0
        int numEpisodesDifference = Math.abs(numEpisodes - ((Show) m).getNumEpisodes());
        double numEpisodesValue = numEpisodesDifference > 20 ?
                0 : (1 - (numEpisodesDifference / 20.0)) * 0.05; // number of episodes is worth 5% of score

        return initialValue + creatorValue + numSeasonsValue + numEpisodesValue;
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
