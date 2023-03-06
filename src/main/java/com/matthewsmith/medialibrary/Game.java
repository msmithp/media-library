// **********************************************************************************
// Title: Game
// Author: Matthew Smith
// Course Section: CMIS202-ONL1 (Seidel) Spring 2023
// File: Game.java
// Description: Represents a video game
// **********************************************************************************

package com.matthewsmith.medialibrary;

import java.io.Serializable;

public class Game extends Media implements Serializable {
    private String developer;
    private String console;
    private int numPlayers;

    public Game(String name) {
        super(name);
        this.developer = "";
        this.console = "";
        this.numPlayers = 1;
    }

    public Game(String name, String genre, String description, String format,
                int year, int yearConsumed, double rating, double[] color, String developer, String console, int numPlayers) {
        super(name, genre, description, format, year, yearConsumed, rating, color);
        this.developer = developer;
        this.console = console;
        this.numPlayers = numPlayers;
    }

    public String getDeveloper() {
        return developer;
    }

    public String getConsole() {
        return console;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public void setConsole(String console) {
        this.console = console;
    }

    public void setNumPlayers(int numPlayers) {
        this.numPlayers = numPlayers;
    }

    @Override
    public String toString() {
        return "Game{" + super.toString() +
                ", developer='" + developer + '\'' +
                ", console='" + console + '\'' +
                ", numPlayers=" + numPlayers +
                '}';
    }
}
