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
    private Comparator<E> c;
    private HashMap<E, String> groups;
    private BucketTree<String, E> tree;
    private final File file;

    /** Creates an empty library */
    public Library() {
        this(new ArrayList<>());
    }

    /** Creates a library with an array list of media */
    public Library(ArrayList<E> media) {
        this.media = media;
        this.file = new File("library.dat");

        try {
            file.createNewFile();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        this.c = (e1, e2) -> ((Comparable<E>)e1).compareTo(e2);
        this.groups = new HashMap<>();
        this.tree = new BucketTree<>(String::compareToIgnoreCase);
    }

    /** Returns the list of media */
    public ArrayList<E> getMedia() {
        return media;
    }

    /** Adds an element to a specified group */
    public void addToGroup(E e, String name) {
        if (name != null && !name.isBlank()) {
            groups.put(e, name);
        }
    }

    /** Removes an element from a group */
    public void removeFromGroup(E e) {
        groups.remove(e);
    }

    /** Returns the group of an element */
    public String getGroup(E e) {
        return groups.get(e);
    }

    /** Returns the set of keys in the group */
    public Set<E> getGroupKeys() {
        return groups.keySet();
    }

    /** Returns true if at least one element belongs to the entered group name */
    public boolean isGroup(String name) {
        return groups.containsValue(name);
    }

    /** Adds an element to a tree with a specified key */
    public boolean addToTree(String key, E e) {
        return tree.add(key, e);
    }

    /** Returns a bucket of the search tree using a key */
    public ArrayList<E> treeSearch(String key) {
        return tree.get(key);
    }

    /** Returns the file */
    public File getFile() {
        return file;
    }

    /** Returns the size of the library */
    public int getSize() {
        return media.size();
    }

    /** Adds an element to the proper position in the library */
    public void add(String name, E e) {
        int i = getIndex(e);
        if (i < 0) { // element is not in the list
            add(-i - 1, name, e);
        } else { // duplicate element in sorting style found
            add(i, name, e);
        }
    }

    /** Adds an element to a specified index in the library */
    private void add(int index, String name, E e) {
        this.media.add(index, e);
        this.tree.add(name.toLowerCase(), e);
        write();
    }

    /** Removes an element from the library */
    public void remove(String name, E e) {
        this.media.remove(e);
        this.groups.remove(e); // remove item from group, if it is a member of one
        this.tree.remove(name.toLowerCase(), e); // remove item from bucket in tree
        write();
    }

    /** Returns the index of a media entry, or -low - 1 if the media is not in the library */
    public int getIndex(E e) {
        int low = 0;
        int high = media.size() - 1;

        while (high >= low) {
            int mid = (low + high) / 2;
            if (c.compare(e, media.get(mid)) < 0) {
                high = mid - 1;
            } else if (c.compare(e, media.get(mid)) == 0) {
                return mid;
            } else {
                low = mid + 1;
            }
        }

        return -low - 1;
    }

    /** Clears list, groups map, and tree */
    public void clear() {
        media.clear();
        groups.clear();
        tree.clear();
        write();
    }

    /** Recursive quick sort - O(n log n) time complexity */
    public void sort(Comparator<E> c) {
        this.c = c;
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

    /** Writes the contents of the library to the library.dat file */
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
