// **********************************************************************************
// Title: Library
// Author: Matthew Smith
// Course Section: CMIS202-ONL1 (Seidel) Spring 2023
// File: Library.java
// Description: Stores items and manages file input/output for MediaLibrary
// **********************************************************************************

package com.matthewsmith.medialibrary;

import java.io.*;
import java.util.*;

public class Library<E> implements Iterable<E> {
    private ArrayList<E> media;
    private HashMap<E, String> groups;
    private File file;

    /** Constructors */
    public Library() {
        this(new ArrayList<>());
    }

    public Library(ArrayList<E> media) {
        this.media = media;
        this.file = new File("library.dat");

        try {
            file.createNewFile();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        this.groups = new HashMap<>();
    }

    public Library(ArrayList<E> media, HashMap<E, String> groups, File file) {
        this.media = media;
        this.groups = groups;
        this.file = file;
    }

    /** Returns the list of media */
    public ArrayList<E> getMedia() {
        return media;
    }

    /** Returns the list of groups */
    public HashMap<E, String> getGroups() {
        return groups;
    }

    /** Returns the file */
    public File getFile() {
        return file;
    }

    /** Returns the size of the library */
    public int getSize() {
        return media.size();
    }

    /** Adds an element to the library */
    public void add(E e) {
        this.media.add(e);
        write();
    }

    /** Removes an element from the library */
    public void remove(E e) {
        this.media.remove(e);
        groups.remove(e);
        write();
    }

    /** Clears media list and groups map */
    public void clear() {
        media.clear();
        groups.clear();
    }

    /** Recursive quick sort - O(n log n) time complexity */
    public void sort(Comparator<E> c) {
        sort(c, 0, media.size() - 1);
    }

    /** Quick sort helper method */
    private void sort(Comparator<E> c, int first, int last) {
        if (last > first) {
            int pivot = partition(c, first, last);
            sort(c, first, pivot - 1); // Recursive call, first part of list
            sort(c, pivot + 1, last); // Recursive call, second part of list
        }
    }

    /** Partitions the media ArrayList for use in the sort() method */
    private int partition(Comparator<E> c, int first, int last) {
        E pivot = media.get(first);
        int low = first + 1;
        int high = last;

        while (high > low) {
            // Search forwards through the list from the left until an element
            // greater than the pivot is found
            while (low <= high && c.compare(media.get(low), pivot) <= 0) {
                low++;
            }

            // Search backwards through the list from the right until an element
            // less than or equal to the pivot is found
            while (low <= high && c.compare(media.get(high), pivot) > 0) {
                high--;
            }

            if (high > low) {
                // Swap items at high and low
                E temp = media.get(high);
                media.set(high, media.get(low));
                media.set(low, temp);
            }
        }

        while (high > first && c.compare(media.get(high), pivot) >= 0) {
            high--;
        }

        if (c.compare(pivot, media.get(high)) > 0) {
            media.set(first, media.get(high)); // Element at first index becomes element at high
            media.set(high, pivot); // Element at index of high becomes pivot
            return high;
        } else {
            return first; // Return first index
        }
    }

    /** Reads data from the library.dat file into the media ArrayList */
    protected void read() {
        if (file.length() > 0) {
            try {
                FileInputStream in = new FileInputStream("library.dat");
                ObjectInputStream objectIn = new ObjectInputStream(new BufferedInputStream(in));
                media = (ArrayList<E>) objectIn.readObject();
                groups = (HashMap<E, String>) objectIn.readObject();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /** Writes the contents of the media ArrayList to the library.dat file */
    protected void write() {
        try {
            FileOutputStream out = new FileOutputStream("library.dat");
            ObjectOutputStream objectOut = new ObjectOutputStream(new BufferedOutputStream(out));
            objectOut.writeObject(media);
            objectOut.writeObject(groups);
            objectOut.close();
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Iterator<E> iterator() {
        return media.iterator();
    }
}
