package Blockchain.LightChain.Experiments;

import Blockchain.LightChain.Transaction;
import SkipGraph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This static class is used to measure the efficiency, i.e. the average number of honest validators
 * that can be acquired for transaction, with the given ValidatorThreshold (Alpha).
 */

public class EfficiencyExperiment {
    private static final Logger log = LoggerFactory.getLogger(EfficiencyExperiment.class);
    // Time slot -> (Transaction -> Honest validators number)
    private static Map<Integer, Map<Integer, Integer>> honestNodesNumber = new HashMap<>();
    // Calculated efficiencies so far.
    private static List<Double> avgHonestNodes = new LinkedList<>();

    /**
     * Inform the experiment that a transaction has acquired validators.
     * This method should be called when a transaction acquires its validators.
     * @param owner the owner of the transaction.
     * @param tx the transaction itself.
     * @param time current time slot.
     */
    public static void informAcquisition(Node owner, Transaction tx, int time) {
        // We only care about the honest nodes.
        if(owner.isMalicious()) return;
        // Get the number of honest validators this transaction has acquired.
        int honestValidators = (int)tx.getValidators().stream()
                .filter(x -> !x.isMalicious())
                .count();
        if(!honestNodesNumber.containsKey(time)) {
            honestNodesNumber.put(time, new HashMap<>());
        }
        honestNodesNumber.get(time).put(tx.getIndex(), honestValidators);
    }

    /**
     * Calculates and reports the result at the end of time slot. This method should be called at the
     * end of each time slot.
     * @param time current time slot.
     */
    public static void calculateResults(int time) {
        if(!honestNodesNumber.containsKey(time)) {
            log.info("Efficiency Experiment: For t={} there were no validator acquisition.", time);
        } else {
            Map<Integer, Integer> honestNodesNumberForTime = honestNodesNumber.get(time);
            // Find and report the avg. number of honest nodes for the current time slot.
            double avgHonestNodesForTime = honestNodesNumberForTime.values().stream()
                    .mapToInt(x -> x)
                    .average()
                    .orElse(0);
            log.info("Efficiency Experiment: For t={} the avg. honest node per transaction is {}", time, avgHonestNodesForTime);
            avgHonestNodes.add(avgHonestNodesForTime);
        }
        // Find and report the avg. number of honest nodes over all time slots.
        double overallHonestNodes = avgHonestNodes.stream()
                .mapToDouble(x -> x)
                .average()
                .orElse(0);
        log.info("Efficiency Experiment: Avg. honest node over time is {}", overallHonestNodes);
    }

    public static void reset() {
        honestNodesNumber.clear();
        avgHonestNodes.clear();
    }
}
