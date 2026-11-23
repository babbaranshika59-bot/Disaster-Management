package thread;

import service.IncidentService;

/** Daemon thread that reports unresolved critical incidents in the background. */
public class EmergencyMonitor extends Thread {
    private final IncidentService incidentService;
    private final long intervalMillis;

    public EmergencyMonitor(IncidentService service, long intervalMillis) {
        super("EmergencyMonitor");
        this.incidentService = service;
        this.intervalMillis = intervalMillis;
        setDaemon(true);                         // does not stop the program from exiting
        setPriority(Thread.NORM_PRIORITY - 1);   // slightly lower priority
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                Thread.sleep(intervalMillis);
            } catch (InterruptedException e) {
                break;
            }
            int critical;
            synchronized (incidentService) {      // synchronized block on the shared service
                critical = incidentService.countCritical();
            }
            System.out.println("\n[MONITOR] Unresolved CRITICAL incidents: " + critical);
        }
        System.out.println("[MONITOR] Stopped.");
    }
}
