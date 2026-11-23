package thread;

import java.util.LinkedList;
import java.util.Queue;

/** Bounded buffer shared by producer and consumer threads (guarded blocks with wait/notifyAll). */
public class SharedIncidentBuffer {
    private final Queue<String[]> queue = new LinkedList<>();
    private final int capacity;
    private boolean closed = false;

    public SharedIncidentBuffer(int capacity) { this.capacity = capacity; }

    public synchronized void put(String[] request) throws InterruptedException {
        while (queue.size() == capacity) {
            wait();                      // buffer full: producer waits
        }
        queue.add(request);
        notifyAll();                     // wake the consumer
    }

    /** Returns null when the buffer is closed and empty. */
    public synchronized String[] take() throws InterruptedException {
        while (queue.isEmpty() && !closed) {
            wait();                      // buffer empty: consumer waits
        }
        if (queue.isEmpty()) return null;
        String[] request = queue.poll();
        notifyAll();                     // wake the producer
        return request;
    }

    public synchronized void close() {
        closed = true;
        notifyAll();
    }

    public synchronized int size() { return queue.size(); }
}
