//import time.LocalTime;
//
//
//public class Rep_Alg13_AdaptiveSubProblemSubRegion extends Replication {
//
//    @Override
//    public double Algorithm(SkipGraph.SkipGraphOperations inputSgo)
//        {
//
//            sgo = inputSgo;
//            //reset();
//            updateRegionsPopulation(Simulator.system.PUBLIC_REPLICATION);
//            //publicRepShareDefining();
//            improvedRepShare(Simulator.system.PUBLIC_REPLICATION);
//            adaptiveSubproblemSizeDefining(16);
//            tablesInit();
//            replicaSetInit();
//
//            //repTools.replicaSetGenerator(repTools.PublicOptimizer(repTools.realDistance, repTools.getProblemSize()), "Real", repTools.getProblemSize());
//
//
//            for (int i = 0; i < Simulator.system.getLandmarksNum(); i++)
//                {
//                    setReplicationDegree(getSubReplicationDegree(i));
//                    if (getReplicationDegree() == 0) continue;
//                    setAdaptiveSubProblemSize(i);
//                    nameidsDistanceGenerator();
//                    replicaSetGenerator2(ILP(nameidsDistance, getSubProblemSize(), Simulator.system.PUBLIC_REPLICATION), "Local", getSubProblemSize(), i);
//                    //System.out.println(repStatus);
//                    //validityTest();
//                    //realWordTransform(i);
//                    localReplicaSetInit();
//                }
//
//            setCorrespondingReplica();
//            //replicaAssignmentSetGenerator(getProblemSize());
//            //assignToOther();
//            //double localDelay = publicTotalDelay();
//            //printReplicationTables(sgo);
//            //int realDelay  = repTools.publicTotalDelay(repTools.realReplicaAssignment);
//            //System.out.println("Real Delay " + localDelay);
//            //System.out.println("Local Delay " + realDelay);
//            double averageAccessDelay = publicAverageDelay();
//
//            //double averageAccessDelay = (double) localDelay / Simulator.system.getSystemCapacity(); ///realDelay;
//            System.out.println("Average Delay " + averageAccessDelay + " Run " + Simulator.system.getCurrentTopologyIndex());
//            setRatioDataSet(Simulator.system.getCurrentTopologyIndex() - 1, averageAccessDelay);
//
//            //repEvaluation.publicReplicationLoadAnalysis(repTools.getProblemSize(), repTools.realWorldReplicaAssignment);
//            if (Simulator.system.getCurrentTopologyIndex() == Simulator.system.getTopologyNumbers() && !Simulator.system.isDelayBasedSimulaton())
//                {
//                    //repEvaluation.loadEvaluation();
//                    evaluation(" Algorithm 13 AdaptiveSubProblemSubRegion ");
//                    System.out.println(LocalTime.now());
//                }
//
//            return averageAccessDelay;
//        }
//
//
//    /*
//    Note: before calling this function the subProblemNameIDSize and subProblemSize should be updated accordingly.
//     */
//    private void nameidsDistanceGenerator()
//        {
//            nameidsDistance = new int[getSubProblemSize()][getSubProblemSize()];
//            for (int i = 0; i < getSubProblemSize(); i++)
//                for (int j = 0; j < getSubProblemSize(); j++)
//                    {
//                        if (i == j)
//                            {
//                                nameidsDistance[i][j] = 0;
//                            } else nameidsDistance[i][j] = Simulator.system.getNameIDLength() - commonPrefixLength(i, j);
//                    }
//        }
//}