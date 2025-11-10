//import time.LocalTime;
//
//
//public class Rep_Alg07_PrivateSubRegion extends Replication {
//
//
//    @Override
//    public double Algorithm(SkipGraph.SkipGraphOperations inputSgo)
//        {
//            sgo = inputSgo;
//            reset();
//            privateRepShareDefining();
//            tablesInit();
//            replicaSetInit();
//            //repTools.replicaSetGenerator(repTools.PrivateOptimizer(repTools.realDistance, repTools.getProblemSize()), "Real", repTools.getProblemSize());
//
//            System.out.println(LocalTime.now());
//            for (int i = 0; i < Simulator.system.getLandmarksNum(); i++)
//                {
//                    setReplicationDegree(getSubReplicationDegree(i));
//                    if (getReplicationDegree() == 0) continue;
//                    replicaSetGenerator2(PrivateOptimizer(nameidsDistance, getNameSpace()), "Local", getNameSpace(), i);
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
//            //System.out.println("Average Latency " + localDelay);
//            //System.out.println("Local Delay " + realDelay);
//
//            //	     if(localDelay == 0)
//            //	     {
//            //	    	 if(Simulator.system.simIndex == 1)
//            //	    		 ratioDataSet[Simulator.system.simIndex - 1] = 1;
//            //	    	 else
//            //	    	     ratioDataSet[Simulator.system.simIndex - 1] = ratioDataSet[Simulator.system.simIndex - 2];
//            //	     }
//            //	     else
//            {
//                // double ratio = (double)localDelay/realDelay;
//                System.out.println("Average Latency " + averageAccessDelay + " Run " + Simulator.system.getCurrentTopologyIndex());
//                setRatioDataSet(Simulator.system.getCurrentTopologyIndex() - 1, averageAccessDelay);
//            }
//
//            System.out.println(LocalTime.now());
//
//            if (Simulator.system.getCurrentTopologyIndex() == Simulator.system.getTopologyNumbers() && !Simulator.system.isDelayBasedSimulaton())
//                {
//                    evaluation(" Algorithm 07 PrivateSubRegion ");
//                }
//
//            return averageAccessDelay;
//
//
//        }
//}
