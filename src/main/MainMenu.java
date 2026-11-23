package main;

import dao.*;
import exception.RescueException;
import exception.InvalidInputException;
import filehandling.CsvExporter;
import filehandling.CsvImporter;
import model.*;
import service.*;
import thread.*;
import util.DataStore;
import util.InputHelper;
import java.io.IOException;
// import java.util.List;

public class MainMenu {
    private static final String[][] MENU = {
        {"1", "User Management"}, {"2", "Disaster Incident Management"}, {"3", "Emergency Priority Queue"},
        {"4", "Rescue Team Management"}, {"5", "Resource Management"}, {"6", "Resource Allocation"},
        {"7", "Relief Camp Management"}, {"8", "Rescue Operations"}, {"9", "Reports and Statistics"},
        {"10", "File Import/Export"}, {"11", "Database Operations"}, {"12", "Exit"}
    };

    private final InputHelper in = new InputHelper();
    private final PriorityManager priorityManager = new PriorityManager();
    private final UserService userService = new UserService();
    private final IncidentService incidentService = new IncidentService(priorityManager);
    private final TeamService teamService = new TeamService();
    private final ResourceService resourceService = new ResourceService();
    private final CampService campService = new CampService();
    private final OperationService operationService = new OperationService(incidentService, teamService, resourceService);
    private final ReportService reportService = new ReportService(incidentService, teamService, resourceService, campService);

    private final UserDAO userDAO = new UserDAO();
    private final IncidentDAO incidentDAO = new IncidentDAO();
    private final TeamDAO teamDAO = new TeamDAO();
    private final ResourceDAO resourceDAO = new ResourceDAO();
    private final CampDAO campDAO = new CampDAO();
    private final CsvExporter exporter = new CsvExporter();
    private final CsvImporter importer = new CsvImporter();

    private User currentUser;
    private EmergencyMonitor monitor;

    // ------------------------------------------------------------ start / main loop
    public void start() {
        System.out.println("=======================================================");
        System.out.println("  RESCUEGRID - Intelligent Disaster Response System");
        System.out.println("=======================================================");
        if (!login()) {
            System.out.println("Too many failed attempts. Goodbye.");
            return;
        }
        if (in.readYesNo("Load sample demo data")) seedDemoData();

        boolean running = true;
        while (running) {
            showMainMenu();
            int choice = in.readInt("Enter choice: ", 1, 12);
            if (choice == 12) {
                stopMonitor();
                System.out.println("Thank you for using RescueGrid. Stay safe!");
                running = false;
                continue;
            }
            if (!currentUser.canAccess(choice)) {
                System.out.println("[ERROR] Access denied: your role cannot open this module.");
                continue;
            }
            switch (choice) {
                case 1: userMenu(); break;
                case 2: incidentMenu(); break;
                case 3: priorityMenu(); break;
                case 4: teamMenu(); break;
                case 5: resourceMenu(); break;
                case 6: allocationMenu(); break;
                case 7: campMenu(); break;
                case 8: operationMenu(); break;
                case 9: reportMenu(); break;
                case 10: fileMenu(); break;
                default: databaseMenu(); break;
            }
        }
    }

    private boolean login() {
        System.out.println("Default login -> username: admin  password: admin123");
        for (int attempt = 1; attempt <= 3; attempt++) {
            String u = in.readString("Username: ");
            String p = in.readString("Password: ");
            User found = userService.login(u, p);
            if (found != null) {
                currentUser = found;
                System.out.println("[OK] Welcome, " + found.getName() + "!");
                found.showRoleMenu();
                return true;
            }
            System.out.println("[ERROR] Wrong username or password (attempt " + attempt + " of 3).");
        }
        return false;
    }

    private void showMainMenu() {
        System.out.println("\n===== RESCUEGRID MAIN MENU =====");
        for (String[] item : MENU) {
            System.out.println(String.format("%2s. %s", item[0], item[1]));
        }
    }

    private void error(Exception e) { System.out.println("[ERROR] " + e.getMessage()); }
    private void ok(String msg) { System.out.println("[OK] " + msg); }

    // ------------------------------------------------------------ 1. users
    private void userMenu() {
        while (true) {
            System.out.println("\n--- USER MANAGEMENT ---");
            System.out.println("1. Register user\n2. Display all users\n3. Search by name\n4. Search by role\n0. Back");
            int ch = in.readInt("Choice: ", 0, 4);
            if (ch == 0) return;
            try {
                switch (ch) {
                    case 1:
                        System.out.println("Roles: 1 Administrator, 2 Rescue Coordinator, 3 Rescue Team Member, 4 Relief Camp Manager");
                        int role = in.readInt("Role: ", 1, 4);
                        String name = in.readString("Full name: ");
                        String phone = in.readString("Phone (10 digits): ");
                        String username = in.readString("Username: ");
                        String password = in.readString("Password: ");
                        User u = userService.registerUser(role, name, phone, username, password);
                        ok("User registered with ID " + u.getUserId());
                        break;
                    case 2: userService.displayAll(); break;
                    case 3: DataStore.printAll(userService.searchByName(in.readString("Name contains: "))); break;
                    default:
                        System.out.println("Role codes: ADMIN, COORDINATOR, TEAM_MEMBER, CAMP_MANAGER");
                        DataStore.printAll(userService.searchByRole(in.readString("Role code: ")));
                }
            } catch (InvalidInputException e) {
                error(e);
            }
        }
    }

    // ------------------------------------------------------------ 2. incidents
    private String pickType() {
        for (int i = 0; i < IncidentService.TYPES.length; i++) {
            System.out.println((i + 1) + ". " + IncidentService.TYPES[i]);
        }
        return IncidentService.TYPES[in.readInt("Disaster type: ", 1, IncidentService.TYPES.length) - 1];
    }

    private int pickSeverity() {
        System.out.println("Severity: 1 Low, 2 Medium, 3 High, 4 Critical");
        return in.readInt("Severity: ", 1, 4);
    }

    private void incidentMenu() {
        while (true) {
            System.out.println("\n--- DISASTER INCIDENT MANAGEMENT ---");
            System.out.println("1. Register incident\n2. Display all\n3. Search\n4. Update\n5. Mark resolved\n"
                    + "6. Delete\n7. Display latest first (ListIterator)\n0. Back");
            int ch = in.readInt("Choice: ", 0, 7);
            if (ch == 0) return;
            try {
                switch (ch) {
                    case 1:
                        String type = pickType();
                        String loc = in.readString("Location: ");
                        String desc = in.readString("Description: ");
                        int affected = in.readInt("Affected people: ", 0, 10000000);
                        int sev = pickSeverity();
                        String extra;
                        if (type.equals("Fire") || type.equals("Building Collapse")) extra = in.readString("Cause: ");
                        else extra = String.valueOf(in.readInt("Intensity (1-10): ", 1, 10));
                        Incident inc = incidentService.createIncident(type, loc, desc, affected, sev, extra);
                        ok("Incident registered with ID " + inc.getIncidentId() + " and added to the priority queue.");
                        break;
                    case 2: incidentService.displayAll(); break;
                    case 3: searchIncidents(); break;
                    case 4: updateIncident(); break;
                    case 5:
                        incidentService.resolve(in.readInt("Incident ID: "));
                        ok("Incident marked as resolved.");
                        break;
                    case 6:
                        incidentService.delete(currentUser, in.readInt("Incident ID: "));
                        ok("Incident deleted.");
                        break;
                    default: incidentService.displayReverse();
                }
            } catch (InvalidInputException e) {
                error(e);
            }
        }
    }

    private void searchIncidents() {
        System.out.println("Search by: 1 ID, 2 Type, 3 Location, 4 Severity");
        int by = in.readInt("Choice: ", 1, 4);
        switch (by) {
            case 1:
                Incident i = incidentService.findById(in.readInt("ID: "));
                System.out.println(i == null ? "  Not found." : "  " + i);
                break;
            case 2: DataStore.printAll(incidentService.searchByType(pickType())); break;
            case 3: DataStore.printAll(incidentService.searchByLocation(in.readString("Location contains: "))); break;
            default: DataStore.printAll(incidentService.searchBySeverity(pickSeverity()));
        }
    }

    private void updateIncident() {
        int id = in.readInt("Incident ID: ");
        System.out.println("Update: 1 Location + description, 2 Affected people + severity");
        if (in.readInt("Choice: ", 1, 2) == 1) {
            incidentService.updateText(id, in.readString("New location: "), in.readString("New description: "));
        } else {
            incidentService.updateNumbers(id, in.readInt("New affected people: ", 0, 10000000), pickSeverity());
        }
        ok("Incident updated.");
    }

    // ------------------------------------------------------------ 3. priority queue + threads
    private void priorityMenu() {
        while (true) {
            System.out.println("\n--- EMERGENCY PRIORITY QUEUE ---");
            System.out.println("1. Show most urgent incident\n2. Dispatch most urgent (create operation)\n"
                    + "3. Show queue sorted\n4. Producer-Consumer demo (threads)\n5. Start/Stop background monitor\n"
                    + "6. Resource race-condition demo (2 threads)\n0. Back");
            int ch = in.readInt("Choice: ", 0, 6);
            if (ch == 0) return;
            try {
                switch (ch) {
                    case 1:
                        Incident top = priorityManager.peek();
                        System.out.println(top == null ? "  Queue is empty." : "  " + top);
                        System.out.println("  Incidents waiting: " + priorityManager.size());
                        break;
                    case 2:
                        Incident next = priorityManager.pollMostUrgent();
                        if (next == null) {
                            System.out.println("  Queue is empty.");
                        } else {
                            RescueOperation op = operationService.createOperation(next.getIncidentId());
                            ok("Dispatched incident #" + next.getIncidentId() + " -> operation #" + op.getOperationId()
                                    + ". Assign teams in module 8.");
                        }
                        break;
                    case 3:
                        System.out.println("Sort by: 1 Urgency, 2 Affected people, 3 Location");
                        DataStore.printAll(priorityManager.showSorted(in.readInt("Choice: ", 1, 3)));
                        break;
                    case 4: producerConsumerDemo(); break;
                    case 5: toggleMonitor(); break;
                    default: raceConditionDemo();
                }
            } catch (InvalidInputException e) {
                error(e);
            }
        }
    }

    private void producerConsumerDemo() {
        int count = in.readInt("How many emergency calls to simulate (1-10): ", 1, 10);
        SharedIncidentBuffer buffer = new SharedIncidentBuffer(3);
        Thread producer = new Thread(new IncidentProducer(buffer, count), "Producer");
        Thread consumer = new Thread(new IncidentConsumer(buffer, incidentService), "Consumer");
        producer.setPriority(Thread.NORM_PRIORITY + 1);
        System.out.println("Producer state before start: " + producer.getState());
        producer.start();
        consumer.start();
        try {
            producer.join();           // wait until all calls are produced
            buffer.close();            // tell the consumer there is nothing more
            consumer.join();
        } catch (InterruptedException e) {
            producer.interrupt();
            consumer.interrupt();
            System.out.println("[WARN] Demo interrupted.");
        }
        System.out.println("Producer state at end: " + producer.getState());
        ok("Demo complete. Incidents processed so far: " + IncidentConsumer.getTotalProcessed());
    }

    private void toggleMonitor() {
        if (monitor != null && monitor.isAlive()) {
            stopMonitor();
            ok("Monitor stopped.");
        } else {
            monitor = new EmergencyMonitor(incidentService, 10000);
            monitor.start();
            ok("Monitor started (reports every 10 seconds, daemon thread).");
        }
    }

    private void stopMonitor() {
        if (monitor != null && monitor.isAlive()) {
            monitor.interrupt();
            try {
                monitor.join(1000);
            } catch (InterruptedException e) {
                System.out.println("[WARN] Interrupted while stopping the monitor.");
            }
        }
    }

    /** Two threads allocate from the same stock. Because ResourceService is synchronized, the result is always correct. */
    private void raceConditionDemo() {
        String name = "Demo Packets";
        resourceService.addResource(name, "Food", 1000);
        Runnable task = () -> {
            for (int i = 0; i < 400; i++) {
                try {
                    resourceService.allocate(name, 1, "INCIDENT", 0);
                } catch (RescueException e) {
                    System.out.println("[" + Thread.currentThread().getName() + "] " + e.getMessage());
                    return;
                }
            }
        };
        Thread t1 = new Thread(task, "Worker-1");
        Thread t2 = new Thread(task, "Worker-2");
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            System.out.println("[WARN] Demo interrupted.");
        }
        Resource r = resourceService.findResource(name);
        System.out.println("Expected allocated = 800, actual = " + r.getAllocatedQty() + " (remaining " + r.getRemaining() + ")");
    }

    // ------------------------------------------------------------ 4. teams
    private void teamMenu() {
        while (true) {
            System.out.println("\n--- RESCUE TEAM MANAGEMENT ---");
            System.out.println("1. Register team\n2. Add member (from registered team members)\n3. Assign team to incident\n"
                    + "4. Release team\n5. Display all teams\n6. Show available teams\n0. Back");
            int ch = in.readInt("Choice: ", 0, 6);
            if (ch == 0) return;
            try {
                switch (ch) {
                    case 1:
                        RescueTeam t = teamService.registerTeam(in.readString("Team name: "), in.readString("Specialization: "));
                        ok("Team registered with ID " + t.getTeamId());
                        break;
                    case 2:
                        DataStore.printAll(userService.searchByRole("TEAM_MEMBER"));
                        int uid = in.readInt("User ID of the member: ");
                        User u = userService.findById(uid);
                        if (!(u instanceof RescueTeamMember)) throw new InvalidInputException("That user is not a Rescue Team Member.");
                        RescueTeamMember m = (RescueTeamMember) u;
                        m.setSkill(in.readString("Member skill: "));
                        teamService.addMember(in.readInt("Team ID: "), m);
                        ok("Member added to the team.");
                        break;
                    case 3:
                        int teamId = in.readInt("Team ID: ");
                        teamService.assignTeam(teamId, incidentService.findById(in.readInt("Incident ID: ")));
                        ok("Team assigned.");
                        break;
                    case 4:
                        teamService.releaseTeam(in.readInt("Team ID: "));
                        ok("Team released and available again.");
                        break;
                    case 5: teamService.displayAll(); break;
                    default: DataStore.printAll(teamService.findAvailable());
                }
            } catch (RescueException | InvalidInputException e) {
                error(e);
            }
        }
    }

    // ------------------------------------------------------------ 5. resources
    private void resourceMenu() {
        while (true) {
            System.out.println("\n--- RESOURCE MANAGEMENT ---");
            System.out.println("1. Add resource\n2. Update total quantity\n3. Search\n4. Display all\n0. Back");
            int ch = in.readInt("Choice: ", 0, 4);
            if (ch == 0) return;
            try {
                switch (ch) {
                    case 1:
                        for (int i = 0; i < ResourceService.CATEGORIES.length; i++)
                            System.out.println((i + 1) + ". " + ResourceService.CATEGORIES[i]);
                        String cat = ResourceService.CATEGORIES[in.readInt("Category: ", 1, ResourceService.CATEGORIES.length) - 1];
                        Resource r = resourceService.addResource(in.readString("Resource name: "), cat, in.readInt("Quantity: ", 0, 100000000));
                        ok("Saved: " + r);
                        break;
                    case 2:
                        resourceService.updateQuantity(in.readString("Resource name: "), in.readInt("New total quantity: ", 0, 100000000));
                        ok("Quantity updated.");
                        break;
                    case 3: DataStore.printAll(resourceService.search(in.readString("Name or category contains: "))); break;
                    default: resourceService.displayAll();
                }
            } catch (InvalidInputException e) {
                error(e);
            }
        }
    }

    // ------------------------------------------------------------ 6. allocation
    private void allocationMenu() {
        while (true) {
            System.out.println("\n--- RESOURCE ALLOCATION ---");
            System.out.println("1. Allocate to incident\n2. Allocate to relief camp\n3. Return resource\n"
                    + "4. Replenish stock\n5. Undo last allocation\n6. View allocation records\n0. Back");
            int ch = in.readInt("Choice: ", 0, 6);
            if (ch == 0) return;
            try {
                switch (ch) {
                    case 1:
                        int incId = in.readInt("Incident ID: ");
                        if (incidentService.findById(incId) == null) throw new InvalidInputException("Incident not found.");
                        ResourceAllocation a = resourceService.allocate(in.readString("Resource name: "),
                                in.readInt("Quantity: "), "INCIDENT", incId);
                        ok("Allocated. " + a);
                        break;
                    case 2:
                        campService.allocateSupplies(in.readInt("Camp ID: "), in.readString("Resource name: "),
                                in.readInt("Quantity: "), resourceService);
                        ok("Supplies allocated to the camp.");
                        break;
                    case 3:
                        resourceService.returnResource(in.readString("Resource name: "), in.readInt("Quantity to return: "));
                        ok("Returned to stock.");
                        break;
                    case 4:
                        resourceService.replenish(in.readString("Resource name: "), in.readInt("Quantity to add: "));
                        ok("Stock replenished.");
                        break;
                    case 5: ok("Undone: " + resourceService.undoLastAllocation()); break;
                    default: DataStore.printAll(resourceService.getAllocations());
                }
            } catch (RescueException | InvalidInputException e) {
                error(e);
            }
        }
    }

    // ------------------------------------------------------------ 7. camps
    private void campMenu() {
        while (true) {
            System.out.println("\n--- RELIEF CAMP MANAGEMENT ---");
            System.out.println("1. Register camp\n2. Register displaced person\n3. Occupancy and availability\n"
                    + "4. Daily food and water requirement\n5. List people in a camp\n0. Back");
            int ch = in.readInt("Choice: ", 0, 5);
            if (ch == 0) return;
            try {
                switch (ch) {
                    case 1:
                        ReliefCamp c = campService.registerCamp(in.readString("Camp name: "), in.readString("Location: "),
                                in.readInt("Capacity: ", 1, 100000));
                        ok("Camp registered with ID " + c.getCampId());
                        break;
                    case 2:
                        int campId = in.readInt("Camp ID: ");
                        String name = in.readString("Person name: ");
                        int age = in.readInt("Age: ", 0, 120);
                        String gender = in.readString("Gender (M/F/O): ").toUpperCase();
                        DisplacedPerson p = campService.registerPerson(campId, name, age, gender);
                        ok("Registered: " + p);
                        break;
                    case 3: campService.showOccupancy(); break;
                    case 4: campService.showRequirements(); break;
                    default:
                        ReliefCamp camp = campService.findById(in.readInt("Camp ID: "));
                        if (camp == null) throw new InvalidInputException("Camp not found.");
                        DataStore.printAll(camp.getPeople());
                        System.out.println("  Supplies received: " + camp.getSupplies());
                }
            } catch (RescueException | InvalidInputException e) {
                error(e);
            }
        }
    }

    // ------------------------------------------------------------ 8. operations
    private void operationMenu() {
        while (true) {
            System.out.println("\n--- RESCUE OPERATIONS ---");
            System.out.println("1. Create operation for incident\n2. Assign team\n3. Allocate resource\n4. Start operation\n"
                    + "5. Complete operation\n6. Show ongoing\n7. Show completed\n8. Show all\n0. Back");
            int ch = in.readInt("Choice: ", 0, 8);
            if (ch == 0) return;
            try {
                switch (ch) {
                    case 1:
                        RescueOperation op = operationService.createOperation(in.readInt("Incident ID: "));
                        ok("Operation created with ID " + op.getOperationId());
                        break;
                    case 2:
                        operationService.assignTeam(in.readInt("Operation ID: "), in.readInt("Team ID: "));
                        ok("Team assigned to operation.");
                        break;
                    case 3:
                        ResourceAllocation a = operationService.allocateResource(in.readInt("Operation ID: "),
                                in.readString("Resource name: "), in.readInt("Quantity: "));
                        ok("Allocated: " + a);
                        break;
                    case 4:
                        operationService.start(in.readInt("Operation ID: "));
                        ok("Operation started.");
                        break;
                    case 5:
                        operationService.complete(in.readInt("Operation ID: "));
                        ok("Operation completed, teams released, incident resolved.");
                        break;
                    case 6: DataStore.printAll(operationService.getByStatus("ONGOING")); break;
                    case 7: DataStore.printAll(operationService.getByStatus("COMPLETED")); break;
                    default: DataStore.printAll(operationService.getAll());
                }
            } catch (RescueException | InvalidInputException e) {
                error(e);
            }
        }
    }

    // ------------------------------------------------------------ 9. reports
    private void reportMenu() {
        while (true) {
            System.out.println("\n--- REPORTS AND STATISTICS ---");
            System.out.println("1. Critical incidents\n2. Available rescue teams\n3. Camps with low free beds\n"
                    + "4. Incident statistics\n5. Resources sorted by remaining stock\n6. Severity matrix\n"
                    + "7. Top 3 urgent incidents\n8. Incident list page by page\n9. Unique incident locations\n0. Back");
            int ch = in.readInt("Choice: ", 0, 9);
            if (ch == 0) return;
            try {
                switch (ch) {
                    case 1: DataStore.printAll(reportService.criticalIncidents()); break;
                    case 2: DataStore.printAll(reportService.availableTeams()); break;
                    case 3: DataStore.printAll(reportService.lowCapacityCamps(in.readInt("Show camps with fewer free beds than: ", 1, 100000))); break;
                    case 4: System.out.print(reportService.incidentStats()); break;
                    case 5: DataStore.printAll(reportService.sortedResources()); break;
                    case 6: System.out.print(reportService.severityMatrix()); break;
                    case 7: DataStore.printAll(reportService.topUrgent(3)); break;
                    case 8:
                        int page = in.readInt("Page number: ", 1, 1000);
                        DataStore.printAll(reportService.incidentPage(page, 5));
                        break;
                    default: incidentService.getUniqueLocations().forEach(System.out::println);
                }
            } catch (InvalidInputException e) {
                error(e);
            }
        }
    }

    // ------------------------------------------------------------ 10. files
    private void fileMenu() {
        while (true) {
            System.out.println("\n--- FILE IMPORT / EXPORT (CSV) ---");
            System.out.println("1. Export incidents\n2. Export resources\n3. Import incidents\n4. Import resources\n0. Back");
            int ch = in.readInt("Choice: ", 0, 4);
            if (ch == 0) return;
            try {
                String file;
                switch (ch) {
                    case 1:
                        file = in.readString("File name (e.g. incidents.csv): ");
                        ok(exporter.exportIncidents(incidentService.getAll(), file) + " incidents exported to " + file);
                        break;
                    case 2:
                        file = in.readString("File name (e.g. resources.csv): ");
                        ok(exporter.exportResources(resourceService.getAll(), file) + " resources exported to " + file);
                        break;
                    case 3:
                        file = in.readString("File name to import: ");
                        int[] r1 = importer.importIncidents(file, incidentService);
                        ok(r1[0] + " imported, " + r1[1] + " skipped.");
                        break;
                    default:
                        file = in.readString("File name to import: ");
                        int[] r2 = importer.importResources(file, resourceService);
                        ok(r2[0] + " imported, " + r2[1] + " skipped.");
                }
            } catch (IOException e) {
                System.out.println("[ERROR] File problem: " + e.getMessage());
            } catch (InvalidInputException e) {
                error(e);
            }
        }
    }

    // ------------------------------------------------------------ 11. database
    private void databaseMenu() {
        while (true) {
            System.out.println("\n--- DATABASE OPERATIONS (MySQL via JDBC) ---");
            System.out.println("1. Set database user/password\n2. Create database and tables\n3. Save all data to database\n"
                    + "4. Load all data from database\n5. Row counts (Statement)\n6. Delete an incident from database\n"
                    + "7. Show saved operations and allocations\n0. Back");
            int ch = in.readInt("Choice: ", 0, 7);
            if (ch == 0) return;
            try {
                switch (ch) {
                    case 1:
                        DBConnection.setCredentials(in.readString("MySQL user: "), in.readString("MySQL password: "));
                        ok("Credentials updated.");
                        break;
                    case 2:
                        DBConnection.initializeDatabase();
                        ok("Database 'rescuegrid_db' and all tables are ready.");
                        break;
                    case 3: saveAll(); break;
                    case 4: loadAll(); break;
                    case 5:
                        String[] tables = {"users", "incidents", "rescue_teams", "resources", "relief_camps",
                                "rescue_operations", "resource_allocations"};
                        for (String t : tables) System.out.println("  " + t + ": " + DBConnection.countRows(t));
                        break;
                    case 6:
                        boolean gone = incidentDAO.delete(in.readInt("Incident ID: "));
                        System.out.println(gone ? "[OK] Deleted from database." : "[INFO] No such row in database.");
                        break;
                    default:
                        DataStore.printAll(teamDAO.findAllOperations());
                        DataStore.printAll(resourceDAO.findAllAllocations());
                }
            } catch (RescueException | InvalidInputException e) {
                error(e);
            }
        }
    }

    private void saveAll() throws RescueException {
        for (User u : userService.getAll()) userDAO.save(u);
        for (Incident i : incidentService.getAll()) incidentDAO.save(i);
        for (RescueTeam t : teamService.getAll()) teamDAO.saveTeam(t);
        for (Resource r : resourceService.getAll()) resourceDAO.save(r);
        for (ResourceAllocation a : resourceService.getAllocations()) resourceDAO.saveAllocation(a);
        for (ReliefCamp c : campService.getAll()) campDAO.saveCamp(c);
        for (RescueOperation op : operationService.getAll()) teamDAO.saveOperation(op);
        ok("All records saved to the database.");
    }

    private void loadAll() throws RescueException {
        for (User u : userDAO.findAll()) userService.addLoaded(u);
        incidentService.replaceAll(incidentDAO.findAll());
        teamService.replaceAll(teamDAO.findAllTeams());
        resourceService.replaceAll(resourceDAO.findAll());
        campService.replaceAll(campDAO.findAll());
        ok("Users, incidents, teams, resources and camps loaded from the database.");
    }

    // ------------------------------------------------------------ demo data
    private void seedDemoData() {
        try {
            incidentService.createIncident("Flood", "Patna", "River overflow in low areas", 1200, 4, "8");
            incidentService.createIncident("Fire", "Mumbai", "Fire in market complex", 150, 3, "Short circuit");
            incidentService.createIncident("Earthquake", "Bhuj", "Strong tremors, cracked buildings", 3000, 4, "7");
            incidentService.createIncident("Landslide", "Shimla", "Road blocked", 60, 2, "5");
            teamService.registerTeam("Alpha Squad", "Flood");
            teamService.registerTeam("Bravo Unit", "Fire");
            resourceService.addResource("Ambulance", "Transport", 10);
            resourceService.addResource("Food Packets", "Food", 500);
            resourceService.addResource("Drinking Water", "Water", 800);
            resourceService.addResource("Medical Kits", "Medical", 100);
            campService.registerCamp("Relief Camp A", "Patna", 5);
            ok("Demo data loaded: 4 incidents, 2 teams, 4 resources, 1 camp.");
        } catch (InvalidInputException e) {
            error(e);
        }
    }
}
