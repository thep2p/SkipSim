# SkipSim Test Fixtures

This directory contains reusable test fixtures and utilities for testing SkipSim components.

## Overview

The test infrastructure provides:
- **SkipGraphTestFixture**: Factory for creating Skip Graph test data
- **TestRunner**: Simple test runner without external dependencies

## Quick Start

```java
// Create a test class
public class MyFeatureTest {
    public void testSomething() {
        // Setup
        SkipGraphTestFixture fixture = new SkipGraphTestFixture();
        TestNetwork network = fixture.createTestNetwork(10, 2);

        // Test
        Node node = network.getNode(5);
        // ... perform operations ...

        // Assert
        TestRunner.Assert.assertNotNull("Node should exist", node);
    }

    public static void main(String[] args) {
        TestRunner runner = new TestRunner();
        runner.runTests(MyFeatureTest.class);
    }
}
```

## SkipGraphTestFixture

### Creating Networks

```java
SkipGraphTestFixture fixture = new SkipGraphTestFixture();

// Quick network creation
TestNetwork network = fixture.createTestNetwork(20, 3);
// Creates: 20 nodes, each with 3 transactions

// Access network components
SkipGraphOperations ops = network.operations;
List<Node> nodes = network.nodes;
List<Transaction> transactions = network.transactions;
```

### Manual Component Creation

```java
// Create Skip Graph Operations
SkipGraphOperations ops = fixture.createSkipGraphOperations();

// Create nodes
List<Node> nodes = fixture.createAndInsertNodes(10);
Node specificNode = nodes.get(5);

// Create transaction for a node
Transaction tx = fixture.createAndInsertTransaction(specificNode, 1, 0);

// Create multiple transactions
List<Transaction> txs = fixture.createAndInsertMultipleTransactions(
    specificNode, 5, 1, 0  // 5 transactions starting at index 1
);
```

### Reproducibility

```java
// Use fixed seed for reproducible tests
SkipGraphTestFixture fixture = new SkipGraphTestFixture(12345L);
```

## TestRunner

### Assertions

```java
// Boolean assertions
Assert.assertTrue("Should be true", someCondition);
Assert.assertFalse("Should be false", someCondition);

// Equality assertions
Assert.assertEquals("Values should match", expected, actual);

// Null checks
Assert.assertNotNull("Should not be null", someObject);
Assert.assertNull("Should be null", someObject);

// Explicit failure
Assert.fail("This should never happen");
```

### Running Tests

Tests are discovered by method name (must start with "test"):

```java
public void testFeatureA() { ... }
public void testFeatureB() { ... }

public static void main(String[] args) {
    TestRunner runner = new TestRunner();
    runner.runTests(MyTestClass.class);

    if (!runner.allTestsPassed()) {
        System.exit(1);  // Return non-zero for CI/CD
    }
}
```

## Best Practices

### 1. Use Fixtures for Setup

**Good:**
```java
SkipGraphTestFixture fixture = new SkipGraphTestFixture();
Transaction tx = fixture.createAndInsertTransaction(node, 1, 0);
```

**Bad:**
```java
Transaction tx = new Transaction(1, nodeIndex);
// Manual insertion is error-prone
```

### 2. Test One Thing

```java
public void testTransactionInsertion() {
    // Focus on transaction insertion only
    Transaction tx = fixture.createAndInsertTransaction(node, 1, 0);
    Assert.assertNotNull("Transaction created", tx);
    Assert.assertFalse("Lookup table populated",
        tx.isLookupTableEmpty(Transaction.LOOKUP_TABLE_SIZE));
}
```

### 3. Descriptive Assertions

```java
// Good: Clear message explains what failed
Assert.assertTrue("Node 5 should own transaction 10",
    node5.getTxSet().contains(10));

// Bad: No context when it fails
Assert.assertTrue("Test failed", node5.getTxSet().contains(10));
```

### 4. Clean Test Names

```java
public void testMultipleNodesCanInsertTransactionsConcurrently() { }
public void testTransactionInsertionWithEmptySkipGraph() { }
public void testRegressionIssue123NullPointerDuringInsertion() { }
```

## Common Test Patterns

### Testing Node Operations

```java
public void testNodeInsertion() {
    SkipGraphTestFixture fixture = new SkipGraphTestFixture();
    List<Node> nodes = fixture.createAndInsertNodes(10);

    for (Node node : nodes) {
        if (node.getIndex() > 0) {
            Assert.assertFalse("Node lookup table should be populated",
                node.isLookupTableEmpty(SkipSimParameters.getLookupTableSize()));
        }
    }
}
```

### Testing Transaction Operations

```java
public void testTransactionOwnership() {
    SkipGraphTestFixture fixture = new SkipGraphTestFixture();
    TestNetwork network = fixture.createTestNetwork(5, 2);

    for (Node node : network.nodes) {
        for (int txIndex : node.getTxSet()) {
            Transaction tx = (Transaction) network.operations
                .getTransactions().getNode(txIndex);
            Assert.assertEquals("Transaction owner matches",
                node.getIndex(), tx.getOwnerIndex());
        }
    }
}
```

### Testing Skip Graph Consistency

```java
public void testSkipGraphNeighborConsistency() {
    SkipGraphTestFixture fixture = new SkipGraphTestFixture();
    List<Node> nodes = fixture.createAndInsertNodes(20);

    for (Node node : nodes) {
        int rightNeighbor = node.getLookup(0, 1);
        if (rightNeighbor != -1) {
            Node right = nodes.get(rightNeighbor);
            int leftOfRight = right.getLookup(0, 0);
            Assert.assertEquals("Right neighbor's left should be current",
                node.getIndex(), leftOfRight);
        }
    }
}
```

## Troubleshooting

### NullPointerException during test

- Ensure you're using `createAndInsertTransaction()` not manual construction
- Check that nodes are inserted before transactions
- Verify SkipGraphOperations is initialized

### Test hangs or takes too long

- Reduce network size in test
- Check for infinite loops in Skip Graph operations
- Verify introducers are being found (not all offline)

### Inconsistent test results

- Use fixed seed: `new SkipGraphTestFixture(12345L)`
- Avoid shared state between tests
- Reset SkipSimParameters if modified

## Adding New Fixtures

To add a new fixture class:

1. Create class in `TestFixtures` package
2. Follow naming: `*TestFixture.java`
3. Provide reusable setup methods
4. Document usage in this README

Example:
```java
public class ReplicationTestFixture {
    public TestNetwork createReplicatedNetwork(int nodes, int replicas) {
        // Setup replication scenario
    }
}
```
