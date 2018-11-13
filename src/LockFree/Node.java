package LockFree;

import java.util.concurrent.atomic.AtomicMarkableReference;

import static LockFree.SkipList.MAX_LEVEL;

public class Node {
    int key;
    AtomicMarkableReference<Node>[] next;
    int topLevel;

    // constructor for sentinel nodes
    public Node(int key) {
        this.key = key;
        this.next = (AtomicMarkableReference<Node>[]) new AtomicMarkableReference[MAX_LEVEL + 1];
        for (int i = 0; i < next.length; ++i) {
            next[i] = new AtomicMarkableReference<>(null, false);
        }
        this.topLevel = MAX_LEVEL;
    }

    //constructor for ordinary nodes
    public Node(int key, int height) {
        this.key = key;
        next = (AtomicMarkableReference<Node>[]) new AtomicMarkableReference[height + 1];
        for (int i = 0; i < next.length; ++i) {
            next[i] = new AtomicMarkableReference<>(null, false);
        }
        this.topLevel = height;
    }

}
