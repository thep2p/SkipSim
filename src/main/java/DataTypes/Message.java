package DataTypes;

import Blockchain.LightChain.Transaction;
import Simulator.AlgorithmInvoker;
import Simulator.SkipSimParameters;
import SkipGraph.*;
import ChurnStabilization.*;

import java.util.ArrayList;

/**
 * Created by Yahya on 8/28/2016.
 */
public class Message
{
    private static ArrayList<Integer> nodeIndices;

    /**
     * Numerical ID distance to the search target that is used to evaluate the closeness to the target
     */
    private static int numIDistanceToTarget;

    /**
     * Name ID similarity to the search target that is used to evaluate the closeness to the target
     */
    private static int nameIDSimilarityToTarget;

    public static int getNumIDistanceToTarget()
    {
        return numIDistanceToTarget;
    }

    public static void setNumIDistanceToTarget(int numIDistanceToTarget)
    {
        Message.numIDistanceToTarget = numIDistanceToTarget;
    }

    public static int getNameIDSimilarityToTarget()
    {
        return nameIDSimilarityToTarget;
    }

    public static void setNameIDSimilarityToTarget(int nameIDSimilarityToTarget)
    {
        Message.nameIDSimilarityToTarget = nameIDSimilarityToTarget;
    }

    public Message()
    {
        //System.out.println("Message.java: a new search Message created");
        numIDistanceToTarget = Integer.MAX_VALUE;
        nameIDSimilarityToTarget = Integer.MIN_VALUE;
        nodeIndices = new ArrayList<>();
    }

    public int getSearchPathSize()
    {
        return nodeIndices.size();
    }

    public ArrayList<Integer> getPiggyBackedNodes()
    {
        return nodeIndices;
    }


    //TODO this function only piggybacks the SkipGraph Nodes (i.e., peers) and not blockchain's blocks and transactions

    /**
     * Piggybacks the availability vector and instantaneous availability probability of the nodes to the message
     * @param index the index of the node to piggyback on the message
     * @param ns the Nodes instance
     */
    public void piggyback(int index, Nodes ns)
    {
        ChurnStabilization alg = AlgorithmInvoker.churnStabilization();

        /*
        Online probabilities for churn stabilization
        */
        if(alg != null)
        {
            for(int i: nodeIndices)
            {
                Node n = (Node) ns.getNode(i);
                alg.insertIntoBucket(n.getIndex(), n.getNumID(), n.getAvailabilityProbability(), index, ns);
            }
        }


        /*
        Availability vectors for replication
         */
        if(SkipSimParameters.isDynamicReplication())
        {
            for(int i: nodeIndices)
            {
                Node n = (Node) ns.getNode(i);
                double[] availabilityVector;
                if(SkipSimParameters.isHeterogeneous())
                {
                    /*
                    Nodes have heterogeneous bandwidth and storage capacity, and hence their availability vector should reflect
                    these as well.
                     */
                    availabilityVector = new double[SkipSimParameters.getFPTI()];
                    for(int time = 0; time < availabilityVector.length; time++)
                    {
                        availabilityVector[time] = n.getBandwidthCapacity() * n.getAvailabilityVector()[time] / (n.getReplicatedLoad() + 1);
                    }
                }
                else
                {
                    /*
                    Nodes have homogeneous bandwidth and storage capacity, and their availability vector just corresponds to their
                    probability of being online
                     */
                    availabilityVector = n.getAvailabilityVector().clone();
                }
                ((Node) ns.getNode(index)).setAvailabilityTable(n.getIndex(), availabilityVector);
            }
        }

        /*
        Adding the intermediate Node information
         */
        if(!nodeIndices.contains(index))
        {
            nodeIndices.add(index);
        }
    }

//    public void piggybackBlock(int index, BlockGraphOperations bgo, int currentTime)
//    {
//        /*ChurnStabilization alg = new AlgorithmInvoker().churnStabilization();
//        if(alg != null)
//        {
//            for(int i: nodeIndices)
//            {
//                Node n = sgo.getTG().mNodeSet.getNode(i);
//                //sgo.getTG().mNodeSet.getNode(index).addToBucket(n.getNumID(), n.getIndex(), sgo.getTG().mNodeSet.commonPrefixLength(i, index), currentTime);
//                alg.insertIntoBucket(n.getIndex(), n.getNumID(), index, sgo.getTG().mNodeSet);
//            }
//        }*/
//        if(!nodeIndices.contains(index))
//        {
//            nodeIndices.add(index);
//        }
//    }

//    /**
//     *
//     * @param searchTarget search target
//     * @param tg TopologyGenerator
//     * @return the closest num ID less than the search target, or -1 if there isn't such
//     */
//    public int getBest(int searchTarget, TopologyGenerator tg)
//    {
//        int minDistance = Integer.MAX_VALUE;
//        int bestIndex = -1;
//        for(int index : nodeIndices)
//        {
//            int numID = tg.mNodeSet.getNode(index).getNumID();
//            if(numID == searchTarget)
//                return index;
//            int distance = Math.abs(searchTarget - numID);
//            if(distance < minDistance && tg.mNodeSet.getNode(index).isOnline())
//            {
//                minDistance = distance;
//                bestIndex = index;
//            }
//        }
//        return bestIndex;
//    }

    public boolean contains(int index)
    {
        return nodeIndices.contains(index);
    }

    public static ArrayList<Integer> getNodeIndices()
    {
        return nodeIndices;
    }

    public void printSearchPath(SkipGraphNodes nodeSet, boolean lookupPrinted)
    {
        System.out.println("----------------------------------");
        System.out.println("Message.java: print search path");
        for(int index: nodeIndices)
        {
            /*
            Null safety check: Skip nodes/transactions that haven't been fully initialized yet.
            This can happen during insertion when a transaction is added to the search path
            before it's been fully inserted into the Skip Graph.
             */
            if(nodeSet.getNode(index) == null)
            {
                System.out.println("Message.java/ WARNING: Skipping null node at index " + index + " (not yet initialized)");
                continue;
            }

            if(nodeSet instanceof Nodes)
            {
                System.out.println("Message.java/ Node: index " + index + " numID " + nodeSet.getNode(index).getNumID());
            }
            else
            {
                System.out.println("Message.java/ Block or Transaction: index " + index
                        + " numID " + nodeSet.getNode(index).getNumID()
                        + " owner " + ((Transaction) nodeSet.getNode(index)).getOwnerIndex());
            }
            if(lookupPrinted)
            {
                nodeSet.printLookupNumID(index);
            }

        }
        System.out.println("----------------------------------");
    }
}
