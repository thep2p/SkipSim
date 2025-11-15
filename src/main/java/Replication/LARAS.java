package Replication;

import Simulator.SkipSimParameters;
import SkipGraph.Node;
import SkipGraph.SkipGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.sf.javailp.*;

import java.util.ArrayList;


public class LARAS extends Replication
{
    private static final Logger log = LoggerFactory.getLogger(LARAS.class);

    @Override
    public void Algorithm(SkipGraphOperations inputSgo, int dataOwnerIndex)
    {
        this.sgo = inputSgo;
        resetRep();
        dataRequesterPopulation();
        populationBasedRepShareDefining();
        adaptivePopulationBasedSubproblemSizeDefining();
        RWD(dataOwnerIndex);
    }


    private void RWD(int dataOwnerIndex)
    {

        log.info("LARAS RWD started [dataOwner={}, regions={}, capacity={}, topology={}]",
            dataOwnerIndex,
            SkipSimParameters.getLandmarksNum(),
            SkipSimParameters.getSystemCapacity(),
            SkipSimParameters.getCurrentTopologyIndex());
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            if (getSubReplicationDegree(i) == 0) continue;
            int[][] nameIDTable = virtualSystemNameIDTable(getAdaptiveSubproblemSizes(i));
            Result result = ILP(nameIDTable, Math.max(SkipSimParameters.getReplicationDegree(), 16), getSubReplicationDegree(i), i, new ArrayList<String>());
            replicaSetGenerator(result, Math.max(SkipSimParameters.getReplicationDegree(), 16), i, dataOwnerIndex);
        }
        sgo.getTG().getNodeSet().setCorrespondingReplica(dataOwnerIndex);
        //TOdo ready to detach
//        double averageAccessDelay = 0;
//
//        if (isPublicReplication()) averageAccessDelay = publicAverageDelay();
//        else averageAccessDelay = privateAverageDelay();
//        System.out.println("Average Delay " + averageAccessDelay + " Run " + SkipSimParameters.getCurrentTopologyIndex());
//        return averageAccessDelay;
    }

    private double replicaSetGenerator(Result R, int virtualSystemSize, int landmarkIndex, int dataOwnerID)
    {
        if (R == null)
        {
            throw new IllegalStateException("LARAS: replicaSetGenerator is invoked on a null Result value");
        }
        String result;
        result = R.toString();
        boolean flag = false;
        for (int i = 0; i < virtualSystemSize; i++)
        {
            String target = "Y" + i + "=1";
            if (result.contains(target))
            {
                int replicaIndex = mapToOriginalSystem(i, landmarkIndex, virtualSystemNameIDSize(virtualSystemSize), dataOwnerID);
                //Todo should throw exception if node replicaIndex already assigned as a replica
                ((Node) sgo.getTG().mNodeSet.getNode(replicaIndex)).setAsReplica(dataOwnerID);
                flag = true;
            }
        }

        if (!flag) return -1;
        return 0;
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
            if (virtualNameID.equals((sgo.getTG().mNodeSet.getNode(i)).getNameID()))
            {
                //System.out.println("Closest name id to " + virtualNameID + " is " + ((Node) sgo.getTG().mNodeSet.getNode(i)).getNameID());
                return i;
            }
            else if (commonBits(virtualNameID, (sgo.getTG().mNodeSet.getNode(i)).getNameID()) >= maxCommon
                    && !((Node) sgo.getTG().mNodeSet.getNode(i)).isReplica(dataOwnerIndex)
                    && ((Node) sgo.getTG().mNodeSet.getNode(i)).isOnline())
            {
                maxCommon = commonBits(virtualNameID, ((Node) sgo.getTG().mNodeSet.getNode(i)).getNameID());
                maxIndex = i;
            }
        }
        //System.out.println("Closest name id to " + virtualNameID + " is " + ((Node) sgo.getTG().mNodeSet.getNode(maxIndex)).getNameID() + " with " + maxCommon + " bits name id common prefix length");
        return maxIndex;
    }

    /**
     * Given the size of the virtual system, it returns the name ID size of the virtual system
     *
     * @param virtualSystemSize the name ID size of virtual system
     * @return name ID size of the virtual system
     */
    protected int virtualSystemNameIDSize(int virtualSystemSize)
    {
        int virtualSystemNameIDSize = 1;
        while (Math.pow(2, virtualSystemNameIDSize) < virtualSystemSize) virtualSystemNameIDSize++;
        return virtualSystemNameIDSize;
    }

    /**
     * Given the size of virtual system (in nodes), this function creates and returns a name ID table assuming that the
     * virtual system is full size i.e., all name IDs exist. The table[i][j] returns the approximated latency between
     * name IDs i and j assuming that the system is in full size
     *
     * @param virtualSystemSize name ID size of virtual system
     * @return the name Id distance table
     */
    protected int[][] virtualSystemNameIDTable(int virtualSystemSize)
    {
        int virtualSystemNameIDSize = 1;
        while (Math.pow(2, virtualSystemNameIDSize) < virtualSystemSize) virtualSystemNameIDSize++;
        int[][] vsNameIDTable = new int[virtualSystemSize][virtualSystemSize];
        for (int i = 0; i < virtualSystemSize; i++)
            for (int j = 0; j < virtualSystemSize; j++)
            {
                if (i == j)
                {
                    vsNameIDTable[i][j] = 0;
                }
                else
                    vsNameIDTable[i][j] = virtualSystemNameIDSize - commonBitsSubProblemSize(i, j, virtualSystemNameIDSize);
            }
        return vsNameIDTable;
    }


    /**
     * This function computes and retuns the common prefix of two nodes in the virtual system given their indices only
     *
     * @param i                       the first node's index
     * @param j                       the second node's index
     * @param virtualSystemNameIDSize the virtual system name ID size
     * @return the common prefix length in the name IDs of nodes i and j
     */
    protected int commonBitsSubProblemSize(int i, int j, int virtualSystemNameIDSize)
    {
        String s1 = Integer.toBinaryString(i);
        String s2 = Integer.toBinaryString(j);
        while (s1.length() < virtualSystemNameIDSize) s1 = "0" + s1;
        while (s2.length() < virtualSystemNameIDSize) s2 = "0" + s2;
        return commonBits(s1, s2);
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
                    String nameID = ((Node) sgo.getTG().mNodeSet.getNode(i)).getNameID();
                    String prefixFreeNameID = nameID.substring(nameID.length() - SkipSimParameters.getNameIDLength());
                    String squeezedNameID = prefixFreeNameID.substring(0, virtualNameIDSize);
                    int virtualSystemIndex = Integer.parseInt(squeezedNameID);
                    if (!dataRequesters.contains(virtualSystemIndex)) dataRequesters.add(virtualSystemIndex);
                }
            }
        }
        return dataRequesters;
    }

    public Result ILP(int[][] L, int size, int MNR, int landmarkIndex, ArrayList<String> badCandidates)
    {

        SolverFactory factory = new SolverFactoryLpSolve(); // use lp_solve
        factory.setParameter(Solver.VERBOSE, 0);
        factory.setParameter(Solver.TIMEOUT, Integer.MAX_VALUE); // set timeout to 100 seconds

        ArrayList<Integer> badCandidateIndices = new ArrayList<>();
        for (int i = 0; i < size; i++)
        {
            for (String nameID : badCandidates)
            {
                if (commonBits(nameID, toNameID(i, virtualSystemNameIDSize(size))) >= nameID.length() && !badCandidateIndices.contains(i))
                    badCandidateIndices.add(i);
            }
        }


        ArrayList<Integer> dataRequesters = dataRequesterIndices(size, landmarkIndex);

        /**
         * Constructing a Problem:
         * Minimize: Sigma(i)Sigma(j) LijXij
         * Subject to:
         * for each i,j Yi>= Xij
         * Sigma(i)Xij = 1
         * Sigma(j)Xij >= Yi
         * Sigma(i)Yi <= MNR
         */

        Problem problem = new Problem();

        /**
         * Part 1: Minimize: Sigma(i)Sigma(j) LijXij
         */
        Linear linear = new Linear();
        for (int i = 0; i < size; i++)
        {
            if (badCandidateIndices.contains(i)) continue;
            for (int j = 0; j < size; j++)
            {
                if (badCandidateIndices.contains(j)) continue;
                if (!dataRequesters.contains(j)) continue;
                String var = "X" + i + "," + j;
                linear.add(L[i][j], var);
            }
        }
        problem.setObjective(linear, OptType.MIN);


        /**
         * Part 2: for each i,j Yi>= Xij
         */
        for (int i = 0; i < size; i++)
        {
            if (badCandidateIndices.contains(i)) continue;
            for (int j = 0; j < size; j++)
            {
                if (badCandidateIndices.contains(j)) continue;
                //if ((repType == Simulator.system.PRIVATE_REPLICATION && j >= Simulator.system.getDataRequesterNumber())) continue;
                linear = new Linear();
                String var = "X" + i + "," + j;
                linear.add(1, var);
                var = "Y" + i;
                linear.add(-1, var);
                problem.add(linear, "<=", 0);
            }

        }

        /**
         * Part 3: Sigma(i)Xij = 1
         */
        for (int j = 0; j < size; j++)
        {
            if (badCandidateIndices.contains(j)) continue;
            //if ((repType == Simulator.system.PRIVATE_REPLICATION && j >= Simulator.system.getDataRequesterNumber())) continue;
            linear = new Linear();
            for (int i = 0; i < size; i++)
            {
                if (badCandidateIndices.contains(i)) continue;
                String var = "X" + i + "," + j;
                linear.add(1, var);
            }
            problem.add(linear, "=", 1);
        }


        /**
         * Part 4: Sigma(j)Xij >= Yi
         */
        for (int i = 0; i < size; i++)
        {
            if (badCandidateIndices.contains(i)) continue;
            linear = new Linear();
            for (int j = 0; j < size; j++)
            {
                if (badCandidateIndices.contains(j)) continue;
                //if ((repType == Simulator.system.PRIVATE_REPLICATION && j >= Simulator.system.getDataRequesterNumber())) continue;
                String var = "X" + i + "," + j;
                linear.add(-1, var);
            }

            String var = "Y" + i;
            linear.add(1, var);
            problem.add(linear, "<=", 0);
        }


        /**
         * Part 5: Sigma(i)Yi <= MNR
         */
        linear = new Linear();
        for (int i = 0; i < size; i++)
        {
            if (badCandidateIndices.contains(i)) continue;
            String var = "Y" + i;
            linear.add(1, var);
        }
        problem.add(linear, "=", MNR);


        /**
         * Part 6: Sigma(j)Xij >= Yi
         */
        for (int i = 0; i < size; i++)
        {
            if (badCandidateIndices.contains(i)) continue;

            for (int j = 0; j < size; j++)
            {
                if (badCandidateIndices.contains(j)) continue;
                //if (repType == Simulator.system.PRIVATE_REPLICATION && j >= Simulator.system.getDataRequesterNumber()) continue;
                linear = new Linear();
                String var = "X" + i + "," + j;
                linear.add(1, var);
                problem.add(linear, ">=", 0);
                linear = new Linear();
                linear.add(1, var);
                problem.add(linear, "<=", 1);

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
        for (int i = 0; i < size; i++)
        {
            if (badCandidateIndices.contains(i)) continue;
            for (int j = 0; j < size; j++)
            {
                if (badCandidateIndices.contains(j)) continue;
                //if ((repType == Simulator.system.PRIVATE_REPLICATION && j > Simulator.system.getDataRequesterNumber())) continue;
                String var = "X" + i + "," + j;
                problem.setVarType(var, Integer.class);
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


        //System.out.println(result.get("X30,30"));
        //System.out.println(result);

        //System.exit(0);
        return (result);
    }

    /**
     * Given index of a node in virtual system, this function returns the name ID of that node in the virtual system
     *
     * @param index                   index of node in virtual system
     * @param virtualSystemNameIDSize the name ID size of virtual system
     * @return name ID of the node in virtual system
     */
    protected String toNameID(int index, int virtualSystemNameIDSize)
    {
        String nameID = Integer.toBinaryString(index);
        while (nameID.length() < virtualSystemNameIDSize) nameID = "0" + nameID;
        return nameID;
    }


}