package Blockchain.LightChain.Experiments;

import Simulator.SkipSimParameters;
import SkipGraph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This static class is used to measure the chance of a malicious success in case of randomized bootstrapping.
 * When a malicious node achieves to find SignatureThreshold (T) many malicious view introducers, we
 * define this as malicious success.
 */
public class BtsMaliciousSuccessExperiment {
    private static final Logger log = LoggerFactory.getLogger(BtsMaliciousSuccessExperiment.class);

    // Time -> (Node -> malicious success #)
    private static Map<Integer, Map<Integer, Integer>> successMap = new HashMap<>();
    private static List<Double> successChances = new LinkedList<>();
    private static int acquisitions = 0;

    /**
     * Inform the experiment that a node has gone online and acquired its view introducers.
     * This method should be called whenever a node goes online.
     * @param node the node that has gone online.
     * @param time current time slot.
     */
    public static void informIntroduction(Node node, int time) {
        // We don't care about the honest nodes.
        if(!node.isMalicious()) return;
        acquisitions++;
        // Get the number of malicious view introducers the node has.
        int maliciousIntroducers = (int)node.getViewIntroducers().stream()
                .filter(x -> x.isMalicious())
                .count();

        if(!successMap.containsKey(time)) {
            successMap.put(time, new HashMap<>());
        }

        if(!successMap.get(time).containsKey(node.getIndex())) {
            successMap.get(time).put(node.getIndex(), 0);
        }

        // If there are enough malicious introducers, the node has acquired a malicious view.
        if(maliciousIntroducers >= SkipSimParameters.getSignatureThreshold())
            successMap.get(time).put(node.getIndex(), successMap.get(time).get(node.getIndex()) + 1);
    }

    /**
     * Calculates and reports the result at the end of a time slot. This method should be called
     * at the end of each time slot.
     * @param time current time slot.
     */
    public static void calculateResults(int time) {
        if(!successMap.containsKey(time)) {
            log.info("BtsMaliciousSuccess experiment [topology={}, time={}]: noMaliciousBootstrapping=true",
                SkipSimParameters.getCurrentTopologyIndex(), time);
        } else {
            Map<Integer, Integer> successMapForTime = successMap.get(time);
            // Find and report the avg. success chance for the current time.
            double avgSuccessForTime = (double) successMapForTime.values().stream()
                    .mapToInt(x -> x)
                    .sum()
                    / acquisitions;
            log.info("BtsMaliciousSuccess experiment [topology={}, time={}]: avgMaliciousSuccessRate={}",
                SkipSimParameters.getCurrentTopologyIndex(), time, avgSuccessForTime);
            successChances.add(avgSuccessForTime);
        }
        // Find and report the avg. success chance over all time slots.
        double overallSuccessChance = successChances.stream().mapToDouble(x -> x).average().orElse(0);
        log.info("BtsMaliciousSuccess experiment [topology={}, time={}]: overallAvgSuccessRate={}",
            SkipSimParameters.getCurrentTopologyIndex(), time, overallSuccessChance);
        acquisitions = 0;
    }

    public static void reset() {
        successMap.clear();
        successChances.clear();
        acquisitions = 0;
    }
}
