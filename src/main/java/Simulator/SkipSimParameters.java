package Simulator;

import DataTypes.Constants;

public class SkipSimParameters
{

    /**
     * The amount of transactions each node generates per time slot.
     */
    public static int TXB_RATE = 1;
    /**
     * The delay between replication time in dynamic replication when there exists more than a
     * single data owner.
     */
    public static final int REPLICATION_TIME_INTERVAL = 1;
    /**
     * Boolean flag determining whether randomized lookup tests should be performed at the beginning
     * of each time slot.
     */
    public static final boolean RandomizedLookupTests = false;
    /**
     * Boolean flag determining whether the numerical ids of the nodes are computed by a hash
     * function using their indexes as input. Proof-of-Validation and randomized bootstrapping
     * requires this to be true.
     */
    public static final boolean NumIDHashing = true;

    //////////////////////////////////////Consensus/////////////////////////////////////////

    /**
     * ValidatorThreshold determines how many validators will be searched on the Nodes skip-graph
     * for the validator acquisition for transactions (Proof-of-Validation). Also, this determines
     * how many view introducers will be searched on the Nodes skip-graph for randomized bootstrapping.
     */
    public static int ValidatorThreshold = 12;
    /**
     * SignatureThreshold determines how many validators are required to sign a transaction. If an
     * honest node acquires this many honest validators, the valid transaction is successfully validated.
     * If a malicious node acquires this many malicious validators, an attack has been successfully
     * performed by introducing an invalid transaction to the skip-graph.
     *
     * This parameter also determines the amount of honest view introducers required to form a view of
     * the network. This many malicious view introducers will provide an invalid view for the node.
     */
    public static int SignatureThreshold = 1;

    public static int getValidatorThreshold() {
        return ValidatorThreshold;
    }

    public static int getSignatureThreshold() {
        return SignatureThreshold;
    }

    public static void setValidatorThreshold(int validatorThreshold) {
        ValidatorThreshold = validatorThreshold;
    }

    public static void setSignatureThreshold(int signatureThreshold) {
        SignatureThreshold = signatureThreshold;
    }

    /**
     * The fraction of malicious nodes to all the nodes. The nodes that have an index in [0, MaliciousFraction*SystemCapacity)
     * will be chosen as malicious.
    */

    public static float MaliciousFraction = 0.33f;

    public static void setMaliciousFraction(float maliciousFraction) {
        MaliciousFraction = maliciousFraction;
    }

    public static String BlockchainProtocol = Constants.Protocol.LIGHTCHAIN;

    public static void setBlockchainProtocol(String protocol) {
        BlockchainProtocol = protocol;
    }

    //////////////////////////////////////View/////////////////////////////////////////

    /**
     *  At the view layer, we aim to acquire SignatureThreshold many honest nodes from a random-oracle model that can
     *  provide views for a new node that is introduced to the system.
     */
    public static final boolean RandomizedBootstrapping = true;

    //////////////////////////////////////Experiments/////////////////////////////////////////

    /**
     * This Proof-of-Validation experiment measures the probability of acquiring SignatureThreshold many malicious
     * validators out of ValidatorThreshold many searches over the nodes skip-graph during validator
     * acquisition for a transaction.
     */
    public static boolean MaliciousSuccessExperiment = false;
    /**
     * This Proof-of-Validation experiment measures the average number of honest nodes that can be acquired during
     * validator-acquisition for a transaction.
     */
    public static boolean EfficiencyExperiment = false;
    /**
     * This Proof-of-Validation experiment measures the average number of online replicas a transaction has at any time
     * during the simulation. The honest validators of an honest transaction are assumed to be replicas in this experiment.
     */
    public static boolean AvailabilityExperiment = true;
    /**
     * This experiment measures the chance of a node being online at any time slot. This directly depends on the churn
     * model that was selected for the simulation.
     */
    public static boolean OnlineProbabilityExperiment = false;
    /**
     * This randomized bootstrapping experiment measures the probability of acquiring SignatureThreshold many malicious
     * view introducers out of ValidatorThreshold many searches over the nodes skip-graph when a node goes online.
     */
    public static boolean btsMaliciousSuccessExperiment = false;
    /**
     * This randomized bootstrapping experiment measures the average number of honest view introducers a node acquires
     * when it goes online in the system.
     */
    public static boolean btsEfficiencyExperiment = false;

    //////////////////////////////////////Evaluations/////////////////////////////////////////
    /**
     * Flag to enable the evaluation of locality awareness of replication. If enabled, reports the
     * average latency between each data requester and its closest replica
     */
    protected static boolean ReplicationLocalityAwarenessEvaluation = false;

    /**
     * Boolean variable determining whether or not to do the name ID assignment evaluation. You should make it true
     * specially if you do randomized searches for name or numerical IDs. It then mesuares the average latency between
     * each Node and its neighbors, as well as the average end to end latency of searches
     */
    protected static boolean NameIDLocalityAwarenessEvaluatgion = false;
    /**
     * Boolean variable determining whether or not to do the replication load evaluation. You should make it true
     * when you have a replication and want to see the load of replicas. By the load we mean the average replication
     * duties assigned to each replica. It then mesuares the average load on the replicas.
     */
    protected static boolean ReplicationLoadEvaluation = false;

    /**
     * Boolean variable determining whether or not to do the replication availability evaluation. You should make it true when
     * you have a replication and want to see the average number of available replicas, per each data owner, at each
     * time slot.
     */
    protected static boolean ReplicationAvailabilityAwarenessEvaluation = false;

    /**
     * Boolean variable determining whether or not to do the replication QoS evaluation. You should make it true when
     * you have a dynamic replication and you want to see the average QoS, per each data owner, at each time slot.
     * When it is true, it means that the nodes are heterogeneous with respect to their bandwidth and storage capacity.
     * Otherwise, when it is false, it means that all the nodes are having the homogeneous bandwidth and capacity, and
     * hence, it does not evaluate the nodes with anything with respect to the average available bandwidth etc.
     */
    protected static boolean Heterogeneous = false;

    public static boolean isHeterogeneous()
    {
        return Heterogeneous;
    }

    public static boolean isReplicationAvailabilityAwarenessEvaluation()
    {
        return ReplicationAvailabilityAwarenessEvaluation;
    }

    public static boolean isNameIDLocalityAwarenessEvaluatgion()
    {
        return NameIDLocalityAwarenessEvaluatgion;
    }

    public static boolean isReplicationLocalityAwarenessEvaluation()
    {
        return ReplicationLocalityAwarenessEvaluation;
    }

    public static boolean isReplicationLoadEvaluation()
    {
        return ReplicationLoadEvaluation;
    }
    //////////////////////////////////////System//////////////////////////////////////////////
    /**
     * The debug mode of the SkipSim, being enabled to true, it shows the debug messages
     */
    protected static boolean LOG = false;


    /**
     * SimulationType can be:
     * Static: no time
     * Dynamic: with time slots
     * Blockchain: it is with time slots supported and two overlays one for Skip Graph of Nodes, one for Skip Graph
     * of the
     * transactions and blocks
     * Options should always be selected from
     */
    protected static String SimulationType = Constants.SimulationType.DYNAMIC;


    /**
     * SystemCapacity denotes the number of Nodes in the system it should be non-negative and greater than zero
     */
    protected static  int SystemCapacity = 1024;

    public static void setSystemCapacity(int capacity) {
        SystemCapacity = capacity;
    }

    /**
     * LandmarksNum denotes the number of landmarks in the system, it is recommended to be equal to
     * Log(SystemCapacity) in base 2 e.g., SystemCapacity = 1024 then LandmarkNum = 10
     */
    protected static  int LandmarksNum = 10;

    /**
     * NameIDLength is the minimum length of the name IDs of the Nodes, similar to LandmarksNum it is recommended to
     * keep it as Log(SystemCapacity) in base 2 e.g., SystemCapacity = 1024 then LandmarkNum = 10
     */
    protected static  int NameIDLength = 10;

    /**
     * Lookup table size is the size of lookup table of the Nodes. It it determined automatically by the
     * name ID assignment approach
     */
    protected static int LookupTableSize =
            (int) Math.ceil(NameIDLength + (Math.log(SkipSimParameters.getSystemCapacity()) / Math.log(2)));

    /**
     * In case it set to true, each region of the system has its own inter-arrival time
     */
    protected static boolean MultipleInterArrivalDistribution = false;

    public static boolean isMultipleInterArrivalDistribution()
    {
        return MultipleInterArrivalDistribution;
    }

    public static boolean isLog()
    {
        return LOG;
    }

    public static String getSimulationType()
    {
        return SimulationType;
    }

    public static void setSimulationType(String simulationType)
    {
        SkipSimParameters.SimulationType = simulationType;
    }


    public static void incrementSimIndex()
    {
        CurrentTopologyIndex++;
    }


    public static int getCurrentTopologyIndex()
    {
        return CurrentTopologyIndex;
    }

    public static int getTopologies()
    {
        return Topologies;
    }

    public static int getDomainSize()
    {
        return DomainSize;
    }

    public static int getNameIDLength()
    {
        return NameIDLength;
    }

    public static int getLandmarksNum()
    {
        return LandmarksNum;
    }

    /**
     * @return the system capacity i.e., the number of Nodes in the system. By the
     * number of Nodes, we mean the total number, despite of their online/offline status
     */
    public static int getSystemCapacity()
    {
        return SystemCapacity;
    }

    /**
     * @return number of rows in the lookup table of the Nodes
     * The number of columns is always fixed to 2 i.e., right and left
     */
    public static int getLookupTableSize()
    {
        return LookupTableSize;
    }

    ///////////////////////Dynamic SimulationParameters///////////////////////////////////////////
    /*
    The followings are the simulation parameters that are common between all the dynamic simulations of SkipSim
    They are considered when the SimulationType is Dynamic
     */
    /**
     * System life time, i.e., the total number of time slots of the simulation
     */
    protected static int LifeTime = 72;

    public static void setLifeTime(int lifeTime) {
        LifeTime = lifeTime;
    }

    public static void setChurnModel(String churnModel) {
        switch(churnModel) {
            case Constants.Churn.Model.Debian.Fast.Name:
                SessionLengthScaleParameter = Constants.Churn.Model.Debian.Fast.SessionLength.Scale;
                SessionLengthShapeParameter = Constants.Churn.Model.Debian.Fast.SessionLength.Shape;
                InterarrivalScaleParameter = Constants.Churn.Model.Debian.Fast.SessionInterarrival.Scale;
                InterarrivalShapeParameter = Constants.Churn.Model.Debian.Fast.SessionInterarrival.Shape;
            break;
            case Constants.Churn.Model.Debian.Slow.Name:
                SessionLengthScaleParameter = Constants.Churn.Model.Debian.Slow.SessionLength.Scale;
                SessionLengthShapeParameter = Constants.Churn.Model.Debian.Slow.SessionLength.Shape;
                InterarrivalScaleParameter = Constants.Churn.Model.Debian.Slow.SessionInterarrival.Scale;
                InterarrivalShapeParameter = Constants.Churn.Model.Debian.Slow.SessionInterarrival.Shape;
            break;
            case Constants.Churn.Model.Flatout.Name:
                SessionLengthScaleParameter = Constants.Churn.Model.Flatout.SessionLength.Scale;
                SessionLengthShapeParameter = Constants.Churn.Model.Flatout.SessionLength.Shape;
                InterarrivalScaleParameter = Constants.Churn.Model.Flatout.SessionInterarrival.Scale;
                InterarrivalShapeParameter = Constants.Churn.Model.Flatout.SessionInterarrival.Shape;
            break;
        }
    }

    /**
     * Intterarrival distribution is the distribution of the time between two consecutive arrivals
     * to the system
     * The shape parameter of the Weibull interarrival time distribution
     * This parameter is ONLY effective when we are generating a new topology, and not
     * when you are loading a topology.
     */
    protected static  double InterarrivalShapeParameter;
    /**
     * Intterarrival distribution is the distribution of the time between two consecutive arrivals
     * to the system
     * The scale parameter of the Weibull interarrival time distribution
     * This parameter is ONLY effective when we are generating a new topology, and not
     * when you are loading a topology.
     */
    protected static  double InterarrivalScaleParameter;

    /**
     * Session length distribution is the distribution of the time that a Node is online in the system
     * to the system
     * The shape parameter of the Weibull session length distribution
     * This parameter is ONLY effective when we are generating a new topology, and not
     * when you are loading a topology.
     */
    protected static  double SessionLengthShapeParameter;

    /**
     * Session length distribution is the distribution of the time that a Node is online in the system
     * to the system
     * The scale parameter of the Weibull session length distribution
     * This parameter is ONLY effective when we are generating a new topology, and not
     * when you are loading a topology.
     */
    protected static  double SessionLengthScaleParameter;


    public static double getInterarrivalShapeParameter()
    {
        return InterarrivalShapeParameter;
    }

    public static double getInterarrivalScaleParameter()
    {
        return InterarrivalScaleParameter;
    }

    public static double getSessionLengthShapeParameter()
    {
        return SessionLengthShapeParameter;
    }

    public static double getSessionLengthScaleParameter()
    {
        return SessionLengthScaleParameter;
    }

    public static int getLifeTime()
    {
        return LifeTime;
    }

    ///////////////////////Topology Generation Parameters/////////////////////////////////////////
    /**
     * Distributes the Nodes in the simulation domain either based on their position to the LANDMARKS or UNIFORMLY
     * All options available at Constants.Topology.GENERATION_TYPE.
     */
    protected static String NodeGenerationStrategy = Constants.Topology.GENERATION_TYPE.LANADMARK;
    /**
     * DomainSize denotes the side size of the topology, the topology diameter would be then DomainSize \time Sqrt(2)
     */
    protected static  int DomainSize = 7000;

//    /*
//    The following parameters are internal parameters of SkipSim on
//    Generating a new topology, and distributing the Nodes according to the placement
//    of the landmarks
//     */
//    protected static  int UniformLandmarkThereshould = 50;
//    protected static  int UniformSumOfLandmarkThereshould = 1000;
//    protected static  int LandmarkBasedLandmarkThereshould = 5;
//    protected static  int LandmarkBasedSumOfLandmarkThereshould = 200;

    /**
     * Total number of the topologies that the simulation is executed over
     */
    protected static int Topologies;

    public static void setTopologies(int topologies) {
        Topologies = topologies;
    }

    /**
     * Index of the current topology under simulation
     */
    protected static int CurrentTopologyIndex;
//    protected static boolean maltabRepAutoMeanSDEvaluationInit = true;

    /**
     * @return SkipGraph.Node Generation
     */
    public static String getNodeGenerationStrategy()
    {
        return NodeGenerationStrategy;
    }

    /**
     * If it is true, the replication algorithm tries to find the minimum number of replicas to bound the maximum
     * access delay denoted by ReplicationDegree by
     * a bound, otherwise it just reports the average access delay based on the selected ReplicationDegree as the
     * replication degree.
     */
    protected static boolean delayBasedSimulaton = false;
    /**
     * The initial replication degree to start with, only is used in delay based replication
     */
    protected static  int initialReplicationDegree = 1;
    /**
     * The delay bound which is used in the delay based replication
     */
    protected static  double DelayBound = 1500;
    ////////////////////////////////////////////Name ID assignment strategy////////////////////////////////////////////
    /**
     * The name ID assignment algorithm of the Nodes read our DPAD paper for more details
     */
    protected static  String NameIDAssignment = Constants.NameID.LANS;

    /**
     * @return returns the name ID assignment strategy name
     */
    public static String getNameIDAssignment()
    {
        return NameIDAssignment;
    }

    /**
     * Number of to be performed search by numerical IDs to evaluate the locality awareness of the name ID
     */
    protected static  int SearchByNumericalID = 0;
    /**
     * Number of to be performed search by name IDs to evaluate the locality awareness of the name ID
     */
    protected static int SearchByNameID = 0;


    /**
     * @return number of randomized search for name IDs to evaluate the locality awareness of name IDs
     */
    public static int getSearchByNameID()
    {
        return SearchByNameID;
    }

    /**
     * @return number of randomized search for numerical IDs to evaluate the locality awareness of name IDs
     */
    public static int getSearchByNumericalID()
    {
        return SearchByNumericalID;
    }

    ////////////////////////////////////////////Replication Setup//////////////////////////////////////////////////////

    /**
     * The search for utility alpha. This corresponds to the number of name IDs that the search traverse to pick the best
     * one based on Utility. It is used as part of Pyramid only.
     */
    protected static int sSearchForUtilityAlpha = 3;

    public static int getSearchForUtilityAlpha()
    {
        return sSearchForUtilityAlpha;
    }

    /**
     * Replication type: public; all Nodes are data requesters, protected; we have a specified set of data rquesters
     */
    protected static  String ReplicationType = Constants.Replication.Type.PUBLIC;

    /**
     * The replication algorithm name, the names are available in Constants.Replication.Algorithms.
     */
    protected static  String ReplicationAlgorithm = Constants.Replication.Algorithms.NONE;
    /**
     * Total number of replicas for each data owner. This is applicable in cases where we are interested to see the
     * effect
     * of a replication degree in our performance
     */
    protected static int ReplicationDegree = 0;

    /**
     * Total number of data requesters in case that the replication is protected see LARAS paper for more details
     */
    protected static  int DataRequesterNumber = 0;

    public static String getReplicationAlgorithm()
    {
        return ReplicationAlgorithm;
    }

    public static boolean isDelayBasedSimulaton()
    {
        return delayBasedSimulaton;
    }


    public static int getInitialReplicationDegree()
    {
        return initialReplicationDegree;
    }

    /**
     * @return retplication type: PUBLIC or protected
     */
    public static String getReplicationType()
    {
        return ReplicationType;
    }


    public static double getDelayBound()
    {
        return DelayBound;
    }


    /**
     * @return DataRequesterNumber
     */
    public static int getDataRequesterNumber()
    {
        return DataRequesterNumber;
    }

    /**
     * @return ReplicationDegree
     */
    public static int getReplicationDegree()
    {
        return ReplicationDegree;
    }


    /////////////////////////////////////////Aggregation///////////////////////////////////////////////////////////////
    protected static String AggregationAlgorithm = Constants.Aggregation.Algorithms.NONE;
    public static String getAggregationAlgorithm()
    {
        return AggregationAlgorithm;
    }

    //////////////////////////////////////////Dynamic Simulation/Replication///////////////////////////////////////////
    /**
     * Fixed periodic time interval from Awake paper. For example, if sFPTI = 24, then the dynamic simulation
     * considers that Node's availability and replication availability are evaluated and reported in each hour of the
     * day
     * In general, SkipSim always have 1 hours time slot. So, sFPTI = 48 evaluates everything related to the dynamic
     * replication
     * within cycles of two days e.g., availability of replicas within each hours in periods of two days. Read Awake
     * paper
     * for more intution of this. I have recorded presentation of Awake at the following YouTube link
     * https://www.youtube.com/watch?v=aKGd890aGU4
     */
    protected static int sFPTI = 24;

    /**
     * AvailabilityAggregationDomainSize corresponds to the prefix length of the name IDs in the blockchain-based
     * aggregation of QoS and availability. It is used in dynamic replication, and in specific in BlockchainAvailabilityAggregation
     */
    protected static int AvailabilityAggregationDomainSize = -1;

    /**
     * Number of data owners for multi-objective replication that considers the load balancing
     * in other approaches, it may leave 1 for simulations of papers like Awake and LARAS
     */
    protected static int sDataOwnerNumber = 1;

    /**
     * @return Checks the dynamic type of the replication. In general we consider to do dynamic replication, if we are
     * simulating in dynamic replication type.
     */
    public static boolean isDynamicReplication()
    {
        return SimulationType.equalsIgnoreCase(Constants.SimulationType.DYNAMIC);
    }

    public static int getFPTI()
    {
        return sFPTI;
    }

    public static int getDataOwnerNumber()
    {

        return sDataOwnerNumber;
    }


    /**
     * Replica load corresponds to the storage load constraint of the replicas, and is the number of replication
     * duties that each node can have from different data owners. If replica load is 2 for example, it means that
     * each node can be selected by at most two data owners as replicas of them.
     */
    protected static int sStorageCapacity = -1;



    public static int getStorageCapacity()
    {
        return sStorageCapacity;
    }

    /**
     * Bandwidth capacity corresponds to the bandwidth limit of the nodes. The bandwidth of the nodes in SkipSim follows
     * and exponential distribution between [0,1] where the sBandwidthCapacityRate corresponds to the rate of the
     * exponential distribution
     */
    protected static double sBandwidthCapacityRate = -1;

    public static double getBandwidthCapacityRate()
    {
        return sBandwidthCapacityRate;
    }

    /**
     * The time which the (first) data owner starts its replication
     */
    protected static int ReplicationTime = -1;


    //    public static void setDataOwnerNumber(int dataOwnerNumber)
//    {
//        if(sDataOwnerNumber == -1)
//            throw new IllegalStateException("Wrong number of data owners for replication. It should be at least 1
// for the replication to proceed. Check config.txt");
//        sDataOwnerNumber = dataOwnerNumber;
//    }





//    public static void setLifeTime(int t)
//    {
//        if (t > 0)
//        {
//            LifeTime = t;
//        }
//        else
//        {
//            System.out.println("Wrong value has been selected as the life time");
//            System.exit(0);
//        }
//    }

    public static int getReplicationTime()
    {
        return ReplicationTime;
    }

//    public static void setReplicationTime(int inputReplicationTime)
//    {
//        if (inputReplicationTime > 0)
//            ReplicationTime = inputReplicationTime;
//    }


    ////////////////////////////////////////Churn Stabilization/////////////////////////////////////////////////////////
    /**
     * Churn type can be either COOPERATIVE i.e., Nodes inform their neighbors upon departure
     * OR
     * ADVERSARIAL i.e., Nodes do not inform their neighbors upon departure
     */
    protected static String ChurnType = Constants.Churn.Type.COOPERATIVE;

    public static void setChurnType(String churnType) {
        ChurnType = churnType;
    }

    /**
     * The name of churn stabilization algorithm.
     * All the options are available at Constants.Churn.ChurnStabilizationAlgorithm
     */
    protected static String ChurnStabilizationAlgorithm = Constants.Churn.ChurnStabilizationAlgorithm.NONE;
    /**
     * The backup table size in case that the churn type is adversarial, and we are using a churn stabilization
     * algorithm
     * enabled. It denotes the backup table entry set size as we specified in the Interlaced paper. For example, a
     * value of
     * 2 for the BackupTableEntrySize means that each entry of the backup table is a set of size 2.
     */
    protected static  int BackupTableEntrySize = 1;

    /**
     * The availability predictor algorithm that is used for the sake of churn stabilization. More details are
     * available in
     * our interlaced paper. The list of options available at Constants.Churn.AvailabilityPredictorAlgorithm
     */
    protected static  String AvailabilityPredictor = Constants.Churn.AvailabilityPredictorAlgorithm.NONE;

    /**
     * The state size of the Bruijn graph, it is applicable only when we have DBG as the availability predictor
     */
    protected static  int BuijnGraphStateSize = 1;

    public static int getBackupTableEntrySize()
    {
        return BackupTableEntrySize;
    }

    /**
     * @return name of the churn stabilization algorithm under simulation
     */
    public static String getChurnStabilizationAlgorithm()
    {
        return ChurnStabilizationAlgorithm;
    }


//    public static void setPredictionParameter(int predictionParameter)
//    {
//        PredictionParameter = predictionParameter;
//    }

    /**
     * @return name of the availability predictor algorithm
     */
    public static String getAvailabilityPredictor()
    {
        return AvailabilityPredictor;
    }

    /**
     * @return the churn type which is either cooperative or adversarial
     */
    public static String getChurnType()
    {
        return ChurnType;
    }

    public static int getPredictionParameter()
    {
        return BuijnGraphStateSize;
    }

    public static void setReplicationDegree(int replicationDegree)
    {
        if(isDelayBasedSimulaton())
        {
            ReplicationDegree = replicationDegree;
        }
        else
        {
            throw new IllegalStateException("SkipSimParameters: Attempt to change the replication degree in the middle of a non-delay-based simulation");
        }
    }

    ///////////////////////////////////Setters/////////////////////////////////////////////////////////////////////////

    public static double getStandardDeviation(int[] inputArray, double inputArrayAverage)
    {
        double SD = 0;
        for (int i = 0; i < inputArray.length; i++)
        {
            SD += Math.pow(inputArray[i] - inputArrayAverage, 2);
        }
        SD = SD / SkipSimParameters.getTopologies();
        SD = Math.sqrt(SD);

        return SD;
    }
    public static double getStandardDeviation(double[] inputArray, double inputArrayAverage)
    {
        double SD = 0;
        for (int i = 0; i < inputArray.length; i++)
        {
            SD += Math.pow(inputArray[i] - inputArrayAverage, 2);
        }
        SD = SD / SkipSimParameters.getTopologies();
        SD = Math.sqrt(SD);

        return SD;
    }

    public static boolean isPublicReplication()
    {
        return getReplicationType().equalsIgnoreCase(Constants.Replication.Type.PUBLIC);
    }

    public static boolean isStaticSimulation()
    {
        return SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.STATIC);
    }

    public static int getAvailabilityAggregationDomainSize()
    {
        return AvailabilityAggregationDomainSize;
    }
}


