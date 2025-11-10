//import net.sf.javailp.*;
//
//public class AvailabilityRep_Public_Alg03_LPOnAvailability
//{
//	private static double  averageNumberOfReps  = 0;
//	 public static void Algorithm()
//	 {
//		   System.out.println("LP on availability starts");
//		   repTools.reset();
//		   repTools.tablesInit();
//		   repTools.dynamicReplicaSetInit();
//
////		   for(int i = 0 ; i < Simulator.system.numIDSeed ; i++)
////		   {
////			   System.out.println(DataTypes.nodesTimeTable.globalAvailabilityProbability[i]);
////		   }
//
//		   /*
//		    * Models the LP on only the availability and generates the set of replicas
//		    * Note that in contrast to the other LPs, the LP on availability is solved on the numIDseed not the
//		    * Simulator.system size.
//		    */
//		   replicaSetGeneratorZY(AvailabilityOptimizer(Simulator.system.getNumIDSeed()), Simulator.system.getNumIDSeed());
//
//
//
//	 }
//
//	/**
//	 * This function solves a LP problem that optimizes the availability, given the number of replicas
//	 *
//	 * @return the result of the linear programming as a String
//	 */
//	private static Result AvailabilityOptimizer(int size)
//	{
//		SolverFactory factory = new SolverFactoryLpSolve(); // use lp_solve
//		factory.setParameter(Solver.VERBOSE, 0);
//		factory.setParameter(Solver.TIMEOUT, Integer.MAX_VALUE);
//
//		 /*A new LP problem*/
//		Problem problem = new Problem();
//
//		 /*The objective
//		  * maximize sigma(i) Zit * Tit
//		  */
//		Linear linear = new Linear();
//		String objective = new String();
//		for(int i = 0 ; i < size ; i++)
//		{
//			for(int t = 0; t < repTools.getTimeSlots(); t++)
//			{
//				objective = new String();
//				objective = "Z"+i+","+t;
//				double prob = (DataTypes.nodesTimeTable.getAvailabilityProbability(SkipGraph.Nodes.getNode(i).getNumID(), t));
//
//				linear.add(prob, objective);
//			}
//
//		}
//		//System.out.println("The Objective" + linear.toString());
//		problem.setObjective(linear, OptType.MAX);
//
//
//		 /*
//		  * Part 1: for each i and t Yi > Zit
//		  * In the other word if a SkipGraph.Node is a replica during one time slot, it should be counted as a replica
//		  */
//		for(int i = 0 ; i < size ; i++)
//		{
//			for(int t = 0 ; t < repTools.getTimeSlots() ; t++)
//			{
//				String cons1 = new String();
//				linear = new Linear();
//				cons1 = new String();
//				cons1 = "Y"+i;
//				linear.add(-1, cons1);
//
//				cons1 = new String();
//				cons1 = "Z"+i+","+t;
//				linear.add(+1, cons1);
//
//				problem.add(linear, "<=", 0);
//			}
//
//		}
//
//		 /*
//		  * Part 3: Sigma(i) Yi <= MNR
//		  */
//		linear = new Linear();
//		for(int i = 0 ; i < size ; i++)
//		{
//			String cons2 = "Y"+i;
//			linear.add(1, cons2);
//		}
//		problem.add(linear, "<=", Simulator.system.getReplicationDegree());
//
//		 /*
//		  * Constraint on Yi
//		  * Yi >= 0 && Yi <= 1
//		  */
//		linear = new Linear();
//		for(int i = 0 ; i < size ; i++)
//		{
//			linear = new Linear();
//			String cons3 = "Y"+i;
//			linear.add(-1, cons3);
//			problem.add(linear, "<=", 0);
//
//			linear = new Linear();
//			String cons4 = "Y"+i;
//			linear.add(1, cons4);
//			problem.add(linear, "<=", 1);
//		}
//
//
//		 /*
//		  * Part 3: Set the type of Xij and Yi
//		  */
//		for(int i = 0 ; i < size ; i++)
//		{
//			for(int t = 0 ; t < repTools.getTimeSlots() ; t++)
//			{
//				String cons3 = "Z"+i+","+t;
//				problem.setVarType(cons3, Integer.class);
//			}
//
//			String cons3 = "Y"+i;
//			problem.setVarType(cons3, Integer.class);
//		}
//
//
//		/**
//		 * Solving the problem
//		 */
//		Solver solver = factory.get(); // you should use this solver only once for one problem
//		Result result = solver.solve(problem);
//		return(result);
//	}
//
//	/**
//	 * This function is ONLY should be called on the results coming from AvailabilityOptimizer
//	 * In the other word the results should only have Z and Y in it.
//	 * @param R
//	 * @param size
//	 * @return 0 if result is understandable, -1 otherwise
//	 */
//	private static int replicaSetGeneratorZY(Result R, int size)
//	{
//		if(R == null)
//			return -1;
//		String result = new String();
//		result = R.toString();
//		//System.out.println("The Results of LP: ");
//		//System.out.println(result);
//		int counter = 0;
//		for(int i = 0 ; i < size ; i++)
//		{
//			String target = "Y"+i+"=1";
//			if(result.contains(target))
//			{
//
//				repTools.setDeyamicRealWorldReplicaSet(i, true);
//				counter ++;
//			}
//			System.out.println();
//			for(int t = 0 ; t < repTools.getTimeSlots() ; t++)
//			{
//				int prob = (int) (100 * DataTypes.nodesTimeTable.getAvailabilityProbability(i, t));
//				System.out.print(prob + " ");
//			}
//			System.out.println();
//		}
//		System.out.println("reptools\\replicaSetGeneratorZY: Number of extracted replicas from LP " + counter);
//		return 0;
//	}
//
//	private static void numOfReplicas()
//	{
//		int counter = 0;
//		for(int i = 0 ; i < Simulator.system.getNumIDSeed(); i++)
//		{
//			if(repTools.getDynamicRealWorldReplicaSet(i))
//			{
//
//				counter++;
//			}
//		}
//
//		System.out.println("Number of replicas obtained by LP " + counter);
//		averageNumberOfReps += counter;
//
//		if(Simulator.system.getCurrentTopologyIndex() == Simulator.system.getTopologyNumbers())
//		{
//			averageNumberOfReps /= Simulator.system.getTopologyNumbers();
//			System.out.println("Average number of replicas obtained by LP over the simulations was " + averageNumberOfReps);
//		}
//
//	}
//}
