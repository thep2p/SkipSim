package Blockchain.LightChain.Experiments;

import Simulator.SkipSimParameters;
import SkipGraph.SkipGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * This static class is used to measure the probability of a node being online. This is done by collecting
 * the number of online nodes at each time slot and taking its ratio to the system capacity.
 */
public class OnlineProbabilityExperiment {
    private static final Logger log = LoggerFactory.getLogger(OnlineProbabilityExperiment.class);

    // A list of online node amounts for each time slot.
    private static List<Integer> onlineNodeAmounts = new LinkedList<>();

    /**
     * Calculates the online probability for the given time slot. This method must be called at the
     * end of each time slot.
     * @param sgo SkipGraphOperations object that the calculations should be performed on.
     * @param time current time slot.
     */
    public static void calculateResults(SkipGraphOperations sgo, int time) {
        // Get the current number of online nodes.
        int onlineNodes = sgo.getTG().getNodeSet().getNumberOfOnlineNodes();
        onlineNodeAmounts.add(onlineNodes);
        // Calculate and report the current online probability.
        double probCurrent = (double)onlineNodes/ SkipSimParameters.getSystemCapacity();
        log.info("OnlineProbability experiment [topology={}, time={}]: onlineProb={}, onlineNodes={}",
            SkipSimParameters.getCurrentTopologyIndex(), time, probCurrent, onlineNodes);
        // Calculate and report the overall online probability (over all the time slots).
        double probOverall = onlineNodeAmounts.stream()
                .mapToDouble(x -> (double)x/SkipSimParameters.getSystemCapacity())
                .average()
                .orElse(0);
        log.info("OnlineProbability experiment [topology={}, time={}]: overallAvgOnlineProb={}",
            SkipSimParameters.getCurrentTopologyIndex(), time, probOverall);
    }
}
