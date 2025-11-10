//import net.sf.javailp.*;
//
//import java.util.Random;
//
///**
// * Created by Yahya on 5/12/2016.
// */
//public class AvailabilityRep_Public_Alg06_LPOnMaximizingAvailabilityPerHour
//{
//    private static int     dataOwner = 0;
//
//
//    public static void Algorithm()
//    {
//        repTools.reset();
//        repTools.tablesInit();
//        repTools.dynamicReplicaSetInit();
//
//
////        System.out.println();
////        for(int i = 0 ; i < Simulator.system.numIDSeed ; i++)
////        {
////            for (int t = 0; t < repTools.getTimeSlots(); t++)
////            {
////                double prob =  ( DataTypes.nodesTimeTable.getAvailabilityProbability(i, t));
////                prob = Math.log(prob * 100);
////                if(prob < -1000 || prob == 0)
////                    prob = Math.log(0.01);
////                System.out.print(String.format( "%.2f", prob) + " ");
////            }
////            System.out.println();
////            System.out.println();
////        }
//        //availabilityParameter = computeAvailabilityParameter();
//
//
//        System.out.println("LP on maximizing availability per hour started");
//        replicaSetGeneratorZY(ReplicaOptimizer(Simulator.system.getNumIDSeed()), Simulator.system.getNumIDSeed());
//        numOfReplicas();
//    }
//
//
//
//    private static void numOfReplicas()
//    {
//        int counter = 0;
//        for(int i = 0 ; i < Simulator.system.getNumIDSeed(); i++)
//        {
//            if(repTools.getDynamicRealWorldReplicaSet(i))
//            {
//                counter++;
//            }
//        }
//
//        System.out.println("Number of replicas obtained by LP " + counter);
//        //averageNumberOfReps += counter;
//
//        if(Simulator.system.getCurrentTopologyIndex() == Simulator.system.getTopologyNumbers())
//        {
//            //averageNumberOfReps /= Simulator.system.simRun;
//            //System.out.println("Average number of replicas obtained by LP over the simulations was " + averageNumberOfReps);
//        }
//
//    }
//
//    private static Result ReplicaOptimizer(int size)
//    {
//        SolverFactory factory = new SolverFactoryLpSolve(); // use lp_solve
//        factory.setParameter(Solver.VERBOSE, 0);
//        factory.setParameter(Solver.TIMEOUT, Integer.MAX_VALUE);
//
//		 /*A new LP problem*/
//        Problem problem = new Problem();
//
//		 /*The objective
//		  * minimize sigma(i) Ui
//		  */
//        Linear linear = new Linear();
//        String objective = new String();
//        for(int t = 0 ; t < repTools.getTimeSlots() ; t++)
//        {
//            objective = new String();
//            objective = "U"+t;
//            linear.add(1, objective);
//        }
//
//        //System.out.println(linear.toString());
//        /*
//        Maximize LP objective
//         */
//        problem.setObjective(linear, OptType.MAX);
//
//
//		 /*
//		  * Part 1: for each t Ut = sigma(i) YiTit
//		  * Ut represents the availability per hour
//		  */
//        for(int t = 0 ; t < repTools.getTimeSlots() ; t++)
//        {
//            linear = new Linear();
//            linear.add(-1, "U"+t);
//
//            for (int i = 0; i < size; i++)
//            {
//                String cons1 = new String();
//                cons1 = "Y" + i;
//                //if(DataTypes.nodesTimeTable.getAvailabilityProbability(i,t) > 0.5)
//                double prob = (double) (DataTypes.nodesTimeTable.getAvailabilityProbability(i, t));
//                prob = Math.log(prob * 100);
//                if(prob < -1000 || prob == 0)
//                    prob = Math.log(0.01);
//                linear.add(prob, cons1);
//
//            }
//            problem.add(linear, "=", 0);
//            //System.out.println(linear.toString());
//        }
//
//
//		 /*
//		  * Part 2: sigma(i) Yi = MNR
//		  */
//        linear = new Linear();
//        for(int i = 0 ; i < size ; i++)
//        {
//            String cons2 = "Y" + i;
//            linear.add(1, cons2);
//        }
//        problem.add(linear, "=", Simulator.system.getReplicationDegree());
//
//		 /*
//		  * Part 3 = Constraint on Yi
//		  * Yi >= 0 && Yi <= 1
//		  */
//        linear = new Linear();
//        for(int i = 0 ; i < size ; i++)
//        {
//            linear = new Linear();
//            String cons3 = "Y"+i;
//            linear.add(-1, cons3);
//            problem.add(linear, "<=", 0);
//
//            linear = new Linear();
//            String cons4 = "Y"+i;
//            linear.add(1, cons4);
//            problem.add(linear, "<=", 1);
//        }
//
//
//		 /*
//		  * Part 4: Set the type of Yi
//		  */
//        for(int i = 0 ; i < size ; i++)
//        {
//            String cons5 = "Y"+i;
//            problem.setVarType(cons5, Integer.class);
//        }
//
//
//        /**
//         * Solving the problem
//         */
//        Solver solver = factory.get(); // you should use this solver only once for one problem
//        Result result = solver.solve(problem);
//        System.out.println(result.getObjective().toString());
//        return(result);
//    }
//
//    /**
//     * This function is ONLY should be called on the results coming from AvailabilityOptimizer
//     * In the other word the results should only have Z and Y in it.
//     * @param R
//     * @param size
//     * @return 0 if result is understandable, -1 otherwise
//     */
//    private static int replicaSetGeneratorZY(Result R, int size)
//    {
//        if(R == null)
//            return -1;
//        String result = new String();
//        result = R.toString();
//        //System.out.println("The Results of LP: ");
//        //System.out.println(result);
//        int counter = 0;
//        for(int i = 0 ; i < size ; i++)
//        {
//            String target = "Y"+i+"=1";
//            if(result.contains(target))
//            {
//                repTools.setDeyamicRealWorldReplicaSet(i, true);
//
//                counter ++;
//            }
//        }
//        System.out.println("reptools\\replicaSetGeneratorZY: Number of extracted replicas from LP " + counter);
//        return 0;
//    }
//}
