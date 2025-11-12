package Replication;

import DataTypes.Constants;
import Simulator.SkipSimParameters;
import SkipGraph.Node;
import SkipGraph.Nodes;
import SkipGraph.SkipGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Created by Dell on 26.08.2016.
 */
public abstract class Replication
{
    private static final Logger log = LoggerFactory.getLogger(Replication.class);


    private static int[] replicationDegreeDataBase = new int[SkipSimParameters.getTopologies()];
    public int[][] realDistance;
    public int[][] nameidsDistance;
    protected int[] regionPopulation;
    protected int[] dataRequesterNumbers;
    protected SkipGraphOperations sgo;
    /**
     * pathHistoragm[i] shows the number of pathes to a data owner that SkipGraph.Node i is located
     */
    protected int[] pathHistogram;
    /**
     * subReplicationDegree[i] represents the number of replicas in region i of the system
     */
    protected int[] subReplicationDegree;
    private int[] adaptiveSubproblemSizes; //only for the adaptice SSLP

    public int getAdaptiveSubproblemSizes(int index)
    {
        return adaptiveSubproblemSizes[index];
    }

    public Replication()
    {
        regionPopulation = new int[SkipSimParameters.getLandmarksNum()];
        dataRequesterNumbers = new int[SkipSimParameters.getLandmarksNum()];
        subReplicationDegree = new int[SkipSimParameters.getLandmarksNum()];
        adaptiveSubproblemSizes = new int[SkipSimParameters.getLandmarksNum()];
        /*
        We initialize sgo with false parameter as we do not assume the blockchain operation in timeless
        static simulation
         */
        //sgo = new SkipGraphOperations(false);
        pathHistogram = new int[SkipSimParameters.getSystemCapacity()];
    }

    public abstract void Algorithm(SkipGraphOperations sgo, int dataOwnerID);

    public void setSubReplicationDegree(int index, int value)
    {
        this.subReplicationDegree[index] = value;
    }


    public int RepOnPath(int num, int startIndex, int RepNum, boolean histogramUpdate, int dataOwnerID)
    {
        int localRepCounter = 0;
        int level = SkipSimParameters.getLookupTableSize() - 1; //Start from the top
        int next = -1;
        int before = startIndex;
        if (sgo.getTG().mNodeSet.getNode(startIndex).getLookup(0, 0) == -1 && sgo.getTG().mNodeSet.getNode(startIndex).getLookup(0, 1) == -1)
        { // if only the introducer exists
            log.debug("Search by num id for {} resulted in null", num);
            return RepNum;
        }
        else if (sgo.getTG().mNodeSet.getNode(startIndex).getNumID() < num)
        {

            while (level > 0 && sgo.getTG().mNodeSet.getNode(startIndex).getLookup(level, 1) == -1) level = level - 1;
            while (level > 0)
            {
                if (sgo.getTG().mNodeSet.getNode(startIndex).getLookup(level, 1) != -1)
                {
                    if (sgo.getTG().mNodeSet.getNode(sgo.getTG().mNodeSet.getNode(startIndex).getLookup(level, 1)).getNumID() > num)
                        level = level - 1;
                    else break;
                }
                else level--;
            }

            if (level == 0)
            {
                log.debug("Search by num id for {} resulted in {}", num, sgo.getTG().mNodeSet.getNode(sgo.getTG().mNodeSet.getNode(startIndex).getLookup(level, 1)).getNumID());
                return RepNum;
            }
            if (level > 0)
            {
                next = sgo.getTG().mNodeSet.getNode(startIndex).getLookup(level, 1);
                //Replicate here
                if (histogramUpdate) pathHistogram[next]++;
                else
                {
                    if (!((Node) sgo.getTG().mNodeSet.getNode(next)).isReplica(dataOwnerID))
                    {

                        //************Replication Unit***************************************
                        //if (RepNum <= getReplicationDegree() && localRepCounter < 0.2 * getReplicationDegree())
                        if (RepNum <= SkipSimParameters.getReplicationDegree())
                        {
                            if (SkipSimParameters.isPublicReplication() || next < SkipSimParameters.getDataRequesterNumber())
                            {
                                boolean replicationResult = ((Node) sgo.getTG().mNodeSet.getNode(next)).setAsReplica(dataOwnerID);
                                if (replicationResult)
                                {
                                    RepNum++;
                                    localRepCounter++;
                                }
                            }
                        }
                        else
                        {
                            log.debug("Rep on path terminates the search");
                            return RepNum;
                        }
                        //realWorldReplicaSet[next] = true;
                        //**************
                    }
                }
                //*********************
                sgo.getTG().mNodeSet.addTime(next, startIndex);
                while (level >= 0)
                {
                    if (sgo.getTG().mNodeSet.getNode(next).getLookup(level, 1) != -1)
                    {
                        if (sgo.getTG().mNodeSet.getNode(sgo.getTG().mNodeSet.getNode(next).getLookup(level, 1)).getNumID() <= num)
                        {
                            before = next;
                            next = sgo.getTG().mNodeSet.getNode(next).getLookup(level, 1);
                            //Replicate here
                            if (histogramUpdate) pathHistogram[next]++;
                            else
                            {
                                if (!((Node) sgo.getTG().mNodeSet.getNode(next)).isReplica(dataOwnerID))
                                {

                                    //************Replication Unit***************************************
                                    //if (RepNum <= getReplicationDegree() && localRepCounter < 0.2 * getReplicationDegree())
                                    if (RepNum <= SkipSimParameters.getReplicationDegree())
                                    {
                                        if (SkipSimParameters.isPublicReplication() || next < SkipSimParameters.getDataRequesterNumber())
                                        {
                                            boolean replicationResult = ((Node) sgo.getTG().mNodeSet.getNode(next)).setAsReplica(dataOwnerID);
                                            if (replicationResult)
                                            {
                                                RepNum++;
                                                localRepCounter++;
                                            }
                                        }
                                    }
                                    else
                                    {
                                        log.trace("Rep on path terminates the search");
                                        return RepNum;
                                    }
                                    //realWorldReplicaSet[next] = true;
                                    //**************
                                }
                            }

                            //**************
                            sgo.getTG().mNodeSet.addTime(before, next);
                            if (sgo.getTG().mNodeSet.getNode(next).getNumID() == num)
                            {
                                log.debug("Search by num id for {} resulted in {}", num, sgo.getTG().mNodeSet.getNode(next).getNumID());
                                return RepNum;
                            }
                        }
                        else level = level - 1;
                    }
                    else level = level - 1;
                }
            }

            log.debug("Search by num id for {} resulted in {}", num, sgo.getTG().mNodeSet.getNode(next).getNumID());
            return RepNum;
        }
        else
        {
            next = -1;
            while (level > 0 && sgo.getTG().mNodeSet.getNode(startIndex).getLookup(level, 0) == -1) level = level - 1;
            while (level > 0)
            {
                if (sgo.getTG().mNodeSet.getNode(startIndex).getLookup(level, 0) != -1)
                {
                    if (sgo.getTG().mNodeSet.getNode(sgo.getTG().mNodeSet.getNode(startIndex).getLookup(level, 0)).getNumID() < num)
                        level = level - 1;
                    else break;
                }
                else level = level - 1;
            }

            if (level >= 0)
            {
                before = next;
                next = sgo.getTG().mNodeSet.getNode(startIndex).getLookup(level, 0);
                //Replicate here
                if (histogramUpdate) pathHistogram[next]++;
                else
                {
                    if (!((Node) sgo.getTG().mNodeSet.getNode(next)).isReplica(dataOwnerID))
                    {

                        //************Replication Unit***************************************
                        //if (RepNum <= getReplicationDegree() && localRepCounter < 0.2 * getReplicationDegree())
                        if (RepNum <= SkipSimParameters.getReplicationDegree())
                        {
                            if (SkipSimParameters.isPublicReplication() || next < SkipSimParameters.getDataRequesterNumber())
                            {
                                boolean replicationResult = ((Node) sgo.getTG().mNodeSet.getNode(next)).setAsReplica(dataOwnerID);
                                if (replicationResult)
                                {
                                    RepNum++;
                                    localRepCounter++;
                                }
                            }
                        }
                        else
                        {
                            log.trace("Rep on path terminates the search");
                            return RepNum;
                        }
                        //realWorldReplicaSet[next] = true;
                        //**************
                    }
                }

                //**************
                sgo.getTG().mNodeSet.addTime(before, next);
                while (level >= 0)
                {
                    if (sgo.getTG().mNodeSet.getNode(next).getLookup(level, 0) != -1)
                    {
                        if (sgo.getTG().mNodeSet.getNode(sgo.getTG().mNodeSet.getNode(next).getLookup(level, 0)).getNumID() >= num)
                        {
                            before = next;
                            next = sgo.getTG().mNodeSet.getNode(next).getLookup(level, 0);
                            //Replicate here
                            if (histogramUpdate) pathHistogram[next]++;
                            else
                            {
                                if (!((Node) sgo.getTG().mNodeSet.getNode(next)).isReplica(dataOwnerID))
                                {

                                    //************Replication Unit***************************************
                                    //if (RepNum <= getReplicationDegree() && localRepCounter < 0.2 * getReplicationDegree())
                                    if (RepNum <= SkipSimParameters.getReplicationDegree())
                                    {
                                        if (SkipSimParameters.isPublicReplication() || next < SkipSimParameters.getDataRequesterNumber())
                                        {
                                            boolean replicationResult = ((Node) sgo.getTG().mNodeSet.getNode(next)).setAsReplica(dataOwnerID);
                                            if (replicationResult)
                                            {
                                                RepNum++;
                                                localRepCounter++;
                                            }
                                        }
                                    }
                                    else
                                    {
                                        log.trace("Rep on path terminates the search");
                                        return RepNum;
                                    }
                                    //realWorldReplicaSet[next] = true;
                                    //**************
                                }
                            }

                            //**************
                            sgo.getTG().mNodeSet.addTime(before, next);
                            if (sgo.getTG().mNodeSet.getNode(next).getNumID() == num)
                            {
                                log.debug("Search by num id for {} resulted in {}", num, sgo.getTG().mNodeSet.getNode(next).getNumID());
                                return RepNum;
                            }
                        }
                        else level = level - 1;
                    }
                    else level = level - 1;
                }

            }

            log.debug("Search by num id for {} resulted in {}", num, sgo.getTG().mNodeSet.getNode(next).getNumID());
            return RepNum;
        }
    }

    /**
     * Given index of a region, this function
     *
     * @param i
     * @return
     */
    public int getSubReplicationDegree(int i)
    {
        return subReplicationDegree[i];
    }

    public void reset()
    {
        subReplicationDegree = new int[SkipSimParameters.getLandmarksNum()];
        adaptiveSubproblemSizes = new int[SkipSimParameters.getLandmarksNum()];
    }

    public void resetRep()
    {
        if (SkipSimParameters.isStaticSimulation())
        {
            for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
            {
                ((Node) sgo.getTG().mNodeSet.getNode(i)).clearReplicaIDSet();
                ((Node) sgo.getTG().mNodeSet.getNode(i)).clearDataRequesterIDSet();
                ((Node) sgo.getTG().mNodeSet.getNode(i)).clearCorrespondingReplicaData();
            }
        }
    }


    /**
     * Given two strings s1 and s2 returns the length of common prefix between them
     *
     * @param s1
     * @param s2
     * @return
     */
    protected int commonBits(String s1, String s2)
    {
        if (s1.length() == 0 || s2.length() == 0)
        {
            if (!SkipSimParameters.isStaticSimulation())
            {
                /*
                In dynamic replication nodes are allowed to have empty name ID as they may not yet arrived to the system
                 */
                return 0;
            }
            else
            {
                throw new IllegalStateException("GLARAS.java: commonBits is called with empty string");
            }
        }
        int k = 0;
        while (s1.charAt(k) == s2.charAt(k))
        {
            k++;
            if (k >= s1.length() || k >= s2.length()) break;
        }
        return k;
    }


    /*
    Counts each landmark is the closest landmark of how many others
     */
    protected int[] closestLandmarkCounter()
    {
        int[] counter = new int[SkipSimParameters.getLandmarksNum()];
        Arrays.fill(counter, 0);

        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            if (SkipSimParameters.getReplicationType().equalsIgnoreCase(Constants.Replication.Type.PRIVATE) && dataRequesterNumbers[i] == 0)
                continue;
            double minDistance = Double.MAX_VALUE;
            int minIndex = 0;
            for (int j = 0; j < SkipSimParameters.getLandmarksNum(); j++)
            {
                if (sgo.getTG().mLandmarks.getLandmarkCoordination(i).distance(sgo.getTG().mLandmarks.getLandmarkCoordination(j)) < minDistance && i != j)
                {
                    minDistance = sgo.getTG().mLandmarks.getLandmarkCoordination(i).distance(sgo.getTG().mLandmarks.getLandmarkCoordination(j));
                    minIndex = j;
                }
            }
            counter[minIndex]++;
        }
        return counter;
    }

    /**
     * @return Two dimensional matrix boolean matrix of map, which map[i][j] = true if and only if landmark j's closest landmark is landmark i
     */
    protected boolean[][] closestLandmarkMap()
    {
        boolean[][] map = new boolean[SkipSimParameters.getLandmarksNum()][SkipSimParameters.getLandmarksNum()];
        for (boolean[] row : map)
            Arrays.fill(row, false);
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            double minDistance = Double.MAX_VALUE;
            int minIndex = 0;
            for (int j = 0; j < SkipSimParameters.getLandmarksNum(); j++)
            {
                if (sgo.getTG().mLandmarks.getLandmarkCoordination(i).distance(sgo.getTG().mLandmarks.getLandmarkCoordination(j)) < minDistance && i != j)
                {
                    minDistance = sgo.getTG().mLandmarks.getLandmarkCoordination(i).distance(sgo.getTG().mLandmarks.getLandmarkCoordination(j));
                    minIndex = j;
                }
            }
            map[minIndex][i] = true;
        }

        return map;
    }


    /**
     * @param landmarkIndex index of the landmark
     * @return sum of latencies between landmak index to the rest of landmarks
     */
    protected double totalLatencyToLandmark(int landmarkIndex)
    {
        double sum = 0;
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            sum += sgo.getTG().mLandmarks.getLandmarkCoordination(landmarkIndex).distance(sgo.getTG().mLandmarks.getLandmarkCoordination(i));
        }
        return sum;
    }


    /*
    Computes and returns the average distance of each landmark to SkipGraph.Nodes in the Simulator.system
     */
    protected double[] averageDistanceOfLandmarks()
    {
        double[] averageDistance = new double[SkipSimParameters.getLandmarksNum()];
        Arrays.fill(averageDistance, 0);
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            for (int j = 0; j < SkipSimParameters.getLandmarksNum(); j++)
            {
                averageDistance[j] += ((Node) sgo.getTG().mNodeSet.getNode(i)).getCoordinate().distance(sgo.getTG().mLandmarks.getLandmarkCoordination(j));
            }
        }

        for (int j = 0; j < SkipSimParameters.getLandmarksNum(); j++)
        {
            averageDistance[j] /= SkipSimParameters.getSystemCapacity();
        }

        return averageDistance;
    }

    protected void printLandmarkPairwiseLatency()
    {
        StringBuilder sb = new StringBuilder("SkipGraph.Landmarks pairwise latencies:\n");
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            for (int j = 0; j < SkipSimParameters.getLandmarksNum(); j++)
            {
                sb.append((int) sgo.getTG().mLandmarks.getLandmarkCoordination(i).distance(sgo.getTG().mLandmarks.getLandmarkCoordination(j))).append("    ");
            }
            sb.append("\n");
        }
        log.debug(sb.toString());
    }


    /**
     * Should only be used by LARAS, the implementation of our NOMS paper
     */
    public void populationBasedRepShareDefining()
    {
        int[] regionsPopulation = new int[SkipSimParameters.getLandmarksNum()];


        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            //Todo suspicious if statement
            if ((i < SkipSimParameters.getDataRequesterNumber() && !SkipSimParameters.isPublicReplication()) || SkipSimParameters.isPublicReplication())
                regionsPopulation[sgo.getTG().mNodeSet.ClosestLandmark(i, sgo.getTG().mLandmarks)] += 1;
        }

        double sum = 0;
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            sum = sum + regionsPopulation[i];
        }

        sum = SkipSimParameters.getReplicationDegree() / sum;

        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            subReplicationDegree[i] = (int) Math.round(sum * regionsPopulation[i]);
        }
        sum = 0;
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            sum = sum + subReplicationDegree[i];
        }

        while (sum < SkipSimParameters.getReplicationDegree())
        {
            int minIndex = 0;
            double min = Double.MAX_VALUE;
            for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
                if (regionsPopulation[i] < min && subReplicationDegree[i] < 1)
                {
                    min = regionsPopulation[i];
                    minIndex = i;
                }
            subReplicationDegree[minIndex] = subReplicationDegree[minIndex] + 1;
            if (subReplicationDegree[minIndex] < 0) subReplicationDegree[minIndex] = 0;

            sum = 0;
            for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
            {
                sum = sum + subReplicationDegree[i];

            }
        }


        sum = 0;
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            sum = sum + subReplicationDegree[i];
        }

        while (sum > SkipSimParameters.getReplicationDegree())
        {
            int minIndex = 0;
            double min = Double.MAX_VALUE;
            for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
                if (subReplicationDegree[i] < min && subReplicationDegree[i] > 0)
                {
                    min = subReplicationDegree[i];
                    minIndex = i;
                }
            subReplicationDegree[minIndex] = subReplicationDegree[minIndex] - 1;
            if (subReplicationDegree[minIndex] < 0) subReplicationDegree[minIndex] = 0;

            sum = 0;
            for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
            {
                sum = sum + subReplicationDegree[i];

            }
        }

        if (log.isDebugEnabled()) {
            StringBuilder regionStats = new StringBuilder();
            for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
            {
                regionStats.append(String.format("[R%d: degree=%d, pop=%d] ",
                    i, subReplicationDegree[i], regionsPopulation[i]));
            }
            log.debug("Region replication stats: {}", regionStats.toString().trim());
        }
    }

    /**
     * Should only be used by LARAS, the implementation of our NOMS paper
     */
    public void adaptivePopulationBasedSubproblemSizeDefining()
    {
        double[] regionsPopulation = new double[SkipSimParameters.getLandmarksNum()];
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            //Todo suspicious if statement
            if ((i < SkipSimParameters.getDataRequesterNumber() && SkipSimParameters.isPublicReplication()) || !SkipSimParameters.isPublicReplication())
                regionsPopulation[sgo.getTG().mNodeSet.ClosestLandmark(i, sgo.getTG().mLandmarks)] += 1;
        }

        double sum = 0;
        int maxIndex = 0;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            if (regionsPopulation[i] > max)
            {
                maxIndex = i;
                max = regionsPopulation[i];
            }
            sum = sum + regionsPopulation[i];
        }


        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            regionsPopulation[i] = (regionsPopulation[i] / max);
        }

        double expectedPopulation = SkipSimParameters.getSystemCapacity() / SkipSimParameters.getLandmarksNum();
        //double expectedReplicas   = Simulator.system.MNR  / Simulator.system.landmarks;

        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            adaptiveSubproblemSizes[i] = (int) Math.round(regionsPopulation[i] * expectedPopulation * Math.log(SkipSimParameters.getReplicationDegree()) / Math.log(2));
            //adaptiveSubproblemSizes[i] = (int) Math.round(regionsPopulation[i] * expectedPopulation);
        }

        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            int subProblemSize = 2;
            while (subProblemSize < adaptiveSubproblemSizes[i] && subProblemSize < SkipSimParameters.getSystemCapacity())
                subProblemSize *= 2;
            if (subProblemSize > SkipSimParameters.getSystemCapacity() / 2) subProblemSize /= 2;
            while (subProblemSize < 16) subProblemSize *= 2;
            adaptiveSubproblemSizes[i] = subProblemSize;
        }

        log.debug("Adaptive Sub-problem defining:");
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            log.debug("Population based Sub-problem size = {}\t SkipGraph.Landmarks Index = {}", adaptiveSubproblemSizes[i], i);
        }
    }

    protected double latencyToClosestLandmark(int index)
    {
        double minLatency = Double.MAX_VALUE;
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            if (sgo.getTG().mLandmarks.getLandmarkCoordination(i).distance(sgo.getTG().mLandmarks.getLandmarkCoordination(index)) < minLatency && i != index)
                minLatency = sgo.getTG().mLandmarks.getLandmarkCoordination(i).distance(sgo.getTG().mLandmarks.getLandmarkCoordination(index));
        }

        return minLatency;
    }


    protected void dataRequesterPopulation()
    {
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            regionPopulation[i] = 0;
            dataRequesterNumbers[i] = 0;
        }
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            regionPopulation[ClosestLandmark((Node) sgo.getTG().mNodeSet.getNode(i))]++;
            //if (repType == SkipSimParameters.PRIVATE_REPLICATION && i < SkipSimParameters.getDataRequesterNumber())
            if (!SkipSimParameters.isPublicReplication() && i < SkipSimParameters.getDataRequesterNumber())
            {
                dataRequesterNumbers[ClosestLandmark((Node) sgo.getTG().mNodeSet.getNode(i))]++;
            }
        }

    }

    private int ClosestLandmark(Node n)
    {
        double min = Double.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            /*
            By non-empty name ID, we mean that the node arrived to the system at least once, and got coordinate
             */
            if (!n.getNameID().isEmpty() && n.getCoordinate().distance(sgo.getTG().mLandmarks.getLandmarkCoordination(i)) < min)
            {
                min = n.getCoordinate().distance(sgo.getTG().mLandmarks.getLandmarkCoordination(i));
                index = i;
            }


        }
        return index;
    }

    public void delayBasedReplication(double delayBound, int initialReplicationDegree, SkipGraphOperations sgo, String algorithmName, int dataOwnerIndex)
    {
        if (dataOwnerIndex > 0)
        {
            throw new IllegalStateException("Delay based replication is not applicable to multiple-data owner cases, data owner index:" + dataOwnerIndex);
        }
        log.info("Delay-based replication started [dataOwner={}, initialDegree={}, delayBound={}, capacity={}, topology={}]",
            dataOwnerIndex,
            initialReplicationDegree,
            delayBound,
            SkipSimParameters.getSystemCapacity(),
            SkipSimParameters.getCurrentTopologyIndex());
        int replicationDegree = initialReplicationDegree;
        double averageDelay;
        do
        {
            reset();
            SkipSimParameters.setReplicationDegree(replicationDegree);
            Algorithm(sgo, dataOwnerIndex);
            averageDelay = averageAccessDelay(sgo.getTG().mNodeSet, dataOwnerIndex);
            log.debug("Replication degree {} average delay {}", replicationDegree, averageDelay);
            if (averageDelay >= delayBound)
            {
                replicationDegree++;
            }

        } while (averageDelay > delayBound);
        replicationDegreeDataBase[SkipSimParameters.getCurrentTopologyIndex() - 1] = replicationDegree;
        if (SkipSimParameters.getCurrentTopologyIndex() == SkipSimParameters.getTopologies())
        {
            double average = (double) IntStream.of(replicationDegreeDataBase).sum() / SkipSimParameters.getTopologies();
            double SD = SkipSimParameters.getStandardDeviation(replicationDegreeDataBase, average);
            log.info("Delay bound simulation to obtain average bount of {} ms - Average replication degree {} - algorithm name {} - SD {} - NOR = {}",
                    delayBound, average, algorithmName, SD, SkipSimParameters.getDataRequesterNumber());
        }
    }

    public double averageAccessDelay(Nodes nodes, int dataOwnerIndex)
    {
        double averageDelay = 0;
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            if (!SkipSimParameters.isPublicReplication() && i >= SkipSimParameters.getDataRequesterNumber())
                break;
            Node node = (Node) nodes.getNode(i);
            int replicaIndex = node.getCorrespondingReplica(dataOwnerIndex);
            if (replicaIndex != -1)
            {
                Node replica = (Node) nodes.getNode(replicaIndex);
                averageDelay += node.latencyTo(replica);
            }
        }
        return averageDelay;
    }


    public void setSgo(SkipGraphOperations sgo)
    {
        this.sgo = sgo;
    }
}
