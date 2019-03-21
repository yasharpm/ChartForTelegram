package com.yashoid.chartfortelegram.data;

public class CartesianTree {

    final private int[] mValues;

    private Node mRoot = null;

    public CartesianTree(int[] values) {
        mValues = values;

        buildTheTree();
    }

    public int getMaxValue() {
        return mRoot.getValue();
    }

    public int findMaximumValueInRange(int start, int end) {
        return mRoot.findMaximumValueInRange(start, end);
    }

    public int findMaximumValueInRange(long[] base, long start, long end) {
        return mRoot.findMaximumValueInRange(base, start, end);
    }

    private void buildTheTree() {
        mRoot = new Node(0);

        Node latestNode = mRoot;

        for (int i = 1; i < mValues.length; i++) {
            Node newNode = new Node(i);

            placeNode(newNode, latestNode);

            latestNode = newNode;
        }
    }

    private void placeNode(Node newNode, Node latestNode) {
        Node parent = findParentNode(latestNode, newNode.getValue());

        if (parent != null) {
            newNode.leftChild = parent.rightChild;

            if (newNode.leftChild != null) {
                newNode.leftChild.parent = newNode;
            }

            parent.rightChild = newNode;
            newNode.parent = parent;
            return;
        }

        newNode.leftChild = mRoot;
        mRoot.parent = newNode;

        mRoot = newNode;
    }

    private Node findParentNode(Node latestNode, long value) {
        if (value < latestNode.getValue()) {
            return latestNode;
        }

        if (latestNode.parent == null) {
            return null;
        }

        return findParentNode(latestNode.parent, value);
    }

    private class Node {

        final private int index;

        private Node parent = null;
        private Node leftChild = null;
        private Node rightChild = null;

        protected Node(int index) {
            this.index = index;
        }

        protected int getValue() {
            return mValues[index];
        }

        protected int findMaximumValueInRange(int start, int end) {
            if (index >= start) {
                if (index <= end || leftChild == null) {
                    return mValues[index];
                }

                return leftChild.findMaximumValueInRange(start, end);
            }

            if (rightChild == null) {
                return mValues[index];
            }

            return rightChild.findMaximumValueInRange(start, end);
        }

        protected int findMaximumValueInRange(long[] base, long start, long end) {
            if (base[index] >= start) {
                if (base[index] <= end || leftChild == null) {
                    return mValues[index];
                }

                return leftChild.findMaximumValueInRange(base, start, end);
            }

            if (rightChild == null) {
                return mValues[index];
            }

            return rightChild.findMaximumValueInRange(base, start, end);
        }

    }

}
