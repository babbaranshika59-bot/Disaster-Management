package main;

import java.util.NoSuchElementException;

/** Program entry point. All real work happens in MainMenu and the service classes. */
public class Main {
    public static void main(String[] args) {
        try {
            new MainMenu().start();
        } catch (NoSuchElementException e) {
            System.out.println("\n[INFO] Input ended. Closing RescueGrid.");
        }
    }
}
