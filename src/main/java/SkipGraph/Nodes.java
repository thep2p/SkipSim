package SkipGraph;

import Aggregation.BlockchainAvailabilityAggreegation;
import Simulator.AlgorithmInvoker;
import Simulator.SkipSimParameters;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Nodes extends SkipGraphNodes
{
    private static final Logger log = LoggerFactory.getLogger(Nodes.class);
    private static Random numIDRandomGen;
    /**
     * The random variable generating the storage capacity in case of dynamic replication
     */
    private static Random storageCapacityGenerator;
    /**
     * The exponential distribution that generates randomized storage for the nodes
     */
    private static ExponentialDistribution sBandwithCapacityGenerator;
    /**
     * Keeps average storage capacity of the nodes where the average is taken over all the topologies
     */
    private static double overalAverageStorageCapacity;
    /**
     * Keeps average bandwidth capacity of the nodes where the average is taken over all the topologies
     */
    private static double overalAverageBandwidthCapacity;
    protected int totalTime = 0;
    //TODO: Visibility is changed to protected. It was previously private
    protected ArrayList<Integer> searchPathLatency;
    private Node[] mNodeSet;
    private BlockchainAvailabilityAggreegation qosTable;


    public Nodes(Class callerClass)
    {
        /*
        Initializations
         */
        numIDRandomGen = new Random();
        storageCapacityGenerator = new Random();
        if (SkipSimParameters.getBandwidthCapacityRate() > 0)
        {
            double mean = (double) 1.0 / SkipSimParameters.getBandwidthCapacityRate();
            sBandwithCapacityGenerator = new ExponentialDistribution(mean);
        }
        mNodeSet = new Node[SkipSimParameters.getSystemCapacity()];
        double averageStorageCapacity = 0;
        double averageBandwidthCapacity = 0;
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            if (!SkipSimParameters.isStaticSimulation())
            {
                mNodeSet[i] = new Node(i);
                /*
                Assignign storage capacity
                 */
                if (SkipSimParameters.getStorageCapacity() > 0)
                {
//                    int storageLoad = SkipSimParameters.getStorageCapacity();
                    int storageLoad = storageCapacityGenerator.nextInt(SkipSimParameters.getStorageCapacity());
                    while (storageLoad <= 0)
                        storageLoad = storageCapacityGenerator.nextInt(SkipSimParameters.getStorageCapacity());
                    mNodeSet[i].setStorageCapacity(storageLoad);
                    averageStorageCapacity += storageLoad;
                }
                if (SkipSimParameters.getBandwidthCapacityRate() > 0)
                {
                    double bandwithCapacity = sBandwithCapacityGenerator.sample();
                    while (bandwithCapacity <= 0)
                        bandwithCapacity = sBandwithCapacityGenerator.sample();
                    if (bandwithCapacity > 1)
                        bandwithCapacity = 1;
//                    if (bandwithCapacity > 1)
//                        throw new IllegalStateException("Nodes.java: illegal bandwidth load for the nodes: " + bandwithCapacity);
                    mNodeSet[i].setBandwidthCapacity(bandwithCapacity);
                    averageBandwidthCapacity += bandwithCapacity;
                }
            }
            /*
            Generating random coordinates for the nodes in case the caller class is a test class
             */
            if (callerClass.getName().toLowerCase().contains("test"))
            {
                int x = numIDRandomGen.nextInt(SkipSimParameters.getDomainSize() - 1);
                int y = numIDRandomGen.nextInt(SkipSimParameters.getDomainSize() - 1);
                mNodeSet[i].setCoordinate(new Point(x, y));
            }
        }
        if (averageStorageCapacity > 0)
        {
            averageStorageCapacity /= SkipSimParameters.getSystemCapacity();
            overalAverageStorageCapacity += averageStorageCapacity;
            log.info("Nodes.java: Average storage capacity of the nodes on this topology: {}", averageStorageCapacity);
        }
        if (averageBandwidthCapacity > 0)
        {
            averageBandwidthCapacity /= SkipSimParameters.getSystemCapacity();
            overalAverageBandwidthCapacity += averageBandwidthCapacity;
            log.info("Nodes.java: Average bandwidth capacity of the nodes on this topology: {}", averageBandwidthCapacity);
        }

        searchPathLatency = new ArrayList<Integer>();
    }

    /**
     * @param nodeIndex the Node index from mNodeSet
     * @return zero if index if zero, otherwise a random non zero num ID
     */
    public static int getRandomNumID(int nodeIndex)
    {
        /*
         */
        if (nodeIndex == 0)
        {
            return 0;
        }
        else
        {
            int numID = (int) Math.abs(numIDRandomGen.nextInt(10 * SkipSimParameters.getSystemCapacity())) + 1;
            return numID;
        }

    }

    public static double getOveralAverageStorageCapacity()
    {
        return overalAverageStorageCapacity;
    }

//    public void updateAvailabilityVectors(int currentTime)
//    {
//        for (int i = 0; i < system.getSystemCapacity(); i++)
//        {
//            mNodeSet[i].updateAvailabilityVector(currentTime);
//        }
//    }
//
//    private void printAvailabilityTable()
//    {
//        for (int i = 0; i < system.getSystemCapacity(); i++)
//        {
//            double sum = 0;
//            for (int t = 0; t < system.getTimeSlot(); t++)
//                sum = sum + mNodeSet[i].getAvailabilityVector(t);
//            if (sum > 0)
//            {
//                for (int t = 0; t < system.getTimeSlot(); t++)
//                    System.out.print(mNodeSet[i].getAvailabilityVector(t) + " ");
//                System.out.println();
//            }
//        }
//    }

    public static double getOveralAverageBandwidthCapacity()
    {
        return overalAverageBandwidthCapacity;
    }

//  public  double getAverageNumberOfRepCandidates()
//  {
//	  double sum = 0;
//	  int  counter = 0;
//	  for(int i = 0 ; i < Simulator.system.getSystemCapacity(); i++)
//	  {
//		  if(!mNodeSet[i].isEmpty(this))
//		  {
//			  sum += numberOfPossibleReplicationCandidates(i);
//			  counter++;
//		  }
//	  }
//	  averageNumberofRepCandidates += (double)(sum / counter);
//	  return sum;
//  }

//  public int numberOfPossibleReplicationCandidates(int numID)
//  {
//	   int number = 0;
//	   for(int i = 0 ; i < Simulator.system.getNumIDSeed() ; i++)
//		{
//			double sum = 0;
//			if(isNumIDAssigned(i))
//				for(int t = 0 ; t < repTools.getTimeSlots() ; t++)
//					sum = sum + DataTypes.nodesTimeTable.getLocalAvailability(numID, i, t);
//			if(sum > 0)
//			{
//				number++;
//			}
//		}
//	   
//	   return number;
//  }

    public ArrayList<Integer> getSearchPathLatency()
    {
        return searchPathLatency;
    }

    /**
     * NOTE: This function is only getting called at the end of the TopologyGenerator.departureUpdate
     * This function cleans up the qosTable and aggregates the new state of the qos of the nodes per each
     * subdomain of the aggregation table.
     */
    public void qosAggregation(Landmarks landmarks)
    {
//        /*
//        Updates the closet landmark to each node
//         */
//        updateClosestLandmark(landmarks);
        log.info("Blockchain aggregation started");
        qosTable = new BlockchainAvailabilityAggreegation(SkipSimParameters.getAvailabilityAggregationDomainSize(), SkipSimParameters.getFPTI());
        for (int dataOwner = 0; dataOwner < SkipSimParameters.getDataOwnerNumber(); dataOwner++)
        {
            setCorrespondingReplica(dataOwner);
        }
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            Node node = mNodeSet[i];
            if (node.isOnline())
            {
                if (node.nameID.isEmpty())
                {
                    throw new IllegalStateException("Nodes.java: empty name ID for an online node");
                }
                else
                {
                    double[] qosVector = node.getAvailabilityVector();
                    /*
                    Computing the total number of data requesters this node is supporting
                        This node has not yet been selected as a replica, and hence there is no data requester, and hence
                        we give it a single data requester, just to avoid infinity as the qos

                     */
                    int dataRequesterNum = 1;
//                    if(SkipSimParameters.Heterogeneous())
//                    {
//                        for (int dataOwner = 0; dataOwner < SkipSimParameters.getDataOwnerNumber(); dataOwner++)
//                        {
//                            dataRequesterNum += numberOfDataRequesters(node.getIndex(), dataOwner, false);
//                        }
//                    }
                    for (int time = 0; time < SkipSimParameters.getFPTI(); time++)
                    {
                        if (node.getBandwidthCapacity() <= 0 || node.getStorageCapacity() <= 0)
                        {
                            throw new IllegalStateException("Nodes.java: Illegal value for nodes bandwidth or storage capacity");
                        }
                        double qos = ((double) node.getBandwidthCapacity() * node.getNormalizedStorageCapacity() / dataRequesterNum);
                        qosVector[time] *= qos;
                    }
                    int closestLandmarkIndex = node.getClosetLandmarkIndex(landmarks);
                    String closestLandmarkPrefix = landmarks.getDynamicPrefix(closestLandmarkIndex);
                    qosTable.update(closestLandmarkIndex, closestLandmarkPrefix, node.getNameID(), qosVector);
                }
            }
        }
//        /*
//        Prints statistics of available subregions
//         */
//        qosTable.availabileSubregions();
    }

    @Override
    public SkipGraphNode getNode(int i)
    {
        return mNodeSet[i];
    }

    public void clearnodes()
    {
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            setNode(i, null);
        }
    }

    @Override
    public void setNode(int index, SkipGraphNode skipGraphNode)
    {
        mNodeSet[index] = (Node) skipGraphNode;
    }

    public int nodeLength()
    {
        return mNodeSet.length;
    }

    public void renewReplicationInfo()
    {
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            mNodeSet[i].clearReplicaIDSet();
            mNodeSet[i].clearDataRequesterIDSet();
        }
    }

    /*
  Renews a SkipGraph.Node after reading from the skipsim file
  Before renew takes place, SkipGraph.Node only has a coodination
   */
    public void renewNode(int index)
    {
        /*
        replication variables
		 */
        mNodeSet[index].clearDataRequesterIDSet();
        mNodeSet[index].clearReplicaIDSet();
        mNodeSet[index].clearCorrespondingReplicaData();


		/*
        Aggregation.Aggregation parameters
		 */
        mNodeSet[index].setParent(-1);
        mNodeSet[index].resetChildSet();
        mNodeSet[index].resetPathToRoot();


        if (index == 0)
        {
            mNodeSet[index].setNumID(0);
        }
        else
        {
            mNodeSet[index].setNumID(getRandomNumID(index));
        }

        //Used only for static simulations and hence does not need
        //mNodeSet[index].setOnline();


        for (int i = 0; i < SkipSimParameters.getLookupTableSize(); i++)
            for (int j = 0; j < 2; j++)
            {
                mNodeSet[index].setLookup(i, j, -1);
                //mNodeSet[index].setBackup(i, j, -1);
            }
    }

    public int getTotalTime()
    {
        return totalTime;
    }

    public void setTotalTime(int i)
    {
        totalTime = i;
    }

    public void addTime(int destination, int source)
    {
        if (destination != -1 && source != -1)
        {
            double latency = mNodeSet[source].getCoordinate().distance(mNodeSet[destination].getCoordinate());
            searchPathLatency.add((int) latency);
            totalTime += latency;

        }
    }

    public void addMaxTime(int destination1, int destination2, int source)
    {
        if (source != -1)
        {
            if (destination1 != -1 && destination2 != -1)
            {
                totalTime += Math.max(mNodeSet[source].getCoordinate().distance(mNodeSet[destination1].getCoordinate()), mNodeSet[source].getCoordinate().distance(mNodeSet[destination2].getCoordinate()));
            }
            else if (destination1 != -1)
            {
                totalTime += mNodeSet[source].getCoordinate().distance(mNodeSet[destination1].getCoordinate());
            }
            else if (destination2 != -1)
            {
                totalTime += mNodeSet[source].getCoordinate().distance(mNodeSet[destination2].getCoordinate());
            }
        }

    }

    public void resetTotalTime()
    {

        //System.out.println(searchPathLatency.toString() + " " + totalTime);
        totalTime = 0;
        searchPathLatency = new ArrayList<>();
    }

    public int commonBits(int i, int j)
    {
        String s1 = mNodeSet[i].nameID;
        String s2 = mNodeSet[j].nameID;

        int k = 0;

        if (s1.length() > 0 && s2.length() > 0)
        {
            while (s1.charAt(k) == s2.charAt(k))
            {
                k++;
                if (k >= s1.length() || k >= s2.length())// || k >= Simulator.system.nameIDsize )
                {
                    break;
                }
            }
        }

        return k;
    }

    public int minNameIDSize(int i, int j)
    {
        String s1 = mNodeSet[i].nameID;
        String s2 = mNodeSet[j].nameID;

        if (s1.length() < s2.length())
        {
            return s1.length();
        }

        else
        {
            return s2.length();
        }
    }

    public int ClosestLandmark(int nodeIndex, Landmarks L)
    {
        Node n = mNodeSet[nodeIndex];
        double min = Double.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            if (n.getCoordinate().distance(L.getLandmarkCoordination(i)) < min)
            {
                min = n.getCoordinate().distance(L.getLandmarkCoordination(i));
                index = i;
            }
        }

        return index;
    }

    public void updateClosestLandmark(Landmarks L)
    {
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            mNodeSet[i].setClosetLandmarkIndex(ClosestLandmark(i, L));
        }
    }

    public int offlineNodesCounter()
    {
        int num = 0;
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            if (mNodeSet[i].isOffline())
            {
                num++;
            }
        }

        return num;
    }

    public boolean nameIDsDoubleCheck()
    {
        boolean flag = true;
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
            for (int j = 0; j < SkipSimParameters.getSystemCapacity(); j++)
            {
                if (i == j)
                {
                    continue;
                }
                else if (mNodeSet[i].nameID.equals(mNodeSet[j].nameID) && !mNodeSet[i].nameID.isEmpty())
                {
                    log.debug("Same name id: {} {}\n{} {}", i, j, mNodeSet[i].nameID, mNodeSet[j].nameID);
                    flag = false;
                }
            }
        if (flag)
        {
            log.warn("No match was found!");
        }
        return true;
    }

    public int getNumberOfOnlineNodes()
    {
        int counter = 0;
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            if (mNodeSet[i].isOnline())
            {
                //topologyTotalOnlineNodes++;
                counter++;
            }
        }
        return counter;
    }

//TODO awake
//    public double correlation(int id1, int id2)
//    {
//        double sum = 0;
//        for (int t = 0; t < system.getTimeSlot(); t++)
//        {
//            sum = sum + (mNodeSet[id1].getAvailabilityVector(t) * mNodeSet[id1].getAvailabilityVector(t));
//        }
//
//        return Math.sqrt(sum);
//    }

    public ArrayList<Integer> getIndicesOfOnlineNodes()
    {
        ArrayList<Integer> onlineNodes = new ArrayList<>();
        int counter = 0;
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            if (mNodeSet[i].isOnline())
            {
                //topologyTotalOnlineNodes++;
                onlineNodes.add(i);
            }
        }
        return onlineNodes;
    }

    public int getNumberOfOfflineNodes()
    {
        int counter = 0;
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            if (mNodeSet[i].isOffline())
            {
                //topologyTotalOnlineNodes++;
                counter++;
            }
        }
        //System.out.println("Nodes.java: number of offline Nodes" + counter);
        return counter;
    }

    /**
     * @return average number of times a Node received update from other Nodes
     */
    public double averageNumberOfReceivedUpdates()
    {
        double average = 0;
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            double average_row = 0;
            for (int j = 0; j < SkipSimParameters.getSystemCapacity(); j++)
            {
                average_row += mNodeSet[i].getLocalNumberOfUpdatesTable(j);
            }
            average += (average_row / SkipSimParameters.getSystemCapacity());
        }

        return (average / SkipSimParameters.getSystemCapacity());
    }

    private void print(String message)
    {
        if (SkipSimParameters.isLog())
        {
            log.debug("Nodes: {}", message);
        }
    }

    /**
     * @param shouldBeInserted
     * @param sgo
     */
    public void generateNodes(boolean shouldBeInserted, SkipGraphOperations sgo, int currentTime, boolean isTest)
    {
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            Node n = new Node(i);
            Point p = new Point();
            if (!isTest)
            {
                if (SkipSimParameters.getNodeGenerationStrategy().equals("landmark"))
                {
                    p = sgo.getTG().LandmarkBasedSeedRandomNodeGenerator();
                }
                else
                {
                    p = sgo.getTG().UniformRandomNodeGenerator();
                }
            }
            else
            {
                //We are in test mode, and hence the mCoordinate is generated randomly
                p.x = Math.abs(numIDRandomGen.nextInt(SkipSimParameters.getDomainSize() - 1));
                p.y = Math.abs(numIDRandomGen.nextInt(SkipSimParameters.getDomainSize() - 1));
            }

            n.getCoordinate().x = p.x;
            n.getCoordinate().y = p.y;

            if (shouldBeInserted)
            {
                if (AlgorithmInvoker.isNameIDAssignmentDynamic())
                {
                    n.nameID = AlgorithmInvoker.dynamicNameIDAssignment(n, sgo, i);
                }
                sgo.insert(n, sgo.getTG().mNodeSet, i, AlgorithmInvoker.isNameIDAssignmentDynamic(), currentTime);
            }
            else
            {
                mNodeSet[i] = n;
            }

            log.debug("Node generated: name_id={}, num_id={}, index={}", n.nameID, n.getNumID(), i);

        }

    }

    public void printLookupOnlineStatus(int index)
    {
        for (int i = SkipSimParameters.getLookupTableSize() - 1; i >= 0; i--)
        {
            boolean right = false;
            boolean left = false;
            if (mNodeSet[index].getLookup(i, 0) != -1)
                left = mNodeSet[mNodeSet[index].getLookup(i, 0)].isOnline();
            if (mNodeSet[index].getLookup(i, 1) != -1)
                right = mNodeSet[mNodeSet[index].getLookup(i, 1)].isOnline();

            log.debug("Level: {}   Left: {}   Right: {}", i, left, right);
        }

    }

    /**
     * Returns number of data requesters assigned to a replica node by a data owner
     * If the replication is dynamic, it returns the number of online data requesters
     *
     * @param replicaIndex         the address of the replica
     * @param dataOwnerIndex       the address of the data owner
     * @param correspondenceUpdate if TRUE, by each invocation a setCorrespondingReplica is also called to update the
     *                             corresponding set of replicas first. Otherwise, if it is FALSE, it is assumed that the
     *                             setCorrespondingReplica function has been called once for all the nodes, and the is no
     *                             update on the replicas after that, and there is hence no need to recall it agian.
     * @return number of the assigned replicas
     */
    public int numberOfDataRequesters(int replicaIndex, int dataOwnerIndex, boolean correspondenceUpdate)
    {
        if (!SkipSimParameters.isHeterogeneous())
        {
            return 1;
        }
        if (correspondenceUpdate)
            setCorrespondingReplica(dataOwnerIndex);
        int counter = 0;
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            if (!SkipSimParameters.isPublicReplication() && i >= SkipSimParameters.getDataRequesterNumber())
                break;
            if (mNodeSet[i].getCorrespondingReplica(dataOwnerIndex) == replicaIndex)
            {
                if (SkipSimParameters.isDynamicReplication() && mNodeSet[i].isOffline())
                {
                    //Skip offline nodes
                    continue;
                }
                counter++;
            }
        }
        if (counter == 0)
            counter = 1;
        return counter;
    }

    /**
     * This function sets the corresponding replicas of the all nodes with respect
     * to the dataOwner, it only considers online replicas in the dynamic simulations.
     *
     * @param dataOwnerID
     */
    //Todo requires updated tests
    public void setCorrespondingReplica(int dataOwnerID)
    {
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            if(mNodeSet[i].getNameID().isEmpty())
            {
                /*
                if node i name ID is empty, it means that the node has not yet joined the system, since once it joins
                its name ID never gets changed, hence, if a name ID is empty, the corresponding node should be skipped since
                it has not yet registered to the system
                 */
                continue;
            }
            /*
             Finding the corresponding replica of node i
             */
            if (mNodeSet[i].isReplica(dataOwnerID))
            {
                /*
                If node i itself is a replica
                 */
                if (mNodeSet[i].isOnline())
                {
                    mNodeSet[i].setDataRequesterID(dataOwnerID);
                    mNodeSet[i].setCorrespondingReplica(dataOwnerID, i);
                }
                else
                {
                    mNodeSet[i].setCorrespondingReplica(dataOwnerID, -1);
                }
                continue;
            }

            int closestReplicaIndex = -1;
            double closestReplicaDistance = Integer.MAX_VALUE;
            for (int j = 0; j < SkipSimParameters.getSystemCapacity(); j++)
            {

                try
                {
                    if (mNodeSet[j].isOnline() && mNodeSet[j].isReplica(dataOwnerID)
                            && mNodeSet[i].getCoordinate().distance(mNodeSet[j].getCoordinate()) < closestReplicaDistance)
                    {
                        /*
                        Updating the closest replica if there is an online replica closer than the current replica
                         */
                        closestReplicaIndex = j;
                        closestReplicaDistance = mNodeSet[i].getCoordinate().distance(mNodeSet[j].getCoordinate());
                    }
                }
                catch (Exception ex)
                {
                    // Exception handling for coordinate distance calculation
                }
            }

            if (SkipSimParameters.isPublicReplication() || i < SkipSimParameters.getDataRequesterNumber())
            {
                mNodeSet[i].setDataRequesterID(dataOwnerID);
                mNodeSet[i].setCorrespondingReplica(dataOwnerID, closestReplicaIndex);
            }

        }
    }

    /**
     * Returns an instance of the qos table
     *
     * @return an instance of the qos table
     */
    public BlockchainAvailabilityAggreegation getQosTable()
    {
        return qosTable;
    }


    /**
     * Given a landmark prefix, and a name Id that starts with that landmark prefix, this function returns back the
     * body of the name ID i.e., the name ID without that landmark prefix
     *
     * @param landmarkPrefix
     * @param nameID
     * @return
     */
    public static String extractNameIDBody(String landmarkPrefix, String nameID)
    {
        return nameID.substring(landmarkPrefix.length());
    }

}
