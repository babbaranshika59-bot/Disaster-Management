package model;

public class DisplacedPerson {
    private static int counter = 1;

    private final int personId;
    private final String name;
    private final int age;
    private final String gender;
    private int campId;

    public DisplacedPerson(String name, int age, String gender) {
        this(nextId(), name, age, gender);
    }

    public DisplacedPerson(int id, String name, int age, String gender) {
        this.personId = id;
        synchronized (DisplacedPerson.class) { if (id >= counter) counter = id + 1; }
        this.name = name;
        this.age = age;
        this.gender = gender;
    }

    private static synchronized int nextId() { return counter++; }

    public int getPersonId() { return personId; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public int getCampId() { return campId; }
    public void setCampId(int campId) { this.campId = campId; }

    @Override
    public String toString() {
        return String.format("Person #%-3d %-18s age=%-3d %s (camp %d)", personId, name, age, gender, campId);
    }
}
