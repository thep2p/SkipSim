# SkipSim

A comprehensive simulator for designing, implementing, and evaluating distributed Skip Graph-based protocols for peer-to-peer networks and blockchain systems.

## What is SkipSim?

SkipSim is a research-oriented simulator that enables the design and evaluation of distributed protocols built on Skip Graphs, a structured peer-to-peer overlay network. It's particularly suited for:

- **Blockchain implementations** using Skip Graph dual overlays (one for peers, one for transactions)
- **Consensus protocols** including Proof-of-Validation
- **Churn resilience** testing with realistic failure models
- **Replication strategies** for availability and fault tolerance
- **Performance evaluation** of search, insertion, and lookup operations

SkipSim provides a realistic simulation environment with geographic node placement, configurable churn models (based on real-world data from Debian mirror analysis), and comprehensive metrics collection.

## Features

- **Dual Skip Graph Overlays** - Separate overlays for peer nodes and blockchain transactions
- **Proof-of-Validation Consensus** - Lightweight consensus protocol for resource-constrained environments
- **Realistic Churn Models** - DEBIAN_FAST, DEBIAN_SLOW, and FLATOUT models based on real trace data
- **15+ Replication Algorithms** - LP, LARAS, GLARAS, Pyramid, ClusterBased, and more
- **Multiple Experiments** - Efficiency, availability, malicious success rate, online probability
- **Geographic Modeling** - 2D coordinate system with configurable domain size
- **Flexible Configuration** - Property files or Java schemas
- **Comprehensive Testing** - JUnit-based test infrastructure with fixtures
- **Database Persistence** - SQLite storage for simulations, topologies, and churn data

## Quick Start

### Prerequisites

- Java Development Kit (JDK) 11 or higher
- Maven 3.6+ (for dependency management)
- Git (for cloning the repository)

### Installation

```bash
# Clone the repository
git clone https://github.com/yhassanzadeh13/SkipSim.git
cd SkipSim

# Download dependencies and compile
make install
make compile

# Verify installation
make test
```

### Running Your First Simulation

**Step 1: Choose a configuration**

SkipSim comes with predefined configurations:
- `configs/quick-test.properties` - Small test (64 nodes, 2 topologies, 24 hours)
- `configs/full-experiment.properties` - Full research (1024 nodes, 100 topologies, 168 hours)

**Step 2: Run the simulation**

```bash
# Quick test (finishes in minutes)
make run-load NAME=my_first_sim CONFIG=configs/quick-test.properties

# The simulator will:
# 1. Generate network topologies
# 2. Simulate node churn over time
# 3. Run blockchain transactions
# 4. Collect experiment metrics
# 5. Output results to console
```

**Step 3: View available simulations**

```bash
# List all simulations in the database
make run-list

# Load an existing simulation
make run-load NAME=my_first_sim

# Delete a simulation
make run-delete NAME=old_simulation
```

### Understanding Results

SkipSim outputs experiment results to the console, including:

- **Efficiency**: Average number of honest validators acquired per transaction
- **Malicious Success Rate**: Probability of acquiring enough malicious validators
- **Availability**: Average number of online replicas over time
- **Search Performance**: Average hops for numerical and name ID searches

Results are organized by time slot (hourly intervals) and topology instance.

## Configuration

### Using Configuration Files (Recommended)

Configuration files eliminate the need for code recompilation. Edit `.properties` files to change simulation parameters:

```properties
# System Configuration
simulation.type=BLOCKCHAIN          # BLOCKCHAIN, DYNAMIC, STATIC, LANDMARK
system.capacity=64                  # Number of nodes
system.lifetime=24                  # Simulation duration (hours)
system.topologies=2                 # Number of topology instances

# Blockchain Configuration
txb.rate=1                          # Transactions per node per hour
blockchain.protocol=LIGHTCHAIN      # Protocol implementation

# Churn Configuration
churn.model=DEBIAN_FAST            # DEBIAN_FAST, DEBIAN_SLOW, FLATOUT
churn.type=ADVERSARIAL             # ADVERSARIAL, COOPERATIVE

# Security Configuration
malicious.fraction=0.16            # Fraction of malicious nodes (16%)
validator.threshold=12             # Validators to search for
signature.threshold=1              # Minimum honest validators required

# Other
logging.enabled=true               # Enable/disable logging
```

**Run with different configs:**

```bash
# Use default config
make run-load NAME=my_sim

# Use custom config
make run-load NAME=my_sim CONFIG=configs/my-experiment.properties
```

### Key Parameters

**System Parameters:**
- `system.capacity` - Number of nodes in the network (e.g., 64, 1024)
- `system.lifetime` - Simulation duration in hours (e.g., 24, 168)
- `system.topologies` - Number of independent topology instances for statistical significance

**Churn Parameters:**
- `churn.model` - Node failure model (DEBIAN_FAST for high churn, DEBIAN_SLOW for stable)
- `churn.type` - COOPERATIVE (nodes notify before leaving) or ADVERSARIAL (sudden departures)

**Security Parameters:**
- `malicious.fraction` - Proportion of malicious nodes (0.16 = 16%)
- `validator.threshold` - Number of validator search attempts in Proof-of-Validation
- `signature.threshold` - Minimum honest validators needed for valid transaction

### Configuration Examples

**Development Testing:**
```bash
# Create a minimal config for quick tests
cat > configs/dev-test.properties <<EOF
simulation.type=BLOCKCHAIN
system.capacity=16
system.lifetime=6
system.topologies=1
txb.rate=1
churn.model=DEBIAN_FAST
malicious.fraction=0.16
EOF

make run-load NAME=dev_test CONFIG=configs/dev-test.properties
```

**Research Experiment:**
```bash
# Use full experiment config
make run-load NAME=research_exp CONFIG=configs/full-experiment.properties
```

**Parameter Sweep:**
```bash
# Compare different malicious fractions
for fraction in 0.10 0.16 0.25; do
  sed "s/malicious.fraction=.*/malicious.fraction=$fraction/" \
    configs/quick-test.properties > configs/test-$fraction.properties
  make run-load NAME=mal_$fraction CONFIG=configs/test-$fraction.properties
done
```

## Core Concepts

### Skip Graph Architecture

SkipSim uses **Skip Graphs**, a distributed data structure similar to skip lists but with better fault tolerance. Each node maintains:

- **Numerical ID**: Position in the overlay (used for proximity-based search)
- **Name ID**: Membership vector for multi-path routing
- **Lookup Table**: Multi-level neighbor pointers (size = ceil(log₂(capacity) + nameIDLength))

**Search Complexity**: O(log n) expected hops for both numerical and name-based searches

### Dual Overlay System

For blockchain simulations, SkipSim maintains two Skip Graph overlays:

1. **Node Skip Graph** - Peer network overlay
   - Access via: `SkipGraphOperations.getTG().getNodeSet()`
   - Used for: Validator discovery, bootstrapping, peer search

2. **Transaction Skip Graph** - Transaction/block overlay
   - Access via: `SkipGraphOperations.getTransactions()`
   - Used for: Transaction lookup, verification, replication

### Search Operations

**Searching by Numerical ID** (peers):
```java
Nodes peers = sgo.getTG().getNodeSet();
Node initiator = (Node)peers.getNode(0);
int targetNumID = 42;

int searchDirection = (targetNumID > initiator.getNumID())
    ? SkipGraphOperations.RIGHT_SEARCH_DIRECTION
    : SkipGraphOperations.LEFT_SEARCH_DIRECTION;

int result = sgo.SearchByNumID(
    targetNumID,
    initiator,
    new Message(),
    SkipSimParameters.getLookupTableSize() - 1,  // Start from top level
    0,
    peers,
    searchDirection
);
```

**Searching by Numerical ID** (transactions):

When searching for transactions, first find the initiator's most similar transaction:

```java
Nodes transactions = sgo.getTransactions();
int mostSimilarInInitiator = initiator.mostSimilarTXB(
    transactions, targetNumID, new Message(),
    SkipGraphOperations.LEFT_SEARCH_DIRECTION, 0
);

if (mostSimilarInInitiator == -1) {
    mostSimilarInInitiator = initiator.mostSimilarTXB(
        transactions, targetNumID, new Message(),
        SkipGraphOperations.RIGHT_SEARCH_DIRECTION, 0
    );
}

int mostSimilarNumId = transactions.getNode(mostSimilarInInitiator).getNumID();
int searchDirection = (targetNumID < mostSimilarNumId)
    ? SkipGraphOperations.LEFT_SEARCH_DIRECTION
    : SkipGraphOperations.RIGHT_SEARCH_DIRECTION;

int result = sgo.SearchByNumID(
    targetNumID, initiator, new Message(),
    SkipSimParameters.getLookupTableSize() - 1,
    0, transactions, searchDirection
);
```

### Proof-of-Validation Consensus

When a transaction is created, the owner searches for validators:
1. Compute search targets: `Hash(numID || i)` for i = 1 to ValidatorThreshold
2. Search for nodes at those numerical IDs
3. If SignatureThreshold honest validators are found, transaction is valid
4. If attacker acquires SignatureThreshold malicious validators, it's a malicious success

**Key Configuration:**
- `validator.threshold=12` - Number of search attempts
- `signature.threshold=1` - Minimum honest validators needed

### Experiments

Enable experiments in configuration or `SkipSimParameters.java`:

- **EfficiencyExperiment** - Average honest validators acquired
- **MaliciousSuccessExperiment** - Probability of malicious success
- **AvailabilityExperiment** - Average online replicas over time
- **OnlineProbabilityExperiment** - Node online probability
- **btsEfficiencyExperiment** - Honest view introducers in randomized bootstrapping
- **btsMaliciousSuccessExperiment** - Malicious success in bootstrapping

## For Developers

### Project Structure

```
SkipSim/
├── src/main/java/
│   ├── Blockchain/LightChain/     # LightChain protocol implementation
│   │   ├── Transaction.java        # Transaction with Proof-of-Validation
│   │   ├── HashTools.java          # SHA3-256 hashing utilities
│   │   └── Experiments/            # Experiment implementations
│   ├── SkipGraph/                  # Core Skip Graph implementation
│   │   ├── SkipGraphOperations.java # Search, insertion, operations
│   │   ├── Node.java               # Peer node representation
│   │   └── TopologyGenerator.java  # Geographic topology generation
│   ├── Replication/                # 15+ replication algorithms
│   │   ├── Rep_Alg01.java          # LP replication
│   │   ├── Rep_Alg07.java          # GLARAS replication
│   │   └── ...
│   ├── ChurnStabilization/         # Churn handling (DKS, Kademlia, etc.)
│   ├── NameIDAssignment/           # Name ID strategies (LANS, DPAD, LDHT, etc.)
│   ├── AvailabilityPrediction/     # LUDP, BruijnGraph predictors
│   ├── Aggregation/                # Aggregation protocols
│   ├── Simulator/                  # Simulation engine
│   │   ├── Main.java               # CLI entry point and configuration loader
│   │   ├── SkipSimParameters.java  # Global parameters
│   │   └── DynamicSimulation.java  # Simulation loop
│   └── DataBase/                   # SQLite persistence layer
├── src/test/java/
│   ├── TestFixtures/               # Reusable test fixtures
│   │   └── SkipGraphTestFixture.java
│   └── SkipGraph/                  # Skip Graph tests
│       └── TransactionInsertionTest.java
├── configs/                        # Configuration files
│   ├── quick-test.properties
│   └── full-experiment.properties
├── Makefile                        # Build automation
├── pom.xml                         # Maven configuration
└── README.md                       # This file
```

### Adding New Features

**Creating a New Replication Algorithm:**

1. Create `Replication/Rep_AlgXX.java` extending `ReplicationAlgorithm`
2. Implement `replicate()` method
3. Register in `Constants.java`
4. Set in configuration: `replication.algorithm=REP_ALGXX`

**Creating a New Experiment:**

1. Create class in `Blockchain/LightChain/Experiments/`
2. Add boolean flag in `SkipSimParameters.java`
3. Initialize experiment in `DynamicSimulation.java`
4. Enable in configuration

**Adding Configuration Parameters:**

1. Add field and setter in `SkipSimParameters.java`:
```java
protected static int MyParameter = 10;
public static void setMyParameter(int value) { MyParameter = value; }
```

2. Add to `ConfigLoader.java`:
```java
SkipSimParameters.setMyParameter(
    getIntProperty(props, "my.parameter", 10)
);
```

3. Use in config files:
```properties
my.parameter=20
```

### Testing

SkipSim uses **JUnit 4** for testing. Tests are located in `src/test/java/`.

**Running Tests:**
```bash
# Using Maven
make test

# Verbose output
make test-verbose

# In IntelliJ
# Right-click on test class → Run
```

**Writing Tests:**

```java
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import TestFixtures.SkipGraphTestFixture;

public class MyFeatureTest {
    private SkipGraphTestFixture fixture;

    @Before
    public void setUp() {
        fixture = new SkipGraphTestFixture();
    }

    @Test
    public void testMyFeature() {
        // Create test network
        TestNetwork network = fixture.createTestNetwork(20, 3);

        // Perform operations
        Node node = network.getNode(5);

        // Assert results
        assertNotNull("Node should exist", node);
        assertEquals("Node index", 5, node.getIndex());
    }
}
```

**Test Fixtures:**

The `SkipGraphTestFixture` class provides:
- `createTestNetwork(nodes, txsPerNode)` - Quick network setup
- `createAndInsertNodes(numNodes)` - Safe node creation
- `createAndInsertTransaction(owner, index, time)` - Safe transaction insertion

Always use fixtures instead of manual construction to avoid common pitfalls.

### Important Implementation Notes

- **Transaction Insertion Order**: Transactions MUST be inserted into the Skip Graph BEFORE being added to the owner's `txSet`. This prevents NullPointerException during search operations.

- **Numerical ID Hashing**: Set `NumIDHashing = true` for Proof-of-Validation and randomized bootstrapping.

- **Lookup Table Size**: Automatically computed as `ceil(NameIDLength + log₂(SystemCapacity))`.

- **Time Slots**: Blockchain simulations operate in hourly time slots.

- **Geographic Coordinates**: Nodes have 2D coordinates in a `DomainSize × DomainSize` domain.

### API Reference

**Core Classes:**

- `SkipGraphOperations` - Main Skip Graph operations
  - `SearchByNumID()` - Numerical ID search
  - `SearchByNameID()` - Name ID search
  - `insert()` - Insert node into Skip Graph
  - `addTXBtoLedger()` - Add transaction to ledger

- `Node` - Peer node
  - `getNumID()` / `setNumID()` - Numerical ID
  - `getNameID()` / `setNameID()` - Name ID
  - `getTxSet()` - Transaction indices owned
  - `getLookup(level, direction)` - Get neighbor at level

- `Transaction` - Blockchain transaction
  - `acquireValidators()` - Run Proof-of-Validation
  - `getOwnerIndex()` - Get owner node index
  - `getValidators()` - Get validator list

- `SkipSimParameters` - Global configuration
  - `getSystemCapacity()` - Number of nodes
  - `getLookupTableSize()` - Skip Graph levels
  - `getMaliciousFraction()` - Malicious node fraction

## Build Commands Reference

```bash
# Build and Install
make install          # Download Maven dependencies
make compile          # Compile the project
make clean            # Remove build artifacts
make help             # Show available commands

# Running Simulations
make run-list                                      # List simulations
make run-load NAME=sim_name                        # Load simulation
make run-new NAME=sim_name                         # Create simulation
make run-delete NAME=sim_name                      # Delete simulation
make run-load NAME=sim CONFIG=path/to/config.properties  # With config

# Testing
make test             # Run all tests
make test-verbose     # Run tests with verbose output

# Using Maven directly
mvn dependency:resolve              # Download dependencies
mvn compile                         # Compile
mvn test                           # Run tests
mvn exec:java -Dexec.args="list"   # Run simulation
```

## Publications

Hassanzadeh-Nazarabadi, Yahya, Ali Utkan Şahin, Öznur Özkasap, and Alptekin Küpçü. "SkipSim: Scalable Skip Graph Simulator." In 2020 IEEE International Conference on Blockchain and Cryptocurrency (ICBC), pp. 1-2. IEEE, 2020. [(PDF)](https://arxiv.org/pdf/2007.13200.pdf)

## Citation

```bibtex
@inproceedings{hassanzadeh2020skipsim,
  title={SkipSim: Scalable Skip Graph Simulator},
  author={Hassanzadeh-Nazarabadi, Yahya and {\c{S}}ahin, Ali Utkan and {\"O}zkasap, {\"O}znur and K{\"u}p{\c{c}}{\"u}, Alptekin},
  booktitle={2020 IEEE International Conference on Blockchain and Cryptocurrency (ICBC)},
  pages={1--2},
  year={2020},
  organization={IEEE}
}
```

## Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Submit a pull request

