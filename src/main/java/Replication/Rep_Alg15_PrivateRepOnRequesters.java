//
//import net.sf.javailp.Linear;
//import net.sf.javailp.OptType;
//import net.sf.javailp.Problem;
//import net.sf.javailp.Result;
//import net.sf.javailp.Solver;
//import net.sf.javailp.SolverFactory;
//import net.sf.javailp.SolverFactoryLpSolve;
//import lpsolve.*;
//
//
//
//
//
//public class Rep_Alg15_PrivateRepOnRequesters extends Replication
//{
//
//	 public Result Optimizer(int[][] L, int size)
//	 {
//		 SolverFactory factory = new SolverFactoryLpSolve(); // use lp_solve
//		 factory.setParameter(Solver.VERBOSE, 0);
//		 factory.setParameter(Solver.TIMEOUT, Integer.MAX_VALUE);
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
//					if(j % getM() == 0 && i % getM() == 0)
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
//				 if(j % getM() == 0 && i % getM() == 0)
//				 {
//				 	 linear = new Linear();
//					 String var = "X"+i+","+j;
//					 linear.add(1, var);
//					  var = "Y"+i;
//					 linear.add(-1, var);
//					 problem.add(linear, "<=", 0);
//				 }
//			 }
//
//		 }
//
//		 /**
//		  * Part 3: Sigma(i)Xij = 1
//		  */
//		 for(int j = 0 ; j < size ; j++)
//		 {
//			 if(j % getM() == 0)
//			 {
//				 linear = new Linear();
//				 for(int i = 0 ; i < size ; i++)
//				 {
//					 if(i % getM() == 0)
//					 {
//							 String var = "X"+i+","+j;
//							 linear.add(1, var);
//					 }
//				 }
//				 problem.add(linear, "=", 1);
//			 }
//		 }
//
//
//		 /**
//		  * Part 4: Sigma(j)Xij >= Yi
//		  */
//		 for(int i = 0 ; i < size ; i++)
//		 {
//			 if(i % getM() == 0)
//			 {
//				 linear = new Linear();
//				 for(int j = 0 ; j < size ; j++)
//				 {
//					 if(j % getM() == 0)
//					 {
//						 String var = "X"+i+","+j;
//						 linear.add(-1, var);
//					 }
//				 }
//
//				 String var = "Y"+i;
//				 linear.add(1, var);
//				 problem.add(linear, "<=", 0);
//			 }
//		 }
//
//
//		 /**
//		  * Part 5: Sigma(i)Yi <= MNR
//		  */
//		 linear = new Linear();
//		 for(int i = 0 ; i < size ; i++)
//		 {
//			 if(i % getM() == 0)
//			 {
//				 String var = "Y"+i;
//				 linear.add(1, var);
//			 }
//		 }
//		 problem.add(linear, "<=", getReplicationDegree());
//
//
//		 /**
//		  * Part 6: Set the type of Xij and Yi
//		  */
//		 for(int i = 0 ; i < size ; i++)
//		 {
//			 if(i % getM() == 0)
//			 {
//				 for(int j = 0 ; j < size ; j++)
//				 {
//					 if(j % getM() == 0 )
//					 {
//						 String var = "X"+i+","+j;
//						 problem.setVarType(var, Integer.class);
//					 }
//				 }
//
//				 String var = "Y"+i;
//				 problem.setVarType(var, Integer.class);
//			 }
//		 }
//
//
//		 /**
//		  * Solving the problem
//		  */
//		 Solver solver = factory.get(); // you should use this solver only once for one problem
//		 Result result = solver.solve(problem);
//		 System.out.println(result.toString());
//		 return(result);
//	 }
//
//    @Override
//    public void Algorithm(SkipGraph.SkipGraphOperations sgo)
//    {
//        reset();
//        tablesInit(sgo);
//        replicaSetInit();
//        replicaSetGenerator(Optimizer(realDistance, getProblemSize()), "Real", getProblemSize(), sgo);
//
//        replicaSetGenerator2(Optimizer(nameidsDistance, getNameSpace()), "Local", getNameSpace(), sgo);
//
//        replicaAssignmentSetGenerator(getProblemSize(), sgo);
//        //realWordTransform();
//        int localDelay = privateTotalDelay(realWorldReplicaAssignment);
//        int realDelay = privateTotalDelay(realReplicaAssignment);
//
//        System.out.println("Local " + localDelay);
//        System.out.println("Real  " + realDelay);
//
//        double ratio = (double) localDelay / realDelay;
//        System.out.println("Ratio " + ratio);
//        setRatioDataSet(Simulator.system.getCurrentTopologyIndex() - 1, ratio);
//
//        if (Simulator.system.getCurrentTopologyIndex() == Simulator.system.getTopologyNumbers()) {
//            evaluation(" Algorithm 15 RepOnTheRequesters ");
//        }
//
//    }
//}