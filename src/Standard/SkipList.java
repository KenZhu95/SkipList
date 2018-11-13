package Standard;

import java.util.concurrent.ConcurrentSkipListSet;

public class SkipList implements Interface.SkipList {
    ConcurrentSkipListSet<Integer> set;

    public SkipList() {
        set = new ConcurrentSkipListSet<>();
    }

    public boolean add(int key) {
        return set.add(key);
    }

    public boolean remove(int key) {
        return set.remove(key);
    }

    public boolean contains(int key) {
        return set.contains(key);
    }

    public void clear() {
        set.clear();
    }
}
