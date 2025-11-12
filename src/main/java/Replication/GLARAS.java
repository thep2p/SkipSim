package Replication;

import Simulator.SkipSimParameters;
import SkipGraph.Node;
import SkipGraph.SkipGraphOperations;
import net.sf.javailp.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.IntStream;


public class GLARAS extends LARAS
{
    private static final Logger log = LoggerFactory.getLogger(GLARAS.class);
    /**
     * The initial size of the virtual system, it is normally one and grows as the bad candidates are being removed from
     * GLARAS, however, if you are running on a strong machine, you may choose it in bigger size, the bigger size implies
     * higher precision in solving the replication
     */
    public static final int VIRTUAL_SYSTEM_INITIAL_SIZE = 1;

    /**
     * W1: is the average weight of the system wide distribution (SWD) of GLARAS on the estimate of the number of data requesters per region
     */
    private static double averageW1 = 0;
    /**
     * W2: is the weight of the system wide distribution (SWD) of GLARAS on minimum latency between the landmark of each region, and the
     * closest region to it with non-zero sub replication degree
     */
    private static double averageW2 = 0;
    /**
     * W3: is the weight of the system wide distribution (SWD) of GLARAS on the number of the nearby regions to each region of the system
     */
    private static double averageW3 = 0;

    private static double numberOfIterations = 0;

    /**
     * weightHistogram is not a part of GLARAS, rather is defined in the research phase over GLARAS to find the best
     * distribution of weights w1, w2, and w3 of SWD, in a way that it yields in the optimal average access delay.
     * weightHistogram[i][j] denotes the number of topologies that could achieve the minimum average access delay by
     * w1 = i, w2 = j, and w3 = 100 - w1 - w2
     */
    private static int[][] weightHistorgam = new int[100][100];
    private ArrayList<String> badCandidates;
    private int[] SubProblemRepSet = new int[SkipSimParameters.getSystemCapacity()];
    private boolean[] RepSet = new boolean[SkipSimParameters.getSystemCapacity()];
    private double[] RepAccuracy = new double[SkipSimParameters.getSystemCapacity()];

    @Override
    public void Algorithm(SkipGraphOperations inputSgo, int dataOwnerIndex)
    {
        sgo = inputSgo;
        resetRep();
        //reset();
        dataRequesterPopulation();
        //oneRegionRepShare();
        SWD(32.01, 33.88, 34.11, null);
        //improvedRepShare(Simulator.system.PUBLIC_REPLICATION);
        //adaptiveSubproblemSizeDefining(128);
        //tablesInit();
        //replicaSetInit();

        //double averageAccessDelay = bruteForceOnWeights();
        //double averageAccessDelay = replicationDegreeDistributionBruteForce();
        RWD(dataOwnerIndex);
    }

    /**
     * This function is not part of the main implementation of GLARAS.
     * Rather, it is solely being used to find the best distribution of replication degree among the regions
     *
     * @return minimum aveage accesss delay of RWD that is caused by the best distribution of replication degree
     * among the regions.
     */
    private double replicationDegreeDistributionBruteForce()
    {
        if (!SkipSimParameters.isPublicReplication())
        {
            throw new IllegalStateException("GLARAS: replicationDegreeDistributionBruteForce should be only invoked in public replication type: found private replication type");
        }
        printLandmarkPairwiseLatency();
        Map<String, Integer> resultTable = new Hashtable<>();
        try
        {
            File file = new File("regionZeroBruteforce.txt");
            PrintWriter w = new PrintWriter(file, "UTF-8");
            int counter = 0;
            double bestAverageAccessDelay = Double.MAX_VALUE;
            String bestReplicaAssignment = new String();
            /*
            i in the following loop determines a binary string representation of the surplus replication degree among
            the regions e.g., assume that we have 4 regions (landmarks) then 1001 denotes that the first and forth regions
            should receive the subreplication degree of 1 and the rest of regions nothing
             */
            for (int i = 0; i < Math.pow(2, SkipSimParameters.getLandmarksNum()); i++)
            {
                sgo.getTG().mNodeSet.renewReplicationInfo();

                for (int k = 0; k < SkipSimParameters.getLandmarksNum(); k++)
                {
                    setSubReplicationDegree(k, 0);
                }

                /*
                Converting i to string representation
                 */
                String representation = Integer.toBinaryString(i);

                /*
                Checks to see if i string represents a valid distribution (i.e., number of 1s is equal to the replication degree.
                 */
                if (org.apache.commons.lang3.StringUtils.countMatches(representation, "1") == SkipSimParameters.getReplicationDegree())
                {
                    /*
                    pads the string from left by 0s to have exactly the length equal to the number of regions
                     */
                    while (representation.length() < SkipSimParameters.getLandmarksNum())
                        representation = "0" + representation;

                    /*
                    distributes the replication share among the regions i.e., the jth region gets sub-replication
                    degree of 1 if and only if the jth character of the binary representation string is 1.
                     */
                    for (int j = 0; j < SkipSimParameters.getLandmarksNum(); j++)
                    {
                        if (Character.getNumericValue(representation.charAt(j)) == 1)
                            setSubReplicationDegree(j, getSubReplicationDegree(j) + 1);
                    }

                    /*
                    Employes the region wide distribution of replicas i.e., RWD on the distributed sub-replication degree
                    among the regions, and seeks obtained average access delay of replication
                     */
                    RWD(0);
                    double accesDelay = averageAccessDelay(sgo.getTG().mNodeSet, 0);
                    /*
                    Keeps the record of the best assignment of replicas as well as the best obtained average access delay
                     */
                    if (accesDelay < bestAverageAccessDelay)
                    {
                        bestAverageAccessDelay = accesDelay;
                        bestReplicaAssignment = representation;
                    }
                    counter++;
                    log.debug("Run {} - average access delay for repshare assignment {}: {}", counter, representation, (int) accesDelay);
                    w.println("Run of " + counter + " the average access delay for repshare assignment of " + representation + " is " + (int) accesDelay);
                    w.println("***********************************************************************");
                    resultTable.put(representation, (int) accesDelay);
                }
            }
            w.println("************************************************************************");
            w.println("****************The optimal result**************************************");
            w.println("************************************************************************");
            for (int j = 0; j < SkipSimParameters.getLandmarksNum(); j++)
            {
                setSubReplicationDegree(j, Character.getNumericValue(bestReplicaAssignment.charAt(j)));
            }

            /*
            Traversing the hash map
             */
            Iterator it = resultTable.entrySet().iterator();
            while (it.hasNext())
            {
                Map.Entry<String, Integer> pair = (Map.Entry) it.next();
                if (pair.getValue().intValue() == (int) bestAverageAccessDelay)
                {
                    log.debug("Similar accuracy to optimal - assignment: {}, access delay: {}", pair.getKey(), pair.getValue().intValue());
                    w.println("Simialr accuracy to optimal " + pair.getKey() + " access delay " + pair.getValue().intValue() + "\n\n");
                }
            }


            w.close();
            return bestAverageAccessDelay;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return -Double.MIN_VALUE;
    }

    /**
     * Bruteforces over w1, w2, and w3 of the SWD to find the optimal value of the weights
     *
     * @param dataOwnerIndex
     * @return
     */
    private double bruteForceOnWeights(int dataOwnerIndex)
    {
        /*
        results[i][j] is the average access delay obtained by performing SWD on w1 = i, w2 = j, and w3 = 100 - i - j
         */
        double[][] results = new double[100][100];
        if (SkipSimParameters.getCurrentTopologyIndex() == 1)
        {
            for (int i = 0; i < 100; i++)
                Arrays.fill(weightHistorgam[i], 0);
        }
        double bestAverageAccessDelay = Double.MAX_VALUE;
        double bestW1 = 0;
        double bestW2 = 0;
        double bestW3 = 0;

        /*
        Bruteforcing over w1, w2, and w3.
         */
        for (double w1 = 0; w1 < 100; w1++)
            for (double w2 = 0; w2 < 100 - w1; w2++)
            {
                double w3 = 100 - w2 - w1;
                /*
                Resetting the replication information for the new experiment
                 */
                sgo.getTG().mNodeSet.renewReplicationInfo();
                for (int k = 0; k < SkipSimParameters.getLandmarksNum(); k++)
                {
                    setSubReplicationDegree(k, 0);
                }

                SWD(w1 / 100, w2 / 100, w3 / 100, null);
                RWD(dataOwnerIndex);
                double averageAccessDelay = averageAccessDelay(sgo.getTG().mNodeSet, 0);
                results[(int) w1][(int) w2] = averageAccessDelay;
                if (averageAccessDelay < bestAverageAccessDelay)
                {
                    bestAverageAccessDelay = averageAccessDelay;
                    bestW1 = w1;
                    bestW2 = w2;
                    bestW3 = w3;
                }
                log.debug("Weights - W1: {}, W2: {}, W3: {}, Avg delay: {}, Min avg: {}", w1, w2, w3, averageAccessDelay, bestAverageAccessDelay);
            }


        for (int i = 0; i < 100; i++)
            for (int j = 0; j < 100; j++)
            {
                if (Math.abs(results[i][j] - bestAverageAccessDelay) < 0.01 * bestAverageAccessDelay)
                {
                    weightHistorgam[i][j]++;
                }
            }
        averageW1 += bestW1;
        averageW2 += bestW2;
        averageW3 += bestW3;

        if (SkipSimParameters.getCurrentTopologyIndex() == SkipSimParameters.getTopologies())
        {
            log.info("Best average weights - W1: {}, W2: {}, W3: {}",
                    averageW1 / SkipSimParameters.getTopologies(),
                    averageW2 / SkipSimParameters.getTopologies(),
                    averageW3 / SkipSimParameters.getTopologies());

            for (int i = 0; i < 100; i++)
                for (int j = 0; j < 100; j++)
                {
                    if (100 - i - j >= 0)
                        log.debug("Weight histogram - ({}, {}, {}, {})", i, j, 100 - i - j, weightHistorgam[i][j]);
                }

        }

        return bestAverageAccessDelay;


    }

    /**
     * The region-wide optimization of GLARAS, distributes the replicas for the data owner over the defined region
     * @param dataOwnerIndex
     * @return
     */
    private void RWD(int dataOwnerIndex)
    {
        /*
        Initial logarithm of the virtual system size
         */
        int virtualSystemSizePower = VIRTUAL_SYSTEM_INITIAL_SIZE;
        log.info("RWD (Replica with Distance) of GLARAS started");

        /*
        Number of created replicas
         */
        int repCounter = 0;

        /*
        Number of times RWD iterates over ILP until it makes the replicas for this region
         */
        int loopCounter = 0;

        /*
        Iterating over all the regions of the system and placing the replicas accordingly
         */
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            virtualSystemSizePower = VIRTUAL_SYSTEM_INITIAL_SIZE;
            badCandidates = new ArrayList<>();

            if (getSubReplicationDegree(i) == 0) continue;


            boolean[] BestRepSet = new boolean[SkipSimParameters.getSystemCapacity()];
            double[] BestRepAccuracy = new double[SkipSimParameters.getSystemCapacity()];
            int[] BestSubProblemRepSet = new int[SkipSimParameters.getSystemCapacity()];
            double BestAccuracy = -1;
            Arrays.fill(BestSubProblemRepSet, 0);
            Arrays.fill(BestRepAccuracy, 0);
            Arrays.fill(BestRepSet, false);
            double accuracy;

            int virtualSystemSize = 0;
            /*
            The maximum size that the virtual system can grow to, we consider it as the expected number of nodes in each region
             */
            int extremeVirturalSystemSize = SkipSimParameters.getSystemCapacity() / SkipSimParameters.getLandmarksNum();

            int oldBadCandidateSize = -1;
            do
            {
                loopCounter++;
                virtualSystemSize = virtualSystemSizeSet((int) Math.pow(2, virtualSystemSizePower), i);
                /*
                Terminate iterating over ILP if we passed beyond the extreme size
                 */
                if (virtualSystemSize > extremeVirturalSystemSize)
                {
                    virtualSystemSize /= 2;
                    break;
                }

                /*
                Generating the name ID distance table as full initially based on the size of virtual system
                 */
                int[][] nameIDTable = virtualSystemNameIDTable(virtualSystemSize);


                /*
                Modeling and solving the region wide distribution of replicas over this region i of the system
                 */
                Result result = ILP(nameIDTable, virtualSystemSize, getSubReplicationDegree(i), i, badCandidates);
                accuracy = replicaSetGenerator(result, virtualSystemSize, i, dataOwnerIndex);

                /*
                Keeping the track of the replicas set that yeailds to the best accuracy
                 */
                if (virtualSystemSizePower * accuracy > BestAccuracy)
                {
                    BestAccuracy = accuracy * virtualSystemSizePower;
                    System.arraycopy(RepSet, 0, BestRepSet, 0, SkipSimParameters.getSystemCapacity());
                    System.arraycopy(RepAccuracy, 0, BestRepAccuracy, 0, SkipSimParameters.getSystemCapacity());
                }

                /*
                marking the replicas with mapping accuracy less than 0.99 as bad candidates
                 */
                refineBadCandidates(virtualSystemSize);

                /*
                Terminate the iteration over ILP for this region if we have no progress,
                 and no new bad candidates have been generated due to the iteration over ILP
                 */
                if (accuracy < 0 || oldBadCandidateSize == badCandidates.size())
                {
                    break;
                }
                /*
                In case bad candidates size grows beyond the half of the virtual system size, the virtual system size
                is doubled in size by extending the bad candidates set, the zero size condition is to initially boost up
                the extension towards the larger sizes.
                 */
                else if (badCandidates.size() >= (virtualSystemSize / 2) || badCandidates.size() == 0)
                {
                    if (badCandidates.size() > 0) oldBadCandidateSize = badCandidates.size();
                    badCandidates = expandBadCandidates();
                    virtualSystemSizePower++;
                }
                else
                {
                    break;
                }

            } while (true);



            /*
            Placing the replicas from the best replica generated set on the nodes of original system
             */
            for (int j = 0; j < SkipSimParameters.getSystemCapacity(); j++)
            {
                if (BestRepSet[j])
                {
                    boolean replicationResult = ((Node) sgo.getTG().mNodeSet.getNode(j)).setAsReplica(dataOwnerIndex);
                    if (replicationResult)
                    {
                        repCounter++;
                        log.debug("Replica selected - Name ID: {}, Node: {}", ((Node) sgo.getTG().mNodeSet.getNode(j)).getNameID(), j);
                    }
                    else
                    {
                        throw new IllegalStateException("GLARAS: duplicative replica detected for the same data owner " + dataOwnerIndex + " , on the same node " + j);
                    }
                }

            }
            log.debug("Sub-replication degree: {}", getSubReplicationDegree(i));
            log.debug("----------------------------------------------------------------------");

        }
        /*
        Keeping record of average number of iterations per each replica placement
         */
        numberOfIterations += (double) loopCounter / SkipSimParameters.getReplicationDegree();
        if (repCounter != SkipSimParameters.getReplicationDegree())
        {
            throw new IllegalStateException("GLARAS: Error in the number of placed replicas, the degree is: " + SkipSimParameters.getReplicationDegree() + " but only " + repCounter + " replicas where made");
        }
        log.info("GLARAS completed - total replicas created: {}", repCounter);
        sgo.getTG().getNodeSet().setCorrespondingReplica(dataOwnerIndex);

        log.debug("Virtual system size logarithm: {}, Current topology index: {}",
                virtualSystemSizePower, SkipSimParameters.getCurrentTopologyIndex());
    }


    private ArrayList<String> expandBadCandidates()
    {
        ArrayList<String> newBadCandidates = new ArrayList<>();
        for (int i = 0; i < badCandidates.size(); i++)
        {
            newBadCandidates.add(badCandidates.get(i) + "0");
            newBadCandidates.add(badCandidates.get(i) + "1");
        }
        return newBadCandidates;
    }

    private double replicaSetGenerator(Result R, int virtualSystemSize, int landmarkIndex, int dataOwnerIndex)
    {
        SubProblemRepSet = new int[SkipSimParameters.getSystemCapacity()];
        RepSet = new boolean[SkipSimParameters.getSystemCapacity()];
        RepAccuracy = new double[SkipSimParameters.getSystemCapacity()];

        int virtualSystemNameIDSize = virtualSystemNameIDSize(virtualSystemSize);

        if (R == null)
        {
            return -Double.MAX_VALUE;
        }

        String result = new String();
        result = R.toString();

        double minAccuracy = Double.MAX_VALUE;

        for (int i = 0; i < virtualSystemSize; i++)
        {
            String target = "Y" + i + "=1";
            if (result.contains(target))
            {
                int replicaIndex = mapToOriginalSystem(i, landmarkIndex, virtualSystemNameIDSize, dataOwnerIndex);
                if (minAccuracy > RepAccuracy[replicaIndex]) minAccuracy = RepAccuracy[replicaIndex];
                SubProblemRepSet[replicaIndex] = i;
            }


        }

        return minAccuracy;
    }

    /**
     * Given index of a node in the virtual system, this function computes and returns the index of the most similar
     * node in the original sustem
     *
     * @param virtualNodeIndex        the index of the virtual system node
     * @param landmarkIndex           the index of the associated landmark (i.e., region) to the virtual node
     * @param virtualSystemNameIDSize the name Id size of the virtual system
     * @param dataOwnerIndex          the index of the data owner
     * @return address of the mapped node in the original system that owns the closest name ID to the virtual node
     */
    private int mapToOriginalSystem(int virtualNodeIndex, int landmarkIndex, int virtualSystemNameIDSize, int dataOwnerIndex)
    {
        /*
        Creating the name ID of the node in the virtual system
         */
        String virtualNameID = Integer.toBinaryString(virtualNodeIndex);
        while (virtualNameID.length() < virtualSystemNameIDSize) virtualNameID = "0" + virtualNameID;
        String prefix = sgo.getTG().mLandmarks.getDynamicPrefix(landmarkIndex);
        virtualNameID = prefix + virtualNameID;

        /*
        Finding the most similar available node to the name ID of the virtual node
         */
        int maxCommon = 0;
        int maxIndex = 0;
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            Node node =  (Node) sgo.getTG().mNodeSet.getNode(i);
            if (virtualNameID.equals(node.getNameID()))
            {
                /*
                We have found a perfect match with the virtual node name ID in the original system, hence to return it
                 */
                return i;
            }
            else if (commonBits(virtualNameID, node.getNameID()) >= maxCommon
                    && !node.isReplica(dataOwnerIndex)
                    && !RepSet[i]
                    && node.hasStorageCapacity()
                    && node.isOnline())
            {
                /*
                We have found an online node, that has not yet being selected as the replica for this data owner, and
                not even been selected as the temporary replica, and has more similarity to the virtual systen
                name ID fo the interest, and has storage capacity
                 */
                maxCommon = commonBits(virtualNameID, sgo.getTG().mNodeSet.getNode(i).getNameID());
                maxIndex = i;
                if (virtualNameID.length() == maxCommon) break;
            }
        }

        /*
        Computing the mapping accuracy
         */
        double mappingAccuracy = (double) (commonBits(virtualNameID, sgo.getTG().mNodeSet.getNode(maxIndex).getNameID())
                - sgo.getTG().mLandmarks.getDynamicPrefix(landmarkIndex).length()) / virtualSystemNameIDSize;

        RepSet[maxIndex] = true;
        RepAccuracy[maxIndex] = mappingAccuracy;

        //System.out.println("Closest name id to " + virtualNameID +
        // " is SkipGraph.Node " + maxIndex + " with name id " + sgo.getTG().mNodeSet.getNode(maxIndex).nameID
        // + " with " + (maxCommon - sgo.getTG().mLandmarks.getDynamicPrefix(landmarkIndex).length())
        // + " bits name id common prefix length, accuracy " + ac + " landmark prefix " + sgo.getTG().mLandmarks.getDynamicPrefix(landmarkIndex));
        return maxIndex;
    }

    private void refineBadCandidates(int virtualSystemSize)
    {
        int virtualSystemNameIDSize = 1;
        while (Math.pow(2, virtualSystemNameIDSize) < virtualSystemSize) virtualSystemNameIDSize++;

        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            if (RepAccuracy[i] < 0.99 && RepSet[i]) for (int j = 0; j < virtualSystemSize; j++)
            {
                if (commonBitsSubProblemSize(SubProblemRepSet[i], j, virtualSystemNameIDSize) > virtualSystemNameIDSize * RepAccuracy[i])
                {
                    badCandidates.add(toNameID(j, virtualSystemNameIDSize));
                }
            }
        }

    }


    private int virtualSystemSizeSet(int minSubProblemSize, int landmarkIndex)
    {

        int virtualSystemSize = Math.max(minSubProblemSize, getSubReplicationDegree(landmarkIndex));

        int powerVirtualSystemSize = 2;
        while (powerVirtualSystemSize < virtualSystemSize && powerVirtualSystemSize < SkipSimParameters.getSystemCapacity())
            powerVirtualSystemSize *= 2;

        return powerVirtualSystemSize;
    }

    /**
     * This function is developed for the debug purposes, and is used to print out the name ID distance table of the GLARAS
     * ILP. The bad canidates for replication are determined by *
     *
     * @param nameIDDistanceTable the name ID distance table
     * @param virtualSystemSize   the size of virtual system (i.e., number of rows and columns in the name ID distance table)
     * @param badCandidateIndices the indices of bad candidate
     */
    private void printNameIDDistanceTable(int[][] nameIDDistanceTable, int virtualSystemSize, ArrayList<Integer> badCandidateIndices)
    {
        int virtualSystemNameIDSize = virtualSystemNameIDSize(virtualSystemSize);
        for (int i = 0; i < virtualSystemSize; i++)
        {
            if (log.isTraceEnabled()) {
                StringBuilder row = new StringBuilder(toNameID(i, virtualSystemNameIDSize) + "    ");
                for (int j = 0; j < virtualSystemSize; j++)
                {
                    if (badCandidateIndices.contains(i) || badCandidateIndices.contains(j))
                        row.append(" * ");
                    else
                        row.append((int) nameIDDistanceTable[i][j]).append("    ");
                }
                log.trace("{}", row.toString());
            }
        }
    }

    private ArrayList<Integer> dataRequesterIndices(int virtualSystemSize, int landmarkIndex)
    {
        ArrayList<Integer> dataRequesters = new ArrayList<>();
        if (SkipSimParameters.isPublicReplication())
        {
            for (int i = 0; i < virtualSystemSize; i++)
            {
                dataRequesters.add(i);
            }
        }
        else
        {
            sgo.getTG().mNodeSet.updateClosestLandmark(sgo.getTG().mLandmarks);
            int virtualNameIDSize = virtualSystemNameIDSize(virtualSystemSize);
            for (int i = 0; i < SkipSimParameters.getDataRequesterNumber(); i++)
            {
                if (((Node) sgo.getTG().mNodeSet.getNode(i)).getClosetLandmarkIndex(sgo.getTG().mLandmarks) == landmarkIndex)
                {
                    String nameID = sgo.getTG().mNodeSet.getNode(i).getNameID();
                    String prefixFreeNameID = nameID.substring(nameID.length() - SkipSimParameters.getNameIDLength());
                    String squeezedNameID = prefixFreeNameID.substring(0, virtualNameIDSize);
                    int virtualSystemIndex = Integer.parseInt(squeezedNameID);
                    if (!dataRequesters.contains(virtualSystemIndex)) dataRequesters.add(virtualSystemIndex);
                }
            }
        }
        return dataRequesters;
    }

    /**
     * The system wide distribution of replicas among the regions, each region receives a sub-replication degree.
     * More information about this function and weights w1, w2, and w3, are available in our GLARAS paper
     *
     * @param w1
     * @param w2
     * @param w3
     */
    public void SWD(double w1, double w2, double w3, int[] availableSubDomains)
    {
        /*
        Landmarks pairwise latency
         */
        double maxPairwiseLatency = Double.MIN_VALUE;
        /*
        totalLatencyToLandmarks[i] keeps the total latency of landmark i to all the landmarks
         */
        double[] totalLatencyToLandmarks = new double[SkipSimParameters.getLandmarksNum()];
        /*
        D[i][j] keeps the latency between landmarks i and j
         */
        double[][] D = new double[SkipSimParameters.getLandmarksNum()][SkipSimParameters.getLandmarksNum()];

        /*
        Computing the total latency between each landmark to every other landmarks, as well as the maxmimum pairwise
        latency of landmarks
         */
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            totalLatencyToLandmarks[i] = 0;
            //double minLatency = Double.MAX_VALUE;
            for (int j = 0; j < SkipSimParameters.getLandmarksNum(); j++)
            {
                D[i][j] = sgo.getTG().mLandmarks.getLandmarkCoordination(i).distance(sgo.getTG().mLandmarks.getLandmarkCoordination(j));
                //if (D[i][j] < minLatency) minLatency = D[i][j];
                if (D[i][j] > maxPairwiseLatency) maxPairwiseLatency = D[i][j];
                totalLatencyToLandmarks[i] += D[i][j];
            }
        }


        /*
        closestCounter for each landmark i keeps the number of landmarks that landmark i covers as their closest landmark
        EXCLUDING landmark i itself.
         */
        int[] closestCounter = closestLandmarkCounter();

        /*
        Computing the map of closest landmarks, if map[i][j] = true, it means that region i covers
         */
        boolean[][] map = closestLandmarkMap();

        /*
        Holds the total score of each landmark on receiving the sub-replication degree
         */
        double[] landmarksScore = new double[SkipSimParameters.getLandmarksNum()];
        Arrays.fill(landmarksScore, 0);

        /*
        The ordered set of landmarks based on their scores so that the replication degree is distributed among in a
        cyclic manner
         */
        int[] orderdLandmarkIndices = new int[SkipSimParameters.getLandmarksNum()];

        /*
        Finding the landmark with the minimum total latency
         */
        double minTotalLatency = Double.MAX_VALUE;
        double maxTotalLatency = Double.MIN_VALUE;
        int minTotalLatencyIndex = 0;
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            if (totalLatencyToLandmarks[i] > maxTotalLatency)
            {
                maxTotalLatency = totalLatencyToLandmarks[i];
            }

            if (totalLatencyToLandmarks[i] < minTotalLatency)
            {
                minTotalLatency = totalLatencyToLandmarks[i];
                minTotalLatencyIndex = i;
            }
        }
        /*
        Remove the landmark with the mimimum total latency and place it as the first one in the permutation
         */
        landmarksScore[minTotalLatencyIndex] = -1;
        orderdLandmarkIndices[0] = minTotalLatencyIndex;

        /*
        Keeps the total prefix of landmarks as an estimate on the system capacity to normalize the
        population of each region
         */
        int totalDynamicPrefix = 0;
        for (int landmark = 0; landmark < SkipSimParameters.getLandmarksNum(); landmark++)
        {
            totalDynamicPrefix += sgo.getTG().mLandmarks.dynamicPrefixLength(landmark);
        }

        /*
        Filling the permutation from the second landmark to the end
         */
        for (int landmark = 1; landmark < SkipSimParameters.getLandmarksNum(); landmark++)
        {
            double maxPoint = Double.MIN_VALUE;
            int maxIndex = 0;
            /*
            Finding the closest landmark with negative landmark score (i.e., -1) to each landmark
            this means the closest landmark that has already been placed in the permutation
             */
            for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
            {
                /*
                If landmark i has already been selected so there is not need to reprocess it
                 */
                if (landmarksScore[i] < 0) continue;

                /*
                Minimum latency of landmark i to  the landmarks in the permutation (i.e., with negative score)
                 */
                double minLatency = Double.MAX_VALUE;

                /*
                Number of regions landmark i covers as their closest landmark
                 */
                int coverinRegions = 0;
                for (int j = 0; j < SkipSimParameters.getLandmarksNum(); j++)
                {
                    /*
                    Checking the minimum latency
                     */
                    if (D[i][j] < minLatency && landmarksScore[j] < 0 && i != j) minLatency = D[i][j];
                    /*
                    Checking and updating the coverage
                     */
                    if (map[i][j])
                    {
                        if (SkipSimParameters.isPublicReplication()) coverinRegions++;
                        else coverinRegions += dataRequesterNumbers[j];
                    }
                }
                /*
                Finalizing the score of landmark i based on the replication mode
                 */
                if (SkipSimParameters.isPublicReplication())
                {
                    landmarksScore[i] = (w1 * ((double) sgo.getTG().mLandmarks.dynamicPrefixLength(i)) / totalDynamicPrefix)
                            + (w2 * minLatency / maxPairwiseLatency)
                            + (w3 * (double) coverinRegions / SkipSimParameters.getLandmarksNum());
                }
                else
                {
                    /*
                    private replication
                     */
                    landmarksScore[i] = (w1 * ((double) dataRequesterNumbers[i] / SkipSimParameters.getDataRequesterNumber()))
                            + (w2 * minLatency / maxPairwiseLatency)
                            + (w3 * (double) coverinRegions / SkipSimParameters.getDataRequesterNumber());
                }
                /*
                Updating the maximum landmark
                 */
                if (landmarksScore[i] > maxPoint)
                {
                    maxPoint = landmarksScore[i];
                    maxIndex = i;
                }
            }

            /*
            Moving the maximum landmark to the permutation, and making it removed from the set of unchosen landmarks
             */
            landmarksScore[maxIndex] = -1;
            orderdLandmarkIndices[landmark] = maxIndex;
        }

        int region = 0;
        int assignedRepDegree = 0;
        while (assignedRepDegree < SkipSimParameters.getReplicationDegree())
        {
            int landmarkIndex = orderdLandmarkIndices[region % SkipSimParameters.getLandmarksNum()];
            region++;

            if(availableSubDomains != null && IntStream.of(availableSubDomains).sum() < SkipSimParameters.getReplicationDegree())
            {
                throw new IllegalStateException("GLARAS.java: SWD, too bing replication degree than the number of available subdomains.");
            }
            /*
            If we have the dynamic replication and accumulated availability information, then each region should
            receive as many as sub-replication degree as its available sub-regions i.e., sub-regions with non-zero sum
            of aggregated availability vector
             */
            if(availableSubDomains != null &&
                    (availableSubDomains[landmarkIndex] == subReplicationDegree[landmarkIndex]
                            || availableSubDomains[landmarkIndex] == 0))
                continue;
            else
            {
                subReplicationDegree[landmarkIndex]++;
                assignedRepDegree++;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("SWD assignment info:");
            for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++) {
                String info = String.format("index=%d sub-rep-degree=%d score=%d closest-counter=%d dynamic-prefix=%d total-latency=%d",
                        i, subReplicationDegree[i], (int) landmarksScore[i], closestCounter[i],
                        sgo.getTG().mLandmarks.dynamicPrefixLength(i), totalLatencyToLandmarks[i]);
                if (!SkipSimParameters.isPublicReplication())
                    info += " data-requesters=" + dataRequesterNumbers[i];
                log.debug("{}", info);
            }
        }
    }


}