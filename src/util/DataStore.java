package util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/** Small generic container with reusable, type-safe operations. */
public class DataStore<T> {
    private final List<T> items = new ArrayList<>();

    public void add(T item) { items.add(item); }
    public boolean remove(T item) { return items.remove(item); }
    public int size() { return items.size(); }
    public void clear() { items.clear(); }
    public List<T> getAll() { return new ArrayList<>(items); }

    public T findFirst(Predicate<T> condition) {
        for (T item : items) {
            if (condition.test(item)) return item;
        }
        return null;
    }

    public List<T> filter(Predicate<T> condition) {
        List<T> result = new ArrayList<>();
        for (T item : items) {
            if (condition.test(item)) result.add(item);
        }
        return result;
    }

    /** Generic method with a bounded type. */
    public static <E extends Comparable<E>> E maxOf(List<E> list) {
        if (list.isEmpty()) return null;
        E best = list.get(0);
        for (E e : list) {
            if (e.compareTo(best) > 0) best = e;
        }
        return best;
    }

    /** Wildcard: works with a list of any type. */
    public static void printAll(List<?> list) {
        if (list.isEmpty()) {
            System.out.println("  (no records)");
            return;
        }
        for (Object o : list) System.out.println("  " + o);
    }
}
