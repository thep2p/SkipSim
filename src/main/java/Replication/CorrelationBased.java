package Replication;

import Simulator.SkipSimParameters;
import SkipGraph.Node;
import SkipGraph.SkipGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static Replication.Pyramid.secondNorm;

/**
 * Created by Yahya on 5/23/2016.
 */
public class CorrelationBased extends Replication
{
    private static final Logger log = LoggerFactory.getLogger(CorrelationBased.class);
    @Override
    public void Algorithm(SkipGraphOperations sgo, int dataOwnerID)
    {
        this.sgo = sgo;
        log.info("CorrelationBased replication started [dataOwner={}, degree={}, capacity={}, topology={}]",
            dataOwnerID,
            SkipSimParameters.getReplicationDegree(),
            SkipSimParameters.getSystemCapacity(),
            SkipSimParameters.getCurrentTopologyIndex());
        resetRep();

        Node dataOwner = (Node) sgo.getTG().mNodeSet.getNode(dataOwnerID);
        int repCounter = 0;
        while (repCounter < SkipSimParameters.getReplicationDegree())
        {
            /*
            Finding the best replicating candidate based on its QoS table and replicating on that node
             */
            int bestRepCandidateIndex = bestNode(dataOwner);
            Node bestRepCandidateNode = (Node) sgo.getTG().mNodeSet.getNode(bestRepCandidateIndex);
            bestRepCandidateNode.setAsReplica(dataOwnerID);

            /*
            To handle the odd replication degrees
             */
            repCounter++;
            if (repCounter >= SkipSimParameters.getReplicationDegree())
            {
                break;
            }

            /*
            Finding the best anti-correlating node with the bestRepCandidate and replicating on that node
             */
            int bestAntiCorrelatedCandidate = bestAntiCorrelated(dataOwner.getAvailabilityTable().get(bestRepCandidateIndex), dataOwner);
            Node bestAntiCorrelatedNode = (Node) sgo.getTG().mNodeSet.getNode(bestAntiCorrelatedCandidate);
            bestAntiCorrelatedNode.setAsReplica(dataOwnerID);

            repCounter++;
        }


    }


    public int bestNode(Node dataOwner)
    {
        /*
        Finding the most available replica based on its QoS
        */
        double maxScore = 0;
        int bestMatch = -1;
        double[] scoreCache = new double[SkipSimParameters.getSystemCapacity()];
        Arrays.fill(scoreCache, -1);

        for (int nodeIndex : dataOwner.getAvailabilityTable().keySet())
        {
            Node node = (Node) sgo.getTG().mNodeSet.getNode(nodeIndex);
            if (node.isOnline() && node.hasStorageCapacity() && !node.isReplica(dataOwner.getIndex()))
            {
                double score;
                if (scoreCache[nodeIndex] >= 0)
                    score = scoreCache[nodeIndex];
                else
                {
                    score = secondNorm(dataOwner.getAvailabilityTable().get(node.getIndex()));
                    scoreCache[nodeIndex] = score;
                }

                //System.out.println("Node " + node.getIndex() + " score " + score);
                if (score > maxScore)
                {
                    bestMatch = node.getIndex();
                    maxScore = score;
                }
            }
        }

        if (bestMatch == -1)
        {
            throw new IllegalStateException("ClusterBased.java: could not find the best QoS node");
        }
        //System.out.println("Best match " + bestMatch);
        return bestMatch;
    }

    /**
     *
     * @param qosVector The qosVector that we want to find the minimum correlation with
     * @param dataOwner The dataOwner node that has the availability table, and we aim on searching in its availability
     *                  table for the minimum correlation with the qosVector
     * @return the index of the node with the minimum correlation with qosVector or -1 if no such a node exists
     */
    public int bestAntiCorrelated(double[] qosVector, Node dataOwner)
    {
        /*
        Finding the best anti-correlated candidate
        */
        double minCorrelation = Double.MAX_VALUE;
        int bestMatch = 0;
        double maxScore = 0;
        for (int nodeIndex : dataOwner.getAvailabilityTable().keySet())
        {
            Node node = (Node) sgo.getTG().mNodeSet.getNode(nodeIndex);

            if(!node.hasStorageCapacity())
                continue;
            double correlation = Correlation(qosVector.clone(), dataOwner.getAvailabilityTable().get(nodeIndex).clone());
            double score = secondNorm(dataOwner.getAvailabilityTable().get(node.getIndex()));
            score /= (correlation + 0.00000000000001);
            if (score > maxScore)
            {
                bestMatch = nodeIndex;
                maxScore = score;
            }
        }

        if (bestMatch == -1)
        {
            throw new IllegalStateException("ClusterBased.java: could not find the best anti correlated node");
        }
        return bestMatch;
    }

    private double Correlation(double[] vector1, double[] vector2)
    {
        if (vector1.length != vector2.length)
        {
            throw new IllegalStateException("CorrelationBased.java: cannot find the correlation between two vectors of different sizes.");
        }
        double c = 0;
        for (int i = 0; i < vector1.length; i++)
        {
            if(vector1[i] == 0)
            {
                vector1[i] = -1;
            }
            if(vector2[i] == 0)
            {
                vector2[i] = -1;
            }
            c += vector1[i] * vector2[i];
        }
        return c;
    }
}
