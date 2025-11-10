//public class Rep_Alg11_SubProblemSubRegion extends Replication {
//    @Override
//    public double Algorithm(SkipGraph.SkipGraphOperations inputSgo)
//        {
//            sgo = inputSgo;
//            reset();
//            SWD(Simulator.system.PUBLIC_REPLICATION, 0, 0, 0);
//            tablesInit();
//            replicaSetInit();
//            //repTools.replicaSetGenerator(repTools.PublicOptimizer(repTools.realDistance, repTools.getProblemSize()), "Real", repTools.getProblemSize());
//
//
//            for (int i = 0; i < Simulator.system.getLandmarksNum(); i++)
//                {
//                    setReplicationDegree(getSubReplicationDegree(i));
//                    if (getReplicationDegree() == 0) continue;
//                    replicaSetGenerator2(ILP(nameidsDistance, getSubProblemSize(), Simulator.system.PUBLIC_REPLICATION), "Local", getSubProblemSize(), i);
//                    //validityTest();
//                    //realWordTransform(i);
//                    localReplicaSetInit();
//                }
//
//            replicaAssignmentSetGenerator(getProblemSize());
//            //assignToOther();
//            //double averageAccessDelay = publicTotalDelay() / Simulator.system.getSystemCapacity();
//            //int realDelay  = repTools.publicTotalDelay(repTools.realReplicaAssignment);
//            //System.out.println("Real Delay " + localDelay);
//            //System.out.println("Local Delay " + realDelay);
//            double averageAccessDelay = publicAverageDelay();
//
//
//            System.out.println("Average Delay " + averageAccessDelay + " Run " + Simulator.system.getCurrentTopologyIndex());
//            setRatioDataSet(Simulator.system.getCurrentTopologyIndex() - 1, averageAccessDelay);
//
//            new repEvaluation().publicReplicationLoadAnalysis(getProblemSize(), realWorldReplicaAssignment);
//            if (Simulator.system.getCurrentTopologyIndex() == Simulator.system.getTopologyNumbers() && !Simulator.system.isDelayBasedSimulaton())
//                {
//                    new repEvaluation().loadEvaluation();
//                    evaluation(" Algorithm 11 SubProblemSubRegion ");
//                }
//
//            return averageAccessDelay;
//
//
//        }
//}