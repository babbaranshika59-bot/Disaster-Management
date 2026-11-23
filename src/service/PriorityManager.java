package service;

import model.Incident;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/** Keeps unresolved incidents in a PriorityQueue (most urgent first). */
public class PriorityManager {
    private final PriorityQueue<Incident> queue = new PriorityQueue<>();   // uses Comparable

    public synchronized void enqueue(Incident i) {
        if (!i.isResolved() && !queue.contains(i)) queue.add(i);
    }

    public synchronized Incident peek() { return queue.peek(); }
    public synchronized Incident pollMostUrgent() { return queue.poll(); }
    public synchronized boolean remove(Incident i) { return queue.remove(i); }
    public synchronized void clear() { queue.clear(); }
    public synchronized int size() { return queue.size(); }

    /** mode 1 = urgency, 2 = affected people, 3 = location A-Z. */
    public synchronized List<Incident> showSorted(int mode) {
        List<Incident> list = new ArrayList<>(queue);
        if (mode == 2) {
            Comparator<Incident> byAffected = (a, b) -> Integer.compare(b.getAffectedPeople(), a.getAffectedPeople());
            Collections.sort(list, byAffected);
        } else if (mode == 3) {
            list.sort(Comparator.comparing(Incident::getLocation, String.CASE_INSENSITIVE_ORDER));
        } else {
            Collections.sort(list);
        }
        return list;
    }
}
