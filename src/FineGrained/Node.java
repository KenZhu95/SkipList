package FineGrained;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static FineGrained.SkipList.MAX_LEVEL;

public class Node {
    int key;
    int topLevel;
    public Node[] next;
    volatile boolean marked = false;
    volatile boolean fullyLinked = false;
    final Lock lock = new ReentrantLock();

    // constructor for sentinel nodes
    public Node(int key) {
        this.key = key;
        this.next = new Node[MAX_LEVEL];
        this.topLevel = MAX_LEVEL;
    }

    //constructor for ordinary nodes
    public Node(int key, int height) {
        this.key = key;
        this.next = new Node[MAX_LEVEL];
        this.topLevel = height;
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

}
