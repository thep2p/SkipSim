package ChurnStabilization;

import DataTypes.Constants;
import Simulator.SkipSimParameters;
import SkipGraph.Node;
import SkipGraph.Nodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class ChurnStochastics
{
    private static final Logger log = LoggerFactory.getLogger(ChurnStochastics.class);

    /*
    Average number of routing candidates
     */
    private static double topologyAverageRoutingCandidates = 0;
    private static double topologyAverageBucketSize = 0;
    private static double totalAverageBucketSize = 0;
    private static int numberOfIntermediateNodes = 0;
    private static int totalSuccessSearchPathLength = 0;
    private static int totalFailedSearchPathLength = 0;
    private static double totalAverageRoutingCandidates = 0;
    private static double topologyAverageOfflineRoutingCandidates = 0;
    private static double totalAverageOfflineRoutingCandidates = 0;
    private static double topologyAverageTimeOuts = 0;
    //private static double topologyEffectiveSearchPath = 0;



    /******************************Number of timeouts********************************************************/
    /**
     * Keeps the total number of timeouts over all the search paths within one topology that is caused by the
     * successful searches
     */
    private static double topologyTotalSuccessTimeOuts = 0;
    /**
     * Keeps the total number of timeouts over all the search paths within one topology that is caused by the
     * failed searches
     */
    private static double topologyTotalFailureTimeOuts = 0;
    /**
     * Keeps the total number of timeouts over all the search paths within one topology that is caused by the
     * successful searches
     */
    private static double totalAverageSuccessTimeOuts = 0;
    /**
     * Keeps the summation of the average number of failure timeouts over all the topologies is caused by the
     * failed searches
     */
    private static double totalAverageFailureTimeOuts = 0;

    /**********************************SWDBG*************************************************************/
    /**
     * Total size of SWDBGs within topology, each Node has one SWDBG if the prediction is based on DBG
     */
    private static double topologyTotalSWDBGSize = 0;
    /**
     * The maximum size of SWDBG state size within one topology in case the prediction is SWDBG
     */
    private static double topologyMaxSWDBGSize = 0;
    /**
     * The maximum size of SWDBG state size within one topology in case the prediction is SWDBG
     */
    private static double totalMaxSWDBGSize = 0;
    /**
     * Counts the number of time takes summation of state sizes, uses for taking the average within this topology,
     * note that SWDBG is updated only when the Node is online, hence, there might be cases where SWDBG size is not recorded
     */
    private static int stateSizeUpdateCounter = 0;
    /**
     * The summation of all SWDBG sizes over all topologies
     */
    private static double totalAverageSWDBGSize = 0;


    /*
    Availability prediction measurments
     */
    private static double timeSlotPredictionerror = 0;
    private static double topologyPredictionErrorEMA = 0;
    /**
     * Used to update the topologyPredictionErrorEMA
     */
    private static int lastCheckedTime = 0;
    private static double overalAveragePredictionError = 0;

    /*
    Average churn stabilization time outs, meaning the times resolveFailure contacts an offline Node to resolve failure
     */
    private static double topologyAverageResolveFailureTimeOuts = 0;
    private static double totalAverageResolveFailureTimeOuts = 0;
    /*
    Average interarrival time data
     */
    private static double totalAverageInterArrivalTime = 0;
    private static double totalGeneratedInterArrivalTimes = 0;
    /*
    Average number of arrivals and departures
    */

    private static double totalAverageOfOnlineNodes = 0;
    //private static double totalAverageArrivals = 0;
    private static double totalAverageDepartures = 0;
    private static double averageLookups = 0;
    /*
    Average session length data
    */
    private static double totalAverageSessionLength = 0;
    //private static double totalGeneratedSessionLengthes = 0;

    /*
Average interarrival time data
 */
    private static double topologyAverageInterArrivalTime = 0;
    private static double topologyArrivals = 0;
    /*
    Average number of arrivals and departures
    */
    private static double topologyAverageOfOnlineNodes = 0;
    //private static double topologyAverageArrivals = 0;
    private static double topologyAverageDepartures = 0;
    private static double topologyAverageLookups = 0;
    /*
    Average session length data
    */
    private static double topologyAverageSessionLength = 0;

    private static double topologyGeneratedSessionLengthes = 0;

    public static double getTopologyArrivals()
    {
        return topologyArrivals;
    }

    public ChurnStochastics()
    {
    }


    public static void updateTotalAverageInterArrivalTime(double interArrivalTime)
    {
        topologyAverageInterArrivalTime += interArrivalTime;
        topologyArrivals++;
    }

    public static void updateTotalAverageOfOnlineNodes(double numberOfOnlineNodes)
    {
        topologyAverageOfOnlineNodes += numberOfOnlineNodes;
    }


    public static void increaseTotalAverageDepartures()
    {
        topologyAverageDepartures++;
    }

    public static void updateAverageLookups(double averageLookups)
    {
        topologyAverageLookups += averageLookups;
    }

    public static void updateTotalAverageSessionLength(double inputSessionLength)
    {
        topologyAverageSessionLength += inputSessionLength;
        topologyGeneratedSessionLengthes++;
    }

    public static void uptadeAverageBruijnStateSize(double stateSize)
    {
        topologyTotalSWDBGSize += stateSize;
        if(stateSize > topologyMaxSWDBGSize)
        {
            topologyMaxSWDBGSize = stateSize;
        }
        stateSizeUpdateCounter++;
    }

    public static void updateAverageResolveFailureTimeOuts()
    {
        topologyAverageResolveFailureTimeOuts++;
    }

    /**
     * Updates average number of routing candidates on the search path
     * @param candidatesNumber number of routing candidates per Node on the search path
     */
    public static void updateAverageRoutingCandidates(int candidatesNumber)
    {
        topologyAverageRoutingCandidates += candidatesNumber;
    }

    public static void updateNumberOfIntermediateNodesOnPath(int num)
    {
        numberOfIntermediateNodes = num;
    }

    /**
     * Increases number of time outs each time a search faces timeout failure on any of the
     * Nodes on the search path
     */
    public static void updateAverageTimeOuts()
    {
        topologyAverageTimeOuts++;
    }

    public static void updateAverageSuccessTimeOuts()
    {
        topologyTotalSuccessTimeOuts += topologyAverageTimeOuts;
        totalSuccessSearchPathLength += numberOfIntermediateNodes;
        topologyAverageTimeOuts = 0;
    }

    public static void updateAverageFailureTimeOuts()
    {
        topologyTotalFailureTimeOuts += topologyAverageTimeOuts;
        totalFailedSearchPathLength += numberOfIntermediateNodes;
        topologyAverageTimeOuts = 0;
    }

    /**
     * Updates number of average routing candidates on the search path that are online
     * Note: for this function to work properly, "updateAverageRoutingCandidates" should be also invoked simultanously
     * to keep record of the number of Nodes on the search path.
     * @param ns a Nodes instance
     * @param candidates routing candidates of the Node on the search path
     */
    public static void updateAverageOfflineRoutingCandidates(Nodes ns, ArrayList<BucketItem> candidates)
    {
        for (BucketItem e : candidates)
        {
            if (((Node) ns.getNode(e.getNodeIndex())).isOffline())
            {
                topologyAverageOfflineRoutingCandidates++;
            }
        }
    }

    /**
     * @param predictionError prediction error of the graph
     * @param currentTime     current time slot of simulation
     */
    public static void updateAveragePredictionError(double predictionError, int currentTime)
    {
        if (lastCheckedTime < currentTime)
        {
//            if (lastCheckedTime == 0)
//            {
                topologyPredictionErrorEMA += timeSlotPredictionerror / SkipSimParameters.getSystemCapacity();
//            }
//            if (lastCheckedTime > 0)
//            {
//                topologyPredictionErrorEMA = (1 - timeSlotPredictionErrorEMA_beta) * topologyPredictionErrorEMA
//                        + timeSlotPredictionErrorEMA_beta * (timeSlotPredictionerror / system.getSystemCapacity());
//            }
//            else
//            {
//                topologyPredictionErrorEMA = (timeSlotPredictionerror / system.getSystemCapacity());
//            }
            lastCheckedTime = currentTime;

            if (SkipSimParameters.getAvailabilityPredictor().equalsIgnoreCase(Constants.Churn.AvailabilityPredictorAlgorithm.SWDBG) && stateSizeUpdateCounter > 0)
            {
                log.debug("So far: average size of SW-DBG {}", topologyTotalSWDBGSize / stateSizeUpdateCounter);
                log.debug("Max SW-DBG state size: {}", topologyMaxSWDBGSize);
            }
            log.debug("Availability prediction error EMA: {}", topologyPredictionErrorEMA / (currentTime+1));
            timeSlotPredictionerror = 0;
        }
        else
        {
            timeSlotPredictionerror += predictionError;
            //System.out.println("Prediction error " +timeSlotPredictionerror);
        }
        //System.out.println("ChurnStochastic.java: Average intermediate prediction accuracy " + timeSlotPredictionerror / topologyPredictionTotalSamples);
    }

    public static void updateAverageBucketSize(int bucketSize)
    {
        topologyAverageBucketSize += bucketSize;
    }



    public static void flush()
    {

        overalAveragePredictionError += (double) topologyPredictionErrorEMA / SkipSimParameters.getLifeTime();
        //totalAverageUnpredictableNodes += topologyTotalUnpredicatbleNodes;

        if (stateSizeUpdateCounter != 0)
        {
            totalAverageSWDBGSize += (topologyTotalSWDBGSize / stateSizeUpdateCounter);
            totalMaxSWDBGSize += topologyMaxSWDBGSize;
        }
        if (numberOfIntermediateNodes != 0)
        {
            totalAverageRoutingCandidates += (topologyAverageRoutingCandidates / numberOfIntermediateNodes);
            totalAverageOfflineRoutingCandidates += (topologyAverageOfflineRoutingCandidates / numberOfIntermediateNodes);
            totalAverageBucketSize += (topologyAverageBucketSize / numberOfIntermediateNodes);
        }
        if (totalSuccessSearchPathLength != 0)
        {
            totalAverageSuccessTimeOuts += (topologyTotalSuccessTimeOuts / totalSuccessSearchPathLength);
        }
        if (totalFailedSearchPathLength != 0)
        {
            totalAverageFailureTimeOuts += (topologyTotalFailureTimeOuts / totalFailedSearchPathLength);
        }
        totalAverageResolveFailureTimeOuts += (topologyAverageResolveFailureTimeOuts / topologyAverageLookups);
        totalAverageDepartures += (topologyAverageDepartures / SkipSimParameters.getLifeTime());
        // this.totalAverageArrivals += (topologyAverageArrivals / system.getLifeTime());
        totalAverageSessionLength += (topologyAverageSessionLength / topologyGeneratedSessionLengthes);
        totalAverageInterArrivalTime += (topologyAverageInterArrivalTime / topologyArrivals);
        averageLookups += (topologyAverageLookups / SkipSimParameters.getLifeTime());
        totalAverageOfOnlineNodes += (topologyAverageOfOnlineNodes / SkipSimParameters.getLifeTime());
        totalGeneratedInterArrivalTimes += (topologyArrivals / SkipSimParameters.getLifeTime());
        //this.totalGeneratedSessionLengthes += (topologyGeneratedSessionLengthes / system.getLifeTime());

        log.info("--------------------------------------------------");
        log.info("ChurnStochastics Intermediate Results");
        log.info("Algorithm: {}", SkipSimParameters.getChurnStabilizationAlgorithm());
        log.info("Bucket size: {}", SkipSimParameters.getBackupTableEntrySize());
        log.info("Availability prediction algorithm: {}", SkipSimParameters.getAvailabilityPredictor());
        if (SkipSimParameters.getAvailabilityPredictor().equals(Constants.Churn.AvailabilityPredictorAlgorithm.DBG))
        {
            log.info("State size of DBG: {}", SkipSimParameters.getPredictionParameter());
        }
        else if (SkipSimParameters.getAvailabilityPredictor().equals(Constants.Churn.AvailabilityPredictorAlgorithm.SWDBG))
        {
            log.info("Average SW-DBG state size: {}", topologyTotalSWDBGSize / stateSizeUpdateCounter);
            log.info("Max SW-DBG state size: {}", topologyMaxSWDBGSize);
        }
        log.info("Average prediction error of this topology: {}", topologyPredictionErrorEMA / SkipSimParameters.getLifeTime());
        log.info("--------------------------------------------------");

        topologyAverageInterArrivalTime = 0;
        topologyAverageOfOnlineNodes = 0;
        //topologyAverageArrivals = 0;
        topologyAverageDepartures = 0;
        topologyAverageLookups = 0;
        topologyAverageSessionLength = 0;
        topologyGeneratedSessionLengthes = 0;
        topologyArrivals = 0;
        topologyTotalSWDBGSize = 0;
        stateSizeUpdateCounter = 0;
        topologyAverageResolveFailureTimeOuts = 0;
        topologyAverageRoutingCandidates = 0;
        numberOfIntermediateNodes = 0;
        topologyAverageTimeOuts = 0;
        topologyTotalSuccessTimeOuts = 0;
        topologyTotalFailureTimeOuts = 0;
        topologyAverageOfflineRoutingCandidates = 0;
        totalFailedSearchPathLength = 0;
        totalSuccessSearchPathLength = 0;
        topologyAverageBucketSize = 0;
        timeSlotPredictionerror = 0;
        lastCheckedTime = 0;
        topologyPredictionErrorEMA = 0;
        topologyMaxSWDBGSize = 0;

    }

    public static void printChurnStochastics()
    {
        if (SkipSimParameters.getCurrentTopologyIndex() == SkipSimParameters.getTopologies())
        {
            log.info("---------------------------------------------------------");
            log.info("ChurnStochastics Final Results");
            log.info("Average number of online Nodes at a single time slot: {}", totalAverageOfOnlineNodes / SkipSimParameters.getTopologies());
            log.info("Average number of arrival at each timeslot: {}", totalGeneratedInterArrivalTimes / SkipSimParameters.getTopologies());
            log.info("Average number of departures at each timeslot: {}", totalAverageDepartures / SkipSimParameters.getTopologies());
            log.info("Average session length: {}", totalAverageSessionLength / SkipSimParameters.getTopologies());
            log.info("Average inter arrival time: {}", totalAverageInterArrivalTime / SkipSimParameters.getTopologies());
            log.info("Average number of lookups at each timeslot: {}", averageLookups / SkipSimParameters.getTopologies());
            log.info("Average availability prediction error: {}", overalAveragePredictionError / SkipSimParameters.getTopologies());
            if (SkipSimParameters.getAvailabilityPredictor().equals(Constants.Churn.AvailabilityPredictorAlgorithm.SWDBG))
            {
                log.info("Average SWDBG state size: {}", totalAverageSWDBGSize / SkipSimParameters.getTopologies());
                log.info("Average SWDBG max state size: {}", topologyMaxSWDBGSize / SkipSimParameters.getTopologies());
            }
            log.info("---------------------------------------------------------");

        }
    }

}


