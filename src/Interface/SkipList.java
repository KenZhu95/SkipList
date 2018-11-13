package Interface;

public interface SkipList {
    boolean contains(int key);
    boolean add(int key);
    boolean remove(int key);
    void clear();
}
