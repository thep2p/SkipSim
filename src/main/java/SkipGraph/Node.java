package SkipGraph;

import AvailabilityPrediction.*;
import Blockchain.LightChain.HashTools;
import Blockchain.LightChain.Transaction;
import Blockchain.LightChain.Transactions;
import ChurnStabilization.BucketItem;
import ChurnStabilization.ChurnStochastics;
import DataTypes.Constants;
import DataTypes.Message;
import Simulator.Main;
import Simulator.SkipSimParameters;

import java.awt.*;
import java.io.Serializable;
import java.util.List;
import java.util.*;


public class Node extends SkipGraphNode implements Serializable
{

    private Point mCoordinate;
    /**
     * A mapping as (DataOwnerIndex, ReplicaIndex) that mappes each data owner index to a replica index
     * which this node is supposed to contact instead of contacting the data owner
     */
    private Hashtable<Integer, Integer> correspondingReplica;

    /**
     * Keeps the index of the closest landmark to this node
     * By closest we mean the with respect to Euclidean coordinate
     * This variable is updated locally by closestLandmark function without any external access
     */
    private int closestLandmark;

    /**
     * Availability vector of the Nodes as described by awake
     * mAvailabilityVector[timeSlot] returns the availability probability of the Node within the
     * timeSlot of FPTI (see in Awake) as an integer number btw 0 to 1
     */
    private double[] mAvailabilityVector;

    /**
     * Availability table of a Node as described by Awake
     * upon existence, the (index, availabilityVector) entry corresponds to the availability vector of the Node denoted by index.
     */
    private Hashtable<Integer, double[]> mAvailabilityTable;

    private int arrivalTime; //Arrival time of a SkipGraph.Node to the Simulator.system
    //private int numID;//Number ID is assumed to be greater than 0
    private double sessionLength;
    private double departureTime; //Departure time of a SkipGraph.Node to the Simulator.system


    private LinkedList<BucketItem> bucket[][];
    /**
     * Keeps the latency experience of each Node to its bucket list
     */
    private Hashtable<Integer, Double> bucketLatencyTable;

    /**
     * Keeps the last time the Node connected to each of its bucket Nodes
     */
    private Hashtable<Integer, Integer> mLastConnectionTime;

    /**
     * number of messages the Node received with the address of piggybacked Node
     */
    private Hashtable<Integer, Integer> messageHistogram;
    /**
     * The set of data owners that this node is a data requester of them
     */
    private HashSet<Integer> dataRequesterIDSet;
    /**
     * The set of data owner's indices that this node keeps their replica
     */
    private HashSet<Integer> replicaIDSet;
    /**
     * Available Storage Load of a node, if a node has the storage node of 2 it means that it can be the replicas of
     * two data owners.
     */
    private int mStorageCapacity;
    /**
     * Available bandwidth of the node between 0 and 1, used to measure the quality of service of replication
     */
    private double mBandwidthCapacity;
    private boolean testedForALandmark = false;


    /**
     * Set of all transactions that this Node holds
     */
    private HashSet<Integer> txSet = new HashSet<>();

    private AvailabilityPrediction mAvailabilityPredictor;
    /**
     * Determines whether the Node is online or not, used for dynamic simulation
     */
    private boolean isOnline;
    /**
     * The average max probability that a Node in bucket has an yet is offline. Moves as an EMA.
     */

    private int parent;
    private ArrayList<Integer> children;
    private ArrayList<Integer> pathToRoot;
    /**
     * The localNumberOfUpdatesTable of all the SkipGraph.Nodes, localNumberOfUpdates[oneNode][anotherNode] returns number of the times oneNode has updated its
     * local table about the anotherNode
     */
    private double[] localNumberOfUpdatesTable;

    /**
     * Determines whether the node is malicious or not. We assume that a malicious node cannot perform
     * routing attacks, but it can forge transactions and ill-formed views.
     */
    private boolean malicious;

    /**
     * A set of Nodes that share their view with this node.
     */
    private Set<Node> viewIntroducers = new HashSet<>(SkipSimParameters.getValidatorThreshold());;

    /**
     * A list of honest Nodes that are accepted as view introducers
     */
    private List<Node> honestViewIntroducers = new ArrayList<>(SkipSimParameters.getSignatureThreshold());;

    public Node(int nodeIndex)
    {
        super();
        mCoordinate = new Point();
        /*
        Bandwidth capacity is by default set to maximum (i.e., 1) but if the BandwidthCapacityReate parameter of SkipSimParameters
        is defined by a non-zero value, then the bandwidth capacity of each node is assigned randomly by the Nodes class  following
        the exponential distribution.
         */
        mBandwidthCapacity = 1;
        closestLandmark = -1;
        /*
        Storage load is initially -1, and if the replication is dynamic, it is being selected as random by the Nodes class
         */
        mStorageCapacity = -1;
        lookup = new int[SkipSimParameters.getLookupTableSize()][2];
        if (!SkipSimParameters.isStaticSimulation())
        {
            //falsePositiveThreshold = 0;
            bucket = new LinkedList[SkipSimParameters.getLookupTableSize()][2];
            bucketLatencyTable = new Hashtable<>();
            messageHistogram = new Hashtable<>();
            mLastConnectionTime = new Hashtable<>();
            if (SkipSimParameters.isDynamicReplication())
            {
                mAvailabilityVector = new double[SkipSimParameters.getFPTI()];
                mAvailabilityTable = new Hashtable<>();
                for (int i = 0; i < SkipSimParameters.getFPTI(); i++)
                {
                    mAvailabilityVector[i] = 0;
                }
            }
            if (SkipSimParameters.getAvailabilityPredictor().equalsIgnoreCase(Constants.Churn.AvailabilityPredictorAlgorithm.SWDBG))
            {
                mAvailabilityPredictor = new SlidingBruijnGraph();
            }
            else if (SkipSimParameters.getAvailabilityPredictor().equalsIgnoreCase(Constants.Churn.AvailabilityPredictorAlgorithm.DBG))
            {
                mAvailabilityPredictor = new BruijnGraph(SkipSimParameters.getPredictionParameter());
            }
            else if (SkipSimParameters.getAvailabilityPredictor().equalsIgnoreCase(Constants.Churn.AvailabilityPredictorAlgorithm.LIFETIME))
            {
                mAvailabilityPredictor = new LifeTimePredictor();
            }
            else if (SkipSimParameters.getAvailabilityPredictor().equalsIgnoreCase(Constants.Churn.AvailabilityPredictorAlgorithm.LUDP))
            {
                mAvailabilityPredictor = new LUDP();
            }

            if (SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.BLOCKCHAIN))
            {
                txSet = new HashSet<>();
                //blockSet = new HashSet<>();
            }
        }
        arrivalTime = -1;
        departureTime = -1;
        sessionLength = -1;
        dataRequesterIDSet = new HashSet<>();
        replicaIDSet = new HashSet<>();
        isOnline = false;
        malicious = false;
        correspondingReplica = new Hashtable<>();
        //availabilityTable = new double[Simulator.system.getNumIDSeed()][Simulator.system.getTimeSlot()];
        //availabilityCheckTable = new int[Simulator.system.getTimeSlot()];
        //availabilityVector = new double[Simulator.system.getTimeSlot()];
        localNumberOfUpdatesTable = new double[SkipSimParameters.getSystemCapacity()];
//        if (system.getSimulationType().equalsIgnoreCase(Constants.SimulationType.DYNAMIC))
//        {
//            bucket = new LinkedList[system.getLookupTableSize()][2];
//            bucketLatencyTable = new Hashtable<>();
//        }

        parent = -1;
        children = new ArrayList<>();
        pathToRoot = new ArrayList<>();

        //numberOfUpdates = new int[Simulator.system.getSystemCapacity()];

        /*
        Assigning the numerical ID
         */
        if (nodeIndex == 0)
        {
            numID = 0;
        }
        else
        {
            numID = Nodes.getRandomNumID(nodeIndex);
        }

        /*
        Initializing the lookup table
         */
        for (int i = 0; i < SkipSimParameters.getLookupTableSize(); i++)
        {
            for (int j = 0; j < 2; j++)
            {
                lookup[i][j] = -1;
                /*
                Initializing the backup table in Dynamic and Blockchain simulation
                 */
                if (SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.DYNAMIC)
                        || SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.BLOCKCHAIN))
                {
                    bucket[i][j] = new LinkedList<>();
                }
            }
        }

    }

    /**
     * Once a node is introduced into the system (i.e. it goes online), it tries to construct a view of the
     * network. This is done by acquiring SignatureThreshold (T) many "honest view introducers" that share
     * their view of the system with this node.
     * @param sgo SkipGraphOperation object that holds the Nodes skip-graph.
     */
    public void acquireViewIntroducers(SkipGraphOperations sgo) {
        int i = 1;
        // We perform (ideally) ValidatorThreshold (Alpha) many searches over the Nodes skip-graph to acquire
        // Alpha many unique nodes to act as view introducers.
        for(; viewIntroducers.size() < SkipSimParameters.getValidatorThreshold(); i++) {
            if(i > sgo.getTG().getNodeSet().getNumberOfOnlineNodes()) {
//                System.err.println("Node.java: not enough online nodes to choose as view introducers" +
//                        " for node " + getIndex() + " with num. id " + getNumID());
                break;
            }
            // At each search iteration, we perform a search for a node with the numerical
            // id of hash(numId || i).
            String hashInput = "" + getNumID() + i;
            byte[] targetNumIdBytes = HashTools.hash(hashInput);
            int targetNumId = HashTools.compressToInt(targetNumIdBytes);
            // Determine the search direction.
            int searchDirection = (targetNumId < getNumID())
                    ? SkipGraphOperations.LEFT_SEARCH_DIRECTION
                    : SkipGraphOperations.RIGHT_SEARCH_DIRECTION;
            // Perform the search & acquire the node.
            int searchResultIndex = sgo.SearchByNumID(targetNumId,
                    this,
                    new Message(),
                    SkipSimParameters.getLookupTableSize() - 1,
                    0,
                    sgo.getTG().getNodeSet(),
                    searchDirection);
            Node viewIntroducer = (Node) sgo.getTG().getNodeSet().getNode(searchResultIndex);
            // Put the view introducer into the set.
            viewIntroducers.add(viewIntroducer);
        }

        // In case we have acquired repetitive results from our searches, we may have need to perform
        // extra searches.
        int extraSearches = i - SkipSimParameters.getValidatorThreshold();
        if(extraSearches > 0) {
//            System.err.println("Node.java: " + extraSearches + " many extra searches are performed during " +
//                    "randomized bootstrapping for node " + getIndex() + " with num. id " + getNumID());
        }
        // Iterate through acquired view introducers and put the honest ones in a separate list. We need
        // SignatureThreshold (T) many honest introducers.
        for(Node viewIntroducer : viewIntroducers) {
            if(honestViewIntroducers.size() == SkipSimParameters.getSignatureThreshold())
                break;
            if(!viewIntroducer.isMalicious()) {
                honestViewIntroducers.add(viewIntroducer);
            }
        }
    }

    /**
     * Returns the set of unique view introducers that this node has acquired.
     * @return the set of unique view introducers.
     */
    public Set<Node> getViewIntroducers() {
        return viewIntroducers;
    }

    /**
     * Returns the list of honest view introducers that this node was able to acquire.
     * @return the list of honest view introducers.
     */
    public List<Node> getHonestViewIntroducers() { return honestViewIntroducers; }

    /**
     * Computes the latency between this and other nodes as their Euclidean distance in Coordination
     *
     * @param other the other node, which we are interested to measure latency with respect to this node
     * @return latency in milliseconds
     */
    public double latencyTo(Node other)
    {
        return this.getCoordinate().distance(other.getCoordinate());
    }

    public AvailabilityPrediction getAvailabilityPredictor()
    {
        return mAvailabilityPredictor;
    }

    /**
     * Returns whether the online is or not. In a static simulation (no churn) every node is always online.
     * @return True if the nodes is online, False otherwise.
     */
    public boolean isOnline()
    {
        if(SkipSimParameters.isStaticSimulation())
        {
            return true;
        }
        if (!SkipSimParameters.getSimulationType().equals(Constants.SimulationType.DYNAMIC) && !SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.BLOCKCHAIN))
        {
            return true;
        }
        if (!isOnline)
        {
            ChurnStochastics.updateAverageResolveFailureTimeOuts();
        }
        return isOnline;
    }

    /**
     * Returns whether the node is malicious or not.
     * @return true if malicious, false if honest.
     */
    public boolean isMalicious() {
        return malicious;
    }

    /**
     * Returns whether the node is offline or not.
     * @return true if offline, false if online.
     */
    public boolean isOffline()
    {
        if (!SkipSimParameters.getSimulationType().equals(Constants.SimulationType.DYNAMIC) && !SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.BLOCKCHAIN))
        {
            return false;
        }
        if (!isOnline)
        {
            ChurnStochastics.updateAverageResolveFailureTimeOuts();
        }
        return !isOnline;
    }

    public void setOnline()
    {
        isOnline = true;
    }

    public void setOffline()
    {
        isOnline = false;
    }

    public void setMalicious(boolean malicious) {
        this.malicious = malicious;
    }

    public boolean isTestedForALandmark()
    {
        return testedForALandmark;
    }

    public void setTestedForALandmark(boolean testedForALandmark)
    {
        this.testedForALandmark = testedForALandmark;
    }

    /**
     * @param landmarks set of all landmarks in the system
     * @return index of the closest landmark to the node based on the Euclidean coordinate distance
     */
    public int getClosetLandmarkIndex(Landmarks landmarks)
    {
        //Todo move the closest landmark function to topology generator
        if (closestLandmark > -1)
        {
            /*
            This function has been executed once for this node, and
            the closest landmark has been cached.
             */
            return closestLandmark;
        }
        double min = Double.MAX_VALUE;
        int closestLandmarkIndex = 0;
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            if (getCoordinate().distance(landmarks.getLandmarkCoordination(i)) < min)
            {
                min = getCoordinate().distance(landmarks.getLandmarkCoordination(i));
                closestLandmarkIndex = i;
            }
        }
        this.closestLandmark = closestLandmarkIndex;
        return closestLandmarkIndex;

    }


    public void setClosetLandmarkIndex(int closetLandmarkIndex)
    {
        this.closestLandmark = closetLandmarkIndex;
    }

    public boolean ancestorCheck(int index)
    {
        return pathToRoot.contains(index);
    }

    public ArrayList<Integer> getPathToRoot()
    {
        return pathToRoot;
    }

    public void clonePathToRoot(int source, ArrayList<Integer> list)
    {
        pathToRoot = list;
        pathToRoot.add(source);
    }

    public int getParent()
    {
        return parent;
    }

    public void setParent(int parent)
    {
        this.parent = parent;
    }

    public ArrayList<Integer> getChildren()
    {
        return children;
    }

    public void removeChild(int c)
    {
        children.removeAll(Collections.singleton(c));
    }

    public int addChildren(int i)
    {
        if (i >= 0 && i < SkipSimParameters.getSystemCapacity())
        {
            children.add(i);
        }
        else
        {
            return -1;
        }
        return 0;
    }

    public double getLocalNumberOfUpdatesTable(int index)
    {
        return localNumberOfUpdatesTable[index];
    }

//    public double getAvailabilityTable(int destNumId, int time)
//    {
//        return availabilityTable[destNumId][time];
//    }
//
//    public double getAvailabilityVector(int time)
//    {
//        return availabilityVector[time];
//    }

    //TODO make it more efficient in running time
//    public void updateAvailabilityTable(boolean[] piggy)
//    {
//        for (int i = 0; i < system.getSystemCapacity(); i++)
//            if (piggy[i] && i != index)
//            {
//                localNumberOfUpdatesTable[i]++;
//                for (int t = 0; t < system.getTimeSlot(); t++)
//                {
//                    availabilityTable[i][t] = getAvailabilityProbability(i, t);
//                }
//            }
//    }

//TODO from Awake's implementation
//    public double getAvailabilityProbability(int id, int time)
//    {
//        if (checkTable[time] > 0)
//        {
//            double numnator = availabilityTable[id][time];
//            int denuminator = checkTable[time];
//            double prob = (double) numnator / denuminator;
//            if (prob >= 0 && prob <= 1)
//            {
//                return prob;
//            }
//            else
//            {
//                System.err.println("Nodes: getAvailabilityProbability problem, probablity is not between 0 and one!");
//            }
//            System.exit(0);
//            return 0;
//        }
//        else
//        {
//            return 0;
//        }
//    }


    //TODO needs further computational revisions
//    public void updateAvailabilityVector(int time)
//    {
//        double alphaValue = 0.5;
//        if (isOnline)
//        {
//            availabilityVector[time % system.getTimeSlot()] += time;
//        }
//        else
//        {
//            availabilityVector[time % system.getTimeSlot()] = ((1 - alphaValue) * availabilityVector[time % system.getTimeSlot()]);
//        }
//
//        availabilityCheckTable[time % system.getTimeSlot()] += time;
//
//    }


    public HashSet<Integer> getDataRequesterIDSet()
    {
        return dataRequesterIDSet;
    }

    public void setDataRequesterID(int dataOwnerIndex)
    {
        dataRequesterIDSet.add(dataOwnerIndex);
    }

    public boolean isReplica(int dataOwnerIndex)
    {
        return replicaIDSet.contains(dataOwnerIndex);
    }

    /**
     * @return TRUE if this node holds at least one replication duty, FALSE otherwise
     */
    public boolean isReplica()
    {
        return !(replicaIDSet.isEmpty());
    }

    /**
     * @return number of data owners this node is their replica
     */
    public int getReplicatedLoad()
    {
        if(SkipSimParameters.isHeterogeneous())
        {
            return replicaIDSet.size();
        }
        else
        {
            return 0;
        }
    }

    /**
     * Assings this node as a replica of the dataOwnerIndex in case that the node have enough storage load
     *
     * @param dataOwnerIndex the index of the data owner that aims on placing a replica on this node
     * @return TRUE if a replica can be placed on this node by the data owner, FALSE otherwise, this function
     * also returns false if this node has already been assigned as the replica for this data owner
     */
    public boolean setAsReplica(int dataOwnerIndex)
    {
        if (replicaIDSet == null)
        {
            replicaIDSet = new HashSet<>();
        }

        if (replicaIDSet.contains(dataOwnerIndex))
        {
            /*
            If this node has already been assigned as a replica for this data owner, to avoid duplicated replication
            of the same data owner on the same node returns false
             */
            return false;
        }
        if(SkipSimParameters.isHeterogeneous())
        {
            if (mStorageCapacity > -1)
            {
            /*
            There is a load constraint on the number of replicas a node can take
             */
                if (replicaIDSet.size() >= mStorageCapacity)
                {
                    //System.out.println("Node.java: index " + index + " replication load " + replicaIDSet.size() + " maximum load " +  SkipSimParameters.getStorageCapacity());
                /*
                This node used its storage load and cannot be employed as a replica
                 */
                    return false;
                }
            }
        }
        if (Main.isReplica != null)
            Main.isReplica[index] = true;
        replicaIDSet.add(dataOwnerIndex);
        //System.out.println("Node.java: index " + index + " replication load " + replicaIDSet.size() + " maximum load " +  SkipSimParameters.getStorageCapacity());
        return true;
    }


    /**
     * Given the data owner index, this function returns the index of the corresponding replica for that data owner, or
     * -1 if there is no record of corresponding replica for that data owner
     *
     * @param dataOwnerIndex the data owner index
     * @return the corresponding replica index or -1
     */
    public int getCorrespondingReplica(int dataOwnerIndex)
    {
        if (correspondingReplica.containsKey(dataOwnerIndex))
            return correspondingReplica.get(dataOwnerIndex);
        else
            return -1;
    }

    /**
     * Sets the corresponding replica of this node for the dataOwnerIndex as the one that is denoted by the replicaIndex
     * i.e., the node needs to contact the replicaIndex instead of contacting the dataowner index directly
     *
     * @param dataOwnerIndex
     * @param replicaIndex
     */
    public void setCorrespondingReplica(int dataOwnerIndex, int replicaIndex)
    {
        correspondingReplica.put(dataOwnerIndex, replicaIndex);
    }

    /**
     * Clears the hash table of correspondingReplica information, by removing all the existing elements
     */
    public void clearCorrespondingReplicaData()
    {
        correspondingReplica.clear();
    }

    public double getDepartureTime()
    {
        return departureTime;
    }

    public double getSessionLength()
    {
        return sessionLength;
    }

    public void setSessionLength(double t, int currentTime)
    {
        if (t >= 0)
        {
            sessionLength = t;
            departureTime = currentTime + sessionLength;
        }

        else
        {
            System.out.println("Wrong value for departure time  ");
            System.exit(0);
        }

    }


    public int getArrivalTime()
    {
        return arrivalTime;
    }



    public int neighborNumber()
    {
        ArrayList<Integer> neighbors = new ArrayList<Integer>();
        for (int i = 0; i < SkipSimParameters.getLookupTableSize(); i++)
            for (int j = 0; j < 2; j++)
            {
                if (lookup[i][j] != -1 && !neighbors.contains(lookup[i][j]))
                {
                    neighbors.add(lookup[i][j]);
                }
            }

        return neighbors.size();
    }


    public void resetChildSet()
    {
        children = new ArrayList<>();
    }


    public void resetPathToRoot()
    {
        pathToRoot = new ArrayList<>();
    }


    /**
     * Prints the arrival info of a Node once it joins the system
     */
    public void printAvailabilityInfo(int currentTime, String message)
    {
        System.out
                .println("--------------------------" + "\n" + message + "\n  Current time: " + currentTime + "\n index is " + index + "\n introducer " + getIntroducer() + "\n name id is " + nameID + "\n numerical id is " + getNumID() + "\n session length = " + getSessionLength() + "\n departure time = " + getDepartureTime() + "\n online status = " + isOnline() + "--------------------------");
    }


    public LinkedList<BucketItem> getBucket(final int level, int direction)
    {
        return bucket[level][direction];
    }

    public void setBucket(final int level, final int direction, final LinkedList<BucketItem> b)
    {
        bucket[level][direction] = b;
    }

    public void updateAvailabilityState(int currentTime)
    {
        if (mAvailabilityPredictor != null)
        {
            mAvailabilityPredictor.updateState(isOnline, currentTime);
        }

        /*
        Updates the availability vector of the Node
         */
        if (SkipSimParameters.isDynamicReplication())
        {
            if (SkipSimParameters.getFPTI() > 0)
            {
                int visitCounter = currentTime / SkipSimParameters.getFPTI(); //number of times each timeSlot visited
                int timeSlotIndex = currentTime % SkipSimParameters.getFPTI();
                double currentProb = mAvailabilityVector[timeSlotIndex] * visitCounter;
                if (isOnline) currentProb += 1;
                else currentProb += 0;
                mAvailabilityVector[timeSlotIndex] = currentProb / (visitCounter + 1);
//                if(this.index == 1)
//                {
//                    System.out.println(Arrays.toString(mAvailabilityVector));
//                }
            }
        }
    }

    public double getAvailabilityProbability()
    {
        if (mAvailabilityPredictor != null)
        {
            return mAvailabilityPredictor.getAvailabilityProbability();
        }
        else
        {
            return 0;
        }
    }


    public LinkedList bucketToLinkedList()
    {
        LinkedList<BucketItem> b = new LinkedList<>();
        for (int i = 0; i < SkipSimParameters.getLookupTableSize(); i++)
            for (int j = 0; j < 2; j++)
            {
                if (this.getBucket(i, j).size() > 0)
                {
                    LinkedList<BucketItem> bucekt = this.getBucket(i, j);
                    for (BucketItem e : bucekt)
                    {
                        b.add(e);
                    }
                }
            }
        return b;
    }

    public ArrayList<BucketItem> bucketToArrayList()
    {
        ArrayList<BucketItem> b = new ArrayList<>();
        b.addAll(bucketToLinkedList());
        return b;
    }

    public int removeFromBucket(int nodeIndex)
    {
        int bucketSize = 0;
        BucketItem toBeRemovedElement = null;
        for (int i = 0; i < SkipSimParameters.getLookupTableSize(); i++)
            for (int j = 0; j < 2; j++)
            {
                if (this.getBucket(i, j).size() > 0)
                {
                    for (BucketItem e : getBucket(i, j))
                    {
                        if (e.getNodeIndex() == nodeIndex)
                        {
                            if (toBeRemovedElement != null)
                            {
                                System.err.println("Node.java/removeFromBucket, Duplicative to be removed element.");
                                System.exit(0);
                            }
                            toBeRemovedElement = e;
                        }
                        else
                        {
                            bucketSize++;
                        }
                    }
                    if (toBeRemovedElement != null)
                    {
                        getBucket(i, j).remove(toBeRemovedElement);
                    }
                }
            }

        return bucketSize;
    }

    public void LatencyTableUpdate(int currentTime)
    {
        List<Integer> toBeRemovedElements = new ArrayList<>();
        Iterator iterator = bucketLatencyTable.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) iterator.next();
            if (mLastConnectionTime.get(entry.getKey()) < currentTime)
            {
                double updatedLatencyExperience = (double) bucketLatencyTable.get(entry.getKey()) * Math.exp(-(currentTime - mLastConnectionTime.get(entry.getKey())));

                //double updatedLatencyExperience = (double) bucketLatencyTable.get(entry.getKey()) * 0.5;
                if (updatedLatencyExperience < 1)
                {
                    updatedLatencyExperience = 0;
                    toBeRemovedElements.add(entry.getKey());
                }
                bucketLatencyTable.put(entry.getKey(), updatedLatencyExperience);
                mLastConnectionTime.put(entry.getKey(), currentTime);

            }
        }

        for (Integer e : toBeRemovedElements)
        {
            bucketLatencyTable.remove(e);
        }


    }

    public void bucketProbUpdate(int nodeIndex, boolean isOnline, double latency, int currentTime)
    {
        //TODO simplify this function
        //BucketItem toBeRemovedElement = null;
        //for (int i = 0; i < system.getLookupTableSize(); i++)
        //   for (int j = 0; j < 2; j++)
        //   {
        //       if (this.getBucket(i, j).size() > 0)
        //       {
        //           for (BucketItem e : getBucket(i, j))
        //           {
        //               if (e.getNodeIndex() == nodeIndex)
        //              {
        if (isOnline)
        {
            //e.setOnlineProbability((0.8 * e.getOnlineProbability()) + 0.2);
            if (bucketLatencyTable.containsKey(nodeIndex))
            {
                //double updatedLatencyExperience = (double) bucketLatencyTable.get(nodeIndex) / ((currentTime+1) * latency);
                double updatedLatencyExperience = (double) bucketLatencyTable.get(nodeIndex) - latency;
                if (updatedLatencyExperience < 1)
                    updatedLatencyExperience = 0;
                bucketLatencyTable.put(nodeIndex, updatedLatencyExperience);
                mLastConnectionTime.put(nodeIndex, currentTime);

            }
        }
        else
        {
            if (bucketLatencyTable.containsKey(nodeIndex))
            {
                //bucketLatencyTable.put(e.getNodeIndex(), bucketLatencyTable.get(nodeIndex) + latency * (currentTime + 1));
                bucketLatencyTable.put(nodeIndex, bucketLatencyTable.get(nodeIndex) + latency);
                mLastConnectionTime.put(nodeIndex, currentTime);
            }
            else
            {
                //bucketLatencyTable.put(e.getNodeIndex(),  latency * (currentTime + 1));
                bucketLatencyTable.put(nodeIndex, latency);
                mLastConnectionTime.put(nodeIndex, currentTime);
            }

            //falsePositiveThreshold = (0.8 * falsePositiveThreshold) + (0.2 * e.getOnlineProbability());
            //e.setOnlineProbability(0.8 * e.getOnlineProbability());
        }
//                            if (e.getOnlineProbability() <= BruijnGraph.MINIMUM_PROB)
//                            {
//                                getBucket(i, j).remove(toBeRemovedElement);
//                            }
        //break;

        //                       }
        //                   }
        //               }
        //           }

    }

    public int removeLowestAvailabile(Nodes ns)
    {
        int bucketSize = 0;
        double minAvaialbityProb = Double.MAX_VALUE;
        int minLevel = 0;
        int minDirection = 0;
        BucketItem toBeRemovedElement = null;
        for (int i = 0; i < SkipSimParameters.getLookupTableSize(); i++)
            for (int j = 0; j < 2; j++)
            {
                if (this.getBucket(i, j).size() > 0)
                {
                    for (BucketItem e : getBucket(i, j))
                    {
                        if (e.getOnlineProbability() < minAvaialbityProb)
                        {
                            toBeRemovedElement = e;
                            minLevel = i;
                            minDirection = j;
                            minAvaialbityProb = e.getOnlineProbability();
                        }
                        bucketSize++;
                    }


                }
            }
        getBucket(minLevel, minDirection).remove(toBeRemovedElement);
        bucketSize--;
        return bucketSize;
    }

    public boolean bucketContains(int inputIndex)
    {
        for (int i = 0; i < SkipSimParameters.getLookupTableSize(); i++)
            for (int j = 0; j < 2; j++)
            {
                if (this.getBucket(i, j).size() > 0)
                {
                    for (BucketItem e : getBucket(i, j))
                    {
                        if (e.getNodeIndex() == inputIndex)
                        {

                            return true;
                        }
                    }


                }
            }
        return false;
    }

    /**
     * @param inputIndex address of the Node to be checked
     * @return true of lookuptable contains the Node at address nodeIndex, false otherwise
     */
    public boolean lookupContains(int inputIndex)
    {
        for (int i = 0; i < SkipSimParameters.getLookupTableSize(); i++)
            for (int j = 0; j < 2; j++)
            {
                if (lookup[i][j] == inputIndex)
                {
                    return true;
                }
            }
        return false;
    }

    public Hashtable<Integer, Double> getBucketLatencyTable()
    {
        return bucketLatencyTable;
    }

    //public int getLastFailed(int i, int j)
//    {


    public Hashtable<Integer, Integer> getMessageHistogram()
    {
        return messageHistogram;
    }

    public void increaseHistogram(int index)
    {
        int oldHistogram = 0;
        if (messageHistogram.containsKey(index))
        {
            oldHistogram = messageHistogram.get(index);

        }

        oldHistogram++;
//        if (oldHistogram > 1)
//            System.out.println(index);

        messageHistogram.put(index, oldHistogram);
    }

    public void addToTXSet(int index)
    {
        this.txSet.add(index);
    }

    public HashSet<Integer> getTxSet()
    {
        return txSet;
    }

//    public HashSet<Integer> getBlockSet()
//    {
//        return blockSet;
//    }
//
//    public void addToBlockSet(int index)
//    {
//        this.blockSet.add(index);
//    }


    public double[] getAvailabilityVector()
    {
        return mAvailabilityVector.clone();
    }

    /**
     * @param timeSlot    the timeslot index on the availability vector
     * @param probability the availability probability that is between 0 and 100
     */
    public void setAvailabilityVector(int timeSlot, int probability)
    {
        mAvailabilityVector[timeSlot] = probability;
    }


    /**
     * @param index              index of the neighbor Node
     * @param availabilityVector availability vector of the neighbor Node
     */
    public void setAvailabilityTable(int index, double[] availabilityVector)
    {
        mAvailabilityTable.put(index, availabilityVector);
    }

    public Hashtable<Integer, double[]> getAvailabilityTable()
    {
        return mAvailabilityTable;
    }

    public void clearReplicaIDSet()
    {
        replicaIDSet = new HashSet<>();
    }

    public void clearDataRequesterIDSet()
    {
        dataRequesterIDSet = new HashSet<>();
    }


    /**
     * given a Block index returns the index of the Block among the blocks that the Node owns with the largest numerical ID
     * that is less than or equal to the numerical ID of the Block determined by the Block index
     * <p>
     * Based on what is perceived form the predecessor function, the initial direction of search is defined.
     *
     * @param blockSet
     * @param targetNumID
     * @param m
     * @return
     */
    public int mostSimilarTXB(SkipGraphNodes blockSet, int targetNumID, Message m, int searchDirection, int searchLevel)
    {
        int minDistance = m.getNumIDistanceToTarget();
        int minIndex = -1;
        HashSet<Integer> set = txSet;
//        if (blockSet instanceof Transactions)
//            set = this.txSet;
//        else
//            set = this.blockSet;

//        System.out.println("---------------------------------------------------");
//        System.out.println("Most similar TXB for target num ID of " + targetNumID);
//        System.out.println("Owner Node index " + this.getIndex());
//        System.out.println("Search direction " + ((searchDirection == 1)?"Right":"Left"));
        for (int txb : set)
        {
            if ((blockSet.getNode(txb)).isLookupTableEmpty(Transaction.LOOKUP_TABLE_SIZE) && blockSet.getNode(txb).index > 0)
            {
                continue;
            }
            int newDistance = targetNumID - blockSet.getNode(txb).getNumID();
//            System.out.println("TX index " + txb + " num ID " + blockSet.getNode(txb).getNumID() + " distance " + newDistance);
            if (searchDirection == SkipGraphOperations.LEFT_SEARCH_DIRECTION)
                newDistance *= -1;
            if (newDistance <= minDistance && newDistance >= 0)
            {
                minDistance = newDistance;
                minIndex = txb;
            }
            //System.out.println(txb + " " + minDistance + " " + newDistance);
        }
        if (minDistance < 0)
        {
            System.err.println("Node.java: new distance negative");
            System.exit(0);
        }
        m.setNumIDistanceToTarget(minDistance);

        /*
        In case there is more than one most similar block, the left most one in the left direction of search or the right most one in the right
        direction of the search is returned to avoid lookup table violation upon insertion
         */
        if (minIndex != -1)
        {
            Transaction mostSimilarTXB = (Transaction) blockSet.getNode(minIndex);
            int neighborInSearchDirection = mostSimilarTXB.getLookup(searchLevel, searchDirection);
            while (neighborInSearchDirection != -1 && blockSet.getNode(neighborInSearchDirection).getNumID() == mostSimilarTXB.getNumID())
            {
                minIndex = neighborInSearchDirection;
                mostSimilarTXB = (Transaction) blockSet.getNode(minIndex);
                neighborInSearchDirection = mostSimilarTXB.getLookup(searchLevel, searchDirection);
            }
        }
//        System.out.println("Min distance numerical id index " + minIndex);
//        System.out.println("---------------------------------------------------");
        return minIndex;
    }

    /**
     * IMPORTANT NOTICE: output of this function should always be checked against -1.
     * Returns the index of the transaction/block that is OWNED by THIS Node, AND has most similar name ID to the search
     * target. Note that the similarity should be also greater than the current level of the search. By similarity we mean
     * the common prefix length between the search target and transaction/block
     *
     * @param nodeSet      an instance of the Transactions (i.e., local database of the transactions)
     * @param targetNameID the target name ID of the search
     * @param m            the search Message
     * @param searchLevel  the current level of the search
     * @return index of the transaction/block with more similarity than both the target and current level of the search.
     */
    public int mostSimilarTXB(SkipGraphNodes nodeSet, String targetNameID, Message m, int searchLevel)
    {
        /*
        Initialize the maximum similarity by extracting the max similarity of name ID to target from the Message
        max similarity means the longest common prefix length of the blocks/transactions of the Nodes on the search path
        to the search target.
         */
        int maxSimilarity = m.getNameIDSimilarityToTarget();

        /*
        Index of the block/transaction with maximum similarity
         */
        int maxIndex = -1;

        HashSet<Integer> set = txSet;
//        if (nodeSet instanceof Transactions)
//            set = this.txSet;
//        else
//            set = this.blockSet;
        /*
        Checks the existence of a block/transaction with more similarity than the existing one on the search Message,
        if found such updates the maxIndex and maxSimilarity accordingly
        IMPORTANT: the maxSimilarity to be updated MUST be greater than the current search level, this is because of the usage of
        this function that is used to check for the jump up during the search for name ID.
         */
        for (int txIndex : set)
        {
            /*
            common prefix length between the target name ID and name ID of the block/transaction denoted by txIndex.
             */
            int similarity = Transactions.commonPrefixLength(targetNameID, nodeSet.getNode(txIndex).getNameID());


            if (similarity >= maxSimilarity && similarity > searchLevel)
            {
                maxSimilarity = similarity;
                maxIndex = txIndex;
            }
        }

        if (maxIndex != -1)
            m.setNameIDSimilarityToTarget(maxSimilarity);
        return maxIndex;
    }

    /**
     * Returns the index of transaction/block with txNumID upon this Node owns, otherwise, returns -1
     * IMPORTANT: the transaction/block with txNumID to be returned MUST have already been inserted in
     * the Skip Graph overly i.e., its lookuptable is non-empty, otherwise, it returns -1.
     *
     * @param nodeSet the set of all transactions or blocks
     * @param txNumID numerical ID of the block or transaction that its ownership is being evaluated
     * @return index of the block or transaction from Transactions or blocks dataset that corresponds to txNumID as its numerical ID
     * if this Node owns the block or transaction, if this Skip Graph Node does not own such transaction or block with the specified
     * numerical ID of txNumID, it returns -1.
     */
    public int containsTXB(SkipGraphNodes nodeSet, int txNumID)
    {
        HashSet<Integer> set = txSet;
//        if (nodeSet instanceof Transactions)
//            set = this.txSet;
//        else
//            set = this.blockSet;
        for (int txIndex : set)
        {
            if (nodeSet.getNode(txIndex).getNumID() == txNumID && !nodeSet.getNode(txIndex).isLookupTableEmpty(Transaction.LOOKUP_TABLE_SIZE))
                return txIndex;
        }

        return -1;
    }


    /**
     * Chooses a random transaction or block index from the set of blocks that the Node owns, and returns its address
     *
     * @param random a random generator instance
     * @return index of a randomly chosen block or transaction that the Node owns, or -1 if the Node does not own any
     */
    public int chooseRandomTXB(Random random)
    {
        int size = txSet.size();
        if (size == 0)
        {
            return -1;
        }
        int item = random.nextInt(size);
        int i = 0;
        for (int txIndex : txSet)
        {
            if (i == item)
                return txIndex;
            i++;
        }
        return -1;
    }

    public Point getCoordinate()
    {
        return mCoordinate;
    }

    public void setCoordinate(Point coordinate)
    {
        mCoordinate = coordinate;
    }

    public int getStorageCapacity()
    {
        return mStorageCapacity;
    }

    public void setStorageCapacity(int storageCapacity)
    {
        mStorageCapacity = storageCapacity;
    }

    public double getBandwidthCapacity()
    {
        if(SkipSimParameters.isHeterogeneous())
        {
            return mBandwidthCapacity;
        }
        else
        {
            return 1;
        }
    }

    public void setBandwidthCapacity(double bandwidthCapacity)
    {
        mBandwidthCapacity = bandwidthCapacity;
    }

    /**
     * @return TRUE is the node has storage capacity, FALSE otherwise
     */
    public boolean hasStorageCapacity()
    {
        if(SkipSimParameters.isHeterogeneous() && mStorageCapacity > -1)
        {
            if(replicaIDSet.size() >= mStorageCapacity)
            {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @return the fraction of the available storage capacity that is between 0 and 1
     */
    public double getNormalizedStorageCapacity()
    {
        if(!SkipSimParameters.isHeterogeneous())
        {
            /*
            If we do not want to consider the load of the nodes
             */
            return 1;
        }
        if(mStorageCapacity > 0)
        {
                return 1-(((double) replicaIDSet.size())/mStorageCapacity);
        }
        throw new IllegalStateException("Node.java: getNormalizedStorageCapacity is executed over a non-positive storage capacity");
    }
}

