package thread;

/** Simulates emergency calls arriving. Each request: type, location, description, affected, severity, extra. */
public class IncidentProducer implements Runnable {
    private static final String[][] SAMPLES = {
        {"Flood", "Patna", "River overflow in low areas", "1200", "4", "8"},
        {"Fire", "Mumbai", "Fire in market complex", "150", "3", "Short circuit"},
        {"Earthquake", "Bhuj", "Strong tremors, cracked buildings", "3000", "4", "7"},
        {"Landslide", "Shimla", "Road blocked by landslide", "60", "2", "5"},
        {"Building Collapse", "Delhi", "Old building collapsed", "40", "4", "Poor construction"}
    };

    private final SharedIncidentBuffer buffer;
    private final int count;

    public IncidentProducer(SharedIncidentBuffer buffer, int count) {
        this.buffer = buffer;
        this.count = count;
    }

    @Override
    public void run() {
        for (int i = 0; i < count; i++) {
            try {
                String[] request = SAMPLES[i % SAMPLES.length];
                buffer.put(request);
                System.out.println("[PRODUCER] Emergency call received: " + request[0] + " at " + request[1]);
                Thread.sleep(300);
            } catch (InterruptedException e) {
                System.out.println("[PRODUCER] Interrupted, stopping.");
                return;
            }
        }
        System.out.println("[PRODUCER] No more calls.");
    }
}
