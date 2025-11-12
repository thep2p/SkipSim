package Aggregation;

import Simulator.SkipSimParameters;
import SkipGraph.Node;
import SkipGraph.SkipGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Created by Yahya on 1/11/2017.
 */
public class Agg_Alg2_BroadcastTree extends Aggregation
    {
        private static final Logger log = LoggerFactory.getLogger(Agg_Alg2_BroadcastTree.class);

        public Agg_Alg2_BroadcastTree(SkipGraphOperations insgo)
            {
                log.info("BroadcastTree started");
                sgo = insgo;
                Random r  = new Random();
                initiator = r.nextInt()% SkipSimParameters.getSystemCapacity();
                while(initiator<0)
                    initiator = r.nextInt()% SkipSimParameters.getSystemCapacity();
                log.debug("Initiator: {}", initiator);
                broadCast(initiator);
                //findRoot();
                energyEvaluation("BroadCast ");
                latencyEvaluation("BroadCast ");
            }
        private void broadCast(int executer)
            {
                for(int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
                    {
                        int leftNeighbor = sgo.getTG().mNodeSet.getNode(executer).getLookup(i,0);
                        if(leftNeighbor >= 0)
                            {
                                if(((Node) sgo.getTG().mNodeSet.getNode(leftNeighbor)).getParent() == -1 && leftNeighbor != initiator)
                                    {
                                        ((Node) sgo.getTG().mNodeSet.getNode(leftNeighbor)).setParent(executer);
                                        ((Node) sgo.getTG().mNodeSet.getNode(executer)).addChildren(leftNeighbor);
                                        //System.out.println("Parent " + executer + " child " + leftNeighbor);
                                        broadCast(leftNeighbor);
                                    }
                            }

                        int rightNeighbor = sgo.getTG().mNodeSet.getNode(executer).getLookup(i,1);
                        if(rightNeighbor >= 0)
                            {
                                if(((Node) sgo.getTG().mNodeSet.getNode(rightNeighbor)).getParent() == -1 && rightNeighbor != initiator)
                                    {
                                        ((Node) sgo.getTG().mNodeSet.getNode(rightNeighbor)).setParent(executer);
                                        ((Node) sgo.getTG().mNodeSet.getNode(executer)).addChildren(rightNeighbor);
                                        //System.out.println("Parent " + executer + " child " + rightNeighbor);
                                        broadCast(rightNeighbor);
                                    }
                            }
                    }
            }
    }
