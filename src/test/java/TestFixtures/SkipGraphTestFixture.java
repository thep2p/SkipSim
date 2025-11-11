package TestFixtures;

import Blockchain.LightChain.Transaction;
import DataTypes.Constants;
import Simulator.SkipSimParameters;
import SkipGraph.Node;
import SkipGraph.Nodes;
import SkipGraph.SkipGraphOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Test fixture for creating Skip Graph test data.
 * This class provides reusable methods for generating nodes, transactions,
 * and Skip Graph structures for testing purposes.
 */
public class SkipGraphTestFixture {

    private SkipGraphOperations skipGraphOps;
    private Random random;

    /**
     * Creates a new SkipGraphTestFixture with default parameters.
     */
    public SkipGraphTestFixture() {
        this.random = new Random(42); // Fixed seed for reproducibility
        initializeParameters();
    }

    /**
     * Creates a new SkipGraphTestFixture with a custom random seed.
     * @param seed Random seed for reproducibility
     */
    public SkipGraphTestFixture(long seed) {
        this.random = new Random(seed);
        initializeParameters();
    }

    /**
     * Initializes SkipSim parameters for testing.
     * Sets up a minimal blockchain simulation configuration.
     */
    private void initializeParameters() {
        // Set minimal test parameters
        SkipSimParameters.setSimulationType(Constants.SimulationType.BLOCKCHAIN);
        SkipSimParameters.setValidatorThreshold(12);
        SkipSimParameters.setSignatureThreshold(1);
    }

    /**
     * Creates a new SkipGraphOperations instance for blockchain mode.
     * Also initializes transaction 0, which is required for the transaction Skip Graph.
     * @return SkipGraphOperations configured for blockchain
     */
    public SkipGraphOperations createSkipGraphOperations() {
        skipGraphOps = new SkipGraphOperations(true);

        // Initialize transaction 0 (required as the root transaction in the Skip Graph)
        // Transaction 0 doesn't need to be inserted - it's the starting point
        // We use addToSet to properly register it and increment the index counter
        Transaction tx0 = new Transaction(0, 0);
        skipGraphOps.getTransactions().addToSet(tx0);

        return skipGraphOps;
    }

    /**
     * Creates a specified number of nodes and inserts them into the Skip Graph.
     * @param numNodes Number of nodes to create
     * @return List of created Node objects
     */
    public List<Node> createAndInsertNodes(int numNodes) {
        if (skipGraphOps == null) {
            createSkipGraphOperations();
        }

        List<Node> nodes = new ArrayList<>();
        Nodes nodeSet = skipGraphOps.getTG().getNodeSet();

        for (int i = 0; i < numNodes; i++) {
            Node node = new Node(i);
            // Set online status for dynamic simulations
            node.setOnline();

            if (i > 0) {
                // Insert nodes starting from index 1 (node 0 doesn't need insertion)
                skipGraphOps.insert(node, nodeSet, i, true, 0);
            } else {
                // Node 0 is special - just set it in the node set
                node.setIndex(0);
                nodeSet.setNode(0, node);
                // Add transaction 0 to node 0's txSet
                node.addToTXSet(0);
            }
            nodes.add(node);
        }

        return nodes;
    }

    /**
     * Creates a transaction owned by the specified node.
     * The transaction is NOT inserted into the Skip Graph yet.
     * @param ownerNode The node that owns the transaction
     * @param txIndex The index for the transaction
     * @return Created Transaction object
     */
    public Transaction createTransaction(Node ownerNode, int txIndex) {
        return new Transaction(txIndex, ownerNode.getIndex());
    }

    /**
     * Creates and inserts a transaction into the Skip Graph.
     * This is the safe way to add a transaction - it properly inserts it
     * into the Skip Graph before making it accessible.
     *
     * @param ownerNode The node that owns the transaction
     * @param txIndex The index for the transaction
     * @param currentTime Current simulation time
     * @return Created and inserted Transaction object
     */
    public Transaction createAndInsertTransaction(Node ownerNode, int txIndex, int currentTime) {
        if (skipGraphOps == null) {
            throw new IllegalStateException("SkipGraphOperations not initialized. Call createSkipGraphOperations() first.");
        }

        Transaction tx = createTransaction(ownerNode, txIndex);
        skipGraphOps.addTXBtoLedger(tx, currentTime, true);
        return tx;
    }

    /**
     * Creates multiple transactions for a given owner node.
     * @param ownerNode The node that owns the transactions
     * @param numTransactions Number of transactions to create
     * @param startingIndex Starting index for transactions
     * @param currentTime Current simulation time
     * @return List of created and inserted Transaction objects
     */
    public List<Transaction> createAndInsertMultipleTransactions(Node ownerNode, int numTransactions,
                                                                  int startingIndex, int currentTime) {
        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < numTransactions; i++) {
            Transaction tx = createAndInsertTransaction(ownerNode, startingIndex + i, currentTime);
            transactions.add(tx);
        }
        return transactions;
    }

    /**
     * Creates a small test network with nodes and transactions.
     * Useful for quick tests.
     *
     * @param numNodes Number of nodes to create (recommended: 10-50)
     * @param transactionsPerNode Number of transactions per node
     * @return TestNetwork object containing all created entities
     */
    public TestNetwork createTestNetwork(int numNodes, int transactionsPerNode) {
        SkipGraphOperations ops = createSkipGraphOperations();
        List<Node> nodes = createAndInsertNodes(numNodes);

        List<Transaction> allTransactions = new ArrayList<>();
        int txIndex = 0;

        for (Node node : nodes) {
            List<Transaction> nodeTxs = createAndInsertMultipleTransactions(
                node, transactionsPerNode, txIndex, 0
            );
            allTransactions.addAll(nodeTxs);
            txIndex += transactionsPerNode;
        }

        return new TestNetwork(ops, nodes, allTransactions);
    }

    /**
     * Gets the SkipGraphOperations instance.
     * @return Current SkipGraphOperations instance
     */
    public SkipGraphOperations getSkipGraphOperations() {
        return skipGraphOps;
    }

    /**
     * Gets the random number generator for this fixture.
     * @return Random instance
     */
    public Random getRandom() {
        return random;
    }

    /**
     * Container class for a complete test network.
     */
    public static class TestNetwork {
        public final SkipGraphOperations operations;
        public final List<Node> nodes;
        public final List<Transaction> transactions;

        public TestNetwork(SkipGraphOperations operations, List<Node> nodes, List<Transaction> transactions) {
            this.operations = operations;
            this.nodes = nodes;
            this.transactions = transactions;
        }

        /**
         * Gets a node by index.
         * @param index Node index
         * @return Node at the specified index
         */
        public Node getNode(int index) {
            return nodes.get(index);
        }

        /**
         * Gets a transaction by index.
         * @param index Transaction index
         * @return Transaction at the specified index
         */
        public Transaction getTransaction(int index) {
            return transactions.get(index);
        }

        /**
         * Gets the number of nodes in the network.
         * @return Number of nodes
         */
        public int getNodeCount() {
            return nodes.size();
        }

        /**
         * Gets the number of transactions in the network.
         * @return Number of transactions
         */
        public int getTransactionCount() {
            return transactions.size();
        }
    }
}
