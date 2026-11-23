package util;

import java.util.Scanner;

/** Safe console input: keeps asking until the input is valid. */
public class InputHelper {
    private final Scanner sc = new Scanner(System.in);

    public String readString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            if (!s.isEmpty()) return s;
            System.out.println("[ERROR] Input cannot be empty.");
        }
    }

    public int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            try {
                return Integer.parseInt(s);          // wrapper class method
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Please enter a valid whole number.");
            }
        }
    }

    /** Overloaded: number must be inside min..max. */
    public int readInt(String prompt, int min, int max) {
        while (true) {
            int v = readInt(prompt);
            if (v >= min && v <= max) return v;
            System.out.println("[ERROR] Enter a number between " + min + " and " + max + ".");
        }
    }

    public boolean readYesNo(String prompt) {
        while (true) {
            String s = readString(prompt + " (y/n): ");
            if (s.equalsIgnoreCase("y")) return true;
            if (s.equalsIgnoreCase("n")) return false;
            System.out.println("[ERROR] Type y or n.");
        }
    }
}
