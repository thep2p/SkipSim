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
//public class Rep_Alg01_LP extends Replication
//{
//	@Override
//	public double Algorithm(SkipGraph.SkipGraphOperations inputSgo)
//		{
//			sgo = inputSgo;
//			System.out.println("Static LP has been started");
//			reset();
//			tablesInit();
//			replicaSetInit();
//
//
//			//realWordTransform();
//			replicaSetGenerator2(ILP(nameidsDistance, getProblemSize(), Simulator.system.PUBLIC_REPLICATION), "Local", getProblemSize());
//			setCorrespondingReplica();
//			//replicaAssignmentSetGenerator(getProblemSize(),sgo);
//			//int localDelay = publicTotalDelay();
//			double averageAccessDelay = publicAverageDelay();
//
//			resetRep();
//
//			//replicaSetGenerator(PublicOptimizer(realDistance, getProblemSize()), "Real", getProblemSize());
//			//int realDelay  = publicTotalDelay();
//
//			//double ratio = (double)localDelay/realDelay;
//			//System.out.println("Ratio " + ratio);
//			//setRatioDataSet(Simulator.system.getCurrentTopologyIndex() - 1, ratio);
//			//double averageAccessDelay = localDelay / Simulator.system.getDataRequesterNumber();
//			System.out.println("Average Local Delay " + averageAccessDelay);
//			setRatioDataSet(Simulator.system.getCurrentTopologyIndex() - 1, averageAccessDelay);
//
//			if (Simulator.system.getCurrentTopologyIndex() == Simulator.system.getTopologyNumbers() && !Simulator.system.isDelayBasedSimulaton())
//			{
//				evaluation(" Algorithm 01 LP ");
//			}
////			if(Simulator.system.isReplicationLoadEvaluation())
////			{
////				new repEvaluation().loadEvaluation();
////			}
//
//			return averageAccessDelay;
//		}
//
//}
