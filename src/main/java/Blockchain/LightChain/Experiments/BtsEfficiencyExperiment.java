package Blockchain.LightChain.Experiments;

import SkipGraph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This static class is used to measure the bootstrapping efficiency, i.e. the average number of honest
 * view introducers that can be acquired for a node, with the given ValidatorThreshold (Alpha).
 */

public class BtsEfficiencyExperiment {
    private static final Logger log = LoggerFactory.getLogger(BtsEfficiencyExperiment.class);
    // Time slot -> (Node -> Honest view introducer number)
    private static Map<Integer, Map<Integer, Integer>> honestIntroducerNumber = new HashMap<>();
    // Calculated efficiencies so far.
    private static List<Double> avgHonestIntroducers = new LinkedList<>();

    /**
     * Inform the experiment that a node has gone online and acquired its view introducers.
     * This method should be called whenever a node goes online.
     * @param node the node that has gone online.
     * @param time current time slot.
     */
    public static void informIntroduction(Node node, int time) {
        // We only measure efficiency for the honest nodes.
        if(node.isMalicious()) return;
        // Find the list of honest view introducers this node has.
        int honestIntroducers = (int)node.getViewIntroducers().stream()
                .filter(x -> !x.isMalicious())
                .count();
        if(!honestIntroducerNumber.containsKey(time)) {
            honestIntroducerNumber.put(time, new HashMap<>());
        }
        honestIntroducerNumber.get(time).put(node.getIndex(), honestIntroducers);
    }

    /**
     * Calculates and reports the result at the end of a time slot. This method should be called
     * at the end of each time slot.
     * @param time current time slot.
     */
    public static void calculateResults(int time) {
        if(!honestIntroducerNumber.containsKey(time)) {
            log.info("Bts. Efficiency Experiment: For t={} there were no bootstrapping.", time);
        } else {
            Map<Integer, Integer> honestIntroducerNumberForTime = honestIntroducerNumber.get(time);
            // Finds and reports the average honest introducers for this time slot.
            double avgHonestIntroducerForTime = honestIntroducerNumberForTime.values().stream()
                    .mapToInt(x -> x)
                    .average()
                    .orElse(0);
            log.info("Bts. Efficiency Experiment: For t={} the avg. honest view introducer per node is {}", time, avgHonestIntroducerForTime);
            avgHonestIntroducers.add(avgHonestIntroducerForTime);
        }
        // Finds and reports the average honest introducers for all time slots.
        double overallHonestNodes = avgHonestIntroducers.stream()
                .mapToDouble(x -> x)
                .average()
                .orElse(0);
        log.info("Bts. Efficiency Experiment: Avg. honest view introducer over time is {}", overallHonestNodes);
    }

    public static void reset() {
        honestIntroducerNumber.clear();
        avgHonestIntroducers.clear();
    }
}
