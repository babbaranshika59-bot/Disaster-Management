package thread;

import exception.InvalidInputException;
import model.Incident;
import service.IncidentService;

/** Takes requests from the buffer and registers them as incidents. */
public class IncidentConsumer implements Runnable {
    private static int totalProcessed = 0;

    private final SharedIncidentBuffer buffer;
    private final IncidentService incidentService;

    public IncidentConsumer(SharedIncidentBuffer buffer, IncidentService service) {
        this.buffer = buffer;
        this.incidentService = service;
    }

    private static synchronized void countOne() { totalProcessed++; }
    public static synchronized int getTotalProcessed() { return totalProcessed; }

    @Override
    public void run() {
        while (true) {
            try {
                String[] r = buffer.take();
                if (r == null) break;                  // buffer closed and empty
                Incident i = incidentService.createIncident(r[0], r[1], r[2],
                        Integer.parseInt(r[3]), Integer.parseInt(r[4]), r[5]);
                countOne();
                System.out.println("[CONSUMER] Registered incident #" + i.getIncidentId() + " (" + i.getDisasterType() + ")");
            } catch (InterruptedException e) {
                System.out.println("[CONSUMER] Interrupted, stopping.");
                return;
            } catch (NumberFormatException | InvalidInputException e) {
                System.out.println("[CONSUMER] Bad request skipped: " + e.getMessage());
            }
        }
        System.out.println("[CONSUMER] Finished.");
    }
}
