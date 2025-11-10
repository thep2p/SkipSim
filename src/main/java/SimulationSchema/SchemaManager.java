package SimulationSchema;

import Simulator.ConfigLoader;
import java.io.File;

/**
 * SchemaManager loads simulation configuration from either:
 * 1. A configuration file (recommended) - simulation-config.properties
 * 2. Hardcoded schema classes (legacy) - Blockchain, BlockchainSmall, etc.
 *
 * The config file approach is more flexible and doesn't require recompilation.
 */
public class SchemaManager
{
    /**
     * Default constructor - tries to load from config file, falls back to schema class.
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
        // Try to load from config file first
        File configFile = new File(configPath);
        if (configFile.exists()) {
            System.out.println("Loading configuration from: " + configPath);
            ConfigLoader.loadFromFile(configPath);
        } else {
            System.out.println("Config file not found: " + configPath);
            System.out.println("Using default schema class (BlockchainSmall)");

            // Fallback to hardcoded schema for backward compatibility
            new BlockchainSmall();

            // Alternative schemas (uncomment to use):
            // new Blockchain();
            // new MultiObjectiveReplication();
            // new StaticReplication();
        }
    }
}
