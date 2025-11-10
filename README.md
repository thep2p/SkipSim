
# SkipSim

SkipSim is a simulator that can be used for the design, implementation, and evaluation of the distributed Skip Graph-based protocols.

## Quick Start

### Build and Test

```bash
# Clean and compile everything
make clean && make compile

# Run tests to verify everything works
make test

# Get help on available commands
make help
```

### Run Simulations

**Using Makefile (recommended):**
```bash
# List available simulations
make run-list

# Create or load a simulation (recommended - creates if doesn't exist)
make run-load NAME=my_simulation

# Explicitly create a new simulation
make run-new NAME=my_simulation

# Delete a simulation
make run-delete NAME=old_simulation
```

**Direct command-line usage:**
```bash
java -cp "libs/*:out/production" Simulator.Main <command> [arguments]

Commands:
  load <simulation-name>    Load existing or create new simulation (recommended)
  new <simulation-name>     Explicitly create a new simulation
  list                      List available simulations
  delete <simulation-name>  Delete a simulation

Examples:
  java -cp "libs/*:out/production" Simulator.Main list
  java -cp "libs/*:out/production" Simulator.Main load my_blockchain_sim
  java -cp "libs/*:out/production" Simulator.Main new my_explicit_sim
```

> **Note**: The `load` command will automatically create the simulation if it doesn't exist in the database, making it the most convenient option for both creating and running simulations.

### Configure Simulation Parameters

SkipSim supports **two configuration methods**:

#### Method 1: Configuration Files (Recommended - No Recompilation!)

Edit `simulation-config.properties` or create custom config files:

```properties
# Quick Test Configuration
simulation.type=BLOCKCHAIN
system.capacity=64
system.lifetime=24
system.topologies=2
txb.rate=1
churn.model=DEBIAN_FAST
churn.type=ADVERSARIAL
malicious.fraction=0.16
validator.threshold=12
signature.threshold=1
```

**Run with different configs:**
```bash
# Use default config (simulation-config.properties)
make run-load NAME=my_sim

# Use custom config
make run-load NAME=my_sim CONFIG=configs/full-experiment.properties
make run-load NAME=quick_test CONFIG=configs/quick-test.properties
```

**Available config files:**
- `simulation-config.properties` - Default configuration
- `configs/quick-test.properties` - Small test (64 nodes, 2 topologies, 24 hours)
- `configs/full-experiment.properties` - Full research (1024 nodes, 100 topologies, 168 hours)

#### Method 2: Schema Classes (Legacy)

Alternatively, use Java classes in `src/main/java/SimulationSchema/`:
- `Blockchain.java` - Full blockchain simulation (100 topologies, 1024 nodes, 168 hours)
- `BlockchainSmall.java` - Quick test simulation (2 topologies, 64 nodes, 24 hours)
- `StaticReplication.java` - For static replication experiments
- `MultiObjectiveReplication.java` - For multi-objective optimization

Edit `SimulationSchema/SchemaManager.java` and recompile (not recommended).

#### Configurable Parameters

All parameters in config files or schema classes:
- `simulation.type` - BLOCKCHAIN, DYNAMIC, STATIC, LANDMARK
- `system.capacity` - Number of nodes (e.g., 64, 1024)
- `system.lifetime` - Simulation duration in hours (e.g., 24, 168)
- `system.topologies` - Number of topology instances (e.g., 2, 100)
- `malicious.fraction` - Proportion of malicious nodes (e.g., 0.16 = 16%)
- `validator.threshold` - Validators in Proof-of-Validation (e.g., 12)
- `signature.threshold` - Minimum honest validators required (e.g., 1)
- `churn.model` - DEBIAN_FAST, DEBIAN_SLOW, FLATOUT
- `churn.type` - ADVERSARIAL, COOPERATIVE
- `txb.rate` - Transactions per node per hour (e.g., 1)
- `blockchain.protocol` - LIGHTCHAIN (default)
- `logging.enabled` - true/false

#### Quick Start Examples

```bash
# 1. Quick test (finishes in minutes)
make run-load NAME=quick_test CONFIG=configs/quick-test.properties

# 2. Full experiment (takes hours)
make run-load NAME=full_exp CONFIG=configs/full-experiment.properties

# 3. Custom parameters - just edit simulation-config.properties!
nano simulation-config.properties
make run-load NAME=my_custom_sim
```

**No recompilation needed when using config files!**

Please see the [first tutorial](https://github.com/yhassanzadeh13/SkipSim/blob/master/Tutorial1.md) for detailed instructions on running SkipSim experiments.

## Skip graph
Skip-graph(s)
are stored in a `SkipGraphOperations` object and all the operations regarding to skip-graph(s)
can be performed using an instance of that class.

## Usage of SkipGraphOperations
First, create a SkipGraphOperations object
```
SkipGraphOperations sgo = new SkipGraphOperations(true);
```
We've passed `true` to the constructor because we want a blockchain type structure 
where two skip-graph overlays are generated (one for peers, one for transactions.)
We can reach the peer skip-graph by doing `sgo.getTG().getNodeSet()` and transactions
skip-graph by doing `sgo.getTransactions()`.
### Searching by Numerical ID for peers
An initiator peer required to perform a search over the skip-graph. Here, we use the node
with the index of 0 as the initiator.
```java
Nodes peers = sgo.getTG().getNodeSet();
Node initiator = (Node)peers.getNode(0);
// We want the node with the num.id most similar to 42.
int targetNumID = 42;
int searchDirection;
// Determining the direction of search.
if(targetNumID > initiator.getNumID) {
    // Target is on the right of the initiator.
    searchDirection = SkipGraphOperations.RIGHT_SEARCH_DIRECTION
} else {
    // Target is on the left of the initiator.
    searchDirection = SkipGraphOperations.LEFT_SEARCH_DIRECTION
}
int result = sgo.SearchByNumID(
                targetNumID, // search for the node with num. id of 42
                initiator, // initiate the search from this node
                new Message(),
                SkipSimParameters.getLookupTableSize()-1, // initiate the search from the uppermost level
                0,
                peers, // search over the peers skip-graph
                searchDirection // the direction of the search
);
```
The result will store the node's index with the numerical id most similar to 42.
### Searching by Numerical ID for transactions
An initiator peer is required to perform a search over the skip-graph as well. Note that this initiator should own
at least one transaction. We use the node 0 as previously.  

We also need the search direction as we did previously. However, we cannot simply use the numerical id of the initiator, as we are looking for a transaction while the initiator 
is a peer. We need to search for the initiator's transaction that is **most similar to our target**. That transaction 
will be the starting point of our search. So, we use that transaction to determine the search direction as well.
```java
Nodes transactions = sgo.getTransactions();
Node initiator = (Node)peers.getNode(0);
// we want the *transaction* with the num.id most similar to 42.
int targetNumID = 42;
/*
Determining the direction of search.
 */
// First, we need to find the most similar transaction that is stored in the initiator.
int mostSimilarInInitiator = initiator
        .mostSimilarTXB(transactions, targetNumID,
                new Message(), SkipGraphOperations.LEFT_SEARCH_DIRECTION, 0);
if(mostSimilarInInitiator == -1) {
    mostSimilarInInitiator = nodeSearchInitiator
            .mostSimilarTXB(transactions, targetNumID,
                    new Message(), SkipGraphOperations.RIGHT_SEARCH_DIRECTION, 0);

}
// Now, we have the most similar transaction on the initiator node.
// The search will be performed starting from that transaction, so we
// needed to find this value explicitly in order to determine the search 
// direction.
int mostSimilarNumId = transactions.getNode(mostSimilarInInitiator).getNumID();
int searchDirection = (searchTargetTXBNumId < mostSimilarNumId)
        ? SkipGraphOperations.LEFT_SEARCH_DIRECTION
        : SkipGraphOperations.RIGHT_SEARCH_DIRECTION;

int result = sgo.SearchByNumID(
        targetNumID,
        initiator,
        new Message(),
        SkipSimParameters.getLookupTableSize() - 1,
        0,
        transactions,
        searchDirection);
```
Similarly, the result will store the transaction's index with the numerical id most similar to 42.


### Insertion into the peer skip-graph
Insertion into the peer skip-graph is relatively easy.
```java
// Determine the new index.
int newIndex = sgo.getTG.getNodeSet().size();
// Construct the new node.
Node newNode = new Node(newIndex);
// Insert the new node into the peer skip-graph
sgo.insert(newNode, 0, sgo.getTG().getNodeSet());
```
### Insertion into the transaction skip-graph
Insertion into the transaction skip-graph is easy as well. We only need an owner peer.
```java
// Choose the node with the index of 0 as the owner of the new transaction.
Node owner = (Node) sgo.getTG().getNodeSet().getNode(0);
// Construct the new transaction.
Transaction tx = new Transaction(0, owner.getIndex());
// Insert the transaction into the skip-graph.
sgo.addTXBtoLedger(tx, 0, true);
```


## Simulation schemas
In order to create a new simulation-schema, first we need to create a new class 
that extends from SkipSimParameters. In its constructor, we set the parameters. 
Then we need to call the constructor of this class from the constructor of the SchemaManager. 
SkipSim calls the SchemaManager's constructor once to initialize the simulation with correct configuration.

Please see Blockchain, StaticReplication and MultiObjectiveReplication classes for
reference simulation-schemas. If the user wishes, for example, to use the already provided
StaticReplication schema, they should call the constructor of that class from the constructor
of the SchemaManager, as following:

```java
package SimulationSchema;

public class SchemaManager {
    public SchemaManager() {
        // calling the constructor of the schema that we want to use
        new StaticReplication(); 
    }
}
```


## Tutorials
Please refer to the tutorials for getting familiar with SkipSim quickly:
1. [Performing an efficiency experiment](https://github.com/yhassanzadeh13/SkipSim/blob/master/Tutorial1.md)
2. [Performing randomized look-up tests](https://github.com/yhassanzadeh13/SkipSim/blob/master/Tutorial2.md)
3. [Using the skip-graph](https://github.com/yhassanzadeh13/SkipSim/blob/master/Tutorial3.md)

## Publications
- Hassanzadeh-Nazarabadi, Yahya, Ali Utkan Şahin, Öznur Özkasap, and Alptekin Küpçü. "SkipSim: Scalable Skip Graph Simulator." In 2020 IEEE International Conference on Blockchain and Cryptocurrency (ICBC), pp. 1-2. IEEE, 2020. [(PDF)](https://arxiv.org/pdf/2007.13200.pdf).

## Citation
For citing this implementation in a publication please use:
_Hassanzadeh-Nazarabadi, Yahya, Ali Utkan Şahin, Öznur Özkasap, and Alptekin Küpçü. "SkipSim: Scalable Skip Graph Simulator." In 2020 IEEE International Conference on Blockchain and Cryptocurrency (ICBC), pp. 1-2. IEEE, 2020._
```
@inproceedings{hassanzadeh2020skipsim,
  title={SkipSim: Scalable Skip Graph Simulator},
  author={Hassanzadeh-Nazarabadi, Yahya and {\c{S}}ahin, Ali Utkan and {\"O}zkasap, {\"O}znur and K{\"u}p{\c{c}}{\"u}, Alptekin},
  booktitle={2020 IEEE International Conference on Blockchain and Cryptocurrency (ICBC)},
  pages={1--2},
  year={2020},
  organization={IEEE}
}
```
