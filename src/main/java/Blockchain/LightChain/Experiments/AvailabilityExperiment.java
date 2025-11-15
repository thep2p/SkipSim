package Blockchain.LightChain.Experiments;

import Blockchain.LightChain.Transaction;
import Simulator.SkipSimParameters;
import SkipGraph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * This static class is used to measure the average availability a transaction. For Proof-of-Validation, we assume
 * that the validators of a transaction are also its replicas. This experiment keeps track of each transactions
 * replicas and finds the average number of online replicas (availability) for a transaction.
 */
public class AvailabilityExperiment {
    private static final Logger log = LoggerFactory.getLogger(AvailabilityExperiment.class);

    // (Transaction -> Available validator amounts as stack)
    private static Map<Transaction, Stack<Integer>> txAvailableValidatorStack = new HashMap<>();
    // (Generation time -> TransactionList containing the transactions generated at this time)
    private static Map<Integer, TransactionList> genTimeTxList = new HashMap<>();
    // The set of transactions that are being watched.
    private static Set<Transaction> watchedTransactions = new HashSet<>();

    /**
     * Represents a list of transactions that are generated at the same time slot. We keep the transactions
     * that are generated at the same time slot together under a TransactionList. The average availabilities
     * for each TransactionList is calculated separately, then all the TransactionList averages are taken into
     * account to report the final value.
     */
    static class TransactionList {
        // The transactions that are stored.
        List<Transaction> transactions = new LinkedList<>();
        // The calculated average availabilities so far.
        List<Double> averages = new LinkedList<>();
        // The time that these transactions were generated.
        int generationTime;

        TransactionList(int generationTime) {
            this.generationTime = generationTime;
        }

        int size() {
            return transactions.size();
        }
    }

    /**
     * Registers a newly generated transaction into this experiment.
     * This method should be called whenever a new transaction is generated and validators are acquired.
     * @param owner the owner of the transaction.
     * @param tx the transaction itself.
     * @param time current time slot.
     */
    public static void registerTransaction(Node owner, Transaction tx, int time) {
        // We do not care about malicious owners.
        if(owner.isMalicious()) return;
        // Get the number of honest validators.
        int numOfHonestValidators = tx.getSignedValidators().size();
        // We only care about transactions that have honest success, i.e. they should
        // have at least SignatureThreshold many honest validators.
        if(numOfHonestValidators < SkipSimParameters.getSignatureThreshold()) {
            return;
        }
        if(!genTimeTxList.containsKey(time)) {
            genTimeTxList.put(time, new TransactionList(time));
        }
        genTimeTxList.get(time).transactions.add(tx);
        txAvailableValidatorStack.put(tx, new Stack<>());
        watchedTransactions.add(tx);
    }

    /**
     * Calculates and reports the availability results for the current time slot.
     * This method should be called at the end of each time slot.
     *
     * NOTE: For each TransactionList (a list of transactions that are generated at the same time slots), we
     * find the avg. availability separately, then combine them. This is because each TransactionList converges
     * to an expected value. Because we generate transactions at each time slot, there are always transactions
     * that are yet to be converged to their expected availability. Thus, a naive calculation returns a much
     * higher value than expected. Thus, we also define a corrected result, that takes each TransactionList's
     * convergence behavior into account.
     *
     * @param time current time slot.
     */
    public static void calculateResults(int time) {
        // Acquire each transaction's available validators.
        for(Transaction tx : watchedTransactions) {
            int availableValidators = getAvailableValidators(tx);
            txAvailableValidatorStack.get(tx).push(availableValidators);
        }
        double currAvgAvailability = 0;
        // For each TransactionList, acquire the average availability.
        for(TransactionList txList : genTimeTxList.values()) {
            double averageAvailability = txList.transactions.stream()
                    .mapToInt(tx -> txAvailableValidatorStack.get(tx).peek())
                    .average()
                    .orElse(0);
            txList.averages.add(averageAvailability);
            currAvgAvailability += averageAvailability * txList.size();
        }
        currAvgAvailability /= watchedTransactions.size();
        log.info("Availability experiment [topology={}, time={}]: avgAvailability={}",
            SkipSimParameters.getCurrentTopologyIndex(), time, currAvgAvailability);

        // Calculate the overall availability in two different ways.

        // Naive availability does not take into account the converging of availabilities.
        double overallNaiveAvailability = 0;
        int naiveDivisor = 0;
        // Corrected availability is calculated  by trying to determine the values that the averages of each
        // TransactionList converge to.
        double overallCorrectedAvailability = 0;
        int correctedDivisor = 0;
        for(TransactionList txList : genTimeTxList.values()) {
            double[] avgAvailabilityData = findAvailabilityData(txList);
            overallNaiveAvailability += avgAvailabilityData[0] * avgAvailabilityData[1];
            naiveDivisor += avgAvailabilityData[1];
            overallCorrectedAvailability += avgAvailabilityData[2] * avgAvailabilityData[3];
            correctedDivisor += avgAvailabilityData[3];
        }
        overallCorrectedAvailability /= correctedDivisor;
        overallNaiveAvailability /= naiveDivisor;
        // Report the naive & corrected availability.
        log.info("Availability experiment [topology={}, time={}]: overallNaiveAvailability={}",
            SkipSimParameters.getCurrentTopologyIndex(), time, overallNaiveAvailability);
        log.info("Availability experiment [topology={}, time={}]: overallCorrectedAvailability={}",
            SkipSimParameters.getCurrentTopologyIndex(), time, overallCorrectedAvailability);
    }

    /**
     * Finds the number of replicas that are online at the moment of invocation.
     *
     * NOTE: The transaction itself is not counted as a replica!
     *
     * @param tx the transaction.
     * @return the number of available replicas of the transaction.
     */
    private static int getAvailableValidators(Transaction tx) {
        return (int)tx.getSignedValidators().stream()
                .filter(x -> x.isOnline())
                .count();
    }

    /**
     * Determines the convergence point of a list of double values.
     * @param doubles the list of double values.
     * @return the convergence point of the list.
     */
    private static int findConvergencePoint(List<Double> doubles) {
        double currentSlope = Double.NEGATIVE_INFINITY;
        for(int i = 1; i < doubles.size(); i++) {
            double current = doubles.get(i);
            double previous =  doubles.get(i-1);
            // The slope has decreased this much percent w.r.t the old slope.
            double newSlope = current - previous;
            double slopeChange = Math.abs(newSlope - currentSlope) / Math.abs(currentSlope);
            // If the slope change is less than 1%, we have converged to a value.
            if(slopeChange < 0.01) {
                return i;
            }
            currentSlope = newSlope;
        }
        return doubles.size();
    }

    /**
     * For a given TransactionList (a list of transactions that are generated at the same time slot),
     * finds its availability data.
     * @param txList the transaction list.
     * @return an array with 4 elements: [Naive average, Naive weight, Corrected average, Corrected weight]
     */
    private static double[] findAvailabilityData(TransactionList txList) {
        // Convergence point for the TransactionList.
        int convPoint = findConvergencePoint(txList.averages);
        return new double[] {
                // Naive average.
                txList.averages.stream()
                        .mapToDouble(x -> x)
                        .average()
                        .orElse(0),
                // Naive weight.
                txList.averages.size(),
                // Corrected average is found by cutting of the data
                // points that occur before the convergence point.
                txList.averages.stream()
                        .skip(convPoint)
                        .mapToDouble(x -> x)
                        .average()
                        .orElse(0),
                // Corrected weight.
                txList.averages.size() - convPoint
        };
    }


    public static void reset() {
        watchedTransactions.clear();
        txAvailableValidatorStack.clear();
        genTimeTxList.clear();
    }
}
