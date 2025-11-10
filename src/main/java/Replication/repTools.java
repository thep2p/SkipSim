//import net.sf.javailp.Linear;
//import net.sf.javailp.OptType;
//import net.sf.javailp.Problem;
//import net.sf.javailp.Result;
//import net.sf.javailp.Solver;
//import net.sf.javailp.SolverFactory;
//import net.sf.javailp.SolverFactoryLpSolve;
//
//public class repTools
//{
//	// private static int        timeSlots   = 24;
//	 private static int        problemSize = Simulator.system.getSystemCapacity();
//	 private static int        subProblemSize = 32;
//	 private static int        subProblemNameIDSize = 5;
//	 private static int        ExperimentNumber = 1;
//	 private static int        nameSpace = (int) Math.pow(2, Simulator.system.getNameIDLength());
//	 public static int[][]     realDistance = new int[problemSize][problemSize];
//	 public static double[][]  normalizedNameidDistances = new double[nameSpace][nameSpace];
//	 public static int[][]     nameidsDistance = new int[nameSpace][nameSpace];
//	 public static boolean[][] realReplicaAssignment = new boolean[problemSize][problemSize];
//	 private static boolean[][]localReplicaAssignment = new boolean[nameSpace][nameSpace];
//	 public static boolean[][] realWorldReplicaAssignment = new boolean[problemSize][problemSize];
//	 public static boolean[]   realWorldReplicaSet = new boolean[problemSize];
//	 private static boolean[]  realReplicaSet = new boolean[problemSize];
//	 private static int[]      repShares = new int[Simulator.system.getLandmarksNum()];
//	 private static int        MNR = Simulator.system.getReplicationDegree(); //maximum number of replicas for an object
//	 private static double[]   ratioDataSet = new double[Simulator.system.getTopologyNumbers()];
//	 private static int        NOR = Simulator.system.getDataRequesterNumber();
//	 private static int        M   = (int) Simulator.system.getSystemCapacity() / NOR;
//	 private static int[]	   adaptiveSubproblemSizes = new int[Simulator.system.getLandmarksNum()]; //only for the adaptice SSLP
//
//
//	 //dynamicRealWorldReplicaSet[i][t] == true if and only if the replication algorithm determines SkipGraph.Node i as a replica in the t time slot
//	 //private static boolean[][]    dynamicRealWorldReplicaSet = new boolean [Simulator.system.numIDSeed][timeSlots];
//	 private static boolean[]    dynamicRealWorldReplicaSet = new boolean [Simulator.system.getNumIDSeed()];
//
//	 //dynamicRealWorldReplicaAssignment[i][j][t] == true if SkipGraph.Node j is assigned as the corresponding replica of SkipGraph.Node i at time t
//	 //private static boolean[][][]    dynamicRealWorldReplicaAssignment = new boolean[Simulator.system.numIDSeed][Simulator.system.numIDSeed][timeSlots];
//
//
////	public static boolean getDynamicRealWorldReplicaAssignment(int i, int j, int t)
////	{
////		return dynamicRealWorldReplicaAssignment[i][j][t];
////	}
//
////	public static void setDynamicRealWorldReplicaAssignment(int i, int j, int t, boolean b)
////	{
////		dynamicRealWorldReplicaAssignment[i][j][t] = b;
////	}
//
//	 public boolean getDynamicRealWorldReplicaSet(int i)
//	 {
//		 //return dynamicRealWorldReplicaSet[i][t];
//		 return dynamicRealWorldReplicaSet[i];
//
//	 }
//
//	 public static void setDeyamicRealWorldReplicaSet(int i, boolean b)
//	 {
//		 //dynamicRealWorldReplicaSet[i][t] = b;
//		 dynamicRealWorldReplicaSet[i] = b;
//	 }
//
//	 public static int getProblemSize()
//	 {
//		 return problemSize;
//	 }
//
//	 public static int getSubProblemSize()
//	 {
//		 return subProblemSize;
//	 }
//
//	 public static int getNameSpace()
//	 {
//		 return nameSpace;
//	 }
//
//	 public static int getReplicationDegree()
//	 {
//		 return MNR;
//	 }
//
//	 public static void setReplicationDegree(int mnr)
//	 {
//		 MNR = mnr;
//	 }
//
//	 public static int getSubReplicationDegree(int i)
//	 {
//		 return repShares[i];
//	 }
//
//	 public static int getM()
//	 {
//		 return M;
//	 }
//
//	 public static int getExperimentNumber()
//	 {
//		 return ExperimentNumber;
//	 }
//
////	 public static int getTimeSlots()
////	 {
////		 return timeSlots;
////	 }
//
//
//	 public static void setRatioDataSet(int i, double ratio)
//	 {
//		 ratioDataSet[i] = ratio;
//	 }
//
//	 public static boolean getRealWorldReplicaSet(int i)
//	 {
//		 return realWorldReplicaSet[i];
//	 }
//
//	 public static void setAdaptiveSubProblemSize(int LandmarkIndex)
//	 {
//		 subProblemSize = adaptiveSubproblemSizes[LandmarkIndex];
//		 subProblemNameIDSize = 1;
//		 while(Math.pow(2, subProblemNameIDSize) < subProblemSize )
//			 subProblemNameIDSize ++;
//		 System.out.println("The LARAS subProblemSize is set to " + subProblemSize + " subProblemNameID size = " + subProblemNameIDSize + " Current MNR: " + MNR);
//	 }
//	 public static void reset()
//	 {
//		 realDistance = new int[problemSize][problemSize];
//		 nameidsDistance = new int[nameSpace][nameSpace];
//		 realReplicaAssignment = new boolean[problemSize][problemSize];
//		 localReplicaAssignment = new boolean[nameSpace][nameSpace];
//		 realWorldReplicaAssignment = new boolean[problemSize][problemSize];
//		 realWorldReplicaSet = new boolean[problemSize];
//		 realReplicaSet = new boolean[problemSize];
//		 repShares = new int[Simulator.system.getLandmarksNum()];
//		 MNR = Simulator.system.getReplicationDegree(); //maximum number of replicas for an object
//		 adaptiveSubproblemSizes = new int[Simulator.system.getLandmarksNum()];
//		 //ratioDataSet = new double[Simulator.system.simRun];
//	 }
//
//	 public static void tablesInit()
//	 {
//		 for(int i = 0 ; i < problemSize ; i++)
//			 for(int j = 0 ; j < problemSize ; j++)
//			 {
//				 realDistance[i][j] = (int) SkipGraph.Nodes.getNode(i).mCoordinate.distance(SkipGraph.Nodes.getNode(j).mCoordinate) + 1;
//			 }
//
//			 for(int i = 0 ; i < subProblemSize ; i++)
//				 for(int j = 0 ; j < subProblemSize ; j++)
//				 {
//							 if(i == j )
//							 {
//								 nameidsDistance[i][j] = 0;
//								 normalizedNameidDistances[i][j] = 0;
//							 }
//
//							 else
//							 {
//								 nameidsDistance[i][j] = Simulator.system.getNameIDLength() - commonPrefixLength(i, j);
//								 normalizedNameidDistances[i][j] = (double) nameidsDistance[i][j] / Simulator.system.getNameIDLength();
//							 }
//				 }
//	 }
//
//	  private static int commonPrefixLength(String s1, String s2)
//	  {
//		  int k = 0;
//		  while(s1.charAt(k) == s2.charAt(k))
//		  {
//			 k++;
//		     if(k >= s1.length() || k >= s2.length())
//		    	 break;
//		  }
//
//		  return k;
//	  }
//
//	  private static int commonPrefixLength(int i, int j)
//	  {
//         String s1 = Integer.toBinaryString(i);
//         String s2 = Integer.toBinaryString(j);
//
//         while(s1.length() < Simulator.system.getNameIDLength())
//       	  s1 = "0" + s1;
//
//         while(s2.length() < Simulator.system.getNameIDLength())
//       	  s2 = "0" + s2;
//
//		  int k = 0;
//		  while(s1.charAt(k) == s2.charAt(k))
//		  {
//			 k++;
//		     if(k >= s1.length() || k >= s2.length())
//		    	 break;
//		  }
//
//		  return k;
//	  }
//
//
//	 private static int mapToOriginalSystem(int k, int landmarkIndex)
//	 {
//         System.out.println("Find the closest call with k = " + k + " and landmark = " + landmarkIndex );
//		 String s = Integer.toBinaryString(k);
//
//         while(s.length() < subProblemNameIDSize)
//       	  s = "0" + s;
//
//         String b = s;
//
//         s = SkipGraph.Landmarks.getDynamicPrefix(landmarkIndex) + s;
//
//         int maxCommon = 0;
//         int maxIndex = 0;
//         for(int i = 0 ; i < Simulator.system.getSystemCapacity() ; i++)
//         {
//        	 if(s.equals(SkipGraph.Nodes.getNode(i).nameID))
//        	 {
//                 System.out.println("Closest name id to " + s + " is " + SkipGraph.Nodes.getNode(i).nameID );
//        		 return i;
//        	 }
//             else if(commonPrefixLength(s, SkipGraph.Nodes.getNode(i).nameID) > maxCommon && !realWorldReplicaSet[i])
//        	 {
//        		 maxCommon = commonPrefixLength(s, SkipGraph.Nodes.getNode(i).nameID);
//        		 maxIndex = i;
//        	 }
//         }
//         System.out.println("Closest name id to " + s + " is " + SkipGraph.Nodes.getNode(maxIndex).nameID + " with " + maxCommon + " bits name id common prefix length");
//         return maxIndex;
//
//	 }
//
//	 private static int mapToOriginalSystem(int k ,SkipGraph.SkipGraphOperations sgo)
//	 {
//         String s = Integer.toBinaryString(k);
//
//         while(s.length() < subProblemNameIDSize)
//       	  s = "0" + s;
//
//
//         int maxCommon = 0;
//         int maxIndex = 0;
//         for(int i = 0 ; i < Simulator.system.getSystemCapacity() ; i++)
//         {
//        	 if(s.equals(SkipGraph.Nodes.getNode(i).nameID))
//        	 {
//        		 return i;
//        	 }
//             else if(commonPrefixLength(s, SkipGraph.Nodes.getNode(i).nameID) > maxCommon)
//        	 {
//        		 maxCommon = commonPrefixLength(s, SkipGraph.Nodes.getNode(i).nameID);
//        		 maxIndex = i;
//        	 }
//         }
//        	 	  return maxIndex;
//	 }
//
//	 private static int mapToOriginalSystem(String nameID)
//	 {
//
//         int maxCommon = 0;
//         int maxIndex = 0;
//         for(int i = 0 ; i < subProblemSize ; i++)
//         {
//        	 String s = Integer.toBinaryString(i);
//             while(s.length() < subProblemNameIDSize)
//              	  s = "0" + s;
//        	 if(s.equals(nameID))
//        	 {
//        		 return i;
//        	 }
//             else if(commonPrefixLength(s, nameID) > maxCommon)
//        	 {
//        		 maxCommon = commonPrefixLength(s, nameID);
//        		 maxIndex = i;
//        	 }
//         }
//        	 	  return maxIndex;
//	 }
//
//	 private static int findTheNameID(String nameID)
//	 {
//
//		 for(int i = 0 ; i < Simulator.system.getSystemCapacity() ; i++)
//             {
//            	 if(nameID.equals(SkipGraph.Nodes.getNode(i).nameID))
//            		 return i;
//             }
//
//             return -1;
//	 }
//	 public static Result PrivateOptimizer(int[][] L, int size)
//	 {
//		 SolverFactory factory = new SolverFactoryLpSolve(); // use lp_solve
//		 factory.setParameter(Solver.VERBOSE, 0);
//		 factory.setParameter(Solver.TIMEOUT, Integer.MAX_VALUE); // set timeout to 100 seconds
//
//		 /**
//		 * Constructing a Problem:
//		 * Minimize: Sigma(i)Sigma(j) LijXij
//		 * Subject to:
//		 * for each i,j Yi>= Xij
//		 * Sigma(i)Xij = 1
//		 * Sigma(j)Xij >= Yi
//		 * Sigma(i)Yi <= MNR
//		 */
//
//		 Problem problem = new Problem();
//
//		 /**
//		  * Part 1: Minimize: Sigma(i)Sigma(j) LijXij
//		  */
//		 Linear linear = new Linear();
//		 for(int i = 0 ; i < size ; i++)
//			 for(int j = 0 ; j < size ; j++)
//			 {
//					if(j % M == 0)
//					{
//				     String var = "X"+i+","+j;
//					 linear.add(L[i][j], var);
//					}
//			 }
//		 problem.setObjective(linear, OptType.MIN);
//
//
//		 /**
//		  * Part 2: for each i,j Yi>= Xij
//		  */
//		 for(int i = 0 ; i < size ; i++)
//		 {
//			 for(int j = 0 ; j < size ; j++)
//			 {
//				 linear = new Linear();
//				 String var = "X"+i+","+j;
//				 linear.add(1, var);
//				  var = "Y"+i;
//				 linear.add(-1, var);
//				 problem.add(linear, "<=", 0);
//			 }
//
//		 }
//
//		 /**
//		  * Part 3: Sigma(i)Xij = 1
//		  */
//		 for(int j = 0 ; j < size ; j++)
//		 {
//			 linear = new Linear();
//			 for(int i = 0 ; i < size ; i++)
//			 {
//				 String var = "X"+i+","+j;
//				 linear.add(1, var);
//			 }
//			 problem.add(linear, "=", 1);
//		 }
//
//
//		 /**
//		  * Part 4: Sigma(j)Xij >= Yi
//		  */
//		 for(int i = 0 ; i < size ; i++)
//		 {
//			 linear = new Linear();
//			 for(int j = 0 ; j < size ; j++)
//			 {
//				 String var = "X"+i+","+j;
//				 linear.add(-1, var);
//			 }
//
//			 String var = "Y"+i;
//			 linear.add(1, var);
//			 problem.add(linear, "<=", 0);
//		 }
//
//
//		 /**
//		  * Part 5: Sigma(i)Yi = MNR
//		  */
//		 linear = new Linear();
//		 for(int i = 0 ; i < size ; i++)
//		 {
//			 String var = "Y"+i;
//			 linear.add(1, var);
//		 }
//		 problem.add(linear, "=", MNR);
//
//
//		 /**
//		  * Part 6: Set the type of Xij and Yi
//		  */
//		 for(int i = 0 ; i < size ; i++)
//		 {
//			 for(int j = 0 ; j < size ; j++)
//			 {
//				 String var = "X"+i+","+j;
//				 problem.setVarType(var, Integer.class);
//			 }
//
//			 String var = "Y"+i;
//			 problem.setVarType(var, Integer.class);
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
//	 public static Result PublicOptimizer(int[][] L, int size)
//	 {
//		 SolverFactory factory = new SolverFactoryLpSolve(); // use lp_solve
//		 factory.setParameter(Solver.VERBOSE, 0);
//		 factory.setParameter(Solver.TIMEOUT, Integer.MAX_VALUE); // set timeout to 100 seconds
//
//		 /**
//		 * Constructing a Problem:
//		 * Minimize: Sigma(i)Sigma(j) LijXij
//		 * Subject to:
//		 * for each i,j Yi>= Xij
//		 * Sigma(i)Xij = 1
//		 * Sigma(j)Xij >= Yi
//		 * Sigma(i)Yi <= MNR
//		 */
//
//		 Problem problem = new Problem();
//
//		 /**
//		  * Part 1: Minimize: Sigma(i)Sigma(j) LijXij
//		  */
//		 Linear linear = new Linear();
//		 for(int i = 0 ; i < size ; i++)
//			 for(int j = 0 ; j < size ; j++)
//			 {
//				 String var = "X"+i+","+j;
//				 linear.add(L[i][j], var);
//			 }
//		 problem.setObjective(linear, OptType.MIN);
//
//
//		 /**
//		  * Part 2: for each i,j Yi>= Xij
//		  */
//		 for(int i = 0 ; i < size ; i++)
//		 {
//			 for(int j = 0 ; j < size ; j++)
//			 {
//				 linear = new Linear();
//				 String var = "X"+i+","+j;
//				 linear.add(1, var);
//				  var = "Y"+i;
//				 linear.add(-1, var);
//				 problem.add(linear, "<=", 0);
//			 }
//
//		 }
//
//		 /**
//		  * Part 3: Sigma(i)Xij = 1
//		  */
//		 for(int j = 0 ; j < size ; j++)
//		 {
//			 linear = new Linear();
//			 for(int i = 0 ; i < size ; i++)
//			 {
//				 String var = "X"+i+","+j;
//				 linear.add(1, var);
//			 }
//			 problem.add(linear, "=", 1);
//		 }
//
//
//		 /**
//		  * Part 4: Sigma(j)Xij >= Yi
//		  */
//		 for(int i = 0 ; i < size ; i++)
//		 {
//			 linear = new Linear();
//			 for(int j = 0 ; j < size ; j++)
//			 {
//				 String var = "X"+i+","+j;
//				 linear.add(-1, var);
//			 }
//
//			 String var = "Y"+i;
//			 linear.add(1, var);
//			 problem.add(linear, "<=", 0);
//		 }
//
//
//		 /**
//		  * Part 5: Sigma(i)Yi <= MNR
//		  */
//		 linear = new Linear();
//		 for(int i = 0 ; i < size ; i++)
//		 {
//			 String var = "Y"+i;
//			 linear.add(1, var);
//		 }
//		 problem.add(linear, "=", MNR);
//
//
//		 /**
//		  * Part 6: Set the type of Xij and Yi
//		  */
//		 for(int i = 0 ; i < size ; i++)
//		 {
//			 for(int j = 0 ; j < size ; j++)
//			 {
//				 String var = "X"+i+","+j;
//				 problem.setVarType(var, Integer.class);
//			 }
//
//			 String var = "Y"+i;
//			 problem.setVarType(var, Integer.class);
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
//
//	 public static int replicaSetGenerator(Result R, String set, int size)
//	 {
//		 if(R == null)
//			 return -1;
//		 String result = new String();
//		 result = R.toString();
//
//		 for(int i = 0 ; i < size ; i++)
//		 {
//
//			 for(int j = 0 ; j < size ; j++)
//			 {
//				 String target = "X"+i+","+j+"=1";
//				 if(result.contains(target) && set.contains("Real"))
//					 realReplicaAssignment[i][j] = true;
//
//			     else if(result.contains(target) && set.contains("Local"))
//			     {
//
//			    	 realWorldReplicaAssignment[i][j] = true;
//			    	 //System.out.println("SkipGraph.Node " + j + " uses " + i);
//			     }
//			     else if(!set.contains("Real") && !set.contains("Local"))
//			     {
//			    	// System.out.println("Error in calling Rep_Alg01_LP: replicaSetGenerator with " + set);
//			    	 System.exit(0);
//			     }
//			 }
//		 }
//
//		 return 0;
//	 }
//
//
//
//	 public static int replicaSetGenerator2(Result R, String set, int size, int landmarkIndex)
//	 {
//		 if(R == null)
//			 return -1;
//		 String result = new String();
//		 result = R.toString();
//		 boolean flag = false;
//		 //System.out.println(result);
//		 for(int i = 0 ; i < size ; i++)
//		 {
//			 String target = "Y"+i+"=1";
//			 if(result.contains(target) && set.contains("Real"))
//				 realReplicaSet[i] = true;
//
//		     else if(result.contains(target) && set.contains("Local"))
//		     {
//		    	 int replicaIndex = mapToOriginalSystem(i, landmarkIndex );
//		    	 realWorldReplicaSet[replicaIndex] = true;
//		    	 flag = true;
//		     }
//		     else if(!set.contains("Real") && !set.contains("Local"))
//		     {
//		    	 System.exit(0);
//		     }
//
//		 }
//
//		 if(!flag)
//			 return -1;
//
//		 return 0;
//	 }
//
//	 public static int replicaSetGenerator2(Result R, String set, int size, SkipGraph.SkipGraphOperations sgo)
//	 {
//		 if(R == null)
//			 return -1;
//		 String result = new String();
//		 result = R.toString();
//
//		 for(int i = 0 ; i < size ; i++)
//		 {
//
//			 String target = "Y"+i+"=1";
//			 if(result.contains(target) && set.contains("Real"))
//				 realReplicaSet[i] = true;
//
//		     else if(result.contains(target) && set.contains("Local"))
//		     {
//		    	 int replicaIndex = mapToOriginalSystem(i);
//		    	 realWorldReplicaSet[replicaIndex] = true;
//
//		     }
//		     else if(!set.contains("Real") && !set.contains("Local"))
//		     {
//		    	 System.exit(0);
//		     }
//
//		 }
//
//		 return 0;
//	 }
//
//	 //This function should only be called right after calling the replicaSetGenerator2
//	 public static void replicaAssignmentSetGenerator(int size)
//	 {
//		 for(int i = 0 ; i < size ; i++)
//			 {
//				 int minDistance = Integer.MAX_VALUE;
//				 int minReplicaIndex    = 0;
//				 for(int j = 0 ; j < size ; j++)
//				 {
//					 if(realWorldReplicaSet[j] && SkipGraph.Nodes.getNode(i).mCoordinate.distance(SkipGraph.Nodes.getNode(j).mCoordinate) < minDistance)
//					 {
//						 minDistance = (int) SkipGraph.Nodes.getNode(i).mCoordinate.distance(SkipGraph.Nodes.getNode(j).mCoordinate);
//						 minReplicaIndex = j;
//					 }
//				 }
//
//
//				realWorldReplicaAssignment[minReplicaIndex][i] = true;
//
//			 }
//	 }
//
//	 public static int publicTotalDelay(boolean[][] B)
//	 {
//		 int delay = 0;
//		 for(int j = 0 ; j < problemSize ; j++)
//			 for(int i = 0 ; i < problemSize ; i++)
//			 {
//				 if(B[i][j])
//				 {
//					 delay += realDistance[i][j];
//					 break;
//				 }
//			 }
//
//		 return delay;
//	 }
//
//	 public static int privateTotalDelay(boolean[][] B)
//	 {
//		 int delay = 0;
//		 for(int j = 0 ; j < problemSize ; j++)
//			 for(int i = 0 ; i < problemSize ; i++)
//			 {
//				 if(B[i][j])
//				 {
//					 if(j % M == 0)
//					 {
//						 delay += realDistance[i][j];
//						 break;
//					 }
//				 }
//			 }
//
//		 return delay;
//	 }
//
//	 public static double privateAverageDelay(boolean[][] B)
//	 {
//		 int delay = 0;
//		 for(int j = 0 ; j < problemSize ; j++)
//			 for(int i = 0 ; i < problemSize ; i++)
//			 {
//				 if(B[i][j])
//				 {
//					 if(j % M == 0)
//					 {
//						 delay += realDistance[i][j];
//						 break;
//					 }
//				 }
//			 }
//
//		 return delay/Simulator.system.getDataRequesterNumber();
//	 }
//
//	 public static double publicAverageDelay(boolean[][] B)
//	 {
//		 int delay = 0;
//		 for(int j = 0 ; j < problemSize ; j++)
//			 for(int i = 0 ; i < problemSize ; i++)
//			 {
//				 if(B[i][j])
//				 {
//					 delay += realDistance[i][j];
//					 break;
//				 }
//			 }
//
//		 return delay/Simulator.system.getSystemCapacity();
//	 }
//
//     public static void replicaSetInit()
//     {
//		 for(int i = 0 ; i < problemSize ; i++)
//		 {
//			 realWorldReplicaSet[i] = false;
//			 realReplicaSet[i] = false;
//			 for(int j = 0 ; j < problemSize ; j++)
//			 {
//				 realReplicaAssignment[i][j] = false;
//				 realWorldReplicaAssignment[i][j] = false;
//			 }
//		 }
//		 for(int i = 0 ; i < subProblemSize ; i++)
//			 for(int j = 0 ; j < subProblemSize ; j++)
//			 {
//		         localReplicaAssignment[i][j] = false;
//			 }
//     }
//
//     public static void dynamicReplicaSetInit()
//     {
////    	 for(int t = 0 ; t < timeSlots ; t++)
////    	 {
//    		 for(int i = 0 ; i < Simulator.system.getNumIDSeed() ; i++)
//    		 {
//    			 //dynamicRealWorldReplicaSet[i][t] = false;
//    			 dynamicRealWorldReplicaSet[i] = false;
////    			 for(int j = 0 ; j < problemSize ; j++)
////    			 {
////    				 dynamicRealWorldReplicaAssignment[i][j][t] = false;
////    			 }
//    		 }
////    	 }
//     }
//
//     public static void localReplicaSetInit()
//     {
//		 for(int i = 0 ; i < subProblemSize ; i++)
//			 for(int j = 0 ; j < subProblemSize ; j++)
//			 {
//		         localReplicaAssignment[i][j] = false;
//			 }
//     }
//
//
//     public static void evaluation(String algName)
//     {
//    	 double averageRatio = 0;
//
//    	 for(int i = 0 ; i < Simulator.system.getTopologyNumbers() ; i++)
//    	 {
//    		 //System.out.println(i+ " "+ ratioDataSet[i]);
//    		 averageRatio += ratioDataSet[i];
//    	 }
//
//    	 averageRatio = (double) averageRatio / Simulator.system.getTopologyNumbers();
//    	 double SD = 0;
//    	 for(int i = 0 ; i < Simulator.system.getTopologyNumbers() ; i++)
//    	 {
//    		 SD += Math.pow(ratioDataSet[i]-averageRatio, 2);
//    	 }
//
//    	 SD = (double) SD / Simulator.system.getTopologyNumbers();
//    	 SD = Math.sqrt(SD);
//
//    	 System.out.println("Replciation Simulation: " + algName +" , MNR = " + Simulator.system.getReplicationDegree() + " NOR = " + Simulator.system.getDataRequesterNumber());
//    	 System.out.println("Average real delay = " +  averageRatio + " with SD =  " + SD);
//     }
//
//     private static void realWordTransform(int landmarkIndex)
//     {
//    	 for(int j = 0 ; j < problemSize ; j++)
//    	 {
//    			 int nodeIndex = mapToOriginalSystem(SkipGraph.Nodes.getNode(j).nameID);
//	    		 for(int i = 0 ; i  < subProblemSize ; i++)
//	    			 if(localReplicaAssignment[i][j])
//	    			 {
//	    			        int replicaIndex = mapToOriginalSystem(i, landmarkIndex);
//	    				    if(!realWorldReplicaAssignment[replicaIndex][j])
//	    				    {
//	    				    	realWorldReplicaAssignment[replicaIndex][j] = true;
//	    			            //System.out.println("Node " + SkipGraph.Nodes.nodeSet[nodeIndex].nameID + " assigned to " + SkipGraph.Nodes.nodeSet[replicaIndex].nameID);
//	    				    }
//	    			        break;
//	    			 }
//
//
//
//    	 }
//     }
//
//     private static void assignToOther()
//     {
//    	 boolean[] replicaSet = new boolean[Simulator.system.getSystemCapacity()];
//
//    	 for(int i = 0 ; i < Simulator.system.getSystemCapacity() ; i++)
//    		 replicaSet[i] = false;
//    	 for(int i = 0 ; i < Simulator.system.getSystemCapacity() ; i++)
//    	 {
//    		 for(int j = 0 ; j < Simulator.system.getSystemCapacity() ; j++)
//    		 {
//    			 if(realWorldReplicaAssignment[i][j])
//    			 {
//    				 replicaSet[i] = true;
//    				 System.out.println("Replica " + i);
//    				 break;
//    			 }
//    		 }
//    	 }
//
//
//
//    	 for(int j = 0 ; j < Simulator.system.getSystemCapacity() ; j++)
//    	 {
//    		 boolean haveReplica = false;
//    		 int closestReplica = -1;
//    		 int closestReplicaDistance = Integer.MAX_VALUE;
//    		 for(int i = 0 ; i < Simulator.system.getSystemCapacity() ; i++)
//    		 {
//    			  	 if(realWorldReplicaAssignment[i][j])
//    			  	 {
//    			  		 haveReplica = true;
//    			  		 break;
//    			  	 }
//    				 if(replicaSet[i] && SkipGraph.Nodes.getNode(i).mCoordinate.distance(SkipGraph.Nodes.getNode(j).mCoordinate) < closestReplicaDistance)
//    				 {
//    					 closestReplica = i;
//    					 closestReplicaDistance = (int) SkipGraph.Nodes.getNode(i).mCoordinate.distance(SkipGraph.Nodes.getNode(j).mCoordinate);
//    				 }
//    		 }
//    		 if(!haveReplica && closestReplica != -1)
//    		 {
//    		 	realWorldReplicaAssignment[closestReplica][j] = true;
//    		 }
//    	 }
//     }
//
//
//     public static void publicRepShareDefining()
//     {
//
//    	 double sum = 0;
//    	 for(int i = 0 ; i < Simulator.system.getLandmarksNum() ; i++)
//    	 {
//    		 sum = sum + SkipGraph.Landmarks.dynamicPrefixLength(i);
//    	 }
//    	 //System.out.println("Sum1 " + sum );
//    	 sum = MNR/ sum;
//    	 //System.out.println("Sum2 " + sum);
//    	 for(int i = 0; i < Simulator.system.getLandmarksNum() ; i++)
//    	 {
//    		 repShares[i] = (int) Math.ceil(sum * SkipGraph.Landmarks.dynamicPrefixLength(i));
//
//    	 }
//    	 sum = 0;
//    	 for(int i = 0 ; i < Simulator.system.getLandmarksNum() ; i++)
//    	 {
//    		 sum = sum + repShares[i];
//    	 }
//
////    	 while(sum < MNR)
////    	 {
////    		 int maxIndex = 0;
////    		 double max = Double.MIN_VALUE;
////    		 for(int i = 0 ; i < Simulator.system.landmarks ; i++)
////    				 if(repShares[i] > max) // &&  repShares[i] < 1)
////    				 {
////    					 max = repShares[i];
////    					 maxIndex = i;
////    				 }
////    		 repShares[maxIndex] = repShares[maxIndex] + 1;
////    		 if(repShares[maxIndex] < 0)
////    			 repShares[maxIndex] = 0;
////
////    		 sum = 0;
////        	 for(int i = 0 ; i < Simulator.system.landmarks ; i++)
////        	 {
////        		 sum = sum + repShares[i];
////
////        	 }
////    	 }
//
//
//
////    	 sum = 0;
////    	 for(int i = 0 ; i < Simulator.system.landmarks ; i++)
////    	 {
////    		 sum = sum + repShares[i];
////    	 }
////
//    	 while(sum > MNR)
//    	 {
//    		 int minIndex = 0;
//    		 double min = Double.MAX_VALUE;
//    		 for(int i = 0 ; i < Simulator.system.getLandmarksNum() ; i++)
//    				 if(SkipGraph.Landmarks.dynamicPrefixLength(i) < min && repShares[i] > 0)
//    				 {
//    					 min = SkipGraph.Landmarks.dynamicPrefixLength(i);
//    					 minIndex = i;
//    				 }
//    		 repShares[minIndex] = repShares[minIndex] - 1;
//    		 if(repShares[minIndex] < 0)
//    			 repShares[minIndex] = 0;
//
//    		 sum = 0;
//        	 for(int i = 0 ; i < Simulator.system.getLandmarksNum() ; i++)
//        	 {
//        		 sum = sum + repShares[i];
//
//        	 }
//    	 }
//
//    	 for(int i = 0 ; i < Simulator.system.getLandmarksNum() ; i++)
//    	 {
//    		 System.out.println(repShares[i] + " " + i);
//    	 }
//
//     }
//
//
//     public static void validityTest()
//     {
//    	 for(int j = 0 ; j < subProblemSize ; j++)
//    	 {
//    		 int rowSum = 0;
//    		 for(int i = 0 ; i < subProblemSize ; i++)
//    		 {
//    			 if(localReplicaAssignment[i][j])
//    				 rowSum = rowSum + 1;
//    		 }
//
//    		 if(rowSum > 1)
//    		 {
//    			 System.out.println("Row sum error: " + rowSum);
//    		 }
//    	 }
//
//    	 boolean[] repSet = new boolean[subProblemSize];
//    	 for(int i = 0 ; i < subProblemSize ; i++)
//    	 {
//    		 repSet[i] = false;
//    	 }
//    	 for(int i = 0 ; i < subProblemSize ; i++)
//    	 {
//    		 for(int j = 0 ; j < subProblemSize ; j++)
//    		 {
//    			 if(localReplicaAssignment[i][j])
//    				 repSet[i] = true;
//
//    		 }
//
//    	 }
//
//		 int repSum = 0;
//    	 for(int i = 0 ; i < subProblemSize ; i++)
//    	 {
//    		 if(repSet[i])
//    			 repSum = repSum + 1;
//    	 }
//
//
//    	 System.out.println("rep validation check: " + repSum);
//
//
//
//     }
//
//
//     public static void privateRepShareDefining()
//     {
//
//    	 int[] regionsPopulation = new int[Simulator.system.getLandmarksNum()];
//
//    	 for(int i = 0 ; i < Simulator.system.getSystemCapacity() ; i++)
//    	 {
//    		 if(i % M == 0)
//    			 regionsPopulation[SkipGraph.Nodes.ClosestLandmark(i)] += 1;
//    	 }
//
//    	 double sum = 0;
//    	 for(int i = 0 ; i < Simulator.system.getLandmarksNum() ; i++)
//    	 {
//    		 sum = sum + regionsPopulation[i];
//    	 }
//
//    	 sum = MNR / sum;
//
//    	 for(int i = 0; i < Simulator.system.getLandmarksNum() ; i++)
//    	 {
//    		 repShares[i] = (int) Math.round(sum * regionsPopulation[i]);
//    	 }
//    	 sum = 0;
//    	 for(int i = 0 ; i < Simulator.system.getLandmarksNum() ; i++)
//    	 {
//    		 sum = sum + repShares[i];
//    	 }
//
//    	 while(sum < MNR)
//    	 {
//    		 int minIndex = 0;
//    		 double min = Double.MAX_VALUE;
//    		 for(int i = 0 ; i < Simulator.system.getLandmarksNum() ; i++)
//    				 if(regionsPopulation[i] < min &&  repShares[i] < 1)
//    				 {
//    					 min = regionsPopulation[i];
//    					 minIndex = i;
//    				 }
//    		 repShares[minIndex] = repShares[minIndex] + 1;
//    		 if(repShares[minIndex] < 0)
//    			 repShares[minIndex] = 0;
//
//    		 sum = 0;
//        	 for(int i = 0 ; i < Simulator.system.getLandmarksNum() ; i++)
//        	 {
//        		 sum = sum + repShares[i];
//
//        	 }
//    	 }
//
//
//
//    	 sum = 0;
//    	 for(int i = 0 ; i < Simulator.system.getLandmarksNum() ; i++)
//    	 {
//    		 sum = sum + repShares[i];
//    	 }
//
//    	 while(sum > MNR)
//    	 {
//    		 int minIndex = 0;
//    		 double min = Double.MAX_VALUE;
//    		 for(int i = 0 ; i < Simulator.system.getLandmarksNum() ; i++)
//    				 if(repShares[i] < min && repShares[i] > 0)
//    				 {
//    					 min = repShares[i];
//    					 minIndex = i;
//    				 }
//    		 repShares[minIndex] = repShares[minIndex] - 1;
//    		 if(repShares[minIndex] < 0)
//    			 repShares[minIndex] = 0;
//
//    		 sum = 0;
//        	 for(int i = 0 ; i < Simulator.system.getLandmarksNum() ; i++)
//        	 {
//        		 sum = sum + repShares[i];
//
//        	 }
//    	 }
//
//    	 for(int i = 0 ; i < Simulator.system.getLandmarksNum() ; i++)
//    	 {
//    		 System.out.println(repShares[i] + " " + i + " " + regionsPopulation[i]);
//    	 }
//
//
//     }
//
//     public static void adaptivePrivateSubproblemSizeDefining()
//     {
//
//    	 double[] regionsPopulation = new double[Simulator.system.getLandmarksNum()];
//
//    	 for(int i = 0 ; i < Simulator.system.getSystemCapacity() ; i++)
//    	 {
//    		 if(i % M == 0)
//    			 regionsPopulation[SkipGraph.Nodes.ClosestLandmark(i)] += 1;
//    	 }
//
//    	 //double sum = 0;
//    	 int maxIndex = 0;
//    	 double max = Double.MIN_VALUE;
//    	 for(int i = 0 ; i < Simulator.system.getLandmarksNum() ; i++)
//    	 {
//    		 if(regionsPopulation[i] > max)
//    		 {
//    			 maxIndex = i;
//    			 max = regionsPopulation[i];
//    		 }
//    		 //sum = sum + regionsPopulation[i];
//    	 }
//
//
//    	 for(int i = 0 ; i < Simulator.system.getLandmarksNum() ; i++)
//    	 {
//    		 regionsPopulation[i] = (regionsPopulation[i]/ max);
//    	 }
//
//    	 double expectedPopulation = Simulator.system.getSystemCapacity() / Simulator.system.getLandmarksNum();
//    	 //double expectedReplicas   = Simulator.system.MNR  / Simulator.system.landmarks;
//
//    	 for(int i = 0; i < Simulator.system.getLandmarksNum() ; i++)
//    	 {
//    		 adaptiveSubproblemSizes[i] = (int) Math.round(regionsPopulation[i] * expectedPopulation  * Math.log(Simulator.system.getReplicationDegree()) / Math.log(2));
//    	 }
//
//    	 for(int i = 0; i < Simulator.system.getLandmarksNum() ; i++)
//    	 {
//    		int subProblemSize = 2;
//    		while(subProblemSize < adaptiveSubproblemSizes[i] && subProblemSize < Simulator.system.getSystemCapacity())
//    			subProblemSize *= 2;
//    		if(subProblemSize > Simulator.system.getSystemCapacity())
//    			subProblemSize /= 2;
//    		while(subProblemSize < 32)
//    			subProblemSize *= 2;
//    		adaptiveSubproblemSizes[i] = subProblemSize;
//         }
//
//    	 System.out.println("Adaptive Sub-problem defining: ");
//    	 for(int i = 0 ; i < Simulator.system.getLandmarksNum() ; i++)
//    	 {
//    		 System.out.println("Sub-problem size = " + adaptiveSubproblemSizes[i] + "\t SkipGraph.Landmarks Index = " + i);
//    	 }
//
//
//     }
//
//
//
//
//	public static void adaptivePublicSubproblemSizeDefining()
//	{
//
//		 double[] landmarkPrefix = new double[Simulator.system.getLandmarksNum()];
//
//		 for(int i = 0 ; i < Simulator.system.getLandmarksNum() ; i++)
//		 {
//				 landmarkPrefix[i] = Math.pow(4, SkipGraph.Landmarks.dynamicPrefixLength(i));
//		 }
//
//		 //double sum = 0;
//		 int maxIndex = 0;
//		 double max = Double.MIN_VALUE;
//		 for(int i = 0 ; i < Simulator.system.getLandmarksNum() ; i++)
//		 {
//			 if(landmarkPrefix[i] > max)
//			 {
//				 maxIndex = i;
//				 max = landmarkPrefix[i];
//			 }
//			 //sum = sum + regionsPopulation[i];
//		 }
//
//
//		 for(int i = 0 ; i < Simulator.system.getLandmarksNum() ; i++)
//		 {
//			 landmarkPrefix[i] = (landmarkPrefix[i]/ max);
//		 }
//
//    	 double expectedPopulation = Simulator.system.getSystemCapacity() / Simulator.system.getLandmarksNum();
//		 for(int i = 0; i < Simulator.system.getLandmarksNum() ; i++)
//		 {
//			 adaptiveSubproblemSizes[i] = (int) Math.round(landmarkPrefix[i] * expectedPopulation  * Math.log(Simulator.system.getReplicationDegree()) / Math.log(2));
//		 }
//
//		 for(int i = 0; i < Simulator.system.getLandmarksNum() ; i++)
//		 {
//	    		int subProblemSize = 2;
//	    		while(subProblemSize < adaptiveSubproblemSizes[i] && subProblemSize < Simulator.system.getSystemCapacity())
//	    			subProblemSize *= 2;
//	    		if(subProblemSize > Simulator.system.getSystemCapacity())
//	    			subProblemSize /= 2;
//	    		while(subProblemSize < 32)
//	    			subProblemSize *= 2;
//	    		adaptiveSubproblemSizes[i] = subProblemSize;
//	    }
//
//		 System.out.println("Adaptive Sub-problem defining: ");
//		 for(int i = 0 ; i < Simulator.system.getLandmarksNum() ; i++)
//		 {
//			 System.out.println("Sub-problem size = " + adaptiveSubproblemSizes[i] + "\t SkipGraph.Landmarks Index = " + i + " Length of SkipGraph.Landmarks Prefix " + SkipGraph.Landmarks.dynamicPrefixLength(i));
//		 }
//
//
//	}
//
//	public static void printReplicationTables()
//	{
//		System.out.println("Real World Replica Assignment:");
//		int totalCounter = 0;
//		for(int i = 0 ; i < problemSize ; i++)
//		{
//			int rowCounter = 0;
//			for(int j = 0 ; j < problemSize ; j++)
//			{
//				if(realWorldReplicaAssignment[i][j])
//				{
//					System.out.print("1 ");
//					rowCounter++;
//				}
//				else
//					System.out.print("0 ");
//			}
//			System.out.println(" |" + rowCounter);
//			totalCounter += rowCounter;
//		}
//		System.out.println(totalCounter);
//
//		System.out.println("Replicas: ");
//		for(int i = 0 ; i < problemSize; i++)
//		{
//			if(realWorldReplicaSet[i])
//				System.out.println("Node " + i);
//		}
//	}
//
//
//
//
//}
//
