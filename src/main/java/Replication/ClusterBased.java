package Replication;

import Aggregation.AutoKMeanClustering;
import Simulator.SkipSimParameters;
import SkipGraph.Node;
import SkipGraph.Nodes;
import SkipGraph.SkipGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static Replication.Pyramid.secondNorm;

public class ClusterBased extends Replication
{
    private static final Logger log = LoggerFactory.getLogger(ClusterBased.class);
    private Hashtable<Integer, Integer> clusterSize;

    @Override
    public void Algorithm(SkipGraphOperations sgo, int dataOwnerID)
    {
        this.sgo = sgo;
        log.info("ClusterBased.java: The clusterbased replication started");
        resetRep();
        dataRequesterPopulation();
        Node dataOwner = (Node) sgo.getTG().mNodeSet.getNode(dataOwnerID);
        /*
        Perform a clustering based on the availability table of the data owner
         */
        Hashtable<Integer, Integer> mappingCluster = clustering(dataOwner);


        //Hashtable<Integer, Integer> clusterQuota = clusterReplicationQuota(dataOwner.getAvailabilityTable().size());
        /*
		Replicate in each cluster based on quota
		Note: Cluster IDs start from 0
		 */
        for (int i = 1; i <= SkipSimParameters.getReplicationDegree(); i++)
        {
            int bestMatchIndex = getBestMatch(i, mappingCluster, dataOwner, sgo.getTG().mNodeSet);
            Node replica = (Node) sgo.getTG().getNodeSet().getNode(bestMatchIndex);
            replica.setAsReplica(dataOwnerID);
        }

    }

    public Hashtable<Integer, Integer> clustering(Node dataOwner)
    {
        clusterSize = new Hashtable<>();
        Hashtable<Integer, double[]> intKeyAvailabilityTable = dataOwner.getAvailabilityTable();

        /*
        Converting the integer index --> availability mapping into string index --> mapping
         */
        Hashtable<String, double[]> stringKeyAvailabilityTable = new Hashtable<>();
        for (int index : intKeyAvailabilityTable.keySet())
        {
            stringKeyAvailabilityTable.put(String.valueOf(index), intKeyAvailabilityTable.get(index));
        }

        /*
        K-mean clustering on the stringKeyAvailabilityTable
         */
        Hashtable<String, Integer> stringKeyClusterMapString =
                AutoKMeanClustering.AutoKMeanClustering(stringKeyAvailabilityTable, SkipSimParameters.getReplicationDegree());


        /*
        Converting back string index --> cluster mapping into integer index --> cluster
         */
        Hashtable<Integer, Integer> intKeyClusterMapping = new Hashtable<>();
        for (String strIndex : stringKeyClusterMapString.keySet())
        {
            int clusterID = stringKeyClusterMapString.get(strIndex);
            intKeyClusterMapping.put(Integer.parseInt(strIndex), clusterID);
            if (clusterSize.containsKey(clusterID))
                clusterSize.put(clusterID, clusterSize.get(clusterID) + 1);
            else
                clusterSize.put(clusterID, 1);
        }

        return intKeyClusterMapping;


    }

    /**
     * Given a cluster ID find the best node from the availability table of the data owner, that satisfies the best QoS
     *
     * @param targetCluster  id of the target cluster to search for
     * @param mappingIndices the cluster based mapping index between each node and its corresponding cluster. The nodes
     *                       are comming from the availability table of the data owner
     * @param dataOwner      The data owner node
     * @param nodes          the Nodes set
     * @return index of the best match or -1 if there is no match available
     */
    public int getBestMatch(int targetCluster, Hashtable<Integer, Integer> mappingIndices, Node dataOwner, Nodes nodes)
    {
        for (int dataOwnerIndex = 0; dataOwnerIndex < SkipSimParameters.getDataOwnerNumber(); dataOwnerIndex++)
        {
            nodes.setCorrespondingReplica(dataOwnerIndex);
        }

        double maxQoS = 0;
        int bestMatch = -1;
        double[] scoreCache = new double[SkipSimParameters.getSystemCapacity()];
        Arrays.fill(scoreCache, -1);
        for (int i : mappingIndices.keySet())
        {
            if (mappingIndices.get(i) != targetCluster)
                continue;

            Node node = (Node) nodes.getNode(i);
            if (node.isOnline() && node.hasStorageCapacity() && !node.isReplica(dataOwner.getIndex()))
            {
                double score;
                if (scoreCache[i] >= 0)
                    score = scoreCache[i];
                else
                {
                    score = secondNorm(dataOwner.getAvailabilityTable().get(node.getIndex()));
                    scoreCache[i] = score;
                }

                if (score > maxQoS)
                {
                    bestMatch = node.getIndex();
                    maxQoS = score;
                }
            }
        }
        if (bestMatch == -1)
        {
            throw new IllegalStateException("ClusterBased.java: could not find the best match for cluster: " + targetCluster);
        }
        return bestMatch;
    }


    /**
     * Note: This function should be called after a call to the clustering function.
     * This function distributes the replication degree based on the population of nodes inside each cluster.
     *
     * @return a cluster --> replication quota hash table
     */
    public Hashtable<Integer, Integer> clusterReplicationQuota(int availabilityTableSize)
    {
		/*
		Finding Replication Quota of Each Cluster
		 */
        double repUnit = (double) SkipSimParameters.getReplicationDegree() / availabilityTableSize;
        Hashtable<Integer, Integer> clusterQuota = new Hashtable<>();
        int QuotaSum = 0;
        int maxQuota = 0;
        int maxIndex = 0;
        for (int i = 1; i <= SkipSimParameters.getReplicationDegree(); i++) //Cluster ids start from 1
        {
            clusterQuota.put(i, (int) Math.floor((double) repUnit * clusterSize.get(i)));
            if (clusterQuota.get(i) > maxQuota)
            {
                maxQuota = clusterQuota.get(i);
                maxIndex = i;
            }
            QuotaSum += clusterQuota.get(i);
        }

        while (QuotaSum < SkipSimParameters.getReplicationDegree())
        {
            clusterQuota.put(maxIndex, clusterQuota.get(maxIndex) + 1);
            QuotaSum++;
        }

        while (QuotaSum > SkipSimParameters.getReplicationDegree())
        {
            clusterQuota.put(maxIndex, Math.max(clusterQuota.get(maxIndex) - 1, 0));
            QuotaSum--;
        }

        if (QuotaSum != SkipSimParameters.getReplicationDegree())
        {
            throw new IllegalStateException("ClusterBased.java: inconsistent replication quota sum: " + QuotaSum);
        }

        return clusterQuota;

    }

    /**
     * Sorts the hash table t in descending order based on the values
     *
     * @param t the input hashtable
     */
    public static ArrayList<Map.Entry<?, Integer>> sortValue(Hashtable<?, Integer> t)
    {

        //Transfer as List and sort it
        ArrayList<Map.Entry<?, Integer>> l = new ArrayList(t.entrySet());
        Collections.sort(l, new Comparator<Map.Entry<?, Integer>>()
        {
            public int compare(Map.Entry<?, Integer> o1, Map.Entry<?, Integer> o2)
            {
                return -o1.getValue().compareTo(o2.getValue());
            }
        });

        log.debug("Sorted cluster values: {}", l);
        return l;
    }
}