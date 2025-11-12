package SkipGraph;

import Blockchain.LightChain.HashTools;
import Blockchain.LightChain.Transaction;
import DataTypes.Constants;
import Simulator.SkipSimParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * An abstract class representing a node in a skip-graph.
 */
public abstract class SkipGraphNode
{
    private static final Logger log = LoggerFactory.getLogger(SkipGraphNode.class);

    protected static Random sRandom = new Random();

    /**
     * Every node (except the inital node) is inserted into a skip-graph by an introducer. This
     * denotes the introducer's index for this node.
     */
    private int introducer;
    /**
     * The index of the node.
     */
    protected int index;
    /**
     * Numerical ID of the Node, non-negative integers
     */
    protected int numID;

    /**
     * Name ID of the Node.
     */
    protected String nameID;
    /**
     * The lookup table of the Nodes
     * lookup[i][0] means level i and left direction,
     * lookup[i][1] means level i and right direction
     */
    protected int[][] lookup;

    /**
     * Contains the hash of the transaction as a 32-bit integer.
     * This hash value is calculated from the index.
     */
    private int hash;

    public SkipGraphNode()
    {
        nameID = "";
        index = 0;
        calculateHashAndNumID();
        //lookup = new int[system.getLookupTableSize()][2];
    }

    /**
     * Calculates the hash code of the transaction using the index. This should be invoked each time the index is changed.
     */
    private void calculateHashAndNumID() {
        // If numerical id hashing is turned on, perform SHA3 hashing with the index as the input.
        if(SkipSimParameters.NumIDHashing) {
            // Appending a character at the end of the input to distinguish between transactions and nodes.
            String input = "" + index + ((this instanceof Transaction) ? "t" : "n");
            byte[] hashBytes = HashTools.hash(input);
            // Compressing the 256 bit SHA3 hash into a 32 bit integer.
            hash = HashTools.compressToInt(hashBytes);
            numID = hash;
        } else {
            // Otherwise, simply substitute a random number as the hash.
            hash = sRandom.nextInt(100);
        }
    }

    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        this.index = index;
        // Recalculate the hash.
        calculateHashAndNumID();
    }

    public int getIntroducer()
    {
        return introducer;
    }

    public void setIntroducer(int index)
    {
        this.introducer = index;
    }

    public void setNumID(int numId) {
        if(SkipSimParameters.NumIDHashing) {
            // When hashing is on, explicit numerical id assignments are not allowed, as their numerical
            // ids are their hash codes.
            return;
        }
        this.numID = numId;
    }

    public int getNumID() {
        if(SkipSimParameters.NumIDHashing) {
            return hashCode();
        } else {
            return numID;
        }
    }

    public int getLookup(int i, int j)
    {
        return lookup[i][j];
    }

    /**
     * @param i       level number
     * @param j       0: left neighbor and 1: right neighbor
     * @param address address of the neighbor to be inserted in the lookup table
     */
    public void setLookup(int i, int j, int address)
    {
        if (SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.DYNAMIC)
                || SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.BLOCKCHAIN))
        {
            /*
            Preventing self loops in dynamic simulation
             */
            if (address == index)
            {
                lookup[i][j] = -1;
                return;
            }
        }

        lookup[i][j] = address;
    }

    public String getNameID()
    {
        return nameID;
    }

    public void setNameID(String nameID)
    {
        this.nameID = nameID;
    }

    /**
     * @return false if at least one entry is not equal to -1, otherwise returns true
     */
    public boolean isLookupTableEmpty(int lookupTableSize)
    {
        if(lookup == null)
            return true;

        for (int i = lookupTableSize - 1; i >= 0; i--)
            for (int j = 0; j < 2; j++)
                if (lookup[i][j] != -1)
                {
                    return false;
                }
        return true;

    }

    /**
     * Prints the lookup table of the Node
     */
    public void printLookup()
    {
        for (int i = SkipSimParameters.getLookupTableSize() - 1; i >= 0; i--)
            log.debug("Level: {}   Left: {}   Right: {}", i, lookup[i][0], lookup[i][1]);
    }

    @Override
    public String toString()
    {
        return "SkipGraphNode: Index =  " + index + " Name ID = " + nameID  + " Numerical ID = " + numID ;
    }

    @Override
    public int hashCode()
    {
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof SkipGraphNode) && hashCode() == other.hashCode();
    }
}
