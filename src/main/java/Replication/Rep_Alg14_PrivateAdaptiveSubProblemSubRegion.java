//public class Rep_Alg14_PrivateAdaptiveSubProblemSubRegion extends Replication {
//
//    @Override
//    public double Algorithm(SkipGraph.SkipGraphOperations inputSgo)
//        {
//            sgo = inputSgo;
//            reset();
//            privateRepShareDefining();
//            adaptivePrivateSubproblemSizeDefining();
//            tablesInit();
//            replicaSetInit();
//            //repTools.replicaSetGenerator(repTools.PrivateOptimizer(repTools.realDistance, repTools.getProblemSize()), "Real", repTools.getProblemSize());
//
//            for (int i = 0; i < Simulator.system.getLandmarksNum(); i++)
//                {
//                    setReplicationDegree(getSubReplicationDegree(i));
//                    if (getReplicationDegree() == 0) continue;
//                    setAdaptiveSubProblemSize(i);
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
//            //int realDelay  = repTools.privateTotalDelay(repTools.realReplicaAssignment);
//            //System.out.println("Local Delay " + localDelay);
//            //System.out.println("Real Delay "  + realDelay);
//
//
//
//            System.out.println("Average Delay " + averageAccessDelay + " Run " + Simulator.system.getCurrentTopologyIndex());
//            setRatioDataSet(Simulator.system.getCurrentTopologyIndex() - 1, averageAccessDelay);
//
//            new repEvaluation().privateReplicationLoadAnalysis(getProblemSize(), realWorldReplicaAssignment);
//            if (Simulator.system.getCurrentTopologyIndex() == Simulator.system.getTopologyNumbers() && !Simulator.system.isDelayBasedSimulaton())
//                {
//                    new repEvaluation().loadEvaluation();
//                    evaluation(" Algorithm 14 PrivateAdaptiveSubProblemSubRegion ");
//                }
//
//            return averageAccessDelay;
//
//        }
//}