package service;

import exception.InvalidInputException;
import model.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserService {
    private final Map<Integer, User> users = new LinkedHashMap<>();
    private int nextId = 1;

    public UserService() {
        User admin = new Administrator(nextId, "System Admin", "9999999999", "admin", "admin123");
        users.put(nextId, admin);
        nextId++;
    }

    public synchronized User registerUser(int roleChoice, String name, String phone, String username, String password) {
        validateName(name);
        validatePhone(phone);
        if (username.length() < 3 || username.indexOf(' ') >= 0)
            throw new InvalidInputException("Username must be 3+ characters without spaces.");
        if (password.length() < 4)
            throw new InvalidInputException("Password must be at least 4 characters.");
        for (User u : users.values()) {
            if (u.getUsername().equalsIgnoreCase(username))
                throw new InvalidInputException("Username '" + username + "' already exists.");
        }
        int id = nextId++;
        User u;
        switch (roleChoice) {
            case 1: u = new Administrator(id, name, phone, username, password); break;
            case 2: u = new RescueCoordinator(id, name, phone, username, password); break;
            case 3: u = new RescueTeamMember(id, name, phone, username, password); break;
            case 4: u = new ReliefCampManager(id, name, phone, username, password); break;
            case 5: u = new Citizen(id, name, phone, username, password); break;
            default: throw new InvalidInputException("Invalid role choice.");
        }
        users.put(id, u);
        return u;
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) throw new InvalidInputException("Name cannot be empty.");
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isLetter(c) && c != ' ') throw new InvalidInputException("Name can contain only letters and spaces.");
        }
    }

    private void validatePhone(String phone) {
        if (phone.length() != 10) throw new InvalidInputException("Phone number must be exactly 10 digits.");
        for (int i = 0; i < phone.length(); i++) {
            if (!Character.isDigit(phone.charAt(i))) throw new InvalidInputException("Phone number must contain digits only.");
        }
    }

    public synchronized User login(String username, String password) {
        for (User u : users.values()) {
            if (u.getUsername().equals(username) && u.checkPassword(password)) return u;
        }
        return null;
    }

    public synchronized void addLoaded(User u) {
        users.put(u.getUserId(), u);
        if (u.getUserId() >= nextId) nextId = u.getUserId() + 1;
    }

    public synchronized User findById(int id) { return users.get(id); }
    public synchronized List<User> getAll() { return new ArrayList<>(users.values()); }

    public List<User> searchByName(String keyword) {
        String k = keyword.toLowerCase();
        return getAll().stream().filter(u -> u.getName().toLowerCase().contains(k)).collect(Collectors.toList());
    }

    public List<User> searchByRole(String role) {
        return getAll().stream().filter(u -> u.getRole().equalsIgnoreCase(role)).collect(Collectors.toList());
    }

    public void displayAll() {
        System.out.println(String.format("%-4s %-20s %-12s %-14s %s", "ID", "NAME", "PHONE", "USERNAME", "ROLE"));
        util.DataStore.printAll(getAll());
    }
}
