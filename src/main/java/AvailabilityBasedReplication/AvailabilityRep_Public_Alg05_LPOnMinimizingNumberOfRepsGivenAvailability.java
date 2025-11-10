//import java.util.Random;
//
//import net.sf.javailp.Linear;
//import net.sf.javailp.OptType;
//import net.sf.javailp.Problem;
//import net.sf.javailp.Result;
//import net.sf.javailp.Solver;
//import net.sf.javailp.SolverFactory;
//import net.sf.javailp.SolverFactoryLpSolve;
//
//public class AvailabilityRep_Public_Alg05_LPOnMinimizingNumberOfRepsGivenAvailability
//{
//	
//	 private static double  availabilityParameter = 3.30;
//	 private static double  averageNumberOfReps  = 0;
//	 private static int     dataOwner = 0;
//	 
//	 
//	 public static void Algorithm()
//	 {
//		 repTools.reset();
//		 repTools.tablesInit();
//		 repTools.dynamicReplicaSetInit();
//		   
//		 //availabilityParameter = computeAvailabilityParameter();
//		 System.out.println("The availability parameter is " + availabilityParameter);
//		 replicaSetGeneratorZY(ReplicaOptimizer(Simulator.system.getNumIDSeed(), availabilityParameter), Simulator.system.getNumIDSeed());
//		 numOfReplicas();
//	 }
//	 
//	 private static double computeAvailabilityParameter()
//	 {
//    	 Random random = new Random();
//    	 
//    	 /*
//    	  * pick a random data owner
//    	  */
//         dataOwner = 0;
//         while(true)
//         {
//        	 dataOwner = random.nextInt(Simulator.system.getSystemCapacity()-1);
//        	 if(!SkipGraph.Nodes.getNode(dataOwner).isEmpty()
//        			 && SkipGraph.Nodes.getNode(dataOwner).numberOfPossibleReplicationCandidates() >= Simulator.system.getReplicationDegree())
//        	 {
//        		 break;
//        	 }
//        	 
//         }   	 
//         
//   	     
//   	     if(dataOwner == -1)
//   	     {
//   	    	 System.out.println("LPOnMinimizingNumberOfRepsGivenAvailability"
//   	    	 		+ " in the topology number " + Simulator.system.getCurrentTopologyIndex() + " there was no SkipGraph.Node with minimum "
//   	    			 + Simulator.system.getReplicationDegree() + " replication candidates");
//   	    	 System.exit(0);
//   	     }
//   	     
//   	     /*
//   	      * bufferRep is a boolean array that keeps the temporary replicas just to compute the availability
//   	      */
//   	     boolean[] bufferRep = new boolean[Simulator.system.getNumIDSeed()];
//   	     for(int i = 0 ; i < Simulator.system.getNumIDSeed() ; i++)
//   	     {
//   	    	 bufferRep[i] = false;
//   	     }
//   	     
//   	     int repCounter = 0;
//   	     double maxAvailability = 0;
//   	     int maxIndex = 0;
//   	     /*
//   	      * Marking data owner as one of the replicas
//   	      */
//
//   	     while(true)
//   	     {
//	   	     for(int i = 0 ; i < Simulator.system.getNumIDSeed() ; i++)
//	   	     {
//	   	    	double totalAvailability = SkipGraph.Nodes.getNode(dataOwner).totalAvailabilityChanceOfThisNumId(i);
//	   	    	if( totalAvailability > maxAvailability && !bufferRep[i] &&  i != SkipGraph.Nodes.getNode(dataOwner).getNumID())
//	   	    	{
//	   	    		maxAvailability = totalAvailability;
//	   	    		maxIndex = i;
//
//	   	    	}
//	   	    		
//	   	     }
//	   	     
//	   	     bufferRep[maxIndex] = true;
//			 repCounter++;
//			 if(repCounter >= Simulator.system.getReplicationDegree())
//				 break;
//			 maxAvailability = 0;
//			 maxIndex = 0;
//   	     }
//   	     
//   	     /*
//   	      * Computing the average availability
//   	      */
//   	     double availability = 0;
//   	     for(int i = 0 ; i < Simulator.system.getNumIDSeed() ; i++)
//   	     {
//   	    	 if(bufferRep[i])
//   	    	 {
//   	    		 for(int  t = 0 ; t < repTools.getTimeSlots() ; t++)
//   	    		 {
//   	    			 availability += DataTypes.nodesTimeTable.getLocalAvailability(dataOwner, i, t);
//   	    		 }		 
//   	    	 }
//   	     }
//   	     
//   	     /*
//   	      * Average over time
//   	      */
//   	     availability /= repTools.getTimeSlots();
//   	     
////   	     /*
////   	      * Average over numberOfReps
////   	      */
////   	     availability /= Simulator.system.MNR;
//   	     
//   	     return availability;
//   	     
// 
//	 }
//	 
//	 private static void numOfReplicas()
//	 {
//		 int counter = 0;
//		 for(int i = 0 ; i < Simulator.system.getNumIDSeed(); i++)
//		 {
//			 if(repTools.getDynamicRealWorldReplicaSet(i))
//			 {
//				 counter++;
//			 }
//		 }
//		 
//		 System.out.println("Number of replicas obtained by LP " + counter);
//		 averageNumberOfReps += counter;
//		 
//		 if(Simulator.system.getCurrentTopologyIndex() == Simulator.system.getTopologyNumbers())
//		 {
//			 averageNumberOfReps /= Simulator.system.getTopologyNumbers();
//			 System.out.println("Average number of replicas obtained by LP over the simulations was " + averageNumberOfReps);
//		 }
//		 
//	 }
//
//	 private static Result ReplicaOptimizer(int size, double availability)
//	 {
//		 SolverFactory factory = new SolverFactoryLpSolve(); // use lp_solve
//		 factory.setParameter(Solver.VERBOSE, 0); 
//		 factory.setParameter(Solver.TIMEOUT, Integer.MAX_VALUE); 
//		 
//		 /*A new LP problem*/
//		 Problem problem = new Problem();
//		 
//		 /*The objective
//		  * minimize sigma(i) Yi
//		  */
//		 Linear linear = new Linear();
//		 String objective = new String();
//		 for(int i = 0 ; i < size ; i++)
//		 {
//			 objective = new String();
//			 objective = "Y"+i;
//			 linear.add(1, objective);	 
//		 }
//
//		 problem.setObjective(linear, OptType.MIN);
//
//		 
//		 /*
//		  * Part 1: for each i and t Yi > Zit
//		  * In the other word if a SkipGraph.Node is a replica during one time slot, it should be counted as a replica
//		  */
//		 for(int i = 0 ; i < size ; i++)
//		 {			 
//			 for(int t = 0 ; t < repTools.getTimeSlots() ; t++)
//			 {
//				 String cons1 = new String();
//				 linear = new Linear();
//				 cons1 = new String();
//				 cons1 = "Y"+i;
//				 linear.add(-1, cons1);
//				 
//				 cons1 = new String();
//				 cons1 = "Z"+i+","+t;
//				 linear.add(+1, cons1);
//				 
//				 problem.add(linear, "<=", 0); 
//			 }
//
//		 }
//
//		 /*
//		  * Part 2: (1/timeSlot) Sigma(i) ZitTit >= K-availability
//		  * or
//		  * -Sigma(i) ZitTit <= -1 * timeSlots * K-availability 
//		  */		 
//		 linear = new Linear();
//		 for(int i = 0 ; i < size ; i++)
//		 {
//			 for(int t = 0 ; t < repTools.getTimeSlots(); t++)
//			 {
//				 String cons2 = "Z"+i+","+t;
//				 linear.add(-1 * (DataTypes.nodesTimeTable.getAvailabilityProbability(i, t)), cons2);
//			 }
//		 }
//		 problem.add(linear, "<=", -1 * availability * repTools.getTimeSlots());
//		 
//		 /*
//		  * Part 3 = Constraint on Yi
//		  * Yi >= 0 && Yi <= 1
//		  */
//		 linear = new Linear();
//		 for(int i = 0 ; i < size ; i++)
//		 {
//			 linear = new Linear();
//			 String cons3 = "Y"+i;
//			 linear.add(-1, cons3);
//			 problem.add(linear, "<=", 0);
//			 
//			 linear = new Linear();
//			 String cons4 = "Y"+i;
//			 linear.add(1, cons4);
//			 problem.add(linear, "<=", 1);
//		 }
//		 
//
//		 /*
//		  * Part 4: Set the type of Zij and Yi
//		  */
//		 for(int i = 0 ; i < size ; i++)
//		 {
//			 for(int t = 0 ; t < repTools.getTimeSlots() ; t++)
//			 {
//				 String cons3 = "Z"+i+","+t;
//				 problem.setVarType(cons3, Integer.class);
//			 }
//			 
//			 String cons3 = "Y"+i;
//			 problem.setVarType(cons3, Integer.class);
//		 }
//		 
//
//		 /**
//		  * Solving the problem
//		  */
//		 Solver solver = factory.get(); // you should use this solver only once for one problem
//		 Result result = solver.solve(problem);
//		 return(result);
//	 }
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
//}
