// **********************************************************************************
// Title: Media
// Author: Matthew Smith
// Course Section: CMIS202-ONL1 (Seidel) Spring 2023
// File: Media.java
// Description: Parent class of the media types; contains universal attributes of
//              common media
// **********************************************************************************

package com.matthewsmith.medialibrary;

import javafx.scene.paint.Color;

import java.io.Serializable;
import java.time.Year;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

public class Media implements Serializable, Cloneable {
    private String name;
    private String genre;
    private String description;
    private String format;
    private int year;
    private int yearConsumed;
    private double rating;
    private double[] color; // R, G, B, and opacity values
    private Date dateAdded = new Date();

    public Media(String name) {
        this(name, "", "", "", Year.now().getValue(), Year.now().getValue(), 0, new double[4]);
    }

    public Media(String name, String genre, String description, String format, int year, int yearConsumed, double rating, double[] color) {
        this.name = name;
        this.genre = genre;
        this.description = description;
        this.format = format;
        this.year = year;
        this.yearConsumed = yearConsumed;
        this.rating = rating;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getGenre() {
        return genre;
    }

    public String getDescription() {
        return description;
    }

    public String getFormat() {
        return format;
    }

    public int getYear() {
        return year;
    }

    public int getYearConsumed() {
        return yearConsumed;
    }

    public double getRating() {
        return rating;
    }

    public double[] getColorArray() {
        return color;
    }

    public Color getColor() {
        return new Color(color[0], color[1], color[2], color[3]);
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setYearConsumed(int yearConsumed) {
        this.yearConsumed = yearConsumed;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setColorArray(double[] color) {
        this.color = color;
    }

    public void setColor(Color color) {
        double[] colorArray = {color.getRed(), color.getGreen(), color.getBlue(), color.getOpacity()};
        this.setColorArray(colorArray);
    }

    public static boolean validateRating(double rating) {
        return (rating < 0 || rating > 10);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Media media = (Media) o;
        return year == media.year &&
                yearConsumed == media.yearConsumed &&
                Objects.equals(name, media.name) &&
                Objects.equals(genre, media.genre) &&
                Objects.equals(description, media.description) &&
                Objects.equals(format, media.format);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, genre, description, format, year, yearConsumed);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return  "name='" + name + '\'' +
                ", genre='" + genre + '\'' +
                ", description='" + description + '\'' +
                ", format='" + format + '\'' +
                ", year=" + year +
                ", yearConsumed=" + yearConsumed +
                ", rating=" + rating +
                ", color=" + Arrays.toString(color) +
                ", dateAdded=" + dateAdded;
    }
}
