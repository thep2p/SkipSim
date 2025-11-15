package Replication;

import Simulator.SkipSimParameters;
import SkipGraph.Node;
import SkipGraph.SkipGraphOperations;
import net.sf.javailp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Hashtable;

/**
 * Created by Yahya on 6/13/2016.
 */
public class Awake extends Replication
{
    private static final Logger log = LoggerFactory.getLogger(Awake.class);
    @Override
    public void Algorithm(SkipGraphOperations sgo, int dataOwnerID)
    {
        this.sgo = sgo;
        resetRep();
//
//        double[] availability = new double[Simulator.system.getNumIDSeed()];
//        for (int i = 0; i < Simulator.system.getNumIDSeed(); i++)
//        {
//            availability[i] = DataTypes.nodesTimeTable.totalAvailabilityChanceOfThisNumId(i);
//        }
//        Developments.clustering.Developments.clustering(availability);
//        System.out.println("The great Awake has been started");
//        replicaSetGeneratorZY(ReplicaOptimizer(Simulator.system.getNumIDSeed()), Simulator.system.getNumIDSeed());
        Node dataOwner = (Node) sgo.getTG().getNodeSet().getNode(dataOwnerID);
        Result result = ILP(SkipSimParameters.getReplicationDegree(), dataOwner.getAvailabilityTable());
        replicaSetGenerator(result, SkipSimParameters.getSystemCapacity(), dataOwnerID, this.getClass());

    }


//    private static void numOfReplicas()
//    {
//        int counter = 0;
//        for (int i = 0; i < Simulator.system.getNumIDSeed(); i++)
//        {
//            if (repTools.getDynamicRealWorldReplicaSet(i))
//            {
//                counter++;
//            }
//        }
//
//        System.out.println("Number of replicas obtained by LP " + counter);
//    }

    protected Result ILP(int repDegree, Hashtable<Integer, double[]> availabilityTable)
    {

        if (availabilityTable == null)
        {
            return null;
        }
        int timeSlots = 0;
        if (availabilityTable.get(0) != null)
            timeSlots = availabilityTable.get(0).length;
        SolverFactory factory = new SolverFactoryLpSolve(); // use lp_solve
        factory.setParameter(Solver.VERBOSE, 0);
        factory.setParameter(Solver.TIMEOUT, Integer.MAX_VALUE);

        /*A new LP problem*/
        Problem problem = new Problem();

        /*The objective
         * Maximize sigma(i) Ui
         * Ui is the availability of replicas at timeSlot i
         */
        Linear linear = new Linear();
        String objective;
        for (int i = 0; i < timeSlots; i++)
        {
            objective = "U" + i;
            linear.add(1, objective);
        }

        //System.out.println(linear.toString());
        /*
        Maximize LP objective
         */
        problem.setObjective(linear, OptType.MAX);


        /*
         * Part 1: for each t Ut = sigma(i) YiTit
         * Ut represents the availability per hour
         */
        for (int t = 0; t < timeSlots; t++)
        {
            linear = new Linear();
            linear.add(-1, "U" + t);

            for (int i = 0; i < availabilityTable.size(); i++)
            {
                String cons1 = "Y" + i;
                /*
                Extracting availability probability of node i at timeslot t
                 */
                double prob = availabilityTable.get(i)[t];
                linear.add(prob, cons1);
            }
            problem.add(linear, "=", 0);
            //System.out.println(linear.toString());
        }


        /*
         * Part 2: sigma(i) Yi = MNR
         */
        linear = new Linear();
        for (int i = 0; i < availabilityTable.size(); i++)
        {
            String cons2 = "Y" + i;
            linear.add(1, cons2);
        }
        problem.add(linear, "=", repDegree);

        /*
         * Part 3 = Constraint on Yi
         * Yi >= 0 && Yi <= 1
         */
        for (int i = 0; i < availabilityTable.size(); i++)
        {

            linear = new Linear();
            String cons3 = "Y" + i;
            linear.add(-1, cons3);
            problem.add(linear, "<=", 0);

            linear = new Linear();
            String cons4 = "Y" + i;
            linear.add(1, cons4);
            problem.add(linear, "<=", 1);

        }


        /*
         * Part 4: Set the type of Yi
         */
        for (int i = 0; i < availabilityTable.size(); i++)
        {

            String cons5 = "Y" + i;
            problem.setVarType(cons5, Integer.class);


        }


        /**
         * Solving the problem
         */
        Solver solver = factory.get(); // you should use this solver only once for one problem
        Result result = solver.solve(problem);
        if (result != null)
            log.debug("ILP optimization objective: {}", result.getObjective());
        return result;
    }

    protected HashSet<Integer> replicaSetGenerator(Result R, int systemSize, int dataOwnerID, Class caller)
    {
        HashSet<Integer> replicaIndices = new HashSet<>();
        if (R == null)
        {
            throw new IllegalStateException("Awake: replicaSetGenerator is invoked on a null Result value");
        }
        String result;
        result = R.toString();
        for (int i = 0; i < systemSize; i++)
        {
            String target = "Y" + i + "=1";
            if (result.contains(target))
            {
                replicaIndices.add(i);
                try
                {
                    ((Node) sgo.getTG().mNodeSet.getNode(i)).setAsReplica(dataOwnerID);
                }
                catch (NullPointerException ex)
                {
                    if (!caller.getName().toLowerCase().contains("test"))
                    {
                        ex.printStackTrace();
                    }
                    else
                    {
                        continue;
                    }
                }
            }
        }
        return replicaIndices;
    }

}
