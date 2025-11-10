//import net.sf.javailp.*;
//
//public class AvailabilityRep_Public_Alg04_LPPartOfLARASOnAvailability
//{
//	 private static double  averageNumberOfReps  = 0;
//	 public static void Algorithm()
//	 {
//		   System.out.println("LP part of LARAS starts");
//		   repTools.reset();
//		   repTools.tablesInit();
//		   repTools.dynamicReplicaSetInit();
//		   
//		   /*
//		    * Models the LP on only the availability and generates the set of replicas
//		    * Note that in contrast to the other LPs, the LP on availability is solved on the numIDseed not the 
//		    * Simulator.system size.
//		    */
//		   replicaSetGeneratorZY(LPAvailabilityOptimizer(repTools.normalizedNameidDistances, Simulator.system.getNumIDSeed(), 1, 1), Simulator.system.getNumIDSeed());
//		   
//		   numOfReplicas();
//       
//	 }
//
//
//	private static Result LPAvailabilityOptimizer(double[][] L, int size, int w1, int w2)
//	{
//		SolverFactory factory = new SolverFactoryLpSolve(); // use lp_solve
//		factory.setParameter(Solver.VERBOSE, 0);
//		factory.setParameter(Solver.TIMEOUT, Integer.MAX_VALUE);
//
//		/**
//		 * Constructing a Problem:
//		 * Minimize:
//		 * 			Sigma(i)Sigma(t)Sigma(j) w1*Lij*Xijt +
//		 * 			Sigma(i)                 w2*Yi
//		 * 			Sigma(i)Sigma(t)         w3*Zit*(1-Tit)
//		 * Subject to:
//		 * for each i,j,t Zit>= Xijt
//		 * for each j,t   Sigma(i)Xijt = 1
//		 * for each i,t   Sigma(j)Xijt >= Zit
//		 * for each t,    Sigma(i) Zit >= Number of available reps at each hour
//		 * for each t,    Sigma(i) Zit * Tit >= Availability Probability
//		 *                Sigma(i) Yi <= MNR
//		 */
//
//		Problem problem = new Problem();
//
//		 /*
//		 *Minimize:
//		 * 			Sigma(i)Sigma(t)Sigma(j) w1*Lij*Xijt +
//		 * 			Sigma(i)Sigma(t)         w2*Zit*(1-Tit)
//		  */
//		Linear linear = new Linear();
//		String objective    = new String();
//		for(int i = 0 ; i < size ; i++)
//		{
//			for(int t = 0; t < repTools.getTimeSlots(); t++)
//			{
//				objective = new String();
//				objective = "Z"+i+","+t;
//				linear.add(w2 * (1-DataTypes.nodesTimeTable.getAvailabilityProbability(SkipGraph.Nodes.getNode(i).getNumID(), t)), objective);
//				for(int j = 0 ; j < size ; j++)
//				{
//					objective = new String();
//					objective = "X"+i+","+j+","+t;
//					linear.add(L[i][j]*w1, objective);
//				}
//			}
//
//		}
//		problem.setObjective(linear, OptType.MIN);
//
//
//		 /*
//		  * Part 1: for each i,j,t Zit>= Xijt
//		  */
//		for(int i = 0 ; i < size ; i++)
//		{
//			for(int j = 0 ; j < size ; j++)
//			{
//				for(int t = 0 ; t < repTools.getTimeSlots() ; t++)
//				{
//					linear = new Linear();
//					String cons1 = new String();
//					cons1 = "X"+i+","+j+","+t;
//					linear.add(1, cons1);
//
//					cons1 = new String();
//					cons1 = "Z"+i+","+t;
//					linear.add(-1, cons1);
//
//					problem.add(linear, "<=", 0);
//				}
//			}
//
//		}
//
//		 /*
//		  * Part 2: for each j,t   Sigma(i)Xijt = 1
//		  */
//		for(int j = 0 ; j < size ; j++)
//		{
//
//			for(int t = 0 ; t < repTools.getTimeSlots(); t++)
//			{
//				linear = new Linear();
//				for(int i = 0 ; i < size ; i++)
//				{
//					String cons3 = new String();
//					cons3 = "X"+i+","+j+","+t;
//					linear.add(1, cons3);
//				}
//				problem.add(linear, "=", 1);
//			}
//		}
//
//
//		 /*
//		  * Part 3: for each i,t   Sigma(j)Xijt >= Zit
//		  */
//		for(int i = 0 ; i < size ; i++)
//		{
//			for(int t = 0 ; t < repTools.getTimeSlots() ; t++ )
//			{
//				linear = new Linear();
//				for(int j = 0 ; j < size ; j++)
//				{
//					String cons3 = new String();
//					cons3 = "X"+i+","+j+","+t;
//					linear.add(-1, cons3);
//				}
//
//				String cons3 = new String();
//				cons3 = "Z"+i+","+t;
//				linear.add(1, cons3);
//				problem.add(linear, "<=", 0);
//			}
//
//		}
//
//		 /*
//		  * Part 4: for each i,t   Yi >= Zit
//		  */
//		for(int i = 0 ; i < size ; i++)
//		{
//			for(int t = 0 ; t < repTools.getTimeSlots() ; t++ )
//			{
//				linear = new Linear();
//				String cons4 = new String();
//				cons4 = "Y" + i;
//				linear.add(-1, cons4);
//
//				cons4 = new String();
//				cons4 = "Z"+i+","+t;
//				linear.add(+1, cons4);
//
//				problem.add(linear, "<=", 0);
//			}
//		}
//
//
//		 /*
//		  * Part 5: Sigma(i)Yi <= MNR
//		  */
//		linear = new Linear();
//		for(int i = 0 ; i < size ; i++)
//		{
//			String cons5 = "Y"+i;
//			linear.add(1, cons5);
//		}
//		problem.add(linear, "<=", Simulator.system.getReplicationDegree());
//
//
//		 /*
//		  * Part 6: Set the type of Xij and Yi
//		  */
//		for(int i = 0 ; i < size ; i++)
//		{
//			for(int t = 0; t < repTools.getTimeSlots(); t++)
//			{
//
//				String consZ = "Z"+i+","+t;
//				problem.setVarType(consZ, Integer.class);
//				for(int j = 0 ; j < size ; j++)
//				{
//					String consX = "X"+i+","+j+","+t;
//					problem.setVarType(consX, Integer.class);
//				}
//			}
//
//			String consY = "Y"+i;
//			problem.setVarType(consY, Integer.class);
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
//				repTools.setDeyamicRealWorldReplicaSet(i, true);
//				counter ++;
//			}
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
//
//}