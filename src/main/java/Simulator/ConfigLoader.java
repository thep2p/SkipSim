package Simulator;

import DataTypes.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads simulation parameters from configuration file.
 * This eliminates the need for multiple schema classes.
 *
 * Usage:
 *   ConfigLoader.loadFromFile("simulation-config.properties");
 *   ConfigLoader.loadFromFile("configs/large-experiment.properties");
 */
public class ConfigLoader {
    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);

    /**
     * Load configuration from a properties file.
     * @param configPath Path to the configuration file
     */
    public static void loadFromFile(String configPath) {
        Properties props = new Properties();

        try (InputStream input = new FileInputStream(configPath)) {
            props.load(input);
            applyConfiguration(props);
            log.info("Configuration loaded from: {}", configPath);
        } catch (IOException e) {
            log.warn("Could not load config file: {}", configPath);
            log.warn("Using default parameters or command-line overrides");
        }
    }

    /**
     * Load configuration with command-line overrides.
     * @param configPath Path to the configuration file
     * @param overrides Properties to override (from command line)
     */
    public static void loadFromFile(String configPath, Properties overrides) {
        Properties props = new Properties();

        try (InputStream input = new FileInputStream(configPath)) {
            props.load(input);
            // Apply command-line overrides
            if (overrides != null) {
                props.putAll(overrides);
            }
            applyConfiguration(props);
        } catch (IOException e) {
            System.err.println("Error loading config: " + e.getMessage());
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    /**
     * Apply configuration properties to SkipSimParameters.
     */
    private static void applyConfiguration(Properties props) {
        // Simulation Type
        String simType = props.getProperty("simulation.type", "BLOCKCHAIN");
        SkipSimParameters.setSimulationType(simType);

        // System Parameters
        SkipSimParameters.setSystemCapacity(
            getIntProperty(props, "system.capacity", 64));
        SkipSimParameters.setLifeTime(
            getIntProperty(props, "system.lifetime", 24));
        SkipSimParameters.setTopologies(
            getIntProperty(props, "system.topologies", 2));

        // Blockchain Parameters
        SkipSimParameters.TXB_RATE =
            getIntProperty(props, "txb.rate", 1);

        String protocol = props.getProperty("blockchain.protocol", "LIGHTCHAIN");
        SkipSimParameters.setBlockchainProtocol(protocol);

        // Churn Model - map user-friendly names to constant values
        String churnModel = props.getProperty("churn.model", "DEBIAN_FAST");
        String churnModelConstant = mapChurnModel(churnModel);
        SkipSimParameters.setChurnModel(churnModelConstant);

        String churnType = props.getProperty("churn.type", "ADVERSARIAL");
        String churnTypeConstant = mapChurnType(churnType);
        SkipSimParameters.setChurnType(churnTypeConstant);

        // Security Parameters
        SkipSimParameters.setMaliciousFraction(
            getFloatProperty(props, "malicious.fraction", 0.16f));
        SkipSimParameters.setValidatorThreshold(
            getIntProperty(props, "validator.threshold", 12));
        SkipSimParameters.setSignatureThreshold(
            getIntProperty(props, "signature.threshold", 1));

        // Logging
        SkipSimParameters.LOG =
            getBooleanProperty(props, "logging.enabled", true);
    }

    private static int getIntProperty(Properties props, String key, int defaultValue) {
        String value = props.getProperty(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid integer for " + key + ": " + value);
            return defaultValue;
        }
    }

    private static float getFloatProperty(Properties props, String key, float defaultValue) {
        String value = props.getProperty(key);
        if (value == null) return defaultValue;
        try {
            return Float.parseFloat(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid float for " + key + ": " + value);
            return defaultValue;
        }
    }

    private static boolean getBooleanProperty(Properties props, String key, boolean defaultValue) {
        String value = props.getProperty(key);
        if (value == null) return defaultValue;
        return Boolean.parseBoolean(value.trim());
    }

    /**
     * Map user-friendly churn model names to constant values.
     */
    private static String mapChurnModel(String model) {
        switch (model.toUpperCase()) {
            case "DEBIAN_FAST":
            case "FASTDEBIAN":
                return Constants.Churn.Model.Debian.Fast.Name;
            case "DEBIAN_SLOW":
            case "SLOWDEBIAN":
                return Constants.Churn.Model.Debian.Slow.Name;
            case "FLATOUT":
                return Constants.Churn.Model.Flatout.Name;
            default:
                System.err.println("Unknown churn model: " + model + ", using DEBIAN_FAST");
                return Constants.Churn.Model.Debian.Fast.Name;
        }
    }

    /**
     * Map user-friendly churn type names to constant values.
     */
    private static String mapChurnType(String type) {
        switch (type.toUpperCase()) {
            case "ADVERSARIAL":
                return Constants.Churn.Type.ADVERSARIAL;
            case "COOPERATIVE":
                return Constants.Churn.Type.COOPERATIVE;
            default:
                System.err.println("Unknown churn type: " + type + ", using ADVERSARIAL");
                return Constants.Churn.Type.ADVERSARIAL;
        }
    }
}
