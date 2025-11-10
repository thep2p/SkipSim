# SkipSim Configuration Guide

This guide explains the new configuration file system that eliminates the need for code recompilation when changing simulation parameters.

## Quick Summary

**Old way (Required recompilation):**
```java
// Edit SchemaManager.java
new BlockchainSmall();  // Change this line
// Recompile: make clean && make compile
```

**New way (No recompilation!):**
```bash
# Edit config file
nano simulation-config.properties
# Run immediately
make run-load NAME=my_sim
```

## Configuration Files

### Default Configuration
`simulation-config.properties` - Automatically loaded if no config is specified

### Predefined Configurations
- `configs/quick-test.properties` - Small test (64 nodes, 2 topologies, 24 hours)
- `configs/full-experiment.properties` - Full research (1024 nodes, 100 topologies, 168 hours)

### Using Configuration Files

```bash
# Method 1: Use default config
make run-load NAME=my_simulation

# Method 2: Use custom config
make run-load NAME=my_sim CONFIG=configs/quick-test.properties

# Method 3: Direct Java execution
java -cp "libs/*:out/production" Simulator.Main load my_sim --config configs/full-experiment.properties
```

## Configuration Parameters

### System Parameters
```properties
simulation.type=BLOCKCHAIN          # BLOCKCHAIN, DYNAMIC, STATIC, LANDMARK
system.capacity=64                  # Number of nodes in the network
system.lifetime=24                  # Simulation duration in hours
system.topologies=2                 # Number of independent topology instances
```

### Blockchain Parameters
```properties
txb.rate=1                          # Transactions generated per node per hour
blockchain.protocol=LIGHTCHAIN      # Protocol to use (default: LIGHTCHAIN)
```

### Churn Parameters
```properties
churn.model=DEBIAN_FAST            # Options: DEBIAN_FAST, DEBIAN_SLOW, FLATOUT
churn.type=ADVERSARIAL             # Options: ADVERSARIAL, COOPERATIVE
```

### Security Parameters
```properties
malicious.fraction=0.16            # Fraction of malicious nodes (0.16 = 16%)
validator.threshold=12             # Number of validators to search for
signature.threshold=1              # Minimum honest validators required
```

### Other Parameters
```properties
logging.enabled=true               # Enable/disable logging
```

## Creating Custom Configurations

1. **Copy an existing config:**
```bash
cp configs/quick-test.properties configs/my-experiment.properties
```

2. **Edit parameters:**
```bash
nano configs/my-experiment.properties
```

3. **Run with your config:**
```bash
make run-load NAME=my_experiment CONFIG=configs/my-experiment.properties
```

## Migration from Schema Classes

If you were using schema classes (Blockchain.java, BlockchainSmall.java, etc.), you can continue to use them OR migrate to config files:

### Option 1: Keep Using Schema Classes
Edit `src/main/java/SimulationSchema/SchemaManager.java`:
```java
public SchemaManager() {
    // Delete or comment out the config file line
    // this("simulation-config.properties");

    // Use schema class instead
    new BlockchainSmall();
}
```

### Option 2: Migrate to Config Files (Recommended)
1. Delete `simulation-config.properties` if you want SchemaManager to use schema classes
2. OR create a config file with equivalent parameters

**Example: BlockchainSmall.java equivalent**
```properties
simulation.type=BLOCKCHAIN
system.capacity=64
system.lifetime=24
system.topologies=2
txb.rate=1
churn.model=DEBIAN_FAST
churn.type=ADVERSARIAL
malicious.fraction=0.16
```

## Advantages of Configuration Files

✅ **No recompilation** - Change parameters instantly
✅ **Multiple configurations** - Easy to maintain different experiment setups
✅ **Version control friendly** - Track configuration changes
✅ **User-friendly** - No Java knowledge required
✅ **Command-line overrides** - Specify config file per run

## Examples

### Quick Development Testing
```bash
# Create a small test config
cat > configs/dev-test.properties <<EOF
simulation.type=BLOCKCHAIN
system.capacity=16
system.lifetime=6
system.topologies=1
txb.rate=1
churn.model=DEBIAN_FAST
malicious.fraction=0.16
EOF

# Run it
make run-load NAME=dev_test CONFIG=configs/dev-test.properties
```

### Large-Scale Experiment
```bash
# Edit full experiment config
nano configs/full-experiment.properties
# Change: system.topologies=200

# Run without recompiling!
make run-load NAME=large_scale CONFIG=configs/full-experiment.properties
```

### Comparing Different Churn Models
```bash
# Test with DEBIAN_FAST
make run-load NAME=exp_fast CONFIG=configs/quick-test.properties

# Create DEBIAN_SLOW variant
cp configs/quick-test.properties configs/slow-churn.properties
sed -i 's/DEBIAN_FAST/DEBIAN_SLOW/' configs/slow-churn.properties

# Test with DEBIAN_SLOW (no recompilation!)
make run-load NAME=exp_slow CONFIG=configs/slow-churn.properties
```

## Troubleshooting

### Config file not found
```
Config file not found: my-config.properties
Using default schema class (BlockchainSmall)
```
**Solution:** Check the file path. Config paths are relative to the project root.

### Parameter not changing
```
System capacity: 1024  (expected 64)
```
**Solution:** Make sure the config file exists and the parameter name is correct (case-sensitive).

### Compilation errors after adding setters
If you modified SkipSimParameters and get compilation errors:
```bash
make clean && make compile
```

## Advanced: Adding New Parameters

To add support for new parameters in config files:

1. **Add setter to SkipSimParameters.java:**
```java
protected static int MyNewParameter = 10;

public static void setMyNewParameter(int value) {
    MyNewParameter = value;
}
```

2. **Add to ConfigLoader.java:**
```java
SkipSimParameters.setMyNewParameter(
    getIntProperty(props, "my.new.parameter", 10));
```

3. **Use in config files:**
```properties
my.new.parameter=20
```

4. **Recompile once:**
```bash
make clean && make compile
```

5. **Now change the parameter without recompiling!**
