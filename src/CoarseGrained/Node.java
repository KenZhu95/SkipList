package CoarseGrained;

import static CoarseGrained.SkipList.MAX_LEVEL;

public class Node {
    int key;
    int topLevel;
    public Node[] next;

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

}
