// **********************************************************************************
// Title: WeightedGraph
// Author: Matthew Smith (based on Graph, UnweightedGraph, WeightedGraph, Edge, and
//         WeightedEdge by Y. Daniel Liang)
// Course Section: CMIS202-ONL1 (Seidel) Spring 2023
// File: WeightedGraph.java
// Description: Represents a weighted graph data structure
// **********************************************************************************

package com.matthewsmith.medialibrary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WeightedGraph<V> {
    protected List<V> vertices = new ArrayList<>();
    protected List<List<Edge>> neighbors = new ArrayList<>();

    /** Creates an empty weighted graph */
    public WeightedGraph() {
    }

    /** Creates a weighted graph from an arrays of vertices and edges */
    public WeightedGraph(V[] vertices, int[][] edges) {
        createWeightedGraph(java.util.Arrays.asList(vertices), edges);
    }

    /** Creates a weighted graph from lists of vertices and edges */
    public WeightedGraph(List<V> vertices, List<Edge> edges) {
        createWeightedGraph(vertices, edges);
    }

    /** Creates adjacency lists for each vertex */
    private void createWeightedGraph(List<V> vertices, int[][] edges) {
        this.vertices = vertices;

        for (int i = 0; i < vertices.size(); i++) {
            neighbors.add(new ArrayList<>()); // Create a list for vertices
        }

        for (int i = 0; i < edges.length; i++) {
            neighbors.get(edges[i][0]).add(new Edge(edges[i][0], edges[i][1], edges[i][2]));
        }
    }

    /** Creates adjacency lists for each vertex */
    private void createWeightedGraph(List<V> vertices, List<Edge> edges) {
        this.vertices = vertices;

        for (int i = 0; i < vertices.size(); i++) {
            neighbors.add(new ArrayList<>()); // Create a list for vertices
        }

        for (Edge edge: edges) {
            neighbors.get(edge.u).add(edge); // Add an edge into the list
        }
    }

    /** Returns number of vertices in the graph */
    public int getSize() {
        return vertices.size();
    }

    /** Returns list of vertices in the graph */
    public List<V> getVertices() {
        return vertices;
    }

    /** Returns vertex at a specified index */
    public V getVertex(int index) {
        return vertices.get(index);
    }

    /** Returns index of a specified vertex */
    public int getIndex(V v) {
        return vertices.indexOf(v);
    }

    /** Returns weight of a specified edge */
    public double getWeight(int u, int v) throws Exception {
        for (Edge edge : neighbors.get(u)) {
            if (edge.v == v) {
                return edge.weight;
            }
        }

        throw new Exception("Edge does not exit");
    }

    /** Returns list of neighbors of vertex of the specified index */
    public List<Integer> getNeighbors(int index) {
        List<Integer> result = new ArrayList<>();
        for (Edge e: neighbors.get(index))
            result.add(e.v);

        return result;
    }

    /** Returns degree of a specified vertex */
    public int getDegree(int v) {
        return neighbors.get(v).size();
    }

    /** Adds a vertex to the graph */
    public boolean addVertex(V vertex) {
        if (!vertices.contains(vertex)) {
            vertices.add(vertex);
            neighbors.add(new ArrayList<>());
            return true;
        }
        else {
            return false;
        }
    }

    /** Adds an edge to the graph */
    public boolean addEdge(int u, int v, double weight) {
        return addEdge(new Edge(u, v, weight));
    }

    /** Adds an edge to the graph */
    public boolean addEdge(Edge e) {
        if (e.u < 0 || e.u > getSize() - 1)
            throw new IllegalArgumentException("No such index: " + e.u);

        if (e.v < 0 || e.v > getSize() - 1)
            throw new IllegalArgumentException("No such index: " + e.v);

        if (!neighbors.get(e.u).contains(e)) {
            neighbors.get(e.u).add(e);
            return true;
        }
        else {
            return false;
        }
    }

    /** Clears the graph */
    public void clear() {
        vertices.clear();
        neighbors.clear();
    }

    /** Performs depth-first search and returns a search tree */
    public SearchTree dfs(int v) {
        List<Integer> searchOrder = new ArrayList<>();
        int[] parent = new int[vertices.size()];

        // Initialize parent[i] to -1
        Arrays.fill(parent, -1);

        // Mark visited vertices
        boolean[] isVisited = new boolean[vertices.size()];

        // Recursively search
        dfs(v, parent, searchOrder, isVisited);

        // Return a search tree
        return new SearchTree(v, parent, searchOrder);
    }

    /** Recursive method for DFS search */
    private void dfs(int v, int[] parent, List<Integer> searchOrder, boolean[] isVisited) {
        // Store the visited vertex
        searchOrder.add(v);
        isVisited[v] = true; // Vertex v visited

        for (Edge e : neighbors.get(v)) { // Note that e.u is v
            if (!isVisited[e.v]) { // e.v is w in Listing 28.8
                parent[e.v] = v; // The parent of w is v
                dfs(e.v, parent, searchOrder, isVisited); // Recursive search
            }
        }
    }

    /** Performs a breadth-first search and returns a search tree */
    public SearchTree bfs(int v) {
        List<Integer> searchOrder = new ArrayList<>();
        int[] parent = new int[vertices.size()];

        // Initialize parent[i] to -1
        Arrays.fill(parent, -1);

        java.util.LinkedList<Integer> queue =
                new java.util.LinkedList<>(); // list used as a queue
        boolean[] isVisited = new boolean[vertices.size()];
        queue.offer(v); // Enqueue v
        isVisited[v] = true; // Mark it visited

        while (!queue.isEmpty()) {
            int u = queue.poll(); // Dequeue to u
            searchOrder.add(u); // u searched
            for (Edge e: neighbors.get(u)) { // Note that e.u is u
                if (!isVisited[e.v]) { // e.v is w in Listing 28.11
                    queue.offer(e.v); // Enqueue w
                    parent[e.v] = u; // The parent of w is u
                    isVisited[e.v] = true; // Mark w visited
                }
            }
        }

        return new SearchTree(v, parent, searchOrder);
    }

    /** Returns a minimum spanning tree rooted at vertex 0 */
    public MST getMinimumSpanningTree() {
        return getMinimumSpanningTree(0);
    }

    /** Returns a minimum spanning tree rooted at a specified vertex */
    public MST getMinimumSpanningTree(int startingVertex) {
        // cost[v] stores the cost by adding v to the tree
        double[] cost = new double[getSize()];
        Arrays.fill(cost, Double.POSITIVE_INFINITY); // Initial cost
        cost[startingVertex] = 0; // Cost of source is 0

        int[] parent = new int[getSize()]; // Parent of a vertex
        parent[startingVertex] = -1; // startingVertex is the root
        double totalWeight = 0; // Total weight of the tree thus far

        List<Integer> T = new ArrayList<>();

        // Expand T
        while (T.size() < getSize()) {
            // Find smallest cost u in V - T
            int u = -1; // Vertex to be determined
            double currentMinCost = Double.POSITIVE_INFINITY;
            for (int i = 0; i < getSize(); i++) {
                if (!T.contains(i) && cost[i] < currentMinCost) {
                    currentMinCost = cost[i];
                    u = i;
                }
            }

            if (u == -1) break; else T.add(u); // Add a new vertex to T
            totalWeight += cost[u]; // Add cost[u] to the tree

            // Adjust cost[v] for v that is adjacent to u and v in V - T
            for (Edge e : neighbors.get(u)) {
                if (!T.contains(e.v) && cost[e.v] > e.weight) {
                    cost[e.v] = e.weight;
                    parent[e.v] = u;
                }
            }
        } // End of while

        return new MST(startingVertex, parent, T, totalWeight);
    }

    /** Returns tree of single source shortest paths from specified vertex */
    public ShortestPathTree getShortestPath(int sourceVertex) {
        // cost[v] stores the cost of the path from v to the source
        double[] cost = new double[getSize()];
        Arrays.fill(cost, Double.POSITIVE_INFINITY); // Initial cost set to infinity
        cost[sourceVertex] = 0; // Cost of source is 0

        // parent[v] stores the previous vertex of v in the path
        int[] parent = new int[getSize()];
        parent[sourceVertex] = -1; // The parent of source is set to -1

        // T stores the vertices whose path found so far
        List<Integer> T = new ArrayList<>();

        // Expand T
        while (T.size() < getSize()) {
            // Find smallest cost v in V - T
            int u = -1; // Vertex to be determined
            double currentMinCost = Double.POSITIVE_INFINITY;
            for (int i = 0; i < getSize(); i++) {
                if (!T.contains(i) && cost[i] < currentMinCost) {
                    currentMinCost = cost[i];
                    u = i;
                }
            }

            if (u == -1) break; else T.add(u); // Add a new vertex to T

            // Adjust cost[v] for v that is adjacent to u and v in V - T
            for (Edge e : neighbors.get(u)) {
                if (!T.contains(e.v)
                        && cost[e.v] > cost[u] + e.weight) {
                    cost[e.v] = cost[u] + e.weight;
                    parent[e.v] = u;
                }
            }
        } // End of while

        // Create a ShortestPathTree
        return new ShortestPathTree(sourceVertex, parent, T, cost);
    }

    public static class Edge implements Comparable<Edge> {
        public int u;
        public int v;
        public double weight;

        /** Creates a weighted edge */
        public Edge(int u, int v, double weight) {
            this.u = u;
            this.v = v;
            this.weight = weight;
        }

        /** Checks if two edges are equal */
        @Override
        public boolean equals(Object o) {
            return u == ((Edge) o).u && v == ((Edge) o).v && weight == ((Edge) o).weight;
        }

        /** Compares two edges */
        public int compareTo(Edge edge) {
            if (weight > edge.weight) {
                return 1;
            }
            else if (weight == edge.weight) {
                return 0;
            }
            else {
                return -1;
            }
        }
    }

    public class SearchTree {
        private int root; // The root of the tree
        private int[] parent; // Store the parent of each vertex
        private List<Integer> searchOrder; // Store the search order

        /** Creates a tree with root, parent, and searchOrder */
        public SearchTree(int root, int[] parent,
                          List<Integer> searchOrder) {
            this.root = root;
            this.parent = parent;
            this.searchOrder = searchOrder;
        }

        /** Returns the root of the tree */
        public int getRoot() {
            return root;
        }

        /** Returns the parent of a specified vertex */
        public int getParent(int v) {
            return parent[v];
        }

        /** Returns an array representing the search order */
        public List<Integer> getSearchOrder() {
            return searchOrder;
        }

        /** Returns the number of vertices found */
        public int getNumberOfVerticesFound() {
            return searchOrder.size();
        }

        /** Returns the path of vertices from a vertex to the root */
        public List<V> getPath(int index) {
            ArrayList<V> path = new ArrayList<>();

            do {
                path.add(vertices.get(index));
                index = parent[index];
            }
            while (index != -1);

            return path;
        }
    }

    public class MST extends SearchTree {
        private double totalWeight; // Total weight of all edges in the tree

        /** Creates a minimum spanning tree (MST) */
        public MST(int root, int[] parent, List<Integer> searchOrder,
                   double totalWeight) {
            super(root, parent, searchOrder);
            this.totalWeight = totalWeight;
        }

        /** Returns total weight of tree */
        public double getTotalWeight() {
            return totalWeight;
        }
    }

    public class ShortestPathTree extends SearchTree {
        private double[] cost; // cost[v] is the cost from v to source

        /** Creates a path */
        public ShortestPathTree(int source, int[] parent,
                                List<Integer> searchOrder, double[] cost) {
            super(source, parent, searchOrder);
            this.cost = cost;
        }

        /** Returns the cost for a path from the root to vertex v */
        public double getCost(int v) {
            return cost[v];
        }
    }
}
