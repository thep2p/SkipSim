package Simulator;

/**
 * Minimal GUI class for compilation without JavaFX.
 * This allows the codebase to compile and tests to run without GUI dependencies.
 * For full GUI functionality, restore GUI.java.bak and ensure JavaFX is in classpath.
 */
public class GUI {
    public static boolean[] isReplica;

    // Placeholder main method
    public static void main(String[] args) {
        System.err.println("GUI mode requires JavaFX. Please use IntelliJ IDEA with JavaFX configured.");
        System.err.println("For command-line builds, use 'make test' to run tests without GUI.");
        System.exit(1);
    }
}
