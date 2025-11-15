package Blockchain.LightChain;


import Simulator.SkipSimParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.util.Objects.hash;

public class Block extends Transaction
{
    private static final Logger log = LoggerFactory.getLogger(Block.class);

    /**
     * Indices of the transactions that are included in this block. The transactions are retrievable from the Transactions
     * class that acts as the transactions database.
     */
    private ArrayList<Integer> listOfTransactions;
    private int[][] lookup = new int[Transaction.LOOKUP_TABLE_SIZE][2];


    public Block(int blockIndex, int ownerIndex, int previous)
    {
        super(previous, ownerIndex);
        if (SkipSimParameters.isLog())
            log.debug("New block created - index: {}, owner: {}, name ID: {}", getIndex(), mOwnerIndex, getIndex());

//        if(ownerIndex != -1){
//            mNodeSet.getNode(ownerIndex).addToBlockSet(blockIndex);
//            System.out.println("addToBlockSet= Node Index: " + ownerIndex + " Block index: " + blockIndex);
//        }
//        this.previousBlockIndex = previousBlockIndex;
//        if (shouldNumIDBeAssigned)
//        {
//            if (blockIndex == 0)
//            {
//                numID = 0;
//            }
//            else
//            {
//               numID = bs.getRandomNumID(blockIndex);
//            }
//
//        }else
//        {
//            numID = -1;
//        }
        for (int i = 0; i < Transaction.LOOKUP_TABLE_SIZE; i++)
        {
            for (int j = 0; j < 2; j++)
            {
                lookup[i][j] = -1;
            }
        }
    }

//    public void setOwnerIndex(int ownerIndex)
//    {
//        this.ownerIndex = ownerIndex;
//    }
//
//    public int getNumID()
//    {
//
//        return numID;
//    }
//
//    public void setNumID(int numID)
//    {
//        this.numID = numID;
//    }

    public ArrayList<Integer> getListOfTransactions()
    {
        return listOfTransactions;
    }

    public int getLookup(int i, int j)
    {
        return lookup[i][j];
    }

    /**
     * @param i       level number
     * @param j       0: left neighbor and 1: right neighbor
     * @param address address of the Block to be inserted in the lookup table
     */
    public void setLookup(int i, int j, int address)
    {

        // TODO: in Node.java, some work is done depending on the simulation type. Should we impelement a similar logic? - yes
        lookup[i][j] = address;
    }



//    public boolean isOffline(Nodes mNodeSet)
//    {
//        if (ownerIndex == -1)
//        {
//            return false;
//        }
//        return mNodeSet.getNode(ownerIndex).isOffline();
//    }

//    public int getSystemCapacity()
//    {
//        return system.getBlockCapacity();
//    }

//    public int getPreviousBlockIndex()
//    {
//        return previousBlockIndex;
//    }

//    public int neighborNumber()
//    {
//        Set<Integer> neighbors = new HashSet<>();
//        for (int i = 0; i < lookup.length; i++)
//            for (int j = 0; j < 2; j++)
//            {
//                if (lookup[i][j] != -1)
//                {
//                    neighbors.add(lookup[i][j]);
//                }
//            }
//
//        return neighbors.size();
//    }

    /**
     * Prints the lookup table of a block
     */
    public void printLookup()
    {
        if (log.isDebugEnabled()) {
            StringBuilder levels = new StringBuilder();
            for (int i = Transaction.LOOKUP_TABLE_SIZE - 1; i >= 0; i--)
                levels.append(String.format("[L%d: L=%d,R=%d] ", i, lookup[i][0], lookup[i][1]));
            log.debug("Block lookup table: {}", levels.toString().trim());
        }
    }

    /**
     *
     * @return TRUE if the block lookup table is all empty (i.e., -1), otherwise returns FALSE that means that
     * there is at least one non-negative entry.
     */
    public boolean isLookupTableEmpty()
    {
        for (int i = 0 ; i < Transaction.LOOKUP_TABLE_SIZE ; i++)
            for (int j = 0; j < 2; j++)
                if (lookup[i][j] != -1)
                {
                    return false;
                }
        return true;
    }

//    public int getIndex()
//    {
//        return index;
//    }
//
//    public void setIndex(int index)
//    {
//        this.index = index;
//    }
//
//    public int getOwnerIndex()
//    {
//        return ownerIndex;
//    }

//    public String getNameID()
//    {
//        return nameID;
//    }
//
//    public void setNameID(String nameID)
//    {
//        this.nameID = nameID;
//    }

//    public int getIntroducer()
//    {
//        return introducer;
//    }

//    @Override
//    public boolean equals(Object o)
//    {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        Block block = (Block) o;
//        return previousBlockIndex == block.previousBlockIndex &&
//                Objects.equals(ownerIndex, block.ownerIndex) &&
//                Objects.equals(listOfTransactions, block.listOfTransactions) &&
//                Objects.equals(signature, block.signature) &&
//                Arrays.equals(lookup, block.lookup);
//    }
//
    @Override
    public int hashCode()
    {

        int result = Objects.hash(Previous, Wire, listOfTransactions.hashCode());
        //result = 31 * result + Arrays.hashCode(lookup);
        return result;
    }

    /**
     *
     * @param i a counter on the validator number
     * @return the computed numerical ID of the ith validator
     */
    @Override
    public int getValidatorNumID(int i)
    {
        //TODO should be tested against the result
        return hash(Previous, listOfTransactions, i);
    }

}