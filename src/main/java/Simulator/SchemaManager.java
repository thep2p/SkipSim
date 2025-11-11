package Simulator;

import java.io.File;

/**
 * SchemaManager loads simulation configuration from .properties files.
 * All simulation parameters should be configured via property files.
 */
public class SchemaManager
{
    /**
     * Default constructor - loads from default config file.
     */
    public SchemaManager()
    {
        this("simulation-config.properties");
    }

    /**
     * Constructor with specific config file path.
     * @param configPath Path to configuration file
     */
    public SchemaManager(String configPath)
    {
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
}
