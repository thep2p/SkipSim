package Evaluation;

import DataTypes.Constants;
import Simulator.SkipSimParameters;
import SkipGraph.Node;
import SkipGraph.Nodes;

import static Simulator.SkipSimParameters.REPLICATION_TIME_INTERVAL;

public class ReplicationEvaluation
{
    /**
     * Average number of available replicas per each data owner on time slot i in this current topology
     */
    private static double[] averageAvailableReplicas;

    /**
     * Average number of available replicas per each data owner over the entire simulation time for topology i
     */
    protected static double[] topologyAverageAvailableReplicas;
    protected static double[] loadDataSet = new double[SkipSimParameters.getTopologies()];

    ////////////////////////////LocalityAwareEvaluation////////////////////////////////////////////////////////////////
    /**
     * The delayDataSet[i] represents the average access delay of replicas in topology number i
     */
    private static double[] delayDataSet = new double[SkipSimParameters.getTopologies()];

    /**
     * The qosDataSet[i] represents the average QoS of replicas in topology number i
     */
    private static double[] qosDataSet = new double[SkipSimParameters.getTopologies()];

    /**
     * The nonZeroTimeSlots is used solely to compute the average access delay. The idea is to keep record of the
     * timeslots with non-zero average access delays. We do not need to worry about this in static replication.
     * nonZeroTimeSlots[i] corresponds to the number of timeslots with non-zero average access delay.
     * A zero average access delay at a given timeslot happens when all the replicas are offline
     */
    private static double[] nonZeroTimeSlots = new double[SkipSimParameters.getTopologies()];
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void availabilityEvaluation(int currentTime, Nodes nodes, String algorithmName)
    {
        int lastReplicationTime = SkipSimParameters.getReplicationTime() + (REPLICATION_TIME_INTERVAL * (SkipSimParameters.getDataOwnerNumber() - 1));
        if (topologyAverageAvailableReplicas == null)
        {
            topologyAverageAvailableReplicas = new double[SkipSimParameters.getTopologies()];
        }
        if(averageAvailableReplicas == null)
        {
            averageAvailableReplicas = new double[SkipSimParameters.getLifeTime()];
        }

        //Replication time of the last dataowner
//		int lastReplicationTime = system.getReplicationTime() + (10 * system.getDataOwnerNumber() - 1);
        double[] dataOwnerReplicas = new double[SkipSimParameters.getDataOwnerNumber()];
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            for (int j = 0; j < SkipSimParameters.getDataOwnerNumber(); j++)
            {
                Node node = (Node) nodes.getNode(i);
                if (node.isReplica(j) && node.isOnline())
                {
                    dataOwnerReplicas[j]++;
                }
            }
        }

		/*
		average number of online replicas per each data owner. The average is taken over all the data owners
		 */
        double average = 0;
        for (int i = 0; i < SkipSimParameters.getDataOwnerNumber(); i++)
        {
            average += dataOwnerReplicas[i];
        }

        averageAvailableReplicas[currentTime] = average / SkipSimParameters.getDataOwnerNumber();

		/*
		If we are at the end of the current topology and hence to conclude this topology
		 */
        if (currentTime == SkipSimParameters.getLifeTime() - 1) // && system.getCurrentTopologyIndex() != system.getTopologyNumbers())
        {
            /*
            Taking average online replicas per data owner over all time slots.
            NOTE: We are measuring availability of replicas from the exact replication time
             */
            average = 0;
            for (int t = lastReplicationTime; t < SkipSimParameters.getLifeTime(); t++)
            {
                average += averageAvailableReplicas[t];
            }
            topologyAverageAvailableReplicas[SkipSimParameters.getCurrentTopologyIndex() - 1] = average / (SkipSimParameters.getLifeTime() - lastReplicationTime);

            /*
            if we are at the end of the last topology and hence to conclude the results
             */
            if (SkipSimParameters.getCurrentTopologyIndex() == SkipSimParameters.getTopologies())
            {
                average = 0;
                double sd = 0;
                for (int topologyIndex = 0; topologyIndex < SkipSimParameters.getTopologies(); topologyIndex++)
                {
                    average += topologyAverageAvailableReplicas[topologyIndex];
                }

                average /= SkipSimParameters.getTopologies();

                for (int topologyIndex = 0; topologyIndex < SkipSimParameters.getTopologies(); topologyIndex++)
                {
                    sd += Math.pow(average - topologyAverageAvailableReplicas[topologyIndex], 2);
                }

                sd /= SkipSimParameters.getTopologies();
                sd = Math.sqrt(sd);
                System.out.println("----------------------------------------------------------");
                System.out.println("RepEvaluation.java: Dynamic Replication Evaluation. Replication Algorithm: " + algorithmName);
                System.out.println("Replication time " + SkipSimParameters.getReplicationTime());
                if(SkipSimParameters.getReplicationAlgorithm().equalsIgnoreCase(Constants.Replication.Algorithms.PYRAMID))
                {
                    System.out.println("Aggregation domain size " + SkipSimParameters.getAvailabilityAggregationDomainSize());
                    System.out.println("Search for utility alpha " + SkipSimParameters.getSearchForUtilityAlpha());
                }
                System.out.println("Replication degree " + SkipSimParameters.getReplicationDegree());
                System.out.println("Average availability of replicas: " + average + ", standard deviation: " + sd);
                System.out.println("Note: Average and standard deviation are taken over all topologies");
                System.out.println("----------------------------------------------------------");
            }
        }


    }

    /**
     * This function should be invoked only once for each topology preferably at the end of the simulation time of that
     * topology. This function computes the average load on the replica nodes for each topology. If this function is invoked
     * for the last topology, then it computes and reports the average load for all nodes over all topologies
     *
     * @param nodes the Nodes set of the SkipGraphOperations class
     */
    public static double loadEvaluation(Nodes nodes)
    {
        if (nodes == null)
        {
            return -1;
        }
        if (SkipSimParameters.getCurrentTopologyIndex() == 1)
        {
            /*
			 Initializing for the entire simulation at the beginning of the first topology
			 loadDataSet[i] is supposed to keep the average load of replicas per each topology i over all the
			 simulation time
			 Note that this is the average load of replicas, not all the nodes
			*/
            loadDataSet = new double[SkipSimParameters.getTopologies()];
        }

        int replicaSum = 0;
        int replicaLoadSum = 0;
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            Node node = ((Node) nodes.getNode(i));
            if (node.isReplica())
            {
                replicaSum++;
                replicaLoadSum += node.getReplicatedLoad();
            }
        }

        double averageReplicatedLoad;
        if (replicaLoadSum == 0)
            averageReplicatedLoad = 0;
        else
            averageReplicatedLoad = (double) replicaLoadSum / replicaSum;
        loadDataSet[SkipSimParameters.getCurrentTopologyIndex() - 1] = averageReplicatedLoad;

        /*
        We reached the end of simulation and hence we are done to conclude
         */
        if (SkipSimParameters.getCurrentTopologyIndex() == SkipSimParameters.getTopologies())
        {
            double average = 0;

            for (int i = 0; i < SkipSimParameters.getTopologies(); i++)
            {
                average += loadDataSet[i];
            }

            //System.out.println("Sum of average " + average);
            average = average / SkipSimParameters.getTopologies();
            double sd = SkipSimParameters.getStandardDeviation(loadDataSet, average);

            System.out.println("The average load on a replica is " + average + " with the SD of " + sd);
            return average;
        }
        return averageReplicatedLoad;
    }


    /**
     * This function should only gets called from a Test class, otherwise it throws exception
     *
     * @param index       index of the topology
     * @param load        average load the topology
     * @param callerClass name of the class that calls
     */
    public static void setLoadDataSet(int index, double load, Class callerClass)
    {
        if (loadDataSet == null || loadDataSet.length != SkipSimParameters.getTopologies())
        {
            loadDataSet = new double[SkipSimParameters.getTopologies()];
        }
        if (callerClass.getName().toLowerCase().contains("test"))
        {
            loadDataSet[index] = load;
        }
    }

    /**
     * This function computes and keeps track of the average access delay/QoS of the replication. In the other words it evaluates
     * the locality awareness of the replication. If isQoS is FALSE, it evaluates the average access delay of each data requester
     * with respect to its closest replica. If QoS is TRUE, however, it measures the Quality-of-Service of each replica with
     * respect to its corresponding set of data requesters. By quality of service, we mean the average available bandwidth of
     * the replica per each data requester node over its latency to that data requester.
     * In static simulation scenarios it should be executed at the end of each topology.
     * In dynamic simulation scenarios it should be executed at the end of each time slot.
     * @param nodes The Skip Graph Nodes object instance
     * @param isPublicReplication TRUE if the replication is public, FALSE otherwise
     * @param algName Name of the replication algorithm
     * @param currentTime current time slot in dynamic replications, or -1 is static ones
     * @param isQoS is TRUE if we want to evaluate the QoS, FALSE if we want to evaluate the average access delay
     * @return average access delay/QoS of the current topology (in the current time slot if the simulation is dynamic),
     * and the overal
     * average access delay/Qos of this topology if we are in the last topology
     * (and the last time slot also, int he dynamic replication)
     */
    public static double AverageAccessDelay(Nodes nodes, boolean isPublicReplication, String algName, int currentTime, boolean isQoS)
    {
        int lastReplicationTime = SkipSimParameters.getReplicationTime() + (REPLICATION_TIME_INTERVAL * (SkipSimParameters.getDataOwnerNumber() - 1));
        /*
        Set corresponding replicas for all the nodes with respect to all the data owners.
         */
        for (int dataOwnerIndex = 0; dataOwnerIndex < SkipSimParameters.getDataOwnerNumber(); dataOwnerIndex++)
        {
            nodes.setCorrespondingReplica(dataOwnerIndex);
        }
        /*
        Array holding summation of average access delays for every data owner.
        In case isQoS it TRUE, however, this array keeps the average QoS of every data owner
         */
        double[] delay = new double[SkipSimParameters.getDataOwnerNumber()];
        /*
        Array holding number of nodes which are having a corresponding replica for each data owner
         */
        double[] onlineDataRequesterCounter = new double[SkipSimParameters.getDataOwnerNumber()];

        /*
        Computing the average access delay of each data requester with respect to the closest replicas of each data owner
         */
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            /*
            Retrieving the node from the in-memory database
             */
            Node node = (Node) nodes.getNode(i);
            /*
            Offline data requesters are discarded from evaluation
             */
            if(node.isOffline())
                continue;
            if (isPublicReplication || i < SkipSimParameters.getDataRequesterNumber())
            {
                for (int dataOwnerIndex = 0; dataOwnerIndex < SkipSimParameters.getDataOwnerNumber(); dataOwnerIndex++)
                {
                    int correspondingRepIndex = node.getCorrespondingReplica(dataOwnerIndex);
                    if (correspondingRepIndex != -1)
                    {
                        Node replica = (Node) nodes.getNode(correspondingRepIndex);
                        double QoS;
                        if(isQoS)
                        {
                            /*
                            If we are evaluating the quality of service of replication, then the QoS for each data requester
                            node its share of available bandwidth of its closest replica over its corresponding latency
                            to that replica.
                             */
                            int dataReqNum = nodes.numberOfDataRequesters(replica.getIndex(), dataOwnerIndex, false);
                            /*
                            Measuring the QoS for the node, the + 1 for latency is to avoid the division by zero for the
                            self replicated nodes
                             */
                            //QoS = (double) replica.getBandwidthCapacity() / (dataReqNum * (node.latencyTo(replica) + 1));
                            if(dataReqNum == 0)
                                dataReqNum = 1;
                            QoS = (double) replica.getBandwidthCapacity() / dataReqNum;
                            if(QoS <=0 )
                                throw new IllegalStateException("ReplicationEvaluation.java: Illegal value for QoS achieved:" + QoS);
                            delay[dataOwnerIndex] += QoS;
                        }
                        else
                        {
                            delay[dataOwnerIndex] += node.latencyTo(replica);
                        }
                        onlineDataRequesterCounter[dataOwnerIndex]++;
                    }
                }
            }
            else
            {
                break;
            }
        }

        /*
        Sum of average access delays for all the data owners.
        if QoS is enabled, this is the sum of average QoS for all data owners, that is offered by replication to the
        data requesters.
         */
        double overalDelay = 0;
        /*
        Number of data owners with non-zero average access delay/QoS
        The zero average access delay happens when all the replicas of a data owner are offline at a timeslot
         */
        int nonZeroDelayCount = 0;


        for (int dataOwnerIndex = 0; dataOwnerIndex < SkipSimParameters.getDataOwnerNumber(); dataOwnerIndex++)
        {
            if (onlineDataRequesterCounter[dataOwnerIndex] != 0 && delay[dataOwnerIndex] > 0)
            {
                /*
                Computing the average access delay/QoS of the replication over all the data owners
                 */
                delay[dataOwnerIndex] = (double) delay[dataOwnerIndex] / onlineDataRequesterCounter[dataOwnerIndex];
                overalDelay += delay[dataOwnerIndex];
                nonZeroDelayCount++;
            }

        }
        /*
        Average total access delay for a single data owner, the average is taken over all the data owners
         */
        if (nonZeroDelayCount == 0)
        {
            /*
            The delay for all data owners is zero hence, the overall delay is zero
             */
            if (overalDelay > 0)
            {
                throw new IllegalStateException("ReplicationEvaluation: non-zero overal delay with zero-delay counter value:" + overalDelay);
            }
        }
        else
        {
            overalDelay = (double) overalDelay / nonZeroDelayCount;
        }

        int currentTopologyIndex = SkipSimParameters.getCurrentTopologyIndex() - 1;
        /*
        Recording the average delay/QoS at the current time
         */
        if(isQoS) qosDataSet[currentTopologyIndex] += overalDelay;
        else delayDataSet[currentTopologyIndex] += overalDelay;
        if (overalDelay > 0) nonZeroTimeSlots[currentTopologyIndex]++;

        /*
        Concluding the average access delay/QoS of a topology if we
        are in the last time slot of the current topology, or if the simulation is static (i.e., there is no time there)
         */
        if ((currentTime == SkipSimParameters.getLifeTime() - 1 || SkipSimParameters.isStaticSimulation()))
        {
            if(isQoS) qosDataSet[currentTopologyIndex] /= (SkipSimParameters.getLifeTime() - lastReplicationTime);
            else if(nonZeroTimeSlots[currentTopologyIndex] != 0 )
                delayDataSet[currentTopologyIndex] /= nonZeroTimeSlots[currentTopologyIndex];
        }

        /*
        Concluding this simulation if we are in the last time slot of the last topology
         */
        if (SkipSimParameters.getCurrentTopologyIndex() == SkipSimParameters.getTopologies()
                && !SkipSimParameters.isDelayBasedSimulaton()
                && (SkipSimParameters.isStaticSimulation() || currentTime == SkipSimParameters.getLifeTime() - 1))
        {
                /*
                Using the overalDelay and nonZeroDelayCount with the same functionality as before but resetting to zero
                 */
            overalDelay = 0;
            nonZeroDelayCount = 0;
                /*
                The time that the last data owner makes the replication, it is only applicable in dynamic simulations
                 */
            //int lastReplicationTime = SkipSimParameters.getReplicationTime() + (REPLICATION_TIME_INTERVAL * (SkipSimParameters.getDataOwnerNumber() - 1));

            /*
            Taking the average access delay/QoS over all the topologies
             */
            for (int i = 0; i < SkipSimParameters.getTopologies(); i++)
            {
                if(isQoS)
                {
                    overalDelay += qosDataSet[i];
                    if (qosDataSet[i] > 0)
                        nonZeroDelayCount++;
                }
                else
                {
                    overalDelay += delayDataSet[i];
                    if (delayDataSet[i] > 0)
                        nonZeroDelayCount++;
                }

            }
            overalDelay = (double) overalDelay / nonZeroDelayCount;
            double SD = 0;
            for (int i = 0; i < SkipSimParameters.getTopologies(); i++)
            {
                if(isQoS) SD += Math.pow(qosDataSet[i] - overalDelay, 2);
                else SD += Math.pow(delayDataSet[i] - overalDelay, 2);
            }
            SD = (double) SD / SkipSimParameters.getTopologies();
            SD = Math.sqrt(SD);
            System.out.println("--------------------------------------------------");
            if(isQoS) System.out.println("QoS evaluation of replication:");
            else System.out.println("Locality-aware replication evaluation:");
            System.out.println("Replication Simulation: " + algName.toUpperCase() +
                    " , replication degree = " + SkipSimParameters.getReplicationDegree() +
                    " data requesters number = " + SkipSimParameters.getDataRequesterNumber());
            if(isQoS) System.out.println("Average QoS = " + overalDelay + " KB/s with SD =  " + SD);
            else  System.out.println("Average access delay = " + overalDelay + " with SD =  " + SD);
            System.out.println("--------------------------------------------------");
        }
        return overalDelay;
    }



    public static double getDelayDataSet(int topologyIndex)
    {
        return delayDataSet[topologyIndex];
    }

    public static double getAverageAvailableReplicas(int currentTime)
    {
        return averageAvailableReplicas[currentTime];
    }

    public static double getTopologyAverageAvailableReplicas(int topologyIndex)
    {
        return topologyAverageAvailableReplicas[topologyIndex];
    }

    public static double getQosDataSet(int topologyIndex)
    {
        return qosDataSet[topologyIndex];
    }
}
