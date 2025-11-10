//package ChurnStabilization;
//
//import DataTypes.Message;
//import Simulator.AlgorithmInvoker;
//import Simulator.system;
//import ChurnStabilization.ChurnStochastics;
//import SkipGraph.SkipGraphOperations;
//import SkipGraph.Node;
//
//import java.util.*;
//
//public class BlockLookupEvaluation
//{
//    private static double[] successRatios = new double[system.getTopologyNumbers()];
//    // private static double[] searchTimes = new double[system.getTopologyNumbers()];
//    // private static double[] successfulSearchTime = new double[system.getTopologyNumbers()];
//    // private static double[] failureSearchTime = new double[system.getTopologyNumbers()];
//    private static double topologySuccessRate = 0;
//    // private static double topologySearchTime = 0;
//    // private static double topologySuccessfulSearchTime = 0;
//    // private static double topologyFailureSearchTime = 0;
//    private static int iterationCounter;
//
//    public BlockLookupEvaluation()
//    {
//
//    }
//
//    public void lookupSuccessTest(BlockGraphOperations bgo, SkipGraphOperations sgo, int currentTime)
//    {
//        double successRatio = randomizedSearchForNumericalIDs(bgo,sgo, bgo.getSearchRandomGenerator());
//        if (successRatio >= 0)
//        {
//            iterationCounter++;
//
//            if (topologySuccessRate == 0)
//            {
//                topologySuccessRate = successRatio;
//            }
//            else
//            {
//                topologySuccessRate = 0.1 * successRatio + 0.9 * topologySuccessRate;
//            }
//
//            /*
//            System.out.println("ChurnStabilization/LookupEvalution.java: Average success ratio of this topology : " + successRatio
//                    + "\n average success ratio of this simulation: " + topologySuccessRate
//                    + "\n so far: average search time " + topologySearchTime
//                    + "\n so far : average successful search time " + topologySuccessfulSearchTime
//                    + "\n so far: average unsuccessful search time " + topologyFailureSearchTime);
//             */
//
//            System.out.println("ChurnStabilization/BlockLookupEvalution.java: Average success ratio of this topology : " + successRatio
//                    + "\n average success ratio of this simulation: " + topologySuccessRate);
//
//        }
//
//        if (currentTime == system.getLifeTime() - 1)
//        {
//
//            successRatios[system.getCurrentTopologyIndex() - 1] = (double) topologySuccessRate;
//            // searchTimes[system.getCurrentTopologyIndex() - 1] = (double) topologySearchTime;
//            // successfulSearchTime[system.getCurrentTopologyIndex() - 1] = (double) topologySuccessfulSearchTime;
//            // failureSearchTime[system.getCurrentTopologyIndex() - 1] = (double) topologyFailureSearchTime;
//            //DecimalFormat df = new DecimalFormat("###.##");
//            System.out.println("------------------------------------------------------------");
//            System.out.println("Intermediate result for topology " + system.getCurrentTopologyIndex());
//
//            /* System.out.println("ChurnStabilization/LookupEvalution.java: " +
//                    "\n for this topology: average success ratio of this topology: " + successRatios[system.getCurrentTopologyIndex() - 1]
//                    + "\n for this topology: average search time " + searchTimes[system.getCurrentTopologyIndex() - 1]
//                    + "\n for this topology: average successful search time " + successfulSearchTime[system.getCurrentTopologyIndex() - 1]
//                    + "\n for this topology: average unsuccessful search time " + failureSearchTime[system.getCurrentTopologyIndex() - 1]);
//            */
//
//            System.out.println("ChurnStabilization/BlockLookupEvalution.java: " +
//                    "\n for this topology: average success ratio of this topology: " + successRatios[system.getCurrentTopologyIndex() - 1]);
//            System.out.println("------------------------------------------------------------");
//
//            if (system.getCurrentTopologyIndex() == system.getTopologyNumbers())
//            {
//                double averageSuccessRate = 0;
//                // double averageSearchTime = 0;
//                // double averageFailureTime = 0;
//                // double averageSuccessTime = 0;
//                double successRatioSD = 0;
//                // double searchTimeSD = 0;
//                // double searchSuccessTimeSD = 0;
//                // double searchFailureTimeSD = 0;
//                for (int i = 0; i < system.getTopologyNumbers(); i++)
//                {
//                    averageSuccessRate += successRatios[i];
//                    // averageSearchTime += searchTimes[i];
//                    // averageSuccessTime += successfulSearchTime[i];
//                    // averageFailureTime += failureSearchTime[i];
//                }
//
//                averageSuccessRate /= system.getTopologyNumbers();
//                // averageSearchTime /= system.getTopologyNumbers();
//                // averageFailureTime /= system.getTopologyNumbers();
//                // averageSuccessTime /= system.getTopologyNumbers();
//
//                for (int i = 0; i < system.getTopologyNumbers(); i++)
//                {
//                    successRatioSD += Math.pow(averageSuccessRate - successRatios[i], 2);
//                    // searchTimeSD += Math.pow(averageSearchTime - searchTimes[i], 2);
//                    // searchSuccessTimeSD += Math.pow(averageSuccessTime - successfulSearchTime[i], 2);
//                    // searchFailureTimeSD += Math.pow(averageFailureTime - failureSearchTime[i], 2);
//                }
//
//                /*
//                Success ration standard deviation
//                 */
//                successRatioSD = Math.sqrt(successRatioSD);
//                successRatioSD /= system.getTopologyNumbers();
//
//                /*
//                Search time standard deviation
//                 */
//                // searchTimeSD = Math.sqrt(searchTimeSD);
//                // searchTimeSD /= system.getTopologyNumbers();
//
//                /*
//                Successful search time standard deviation
//                 */
//                //searchSuccessTimeSD = Math.sqrt(searchSuccessTimeSD);
//                //searchSuccessTimeSD /= system.getTopologyNumbers();
//
//                /*
//                Unsuccessful search time standard deviation
//                 */
//                // searchFailureTimeSD = Math.sqrt(searchFailureTimeSD);
//                // searchFailureTimeSD /= system.getTopologyNumbers();
//
//
//                System.out.println("------------------------------------------------------------");
//                System.out.println("Finalized Simulation Results: ");
//                System.out.println(system.getChurnStabilizationAlgorithm());
//                System.out.println("ChurnStabilization/BlockLookupEvalution.java: average success ratio of this simulation: " + averageSuccessRate + " SD: " + successRatioSD);
//                // System.out.println("ChurnStabilization/LookupEvalution.java: average search time of this simulation: " + averageSearchTime + " SD: " + searchTimeSD);
//                // System.out.println("ChurnStabilization/LookupEvalution.java: average successful search time of this simulation " + averageSuccessTime + " SD: " + searchSuccessTimeSD);
//                // System.out.println("ChurnStabilization/LookupEvalution.java: average failed search time of this simulation: " + averageFailureTime + " SD: " + searchFailureTimeSD);
//                System.out.println("------------------------------------------------------------");
//            }
//        }
//    }
//
//    /**
//     * @param random the search random generator, use the getter of searchRandomGenerator of the same class
//     * @return -1 if could not perform the search due to the low number of online Nodes, otherwise the success search ratio
//     */
//    private double randomizedSearchForNumericalIDs (BlockGraphOperations bgo, SkipGraphOperations sgo, Random random)
//    {
//        // double averageSearchTime = 0;
//        // double averageSuccessfulSearchTime = 0;
//        // double averageFailedSearchTime = 0;
//        int numberOfSuccessfulSearches = 0;
//        int numberOfFailedSearches = 0;
//
//        int numOfActiveBlocks = bgo.getBs().getNumberOfOnlineBlocks(sgo.getTG().mNodeSet);
//
//        if(numOfActiveBlocks < 0.01 * system.getBlockCapacity())
//        {
//            System.out.println(bgo.getBs().getNumberOfOnlineBlocks(sgo.getTG().mNodeSet));
//            return -1;
//        }
//
//        int iterations = 0;
//        while (iterations == 0)
//            iterations = random.nextInt(numOfActiveBlocks * (numOfActiveBlocks - 1) / 2) + 1;
//        int iterationsSQRT = (int) Math.sqrt(iterations) + 1;
//        //iterations = (int) Math.ceil((double) iterations);
//        ChurnStochastics.updateAverageLookups(iterations);
//        double counter = 0;
//
//        ArrayList<Integer> onlineBlocksOriginalList = bgo.getBs().getIndicesOfOnlineBlocks(sgo.getTG().mNodeSet);
//        ArrayList<Integer> onlineBlocksSearchSeed = (ArrayList<Integer>) onlineBlocksOriginalList.clone();
//
//        // Set<Pair> searchesSet = new HashSet<>();
//        for (int i = 0; i < iterations; i++)
//        {
//
//            if (onlineBlocksSearchSeed.size() <= 2)
//            {
//                onlineBlocksSearchSeed = (ArrayList<Integer>) onlineBlocksOriginalList.clone();
//                Collections.shuffle(onlineBlocksSearchSeed);
//            }
//
//            /*
//            Picking and removing search target from the seed
//             */
//            int searchTargetIndex = random.nextInt(onlineBlocksSearchSeed.size() - 1);
//            int searchTarget = onlineBlocksSearchSeed.get(searchTargetIndex);
//            onlineBlocksSearchSeed.remove(searchTargetIndex);
//
//            /*
//            Picking and removing search target from the seed
//             */
//            int searchInitiatorIndex = random.nextInt(onlineBlocksSearchSeed.size() - 1);
//            int searchInitiator = bgo.getBs().getBlock(onlineBlocksSearchSeed.get(searchInitiatorIndex)).getOwnerIndex();
//            onlineBlocksSearchSeed.remove(searchInitiatorIndex);
//
////            int searchTarget = sgo.getTG().randomlyPickOnline();
////            int initiator = sgo.getTG().randomlyPickOnline();
////            while (initiator == searchTarget || searchesSet.contains(searchTarget) || searchesSet.contains(initiator))
////            {
////                initiator = sgo.getTG().randomlyPickOnline();
////            }
////
////            searchesSet.add(initiator);
////            searchesSet.add(searchTarget);
//
//            Message m = new Message();
//
//            // bgo.getBs().resetTotalTime(); commented out just for now
//
//            int searchResult = bgo.SearchByNumID(bgo.getBs().getBlock(searchTarget).getNumID(), searchInitiator, m, system.getBlockLookupTableSize() - 1);
//           // averageSearchTime += bgo.getBs().getTotalTime();
//
//            //TODO bgo.getBs().containsBlock(blockOwnerIndex, blockNumericalID)
//            if (bgo.getBs().containsBlock(searchResult, searchTarget, sgo.getTG().mNodeSet))
//            {
//
//                ChurnStabilization alg = new AlgorithmInvoker().churnStabilization();
//                if(alg != null) {
//                    for (int piggyIndex : m.getPiggyBackedNodes()) {
//                        Node n = sgo.getTG().mNodeSet.getNode(piggyIndex);
//                        //sgo.getTG().mNodeSet.getNode(index).addToBucket(n.getNumID(), n.getIndex(), sgo.getTG().mNodeSet.commonPrefixLength(i, index), currentTime);
//                        alg.insertIntoBucket(n.getIndex(), n.getNumID(), n.getAvailabilityProbability(), piggyIndex, sgo.getTG().mNodeSet);
//                    }
//                }
//
//                counter++;
//                // ChurnStochastics.updateAverageSuccessTimeOuts();
//                // averageSuccessfulSearchTime += bgo.getBs().getTotalTime();
//                numberOfSuccessfulSearches++;
//
//            } else
//            {
//               // ChurnStochastics.updateAverageFailureTimeOuts();
//              //  averageFailedSearchTime += bgo.getBs().getTotalTime();
//                numberOfFailedSearches++;
//            }
//
//        }
//
//        if (iterations < 0)
//        {
//            System.err.println("SkipGraphOperations.java: All pairs random Lookup failed");
//            System.exit(0);
//        }
//
//        /* TIME RELATED THINGS
//
//        //topologySearchTime += (double) averageSearchTime / iterations;
//        if (topologySearchTime == 0)
//        {
//            topologySearchTime = (double) averageSearchTime / iterations;
//        }
//        else
//        {
//            topologySearchTime = (topologySearchTime * 0.9) + (averageSearchTime * 0.1 / iterations);
//        }
//        if (numberOfSuccessfulSearches != 0)
//        {
//            //topologySuccessfulSearchTime += (double) averageSuccessfulSearchTime / numberOfSuccessfulSearches;
//            if (topologySuccessfulSearchTime == 0)
//            {
//                topologySuccessfulSearchTime = (double) averageSuccessfulSearchTime / numberOfSuccessfulSearches;
//            }
//            else
//            {
//                topologySuccessfulSearchTime = (topologySuccessfulSearchTime * 0.9) + (((double) averageSuccessfulSearchTime / numberOfSuccessfulSearches) * 0.1);
//            }
//        }
//        if (numberOfFailedSearches != 0)
//        {
//            //topologyFailureSearchTime += (double) averageFailedSearchTime / numberOfFailedSearches;
//            if (topologyFailureSearchTime == 0)
//            {
//                topologyFailureSearchTime = (double) averageFailedSearchTime / numberOfFailedSearches;
//            }
//            else
//            {
//                topologyFailureSearchTime = (topologyFailureSearchTime * 0.9) + (((double) averageFailedSearchTime / numberOfFailedSearches) * 0.1);
//            }
//        }
//        */
//
//        return (double) counter / iterations;
//
//    }
//}
