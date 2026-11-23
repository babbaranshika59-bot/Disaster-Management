package service;

import exception.InvalidInputException;
import exception.InvalidTeamAssignmentException;
import exception.RescueException;
import model.*;
import util.DataStore;
import java.util.List;

/** Connects incidents, teams and resources into rescue operations. */
public class OperationService {
    private final DataStore<RescueOperation> operations = new DataStore<>();
    private final IncidentService incidentService;
    private final TeamService teamService;
    private final ResourceService resourceService;

    public OperationService(IncidentService is, TeamService ts, ResourceService rs) {
        this.incidentService = is;
        this.teamService = ts;
        this.resourceService = rs;
    }

    public synchronized RescueOperation createOperation(int incidentId) {
        Incident inc = incidentService.findById(incidentId);
        if (inc == null) throw new InvalidInputException("Incident #" + incidentId + " not found.");
        if (inc.isResolved()) throw new InvalidInputException("Incident #" + incidentId + " is already resolved.");
        if (operations.findFirst(o -> o.getIncident() == inc && !"COMPLETED".equals(o.getStatus())) != null)
            throw new InvalidInputException("An active operation already exists for incident #" + incidentId);
        RescueOperation op = new RescueOperation(inc);
        operations.add(op);
        return op;
    }

    public RescueOperation findById(int id) {
        RescueOperation op = operations.findFirst(o -> o.getOperationId() == id);
        if (op == null) throw new InvalidInputException("Operation #" + id + " not found.");
        return op;
    }

    public synchronized void assignTeam(int opId, int teamId) throws InvalidTeamAssignmentException {
        RescueOperation op = findById(opId);
        if ("COMPLETED".equals(op.getStatus()))
            throw new InvalidTeamAssignmentException("Operation #" + opId + " is already completed.");
        teamService.assignTeam(teamId, op.getIncident());
        op.addTeam(teamService.findById(teamId));
    }

    public synchronized ResourceAllocation allocateResource(int opId, String resourceName, int qty) throws RescueException {
        RescueOperation op = findById(opId);
        if ("COMPLETED".equals(op.getStatus())) throw new InvalidInputException("Operation is already completed.");
        ResourceAllocation a = resourceService.allocate(resourceName, qty, "INCIDENT", op.getIncident().getIncidentId());
        op.addAllocation(a);
        return a;
    }

    public synchronized void start(int opId) { findById(opId).start(); }

    /** Completes the operation, releases its teams and resolves the incident. */
    public synchronized void complete(int opId) {
        RescueOperation op = findById(opId);
        op.complete();
        for (RescueTeam t : op.getTeams()) t.release();
        Incident inc = op.getIncident();
        if (!inc.isResolved()) incidentService.resolve(inc.getIncidentId());
    }

    public List<RescueOperation> getAll() { return operations.getAll(); }
    public List<RescueOperation> getByStatus(String status) { return operations.filter(o -> o.getStatus().equals(status)); }
}
