// **********************************************************************************
// Title: Movie
// Author: Matthew Smith
// Course Section: CMIS202-ONL1 (Seidel) Spring 2023
// File: Movie.java
// Description: Represents a movie
// **********************************************************************************

package com.matthewsmith.medialibrary;

import java.io.Serializable;

public class Movie extends Media implements Serializable {
    private String director;
    private int duration;

    public Movie(String name) {
        super(name);
        this.director = "";
        this.duration = 0;
    }

    public Movie(String name, String genre, String description, String format,
                 int year, int yearConsumed, double rating, double[] color, String director, int duration) {
        super(name, genre, description, format, year, yearConsumed, rating, color);
        this.director = director;
        this.duration = duration;
    }

    public String getDirector() {
        return director;
    }

    public int getDuration() {
        return duration;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "Movie{" + super.toString() +
                ", director='" + director + '\'' +
                ", duration=" + duration +
                '}';
    }
}
