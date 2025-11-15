package SkipGraph;

import Blockchain.LightChain.Transaction;
import TestFixtures.SkipGraphTestFixture;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for transaction insertion into the Skip Graph.
 * These tests verify that transactions are properly inserted without causing
 * NullPointerExceptions or lookup table violations.
 */
public class TransactionInsertionTest {

    private SkipGraphTestFixture fixture;

    @Before
    public void setUp() {
        fixture = new SkipGraphTestFixture();
    }

    /**
     * Tests that a single transaction can be inserted without errors.
     * This is the basic case that should always work.
     */
    @Test
    public void testSingleTransactionInsertion() {
        // Create a small network with nodes
        List<Node> nodes = fixture.createAndInsertNodes(10);

        // Create and insert a transaction for node 5
        Node ownerNode = nodes.get(5);
        Transaction tx = fixture.createAndInsertTransaction(ownerNode, 1, 0);

        // Verify transaction was created
        assertNotNull("Transaction should not be null", tx);

        // Verify transaction has the correct owner
        assertEquals("Transaction owner should be node 5", 5, tx.getOwnerIndex());

        // Verify owner's txSet contains the transaction
        assertTrue("Owner's txSet should contain the transaction",
            ownerNode.getTxSet().contains(tx.getIndex()));

        // Verify transaction was inserted into Skip Graph (lookup table not empty)
        assertFalse("Transaction lookup table should not be empty after insertion",
            tx.isLookupTableEmpty(Transaction.LOOKUP_TABLE_SIZE));
    }

    /**
     * Tests the bug fix: inserting multiple transactions in sequence.
     * This was causing NullPointerException before the fix because transactions
     * were added to owner's txSet before being inserted into Skip Graph.
     */
    @Test
    public void testMultipleSequentialTransactionInsertions() {
        // Create a small network with nodes
        List<Node> nodes = fixture.createAndInsertNodes(10);

        // Insert multiple transactions for the same node
        Node ownerNode = nodes.get(3);
        int numTransactions = 5;

        for (int i = 1; i <= numTransactions; i++) {
            Transaction tx = fixture.createAndInsertTransaction(ownerNode, i, 0);

            assertNotNull("Transaction " + i + " should not be null", tx);
            assertTrue("Owner's txSet should contain transaction " + i,
                ownerNode.getTxSet().contains(tx.getIndex()));
            assertFalse("Transaction " + i + " lookup table should not be empty",
                tx.isLookupTableEmpty(Transaction.LOOKUP_TABLE_SIZE));
        }

        // Verify all transactions are in owner's txSet
        assertEquals("Owner should have " + numTransactions + " transactions",
            numTransactions, ownerNode.getTxSet().size());
    }

    /**
     * Tests that transactions can be inserted when the owner node already has transactions.
     * This specifically tests the scenario where mostSimilarTXB() needs to find a starting
     * point for the search, and verifies it doesn't encounter uninitialized transactions.
     */
    @Test
    public void testTransactionInsertionWithExistingTransactions() {
        // Create network
        List<Node> nodes = fixture.createAndInsertNodes(20);

        // Node 5 inserts first transaction
        Node node5 = nodes.get(5);
        Transaction tx1 = fixture.createAndInsertTransaction(node5, 1, 0);
        assertNotNull("First transaction should not be null", tx1);

        // Node 10 inserts a transaction
        Node node10 = nodes.get(10);
        Transaction tx2 = fixture.createAndInsertTransaction(node10, 2, 0);
        assertNotNull("Second transaction should not be null", tx2);

        // Node 5 inserts another transaction (this tests the mostSimilarTXB path)
        Transaction tx3 = fixture.createAndInsertTransaction(node5, 3, 0);
        assertNotNull("Third transaction should not be null", tx3);

        // Verify node 5 has both transactions
        assertEquals("Node 5 should have 2 transactions", 2, node5.getTxSet().size());
        assertTrue("Node 5 txSet should contain tx1",
            node5.getTxSet().contains(tx1.getIndex()));
        assertTrue("Node 5 txSet should contain tx3",
            node5.getTxSet().contains(tx3.getIndex()));

        // Verify node 10 has one transaction
        assertEquals("Node 10 should have 1 transaction", 1, node10.getTxSet().size());
    }

    /**
     * Tests concurrent-like transaction insertions from multiple nodes.
     * This simulates the scenario from the bug report where multiple nodes
     * generate transactions at the same time slot.
     */
    @Test
    public void testMultipleNodesInsertingTransactions() {
        // Create a moderate-sized network
        List<Node> nodes = fixture.createAndInsertNodes(30);

        int txIndex = 1;
        // Have every 3rd node insert a transaction
        for (int i = 0; i < nodes.size(); i += 3) {
            Node node = nodes.get(i);
            Transaction tx = fixture.createAndInsertTransaction(node, txIndex++, 0);

            assertNotNull("Transaction for node " + i + " should not be null", tx);
            assertTrue("Node " + i + " should contain its transaction",
                node.getTxSet().contains(tx.getIndex()));
        }

        // Now have all those nodes insert a second transaction
        txIndex = 100; // Start at different index range
        for (int i = 0; i < nodes.size(); i += 3) {
            Node node = nodes.get(i);
            Transaction tx = fixture.createAndInsertTransaction(node, txIndex++, 0);

            assertNotNull("Second transaction for node " + i + " should not be null", tx);
            // Node 0 has an extra transaction (transaction 0) used as the root of the transaction Skip Graph
            int expectedTxCount = (i == 0) ? 3 : 2;
            assertEquals("Node " + i + " should have " + expectedTxCount + " transactions",
                expectedTxCount, node.getTxSet().size());
        }
    }

    /**
     * Tests that the Skip Graph remains valid after many transaction insertions.
     * Verifies lookup table consistency.
     */
    @Test
    public void testSkipGraphConsistencyAfterManyInsertions() {
        // Create a test network with multiple transactions per node
        SkipGraphTestFixture.TestNetwork network = fixture.createTestNetwork(15, 3);

        // Verify all transactions have non-empty lookup tables
        for (Transaction tx : network.transactions) {
            if (tx.getIndex() > 0) { // Skip transaction 0 as it's special
                assertFalse("Transaction " + tx.getIndex() + " should have non-empty lookup table",
                    tx.isLookupTableEmpty(Transaction.LOOKUP_TABLE_SIZE));
            }
        }

        // Verify all nodes have their transactions in their txSet
        for (Node node : network.nodes) {
            for (int txIndex : node.getTxSet()) {
                Transaction tx = (Transaction) network.operations.getTransactions().getNode(txIndex);
                assertNotNull("Transaction " + txIndex + " should exist", tx);
                assertEquals("Transaction " + txIndex + " should be owned by node " + node.getIndex(),
                    node.getIndex(), tx.getOwnerIndex());
            }
        }
    }

    /**
     * Regression test for the specific bug: transaction insertion causing NullPointerException.
     * This test mimics the exact scenario from the bug report.
     */
    @Test
    public void testRegressionNoNullPointerDuringInsertion() {
        // Create network similar to bug scenario
        List<Node> nodes = fixture.createAndInsertNodes(50);

        // Node 321 would be index 21 in a smaller network
        // Let's use node 21 to mimic the scenario
        Node problematicNode = nodes.get(21);

        try {
            // Insert first transaction - this should work
            Transaction tx1 = fixture.createAndInsertTransaction(problematicNode, 1, 0);
            assertNotNull("First transaction should succeed", tx1);

            // Insert second transaction - this was causing NPE before fix
            Transaction tx2 = fixture.createAndInsertTransaction(problematicNode, 2, 0);
            assertNotNull("Second transaction should succeed (regression test)", tx2);

            // If we got here without exception, the bug is fixed
            assertTrue("Regression test passed - no NullPointerException during insertion", true);

        } catch (NullPointerException e) {
            fail("NullPointerException during transaction insertion (BUG NOT FIXED): " + e.getMessage());
        }
    }
}
