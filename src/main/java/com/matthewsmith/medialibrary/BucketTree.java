// **********************************************************************************
// Title: BucketTree
// Author: Matthew Smith (based on AVLTree, BST, and Tree by Y. Daniel Liang)
// Course Section: CMIS202-ONL1 (Seidel) Spring 2023
// File: BucketTree.java
// Description: Creates a tree of buckets (ArrayLists) that can be accessed by a key;
//              based on an AVL tree
// **********************************************************************************

package com.matthewsmith.medialibrary;

import java.util.ArrayList;
import java.util.Comparator;

public class BucketTree<K, V> {
    protected BucketTreeNode<K, V> root;
    protected int size = 0;
    protected Comparator<K> c;

    /** Creates a bucket tree with the default comparator */
    public BucketTree() {
        this.c = (e1, e2) -> ((Comparable<K>)e1).compareTo(e2);
    }

    /** Creates a bucket tree with a comparator */
    public BucketTree(Comparator<K> c) {
        this.c = c;
    }

    /** Returns the bucket for a key */
    public ArrayList<V> get(K key) {
        BucketTreeNode<K, V> current = root;

        while (current != null) {
            if (c.compare(key, current.key) < 0) {
                current = current.left;
            } else if (c.compare(key, current.key) > 0) {
                current = current.right;
            } else {
                return current.value; // key found
            }
        }

        return null; // key not in tree
    }

    /** Inserts an element into the tree and rebalances if necessary */
    public boolean add(K k, V v) {
        if (root == null) { // tree is empty
            root = createNewNode(k, v);
        } else {
            BucketTreeNode<K, V> parent = null;
            BucketTreeNode<K, V> current = root;

            while (current != null) {
                if (c.compare(k, current.key) < 0) {
                    parent = current;
                    current = current.left;
                } else if (c.compare(k, current.key) > 0) {
                    parent = current;
                    current = current.right;
                } else {
                    // key located
                    return current.value.add(v);
                }
            }

            // parent element located, set left or right child
            if (c.compare(k, parent.key) < 0) {
                parent.left = createNewNode(k, v);
            } else {
                parent.right = createNewNode(k, v);
            }
        }

        balancePath(k);
        size++;
        return true;
    }

    /** Removes a key and its associated value from the tree and rebalances if necessary */
    public boolean remove(K k) {
        if (root == null) {
            return false; // tree is empty
        }

        BucketTreeNode<K, V> parent = null;
        BucketTreeNode<K, V> current = root;

        // locate node to be deleted
        while (current != null) {
            if (c.compare(k, current.key) < 0) {
                parent = current;
                current = current.left;
            } else if (c.compare(k, current.key) > 0) {
                parent = current;
                current = current.right;
            } else {
                break; // key found
            }
        }

        if (current == null) {
            return false; // key not in tree
        }

        // Case 1: current does not have a left child
        if (current.left == null) {
            if (parent == null) {
                // current is root
                root = current.right;
            } else {
                // make current's right child either the left or right child of parent
                if (c.compare(k, parent.key) < 0) {
                    parent.left = current.right;
                } else {
                    parent.right = current.right;
                }

                balancePath(parent.key);
            }
        } else {
            // Case 2: current does have a left child
            // locate rightmost node in left subtree
            BucketTreeNode<K, V> parentOfRightMost = current;
            BucketTreeNode<K, V> rightMost = current.left;

            while (rightMost.right != null) {
                parentOfRightMost = rightMost;
                rightMost = rightMost.right;
            }

            // replace current key and value with rightMost key and value
            current.key = rightMost.key;
            current.value = rightMost.value;

            if (parentOfRightMost.right == rightMost) {
                // rightMost's left child becomes parentOfRightMost's right child
                parentOfRightMost.right = rightMost.left;
            } else {
                parentOfRightMost.left = rightMost.left;
            }

            balancePath(parentOfRightMost.key);
        }

        size--;
        return true;
    }

    /** Removes a specified value of a key */
    public boolean remove(K k, V v) {
        ArrayList<V> values = get(k);
        return values != null && values.remove(v);
    }

    /** Creates a new BucketTreeNode with a key and one value */
    private BucketTreeNode<K, V> createNewNode(K k, V v) {
        ArrayList<V> values = new ArrayList<>();
        values.add(v);
        return new BucketTreeNode<>(k, values);
    }

    /** Returns a path from the root to a specified node */
    public ArrayList<BucketTreeNode<K, V>> path(K k) {
        ArrayList<BucketTreeNode<K, V>> list = new ArrayList<>();
        BucketTreeNode<K, V> current = root;

        while (current != null) {
            list.add(current);
            if (c.compare(k, current.key) < 0) {
                current = current.left;
            } else if (c.compare(k, current.key) > 0) {
                current = current.right;
            } else {
                break; // specified node located
            }
        }

        return list;
    }

    /** Updates the height of a node */
    private void updateHeight(BucketTreeNode<K, V> node) {
        if (node.left == null && node.right == null) { // leaf node
            node.height = 0;
        } else if (node.left == null) { // no left subtree
            node.height = 1 + node.right.height;
        } else if (node.right == null) { // no right subtree
            node.height = 1 + node.left.height;
        } else { // left and right subtrees
            node.height = 1 + Math.max(node.right.height, node.left.height);
        }
    }

    /** Balances the path from a node up to the root */
    private void balancePath(K k) {
        ArrayList<BucketTreeNode<K, V>> path = path(k);
        for (int i = path.size() - 1; i >= 0; i--) {
            BucketTreeNode<K, V> A = path.get(i);
            updateHeight(A);
            BucketTreeNode<K, V> parentOfA = (A == root) ? null : path.get(i - 1);

            switch (balanceFactor(A)) {
                case -2:
                    if (balanceFactor(A.left) <= 0) {
                        balanceLL(A, parentOfA);
                    } else {
                        balanceLR(A, parentOfA);
                    }
                    break;
                case +2:
                    if (balanceFactor(A.right) >= 0) {
                        balanceRR(A, parentOfA);
                    } else {
                        balanceRL(A, parentOfA);
                    }
                    break;
            }
        }
    }

    /** Gets the balance factor of a node */
    private int balanceFactor(BucketTreeNode<K, V> node) {
        if (node.right == null) {
            return -node.height;
        } else if (node.left == null) {
            return +node.height;
        } else {
            return node.right.height - node.left.height;
        }
    }

    /** Performs an LL rotation */
    private void balanceLL(BucketTreeNode<K, V> A, BucketTreeNode<K, V> parentOfA) {
        BucketTreeNode<K, V> B = A.left;

        if (A == root) {
            root = B;
        } else {
            if (parentOfA.left == A) {
                parentOfA.left = B;
            } else {
                parentOfA.right = B;
            }
        }

        A.left = B.right;
        B.right = A;

        // update heights
        updateHeight(A);
        updateHeight(B);
    }

    /** Performs an LR rotation */
    private void balanceLR(BucketTreeNode<K, V> A, BucketTreeNode<K, V> parentOfA) {
        BucketTreeNode<K, V> B = A.left;
        BucketTreeNode<K, V> C = B.right;

        if (A == root) {
            root = C;
        } else {
            if (parentOfA.left == A) {
                parentOfA.left = C;
            } else {
                parentOfA.right = C;
            }
        }

        A.left = C.right;
        B.right = C.left;
        C.left = B;
        C.right = A;

        // update heights
        updateHeight(A);
        updateHeight(B);
        updateHeight(C);
    }

    /** Performs an RR rotation */
    private void balanceRR(BucketTreeNode<K, V> A, BucketTreeNode<K, V> parentOfA) {
        BucketTreeNode<K, V> B = A.right;

        if (A == root) {
            root = B;
        } else {
            if (parentOfA.left == A) {
                parentOfA.left = B;
            } else {
                parentOfA.right = B;
            }
        }

        A.right = B.left;
        B.left = A;

        // update heights
        updateHeight(A);
        updateHeight(B);
    }

    /** Perform an RL rotation */
    private void balanceRL(BucketTreeNode<K, V> A, BucketTreeNode<K, V> parentOfA) {
        BucketTreeNode<K, V> B = A.right;
        BucketTreeNode<K, V> C = B.left;

        if (A == root) {
            root = C;
        } else {
            if (parentOfA.left == A) {
                parentOfA.left = C;
            } else {
                parentOfA.right = C;
            }
        }

        A.right = C.left;
        B.left = C.right;
        C.left = A;
        C.right = B;

        // update heights
        updateHeight(A);
        updateHeight(B);
        updateHeight(C);
    }

    /** Returns true if the tree is empty */
    public boolean isEmpty() {
        return this.size == 0;
    }

    /** Returns the size of the tree */
    public int getSize() {
        return this.size;
    }

    /** Clears the tree */
    public void clear() {
        root = null;
        size = 0;
    }

    /** Bucket tree node class with key, value, left and right pointers, and height */
    protected static class BucketTreeNode<K, V> {
        protected K key; // key value of type K
        protected ArrayList<V> value; // bucket of values of type V
        protected BucketTreeNode<K, V> left, right;
        protected int height = 0;

        /** Creates a bucket tree node with a specified element */
        public BucketTreeNode(K key, ArrayList<V> value) {
            this.key = key;
            this.value = value;
        }
    }
}
