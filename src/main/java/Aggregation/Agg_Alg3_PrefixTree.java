package Aggregation;

import Simulator.SkipSimParameters;
import SkipGraph.Node;
import SkipGraph.SkipGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created by Yahya on 1/11/2017.
 */
public class Agg_Alg3_PrefixTree extends Aggregation
    {
        private static final Logger log = LoggerFactory.getLogger(Agg_Alg3_PrefixTree.class);

        public Agg_Alg3_PrefixTree(SkipGraphOperations insgo)
            {
                log.info("PrefixTree started");
                sgo = insgo;
//                Random r  = new Random();
//                initiator = r.nextInt()%Simulator.system.getSystemCapacity();
//                while(initiator<0)
//                    initiator = r.nextInt()%Simulator.system.getSystemCapacity();
//                System.out.println("Initiator " + initiator);
///                initPrefix(initiator);
                ArrayList <Integer> initiatorsList = initiators();
                for(int i = 0 ; i < initiatorsList.size() ; i++)
                    prefix(initiatorsList.get(i));
                findRoot();
                energyEvaluation("Prefix ");
                latencyEvaluation("Prefix ");
            }
//        private void initPrefix(int executer)
//            {
//                for(int i = 0; i < Simulator.system.getLandmarksNum(); i++)
//                    {
//                        int leftNeighbor = sgo.getTG().mNodeSet.getNode(executer).getLookup(i,0);
//                        if(leftNeighbor >= 0)
//                            {
//                                if(sgo.getTG().mNodeSet.getNode(leftNeighbor).getParent() == -1 && leftNeighbor != initiator)
//                                    {
//                                        sgo.getTG().mNodeSet.getNode(leftNeighbor).setParent(executer);
//                                        sgo.getTG().mNodeSet.getNode(executer).addChildren(leftNeighbor);
//                                        System.out.println("Parent " + executer + " child " + leftNeighbor);
//                                        prefix(leftNeighbor);
//                                    }
//                            }
//
//                        int rightNeighbor = sgo.getTG().mNodeSet.getNode(executer).getLookup(i,1);
//                        if(rightNeighbor >= 0)
//                            {
//                                if(sgo.getTG().mNodeSet.getNode(rightNeighbor).getParent() == -1 && rightNeighbor != initiator)
//                                    {
//                                        sgo.getTG().mNodeSet.getNode(rightNeighbor).setParent(executer);
//                                        sgo.getTG().mNodeSet.getNode(executer).addChildren(rightNeighbor);
//                                        System.out.println("Parent " + executer + " child " + rightNeighbor);
//                                        prefix(rightNeighbor);
//                                    }
//                            }
//                    }
//            }

        private ArrayList<Integer> initiators()
            {
                ArrayList<Integer> initiators = new ArrayList<>();
                for(int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
                    {
                        for(int j = 0; j < SkipSimParameters.getSystemCapacity(); j++)
                            {
                                if(commonBits(((Node) sgo.getTG().mNodeSet.getNode(j)).getNameID(), sgo.getTG().mLandmarks.getDynamicPrefix(i)) == sgo.getTG().mLandmarks.getDynamicPrefix(i).length())
                                    {
                                        initiators.add(j);
                                        //System.out.println("Node " + sgo.getTG().mNodeSet.getNode(j).nameID + " selected  from region " + sgo.getTG().mLandmarks.getDynamicPrefix(i) + " " + j);

                                        if(i > 0)
                                            {
                                                ((Node) sgo.getTG().mNodeSet.getNode(initiators.get(0))).addChildren(j);
                                                ((Node) sgo.getTG().mNodeSet.getNode(j)).setParent(initiators.get(0));
                                                //System.out.println("SkipGraph.Node " + initiators.get(0) + " became parent of SkipGraph.Node " + j);

                                            }
                                        break;
                                    }

                            }
                    }
                //System.out.println("*****************");
                return initiators;
            }
        private void prefix(int executer)
            {
                int prefixLength = 0;
                for(int i = 0; i < SkipSimParameters.getLandmarksNum() ; i++)
                    {
                        if(commonBits(((Node) sgo.getTG().mNodeSet.getNode(executer)).getNameID(), sgo.getTG().mLandmarks.getDynamicPrefix(i)) == sgo.getTG().mLandmarks.getDynamicPrefix(i).length())
                            {
                                prefixLength = sgo.getTG().mLandmarks.getDynamicPrefix(i).length();
                                //System.out.println("Prefix Length " + prefixLength);
                                break;
                            }

                    }
                if(prefixLength == 0)
                    {
                        log.error("PrefixTree error: prefixLength = 0");
                        System.exit(0);
                    }

                for(int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
                    {
                        if(commonBits(((Node) sgo.getTG().mNodeSet.getNode(executer)).getNameID(), ((Node) sgo.getTG().mNodeSet.getNode(i)).getNameID()) >= prefixLength
                                && (((Node) sgo.getTG().mNodeSet.getNode(i)).getParent() == -1) && i != executer)
                            {
                                ((Node) sgo.getTG().mNodeSet.getNode(i)).setParent(executer);
                                ((Node) sgo.getTG().mNodeSet.getNode(executer)).addChildren(i);
                                //System.out.println("Parent " + executer + " child " + i);
                            }

                    }

            }
    }
