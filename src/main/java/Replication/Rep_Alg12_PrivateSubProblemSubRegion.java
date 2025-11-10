//public class Rep_Alg12_PrivateSubProblemSubRegion extends Replication {
//
//    @Override
//    public double Algorithm(SkipGraph.SkipGraphOperations inputSgo)
//        {
//            reset();
//            privateRepShareDefining();
//            tablesInit();
//            replicaSetInit();
//            //repTools.replicaSetGenerator(repTools.PrivateOptimizer(repTools.realDistance, repTools.getProblemSize()), "Real", repTools.getProblemSize());
//
////		 System.out.println(LocalTime.now());
//
//            for (int i = 0; i < Simulator.system.getLandmarksNum(); i++)
//                {
//                    setReplicationDegree(getSubReplicationDegree(i));
//                    if (getReplicationDegree() == 0) continue;
//                    replicaSetGenerator2(PrivateOptimizer(nameidsDistance, getSubProblemSize()), "Local", getSubProblemSize(), i);
//                    //validityTest();
//                    //realWordTransform(i);
//                    localReplicaSetInit();
//                }
//
//            replicaAssignmentSetGenerator(getProblemSize());
//            //assignToOther();
//            //double averageAccessDelay = privateTotalDelay() / Simulator.system.getDataRequesterNumber();
//            double averageAccessDelay = privateAverageDelay();
////	     int realDelay  = repTools.privateTotalDelay(repTools.realReplicaAssignment);
////         System.out.println("Local Delay " + localDelay);
////         System.out.println("Real Delay "  + realDelay);
//
//
////		 double ratio = (double)localDelay/realDelay;
//            System.out.println("Average Delay " + averageAccessDelay + " Run " + Simulator.system.getCurrentTopologyIndex());
//            setRatioDataSet(Simulator.system.getCurrentTopologyIndex() - 1, averageAccessDelay);
//
////		 System.out.println(LocalTime.now());
//
////		 repEvaluation.privateReplicationLoadAnalysis(repTools.getProblemSize(), repTools.realWorldReplicaAssignment,repTools.getM());
//            if (Simulator.system.getCurrentTopologyIndex() == Simulator.system.getTopologyNumbers() && !Simulator.system.isDelayBasedSimulaton())
//                {
//                    new repEvaluation().loadEvaluation();
//                    evaluation(" Algorithm 12 PrivateSubProblemSubRegion ");
//                }
//
//            return averageAccessDelay;
//
//
//        }
//}