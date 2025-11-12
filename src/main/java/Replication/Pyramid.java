package Replication;

import Simulator.SkipSimParameters;
import SkipGraph.Node;
import SkipGraph.Nodes;
import SkipGraph.SkipGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.sf.javailp.*;

import java.util.*;
import java.util.stream.DoubleStream;

public class Pyramid extends GLARAS
{
    private static final Logger log = LoggerFactory.getLogger(Pyramid.class);
    /**
     * A buffer value to keep the objective function value of the latest time that ILP solved the model
     */
    protected double objectiveValue;
    protected ArrayList<String> bestResult;
    protected double bestObjectiveValue;

    /**
     * Given an array of double, returns the second norm of that
     *
     * @param array input array
     * @return the second norm
     */
    public static double secondNorm(double[] array)
    {
        double norm = 0;
        for (double e : array)
        {
            if (e == 0) continue;
            norm += Math.pow(e, 2);
        }

        return ((double) Math.sqrt(norm));
    }

    /**
     * Given an array of double, returns the second norm of that, the norm is also divided by the number of non-zero elements
     *
     * @param array input array
     * @return second norm
     */
    public static double secondAvailabilityBasedNorm(double[] array)
    {
        int zeroElements = 0;
        double norm = 0;
        for (double e : array)
        {
            if (e == 0)
            {
                zeroElements++;
                continue;
            }
            norm += Math.pow(e, 2);

        }

        if (zeroElements == 0)
        {
            zeroElements = 1;
        }
        return ((double) Math.sqrt(norm)) / (array.length * zeroElements);
    }

    /**
     * Given an array of double, returns the number of zero elements in the array
     *
     * @param array input array
     * @return zero elements
     */
    public static double offlineTimeSlotsCounter(double[] array)
    {
        int zeroElements = 0;
        for (double e : array)
        {
            if (e == 0)
            {
                zeroElements++;
            }
        }

        return zeroElements;
    }

    @Override
    public void Algorithm(SkipGraphOperations inputSgo, int dataOwnerIndex)
    {
        log.info("Pyramid replication started [dataOwner={}, degree={}, capacity={}, topology={}]",
            dataOwnerIndex,
            SkipSimParameters.getReplicationDegree(),
            SkipSimParameters.getSystemCapacity(),
            SkipSimParameters.getCurrentTopologyIndex());
        sgo = inputSgo;
        resetRep();
        dataRequesterPopulation();

        objectiveValue = 0;
        bestResult = new ArrayList<String>();
        bestObjectiveValue = 0;

        final Hashtable<String, double[]>[] qosTable = sgo.getTG().mNodeSet.getQosTable().getAvailabilityTable().clone();
        SWD(32.01, 33.88, 34.11, availabileSubregions(qosTable.clone()));
        for (int region = 0; region < SkipSimParameters.getLandmarksNum(); region++)
        {
            int subRepDegree = getSubReplicationDegree(region);
            if (subRepDegree == 0)
                continue;
            //ArrayList<String> replist = advancedRecursiveILP(qosTable[region], subRepDegree, subRepDegree);
            ArrayList<String> replist = weightedILP(qosTable[region], subRepDegree);
            /*
            Replicating on the list of replicas
             */
            for (String subdomain : replist)
            {
                String landmarkPrefix = sgo.getTG().mLandmarks.getDynamicPrefix(region);
                int bestMatchIndex = searchForUtility(sgo.getTG().mLandmarks.getDynamicPrefix(region) + subdomain, sgo.getTG().mNodeSet, dataOwnerIndex);
                Node repCandidate = (Node) sgo.getTG().getNodeSet().getNode(bestMatchIndex);
                if (repCandidate.isReplica(dataOwnerIndex))
                {
                    /*
                    The replication candidate is already selected as a replica to the dataOwnerIndex
                     */
                    throw new IllegalStateException("Pyramid.java: double-replication found " + bestMatchIndex);
                }
                else
                {
                    repCandidate.setAsReplica(dataOwnerIndex);
                }
            }
        }
    }

    public ArrayList<String> weightedILP(Hashtable<String, double[]> qosTable, int replicationDegree)
    {
        /*
        Computing the average of qos table
         */
        double[] qosTableAverage = averageQosTable(qosTable);

        /*
        Computing below average number of qosTable entries
         */
        double[] counter = belowAverageItems(qosTable, qosTableAverage);
        double[] weights = availabilityWeightsDistribution(counter);
        //System.out.println("Weights " + Arrays.toString(weights));

        return ILP(qosTable, replicationDegree, weights);
    }


//    public ArrayList<String> advancedRecursiveILP(Hashtable<String, double[]> qosTable, int replicationDegree, int clusterNum)
//    {
//        Hashtable<String, Integer> clusterIndex = AutoKMeanClustering.AutoKMeanClustering(qosTable, clusterNum);
//        ArrayList<String> repResults = ILP(qosTable, replicationDegree);
//        if(objectiveValue >= bestObjectiveValue)
//        {
//            bestObjectiveValue = objectiveValue;
//            bestResult = repResults;
//        }
//        if (replicationDegree == 1)
//            return repResults;
//        for (String subdomain1 : repResults)
//        {
//            int clusterId1 = clusterIndex.get(subdomain1);
//            for (String subdomain2 : repResults)
//            {
//                int clusterId2 = clusterIndex.get(subdomain2);
//                if (clusterId1 == clusterId2 && subdomain1 != subdomain2)
//                {
//                    double objective1 = 0, objective2 = 0;
//                    Hashtable<String, double[]> excluded1 = excludedCopy(qosTable, subdomain1);
//                    boolean feasible1 = isFeasible(excluded1, replicationDegree);
//                    if (feasible1)
//                    {
//                        ArrayList<String> repResult1 = ILP(excluded1, replicationDegree);
//                        objective1 = objectiveValue;
//                        if(objectiveValue >= bestObjectiveValue)
//                        {
//                            bestObjectiveValue = objectiveValue;
//                            bestResult = repResult1;
//                        }
//                    }
//
//                    Hashtable<String, double[]> excluded2 = excludedCopy(qosTable, subdomain2);
//                    boolean feasible2 = isFeasible(excluded2, replicationDegree);
//                    if (feasible2)
//                    {
//                        ArrayList<String> repResult2 = ILP(excluded2, replicationDegree);
//                        objective2 = objectiveValue;
//                        if(objectiveValue >= bestObjectiveValue)
//                        {
//                            bestObjectiveValue = objectiveValue;
//                            bestResult = repResult2;
//                        }
//                    }
//                    if (feasible1 && feasible2)
//                    {
//                        if (objective1 > objective2)
//                            return advancedRecursiveILP(excluded1, replicationDegree, ++clusterNum);
//                        else
//                            return advancedRecursiveILP(excluded2, replicationDegree, ++clusterNum);
//                    }
//                    else if (feasible1)
//                    {
//                        return advancedRecursiveILP(excluded1, replicationDegree, ++clusterNum);
//                    }
//                    else if (feasible2)
//                    {
//                        return advancedRecursiveILP(excluded2, replicationDegree, ++clusterNum);
//                    }
//
//                }
//            }
//        }
//        return repResults;
//    }


    public ArrayList<String> ILP(final Hashtable<String, double[]> utilityTable, int replicationDegree, double[] weights)
    {
        /*

         */
        if (replicationDegree == 0)
        {
            return new ArrayList<String>();
        }
        SolverFactory factory = new SolverFactoryLpSolve(); // use lp_solve
        factory.setParameter(Solver.VERBOSE, 0);
        factory.setParameter(Solver.TIMEOUT, Integer.MAX_VALUE); // set timeout to 100 seconds

        /*
        A mapping between the subdomains in qosTable and their corresponding indices in the ILP
         */
        Hashtable<Integer, String> indexMap = new Hashtable<>();
        /*
        Counter of subregions with all zero qos
         */
        int zeroSubRegionCounter = 0;
        /*
        List of unavailable subdomains
         */
        ArrayList<String> unavailableSubDomains = new ArrayList<>();
        for (String subDomain : utilityTable.keySet())
        {
            if (DoubleStream.of(utilityTable.get(subDomain)).sum() == 0)
            {
                zeroSubRegionCounter++;
                unavailableSubDomains.add(subDomain);
            }
            else
            {
                indexMap.put(Integer.parseInt(subDomain, 2), subDomain);
            }
        }

        if (utilityTable.size() - zeroSubRegionCounter < replicationDegree)
        {
            throw new IllegalStateException("Pyramid.java: very small qos table size, resulted in many empty subregions, choose a bigger one: " + zeroSubRegionCounter);
        }

        /*
        Constructing the name ID similarity table
         */
        Hashtable<Integer, Hashtable<Integer, Integer>> similarity = nameIDSimilarityTable(indexMap);

        /**
         * Constructing a Problem:
         * Minimize: Sigma(t)Sigma(i)Sigma(j) utilityTable[i][j][timeSlot]* Cij * Xijt
         * Subject to:
         * for each i,j Yi>= Xij
         * Sigma(i)Xij = 1
         * Sigma(j)Xij >= Yi
         * Sigma(i)Yi <= MNR
         */

        Problem problem = new Problem();


        Linear linear = new Linear();
        /**
         * Part 1 max Sigma(t)Sigma(i)Sigma(j) utilityTable[i][j][timeSlot]* Cij * Xijt
         */
        for (int timeSlot = 0; timeSlot < SkipSimParameters.getFPTI(); timeSlot++)
        {
            for (int i : indexMap.keySet())
            {
                double utility = 0;
                //double offlineTimeSlots = offlineTimeSlotsCounter(qosTable.get(indexMap.get(i))) + 1;
                utility = utilityTable.get(indexMap.get(i))[timeSlot] * weights[timeSlot];

                if (utility == 0)
                    continue;
                else if (utility < 1.0E-10)
                {
                    throw new IllegalStateException("Pyramid.java: very low utility " + utility);
                }
                for (int j : indexMap.keySet())
                {
                    String var = "X" + i + "," + j + "," + timeSlot;
                    double coef = utility * similarity.get(i).get(j); // offlineTimeSlots;
                    /*
                    Excluding the zero-coefficients from the constraints
                     */
                    if (Math.abs(coef) == 0) continue;
                    linear.add(coef, var);
                }

            }
        }
        problem.setObjective(linear, OptType.MAX);

        /**
         * Part 2: for each timeSlot, i,j Yi>= Xijt
         */
        for (int timeSlot = 0; timeSlot < SkipSimParameters.getFPTI(); timeSlot++)
        {
            for (int i : indexMap.keySet())
            {
            /*
            discard unavailable subdomains
             */
                if (unavailableSubDomains.contains(indexMap.get(i)))
                    continue;
                for (int j : indexMap.keySet())
                {
                    linear = new Linear();
                    String var = "X" + i + "," + j + "," + timeSlot;
                    linear.add(1, var);
                    var = "Y" + i;
                    linear.add(-1, var);
                    problem.add(linear, "<=", 0);
                }

            }
        }


        /**
         * Part 3: Sigma(i)Xijt = 1 for each j and for each t
         */
        for (int timeSlot = 0; timeSlot < SkipSimParameters.getFPTI(); timeSlot++)
        {
            for (int j : indexMap.keySet())
            {
                //boolean flag = false;
                linear = new Linear();
                for (int i : indexMap.keySet())
                {
                    String var = "X" + i + "," + j + "," + timeSlot;
                    //double qos = utilityTable.get(indexMap.get(i))[timeSlot];
                    //if (qos == 0)
                    //  continue;
                    //else if (qos < 1.0E-10)
                    //{
                    //    throw new IllegalStateException("Pyramid.java: very low qos " + qos);
                    //}
                    //else if (qos > 1 || qos < 0)
                    //    throw new IllegalStateException("Pyramid.java: invalid qos value, qos should be between 0 and 1: " + qos);
                    //linear.add(qos, var);
                    linear.add(1, var);
                    //flag = true;
                }
                //if (flag)
                    problem.add(linear, "=", 1);
            }
        }


        /**
         * Part 4: for every i, Sigma(t)Sigma(j)Xijt >= Yi
         */
        for (int timeSlot = 0; timeSlot < SkipSimParameters.getFPTI(); timeSlot++)
        {
            for (int i : indexMap.keySet())
            {
            /*
            Discarding unavailable subdomains
             */
                if (unavailableSubDomains.contains(indexMap.get(i)))
                    continue;
                String var;
                linear = new Linear();
                for (int j : indexMap.keySet())
                {
                    var = "X" + i + "," + j + "," + timeSlot;
                    linear.add(-1, var);
                }
                var = "Y" + i;
                linear.add(1, var);
                problem.add(linear, "<=", 0);
            }
        }


        /**
         * Part 5: Sigma(i)Yi = replication degree
         */
        linear = new Linear();
        for (int i : indexMap.keySet())
        {
            /*
            Discarding unavailable subdomains
             */
            if (unavailableSubDomains.contains(indexMap.get(i)))
                continue;
            String var = "Y" + i;
            linear.add(1, var);
        }
        problem.add(linear, "=", replicationDegree);


        /**
         * Types of X and Y
         */
        for (int i : indexMap.keySet())
        {
            /*
            Discarding unavailable subdomains
             */
            if (unavailableSubDomains.contains(indexMap.get(i)))
                continue;
            for (int timeSlot = 0; timeSlot < SkipSimParameters.getFPTI(); timeSlot++)
            {
                for (int j : indexMap.keySet())
                {
                    linear = new Linear();
                    String var = "X" + i + "," + j + "," + timeSlot;
                    linear.add(1, var);
                    problem.add(linear, ">=", 0);
                    linear = new Linear();
                    linear.add(1, var);
                    problem.add(linear, "<=", 1);

                }
            }

            linear = new Linear();
            String var = "Y" + i;
            linear.add(1, var);
            problem.add(linear, "<=", 1);

            linear = new Linear();
            var = "Y" + i;
            linear.add(1, var);
            problem.add(linear, ">=", 0);
        }


        /**
         * Part 7: Set the type of Xij and Yi
         */
        for (int i : indexMap.keySet())
        {
            /*
            Discarding unavailable subdomains
             */
            if (unavailableSubDomains.contains(indexMap.get(i)))
                continue;
            for (int timeSlot = 0; timeSlot < SkipSimParameters.getFPTI(); timeSlot++)
            {
                for (int j : indexMap.keySet())
                {
                    String var = "X" + i + "," + j + "," + timeSlot;
                    problem.setVarType(var, Integer.class);
                }
            }

            String var = "Y" + i;
            problem.setVarType(var, Integer.class);
        }


        /**
         * Solving the problem
         */
        Solver solver = factory.get(); // you should use this solver only once for one problem
        //System.out.println(problem.toString());
        Result result = solver.solve(problem);
        if (result != null) objectiveValue = result.getObjective().floatValue();
        if (objectiveValue <= 0)
        {
            throw new IllegalStateException("Pyramid.java: illegal objective value of ILP obtained: " + objectiveValue);
        }
        return extractReplicaList(result, indexMap, replicationDegree);
    }

    public Hashtable<Integer, Hashtable<Integer, Integer>> nameIDSimilarityTable(Hashtable<Integer, String> mappingIndices)
    {
        Hashtable<Integer, Hashtable<Integer, Integer>> similarity = new Hashtable<>();
        for (int index1 : mappingIndices.keySet())
            for (int index2 : mappingIndices.keySet())
            {
                if (similarity.get(index1) == null)
                {
                    similarity.put(index1, new Hashtable<Integer, Integer>());
                }
                if (SkipSimParameters.isHeterogeneous())
                {
                    int normalizedSimilarity = commonBits(mappingIndices.get(index1), mappingIndices.get(index2));
                    similarity.get(index1).put(index2, normalizedSimilarity);
                }
                else
                {
                    /*
                    The simulation is not QoS based so the locality awareness is discarded
                     */
                    similarity.get(index1).put(index2, 1);
                }
            }
        return similarity;
    }

    /**
     * Given an object of Result from ILP solver, this function extracts the list of replicas and returns the list of replicas prefixes
     *
     * @param result         the instance of lpsolve Result object
     * @param mappingIndices the mapping index between the indices in result and the subdomains
     * @return the array list containing the list of replicas subdomain
     */
    private ArrayList<String> extractReplicaList(Result result, Hashtable<Integer, String> mappingIndices, int replicationDegree)
    {
        ArrayList<String> replicaPrefix = new ArrayList<>();
        if (result != null)
        {
            String r = result.toString();
            for (int i : mappingIndices.keySet())
            {
                if (r.contains("Y" + i + "=1"))
                {
                    String subdomain = mappingIndices.get(i);
                    if (subdomain == null || subdomain.isEmpty())
                        throw new IllegalStateException("Pyramid.java: no map for the index in replica extractor: " + i);
//                    if (!availableSubDomains.contains(sgo.getTG().mLandmarks.getDynamicPrefix(region) + subdomain))
//                        throw new IllegalStateException("Pyramid.java: unavailable subdomain selected for replication: " + subdomain);
                    replicaPrefix.add(subdomain);
                }
            }
            if (replicaPrefix.size() != replicationDegree)
            {
                throw new IllegalStateException("Pyramid.java: replica extractor found a violating number of replicas: " + replicaPrefix.size());
            }
        }
        return replicaPrefix;
    }

    /**
     * Given a prefix, finds the node from that domain of name ID with the highest QoS
     *
     * @param prefix the desire prefix of node
     * @param nodes  the Nodes set
     * @return index of the best match or -1 if there is no match available
     */
    public int searchForUtility(String prefix, Nodes nodes, int dataOwnerIndex)
    {
        ArrayList<Node> nodeMatches = new ArrayList<>();
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            Node node = (Node) nodes.getNode(i);
            //System.out.println(node.getNameID());
            if (node.isOnline() && node.getNameID().startsWith(prefix) && !node.isReplica(dataOwnerIndex) && node.hasStorageCapacity())
            {
                nodeMatches.add(node);
            }
        }

        /*
        Trying alpha-many random nodes that match the search requirements and finding the best one
         */
        int triedCandidates = 0;
        Random random = new Random();
        double maxQoS = 0;
        int bestMatch = -1;
        while (triedCandidates < SkipSimParameters.getSearchForUtilityAlpha() && !nodeMatches.isEmpty())
        {
            int randomIndex = 0;
            if(nodeMatches.size() > 1)
            {
                randomIndex = random.nextInt(nodeMatches.size() - 1);
            }
            Collections.shuffle(nodeMatches);

            Node node = nodeMatches.get(randomIndex);
            nodeMatches.remove(randomIndex);
            double norm = secondAvailabilityBasedNorm(node.getAvailabilityVector());
            int replicationLoad = node.getReplicatedLoad() + 1;
            double score = node.getBandwidthCapacity() * norm / replicationLoad;
            if (score > maxQoS)
            {
                bestMatch = node.getIndex();
                maxQoS = score;
            }
            triedCandidates++;
        }

        if (bestMatch == -1)
        {
            throw new IllegalStateException("Pyramid.java: could not find the best match for replica: " + prefix);
        }
        return bestMatch;
    }


    public Hashtable<String, double[]> excludedCopy(Hashtable<String, double[]> inputTable, String excludedKey)
    {
        Hashtable<String, double[]> newTable = new Hashtable<>();
        for (String key : inputTable.keySet())
        {
            if (key.equals(excludedKey))
                continue;
            newTable.put(key, inputTable.get(key));
        }
        return newTable;
    }

    /**
     * Given a qos table and a replication degree, this function evaluates whether the ILP is feasible to solve or not
     *
     * @param qosTable          the qos table of the subdomains
     * @param replicationDegree the replication degree
     * @return TRUE if ILP is feasible i.e., there are at least replicationDegree-many subdomains with non-zero qos vector,
     * false otherwise.
     */
    private boolean isFeasible(Hashtable<String, double[]> qosTable, int replicationDegree)
    {
        int zeroSubRegionCounter = 0;
        for (String subDomain : qosTable.keySet())
        {
            if (DoubleStream.of(qosTable.get(subDomain)).sum() == 0)
                zeroSubRegionCounter++;
        }

        if (qosTable.size() - zeroSubRegionCounter < replicationDegree)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    private int[] availabileSubregions(final Hashtable<String, double[]>[] qosTable)
    {
        int[] nonZeroSubRegionCounter = new int[SkipSimParameters.getLandmarksNum()];
        Arrays.fill(nonZeroSubRegionCounter, 0);
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            for (String subDomain : qosTable[i].keySet())
            {
                if (DoubleStream.of(qosTable[i].get(subDomain)).sum() > 0)
                    nonZeroSubRegionCounter[i]++;
            }
        }
        return nonZeroSubRegionCounter;
    }

    private ArrayList<String> availabileSubregionsList(final Hashtable<String, double[]>[] qosTable)
    {
        ArrayList<String> nonZeroSubRegionCounter = new ArrayList<String>();
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            for (String subDomain : qosTable[i].keySet())
            {
                if (DoubleStream.of(qosTable[i].get(subDomain)).sum() > 0)
                    nonZeroSubRegionCounter.add(sgo.getTG().mLandmarks.getDynamicPrefix(i) + subDomain);
            }
        }
        return nonZeroSubRegionCounter;
    }

    public void resetObjectiveValue()
    {
        objectiveValue = 0;
        bestResult = new ArrayList<>();
        bestObjectiveValue = 0;
    }

    /**
     * Receiving the qosTable of each region, this function computes the average qos vector and returns
     *
     * @param qosTable the input qosTable
     * @return the average vector of the qosTable
     */
    public double[] averageQosTable(final Hashtable<String, double[]> qosTable)
    {
        if (qosTable == null)
            return null;
        double[] averageQos = new double[SkipSimParameters.getFPTI()];
        if (qosTable.isEmpty())
            return averageQos;
        Arrays.fill(averageQos, 0);
        for (String subDomain : qosTable.keySet())
        {
            for (int time = 0; time < SkipSimParameters.getFPTI(); time++)
            {
                averageQos[time] += qosTable.get(subDomain)[time];
            }
        }

        for (int time = 0; time < SkipSimParameters.getFPTI(); time++)
        {
            averageQos[time] /= qosTable.size();
        }
        return averageQos;
    }

    public double[] availabilityWeightsDistribution(double[] counter)
    {
        if (counter == null)
        {
            return null;
        }
        /*
        Average of the averageQosArray
         */
        double sum = DoubleStream.of(counter).sum();
        double[] weights = new double[counter.length];
        for (int i = 0; i < counter.length; i++)
        {
            if (sum == 0)
                weights[i] = 0;
            else
                weights[i] = counter.length * (counter[i] / sum);
        }

        return weights;
    }

    /**
     * Given the aggregated qosTable, and its average vector, it returns the number of eateries of qosTable that are
     * below the average at each time slot (at each element)
     *
     * @param qosTable      The aggregated qosTable of a region, each row represents an aggregated subdomain, and each column
     *                      represents a time slot
     * @param averageVector The average vector of qosTable, each element represents the average of qosTable entries at that
     *                      time slot
     * @return an array with the same size as the average vector, where each elements represents the number of entries of the
     * qosTable that are below the average at the time slot corresponding to that element.
     */
    public double[] belowAverageItems(final Hashtable<String, double[]> qosTable, final double[] averageVector)
    {
        if (averageVector == null || qosTable == null)
        {
            return null;
        }

        /*
        Keeps counter of the number of items in QoS table that are below the average
         */
        double[] counter = new double[averageVector.length];
        Arrays.fill(counter, 0);

        for (String subDomain : qosTable.keySet())
        {
            for (int time = 0; time < averageVector.length; time++)
            {
                if (qosTable.get(subDomain)[time] <= averageVector[time])
                    counter[time]++;
            }
        }
        return counter;
    }
}
