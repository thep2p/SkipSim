# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SkipSim is a simulator for designing, implementing, and evaluating distributed Skip Graph-based protocols, particularly focused on blockchain implementations using the Skip Graph data structure. The simulator supports various experiments including Proof-of-Validation consensus, replication strategies, churn stabilization, and availability-based protocols.

## Build and Run

This is an IntelliJ IDEA project with Java sources in `src/main/java/`. There is no Maven or Gradle build system configured - the project uses IntelliJ's native build system.

**To build and run in IntelliJ:**
- Open the project in IntelliJ IDEA
- Ensure JDK is configured (JavaFX required for GUI)
- Main entry point: `Simulator.GUI.main()` for GUI mode
- Build using IntelliJ's Build > Build Project

**Running experiments:**
The simulator can either load existing simulations or generate new ones. When running, you'll be prompted to:
1. Load a simulation (e.g., `100_1024_DEBIAN_1W`) from a SQLite database
2. Or create a new simulation with specified parameters

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

### Simulation Schema System

**All simulations are configured via simulation schemas** in the `SimulationSchema` package:
1. Create a class extending `SkipSimParameters` (e.g., `Blockchain`, `StaticReplication`, `MultiObjectiveReplication`)
2. In the constructor, set simulation parameters (system capacity, churn model, experiment flags, etc.)
3. Register the schema in `SimulationSchema.SchemaManager` constructor

Example:
```java
public class SchemaManager {
    public SchemaManager() {
        new Blockchain(); // Active schema
    }
}
```

The schema system controls:
- Simulation type (STATIC, DYNAMIC, BLOCKCHAIN)
- System parameters (capacity, lifetime, landmarks)
- Churn models (Debian Fast/Slow, Flatout)
- Experiment flags
- Replication algorithms
- Name ID assignment strategies

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
See `JUNIT_SETUP.md` for detailed instructions. Quick setup in IntelliJ:
1. Open a test file
2. Click on red `@Test` annotation
3. Alt+Enter → "Add JUnit 4 to classpath"

**Running Tests:**
```bash
# In IntelliJ: Right-click on test class → Run 'TransactionInsertionTest'
# Or click green arrow next to test method/class

# From command line:
java -cp "libs/*:out" org.junit.runner.JUnitCore SkipGraph.TransactionInsertionTest
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
