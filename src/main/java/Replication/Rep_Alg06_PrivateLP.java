//public class Rep_Alg06_PrivateLP extends Replication {
//
//
//    @Override
//    public double Algorithm(SkipGraph.SkipGraphOperations inputSgo)
//        {
//            sgo = inputSgo;
//            reset();
//            tablesInit();
//            replicaSetInit();
//            //repTools.replicaSetGenerator(repTools.PrivateOptimizer(repTools.realDistance, repTools.getProblemSize()), "Real", repTools.getProblemSize());
//            replicaSetGenerator2(PrivateOptimizer(nameidsDistance, getNameSpace()), "Local", getNameSpace());
//            replicaAssignmentSetGenerator(getProblemSize());
//            //realWordTransform();
//            //double averageAccessDelay = privateTotalDelay() / Simulator.system.getDataRequesterNumber();
//            double averageAccessDelay = privateAverageDelay();
//            //int realDelay  = repTools.privateTotalDelay(repTools.realReplicaAssignment);
//
//            //		 double ratio = (double)localDelay/realDelay;
//            System.out.println("Average Delay " + averageAccessDelay);
//            setRatioDataSet(Simulator.system.getCurrentTopologyIndex() - 1, averageAccessDelay);
//
//
//            if (Simulator.system.getCurrentTopologyIndex() == Simulator.system.getTopologyNumbers() && Simulator.system.isReplicationLocalityAwarenessEvaluation() && !Simulator.system.isDelayBasedSimulaton())
//                {
//                    evaluation(" Algorithm 06 PrivateLP ");
//                }
//            if (Simulator.system.isReplicationLoadEvaluation())
//                {
//                    new repEvaluation().loadEvaluation();
//                }
//
//            return averageAccessDelay;
//
//        }
//}
