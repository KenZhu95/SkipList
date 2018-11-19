package FineGrained;

import javax.management.MXBean;
import java.util.Random;

public class SkipList implements Interface.SkipList {
    public static final int MAX_LEVEL = 32;
    final Node head = new Node(Integer.MIN_VALUE);
    final Node tail = new Node(Integer.MAX_VALUE);
    public Random random;

    public SkipList() {
        for (int i = 0; i < head.next.length; ++i) {
            head.next[i] = tail;
        }
        random = new Random();
    }

    public void clear() {
        for (int i = 0; i < head.next.length; ++i) {
            head.next[i] = tail;
        }
        random = new Random();
    }

    private int randomLevel(int maxLevel) {
        int level = 0;
        while (random.nextDouble() < 0.5) {
            level++;
        }
        return Math.min(level, maxLevel);
    }

    private int findNode(int key, Node[] preds, Node[] succs) { //predecessors and successors
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

    public boolean add(int key) {
        int topLevel = randomLevel(MAX_LEVEL - 1);
        Node[] preds = new Node[MAX_LEVEL], succs = new Node[MAX_LEVEL];
        while (true) {
            int lFound = findNode(key, preds, succs);
            if (lFound != -1) { //exists
                Node nodeFound = succs[lFound];
                if (!nodeFound.marked) {    //Not deleted
                    while (!nodeFound.fullyLinked) {}
                    return false;
                }   //Deleted
                continue;
            }
            int highestLocked = -1;
            try {
                Node pred, succ, prevPred = null;
                boolean valid = true;
                for (int level = 0; valid && (level <= topLevel); level++) {
                    pred = preds[level];
                    succ = succs[level];
                    if (pred != prevPred) { //Avoid Repeat Lock on the same node
                        pred.lock();
                        highestLocked = level;
                        prevPred = pred;
                    }
                    valid = !pred.marked && !succ.marked && pred.next[level]==succ;
                }

                if (!valid) continue;   //NOT valid -> finally
                Node newNode = new Node(key, topLevel);
                for (int level = 0; level <= topLevel; level++) {
                    newNode.next[level] = succs[level];
                    preds[level].next[level] = newNode;
                }

                newNode.fullyLinked = true; // successfully add linearization point
                return true;
            } finally {
                if (highestLocked != -1) {
                    Node pred, prevPred = null;
                    for (int level = highestLocked; level >= 0; level--) {
                        pred = preds[level];
                        if (pred != prevPred) {
                            pred.unlock();
                            prevPred = pred;
                        }
                    }
                }
            }
        }
    }


    public boolean remove(int key) {
        Node nodeToDelete = null;
        boolean isMarked = false;
        int topLevel = -1;
        Node[] preds = new Node[MAX_LEVEL], succs = new Node[MAX_LEVEL];

        while (true) {
            int lFound = findNode(key, preds, succs);
            if (isMarked || (lFound != -1 && okToDelete(succs[lFound], lFound))) {
                if (!isMarked) {
                    nodeToDelete = succs[lFound];
                    topLevel = nodeToDelete.topLevel;
                    nodeToDelete.lock();
                    if (nodeToDelete.marked) {
                        nodeToDelete.unlock();
                        return false;
                    }
                    nodeToDelete.marked = true;
                    isMarked = true;
                }
                int highestLocked = -1;
                try {
                    Node pred, succ, prevPred = null;
                    boolean valid = true;
                    for (int level = 0; valid && (level <= topLevel); level++) {
                        pred = preds[level];
                        succ = succs[level];
                        if (pred != prevPred) {
                            pred.lock();
                            highestLocked = level;
                            prevPred = pred;
                        } 
                        valid = !pred.marked && pred.next[level] == succ;
                    }
                    if (!valid) continue;

                    for (int level = topLevel; level >= 0; level--) {
                        preds[level].next[level] = nodeToDelete.next[level];
                    }
                    nodeToDelete.unlock();
                    return true;
                } finally {
                    if (highestLocked != -1) {
                        Node pred, prevPred = null;
                        for (int level = highestLocked; level >= 0; level--) {
                            pred = preds[level];
                            if (pred != prevPred) {
                                pred.unlock();
                                prevPred = pred;
                            }
                        }
                    }
                }
            }
            else return false;

        }

    }


    private boolean okToDelete(Node candidate, int lFound) {
        return (candidate.fullyLinked && candidate.topLevel==lFound && !candidate.marked);
    }


    public boolean contains(int key) {
        Node[] preds = new Node[MAX_LEVEL], succs = new Node[MAX_LEVEL];
        int lFound = findNode(key, preds, succs);
        return (lFound != -1 && succs[lFound].fullyLinked && !succs[lFound].marked);
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
