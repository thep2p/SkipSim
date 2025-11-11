package Simulator;

import DataBase.ChurnDBEntry;
import DataBase.SimulationDB;
import DataTypes.Constants;
import LandmarkPlacement.landmarkSimulation;
import SkipGraph.SkipGraphOperations;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Vector;

/**
 * Command-line main class for SkipSim simulator.
 * Supports creating, loading, listing, and deleting simulations without GUI dependencies.
 *
 * Usage:
 *   java Simulator.Main new <simulation-name>     - Create a new simulation
 *   java Simulator.Main load <simulation-name>    - Load and run an existing simulation
 *   java Simulator.Main list                      - List all available simulations
 *   java Simulator.Main delete <simulation-name>  - Delete a simulation
 */
public class Main {

    public static boolean[] isReplica;
    private SimulationDB simDB;
    private boolean isBlockChain;

    public static void main(String[] args) {
        Main main = new Main();
        main.run(args);
    }

    public void run(String[] args) {
        // Parse command-line arguments
        if (args.length == 0) {
            printUsage();
            return;
        }

        // Check for --config argument and extract it
        String configFile = null;
        int commandIndex = 0;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--config") && i + 1 < args.length) {
                configFile = args[i + 1];
                break;
            }
        }

        // Load configuration
        String configPath = (configFile != null) ? configFile : "simulation-config.properties";
        loadConfiguration(configPath);

        simDB = new SimulationDB();
        FileInteractions.PrintSimulationParameters();

        // Determine if simulation is blockchain type
        isBlockChain = SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.BLOCKCHAIN);
        isReplica = new boolean[SkipSimParameters.getSystemCapacity()];

        String command = args[0].toLowerCase();

        switch (command) {
            case "new":
                if (args.length < 2) {
                    System.err.println("Error: Simulation name required");
                    System.err.println("Usage: java Simulator.Main new <simulation-name>");
                    System.exit(1);
                }
                createNewSimulation(args[1]);
                break;

            case "load":
                if (args.length < 2) {
                    System.err.println("Error: Simulation name required");
                    System.err.println("Usage: java Simulator.Main load <simulation-name>");
                    System.exit(1);
                }
                loadSimulation(args[1]);
                break;

            case "list":
                listSimulations();
                break;

            case "delete":
                if (args.length < 2) {
                    System.err.println("Error: Simulation name required");
                    System.err.println("Usage: java Simulator.Main delete <simulation-name>");
                    System.exit(1);
                }
                deleteSimulation(args[1]);
                break;

            default:
                System.err.println("Error: Unknown command '" + command + "'");
                printUsage();
                System.exit(1);
        }
    }

    /**
     * Loads simulation configuration from a properties file.
     * @param configPath Path to the configuration file
     */
    private static void loadConfiguration(String configPath) {
        File configFile = new File(configPath);
        if (configFile.exists()) {
            System.out.println("Loading configuration from: " + configPath);
            ConfigLoader.loadFromFile(configPath);
        } else {
            System.err.println("ERROR: Configuration file not found: " + configPath);
            System.err.println();
            System.err.println("Available configuration files:");
            System.err.println("  - simulation-config.properties (default)");
            System.err.println("  - configs/quick-test.properties (64 nodes, 24 hours)");
            System.err.println("  - configs/full-experiment.properties (1024 nodes, 168 hours)");
            System.err.println();
            System.err.println("Usage:");
            System.err.println("  make run-load NAME=my_sim CONFIG=configs/quick-test.properties");
            System.err.println("  or create a custom config file based on the examples above");
            System.exit(1);
        }
    }

    private void printUsage() {
        System.out.println("\nSkipSim Command-Line Interface");
        System.out.println("==============================");
        System.out.println("Usage: java Simulator.Main <command> [arguments] [options]");
        System.out.println("\nCommands:");
        System.out.println("  load <simulation-name>    Load existing or create new simulation (recommended)");
        System.out.println("  new <simulation-name>     Explicitly create a new simulation");
        System.out.println("  list                      List all available simulations");
        System.out.println("  delete <simulation-name>  Delete a simulation");
        System.out.println("\nOptions:");
        System.out.println("  --config <file>           Specify custom configuration file");
        System.out.println("                            (default: simulation-config.properties)");
        System.out.println("\nExamples:");
        System.out.println("  java Simulator.Main load my_sim");
        System.out.println("  java Simulator.Main load my_sim --config configs/large-experiment.properties");
        System.out.println("  java Simulator.Main new my_sim --config configs/quick-test.properties");
        System.out.println("  java Simulator.Main list");
        System.out.println("\nConfiguration:");
        System.out.println("  Edit simulation-config.properties or create custom config files in configs/");
        System.out.println("  No recompilation needed when changing configuration files!");
    }

    /**
     * Creates a new simulation and runs all topologies
     */
    private void createNewSimulation(String simulationName) {
        System.out.println("\n=== Creating New Simulation: " + simulationName + " ===");

        // Reset topology index at the start of simulation
        SkipSimParameters.resetTopologyIndex();

        // Save simulation name to database
        simDB.saveSimulationName(simulationName, SkipSimParameters.getSimulationType());

        int totalTopologies = SkipSimParameters.getTopologies();
        System.out.println("Number of topologies to generate: " + totalTopologies);
        System.out.println("Simulation type: " + SkipSimParameters.getSimulationType());

        if (SkipSimParameters.getSimulationType().equals(Constants.SimulationType.DYNAMIC)
                || SkipSimParameters.getSimulationType().equals(Constants.SimulationType.BLOCKCHAIN)) {
            System.out.println("Simulation lifetime: " + SkipSimParameters.getLifeTime() + " hours");
        }

        System.out.println();

        // Run simulation for each topology
        for (int topologyIndex = 0; topologyIndex < totalTopologies; topologyIndex++) {
            SkipSimParameters.incrementSimIndex();

            try {
                System.out.println(">>> Topology " + (topologyIndex + 1) + "/" + totalTopologies);

                // Initialize replica flags
                Arrays.fill(isReplica, false);

                // Save topology name and get topology ID
                int topologyId = simDB.saveTopologyName(SkipSimParameters.getCurrentTopologyIndex(), simulationName);

                // Create Skip Graph operations
                SkipGraphOperations sgo = new SkipGraphOperations(isBlockChain);

                // Generate landmarks for the topology
                sgo.getTG().mLandmarks.generatingLandmarks();

                // Run simulation based on type
                if (SkipSimParameters.getSimulationType().equals(Constants.SimulationType.LANDMARK)) {
                    System.out.println("    Running landmark placement simulation...");
                    new landmarkSimulation(sgo, Constants.Topology.GENERATE);

                } else if (SkipSimParameters.getSimulationType().equals(Constants.SimulationType.STATIC)) {
                    System.out.println("    Running static simulation...");
                    new staticSimulation(sgo, Constants.Topology.GENERATE);

                } else if (SkipSimParameters.getSimulationType().equals(Constants.SimulationType.DYNAMIC)
                        || SkipSimParameters.getSimulationType().equals(Constants.SimulationType.BLOCKCHAIN)) {
                    System.out.println("    Running dynamic/blockchain simulation...");

                    // Run simulation for each time slot
                    for (int time = 0; time < SkipSimParameters.getLifeTime(); time++) {
                        DynamicSimulation ds = new DynamicSimulation(sgo);
                        ArrayList<ChurnDBEntry> churnLog = new ArrayList<>();
                        sgo = ds.Simulate(Constants.Topology.GENERATE, time, churnLog);
                        simDB.saveChurnLogToDB(churnLog, topologyId, time);

                        // Print progress every 10% or at least every 24 hours
                        int progressInterval = Math.max(1, SkipSimParameters.getLifeTime() / 10);
                        if ((time + 1) % progressInterval == 0 || time == SkipSimParameters.getLifeTime() - 1) {
                            int percentComplete = (int) ((time + 1) * 100.0 / SkipSimParameters.getLifeTime());
                            System.out.println("    Time: " + (time + 1) + "/" + SkipSimParameters.getLifeTime()
                                    + " (" + percentComplete + "%)");
                        }
                    }
                }

                // Save topology to database
                simDB.saveSkipGraph(sgo, topologyId);
                System.out.println("    Topology saved successfully");

            } catch (Exception ex) {
                System.err.println("Error during simulation:");
                ex.printStackTrace();
                System.exit(1);
            }
        }

        System.out.println("\n=== Simulation Complete ===");
        System.out.println("Simulation '" + simulationName + "' has been created successfully.");
    }

    /**
     * Loads and runs an existing simulation from the database.
     * If the simulation doesn't exist, it will be created automatically.
     */
    private void loadSimulation(String simulationName) {
        System.out.println("\n=== Loading Simulation: " + simulationName + " ===");

        // Reset topology index at the start of simulation
        SkipSimParameters.resetTopologyIndex();

        // Fetch simulation type from database to determine which type to load
        String simType = SkipSimParameters.getSimulationType().equals(Constants.SimulationType.BLOCKCHAIN)
                ? Constants.SimulationType.DYNAMIC
                : SkipSimParameters.getSimulationType();

        // Check if simulation exists
        Vector<String> availableSimulations = simDB.fetchSimulationNamesFromDB(simType);
        if (!availableSimulations.contains(simulationName)) {
            System.out.println("Simulation '" + simulationName + "' not found in database.");
            System.out.println("Creating new simulation...\n");
            createNewSimulation(simulationName);
            System.out.println("\n=== Now Loading the Created Simulation ===\n");
            // Reset topology index again after creation before loading
            SkipSimParameters.resetTopologyIndex();
        }

        int totalTopologies = SkipSimParameters.getTopologies();
        System.out.println("Number of topologies to load: " + totalTopologies);
        System.out.println("Simulation type: " + SkipSimParameters.getSimulationType());

        if (SkipSimParameters.getSimulationType().equals(Constants.SimulationType.DYNAMIC)
                || SkipSimParameters.getSimulationType().equals(Constants.SimulationType.BLOCKCHAIN)) {
            System.out.println("Simulation lifetime: " + SkipSimParameters.getLifeTime() + " hours");
        }

        System.out.println();

        // Load and run each topology
        for (int topologyIndex = 0; topologyIndex < totalTopologies; topologyIndex++) {
            SkipSimParameters.incrementSimIndex();

            try {
                System.out.println(">>> Topology " + (topologyIndex + 1) + "/" + totalTopologies);

                // Initialize replica flags
                Arrays.fill(isReplica, false);

                // Load Skip Graph from database
                SkipGraphOperations sgo = simDB.fetchSkipGraphFromDB(
                        SkipSimParameters.getCurrentTopologyIndex(),
                        simulationName,
                        isBlockChain);

                // Run simulation based on type
                if (SkipSimParameters.getSimulationType().equals(Constants.SimulationType.LANDMARK)) {
                    System.out.println("    Running landmark placement simulation...");
                    new landmarkSimulation(sgo, Constants.Topology.LOAD);

                } else if (SkipSimParameters.getSimulationType().equals(Constants.SimulationType.STATIC)) {
                    System.out.println("    Running static simulation...");
                    new staticSimulation(sgo, Constants.Topology.LOAD);

                } else if (SkipSimParameters.getSimulationType().equals(Constants.SimulationType.DYNAMIC)
                        || SkipSimParameters.getSimulationType().equals(Constants.SimulationType.BLOCKCHAIN)) {
                    System.out.println("    Running dynamic/blockchain simulation...");

                    // Get topology ID for loading churn logs
                    int topologyId = simDB.fetchTopologyIDFromDB(
                            SkipSimParameters.getCurrentTopologyIndex(),
                            simulationName);

                    // Run simulation for each time slot
                    for (int time = 0; time < SkipSimParameters.getLifeTime(); time++) {
                        ArrayList<ChurnDBEntry> churnLog = simDB.fetchChurnLogFromDB(topologyId, time);
                        DynamicSimulation ds = new DynamicSimulation(sgo);
                        sgo = ds.Simulate(Constants.Topology.LOAD, time, churnLog);

                        // Print progress every 10% or at least every 24 hours
                        int progressInterval = Math.max(1, SkipSimParameters.getLifeTime() / 10);
                        if ((time + 1) % progressInterval == 0 || time == SkipSimParameters.getLifeTime() - 1) {
                            int percentComplete = (int) ((time + 1) * 100.0 / SkipSimParameters.getLifeTime());
                            System.out.println("    Time: " + (time + 1) + "/" + SkipSimParameters.getLifeTime()
                                    + " (" + percentComplete + "%)");
                        }
                    }
                }

                System.out.println("    Topology loaded successfully");

            } catch (Exception ex) {
                System.err.println("Error during simulation:");
                ex.printStackTrace();
                System.exit(1);
            }
        }

        System.out.println("\n=== Simulation Complete ===");
        System.out.println("Simulation '" + simulationName + "' has been loaded and executed successfully.");
    }

    /**
     * Lists all available simulations in the database
     */
    private void listSimulations() {
        System.out.println("\n=== Available Simulations ===");

        // Get simulations for current simulation type
        String simType = SkipSimParameters.getSimulationType().equals(Constants.SimulationType.BLOCKCHAIN)
                ? Constants.SimulationType.DYNAMIC
                : SkipSimParameters.getSimulationType();

        Vector<String> simulations = simDB.fetchSimulationNamesFromDB(simType);

        if (simulations.isEmpty()) {
            System.out.println("No simulations found for type: " + simType);
        } else {
            System.out.println("Simulation type: " + simType);
            System.out.println("Total simulations: " + simulations.size());
            System.out.println();
            for (String sim : simulations) {
                System.out.println("  - " + sim);
            }
        }

        System.out.println();
    }

    /**
     * Deletes a simulation from the database
     */
    private void deleteSimulation(String simulationName) {
        System.out.println("\n=== Deleting Simulation: " + simulationName + " ===");

        // Get simulations for current simulation type
        String simType = SkipSimParameters.getSimulationType().equals(Constants.SimulationType.BLOCKCHAIN)
                ? Constants.SimulationType.DYNAMIC
                : SkipSimParameters.getSimulationType();

        Vector<String> availableSimulations = simDB.fetchSimulationNamesFromDB(simType);

        if (!availableSimulations.contains(simulationName)) {
            System.err.println("Error: Simulation '" + simulationName + "' not found");
            System.exit(1);
        }

        // Confirm deletion
        System.out.print("Are you sure you want to delete '" + simulationName + "'? (yes/no): ");
        Scanner scanner = new Scanner(System.in);
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if (confirmation.equals("yes") || confirmation.equals("y")) {
            simDB.deleteSimulationFromDB(simulationName);
            System.out.println("Simulation '" + simulationName + "' has been deleted successfully.");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }
}
