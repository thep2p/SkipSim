# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SkipSim is a simulator for designing, implementing, and evaluating distributed Skip Graph-based protocols, particularly focused on blockchain implementations using the Skip Graph data structure. The simulator supports various experiments including Proof-of-Validation consensus, replication strategies, churn stabilization, and availability-based protocols.

## Build and Run

This is a Maven-based Java project with sources in `src/main/java/`. The project uses Maven for dependency management and build automation, with a Makefile wrapper for convenience.

**First-time setup:**
```bash
# Download all dependencies from Maven Central
make install
# Or use Maven directly:
mvn dependency:resolve
```

**Command-line build and run:**
```bash
# Compile the project
make compile

# Run tests
make test

# Run a simulation using Makefile
make run-list
make run-load NAME=my_simulation
make run-new NAME=new_simulation

# Or use Maven directly:
mvn compile
mvn test
mvn exec:java -Dexec.args="list"
mvn exec:java -Dexec.args="load my_simulation"
```

**To build and run in IntelliJ:**
- Open the project in IntelliJ IDEA
- IntelliJ will automatically detect the Maven `pom.xml` and import dependencies
- Ensure JDK 11 or higher is configured
- Main entry point: `Simulator.Main.main()`
- Build using IntelliJ's Build > Build Project

**Running simulations:**
The simulator supports four main operations via command-line:

1. **Create a new simulation:**
   ```bash
   make run-new NAME=<simulation-name>
   # Or: mvn exec:java -Dexec.args="new <simulation-name>"
   ```

2. **Load an existing simulation:**
   ```bash
   make run-load NAME=<simulation-name>
   # Or: mvn exec:java -Dexec.args="load <simulation-name>"
   ```

3. **List available simulations:**
   ```bash
   make run-list
   # Or: mvn exec:java -Dexec.args="list"
   ```

4. **Delete a simulation:**
   ```bash
   make run-delete NAME=<simulation-name>
   # Or: mvn exec:java -Dexec.args="delete <simulation-name>"
   ```

Simulations are stored in a SQLite database (`skipsim3db.db`). When creating a new simulation, the system generates the specified number of topologies based on your schema configuration. When loading an existing simulation, it replays the simulation using stored topology and churn data.

## Core Architecture

### Skip Graph Structure

The simulator operates on dual Skip Graph overlays:
- **Node Skip Graph**: Peer network overlay (accessible via `SkipGraphOperations.getTG().getNodeSet()`)
- **Transaction Skip Graph**: Transaction/block overlay (accessible via `SkipGraphOperations.getTransactions()`)

Key classes:
- `SkipGraph.SkipGraphOperations`: Core operations for Skip Graph (search, insertion)
- `SkipGraph.Node`: Represents a peer node in the network
- `Blockchain.LightChain.Transaction`: Represents a transaction in the blockchain overlay
- `SkipGraph.TopologyGenerator`: Generates network topologies with geographic coordinates

### Configuration System

**All simulations are configured via `.properties` files**:
- Default: `simulation-config.properties`
- Examples: `configs/quick-test.properties`, `configs/full-experiment.properties`

Configuration files control:
- Simulation type (STATIC, DYNAMIC, BLOCKCHAIN)
- System parameters (capacity, lifetime, landmarks)
- Churn models (Debian Fast/Slow, Flatout)
- Experiment flags
- Replication algorithms
- Name ID assignment strategies

Example configuration:
```properties
simulation.type=BLOCKCHAIN
system.capacity=1024
system.lifetime=168
system.topologies=100
churn.model=DEBIAN_FAST
churn.type=ADVERSARIAL
malicious.fraction=0.16
validator.threshold=12
signature.threshold=1
```

Configuration is loaded automatically by `Main.java` at startup. No recompilation needed when changing parameters.

### Key Parameter Configuration

Critical parameters in `SkipSimParameters.java`:
- `SystemCapacity`: Number of nodes in the system (e.g., 1024)
- `LifeTime`: Simulation duration in hours (e.g., 168 for one week)
- `ValidatorThreshold`: Number of validators to search for in Proof-of-Validation
- `SignatureThreshold`: Minimum honest validators/introducers required
- `MaliciousFraction`: Proportion of malicious nodes (e.g., 0.16f = 16%)
- `LandmarksNum`: Number of landmarks, typically log2(SystemCapacity)
- `NameIDLength`: Minimum name ID length, typically log2(SystemCapacity)

### Experiment Types

Enable experiments via boolean flags in `SkipSimParameters`:
- `MaliciousSuccessExperiment`: Probability of acquiring malicious validators
- `EfficiencyExperiment`: Average number of honest validators acquired
- `AvailabilityExperiment`: Average online replicas over time
- `OnlineProbabilityExperiment`: Node online probability based on churn
- `btsMaliciousSuccessExperiment`: Randomized bootstrapping malicious success rate
- `btsEfficiencyExperiment`: Average honest view introducers acquired

Located in `Blockchain.LightChain.Experiments/` package.

### Search Operations

**Searching by Numerical ID** (for both peers and transactions):
- Determine search direction (LEFT_SEARCH_DIRECTION or RIGHT_SEARCH_DIRECTION) based on target vs. initiator position
- For peer search: Use initiator's numerical ID directly
- For transaction search: First find the most similar transaction owned by initiator, then use that as starting point
- Call `SkipGraphOperations.SearchByNumID()` with appropriate parameters

Search starts from the uppermost level (`SkipSimParameters.getLookupTableSize()-1`) and proceeds downward.

### Major Functional Areas

**Replication Algorithms** (`Replication/` package):
- 15+ replication strategies (LP, LARAS, GLARAS, Pyramid, ClusterBased, etc.)
- Both public and private/protected replication types
- Supports static and dynamic replication with availability prediction
- Algorithms numbered as Rep_Alg01 through Rep_Alg15

**Availability Prediction** (`AvailabilityPrediction/` package):
- LUDP (Lifetime predictor)
- BruijnGraph and SlidingBruijnGraph for state-based prediction
- Used for churn stabilization and dynamic replication

**Churn Stabilization** (`ChurnStabilization/` package):
- Supports COOPERATIVE and ADVERSARIAL churn types
- Algorithms: DKS, Kademlia, Interlace, Tornado
- Backup tables for adversarial scenarios

**Name ID Assignment** (`NameIDAssignment/` package):
- Multiple strategies: LANS, LAND, LMDS, DPLMDS, DPAD, LDHT, Hierarchical
- Affects locality awareness and search efficiency

**Aggregation** (`Aggregation/` package):
- ELATS, BroadcastTree, PrefixTree algorithms
- Blockchain availability aggregation

**Database Layer** (`DataBase/` package):
- SQLite-based persistence for simulations, topologies, churn data
- Schema classes: SimulationsSchema, NodesDBSchema, ChurnDBSchema, TopologiesDBSchema, LandmarksDBSchema

## Constants and Configuration

`DataTypes.Constants` provides all configuration string constants:
- Simulation types, churn models, protocols
- Replication/aggregation algorithm names
- Name ID assignment strategy names
- Use these constants instead of hardcoded strings

## Testing

### Test Infrastructure

Test code is located in `src/test/java/`. The project uses **JUnit 4** for testing.

**Test Fixtures** (`TestFixtures` package):
- `SkipGraphTestFixture`: Reusable fixture for generating Skip Graph test data

**Setting up JUnit:**
JUnit 4 is managed by Maven and defined in `pom.xml`. Maven will automatically download JUnit (4.13.2) and Hamcrest (1.3) when you run `make install` or `mvn dependency:resolve`.

IntelliJ IDEA will automatically detect Maven dependencies and configure the classpath.

**Running Tests:**
```bash
# Using Makefile
make test

# Using Maven directly
mvn test

# Run with verbose output
make test-verbose
# Or: mvn test -X

# In IntelliJ: Right-click on test class → Run 'TransactionInsertionTest'
# Or click green arrow next to test method/class
```

### Using Test Fixtures

The `SkipGraphTestFixture` provides convenient methods for creating test scenarios:

```java
public class MyTest {
    private SkipGraphTestFixture fixture;

    @Before
    public void setUp() {
        fixture = new SkipGraphTestFixture();
    }

    @Test
    public void testSomething() {
        // Create a network with nodes and transactions
        TestNetwork network = fixture.createTestNetwork(20, 3); // 20 nodes, 3 txs each

        // Or manually create components
        SkipGraphOperations ops = fixture.createSkipGraphOperations();
        List<Node> nodes = fixture.createAndInsertNodes(10);
        Transaction tx = fixture.createAndInsertTransaction(nodes.get(0), 1, 0);

        // Assertions
        assertNotNull("Transaction should exist", tx);
        assertEquals("Owner should match", 0, tx.getOwnerIndex());
    }
}
```

**Key Fixture Methods:**
- `createSkipGraphOperations()`: Creates a SkipGraphOperations instance with blockchain mode
- `createAndInsertNodes(int numNodes)`: Creates and inserts nodes into the Skip Graph
- `createAndInsertTransaction(Node owner, int txIndex, int time)`: Safely creates and inserts a transaction
- `createTestNetwork(int nodes, int txsPerNode)`: Creates a complete test network

**Important:** Always use `createAndInsertTransaction()` rather than manually creating transactions. This ensures proper insertion order and prevents NullPointerExceptions.

### Writing New Tests

To create a new test class:

1. Create the test class in `src/test/java/` (match the package structure of the code under test)
2. Import JUnit annotations and assertions:
```java
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
```
3. Add `@Before` setup method for fixture initialization
4. Write test methods with `@Test` annotation
5. Use JUnit assertions: `assertEquals()`, `assertTrue()`, `assertNotNull()`, etc.

Example:
```java
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MyFeatureTest {
    private SkipGraphTestFixture fixture;

    @Before
    public void setUp() {
        fixture = new SkipGraphTestFixture();
    }

    @Test
    public void testMyFeature() {
        // Arrange
        List<Node> nodes = fixture.createAndInsertNodes(10);

        // Act
        Node node = nodes.get(5);

        // Assert
        assertNotNull("Node should exist", node);
        assertEquals("Node index should be 5", 5, node.getIndex());
    }
}
```

### Existing Tests

- `SkipGraph.TransactionInsertionTest`: Tests for transaction insertion, including regression tests for the transaction insertion NullPointerException bug

## Important Implementation Notes

- **Numerical ID Hashing**: `NumIDHashing = true` is required for Proof-of-Validation and randomized bootstrapping
- **Lookup Table Size**: Automatically computed as `ceil(NameIDLength + log2(SystemCapacity))`
- **Transaction Insertion Order**: Critical bug fix - transactions MUST be inserted into Skip Graph BEFORE being added to owner's txSet. This prevents NullPointerException during insertion when `mostSimilarTXB()` searches through the owner's transactions.
- **Transaction Insertion**: When a peer arrives, all its transactions must be re-inserted into the transaction Skip Graph
- **Time Slots**: Simulations operate in hourly time slots for dynamic/blockchain modes
- **Transaction Rate**: `TXB_RATE` controls transactions generated per node per time slot
- **Geographic Model**: Nodes have 2D coordinates in a domain of size `DomainSize × DomainSize`

## Documentation Guidelines

### Keeping README.md Updated

**IMPORTANT**: When making significant changes to the codebase, ensure that `README.md` remains accurate and up-to-date.

**Update README.md when:**
- Changing how the project is built or configured
- Adding/removing major features or components
- Modifying the project structure
- Changing how users interact with the simulator
- Updating configuration approaches or command-line interfaces

**DO NOT update README.md for:**
- Minor bug fixes that don't change user-facing behavior
- Internal refactoring that doesn't affect usage
- Implementation details that don't impact the API

**Guidelines:**
- Keep changes concise - README.md is for users, not exhaustive documentation
- Update relevant sections only (don't rewrite the entire file)
- Ensure Quick Start examples remain valid and tested
- Verify that configuration examples match actual config files
- Remove outdated references to deprecated approaches

### Configuration-First Approach

SkipSim uses configuration files exclusively. The legacy schema class approach has been removed. All configuration must be done via `.properties` files:

- Default: `simulation-config.properties`
- Examples: `configs/quick-test.properties`, `configs/full-experiment.properties`

When documenting features, always show the configuration file approach, not Java code modification.
