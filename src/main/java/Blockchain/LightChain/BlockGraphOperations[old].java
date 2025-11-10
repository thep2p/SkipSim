package Blockchain.LightChain;//package Blockchain.LightChain;
//
//import Blockchain.LightChain.Block;
//import Blockchain.LightChain.blocks;
//import DataTypes.Constants;
//import DataTypes.Message;
//import Simulator.system;
//import SkipGraph.SignatureLookupTable;
//import SkipGraph.SkipGraphOperations;
//import SkipGraph.Node;
//
//import java.util.*;
//
//public class BlockGraphOperations
//{
//
//    //************************
//    private ArrayList<String> nameIDsOnPath;
//    private Random searchRandomGenerator;
//    private blocks bs;
//    private SkipGraphOperations sgo;
//
//    ///////////////////////////////
//    public BlockGraphOperations(SkipGraphOperations sgo)
//    {
//        searchRandomGenerator = new Random();
//        bs = new blocks(sgo.tg.mNodeSet);
//        System.gc(); //a call to system garbage collector
//        this.sgo = sgo;
//    }
//
//    public Random getSearchRandomGenerator()
//    {
//        return searchRandomGenerator;
//    }
//
//    public int insert(Block b, int index, boolean dynamicNameID)
//    {
//        b.setIndex(index); //
//        bs.setBlock(index, b);
//        if (system.getSimulationType().equalsIgnoreCase(Constants.SimulationType.STATIC) && index == 0)
//        {
//            return b.getIndex();
//        } else if (dynamicNameID)
//        {
//            System.out.println("Search has been started for : " + b.getIndex());
//            //TODO: What to do here? Do we need this part? -- replace it with bs. it is needed.
//            int code = insert(index);
//            if (bs.getBlock(index).isLookupTableEmpty() && code == Constants.SkipGraphOperation.Inserstion.NON_EMPTY_LOOKUP_TABLE)
//            {
//                System.err.println("SkipGraphOperation.java: Empty lookup table after insertion");
//                System.exit(0);
//            }
//            //print("Search has been finished");
//        }
//
//        //n.printLookup();
//        return b.getIndex();
//    }
//
//    // TODO: If it's only used in static simulation, we don't need this part
//    //TODO: As we looked where this "renewInsertion" method is used, it is only used in Static Simulation. Can we delete this method?
//    /**
//    public int renewInsertion(int index, boolean dynamicNameID)
//    {
//        bs.getBlock(index).setIndex(index);
//        if (index > 0 && dynamicNameID)
//        {
//            insert(index);
//        }
//
//
//        //tg.mNodeSet.getNode(index).printLookup();
//        return bs.getBlock(index).getIndex();
//    }
//    **/
//
//    // TODO: if not used, we don't need but it's better to keep it here for now
//    //TODO: This method (in SkipGraphOperations) is never used. Do we need it in BlockChainSimulation?
//
//    /**
//    public void allPairsLookup(Nodes mNodeSet, boolean isBucketBased)
//    {
//        Random random = new Random();
//        int counter = 0;
//        for (int i = 0; i < system.getSystemCapacity(); i++)
//        {
//            if (mNodeSet.getNode(i).isOffline())
//            {
//                continue;
//            }
//            for (int j = 1; j < system.getSystemCapacity(); j++)
//            {
//
//                if (mNodeSet.getNode(i).isOffline() || i == j)
//                {
//                    continue;
//                } else
//                {
//                    counter++;
//                }
//
//
//
//                // Cleaning the DataTypes.Message for piggybacking
//
//                Message m = new Message();
//
//
//                // type of lookup: 0 = search by name id, 1 = search by numerical id
//
//                int searchType = random.nextInt() % 2;
//                if (searchType == 0)
//                {
//                    SearchByNameID(mNodeSet.getNode(j).nameID, i, mNodeSet.getNode(i).getLookup(0, 1), mNodeSet.getNode(i).getLookup(0, 0), 0, m);
//                } else
//                {
//                    SearchByNumID(mNodeSet.getNode(j).getNumID(), i, m, system.getLookupTableSize() - 1);
//                }
//
//            }
//        }
//        if (system.isLog())
//        {
//            System.out.println(counter + " number of all pairs lookup have been done");
//        }
//
//    }
//    */
//    // TODO: same as above, also we should replace tg.mNodeSet with bs
//    // TODO: This methods is never used (Like the one above). Do we keep and modify it?
//
//    /**
//     * @param random the search random generator, use the getter of searchRandomGenerator of the same class
//     * @return -1 if could not perform the search due to the low number of online Nodes, otherwise the success search ratio
//     */
//
//    /**
//    public double randomizedSearchForNumericalIDs(Random random)
//    {
//        if (tg.mNodeSet.getNumberOfOnlineNodes() < 0.01 * system.getSystemCapacity())
//        {
//            return -1;
//        }
//        int numOfActiveNodes = tg.mNodeSet.getNumberOfOnlineNodes();
//        int iterations = random.nextInt(numOfActiveNodes * (numOfActiveNodes - 1) / 2) + 1;
////  	     while(iterations < numOfActiveNodes / 2)
////  	    	iterations = random.nextInt(numOfActiveNodes);
//        ChurnStochastics.updateAverageLookups(iterations);
//
//        //Commented for the sake of time
//           //System.out.println("At " + Simulator.system.getCurrentTimeInHourMinFormat() + " " + iterations + " random iterations initiated "
//           //+ " Number of active SkipGraph.Nodes " + SkipGraph.Nodes.offlineNodesCounter());
//
//        double counter = 0;
//        Set<Pair> searchesSet = new HashSet<>();
//        for (int i = 0; i < iterations; i++)
//        {
//            int searchTarget = tg.randomlyPickOnline();
//            //
//             // picking search searchTarget
//             //
//
//            ///while (tg.mNodeSet.getNode(searchTarget).isOffline()) searchTarget = random.nextInt(system.getSystemCapacity() - 1);
//
//             // picking search initiator
//
//
//            int initiator = tg.randomlyPickOnline();
//            Pair search = new Pair(initiator, searchTarget);
//            while (initiator == searchTarget || searchesSet.contains(search))
//            {
//                initiator = tg.randomlyPickOnline();
//                search = new Pair(initiator, searchTarget);
//            }
//            searchesSet.add(search);
//
//
//
//             // Cleaning the DataTypes.Message for piggybacking
//
//            Message m = new Message();
//
//
//             //type of lookup: 0 = search by name id, 1 = search by numerical id
//            //TODO make search by name ID churn resilient
//            //boolean searchType = random.nextBoolean();
//            //if (searchType)
//            //{
//            //    SearchByNameID(tg.mNodeSet.getNode(searchTarget).nameID, initiator, tg.mNodeSet.getNode(initiator).getLookup(0, 1), tg.mNodeSet.getNode(initiator).getLookup(0, 0), 0, m);
//            //}
//            //else
//            //{
//            int searchResult = SearchByNumID(tg.mNodeSet.getNode(searchTarget).getNumID(), initiator, m, system.getLookupTableSize() - 1);
//            if (tg.mNodeSet.getNode(searchResult).getNumID() == tg.mNodeSet.getNode(searchTarget).getNumID())
//            {
//                counter++;
//                ChurnStochastics.updateAverageSuccessTimeOuts();
//            } else
//            {
//                ChurnStochastics.updateAverageFailureTimeOuts();
////                System.out.println("------------------------------------------------------------");
////                System.out.println("Search for " + tg.mNodeSet.getNode(searchTarget).getNumID() + " index " + searchTarget + " from " + initiator + " failed " );
////                System.out.println("The result is " + searchResult + " with num ID of " +  tg.mNodeSet.getNode(searchResult).getNumID());
////                m.printSearchPath(tg.mNodeSet, false);
////                //tg.mNodeSet.getNode(searchResult).printBucket();
////                tg.mNodeSet.printLookupNumID(searchResult);
////                tg.mNodeSet.printLookupOnlineStatus(searchResult);
////                System.out.println("------------------------------------------------------------");
//            }
//            //}
//        }
//
//
//        if (iterations < 0)
//        {
//            System.err.println("SkipGraphOperations.java: All pairs random Lookup failed");
//            System.exit(0);
//        }
//
//        return (double) counter / iterations;
//
//    }
//    **/
//
//    public int commonPrefixLength(String s1, String s2)
//    {
//
//        int k = 0;
//        if (s1.isEmpty() || s2.isEmpty())
//        {
//            return 0;
//        }
//        while (s1.charAt(k) == s2.charAt(k))
//        {
//            k++;
//            if (k >= s1.length() || k >= s2.length() || k >= system.getBlockLookupTableSize() - 1)
//            {
//                break;
//            }
//        }
//
//        return k;
//
//    }
//
//    public int insert(int index)
//    {
//
//        //System.out.println("Insert started!");
//        int Left = -1;
//        int Right = -1;
//
//        if (system.getSimulationType().equals(Constants.SimulationType.DYNAMIC) || system.getSimulationType().equals(Constants.SimulationType.BLOCKCHAIN))
//        {
//            int counter = 0;
//            while (bs.getBlock(bs.getBlock(index).introducer) == null || bs.getBlock(bs.getBlock(index).introducer).isOffline(sgo.getTG().mNodeSet) || bs.getBlock(index).introducer == index)
//            {
//                bs.getBlock(index).introducer++;
//                bs.getBlock(index).introducer %= system.getBlockCapacity();
//                if (++counter == system.getBlockCapacity())
//                {
//                    return Constants.SkipGraphOperation.Inserstion.EMPTY_LOOKUP_TABLE;
//                }
//            }
//        }
//
//        Message m = new Message();
//        System.out.println("owner Node is " + bs.getBlock(index).getOwnerIndex());
//        //int searchResult = SearchByNumID(bs.getBlock(index).getNumID(), bs.getBlock(index).introducer, new Message(), system.getBlockLookupTableSize() - 1);//gets the port of this SkipGraph
//        int searchResult = SearchByNumID(bs.getBlock(index).getNumID(), bs.getBlock(index).introducer, new Message(), system.getBlockLookupTableSize() - 1);//gets the port of this SkipGraph
//        //m.printSearchPath(tg.mNodeSet,false);
//        // .Node's
//
//
//        // NumID
//        //if(system.isLog())
//        //System.out.println("The search result for insertion of " + tg.mNodeSet.getNode(index).getNumID() + " num ID is " + tg.mNodeSet.getNode(searchResult).getNumID() +" num ID" );
//        int predecessor, successor;
//
//        if (bs.getBlock(index).getNumID() < bs.getBlock(searchResult).getNumID())
//        {
//            predecessor = bs.getBlock(searchResult).getLookup(0, 0);
//            successor = searchResult;
//            //System.err.println("The predecessor greater found less than the Node to be inserted");
//        } else
//        {
//            predecessor = searchResult;
//            successor = bs.getBlock(searchResult).getLookup(0, 1);
//        }
//
//        int insertStatus = adaptiveInsert(index, predecessor, successor);
////        if(!lookupTableValidation(index))
////        {
////            System.err.println("SkipGraphOperations.java: lookup table violation");
////            System.exit(0);
////        }
////        System.out.println("---------------------------------------------------------");
////        System.out.println("After Insertion: ");
////        if(predecessor != -1)
////        {
////            System.out.println("Predecessor: " + tg.mNodeSet.getNode(predecessor).nameID + " " + tg.mNodeSet.getNode(predecessor).getNumID());
////            tg.mNodeSet.getNode(predecessor).printLookup();
////        }
////        System.out.println("Node: "+ tg.mNodeSet.getNode(index).nameID + " " + tg.mNodeSet.getNode(index).getNumID());
////        tg.mNodeSet.getNode(index).printLookup();
////        if(successor != -1)
////        {
////            System.out.println("Successor: " + tg.mNodeSet.getNode(successor).nameID + " " + tg.mNodeSet.getNode(successor).getNumID());
////            tg.mNodeSet.getNode(successor).printLookup();
////        }
////        System.out.println("---------------------------------------------------------");
//        return insertStatus;
//    }
//
//    //TODO: This method is only used in "insert"(with one parameter). If we keep "insert" we will keep and mofify this method.
//    //TODO: we should keep it, left and right means left and right blocks in the same level of skipgraph, we should modify it
//    private int adaptiveInsert(int index, int Left, int Right)
//    {
//        /*
//        Only is used to check the existence of loops in dynamic simulation adversarial churn
//         */
//        ArrayList<Integer> visitedRightBlocks = new ArrayList<>();
//        ArrayList<Integer> visitedLeftBlocks = new ArrayList<>();
//
//
//        bs.getBlock(index).setLookup(0, 0, Left);
//        if (Left != -1)
//        {
//            bs.getBlock(Left).setLookup(0, 1, index);
//            visitedRightBlocks.add(Left);
//        }
//        bs.getBlock(index).setLookup(0, 1, Right);
//        if (Right != -1)
//        {
//            bs.getBlock(Right).setLookup(0, 0, index);
//            visitedRightBlocks.add(Right);
//        }
//
//
//        int level = 0;
//        while (true)
//        {
//            while (true)
//            {
//                //System.out.println("SkipGraphOperations.java: adaptive insert inner loop, Right " + Right + " Left " + Left);
//                if (Left != -1)
//                {
//                    //System.out.println("SkipGraphOperations.java: adaptive insert inner loop Node name ID " + tg.mNodeSet.getNode(index).nameID + " Left name ID " + tg.mNodeSet.getNode(Left).nameID + " level " + level
//                    //       + " common bits " + commonPrefixLength(tg.mNodeSet.getNode(Left).nameID, tg.mNodeSet.getNode(index).nameID));
//                    if (commonPrefixLength(bs.getBlock(Left).getNameID(), bs.getBlock(index).getNameID()) <= level)
//                    {
//                        Left = bs.getBlock(Left).getLookup(level, 0);
//                        //System.out.println("SkipGraphOperations.java: insertion inner loop, left was switched to " + Left );
//                        //tg.mNodeSet.getNode(index).printLookup();
//                        if (visitedRightBlocks.contains(Left)) //Cycle checking in dynamic adversarial churn
//                        {
//                            if (system.getSimulationType().equalsIgnoreCase(Constants.SimulationType.DYNAMIC))
//                            {
//                                if (system.getChurnType().equalsIgnoreCase(Constants.Churn.Type.ADVERSARIAL))
//                                {
//                                    Left = -1;
//                                    break;
//                                } else
//                                {
//                                    System.err.println("SkipGraphOperations.java: cycle detected on visited left during non adversarial churn insertion");
//                                    System.exit(0);
//                                }
//                            } else
//                            {
//                                //System.err.println("SkipGraphOperations.java: cycle detected on visited lefts during non-dynamic simulation insertion");
//                                //System.exit(0);
//                            }
//                        } else
//                        {
//                            if (Left != -1)
//                            {
//                                visitedRightBlocks.add(Left);
//                            }
//                        }
//                    } else
//                    {
//                        break;
//                    }
//                }
//
//                if (Right != -1)
//                {
//                    //System.out.println("SkipGraphOperations.java: adaptive insert inner loop Node name ID " + tg.mNodeSet.getNode(index).nameID + " Right name ID " + tg.mNodeSet.getNode(Right).nameID + " level " + level
//                    //        + " common bits " + commonPrefixLength(tg.mNodeSet.getNode(Right).nameID, tg.mNodeSet.getNode(index).nameID));
//                    if (commonPrefixLength(bs.getBlock(Right).getNameID(), bs.getBlock(index).getNameID()) <= level)
//                    {
//                        Right = bs.getBlock(Right).getLookup(level, 1);
//                        //System.out.println("SkipGraphOperations.java: insertion inner loop, right was switched to " + Right );
//                        //tg.mNodeSet.getNode(index).printLookup();
//                        if (visitedRightBlocks.contains(Right))
//                        {
//                            if (system.getSimulationType().equalsIgnoreCase(Constants.SimulationType.DYNAMIC))
//                            {
//                                if (system.getChurnType().equalsIgnoreCase(Constants.Churn.Type.ADVERSARIAL))
//                                {
//                                    Right = -1;
//                                    break;
//                                } else
//                                {
//                                    System.err.println("SkipGraphOperations.java: cycle detected on visited right during non adversarial churn insertion");
//                                    System.exit(0);
//                                }
//                            } else
//                            {
//                                System.err.println("SkipGraphOperations.java: cycle detected on visited right during non-dynamic simulation insertion");
//                                //System.exit(0);
//                            }
//                        } else
//                        {
//                            if (Right != -1)
//                            {
//                                visitedRightBlocks.add(Right);
//                            }
//                        }
//                    } else
//                    {
//                        break;
//                    }
//                }
//                if (Right == -1 && Left == -1)
//                {
//                    break;
//                }
//
//            }
//
//            if (Left != -1)
//            {
//                if (commonPrefixLength(bs.getBlock(Left).getNameID(), bs.getBlock(index).getNameID()) > level)
//                {
//                    int RightNeighbor = bs.getBlock(Left).getLookup(level + 1, 1);
//
//                    bs.getBlock(Left).setLookup(level + 1, 1, index);
//                    if (RightNeighbor != -1)
//                    {
//                        bs.getBlock(RightNeighbor).setLookup(level + 1, 0, index);
//                    }
//
//                    //if((level != Simulator.system.getLookupTableSize() - 1) || tg.mNodeSet.getNode(index).getLookup(level, 1) == -1)
//                    {
//                        bs.getBlock(index).setLookup(level + 1, 0, Left);
//                        bs.getBlock(index).setLookup(level + 1, 1, RightNeighbor);
//                        Right = RightNeighbor;
//                    }
//                    level++; //Has to add to DS version
//                }
//
//            } else if (Right != -1)
//            {
//                if (commonPrefixLength(bs.getBlock(Right).getNameID(), bs.getBlock(index).getNameID()) > level)
//                {
//                    int LeftNeighbor = -1;
//                    LeftNeighbor = bs.getBlock(Right).getLookup(level + 1, 0);
//
//                    bs.getBlock(Right).setLookup(level + 1, 0, index);
//                    if (LeftNeighbor != -1)
//                    {
//                        bs.getBlock(LeftNeighbor).setLookup(level + 1, 1, index);
//                    }
//
//                    //if((level != Simulator.system.getLookupTableSize() - 1) || tg.mNodeSet.getNode(index).getLookup(level, 0) == -1)
//                    {
//                        bs.getBlock(index).setLookup(level + 1, 0, LeftNeighbor);
//                        bs.getBlock(index).setLookup(level + 1, 1, Right);
//                        Left = LeftNeighbor;
//                    }
//
//                    level++; //Has to add to the DS version
//                }
//            }
//
//
//            //level++;   Has to remove from DS version
//            if (level >= system.getBlockLookupTableSize())
//            {
//                break;
//            }
//            if (Left == -1 && Right == -1)
//            {
//                break;
//            }
//        }
//        if (bs.getBlock(index).isLookupTableEmpty())
//        {
//            if (system.getChurnType().equalsIgnoreCase(Constants.Churn.Type.ADVERSARIAL))
//            {
//                return Constants.SkipGraphOperation.Inserstion.EMPTY_LOOKUP_TABLE;
//            } else
//            {
//                System.err.println("SkipGraphOperations.java: empty lookup table in cooperative churn is detected");
//                System.exit(0);
//            }
//        }
//        return Constants.SkipGraphOperation.Inserstion.NON_EMPTY_LOOKUP_TABLE;
//    }
//
//    //TODO: This method is never used. Do we need it in BlockChainSimulation? Call it after each insertion, if returns false, exit running and debug
//    //TODO: it is needed
//    /**
//     * @param index Node index
//     * @return checks the preservation of logical order among between the Node and its neighbors, returns ture if
//     * it is satisfied for all negihbors, and returns false otherwise.
//     */
//
//    private boolean lookupTableValidation(int index)
//    {
//        int numID = bs.getBlock(index).getNumID();
//        for (int i = 0; i < system.getBlockLookupTableSize(); i++)
//        {
//            int rightNeighbor = bs.getBlock(index).getLookup(i, 1);
//            if (rightNeighbor != -1 && numID > bs.getBlock(rightNeighbor).getNumID())
//            {
//                return false;
//            }
//            int leftNeighbor = bs.getBlock(index).getLookup(i, 0);
//            if (leftNeighbor != -1 && numID < bs.getBlock(leftNeighbor).getNumID())
//            {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    /**
//     * @param searchTarget the name ID under search
//     * @return search path in the case of static simulation, and only the search result in case of dynamic
//     */
//    public ArrayList<String> SearchForNameIDPath(String searchTarget)
//    {
//        nameIDsOnPath = new ArrayList<>();
//        for (int i = 0; i < system.getBlockCapacity(); i++)
//        {
//            if (bs.getBlock(i).isOnline(sgo.getTG().mNodeSet) && bs.getBlock(i).getNameID().equals(searchTarget))
//            {
//                nameIDsOnPath.add(searchTarget);
//            }
//
//        }
//
//        return nameIDsOnPath;
//
//    }
//
//    // TODO: don't need
//    //TODO: What does this method actually do? We modified it for "BlockGraphOperations" but not sure if it is needed?
//    private void piggyBackLookupTable(int index)
//    {
//        if (index == -1)
//        {
//            return;
//        }
//        nameIDsOnPath.add(bs.getBlock(index).getNameID());
//        //TODO: Here again we changed getLookupTableSize() to getBlockLookupTableSize()
//        for (int i = 0; i < system.getBlockLookupTableSize(); i++)
//        {
//            for (int j = 0; j < 2; j++)
//            {
//
//                int neighborAddress = bs.getBlock(index).getLookup(i,j);
//                if (neighborAddress == -1)
//                {
//                    continue;
//                }
//                String neighborNameID = bs.getBlock(neighborAddress).getNameID();
//                if (!neighborNameID.isEmpty() && !nameIDsOnPath.contains(neighborNameID))
//                {
//                    nameIDsOnPath.add(neighborNameID);
//                }
//            }
//        }
//    }
//
//    //TODO: Who uses this method?
//    //TODO: Who is the initiator?
//    //TODO: Who starts the searchByNameID. NOT IN GENERAL BUT THIS METHOD.
//    //TODO: Should we change every NODE related stuff to BLOCK. Maybe some of them should be still NODE related.
//
//    //TODO: initiator is the index of the Node that contains the Block, return value should be the Node that contains
//    //TODO: the Block that is searched (search target Block) I think we should change all NODE stuff to BLOCK but be
//    //TODO: be careful about the return value which should be the index of Node that contains the Block
//    public int SearchByNameID(String searchTarget, int initiator, int Right, int Left, int Level, Message m)
//    {
//        Node initiatorNode = sgo.getTG().mNodeSet.getNode(initiator);
//        Set<Block> ownedBlocks = SignatureLookupTable.signatureLookupTable.get(initiatorNode.getNumID());
//        Block closestBlock = null;
//        int maxCommonBits = -1 ;
//        for (Block b : ownedBlocks){
//            int commonPrefixLength = bs.commonPrefixLength(searchTarget,b.getNameID());
//            if (commonPrefixLength > maxCommonBits) {
//                maxCommonBits = commonPrefixLength;
//                closestBlock = b;
//            }
//        }
//
//        int initiatorBlockIndex = closestBlock.getIndex();
//
//        piggyBackLookupTable(initiatorBlockIndex);
//        m.piggybackBlock(initiatorBlockIndex, this, 0);
//            if (Left != -1 && bs.getBlock(Left).isOnline(sgo.getTG().mNodeSet))
//            {
//                m.piggybackBlock(Left, this, 0);
//            }
//            if (Right != -1 && bs.getBlock(Right).isOnline(sgo.getTG().mNodeSet))
//            {
//            m.piggybackBlock(Right, this, 0);
//        }
//
//        if (bs.getBlock(initiatorBlockIndex).getNameID().equals(searchTarget))
//        {
//            return bs.getBlock(initiatorBlockIndex).getOwnerIndex();
//        }
//
//        int Buffer = 0;
//        while (true)
//        {
//            if (Left != -1 && bs.getBlock(Left).getNameID().equals(searchTarget))
//            {
//                piggyBackLookupTable(Left);
//                m.piggybackBlock(Left, this, 0);
//                return bs.getBlock(Left).getOwnerIndex();
//            }
//            if (Right != -1 && bs.getBlock(Right).getNameID().equals(searchTarget))
//            {
//                piggyBackLookupTable(Right);
//                m.piggybackBlock(Right, this, 0);
//                return bs.getBlock(Right).getOwnerIndex();
//            }
//            if (Left != -1 && bs.getBlock(Left).isOnline(sgo.getTG().mNodeSet))
//            {
//                Buffer = Left;
//                Left = bs.getBlock(Left).getLookup(Level, 0);
//                piggyBackLookupTable(Left);
//            }
//            if (Right != -1 && bs.getBlock(Right).isOnline(sgo.getTG().mNodeSet))
//            {
//                Buffer = Right;
//                Right = bs.getBlock(Right).getLookup(Level, 1);
//                piggyBackLookupTable(Right);
//            }
//
//
//            if (Right != -1 && bs.getBlock(Right).isOnline(sgo.getTG().mNodeSet))
//            {
//                piggyBackLookupTable(Right);
//                m.piggybackBlock(Right, this, 0);
//                if (commonPrefixLength(bs.getBlock(Right).getNameID(), searchTarget) > Level)
//                {
//                    break;
//                }
//            }
//            if (Left != -1 && bs.getBlock(Left).isOnline(sgo.getTG().mNodeSet))
//            {
//                piggyBackLookupTable(Left);
//                m.piggybackBlock(Left, this, 0);
//                if (commonPrefixLength(bs.getBlock(Left).getNameID(), searchTarget) > Level)
//                {
//                    break;
//                }
//            }
//
//            if ((Right == -1 && Left == -1))
//            {
//                //System.out.println("Buffer return!");
//                //System.out.println(Level);
//                //System.out.println(tg.mNodeSet.getNode(initiatorBlockIndex).nameID);
//                return bs.getBlock(Buffer).getOwnerIndex();
//            }
//
//            if ((Right != -1 && Left != -1) && (bs.getBlock(Left).isOffline(sgo.getTG().mNodeSet) && bs.getBlock(Right).isOffline(sgo.getTG().mNodeSet)))
//            {
//                //System.out.println("Buffer return!");
//                //System.out.println(Level);
//                //System.out.println(tg.mNodeSet.getNode(initiatorBlockIndex).nameID);
//                return bs.getBlock(Buffer).getOwnerIndex();
//            }
//
//
//        }
//
//        if (Right != -1 && commonPrefixLength(bs.getBlock(Right).getNameID(), searchTarget) > Level)
//        {
//            Level = commonPrefixLength(bs.getBlock(Right).getNameID(), searchTarget);
//            Buffer = Right;
//            Left = bs.getBlock(Right).getLookup(Level, 0);
//            Right = bs.getBlock(Right).getLookup(Level, 1);
//            //System.out.println("R call");
//            return SearchByNameID(searchTarget, Buffer, Right, Left, Level, m);
//        }
//        if (Left != -1 && commonPrefixLength(bs.getBlock(Left).getNameID(), searchTarget) > Level)
//        {
//            Level = commonPrefixLength(bs.getBlock(Left).getNameID(), searchTarget);
//            Buffer = Left;
//            Right = bs.getBlock(Left).getLookup(Level, 1);
//            Left = bs.getBlock(Left).getLookup(Level, 0);
//            //System.out.println("L call");
//            return SearchByNameID(searchTarget, Buffer, Right, Left, Level, m);
//        }
//
//        return -1;
//
//    }
//
//
////    public int SearchByNameID2(String name, int startIndex)
////    {
////        //System.out.println("Search for name id starts from " + startIndex + " " + SkipGraph.Nodes.nodeSet[startIndex].nameID + " for " + name);
////        int Left = 0;
////        int Right = 0;
////        int level = 0;
////        int before = startIndex;
////
////
////        if (tg.mNodeSet.getNode(startIndex).nameID == name)  // UPDATED!!!!
////        {
////            return startIndex;
////        }
////
////        Left = getResolve(startIndex, 0, 0);
////        Right = getResolve(startIndex, 0, 1);
////        if (commonPrefixLength(tg.mNodeSet.getNode(startIndex).nameID, name) > level)
////        {//goes to upper levels for search
////            level = commonPrefixLength(tg.mNodeSet.getNode(startIndex).nameID, name);
////            Left = getResolve(startIndex, level, 0);
////            Right = getResolve(startIndex, level, 1);
////
////
////        }
////
////
////        while (true)
////        {
////            if (Left != -1)
////            {
////                if (tg.mNodeSet.getNode(Left).nameID == name)
////                {
////                    return Left;
////                }
////                else if (commonPrefixLength(tg.mNodeSet.getNode(Left).nameID, name) <= level)
////                {
////                    before = Left;
////                    Left = getResolve(Left, level, 0);
////                    tg.mNodeSet.addTime(Left, before);
////                }
////                else if (commonPrefixLength(tg.mNodeSet.getNode(Left).nameID, name) > level)
////                {
////                    level = commonPrefixLength(tg.mNodeSet.getNode(Left).nameID, name);
////                    before = Left;
////                    Right = getResolve(Left, level, 1);
////                    Left = getResolve(Left, level, 0);
////                    tg.mNodeSet.addMaxTime(Left, Right, before);
////                    //tg.mNodeSet.addTime(Right, before);
////                    continue;
////                }
////            }
////
////            else if (Right != -1)
////            {
////                if (tg.mNodeSet.getNode(Right).nameID == name)
////                {
////                    return Right;
////                }
////                else if (commonPrefixLength(tg.mNodeSet.getNode(Right).nameID, name) <= level)
////                {
////                    before = Right;
////                    Right = getResolve(Right, level, 1);
////                    tg.mNodeSet.addTime(Right, before);
////                }
////                else if (commonPrefixLength(tg.mNodeSet.getNode(Right).nameID, name) > level)
////                {
////                    level = commonPrefixLength(tg.mNodeSet.getNode(Right).nameID, name);
////                    before = Right;
////                    Right = getResolve(Right, level, 1);
////                    Left = getResolve(Right, level, 0);
////                    tg.mNodeSet.addMaxTime(Left, Right, before);
////                    //tg.mNodeSet.addTime(Left, before);
////                    //tg.mNodeSet.addTime(Right, before);
////                    continue;
////                }
////            }
////            if (Right == -1 && Left == -1)
////            {
////                break;
////            }
////        }
////
////
////        return -1;
////
////    }
//    //TODO: Same questions that we asked for SearchByNameID applies for this SearchByNumID method.
//
//    //TODO current time needs to be added to piggybacked Message
//    public int SearchByNumID(int targetNumId, int startIndex, Message m, int level, final String searchDirection)
//    {
//        if (level < 0)
//        {
//            System.err.println("BlockGraphOperations[old].java: search started with negative level");
//        }
//        //System.out.println("Search by num ID started, target " + targetNumId + " initiator " + startIndex);
//        //tg.mNodeSet.getNode(startIndex).printLookup();
//        //m.printSearchPath(tg.mNodeSet, false);
//
//        //Set<Block> ownedBlocks = SignatureLookupTable.signatureLookupTable.get(initiatorNode.getNumID());
//        Set<Integer> ownedBlocks = sgo.getTG().mNodeSet.getNode(startIndex).getTxSet();
//        if (ownedBlocks == null) {
//            System.out.println("owned blocks is null");
//        }
//        Block closestBlock = null;
//        int minDifference = Integer.MAX_VALUE;
//        for (int blockIndex : ownedBlocks){
//            int difference = Math.abs(targetNumId - bs.getBlock(blockIndex).getNumID());
//            if (minDifference > difference) {
//                minDifference = difference;
//                closestBlock = bs.getBlock(blockIndex);
//            }
//        }
//
//        int initiatorBlockIndex = closestBlock.getIndex();
//        m.piggybackBlock(initiatorBlockIndex, this, 0);
///*        Node initiator = getTG().mNodeSet.getNode(initiatorBlockIndex);//For debugging
//        if (system.getSimulationType().equalsIgnoreCase(Constants.SimulationType.DYNAMIC))
//        {
//            if (initiator.isOffline())
//            {
//                System.err.println("SkipGraphOperations.java: Offline Node is invoked search for NumID initiator");
//                System.exit(0);
//            }
////            else if (tg.mNodeSet.getNode(initiatorBlockIndex).isLookupTableEmpty())
////            {
////                return initiatorBlockIndex;
////            }
//        }*/
//
//
//        if (bs.getBlock(initiatorBlockIndex).getNumID() == targetNumId)
//        {
//            //System.out.println("SkipGraphOperations.java: target was found in starter, search for num ID returns " + initiatorBlockIndex);
//            //ChurnStochastics.updateAverageSuccessTimeOuts(1);
//            return bs.getBlock(initiatorBlockIndex).getOwnerIndex();
//        }
//
//        if (bs.getBlock(initiatorBlockIndex).getNumID() < targetNumId)
//        {
//            while (level >= 0)
//            {
//                //System.out.println("SkipGraphOperations.java: SearchByNumID level " + level);
//                int Right = bs.getBlock(initiatorBlockIndex).getLookup(level, 1);
//                /*if (system.getChurnType().equals(Constants.Churn.Type.ADVERSARIAL))
//                {
//                    try
//                    {
//                        Right = new AlgorithmInvoker().churnStabilization().resolveFailure(tg.mNodeSet, Right, 1, initiatorBlockIndex, level, targetNumId, m);
//                    } catch (NullPointerException ex)
//                    {
//                        //Right = tg.mNodeSet.getNode(initiatorBlockIndex).getLookup(level, 1);
//                    }
//                }*/
//
//                if (Right != -1 && bs.getBlock(Right).getNumID() <= targetNumId)
//                {
//                    //if (Right != -1)
//                    //{
//                    int scanForwardStatus = scanForward(m, initiatorBlockIndex, Right, targetNumId, level);
//                    if (scanForwardStatus == -1)
//                    {
//                        continue;
//                    } else
//                    {
//                        return scanForwardStatus;
//                    }
//                    //}
//
//                }
//
//                if (Right == -1 || (bs.getBlock(Right).getNumID() > targetNumId))
//                {
//                    level--;
//                }
//            }
//        } else
//        {
//            while (level >= 0)
//            {
//                //System.out.println("SkipGraphOperations.java: SearchByNumID level " + level);
//                int Left = bs.getBlock(initiatorBlockIndex).getLookup(level, 0);
//                /*if (system.getChurnType().equals(Constants.Churn.Type.ADVERSARIAL))
//                {
//                    try
//                    {
//                        Left = new AlgorithmInvoker().churnStabilization().resolveFailure(tg.mNodeSet, Left, 0, initiatorBlockIndex, level, targetNumId, m);
//                    } catch (NullPointerException ex)
//                    {
//                        //Left = tg.mNodeSet.getNode(initiatorBlockIndex).getLookup(level, 0);
//                    }
//
//                }*/
//                if (Left != -1 && bs.getBlock(Left).getNumID() >= targetNumId)
//                {
//                    if (Left != -1)
//                    {
//                        int scanForwardStatus = scanForward(m, initiatorBlockIndex, Left, targetNumId, level);
//                        if (scanForwardStatus == -1)
//                        {
//                            continue;
//                        } else
//                        {
//                            return scanForwardStatus;
//                        }
//                    }
//
//                }
//                if (Left == -1 || bs.getBlock(Left).getNumID() < targetNumId)
//                {
//                    level--;
//                }
//            }
//        }
//        //if (level < 0)
//        {
//            if (system.isLog())
//            {
//                System.out.println("Search stops at level " + level);
//            }
//            //System.out.println("SkipGraphOperations.java: Search for num ID returns " + initiatorBlockIndex);
//            //ChurnStochastics.updateAverageFailureTimeOuts(1);
////            if (system.getChurnStabilizationAlgorithm().equalsIgnoreCase(Constants.Churn.ChurnStabilizationAlgorithm.Tornado))
////            {
////
////                int tornadoIndex = new Tornado().rescueJump(initiatorBlockIndex, targetNumId, m, tg);
////                if (tornadoIndex >= 0)
////                    return SearchByNumID(targetNumId, tornadoIndex, m, system.getLookupTableSize()-1);
////                tornadoIndex = m.getBest(targetNumId, tg);
////                if (tornadoIndex >= 0)
////                {
//////                    if(tornadoIndex != initiatorBlockIndex)
//////                    {
//////                        System.out.println("protocol numID " + tg.mNodeSet.getNode(initiatorBlockIndex).getNumID() + " tornado numID " + tg.mNodeSet.getNode(tornadoIndex).getNumID() + " search target " + targetNumId);
//////                        m.printSearchPath(tg.mNodeSet, false);
//////                    }
////                    return tornadoIndex;
////
////                }
////            }
//            return bs.getBlock(initiatorBlockIndex).getOwnerIndex();
//        }
//    }
//    //TODO: This method is also related to SEARCH methods. What should we do here?
//    //TODO: neighbor is next Block in the same level of skip graph, it scans forward in same level
//    private int scanForward(Message m, int startIndex, int neighbor, int targetNumId, final int level)
//    {
//        if (m.contains(neighbor))
//        {
//            if (system.getSimulationType().equalsIgnoreCase(Constants.SimulationType.DYNAMIC))
//            {
//                /*if (system.getChurnType().equalsIgnoreCase(Constants.Churn.Type.ADVERSARIAL))
//                {
//                    return neighbor;
//                } else*/
//
//                System.err.println("SkipGraphOperations.java: cycle detected on search for num ID during non adversarial churn");
//                System.exit(0);
//
//            } else
//            {
//                System.err.println("SkipGraphOperations.java: cycle detected on search for num ID during non-dynamic simulation");
//                System.exit(0);
//            }
//
//        }
//        if (system.getSimulationType().equalsIgnoreCase(Constants.SimulationType.DYNAMIC) &&
//                bs.getBlock(neighbor).isOffline(sgo.getTG().mNodeSet))
//        {
//            if (system.getChurnType().equals(Constants.Churn.Type.COOPERATIVE))
//            {
//                //new ChurnStochastics().updateAverageResolveFailureTimeOuts();
//                return -1;
//            } else
//            {
//                return bs.getBlock(startIndex).getOwnerIndex();
//            }
//        }
//        //Node rightNode = getTG().mNodeSet.getNode(neighbor);//For debugging
//        bs.addTime(neighbor, startIndex, sgo.getTG().mNodeSet);
//        m.piggybackBlock(neighbor, this, 0);
//        //ChurnStochastics.updateAverageSuccessTimeOuts(1);
//        return SearchByNumID(targetNumId, neighbor, m, level, searchDirection);
//    }
////TODO needs to check the functionality with SearchForNameID2
////    public int getResolve(int dst, int i, int j)
////    {
////        if (dst == -1)
////        {
////            return -1;
////        }
////        if (tg.mNodeSet.getNode(dst).getLookup(i, j) == -1)
////        {
////            return -1;
////        }
////        else
////        {
////            if (tg.mNodeSet.getNode(tg.mNodeSet.getNode(dst).getLookup(i, j)).isDeactive() == false)
////            {
////                return (tg.mNodeSet.getNode(dst).getLookup(i, j));
////            }
////            else
////            {
////                if (system.isBackup())
////                {
////                    if (tg.mNodeSet.getNode(dst).getBackup(i, j) == -1)
////                    {
////                        return -1;
////                    }
////                    else
////                    {
////                        if (tg.mNodeSet.getNode(tg.mNodeSet.getNode(dst).getBackup(i, j)).isDeactive() == false)
////                        {
////                            return (tg.mNodeSet.getNode(dst).getBackup(i, j));
////                        }
////                        else
////                        {
////                            return -1;
////                        }
////                    }
////
////                }
////                else
////                {
////                    //RecoveryEvaluation.failTransCount++;
////                    return -1;
////                }
////            }
////        }
////
////    }
//
//    public blocks getBs() {
//        return bs;
//    }
//
//    public int randomlyPickOnline()
//    {
//        //System.out.println("Number of online Nodes " + mNodeSet.getNumberOfOnlineNodes());
//        int index = searchRandomGenerator.nextInt(system.getBlockCapacity() - 1);
//        while ((bs.getBlock(index)== null) || !bs.getBlock(index).isOnline(sgo.getTG().mNodeSet))
//        {
//            index = searchRandomGenerator.nextInt(system.getBlockCapacity() - 1);
//        }
//
//        return index;
//    }
//}
