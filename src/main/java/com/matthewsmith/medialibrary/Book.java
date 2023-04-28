// **********************************************************************************
// Title: Book
// Author: Matthew Smith
// Course Section: CMIS202-ONL1 (Seidel) Spring 2023
// File: Book.java
// Description: Represents a book or any other literature
// **********************************************************************************

package com.matthewsmith.medialibrary;

import java.io.Serializable;

public class Book extends Media implements Serializable {
    private String author;

    public Book(String name) {
        super(name);
        this.author = "";
    }

    public Book(String name, String genre, String description, String format,
                int year, int yearConsumed, double rating, double[] color, String author) {
        super(name, genre, description, format, year, yearConsumed, rating, color);
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public double getSimilarity(Media m) {
        double initialValue = super.getSimilarity(m) * 0.85; // initial score values are worth 85%
        double authorValue = compareStrings(author.toLowerCase(),
                ((Book) m).getAuthor().toLowerCase()) * 0.15; // author is worth 15%

        return initialValue + authorValue;
    }

    @Override
    public String toString() {
        return "Book{" + super.toString() +
                ", author='" + author + '\'' +
                '}';
    }
}
