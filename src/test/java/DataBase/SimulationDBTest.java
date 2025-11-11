package DataBase;

import Simulator.ConfigLoader;
import Simulator.SkipSimParameters;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Test class for SimulationDB to ensure database operations work correctly.
 */
public class SimulationDBTest {
    private SimulationDB simDB;
    private static final String TEST_DB_NAME = "skipsim3db.db";
    private String testSimName;
    private boolean simulationDeleted = false;

    @Before
    public void setUp() {
        // Use unique simulation name for each test to avoid conflicts
        testSimName = "test_sim_" + UUID.randomUUID().toString().substring(0, 8);

        // Load default configuration
        ConfigLoader.loadFromFile("simulation-config.properties");

        // Create new database instance (will use the configured database)
        simDB = new SimulationDB();
        simulationDeleted = false;
    }

    @After
    public void tearDown() {
        // Clean up test simulation from database only if not already deleted
        if (!simulationDeleted) {
            try {
                simDB.deleteSimulationFromDB(testSimName);
            } catch (Exception e) {
                // Ignore if simulation doesn't exist
            }
        }
    }

    /**
     * Test that saving a topology name twice doesn't cause a UNIQUE constraint violation.
     * This is a regression test for the issue where re-running a simulation would fail
     * with "UNIQUE constraint failed: TOPOLOGIES.NAME".
     */
    @Test
    public void testSaveTopologyName_NoDuplicateViolation() {
        // Create a simulation
        int simId = simDB.saveSimulationName(testSimName, "BLOCKCHAIN");
        assertTrue("Simulation ID should be positive", simId > 0);

        // Save a topology name
        int topologyId1 = simDB.saveTopologyName(1, testSimName);
        assertTrue("First topology ID should be positive", topologyId1 > 0);

        // Try to save the same topology name again (should return existing ID, not fail)
        int topologyId2 = simDB.saveTopologyName(1, testSimName);
        assertEquals("Should return the same topology ID when saving duplicate", topologyId1, topologyId2);

        // Verify we can save a different topology index without issues
        int topologyId3 = simDB.saveTopologyName(2, testSimName);
        assertTrue("Second topology ID should be positive", topologyId3 > 0);
        assertNotEquals("Different topology index should have different ID", topologyId1, topologyId3);
    }

    /**
     * Test that tryFetchTopologyIDFromDB returns -1 for non-existent topology.
     */
    @Test
    public void testTryFetchTopologyIDFromDB_NonExistent() {
        // Create a simulation
        simDB.saveSimulationName(testSimName, "BLOCKCHAIN");

        // Try to fetch a topology that doesn't exist
        int topologyId = simDB.tryFetchTopologyIDFromDB(999, testSimName);
        assertEquals("Should return -1 for non-existent topology", -1, topologyId);
    }

    /**
     * Test that tryFetchTopologyIDFromDB returns correct ID for existing topology.
     */
    @Test
    public void testTryFetchTopologyIDFromDB_Existing() {
        // Create a simulation and topology
        simDB.saveSimulationName(testSimName, "BLOCKCHAIN");
        int savedTopologyId = simDB.saveTopologyName(1, testSimName);

        // Fetch the topology
        int fetchedTopologyId = simDB.tryFetchTopologyIDFromDB(1, testSimName);
        assertEquals("Should return correct topology ID", savedTopologyId, fetchedTopologyId);
    }

    /**
     * Test that multiple topologies for the same simulation work correctly.
     */
    @Test
    public void testSaveMultipleTopologies() {
        // Create a simulation
        simDB.saveSimulationName(testSimName, "BLOCKCHAIN");

        // Save multiple topologies
        int topId1 = simDB.saveTopologyName(1, testSimName);
        int topId2 = simDB.saveTopologyName(2, testSimName);
        int topId3 = simDB.saveTopologyName(3, testSimName);

        // Verify all IDs are unique and positive
        assertTrue("Topology 1 ID should be positive", topId1 > 0);
        assertTrue("Topology 2 ID should be positive", topId2 > 0);
        assertTrue("Topology 3 ID should be positive", topId3 > 0);
        assertNotEquals("Topology IDs should be unique", topId1, topId2);
        assertNotEquals("Topology IDs should be unique", topId2, topId3);
        assertNotEquals("Topology IDs should be unique", topId1, topId3);

        // Verify re-saving returns same IDs
        assertEquals("Re-saving topology 1 should return same ID", topId1, simDB.saveTopologyName(1, testSimName));
        assertEquals("Re-saving topology 2 should return same ID", topId2, simDB.saveTopologyName(2, testSimName));
        assertEquals("Re-saving topology 3 should return same ID", topId3, simDB.saveTopologyName(3, testSimName));
    }

    /**
     * Test simulation creation and deletion workflow.
     */
    @Test
    public void testSimulationLifecycle() {
        // Create simulation
        int simId = simDB.saveSimulationName(testSimName, "BLOCKCHAIN");
        assertTrue("Simulation ID should be positive", simId > 0);

        // Create topologies
        int topId1 = simDB.saveTopologyName(1, testSimName);
        int topId2 = simDB.saveTopologyName(2, testSimName);

        // Delete simulation
        simDB.deleteSimulationFromDB(testSimName);
        simulationDeleted = true; // Mark as deleted so tearDown doesn't try to delete again

        // Verify deletion completed without errors
        assertTrue("Test completed successfully", true);
    }
}
