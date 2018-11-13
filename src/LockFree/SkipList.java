package LockFree;

import java.util.Random;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class SkipList implements Interface.SkipList {
    public static final int MAX_LEVEL = 32 - 1;
    final Node head = new Node(Integer.MIN_VALUE);
    final Node tail = new Node(Integer.MAX_VALUE);
    public Random random;

    public SkipList() {
        for (int level = 0; level < head.next.length; ++level) {
            head.next[level] = new AtomicMarkableReference<>(tail, false);
        }
        random = new Random();
    }

    public void clear() {
        for (int level = 0; level < head.next.length; ++level) {
            head.next[level] = new AtomicMarkableReference<>(tail, false);
        }
        random = new Random();
    }

    private int randomLevel(int maxLevel) {
        int level = 0;
        while (random.nextDouble() < 0.5 && level + 1 <= maxLevel) {
            level++;
        }
        return Math.min(level, maxLevel);
    }

    private boolean findNode(int key, Node[] preds, Node[] succs) {
        int bottomLevel = 0;
        boolean[] marked = {false};
        boolean snip;
        Node pred = null, curr = null, succ = null;
        retry:
        while (true) {
            pred = head;
            for (int level = MAX_LEVEL; level >= bottomLevel; level--) {
                curr = pred.next[level].getReference();
                while (true) {
                    succ = curr.next[level].get(marked);
                    while (marked[0]) {
                        snip = pred.next[level].compareAndSet(curr, succ, false, false);

                        if (!snip) continue retry;
                        curr = pred.next[level].getReference();
                        succ = curr.next[level].get(marked);
                    }
                    if (curr.key < key) {
                        pred = curr;
                        curr = succ;
                    } else {
                        break;
                    }
                }

                preds[level] = pred;
                succs[level] = curr;
            }
            return (curr.key == key);
        }
    }

    public boolean add(int key) {
        int topLevel = randomLevel(MAX_LEVEL);
        int bottomLevel = 0;
        Node[] preds = new Node[MAX_LEVEL + 1];
        Node[] succs = new Node[MAX_LEVEL + 1];
        while (true) {
            boolean found = findNode(key, preds, succs);
            if (found) {
                return false;
            } else {
                Node newNode = new Node(key, topLevel);
                for (int level = bottomLevel; level <= topLevel; level++) {
                    Node succ = succs[level];
                    newNode.next[level].set(succ, false);
                }
                Node pred = preds[bottomLevel];
                Node succ = succs[bottomLevel];
                newNode.next[bottomLevel].set(succ, false);
                if (!pred.next[bottomLevel].compareAndSet(succ, newNode, false, false)) {
                    continue;
                }

                for (int level = bottomLevel + 1; level <= topLevel; level++) {
                    while (true) {
                        pred = preds[level];
                        succ = succs[level];
                        if (pred.next[level].compareAndSet(succ, newNode, false, false)) {
                            break;
                        }
                        findNode(key, preds, succs);
                    }
                }
                return true;
            }
        }
    }

    public boolean remove(int key) {
        int bottomLevel = 0;
        Node[] preds = new Node[MAX_LEVEL + 1];
        Node[] succs = new Node[MAX_LEVEL + 1];
        Node succ;
        while (true) {
            boolean found = findNode(key, preds, succs);
            if (!found) {
                return false;
            } else {
                Node nodeToRemove = succs[bottomLevel];
                for (int level = nodeToRemove.topLevel; level >= bottomLevel + 1; level--) {
                    boolean[] marked = {false};
                    succ = nodeToRemove.next[level].get(marked);
                    while (!marked[0]) {
                        nodeToRemove.next[level].attemptMark(succ, true);
                        succ = nodeToRemove.next[level].get(marked);
                    }
                }

                boolean[] marked = {false};
                succ = nodeToRemove.next[bottomLevel].get(marked);
                while (true) {
                    boolean iMarkedIt = nodeToRemove.next[bottomLevel].compareAndSet(succ, succ, false, true);
                    succ = succs[bottomLevel].next[bottomLevel].get(marked);
                    if (iMarkedIt) {
                        findNode(key, preds, succs);
                        return true;
                    } else {
                        if (marked[0]) return false;
                    }
                }
            }
        }
    }

    public boolean contains(int key) {
        int bottomLevel = 0;
        boolean[] marked = {false};
        Node pred = head, curr = null, succ = null;
        for (int level = MAX_LEVEL; level >= bottomLevel; level--) {
            curr = pred.next[level].getReference();
            while (true) {
                succ = curr.next[level].get(marked);
                while (marked[0]) {
                    curr = pred.next[level].getReference();
                    succ = curr.next[level].get(marked);
                }
                if (curr.key < key) {
                    pred = curr;
                    curr = succ;
                } else {
                    break;
                }
            }
        }
        return (curr.key == key);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Node node = head.next[0].getReference(); node != tail; node = node.next[0].getReference()) {
            sb.append(node.key);
            sb.append(",");
        }
        return sb.toString();
    }

}
