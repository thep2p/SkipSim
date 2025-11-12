package Aggregation;

import NameIDAssignment.NameID_Assignment_LANS;
import Simulator.SkipSimParameters;
import SkipGraph.Nodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.stream.DoubleStream;


/**
 * This class emulates the blockchain-based availability aggregation for the nodes
 */
public class BlockchainAvailabilityAggreegation
{
    private static final Logger log = LoggerFactory.getLogger(BlockchainAvailabilityAggreegation.class);

    private final int subDomainLength;
    private final int timeSlots;
    private Hashtable<String, double[]>[] availabilityTable;
    private Hashtable<String, Integer>[] updateCounterTable;

    public BlockchainAvailabilityAggreegation(int subDomainLength, int timeSlots)
    {
        this.subDomainLength = subDomainLength;
        this.timeSlots = timeSlots;
        /*
        subDomainNum = 2^subDomainLength, is the number of entries in the HashTable.
         */
        int subDomainNum = (int) Math.pow(2, subDomainLength);

        /*
        Initializing the array of hash tables for the entire system
         */
        availabilityTable = new Hashtable[SkipSimParameters.getLandmarksNum()];
        updateCounterTable = new Hashtable[SkipSimParameters.getLandmarksNum()];

        /*
        Initializing the hash tables for each region
        In the inner loop, HashTable for each region is initialised.
        Arrays with the size timeSlots containing zeros are added to the binaryString keys.
        HashTable.put(key, array)
        */
        for(int region = 0; region < SkipSimParameters.getLandmarksNum(); region++)
        {
            availabilityTable[region] = new Hashtable<>(subDomainNum);
            updateCounterTable[region] = new Hashtable<>(subDomainNum);
            for (int i = 0; i < subDomainNum; i++)
            {
                String key = Integer.toBinaryString(i);
                //Padding of binaryStrings for example 1 is padded to 001 if subDomainLength is 3.
                if (subDomainLength != key.length())
                    key = String.format("%0" + (subDomainLength - key.length()) + "d%s", 0, key);
                //System.out.println("BlockchainAvailabilityAggreegation.java" + key);
                double[] array = new double[timeSlots];
                Arrays.fill(array, 0);
                availabilityTable[region].put(key, array);
                updateCounterTable[region].put(key, 0);
            }
        }
    }

    /**
     * @param id and an array
     * @requires id to be greater than or equal to subDomainLength
     * @modifies availabilityTable
     * @effects First, it finds the key in the HashTable that corresponds to the
     * prefix of the id. Then, it adds the input array to the array saved
     * for that key in the HashTable and it puts the summation array to the
     * HashTable for the key. Adding two arrays means element-wise summation.
     */
    public void update(int region, String landmarkPrefix, String id, double[] array)
    {
        /*
        Extracting body from the prefix
         */
        String body = Nodes.extractNameIDBody(landmarkPrefix, id);
        if (body.length() < subDomainLength)
            throw new IllegalArgumentException("BlockchainAvailabilityAggreegation.java: Id must be greater than or equal to the size!");
        if (array.length != timeSlots)
            throw new IllegalArgumentException("BlockchainAvailabilityAggreegation.java: Illegal size for input availability array");
        //key which is the prefix of id, is extracted.
        String key = body.substring(0, subDomainLength);
        double[] previousArray = availabilityTable[region].get(key);
        double[] result = new double[previousArray.length];

        /*
        result array is created by element-wise addition the existing array and the input array.
         */
        int oldCounterValue = updateCounterTable[region].get(key);
        int newCounterValue = oldCounterValue + 1;
        for (int i = 0; i < previousArray.length; i++)
        {
            result[i] = ((double) ((previousArray[i] * oldCounterValue) + array[i]))/ newCounterValue;
        }
        /*
        update the HashTable with result array.
         */
        availabilityTable[region].put(key, result);
        updateCounterTable[region].put(key, oldCounterValue + 1);
    }

    /**
     * @param region the closest landmark index to the node
     * @param id the identifier of the node
     * @requires id to be greater than or equal to subDomainLength
     * @modifies -
     * @effects First, it finds the key in the HashTable that corresponds to the
     * prefix of the id. Then, it returns the array corresponding to the key
     * in the HashTable.
     */
    public double[] getArray(int region, String id)
    {
        if (id.length() < subDomainLength)
            throw new IllegalArgumentException("BlockchainAvailabilityAggreegation.java: Id must be greater than or equal to the size!");
        String key = id.substring(0, subDomainLength);
        return availabilityTable[region].get(key);
    }


    //getter for subDomainLength
    public int getSubDomainLength()
    {
        return subDomainLength;
    }

    /**
     * Returns the qos table of each region
     * @param region index of the landmark (region)
     * @return the aggregated qos table of that region
     */
    public Hashtable<String, double[]> getAvailabilityTable(int region)
    {
        return availabilityTable[region];
    }

    /**
     *
     * @return a copied version of the entire availablility table of the system
     */
    public Hashtable<String, double[]>[] getAvailabilityTable()
    {
        Hashtable<String, double[]>[] copyTable = availabilityTable.clone();
        return copyTable;
    }

    public int getUpdateCounterTable(String id, int region)
    {
        if (id.length() < subDomainLength)
            throw new IllegalArgumentException("BlockchainAvailabilityAggreegation.java: Id must be greater than or equal to the size!");
        String key = id.substring(0, subDomainLength);
        return updateCounterTable[region].get(key);
    }

    /**
     * Given an prefix and a region index, returns all the entries of availabilityTable int that region
     * that have that prefix. This function returns the
     * entries as a another HashTable with the same keys but for values it is the aggregated availability vector of each
     * subdomain
     *
     * @param prefix the prefix of interest, which should be less than or equal the subDomainLength of the table.
     * @return a hashtable containing the subtable of all the subdomains that start with the prefix
     */
    public Hashtable<String, double[]> getSubTable(int region, String prefix)
    {
        Hashtable<String, double[]> subTable = new Hashtable<>();
        if (prefix != null && prefix.length() > subDomainLength)
            throw new IllegalArgumentException("BlockchainAvailabilityAggreegation.java: Illegal prefix length value for subTable extraction:" + prefix.length());
        for (String key : availabilityTable[region].keySet())
        {
            if (prefix == null || key.startsWith(prefix))
            {
                double[] scoreVector = availabilityTable[region].get(key);
//                int counter = updateCounterTable[region].get(key);
//                if(counter <= 0 && DoubleStream.of(scoreVector).sum() > 0)
//                    throw  new IllegalStateException("BlockchainAvailabilityAggreegation.java: Illegal update counter state for a non-zero qos table:" + counter);
//                for (int i = 0; i < scoreVector.length; i++)
//                {
//                    if (counter == 0)
//                        scoreVector[i] = 0;
//                    else
//                        scoreVector[i] /= counter;
//                }
                subTable.put(key, scoreVector);
            }
        }
        return subTable;
    }

    public int[] availabileSubregions()
    {
        int[] nonZeroSubRegionCounter = new int[SkipSimParameters.getLandmarksNum()];
        Arrays.fill(nonZeroSubRegionCounter, 0);
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            for (String subDomain : availabilityTable[i].keySet())
            {
                int counter = 0;
                for(int t = 0 ; t < SkipSimParameters.getFPTI(); t++)
                {
                    if(availabilityTable[i].get(subDomain)[t] > 0)
                    {
                        counter++;
                    }
                }
                if (DoubleStream.of(availabilityTable[i].get(subDomain)).sum() > 0)
                {
                    log.debug("Region: {}, sub-domain: {}, total availability: {}",
                        i, subDomain, DoubleStream.of(availabilityTable[i].get(subDomain)).sum());
                }
                if(counter == SkipSimParameters.getFPTI())
                {
                    nonZeroSubRegionCounter[i]++;
                }
            }

        }

        log.debug("Non-zero subregion counters: {}", Arrays.toString(nonZeroSubRegionCounter));
        return nonZeroSubRegionCounter;
    }
}
