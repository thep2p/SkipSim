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
//public class Rep_Alg05_SubRegion extends Replication
//{
//
//
//    @Override
//    public double Algorithm(SkipGraph.SkipGraphOperations inputSgo)
//        {
//            sgo = inputSgo;
//            reset();
//            SWD(Simulator.system.PUBLIC_REPLICATION, 0 , 0, 0);
//            tablesInit();
//            replicaSetInit();
//            //repTools.replicaSetGenerator(repTools.PublicOptimizer(repTools.realDistance, repTools.getProblemSize()), "Real", repTools.getProblemSize());
//
//            for (int i = 0; i < Simulator.system.getLandmarksNum(); i++)
//                {
//                    setReplicationDegree(getSubReplicationDegree(i));
//                    if (getReplicationDegree() == 0)
//                        continue;
//                    replicaSetGenerator2(ILP(nameidsDistance, getSubProblemSize(), Simulator.system.PUBLIC_REPLICATION), "Local", getSubProblemSize(), i);
//
//                    localReplicaSetInit();
//                }
//
//            replicaAssignmentSetGenerator(getProblemSize());
//            //assignToOther();
//            //double localDelay = publicTotalDelay();
//            double averageAccessDelay = publicAverageDelay();
//            resetRep();
//
//
//            //Real delay calculation
//            //replicaSetGenerator(PublicOptimizer(realDistance, Simulator.system.getSystemCapacity()), "Real", Simulator.system.getSystemCapacity());
//            //int realDelay  = publicTotalDelay();
//
//            //double ratio = (double)localDelay / realDelay;
//
//            //double averageAccessDelay = localDelay/Simulator.system.getSystemCapacity();
//            //System.out.println("Ratio " + ratio);
//            System.out.println("Subregion average delay: " + averageAccessDelay);
//            setRatioDataSet(Simulator.system.getCurrentTopologyIndex() - 1, averageAccessDelay);
//
//            if (Simulator.system.getCurrentTopologyIndex() == Simulator.system.getTopologyNumbers() && !Simulator.system.isDelayBasedSimulaton())
//                {
//                     evaluation(" Algorithm 05 SubRegionLP ");
//                }
//
//
//            return averageAccessDelay;
//
//
//        }
//}
