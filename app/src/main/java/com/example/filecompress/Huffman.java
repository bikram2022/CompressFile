package com.example.filecompress;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Comparator;

public class Huffman {
    private String orgStr, encodedStr, decodedStr;
    public HashMap<Character, Integer> hmapWC;  // for occurrence count
    public HashMap<Character, String> hmapCode; // for code(character/code)
    public HashMap<String, Character> hmapCodeR; // for code(code/character)
    private PriorityQueue<node> pq;  // for MinHeap
    private int counter;  // Unique id assigned to each node
    private int treeSize;  // # of total nodes in the tree
    private node root;

    // Inner class
    private class node {
        int uid, weight;
        char ch;
        node left, right;

        // Constructor for class node
        private node(Character ch, Integer weight, node left, node right) {
            uid = ++counter;
            this.weight = weight;
            this.ch = ch;
            this.left = left;
            this.right = right;
        }
    }

    // Constructor for class Huffman
    public Huffman(String orgStr) {
        this.counter = 0;
        this.treeSize = 0;
        this.orgStr = orgStr;
        hmapWC = new HashMap<Character, Integer>();
        hmapCode = new HashMap<Character, String>();
        hmapCodeR = new HashMap<String, Character>();
        pq = new PriorityQueue<node>(1, new Comparator<node>() {
            @Override
            public int compare(node n1, node n2) {
                if (n1.weight < n2.weight)
                    return -1;
                else if (n1.weight > n2.weight)
                    return 1;
                return 0;
            }
        });

        countWord();  // STEP 1: Count frequency of word
        buildTree();  // STEP 2: Build Huffman Tree
        buildCodeTable();  // STEP 4: Build Huffman Code Table
    }

    private void buildCodeTable() {
        String code = "";
        node n = root;
        buildCodeRecursion(n, code);  // Recursion
    }

    private void buildCodeRecursion(node n, String code) {
        if (n != null) {
            if (!isLeaf(n)) {  // n = internal node
                buildCodeRecursion(n.left, code + '0');
                buildCodeRecursion(n.right, code + '1');
            } else {  // n = Leaf node
                hmapCode.put(n.ch, code); // for {character:code}
                hmapCodeR.put(code, n.ch); // for {code:character}
            }
        }
    }

    private void buildTree() {
        buildMinHeap();  // Set all leaf nodes into MinHeap
        node left, right;
        while (!pq.isEmpty()) {
            left = pq.poll();
            treeSize++;
            if (pq.peek() != null) {
                right = pq.poll();
                treeSize++;
                root = new node('\0', left.weight + right.weight, left, right);
            } else {  // only left child. right=null
                root = new node('\0', left.weight, left, null);
            }

            if (pq.peek() != null) {
                pq.offer(root);
            } else {  // = Top root. Finished building the tree.
                treeSize++;
                break;
            }
        }
    }

    private void buildMinHeap() {
        for (Map.Entry<Character, Integer> entry : hmapWC.entrySet()) {
            Character ch = entry.getKey();
            Integer weight = entry.getValue();
            node n = new node(ch, weight, null, null);
            pq.offer(n);
        }
    }

    private void countWord() {
        Character ch;
        Integer weight;
        for (int i = 0; i < orgStr.length(); i++) {
            ch = new Character(orgStr.charAt(i));
            if (hmapWC.containsKey(ch) == false)
                weight = new Integer(1);
            else
                weight = hmapWC.get(ch) + 1;
            hmapWC.put(ch, weight);
        }
    }

    private boolean isLeaf(node n) {
        return (n.left == null) && (n.right == null);
    }

    public String encode() {
        StringBuilder sb = new StringBuilder();
        Character ch;
        for (int i = 0; i < orgStr.length(); i++) {
            ch = orgStr.charAt(i);
            sb.append(hmapCode.get(ch));
        }
        encodedStr = sb.toString();
        return encodedStr;
    }
}