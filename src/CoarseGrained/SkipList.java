package CoarseGrained;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SkipList implements Interface.SkipList {
    public static final int MAX_LEVEL = 32;
    final Node head = new Node(Integer.MIN_VALUE);
    final Node tail = new Node(Integer.MAX_VALUE);
    public Random random;

    Lock mutexLock;

    public SkipList() {
        for (int i = 0; i < head.next.length; ++i) {
            head.next[i] = tail;
        }
        random = new Random();
        mutexLock = new ReentrantLock();
    }

    public void clear() {
        for (int i = 0; i < head.next.length; ++i) {
            head.next[i] = tail;
        }
        random = new Random();
        mutexLock = new ReentrantLock();
    }


    private int randomLevel(int maxLevel) {
        int level = 0;
        while (random.nextDouble() < 0.5 && level+1 <= maxLevel) {
            level++;
        }
        return Math.min(level, maxLevel);
    }

    private synchronized int findNode(int key, Node[] preds, Node[] succs) { //predecessors and successors
        int lFound = -1;
        Node pred = head;
        for (int level = MAX_LEVEL - 1; level >= 0; level--) {
            Node curr = pred.next[level];
            while (key > curr.key) {
                pred = curr;
                curr = pred.next[level];
            }
            if (lFound == -1 && key == curr.key) {
                lFound = level;
            }
            preds[level] = pred;
            succs[level] = curr;
        }
        return lFound;
    }

    public synchronized boolean add(int key) {
        int topLevel = randomLevel(MAX_LEVEL - 1);
        Node[] preds = new Node[MAX_LEVEL], succs = new Node[MAX_LEVEL];
        while (true) {
            int lFound = findNode(key, preds, succs);
            if (lFound != -1) {
                Node nodeFound = succs[lFound];
                nodeFound.key = key;
                return false;
            }
            Node pred, succ;
            boolean valid = true;
            for (int level = 0; valid && (level <= topLevel); level++) {
                pred = preds[level];
                succ = succs[level];
                valid = pred.next[level] == succ;

            }

            Node newNode = new Node(key, topLevel);
            for (int level = 0; level <= topLevel; level++) {
                newNode.next[level] = succs[level];
                preds[level].next[level] = newNode;
            }

            return true;
        }
    }


    public synchronized boolean remove(int key) {
        Node nodeToDelete = null;
        int topLevel = -1;
        Node[] preds = new Node[MAX_LEVEL], succs = new Node[MAX_LEVEL];

        int lFound = findNode(key, preds, succs);
        if (lFound != -1) {
            nodeToDelete = succs[lFound];
            topLevel = nodeToDelete.topLevel;

            Node pred, succ;
            boolean valid = true;
            for (int level = 0; valid && (level <= topLevel); level++) {
                pred = preds[level];
                succ = succs[level];
                valid =  pred.next[level] == succ;
            }

            for (int level = topLevel; level >= 0; level--) {
                preds[level].next[level] = nodeToDelete.next[level];
            }
            return true;

        }
        else return false;

    }


    public synchronized boolean contains(int key) {
        Node[] preds = new Node[MAX_LEVEL], succs = new Node[MAX_LEVEL];
        int lFound = findNode(key, preds, succs);
        return (lFound != -1);
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        Node curr = head.next[0];
        while (curr != tail) {
            System.out.println(curr.key);
            sb.append(curr.key + ",");
            curr = curr.next[0];
        }
        return sb.toString();
    }

}
