package Blockchain.LightChain;

import ChurnStabilization.ChurnStabilization;
import DataTypes.Message;
import Simulator.AlgorithmInvoker;
import Simulator.SkipSimParameters;
import SkipGraph.Node;
import SkipGraph.SkipGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * This class is used to perform lookup-evaluation tests on the skip-graph.
 */
public class LookupEvaluation extends SkipGraph.LookupEvaluation
{
    private static final Logger log = LoggerFactory.getLogger(LookupEvaluation.class);

    public LookupEvaluation()
    {
        super();
    }


    @Override
    public void flush()
    {

    }

    // Returns an array-list of online nodes with at least one transaction.
    private ArrayList<Node> getAvailableNodes(SkipGraphOperations sgo) {
        return sgo.getTG().mNodeSet.getIndicesOfOnlineNodes().stream()
                .map(x -> (Node) sgo.getTG().mNodeSet.getNode(x))
                .filter(x -> !x.getTxSet().isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * This function performs random search for numerical IDS only for a single time slot and returns the success ratio
     * of the search for that time slot. It additionally updates the average search latency, accompained with the average
     * search latency of the successful and unsuccessful searches.
     * @param random the search random generator, use the getter of searchRandomGenerator of the same class
     * @return -1 if could not perform the search due to the low number of online Nodes, otherwise the success search ratio
     */
    @Override
    protected double randomizedSearchForNumericalIDs(SkipGraphOperations sgo, Random random, int currentTime) {
        ArrayList<Node> availableNodes = getAvailableNodes(sgo);

        if (availableNodes.size() < 0.01 * SkipSimParameters.getSystemCapacity()) {
            return -1;
        }

        /*
        Deciding on the number of iterations
         */
        int iterations = random.nextInt(availableNodes.size() * (availableNodes.size() - 1) / 2) + 1;

        /*
         Number of successful searches.
         */
        int successfulIterations = 0;
        /*
        Generating the seed to pick the search target and search initiator, it is implemented as an array-list that
        is initialized to the set of all online Nodes with at least one tx, and then each time a search target or
        initiator is chosen, it is removed from the seed.
         */
        ArrayList<Node> seed = (ArrayList<Node>) availableNodes.clone();

        for (int i = 0; i < iterations; i++) {
            /*
            If size of seed is less than two elements, initialize it again.
             */
            if (seed.size() < 2) {
                // Reinitialize the seed, and shuffle it.
                seed = (ArrayList<Node>) availableNodes.clone();
                Collections.shuffle(seed);
            }

            /*
            Picking and removing search target from the seed
             */
            int searchTargetOwnerChoice = random.nextInt(seed.size() - 1);
            Node searchTargetNode = seed.remove(searchTargetOwnerChoice);

            int searchTargetTXBIndex = searchTargetNode.chooseRandomTXB(random);
            int searchTargetTXBNumId = sgo.getTransactions().getNode(searchTargetTXBIndex).getNumID();

            /*
            Picking and removing search initiator from the seed
             */
            int searchInitiatorChoice = (seed.size() > 1) ? random.nextInt(seed.size() - 1) : 0;
            Node nodeSearchInitiator = seed.remove(searchInitiatorChoice);

            Message m = new Message();

            /*
            Determining the direction of search.
             */
            int mostSimilarInInitiator = nodeSearchInitiator
                    .mostSimilarTXB(sgo.getTransactions(), searchTargetTXBNumId,
                            m, SkipGraphOperations.LEFT_SEARCH_DIRECTION, 0);
            if(mostSimilarInInitiator == -1) {
                mostSimilarInInitiator = nodeSearchInitiator
                        .mostSimilarTXB(sgo.getTransactions(), searchTargetTXBNumId,
                                m, SkipGraphOperations.RIGHT_SEARCH_DIRECTION, 0);

            }
            int mostSimilarNumId = sgo.getTransactions().getNode(mostSimilarInInitiator).getNumID();
            int searchDirection = (searchTargetTXBNumId < mostSimilarNumId)
                    ? SkipGraphOperations.LEFT_SEARCH_DIRECTION
                    : SkipGraphOperations.RIGHT_SEARCH_DIRECTION;

            int searchResult = sgo.SearchByNumID(
                    searchTargetTXBNumId,
                    nodeSearchInitiator,
                    m,
                    SkipSimParameters.getLookupTableSize() - 1,
                    currentTime,
                    sgo.getTransactions(),
                    searchDirection);

            /*
            The owner containing the transaction that we were looking for.
             */
            Node nodeSearchResult = (Node)sgo.getTG().mNodeSet.getNode(searchResult);
            /*
            Checks if the search has been conducted successfully.
             */
            if (nodeSearchResult.containsTXB(sgo.getTransactions(), searchTargetTXBNumId) != -1) {
                ChurnStabilization alg = AlgorithmInvoker.churnStabilization();
                successfulIterations++;
            } else {
                int mostSimilarInResult = nodeSearchResult
                        .mostSimilarTXB(sgo.getTransactions(), searchTargetTXBNumId,
                                m, searchDirection, 0);
                log.trace("Transaction search unsuccessful");
            }
        }

        if (iterations < 0) {
            System.err.println("SkipGraphOperations.java: All pairs random Lookup failed");
            System.exit(0);
        }
        return  ((double)successfulIterations) / iterations;
    }


    @Override
    public void randomizedLookupTests(SkipGraphOperations sgo, int currentTime, int searchType)
    {
        // At t=0, there are no transactions yet.
        if(currentTime == 0) return;
        double successRatio = randomizedSearchForNumericalIDs(sgo, sgo.getSearchRandomGenerator(), currentTime);
        log.info("Transaction lookup success ratio at time {}: {}", currentTime, successRatio);
    }


}
