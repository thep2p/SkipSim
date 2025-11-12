package ChurnStabilization;


import DataTypes.Message;
import Simulator.AlgorithmInvoker;
import Simulator.SkipSimParameters;
import SkipGraph.Node;
import SkipGraph.SkipGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class LookupEvaluation extends SkipGraph.LookupEvaluation
{
    private static final Logger log = LoggerFactory.getLogger(LookupEvaluation.class);
    private static final double beta = ((double) 2 / (SkipSimParameters.getLifeTime() + 1));
    /**
     * A local in memory database of average success rations per each topology, where the average is taken over all
     * the iterations (i.e., topologies)
     */
    private static double[] successRatios = new double[SkipSimParameters.getTopologies()];
    /**
     * A local in memory database of average search times each topology, where the average is taken over all
     * the iterations (i.e., topologies)
     */
    private static double[] searchTimes = new double[SkipSimParameters.getTopologies()];

    /**
     * A local in memory database of average time of the successful searches per each topology, where the average is
     * taken over all the iterations (i.e., topologies)
     */
    private static double[] successfulSearchTime = new double[SkipSimParameters.getTopologies()];

    /**
     * A local in memory database of average time of the failed searches per each topology, where the average is
     * taken over all the iterations (i.e., topologies)
     */
    private static double[] failureSearchTime = new double[SkipSimParameters.getTopologies()];

    /**
     * A buffer variable that keeps the total success ratio of the current topology over different time slots, and
     * is merely used for the sake of computing the average success ratio
     */
    private static double topologySuccessRate = 0;
    // private static double topologySearchTime = 0;
    // private static double topologySuccessfulSearchTime = 0;
    // private static double topologyFailureSearchTime = 0;
    private static int iterationCounter;



    public LookupEvaluation()
    {
        super();
    }

    @Override
    public void flush()
    {
        iterationCounter = 0;
        //topologySuccessfulSearchTime = 0;
        //topologyFailureSearchTime = 0;
        topologySuccessRate = 0;
        //topologySearchTime = 0;
        resetTimes();
    }

    @Override
    public void randomizedLookupTests(SkipGraphOperations sgo, int currentTime, int searchType)
    {
        if(searchType == SkipGraph.LookupEvaluation.SEARCH_FOR_NAME_ID)
        {
            log.error("No search for name ID implemented in the randomized lookup tests");
            System.exit(0);
        }
        double successRatio = randomizedSearchForNumericalIDs(sgo, sgo.getSearchRandomGenerator(), currentTime);
        if (successRatio >= 0)
        {
            iterationCounter++;
            if (topologySuccessRate == 0)
            {
                topologySuccessRate = successRatio;
            }
            else
            {
                topologySuccessRate = beta * successRatio + (1-beta) * topologySuccessRate;
            }
//            System.out.println("ChurnStabilization/LookupEvalution.java: Average success ratio of this topology : " + successRatio
//                    + "\n average success ratio of this simulation: " + topologySuccessRate / iterationCounter
//                    + "\n so far: average search time " + topologySearchTime / iterationCounter
//                    + "\n so far : average successful search time " + topologySuccessfulSearchTime / iterationCounter
//                    + "\n so far: average unsuccessful search time " + topologyFailureSearchTime / iterationCounter);
            log.info("Average success ratio of this topology: {}, simulation: {}, search time: {}, successful: {}, unsuccessful: {}",
                    successRatio, topologySuccessRate, getTopologySearchTime(), getTopologySuccessfulSearchTime(), getTopologyFailureSearchTime());

        }


        if (currentTime == SkipSimParameters.getLifeTime() - 1)
        {
            successRatios[SkipSimParameters.getCurrentTopologyIndex() - 1] = (double) topologySuccessRate;
            searchTimes[SkipSimParameters.getCurrentTopologyIndex() - 1] = (double) getTopologySearchTime();
            successfulSearchTime[SkipSimParameters.getCurrentTopologyIndex() - 1] = (double) getTopologySuccessfulSearchTime();
            failureSearchTime[SkipSimParameters.getCurrentTopologyIndex() - 1] = (double) getTopologyFailureSearchTime();

            log.info("Intermediate results [topology={}]: successRatio={}, searchTime={}, successfulTime={}, unsuccessfulTime={}",
                    SkipSimParameters.getCurrentTopologyIndex(),
                    successRatios[SkipSimParameters.getCurrentTopologyIndex() - 1],
                    searchTimes[SkipSimParameters.getCurrentTopologyIndex() - 1],
                    successfulSearchTime[SkipSimParameters.getCurrentTopologyIndex() - 1],
                    failureSearchTime[SkipSimParameters.getCurrentTopologyIndex() - 1]);
            if (SkipSimParameters.getCurrentTopologyIndex() == SkipSimParameters.getTopologies())
            {
                double averageSuccessRate = 0;
                double averageSearchTime = 0;
                double averageFailureTime = 0;
                double averageSuccessTime = 0;
                double successRatioSD = 0;
                double searchTimeSD = 0;
                double searchSuccessTimeSD = 0;
                double searchFailureTimeSD = 0;
                for (int i = 0; i < SkipSimParameters.getTopologies(); i++)
                {
                    averageSuccessRate += successRatios[i];
                    averageSearchTime += searchTimes[i];
                    averageSuccessTime += successfulSearchTime[i];
                    averageFailureTime += failureSearchTime[i];
                }

                averageSuccessRate /= SkipSimParameters.getTopologies();
                averageSearchTime /= SkipSimParameters.getTopologies();
                averageFailureTime /= SkipSimParameters.getTopologies();
                averageSuccessTime /= SkipSimParameters.getTopologies();

                for (int i = 0; i < SkipSimParameters.getTopologies(); i++)
                {
                    successRatioSD += Math.pow(averageSuccessRate - successRatios[i], 2);
                    searchTimeSD += Math.pow(averageSearchTime - searchTimes[i], 2);
                    searchSuccessTimeSD += Math.pow(averageSuccessTime - successfulSearchTime[i], 2);
                    searchFailureTimeSD += Math.pow(averageFailureTime - failureSearchTime[i], 2);
                }

                /*
                Success ration standard deviation
                 */
                successRatioSD = Math.sqrt(successRatioSD);
                successRatioSD /= SkipSimParameters.getTopologies();

                /*
                Search time standard deviation
                 */
                searchTimeSD = Math.sqrt(searchTimeSD);
                searchTimeSD /= SkipSimParameters.getTopologies();

                /*
                Successful search time standard deviation
                 */
                searchSuccessTimeSD = Math.sqrt(searchSuccessTimeSD);
                searchSuccessTimeSD /= SkipSimParameters.getTopologies();

                /*
                Unsuccessful search time standard deviation
                 */
                searchFailureTimeSD = Math.sqrt(searchFailureTimeSD);
                searchFailureTimeSD /= SkipSimParameters.getTopologies();

                log.info("Final results [algorithm={}]: avgSuccessRatio={} (SD={}), avgSearchTime={} (SD={}), avgSuccessfulTime={} (SD={}), avgFailedTime={} (SD={})",
                        SkipSimParameters.getChurnStabilizationAlgorithm(),
                        averageSuccessRate, successRatioSD,
                        averageSearchTime, searchTimeSD,
                        averageSuccessTime, searchSuccessTimeSD,
                        averageFailureTime, searchFailureTimeSD);
            }
        }
    }

    /**
     * This function performs random search for numerical IDS only for a single time slot and returns the success ratio
     * of the search for that time slot. It additionally updates the average search latency, accompained with the average
     * search latency of the successful and unsuccessful searches.
     * @param random the search random generator, use the getter of searchRandomGenerator of the same class
     * @return -1 if could not perform the search due to the low number of online Nodes, otherwise the success search ratio
     */
    @Override
    protected double randomizedSearchForNumericalIDs(SkipGraphOperations sgo, Random random, int currentTime)
    {
        double averageSearchTime = 0;
        double averageSuccessfulSearchTime = 0;
        double averageFailedSearchTime = 0;
        int numberOfSuccessfulSearches = 0;
        int numberOfFailedSearches = 0;

        int numOfActiveNodes = sgo.getTG().mNodeSet.getNumberOfOnlineNodes();

        if (numOfActiveNodes < 0.01 * SkipSimParameters.getSystemCapacity())
        {
            return -1;
        }

        int iterations = 0;
        while (iterations == 0)
            iterations = random.nextInt(numOfActiveNodes * (numOfActiveNodes - 1) / 2) + 1;
//        if(SkipSimParameters.isDynamicReplication())
//            iterations = (int) Math.sqrt(iterations) + 1;
        //iterations = (int) Math.ceil((double) iterations);
        ChurnStochastics.updateAverageLookups(iterations);
        double counter = 0;
        ArrayList<Integer> onlineNodeOriginalList = sgo.getTG().mNodeSet.getIndicesOfOnlineNodes();
        ArrayList<Integer> onlineNodesSearchSeed = (ArrayList<Integer>) onlineNodeOriginalList.clone();
        //Set<Integer> searchesSet = new HashSet<>();
        for (int i = 0; i < iterations; i++)
        {
            if (onlineNodesSearchSeed.size() <= 2)
            {
                onlineNodesSearchSeed = (ArrayList<Integer>) onlineNodeOriginalList.clone();
                Collections.shuffle(onlineNodesSearchSeed);
            }

            /*
            Picking and removing search target from the seed
             */
            int searchTargetIndex = random.nextInt(onlineNodesSearchSeed.size() - 1);
            int searchTarget = onlineNodesSearchSeed.get(searchTargetIndex);
            onlineNodesSearchSeed.remove(searchTargetIndex);

            /*
            Picking and removing search target from the seed
             */
            int searchInitiatorIndex = random.nextInt(onlineNodesSearchSeed.size() - 1);
            int searchInitiator = onlineNodesSearchSeed.get(searchInitiatorIndex);
            onlineNodesSearchSeed.remove(searchInitiatorIndex);


//            int searchTarget = sgo.getTG().randomlyPickOnline();
//            int initiator = sgo.getTG().randomlyPickOnline();
//            while (initiator == searchTarget || searchesSet.contains(searchTarget) || searchesSet.contains(initiator))
//            {
//                initiator = sgo.getTG().randomlyPickOnline();
//            }
//
//            searchesSet.add(initiator);
//            searchesSet.add(searchTarget);

            Message m = new Message();

            sgo.getTG().mNodeSet.resetTotalTime();
            Node nodeSearchInitiator = (Node) sgo.getTG().mNodeSet.getNode(searchInitiator);

            /*
            Determining the direction of search
             */
            int searchDirection = SkipGraphOperations.LEFT_SEARCH_DIRECTION;
            if(searchTarget >= nodeSearchInitiator.getNumID())
            {
                searchDirection = SkipGraphOperations.RIGHT_SEARCH_DIRECTION;
            }
            int searchResult = sgo.SearchByNumID(sgo.getTG().mNodeSet.getNode(searchTarget).getNumID(),
                    nodeSearchInitiator,
                    m,
                    SkipSimParameters.getLookupTableSize() - 1,
                    currentTime,
                    sgo.getTG().mNodeSet, searchDirection);


            /*
            The following commented code is only for the sake of checking the correctness of search for name ID
             */
//            int nameIDsearchResult = sgo.SearchByNameID(sgo.getTG().mNodeSet.getNode(searchTarget).getNameID(),
//                    nodeSearchInitiator,
//                    sgo.getTG().mNodeSet,
//                    nodeSearchInitiator.getLookup(0, 1),
//                    nodeSearchInitiator.getLookup(0, 0),
//                    0,
//                    new Message(),
//                    new ArrayList<Integer>());




            averageSearchTime += sgo.getTG().mNodeSet.getTotalTime();

            /*
            The Node that is returned as the search result
             */
            Node nodeSearchResult = (Node) sgo.getTG().mNodeSet.getNode(searchResult);
            /*
            The Node corresponding to the search target
             */
            Node nodeSearchTarget = (Node) sgo.getTG().mNodeSet.getNode(searchTarget);

/*
The following commented code is only for the sake of checking the correctness of search for name ID
 */
//            if(searchTarget != nameIDsearchResult)
//            //if(!nodeSearchTarget.getNameID().equalsIgnoreCase(sgo.getTG().mNodeSet.getNode(nameIDsearchResult).getNameID()))
//            {
//                System.err.println("Error in correctness of search for name ID result: "
//                        + sgo.getTG().mNodeSet.getNode(nameIDsearchResult).getNameID() + " " + sgo.getTG().mNodeSet.getNode(nameIDsearchResult).getIndex()
//                        + " target: " + nodeSearchTarget.getNameID() + " " + nodeSearchTarget.getIndex());
//            }

            /*
            Checks if the search has been conducted successfully
             */
            if (nodeSearchResult.getNumID() == nodeSearchTarget.getNumID())
            {
                ChurnStabilization alg = AlgorithmInvoker.churnStabilization();
                //System.out.println(m.getSearchPathSize());
                for (int piggyIndex : m.getPiggyBackedNodes())
                {
                    Node n = (Node) sgo.getTG().mNodeSet.getNode(piggyIndex);
                    //sgo.getTG().mNodeSet.getNode(index).addToBucket(n.getNumID(), n.getIndex(), sgo.getTG().mNodeSet.commonPrefixLength(i, index), currentTime);
                    if(alg != null)
                        alg.insertIntoBucket(n.getIndex(), n.getNumID(), n.getAvailabilityProbability(), piggyIndex, sgo.getTG().mNodeSet);
                }
                counter++;
                ChurnStochastics.updateAverageSuccessTimeOuts();
                averageSuccessfulSearchTime += sgo.getTG().mNodeSet.getTotalTime();
                numberOfSuccessfulSearches++;

            }
            else
            {
                ChurnStochastics.updateAverageFailureTimeOuts();
                averageFailedSearchTime += sgo.getTG().mNodeSet.getTotalTime();
                numberOfFailedSearches++;
            }

            ChurnStochastics.updateNumberOfIntermediateNodesOnPath(m.getSearchPathSize());
        }


        if (iterations < 0)
        {
            log.error("All pairs random Lookup failed");
            System.exit(0);
        }

        //topologySearchTime += (double) averageSearchTime / iterations;
        if (topologySearchTime == 0)
        {
            topologySearchTime = (double) averageSearchTime / iterations;
        }
        else
        {
            topologySearchTime = (topologySearchTime * (1-beta)) + (averageSearchTime * beta / iterations);
        }
        if (numberOfSuccessfulSearches != 0)
        {
            //topologySuccessfulSearchTime += (double) averageSuccessfulSearchTime / numberOfSuccessfulSearches;
            if (topologySuccessfulSearchTime == 0)
            {
                topologySuccessfulSearchTime = (double) averageSuccessfulSearchTime / numberOfSuccessfulSearches;
            }
            else
            {
                topologySuccessfulSearchTime = (topologySuccessfulSearchTime * (1-beta)) + (((double) averageSuccessfulSearchTime / numberOfSuccessfulSearches) * beta);
            }
        }
        if (numberOfFailedSearches != 0)
        {
            //topologyFailureSearchTime += (double) averageFailedSearchTime / numberOfFailedSearches;
            if (topologyFailureSearchTime == 0)
            {
                topologyFailureSearchTime = (double) averageFailedSearchTime / numberOfFailedSearches;
            }
            else
            {
                topologyFailureSearchTime = (topologyFailureSearchTime * (1-beta)) + (((double) averageFailedSearchTime / numberOfFailedSearches) * beta);
            }
        }
        return (double) counter / iterations;

    }


}
