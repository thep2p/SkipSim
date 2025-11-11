package Simulator;

import Blockchain.LightChain.Experiments.*;
import Blockchain.LightChain.Transaction;
import ChurnStabilization.ChurnStochastics;
import ChurnStabilization.Interlace;
import ChurnStabilization.LookupEvaluation;
import DataBase.ChurnDBEntry;
import DataTypes.Constants;
import Evaluation.ReplicationEvaluation;
import NameIDAssignment.NameIDAssignment;
import SkipGraph.Node;
import SkipGraph.Nodes;
import SkipGraph.SkipGraphOperations;

import java.util.ArrayList;

import static Simulator.SkipSimParameters.REPLICATION_TIME_INTERVAL;

/**
 * Created by Yahya on 8/23/2016.
 */
public class DynamicSimulation
{
    //    LookupEvaluation le;
    private static double previousArrivalTime; //Keeps record of previous arrival time for churn statistics only when the topology is loaded
    long time;
    SkipGraphOperations sgo;
    LookupEvaluation mSkipGraphLookupEvaluation;
    Blockchain.LightChain.LookupEvaluation mBlockchainLookupEvaluation;

    public DynamicSimulation(SkipGraphOperations sgo)
    {
        mSkipGraphLookupEvaluation = new LookupEvaluation();
        if (SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.BLOCKCHAIN))
        {
            mBlockchainLookupEvaluation = new Blockchain.LightChain.LookupEvaluation();
        }
        if (!AlgorithmInvoker.isNameIDAssignmentDynamic())
        {
            throw new IllegalStateException("Dynamic Simulation Error: Cannot perform dynamic simulation with static name id assignment "
                    + "\n change the name id assignment in config.txt to one of the dynamic algorithms, DPAD is recommended.");
        }
        else
        {
            this.sgo = sgo;
//            le = new  LookupEvaluation();
        }
    }

    public SkipGraphOperations Simulate(boolean generatingTopology, int currentTime, ArrayList<ChurnDBEntry> churnLog)
    {

        // Initialize and mark the malicious nodes at the beginning of the simulation.
        if(currentTime == 0) {
            int maliciousAmount = (int) (SkipSimParameters.getSystemCapacity() * SkipSimParameters.MaliciousFraction);
            for(int i = 0; i < SkipSimParameters.getSystemCapacity(); i++) {
                // Mark the node as malicious if it is among the first maliciousAmount many nodes.
                ((Node)sgo.getTG().getNodeSet().getNode(i)).setMalicious(i < maliciousAmount);
            }
        }

        //if (system.isLog())
        System.out.println("Current time: " + currentTime + " Topology index " + SkipSimParameters.getCurrentTopologyIndex());
        /*
        If session length and arrival times have already been loaded into the sgo
         */
        if (!generatingTopology)
        {
            loadChurnLog(currentTime, churnLog);
        }

        /*
        No Node has been loaded from database and hence the topology should be generated
        */
        else
        {
            churnLog = generateTopology(currentTime, churnLog);
        }

        /*
        Handling the randomized searches if needed for dynamic replication, churn stabilization, and blockchain
         */
        if(SkipSimParameters.RandomizedLookupTests) {
            randomizedSearches(currentTime);
        }

        /*
        If Simulator.system reaches the replication time, the dynamic replication algorithm is called given the replication time
         */
        if (currentTime >= SkipSimParameters.getReplicationTime())
        {
            for (int dataOwner = 0; dataOwner < SkipSimParameters.getDataOwnerNumber(); dataOwner++)
            {
                if (currentTime == SkipSimParameters.getReplicationTime() + REPLICATION_TIME_INTERVAL * dataOwner)
                {
                    System.out.println("Replication for data owner number " + (dataOwner + 1) + " started");
                    long startTime = System.currentTimeMillis();
                    try
                    {
                        new Simulator.AlgorithmInvoker().dynamicReplication(dataOwner, SkipSimParameters.getReplicationDegree(), sgo);
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                        System.exit(0);
                    }

                    long stopTime = System.currentTimeMillis();
                    long elapsedTime = stopTime - startTime;
                    time += elapsedTime;
                    System.out.println("Replication for data owner number " + (dataOwner + 1) + " finished");
                }
            }
        }

        /*
        Replication time of the last dataowner
         */
        int lastReplicationTime;
        /*
        If we are in the dynamic simulation mode, and not the blockchain
         */
        if (SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.DYNAMIC)
                /*
                And if replication time is greater than zero, meaning that we are simulating for replciation, and not for example churn stabilization
                 */
                && SkipSimParameters.getReplicationTime() > 0)
        {
            /*
            The time in which the last data owner replicates
             */
            lastReplicationTime = SkipSimParameters.getReplicationTime() + (REPLICATION_TIME_INTERVAL * (SkipSimParameters.getDataOwnerNumber() - 1));


            /*
            The immediate subsequent timeslot after the last data owner replicates, the evaluation starts
             */
            if (SkipSimParameters.getReplicationTime() != -1 && currentTime > lastReplicationTime)
            {
                /*
                Updating the corresponding replica of every node at every time slot
                 */
                for (int dataOwnerIndex = 0; dataOwnerIndex < SkipSimParameters.getDataOwnerNumber(); dataOwnerIndex++)
                {
                    sgo.getTG().getNodeSet().setCorrespondingReplica(dataOwnerIndex);
                }
                /*
                Availability Awareness of Replication evaluations
                 */

                if (SkipSimParameters.isReplicationAvailabilityAwarenessEvaluation())
                    ReplicationEvaluation.availabilityEvaluation(currentTime, sgo.getTG().getNodeSet(), SkipSimParameters.getReplicationAlgorithm());
                /*
                Average access delay evaluation
                 */
                if (SkipSimParameters.isReplicationLocalityAwarenessEvaluation())
                    ReplicationEvaluation.AverageAccessDelay(sgo.getTG().getNodeSet(),
                            SkipSimParameters.isPublicReplication(),
                            SkipSimParameters.getReplicationAlgorithm().toUpperCase(),
                            currentTime,
                            false);

                /*
                Average QoS evaluation
                 */
                if (SkipSimParameters.isHeterogeneous())
                    ReplicationEvaluation.AverageAccessDelay(sgo.getTG().getNodeSet(),
                            SkipSimParameters.isPublicReplication(),
                            SkipSimParameters.getReplicationAlgorithm().toUpperCase(),
                            currentTime,
                            true);
//                /*
//                Average Load evaluation
//                 */
//                if (SkipSimParameters.isReplicationLoadEvaluation())
//                    ReplicationEvaluation.loadEvaluation(sgo.getTG().getNodeSet());


            }
        }

        /*
        Blockchain
         */

        if (SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.BLOCKCHAIN))
        {

            for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
            {
                Node peer = ((Node) sgo.getTG().mNodeSet.getNode(i));
                if (peer.isOnline())
                {
                    for(int j = 0; j < SkipSimParameters.TXB_RATE; j++) {
                        Transaction tx = new Transaction(0, i);
                        sgo.addTXBtoLedger(tx, currentTime, true);
                        tx.acquireValidators(sgo);
                        // Inform the malicious success experiment that a malicious peer has created a transaction
                        // and acquired validators.
                        if(SkipSimParameters.BlockchainProtocol.equals(Constants.Protocol.LIGHTCHAIN)) {
                            if (SkipSimParameters.MaliciousSuccessExperiment) {
                                MaliciousSuccessExperiment.informAcquisition(peer, tx, currentTime);
                            }
                            if (SkipSimParameters.EfficiencyExperiment) {
                                EfficiencyExperiment.informAcquisition(peer, tx, currentTime);
                            }
                            if (SkipSimParameters.AvailabilityExperiment) {
                                AvailabilityExperiment.registerTransaction(peer, tx, currentTime);
                            }
                        }
                        //System.out.println("Node " + i + "added a new transaction");
                    }
                }
            }
            /*
            Performing experiments.
             */
            if(SkipSimParameters.BlockchainProtocol.equals(Constants.Protocol.LIGHTCHAIN)) {
                if (SkipSimParameters.MaliciousSuccessExperiment) {
                    MaliciousSuccessExperiment.calculateResults(currentTime);
                }
                if (SkipSimParameters.EfficiencyExperiment) {
                    EfficiencyExperiment.calculateResults(currentTime);
                }
                if (SkipSimParameters.AvailabilityExperiment) {
                    AvailabilityExperiment.calculateResults(currentTime);
                }
                if (SkipSimParameters.OnlineProbabilityExperiment) {
                    OnlineProbabilityExperiment.calculateResults(sgo, currentTime);
                }
                if (SkipSimParameters.btsEfficiencyExperiment) {
                    BtsEfficiencyExperiment.calculateResults(currentTime);
                }
                if (SkipSimParameters.btsMaliciousSuccessExperiment) {
                    BtsMaliciousSuccessExperiment.calculateResults(currentTime);
                }
            }
        }

        /*
        Perform the departure of the Nodes that their session length have been terminated at
        the end of the previous time slot.
         */
        sgo.getTG().departureUpdate(currentTime, sgo.getTransactions());

        /*
        Update the average number of online Nodes at the current time slot
         */
        ChurnStochastics.updateTotalAverageOfOnlineNodes(sgo.getTG().mNodeSet.getNumberOfOnlineNodes());

        //System.out.println("Average number of active SkipGraph.Node is started");
        //TODO update the following function based on the new implementation
        //System.out.println("Average number of SkipGraph.Node has been updated");

                    /*
                    Updating the availability vectors of SkipGraph.Nodes
                     */
        //sgo.getTG().mNodeSet.updateAvailabilityVectors();

        /*
        Whatever needs to be done at the end of the last time slot of ALL TOPOLOGIES
         should be placed within the following if statement body
         */
        if (currentTime == SkipSimParameters.getLifeTime() - 1)
        {
            /*
            Initializing the lock of name ID assignments for the next topology
             */
            NameIDAssignment.initialize();
            ChurnStochastics.flush();
            mSkipGraphLookupEvaluation.flush();
            if (SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.BLOCKCHAIN))
            {
                mBlockchainLookupEvaluation.flush();
            }

            if (SkipSimParameters.isReplicationLoadEvaluation())
            {
                /*
                Performing load evaluation of replication
                 */
                ReplicationEvaluation.loadEvaluation(sgo.getTG().mNodeSet);
            }
        }

        /*
        Whatever needs to be done at the end of the last time slot of the LAST TOPOLOGIES
         should be placed within the following if statement body
         */
        if (currentTime == SkipSimParameters.getLifeTime() - 1 && SkipSimParameters.getCurrentTopologyIndex() == SkipSimParameters.getTopologies())
        {
            if (Nodes.getOveralAverageStorageCapacity() > 0)
                System.out.println("DynamicSimulation.java: Overall average storage capacity of the nodes: " + Nodes.getOveralAverageStorageCapacity());
            if (Nodes.getOveralAverageBandwidthCapacity() > 0)
                System.out.println("DynamicSimulation.java: Overall average bandwidth capacity of the nodes: " + Nodes.getOveralAverageBandwidthCapacity());
            if (SkipSimParameters.isLog())
            {
                sgo.getTG().printGeneratorStochastics();
            }
            ChurnStochastics.printChurnStochastics();
            //repEvaluation.finalizingThisTopologyEvaluation();
            //System.out.println("Average number of SkipGraph.Node has been updated");
            //System.out.println("Last SkipGraph.Node came on " + Simulator.system.getLastArrivalTime());
        }

        return sgo;
    }

    /**
     * Checks whether it is needed to conduct randomized searches or not.
     * When replication time is -1 it means that the simulation's concern is totally on lookup evaluation under churn
     * and hence, we only have the lookup evaluation all over the times. Otherwise, when we have a well-defined non-negative
     * replication time, the lookup evaluation is only done for the sake of the availability vectors being updated
     * Also, lookup evaluation is disabled on blockchain simulation.
     *
     * @param currentTime current time of the simulation
     */
    private void randomizedSearches(int currentTime)
    {
        int lastReplicationTime = SkipSimParameters.getReplicationTime() + (REPLICATION_TIME_INTERVAL * (SkipSimParameters.getDataOwnerNumber() - 1));
        if (SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.DYNAMIC)
                && (SkipSimParameters.getReplicationTime() < 0 || currentTime <= lastReplicationTime))
        {
            /*
            Performing a qos aggregation if we are in dynamic simulation and enabled the aggregation by assigning a positive
            domain size and FPTI
            */
            if (SkipSimParameters.getAvailabilityAggregationDomainSize() > 0
                    && SkipSimParameters.getFPTI() > 0
                    && !SkipSimParameters.isStaticSimulation()
                    && SkipSimParameters.getReplicationAlgorithm().equalsIgnoreCase(Constants.Replication.Algorithms.PYRAMID))
            {
                sgo.getTG().getNodeSet().qosAggregation(sgo.getTG().mLandmarks);
            }
            else
            {
                mSkipGraphLookupEvaluation.randomizedLookupTests(sgo, currentTime, SkipGraph.LookupEvaluation.SEARCH_FOR_NUMERICAL_ID);
            }
        }
        else if (SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.BLOCKCHAIN))
        {
            mBlockchainLookupEvaluation.randomizedLookupTests(sgo, currentTime, SkipGraph.LookupEvaluation.SEARCH_FOR_NUMERICAL_ID);
        }
        else
        {
            System.out.println("DynamicSimulation.java: No random lookup/aggregation takes place at time " + currentTime + " because " +
                    "the replication time has been passed.");
        }
    }

    /**
     * Loads the churn log (arrival/departures) into the SkipGraphOperation instance
     *
     * @param currentTime current time of the simulation
     * @param churnLog    the churn log that is associated with the current time
     */
    private void loadChurnLog(int currentTime, ArrayList<ChurnDBEntry> churnLog)
    {
        if (currentTime == 0)
        {
            previousArrivalTime = 0;
        }
        for (ChurnDBEntry entry : churnLog)
        {
            int arrivalNodeIndex = entry.getNodeIndex();
            Node arrivingNode = (Node) sgo.getTG().mNodeSet.getNode(arrivalNodeIndex);
            arrivingNode.setOnline();
            arrivingNode.setSessionLength(entry.getSessionLength(), currentTime);
            ChurnStochastics.updateTotalAverageSessionLength(arrivingNode.getSessionLength());
            /*
            Only assigns name ID if it has not been assigned previously.
             */
            if (arrivingNode.getNameID() == null || arrivingNode.getNameID().length() < 1)
            {
                arrivingNode.setNameID(new String());
                arrivingNode.setNameID(AlgorithmInvoker.dynamicNameIDAssignment(arrivingNode, sgo, arrivalNodeIndex));
            }
            sgo.insert(arrivingNode, sgo.getTG().mNodeSet, arrivalNodeIndex, AlgorithmInvoker.isNameIDAssignmentDynamic(), currentTime);
            if (SkipSimParameters.getChurnType().equalsIgnoreCase(Constants.Churn.Type.ADVERSARIAL)
                    && SkipSimParameters.getChurnStabilizationAlgorithm().equalsIgnoreCase(Constants.Churn.ChurnStabilizationAlgorithm.INTERLLACED))
            {
                Interlace interlace = new Interlace();
                interlace.rebalanceBucket(arrivingNode.getIndex(), sgo.getTG().mNodeSet);
                interlace.pullBucket(arrivingNode.getIndex(), sgo.getTG().mNodeSet);
            }

            /*
            print the arrival info of the arriving Node if the simulation is enabled with isLog
             */
            printArrivalInfo(arrivingNode, currentTime);

            ChurnStochastics.updateTotalAverageInterArrivalTime(entry.getArrivalTime() - previousArrivalTime);
            previousArrivalTime = entry.getArrivalTime();

            // Let the node acquire its view introducers to construct its view.
            if(SkipSimParameters.BlockchainProtocol.equals(Constants.Protocol.LIGHTCHAIN)
                    && SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.BLOCKCHAIN)
                    && SkipSimParameters.RandomizedBootstrapping) {
                arrivingNode.acquireViewIntroducers(sgo);
                if(SkipSimParameters.btsEfficiencyExperiment) {
                    BtsEfficiencyExperiment.informIntroduction(arrivingNode, currentTime);
                }
                if(SkipSimParameters.btsMaliciousSuccessExperiment) {
                    BtsMaliciousSuccessExperiment.informIntroduction(arrivingNode, currentTime);
                }
            }
        }
    }

    private ArrayList<ChurnDBEntry> generateTopology(int currentTime, ArrayList<ChurnDBEntry> churnLog)
    {
        if (currentTime == 0)
        {
            previousArrivalTime = 0;
            System.out.println("DynamicSimulation.java: Generating the topology");
                    /*
                    Generating landmarks
                    */
            sgo.getTG().mLandmarks.generatingLandmarks();
                    /*
                    Generating Nodes
                     */
            sgo.getTG().mNodeSet.generateNodes(false, sgo, currentTime, false);
        }

        /*
        If there exists any arrival at this time
         */
        int arrivalNodeIndex = sgo.getTG().randomlyPickOffline();
        Node arrivingNode = (Node) sgo.getTG().mNodeSet.getNode(arrivalNodeIndex);
        while (sgo.getTG().getNextArrivalTime() >= currentTime && sgo.getTG().getNextArrivalTime() < currentTime + 1)
        {
            if (sgo.getTG().mNodeSet.getNumberOfOfflineNodes() >= 0.01 * SkipSimParameters.getSystemCapacity()) //arrival only if we have enough offline Nodes
            {
                /*
                An arrival of a Node
                 */
                arrivingNode.setOnline();
                arrivingNode.setSessionLength(sgo.getTG().generateSessionLength(), currentTime);
                ChurnStochastics.updateTotalAverageSessionLength(arrivingNode.getSessionLength());
                /*
                Only assigns name ID if it has not been assigned previously.
                 */
                if (arrivingNode.getNameID() == null || arrivingNode.getNameID().length() < 1)
                {
                    arrivingNode.setNameID(new String());
                    arrivingNode.setNameID(AlgorithmInvoker.dynamicNameIDAssignment(arrivingNode, sgo, arrivalNodeIndex));
                }
                sgo.insert(arrivingNode, sgo.getTG().mNodeSet, arrivalNodeIndex, AlgorithmInvoker.isNameIDAssignmentDynamic(), currentTime);
                churnLog.add(new ChurnDBEntry(arrivalNodeIndex, sgo.getTG().getNextArrivalTime(), arrivingNode.getSessionLength()));

                /*
                print the arrival info of the arriving Node if the simulation is enabled with isLog
                 */
                printArrivalInfo(arrivingNode, currentTime);
                ChurnStochastics.updateTotalAverageInterArrivalTime(currentTime - previousArrivalTime);
                previousArrivalTime = currentTime;


                // Let the node acquire its view introducers to construct its view.
                if(SkipSimParameters.BlockchainProtocol.equals(Constants.Protocol.LIGHTCHAIN)
                        && SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.BLOCKCHAIN)
                        && SkipSimParameters.RandomizedBootstrapping) {
                    arrivingNode.acquireViewIntroducers(sgo);
                    if(SkipSimParameters.btsEfficiencyExperiment) {
                        BtsEfficiencyExperiment.informIntroduction(arrivingNode, currentTime);
                    }
                    if(SkipSimParameters.btsMaliciousSuccessExperiment) {
                        BtsMaliciousSuccessExperiment.informIntroduction(arrivingNode, currentTime);
                    }
                }
            }
            arrivalNodeIndex = sgo.getTG().randomlyPickOffline();
            arrivingNode = (Node) sgo.getTG().mNodeSet.getNode(arrivalNodeIndex);
            sgo.getTG().updateNextArrivalTime(arrivingNode);
        }

        //System.out.println("Departure update started");
        //sgo.getTG().departureUpdate(currentTime);
        //System.out.println("Departure update finished");

        System.out.println("Total number of arrivals: " + ChurnStochastics.getTopologyArrivals());
        return churnLog;
    }

    private void printArrivalInfo(Node arrivingNode, int currentTime)
    {
        if (SkipSimParameters.isLog())
        {
            arrivingNode.printAvailabilityInfo(currentTime, Constants.Churn.ARRIVAL);
            System.out.println("lookup table");
            arrivingNode.printLookup();
            System.out.println("--------------------------------");
        }
    }
}


