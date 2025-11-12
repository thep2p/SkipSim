package SkipGraph;

import AvailabilityPrediction.LUDP;
import Blockchain.LightChain.Transaction;
import Blockchain.LightChain.Transactions;
import ChurnStabilization.ChurnStochastics;
import DataTypes.Constants;
import DataTypes.Message;
import DataTypes.Pair;
import Simulator.AlgorithmInvoker;
import Simulator.SkipSimParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SkipGraphOperations
{
    private static final Logger log = LoggerFactory.getLogger(SkipGraphOperations.class);

    public static final boolean IN_BLOCKCHAIN_SKIP_GRAPH = true;
    public static final boolean IN__NODES_SKIP_GRAPH = false;
    public static final int LEFT_SEARCH_DIRECTION = 0;
    public static final int RIGHT_SEARCH_DIRECTION = 1;
    //************************
    TopologyGenerator mTopologyGenerator;
    //Blocks mBlocks;
    Transactions mTransactions;
    private ArrayList<String> nameIDsOnPath;

//    public Blocks getBlocks()
//    {
//        return mBlocks;
//    }
    private Random searchRandomGenerator;
    /**
     * Denotes whether this Skip Graph Operation object holds an instance of a blockchain or a Skip Graph
     */
    private boolean isBlockchain;
    /**
     * Initiates a Skip Graph Operation object
     *
     * @param isBlockchain TRUE if this Skip Graph Operation is going to operate over the blockchain implemenetation
     *                     and hence does not need to have an instance of Topology Generator, and instead should be
     *                     having an instance of blocks, FALSE, otherwise.
     */
    public SkipGraphOperations(boolean isBlockchain)
    {
        this.isBlockchain = isBlockchain;
        searchRandomGenerator = new Random();
        if (isBlockchain)
        {
            //mBlocks = new Blocks();
            mTransactions = new Transactions();
        }


        mTopologyGenerator = new TopologyGenerator();
        System.gc(); //a call to system garbage collector
    }

    public Transactions getTransactions()
    {
        return mTransactions;
    }

    public Random getSearchRandomGenerator()
    {
        return searchRandomGenerator;
    }

    /**
     * Adds the input transaction to the transaction set and inserts it into the Skip Graph overlay of transactions.
     *
     * @param tx          the transaction to be added to the Transactions set as well as inserted in the Skip Graph overlay of the
     *                    transactions
     * @param currentTime the current time of the simulation
     * @param isNew       indicates whether the transaction has newly been generated, or is being re-inserted because of its owner's
     *                    arrival to the system. In the case that it is a re-insertion, the transaction does not need to be added to the
     *                    Transactions set.
     */
    public void addTXBtoLedger(Transaction tx, int currentTime, boolean isNew)
    {
        int index;
        /*
        if the transaction is new it is added to the
        transaction set
         */
        if (isNew)
            index = mTransactions.addToSet(tx);
        /*
        Otherwise, it is reloaded from the transaction set
         */
        else
            index = tx.getIndex();
        int ownerIndex = tx.getOwnerIndex();

        /*
        FIX: Insert the transaction into the Skip Graph BEFORE adding it to the owner's txSet.
        This prevents the search during insertion from encountering an uninitialized transaction
        in the owner's txSet, which would cause a NullPointerException when the search path
        tries to access the transaction before it's fully inserted.
         */
        if (index > 0)
        {
            insert(tx, mTransactions, index, true, currentTime);
        }

        /*
        Add to owner's txSet AFTER successful insertion into Skip Graph
         */
        ((Node) getTG().mNodeSet.getNode(ownerIndex)).addToTXSet(index);

    }

    /**
     * Inserts a node into the given skip-graph with the given index.
     * @param skipGraphNode the node to be inserted.
     * @param skipGraphNodes the skip-graph to be inserted into.
     * @param index the index of the node.
     * @param dynamicNameID
     * @param currentTime
     * @return
     */
    public int insert(SkipGraphNode skipGraphNode, SkipGraphNodes skipGraphNodes, int index, boolean dynamicNameID, int currentTime)
    {
        if (skipGraphNode instanceof Node)
        {
            ((Node) skipGraphNode).setIndex(index);
            mTopologyGenerator.mNodeSet.setNode(index, (Node) skipGraphNode);
        }
        if (SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.STATIC) && index == 0)
        {
            return ((Node) skipGraphNode).getIndex();
        }
        else if (dynamicNameID)
        {
            //print("Search has been started");
            int code = insert(skipGraphNode, currentTime, skipGraphNodes);

            /*
            Size of the lookuptable of the Node
             */
            int lookupTableSize = (skipGraphNode instanceof Node) ? SkipSimParameters.getLookupTableSize() : Transaction.LOOKUP_TABLE_SIZE;
            if (skipGraphNode.isLookupTableEmpty(lookupTableSize)
                    && code == Constants.SkipGraphOperation.Inserstion.NON_EMPTY_LOOKUP_TABLE)
            {
                System.err.println("SkipGraphOperation.java: Empty lookup table after insertion");
                System.exit(0);
            }
            //print("Search has been finished");
        }

        /*
        If the Node is a peer Node, all of its transactions are re-inserted in the Skip Graph to be available to all the Nodes
         */
        if (skipGraphNode instanceof Node && SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.BLOCKCHAIN))
        {
            HashSet<Integer> txSet = ((Node) mTopologyGenerator.mNodeSet.getNode(index)).getTxSet();
            for (int i : txSet)
            {
                Transaction tx = (Transaction) mTransactions.getNode(i);
                if (tx.isLookupTableEmpty(Transaction.LOOKUP_TABLE_SIZE))
                {
                   /*
                   If the lookup table of the transaction is empty, it is inserted into the
                   Skip Graph, note that we put false for the isNew field of the addTXBtoLedger, so
                   that to prevent an existing transaction being put again in the transaction set.
                    */
                    addTXBtoLedger(tx, currentTime, false);
                }
            }
        }

        //n.printLookup();
        return skipGraphNode.getIndex();
    }

    public int renewInsertion(int index, boolean dynamicNameID, int currentTime, SkipGraphNodes skipGraphNodes)
    {
        mTopologyGenerator.mNodeSet.getNode(index).setIndex(index);

        if (index > 0 && dynamicNameID)
        {
            insert(mTopologyGenerator.mNodeSet.getNode(index), currentTime, skipGraphNodes);
        }


        //mTopologyGenerator.mNodeSet.getNode(index).printLookup();
        return mTopologyGenerator.mNodeSet.getNode(index).getIndex();
    }

//    /**
//     * Only should be in static simulation or the STATIC_SIMULATION_TIME should be corrected
//     *
//     * @param mNodeSet
//     * @param isBucketBased
//     */
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
//                }
//                else
//                {
//                    counter++;
//                }
//
//
//                /*
//                 * Cleaning the DataTypes.Message for piggybacking
//                 */
//                Message m = new Message();
//
//                /*
//                 * type of lookup: 0 = search by name id, 1 = search by numerical id
//                 */
//                int searchType = random.nextInt() % 2;
//                if (searchType == 0)
//                {
//                    SearchByNameID(mNodeSet.getNode(j).getNameID(), i, mNodeSet.getNode(i).getLookup(0, 1), mNodeSet.getNode(i).getLookup(0, 0), 0, m, );
//                }
//                else
//                {
//                    SearchByNumID(mNodeSet.getNode(j).getNumID(), i, m, system.getLookupTableSize() - 1, Constants.SkipGraphOperation.STATIC_SIMULATION_TIME);
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

    /**
     * @param random the search random generator, use the getter of searchRandomGenerator of the same class
     * @return -1 if could not perform the search due to the low number of online Nodes, otherwise the success search ratio
     */
    public double randomLookup(Random random, Nodes ns)
    {
        if (mTopologyGenerator.mNodeSet.getNumberOfOnlineNodes() < 0.01 * SkipSimParameters.getSystemCapacity())
        {
            return -1;
        }
        int numOfActiveNodes = mTopologyGenerator.mNodeSet.getNumberOfOnlineNodes();
        int iterations = random.nextInt(numOfActiveNodes * (numOfActiveNodes - 1) / 2) + 1;
//  	     while(iterations < numOfActiveNodes / 2)
//  	    	iterations = random.nextInt(numOfActiveNodes);
        ChurnStochastics.updateAverageLookups(iterations);

        //Commented for the sake of time
           /*System.out.println("At " + Simulator.system.getCurrentTimeInHourMinFormat() + " " + iterations + " random iterations initiated "
           + " Number of active SkipGraph.Nodes " + SkipGraph.Nodes.offlineNodesCounter());
  	     */
        double counter = 0;
        Set<Pair> searchesSet = new HashSet<>();
        for (int i = 0; i < iterations; i++)
        {
            int searchTarget = mTopologyGenerator.randomlyPickOnline();
            /*
             * picking search searchTarget
             */

            ///while (mTopologyGenerator.mNodeSet.getNode(searchTarget).isOffline()) searchTarget = random.nextInt(system.getSystemCapacity() - 1);
            /*
             * picking search initiator
             */

            int initiator = mTopologyGenerator.randomlyPickOnline();
            Pair search = new Pair(initiator, searchTarget);
            while (initiator == searchTarget || searchesSet.contains(search))
            {
                initiator = mTopologyGenerator.randomlyPickOnline();
                search = new Pair(initiator, searchTarget);
            }
            searchesSet.add(search);


            /*
             * Cleaning the DataTypes.Message for piggybacking
             */
            Message m = new Message();

            /*
             * type of lookup: 0 = search by name id, 1 = search by numerical id
             */
            //TODO make search by name ID churn resilient
            //boolean searchType = random.nextBoolean();
            //if (searchType)
            //{
            //    SearchByNameID(mTopologyGenerator.mNodeSet.getNode(searchTarget).getNameID(), initiator, mTopologyGenerator.mNodeSet.getNode(initiator).getLookup(0, 1), mTopologyGenerator.mNodeSet.getNode(initiator).getLookup(0, 0), 0, m);
            //}
            //else
            //{
            int searchDirection = (((Node) mTopologyGenerator.mNodeSet.getNode(initiator)).getNumID() < mTopologyGenerator.mNodeSet.getNode(searchTarget).getNumID())
                    ? RIGHT_SEARCH_DIRECTION : LEFT_SEARCH_DIRECTION;
            int searchResult = SearchByNumID(mTopologyGenerator.mNodeSet.getNode(searchTarget).getNumID()
                    , (Node) mTopologyGenerator.mNodeSet.getNode(initiator), m, SkipSimParameters.getLookupTableSize() - 1,
                    Constants.SkipGraphOperation.STATIC_SIMULATION_TIME, ns, searchDirection);
            if (mTopologyGenerator.mNodeSet.getNode(searchResult).getNumID() == mTopologyGenerator.mNodeSet.getNode(searchTarget).getNumID())
            {
                counter++;
                ChurnStochastics.updateAverageSuccessTimeOuts();
            }
            else
            {
                ChurnStochastics.updateAverageFailureTimeOuts();
//                System.out.println("------------------------------------------------------------");
//                System.out.println("Search for " + mTopologyGenerator.mNodeSet.getNode(searchTarget).getNumID() + " index " + searchTarget + " from " + initiator + " failed " );
//                System.out.println("The result is " + searchResult + " with num ID of " +  mTopologyGenerator.mNodeSet.getNode(searchResult).getNumID());
//                m.printSearchPath(mTopologyGenerator.mNodeSet, false);
//                //mTopologyGenerator.mNodeSet.getNode(searchResult).printBucket();
//                mTopologyGenerator.mNodeSet.printLookupNumID(searchResult);
//                mTopologyGenerator.mNodeSet.printLookupOnlineStatus(searchResult);
//                System.out.println("------------------------------------------------------------");
            }
            //}
        }


        if (iterations < 0)
        {
            System.err.println("SkipGraphOperations.java: All pairs random Lookup failed");
            System.exit(0);
        }

        return (double) counter / iterations;

    }

    /**
     * Given two strings s1 and s2 that are supposedly name IDs, it returns their length of common prefix
     *
     * @param s1 first string (name ID)
     * @param s2 second string (name ID)
     * @return length of common prefix
     */
    public int commonPrefixLength(String s1, String s2)
    {

        int length = 0;
        if (s1.isEmpty() || s2.isEmpty())
        {
            return 0;
        }
        while (s1.charAt(length) == s2.charAt(length))
        {
            length++;
            //if (k >= s1.length() || k >= s2.length() || k >= system.getLookupTableSize() - 1)
            if (length >= s1.length() || length >= s2.length())
            {
                break;
            }
        }

        return length;

    }

    /**
     * Inserts a node into the given skip-graph.
     * @param skipGraphNode the node to be inserted.
     * @param currentTime insertion time.
     * @param nodeSet the skip-graph to be inserted into.
     * @return the insertion status.
     */
    public int insert(SkipGraphNode skipGraphNode, int currentTime, SkipGraphNodes nodeSet)
    {

        //System.out.println("Insert started!");
        int Left = -1;
        int Right = -1;

        /*
        Assigning the introducer for the Dynamic or Blockchain simulations
         */
        if (SkipSimParameters.getSimulationType().equals(Constants.SimulationType.DYNAMIC)
                || SkipSimParameters.getSimulationType().equals(Constants.SimulationType.BLOCKCHAIN))
        {
            int counter = 0;
            while (true)
            {
                /*
                For a Node
                 */
                if (skipGraphNode instanceof Node)
                {
                    if (counter >= SkipSimParameters.getSystemCapacity())
                        //All Nodes were tired and no one was a proper introducer, hence an error is returned
                        return Constants.SkipGraphOperation.Inserstion.NO_INTRODUCER_FOUND;
                    else if (((Node) getTG().mNodeSet.getNode(counter)).isOnline() && counter != skipGraphNode.index)
                    {
                        //An online introducer is found that is distinct from the Node itself
                        //skipGraphNode.setIntroducer(counter);
                        skipGraphNode.setIntroducer(counter);
                        break;
                    }
                }
                /*
                For a transaction
                 */
                else if (skipGraphNode instanceof Transaction)
                {
                    if (counter >= mTransactions.getCurrentBlockSetIndex())
                    {
                        //All transactions were tried and no one was a proper introducer, hence an error is returned
                        return Constants.SkipGraphOperation.Inserstion.NO_INTRODUCER_FOUND;
                    }
                    else
                    {
                        int ownerIndex = ((Transaction) mTransactions.getNode(counter)).getOwnerIndex();

                        /*
                        If the owner of the transaction is online, AND
                            the candidate transaction's introducer (counter) itself has already been inserted into the
                                OR candidate transaction's introducer is the one with index 0 and hence may not been inserted yet in the Skip Graph
                                (i.e., remember that the first Node is not inserted in the Skip Graph since there is no other Node at the time of its
                                arrival)
                                Then the candidate is designated as the introducer of the transaction on the blockchain overlay
                         */
                        if (((Node) getTG().mNodeSet.getNode(ownerIndex)).isOnline()
                                && (!mTransactions.getNode(counter).isLookupTableEmpty(Transaction.LOOKUP_TABLE_SIZE) || counter == 0))
                        {
                            skipGraphNode.setIntroducer(ownerIndex);
                            break;
//                        /*
//                        Looking for an introducer with greater numerical ID than the Node
//                         */
//                            int rightIntroducer = ((Node) getTG().mNodeSet.getNode(ownerIndex)).mostSimilarTXB(mTransactions, skipGraphNode.getNumID(), new Message(), RIGHT_SEARCH_DIRECTION);
//                            if (rightIntroducer != -1)
//                            {
//                                break;
//                            }
//
//                        /*
//                        Looking for an introducer with lower numerical ID than the Node
//                         */
//                            int leftIntroducer = ((Node) getTG().mNodeSet.getNode(ownerIndex)).mostSimilarTXB(mTransactions, skipGraphNode.getNumID(), new Message(), LEFT_SEARCH_DIRECTION);
//                            if (leftIntroducer != -1)
//                            {
//                                break;
//                            }
                        }
                    }
                }
//                /*
//                For a block
//                 */
//                else if (skipGraphNode instanceof Block)
//                {
//                    if (counter >= mBlocks.getCurrentBlockSetIndex())
//                    {
//                        //All transactions were tired and no one was a proper introducer, hence an error is returned
//                        return Constants.SkipGraphOperation.Inserstion.NO_INTRODUCER_FOUND;
//                    }
//                    else if (((Block) skipGraphNode).numberOfOnlineReplicaHolders(this.getTG().mNodeSet) > 0
//                            && counter != skipGraphNode.index)
//                    {
//                        //skipGraphNode.setIntroducer(counter);
//                        break;
//                    }
//                }
                counter++;

            }
        }

        Message m = new Message();
        int searchResult;
        int searchDirection;
        if (skipGraphNode instanceof Node)
        {
            searchDirection = (mTopologyGenerator.mNodeSet.getNode(skipGraphNode.getIntroducer()).getNumID() < skipGraphNode.getNumID())
                    ? RIGHT_SEARCH_DIRECTION : LEFT_SEARCH_DIRECTION;
            /*
            Search for the to be inserted Node in the Node-based Skip Graph overlay since skipGraphNode is an object of the
            Nodes
             */
            searchResult = SearchByNumID(skipGraphNode.getNumID(),
                    (Node) mTopologyGenerator.mNodeSet.getNode(skipGraphNode.getIntroducer()),
                    new Message(),
                    SkipSimParameters.getLookupTableSize() - 1,
                    currentTime,
                    getTG().mNodeSet, searchDirection);
        }
        else
        {
            Node introducer = (Node) mTopologyGenerator.mNodeSet.getNode(skipGraphNode.getIntroducer());
            int closestNodeIndex = introducer
                    .mostSimilarTXB(mTransactions, skipGraphNode.getNumID(), new Message(), RIGHT_SEARCH_DIRECTION, 0);
            if (closestNodeIndex != -1)
            {
                /*
                There exists a successor for the block or transaction in the introducer Node (smaller numId than what we want to insert)
                NOTE: successor needs to search rightward to find the Node, hence, the search is going to be directed on RIGHT
                 */
                searchDirection = RIGHT_SEARCH_DIRECTION;
            }
            else
            {
                closestNodeIndex = introducer
                        .mostSimilarTXB(mTransactions, skipGraphNode.getNumID(), new Message(), LEFT_SEARCH_DIRECTION, 0);
                if (closestNodeIndex != -1)
                {
                /*
                There exists a predecessor for the block or transaction in the introducer Node (bigger numId than what we want to insert)
                NOTE: predecessor needs to search leftward to find the Node, hence, the search is going to be directed on LEFT
                 */
                    searchDirection = LEFT_SEARCH_DIRECTION;
                }
                else
                {
                    System.err.println("SkipGraphOperations.java/There is no predecessor or successor for the block or transaction on the blockchain");
                    searchDirection = -2; //Just to resolve a compilation error
                    System.exit(0);
                }
            }
            /*
            Search for the to be inserted Node in the block chain overlay since skipGraphNode is either
            a transaction, or a block
            searchResult: Address of the Node that owns the most similar Node/transaction to the search target transaction/block
             */
            searchResult = SearchByNumID(skipGraphNode.getNumID(),
                    introducer,
                    new Message(),
                    SkipSimParameters.getLookupTableSize() - 1,
                    currentTime,
                    mTransactions,
                    searchDirection);
        }


        //m.printSearchPath(mTopologyGenerator.mNodeSet,false);

        //if(system.isLog())
        //System.out.println("The search result for insertion of " + mTopologyGenerator.mNodeSet.getNode(index).getNumID() + " num ID is " + mTopologyGenerator.mNodeSet.getNode(searchResult).getNumID() +" num ID" );

        /*
        Determining the predecessor and successor of the Node on the Skip Graph Overlay
         */
        int predecessor, successor;

        /*
        If the search is conducted over the blockchain ledger, then the searchResult, which is the owner of the most similar
        numerical ID transaction/block to the skipGraphNode, should be replaced with the most similar transaction/block.
         */
        if (!(skipGraphNode instanceof Node))
        {
            searchResult = ((Node) getTG().mNodeSet.getNode(searchResult)).mostSimilarTXB(nodeSet, skipGraphNode.getNumID(), m, searchDirection, 0);
        }
        if (skipGraphNode.getNumID() < nodeSet.getNode(searchResult).getNumID())
        {
            predecessor = nodeSet.getNode(searchResult).getLookup(0, 0);
            successor = searchResult;
            //System.err.println("The predecessor greater found less than the Node to be inserted");
        }
        else
        {
            predecessor = searchResult;
            successor = nodeSet.getNode(searchResult).getLookup(0, 1);
        }

        int insertStatus = adaptiveInsert(skipGraphNode, predecessor, successor, nodeSet);
        //Node theNodeForDebug = mTopologyGenerator.mNodeSet.getNode(index);
        if (!lookupTableValidation(skipGraphNode, nodeSet)
                && mTopologyGenerator.mNodeSet.getNumberOfOnlineNodes() > 1
                && SkipSimParameters.getChurnType().equalsIgnoreCase(Constants.Churn.Type.COOPERATIVE))
        {
            System.err.println("SkipGraphOperations.java: lookup table violation for Node: ");
            System.err.println(skipGraphNode.toString());
            System.exit(0);
        }
//        System.out.println("---------------------------------------------------------");
//        System.out.println("After Insertion: ");
//        if(predecessor != -1)
//        {
//            System.out.println("Predecessor: " + mTopologyGenerator.mNodeSet.getNode(predecessor).getNameID() + " " + mTopologyGenerator.mNodeSet.getNode(predecessor).getNumID());
//            mTopologyGenerator.mNodeSet.getNode(predecessor).printLookup();
//        }
//        System.out.println("Node: "+ mTopologyGenerator.mNodeSet.getNode(index).getNameID() + " " + mTopologyGenerator.mNodeSet.getNode(index).getNumID());
//        mTopologyGenerator.mNodeSet.getNode(index).printLookup();
//        if(successor != -1)
//        {
//            System.out.println("Successor: " + mTopologyGenerator.mNodeSet.getNode(successor).getNameID() + " " + mTopologyGenerator.mNodeSet.getNode(successor).getNumID());
//            mTopologyGenerator.mNodeSet.getNode(successor).printLookup();
//        }
//        System.out.println("---------------------------------------------------------");
        return insertStatus;
    }

    //TODO can be simplified

    /**
     * Used as a subroutine by insertion methods. This method is used to insert a node into the levels
     * of a skip-graph according to its name id. At each level i, a node should have neighbors that have
     * common prefix length of at least i. This method makes sure that this is satisfied.
     * @param skipGraphNode the node to be inserted.
     * @param Left left neighbor of the node on the lowest level.
     * @param Right right neighbor of the node on the lowest level.
     * @param nodeSet the skip-graph to be inserted into.
     * @return the insertion status.
     */
    private int adaptiveInsert(SkipGraphNode skipGraphNode, int Left, int Right, SkipGraphNodes nodeSet)
    {
        /*
        Size of the lookup table of the Node
         */
        boolean isNodeInsertion = (skipGraphNode instanceof Node);
        int lookupTableSize = isNodeInsertion ? SkipSimParameters.getLookupTableSize() : Transaction.LOOKUP_TABLE_SIZE;

        /*
        Only is used to check the existence of loops in dynamic simulation adversarial churn
         */
        ArrayList<Integer> visitedRightNodes = new ArrayList<>();
        ArrayList<Integer> visitedLeftNodes = new ArrayList<>();


        skipGraphNode.setLookup(0, 0, Left);
        if (Left != -1)
        {
            nodeSet.getNode(Left).setLookup(0, 1, skipGraphNode.getIndex());
            visitedLeftNodes.add(Left);
        }
        skipGraphNode.setLookup(0, 1, Right);
        if (Right != -1)
        {
            nodeSet.getNode(Right).setLookup(0, 0, skipGraphNode.getIndex());
            visitedRightNodes.add(Right);
        }


        int level = 0;
        while (level < lookupTableSize - 1)
        {
            //System.out.println("SkipGraphOperations.java: adaptive insert inner loop, Right " + Right + " Left " + Left);
            // Finding left and right nodes with appropriate common prefix length...
            while (Left != -1 && commonPrefixLength(nodeSet.getNode(Left).getNameID(), skipGraphNode.getNameID()) <= level)
            {
                int old = Left;
                Left = nodeSet.getNode(Left).getLookup(level, 0);
                //System.out.println("SkipGraphOperations.java: insertion inner loop, left was switched to " + Left );
                //mTopologyGenerator.mNodeSet.getNode(index).printLookup();
                if (visitedLeftNodes.contains(Left) || (Left != -1 && isNodeInsertion && ((Node) nodeSet.getNode(Left)).isOffline()))
                //Cycle checking in dynamic adversarial churn or offline neighbor
                {
                    if (SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.DYNAMIC))
                    {
                        if (SkipSimParameters.getChurnType().equalsIgnoreCase(Constants.Churn.Type.ADVERSARIAL))
                        {
                            Left = -1;
                            break;
                        }
                        else
                        {
                            System.err.println("SkipGraphOperations.java: cycle detected on visited left during non adversarial churn insertion");
                            System.exit(0);
                        }
                    }
                    else
                    {
                        //System.err.println("SkipGraphOperations.java: cycle detected on visited lefts during non-dynamic simulation insertion");
                        //System.exit(0);
                    }
                }
                else
                {
                    if (Left != -1)
                    {
                        visitedLeftNodes.add(Left);
                    }
                }
            }

            while (Left == -1 && Right != -1
                    && commonPrefixLength(nodeSet.getNode(Right).getNameID(), skipGraphNode.getNameID()) <= level)
            {
                int old = Right;
                Right = nodeSet.getNode(Right).getLookup(level, 1);
                //System.out.println("SkipGraphOperations.java: insertion inner loop, right was switched to " + Right );
                //mTopologyGenerator.mNodeSet.getNode(index).printLookup();
                if (visitedRightNodes.contains(Right) || (Right != -1 && isNodeInsertion && ((Node) nodeSet.getNode(Right)).isOffline()))
                {
                    if (SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.DYNAMIC))
                    {
                        if (SkipSimParameters.getChurnType().equalsIgnoreCase(Constants.Churn.Type.ADVERSARIAL))
                        {
                            Right = -1;
                            break;
                        }
                        else
                        {
                            System.err.println("SkipGraphOperations.java: cycle detected on visited right during non adversarial churn insertion");
                            System.exit(0);
                        }
                    }
                    else
                    {
                        System.err.println("SkipGraphOperations.java: cycle detected on visited right during non-dynamic simulation insertion");
                        System.exit(0);
                    }
                }
                else
                {
                    if (Right != -1)
                    {
                        visitedRightNodes.add(Right);
                    }
                }
            }
            // Climbing up...
            if (Left != -1)
            {
                /*
                level < lookupTableSize is happens only in blockchain case where two block/transaction may arrive at the same name ID and hence
                their common prefix length is equal to the lookupTableSize, in this situation, the check on the RightNeighbor at higher level
                results in ArrayIndexOutOfBoundException
                 */
                if (commonPrefixLength(nodeSet.getNode(Left).getNameID(), skipGraphNode.getNameID()) > level)
                {
                    if (level < lookupTableSize - 2)
                    {
                        int RightNeighbor = nodeSet.getNode(Left).getLookup(level + 1, 1);
                        nodeSet.getNode(Left).setLookup(level + 1, 1, skipGraphNode.getIndex());
                        if (RightNeighbor != -1)
                        {
                            nodeSet.getNode(RightNeighbor).setLookup(level + 1, 0, skipGraphNode.getIndex());
                        }

                        //if((level != Simulator.system.getLookupTableSize() - 1) || mTopologyGenerator.mNodeSet.getNode(index).getLookup(level, 1) == -1)
                        {
                            // Insert the node between left and right neighbor at the upper level.
                            skipGraphNode.setLookup(level + 1, 0, Left);
                            skipGraphNode.setLookup(level + 1, 1, RightNeighbor);
                            Right = RightNeighbor;
                        }
                    }
                    level++; //Has to add to DS version
                }

            }
            else if (Right != -1)
            {
                /*
                level < lookupTableSize is happens only in blockchain case where two block/transaction may arrive at the same name ID and hence
                their common prefix length is equal to the lookupTableSize, in this situation, the check on the LeftNeighbor at higher level
                results in ArrayIndexOutOfBoundException
                 */
                if (commonPrefixLength(nodeSet.getNode(Right).getNameID(), skipGraphNode.getNameID()) > level)
                {
                    if (level < lookupTableSize - 2)
                    {
                        int LeftNeighbor = nodeSet.getNode(Right).getLookup(level + 1, 0);
                        nodeSet.getNode(Right).setLookup(level + 1, 0, skipGraphNode.getIndex());
                        if (LeftNeighbor != -1)
                        {
                            nodeSet.getNode(LeftNeighbor).setLookup(level + 1, 1, skipGraphNode.getIndex());
                        }

                        //if((level != Simulator.system.getLookupTableSize() - 1) || mTopologyGenerator.mNodeSet.getNode(index).getLookup(level, 0) == -1)
                        {
                            skipGraphNode.setLookup(level + 1, 0, LeftNeighbor);
                            skipGraphNode.setLookup(level + 1, 1, Right);
                            Left = LeftNeighbor;
                        }
                    }
                    level++; //Has to add to the DS version
                }
            } else {
                break;
            }
            //level++ has to be removed from DS version
        }

        if (skipGraphNode.isLookupTableEmpty(lookupTableSize))
        {
            if (SkipSimParameters.getChurnType().equalsIgnoreCase(Constants.Churn.Type.ADVERSARIAL))
            {
                return Constants.SkipGraphOperation.Inserstion.EMPTY_LOOKUP_TABLE;
            }
            else
            {
                System.err.println("SkipGraphOperations.java: empty lookup table in cooperative churn is detected");
                System.exit(0);
            }
        }
        return Constants.SkipGraphOperation.Inserstion.NON_EMPTY_LOOKUP_TABLE;
    }

    /**
     * @param node the SkipGraphNode object that just has been inserted into the SkipGraph
     * @return checks the preservation of logical order among between the Node and its neighbors, returns ture if
     * it is satisfied for all negihbors, and returns false otherwise.
     */
    private boolean lookupTableValidation(SkipGraphNode node, SkipGraphNodes nodeSet)
    {
        if(node.getIndex() > 0 && node.getLookup(0, 0) == -1 && node.getLookup(0, 1) == -1) {
            return false;
        }
        int nonZeroEnteries = 0;
        int numID = node.getNumID();
        for (int i = 0; i < SkipSimParameters.getLookupTableSize(); i++)
        {
            int rightNeighbor = node.getLookup(i, 1);
            if (rightNeighbor == node.index) return false; //Avoiding self loop
            if (rightNeighbor != -1)
                nonZeroEnteries++;
            if (rightNeighbor != -1 && numID > nodeSet.getNode(rightNeighbor).getNumID())
            {
                SkipGraphNode rightNeighborToDebug = nodeSet.getNode(rightNeighbor);
                return false;
            }
            int leftNeighbor = node.getLookup(i, 0);
            if (leftNeighbor == node.index) return false; //Avoiding self loop
            if (leftNeighbor != -1)
                nonZeroEnteries++;
            if (leftNeighbor != -1 && numID < nodeSet.getNode(leftNeighbor).getNumID())
            {
                SkipGraphNode leftNeighborToDebug = nodeSet.getNode(leftNeighbor);
                return false;
            }
        }
        if (nonZeroEnteries == 0)
        {
            return false;
        }
        return true;
    }

    /**
     * This function is currently only invoked by LANS (name ID assignment protocol) and returns the
     *
     * @param searchTarget the name ID under search
     * @return search path in the case of static simulation, and an empty ArrayList in dynamic simulation
     */
    public ArrayList<String> SearchForNameIDPath(String searchTarget)
    {
        //TODO introducer index should be received as an input argument
        int INTRODUCER_INDEX = 0;
        nameIDsOnPath = new ArrayList<>();
        if (SkipSimParameters.getSimulationType().equals(Constants.SimulationType.STATIC))
        {
            ArrayList<Integer> searchResultSet = new ArrayList<>();
            int result = SearchByNameID(searchTarget,
                    (Node) getTG().mNodeSet.getNode(INTRODUCER_INDEX),
                    getTG().mNodeSet,
                    mTopologyGenerator.mNodeSet.getNode(0).getLookup(0, 1),
                    mTopologyGenerator.mNodeSet.getNode(0).getLookup(0, 0),
                    0,
                    new Message(),
                    searchResultSet);
        }
        else
        {
            for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
            {
                if (((Node) mTopologyGenerator.mNodeSet.getNode(i)).isOnline() && mTopologyGenerator.mNodeSet.getNode(i).getNameID().equals(searchTarget))
                {
                    nameIDsOnPath.add(searchTarget);
                }
            }
        }

        return nameIDsOnPath;

    }

    private void piggyBackLookupTable(int index)
    {
        if (index == -1)
        {
            return;
        }
        nameIDsOnPath.add(mTopologyGenerator.mNodeSet.getNode(index).getNameID());
        for (int i = 0; i < SkipSimParameters.getLookupTableSize(); i++)
        {
            for (int j = 0; j < 2; j++)
            {
                int neighborAddress = mTopologyGenerator.mNodeSet.getNode(index).getLookup(i, j);
                if (neighborAddress == -1)
                {
                    continue;
                }
                String neighborNameID = mTopologyGenerator.mNodeSet.getNode(neighborAddress).getNameID();
                if (!neighborNameID.isEmpty() && !nameIDsOnPath.contains(neighborNameID))
                {
                    nameIDsOnPath.add(neighborNameID);
                }
            }
        }
    }

    /**
     * Performs a search in the given skip-graph with respect to the name id.
     * @param searchTarget the target name id that needs to be searched.
     * @param currentNode the node that the search is initiated from.
     * @param nodeSet the set of nodes that the search needs to be performed on.
     * @param Right the right neighbor of the initiator node at the current level.
     * @param Left the left neighbor of the initiator node at the current level.
     * @param Level the level at which the search needs to be started.
     * @param m the piggyback message.
     * @param resultSet the list of name ids that have been traversed before reaching to the result.
     * @return the node that has the desired name id, -1 if it doesn't exist.
     */
    public int SearchByNameID(String searchTarget, Node currentNode, SkipGraphNodes nodeSet, int Right, int Left, int Level, Message m, ArrayList<Integer> resultSet)
    {
        nameIDsOnPath = new ArrayList<>();

        piggyBackLookupTable(currentNode.getIndex());
        m.piggyback(currentNode.getIndex(), mTopologyGenerator.mNodeSet);

        /*
        Search is conducted within the Skip Graph of Nodes
         */

        if (Left != -1 && ((Node) mTopologyGenerator.mNodeSet.getNode(Left)).isOnline())
        {
            m.piggyback(Left, mTopologyGenerator.mNodeSet);
        }
        if (Right != -1 && ((Node) mTopologyGenerator.mNodeSet.getNode(Right)).isOnline())
        {
            m.piggyback(Right, mTopologyGenerator.mNodeSet);
        }
        if (nodeSet instanceof Nodes)
        {
            if (currentNode.getNameID().equals(searchTarget))
            {
                return currentNode.getIndex();
            }
        }
        else
        {

            int mostSimilarTXBIndex = currentNode.mostSimilarTXB(nodeSet, searchTarget, m, Level);
            /*
            If most similarTXBIndex is a valid index
             */
            if (mostSimilarTXBIndex != -1)
            {
                String mostSimilarNameID = nodeSet.getNode(mostSimilarTXBIndex).getNameID();
            /*
            The search is conducted within the blockchain over the blocks and transactions
            and the currentNode holds a block or transaction with the searchTarget
             */
                if (mostSimilarNameID.equals(searchTarget))
                {
                    resultSet.add(currentNode.getIndex());
                }
            }
        }


        /*
        The most similar Node to the search target, which is initialized to the current Node on the search path
         */
        Node Buffer = currentNode;

        /*
        Upon a jump, jumpDirection determines wheter the jump caused by the left or right neighbor
         */
        int jumpDirection = -1;
        int jumpLevel = -1;
        while (true)
        {
            int leftCheck = checkNeighborNameID(Left, searchTarget, m, Level, nodeSet, resultSet);
            /*
            Check the left neighbor for a match with the search target
             */
            if (leftCheck >= 0)
            {
                /*
                if the search is conducted on the Skip Graph of Nodes, and a match is found
                 */
                return leftCheck;
            }
            /*
            Check the right neighbor for a match with search target
             */
            int rightCheck = checkNeighborNameID(Right, searchTarget, m, Level, nodeSet, resultSet);
            if (rightCheck >= 0)
            {
                /*
                if the search is conducted on the Skip Graph of Nodes, and a match is found
                 */
                return rightCheck;
            }

            /*
            check the right neighbor of the right Node
             */
            if (Left != -1)
            {
                int leftOfLeft = moveForward(Left, LEFT_SEARCH_DIRECTION, Level, searchTarget, m, nodeSet);
                Buffer = (Node) mTopologyGenerator.mNodeSet.getNode(Left);
                Left = leftOfLeft;
            }
            /*
            check the right neighbor of the right Node
             */
            if (Right != -1)
            {
                int rightOfRight = moveForward(Right, RIGHT_SEARCH_DIRECTION, Level, searchTarget, m, nodeSet);
                Buffer = (Node) mTopologyGenerator.mNodeSet.getNode(Right);
                Right = rightOfRight;
            }


            if (Left != -1)
            {
                jumpLevel = jumpCheck(Left, searchTarget, Level, m, nodeSet);

                if (jumpLevel > 0)
                {
                    jumpDirection = LEFT_SEARCH_DIRECTION;
                    break;
                }
            }

            if (Right != -1)
            {
                jumpLevel = jumpCheck(Right, searchTarget, Level, m, nodeSet);
                if (jumpLevel > 0)
                {
                    jumpDirection = RIGHT_SEARCH_DIRECTION;
                    break;
                }
            }


            if ((Right == -1 && Left == -1)
                    || ((Right != -1 && Left != -1) && (((Node) mTopologyGenerator.mNodeSet.getNode(Left)).isOffline()
                    && ((Node) mTopologyGenerator.mNodeSet.getNode(Right)).isOffline())))
            {
                if (nodeSet instanceof Nodes)
                {
                    return Buffer.getIndex();
                }
                else
                {
                    return -1;
                }
            }


        }

        /*
        When the search reaches here, it has found a neighbor at upper level with a more similar name ID i.e., greater than
        the current level of the search.
         */
        int indexNeighborAtUpperLevel = -1;
        if (Right != -1 && jumpLevel > 0 && jumpDirection == RIGHT_SEARCH_DIRECTION)
        {
            indexNeighborAtUpperLevel = Right;
        }
        else if (Left != -1 && jumpLevel > 0 && jumpDirection == LEFT_SEARCH_DIRECTION)
        {
            indexNeighborAtUpperLevel = Left;
        }

        if (indexNeighborAtUpperLevel != -1)
        {
            Level = jumpLevel;
            Buffer = (Node) mTopologyGenerator.mNodeSet.getNode(indexNeighborAtUpperLevel);

            /*
            IF AN EXCEPTION HAPPENS HERE because of indexNeighborAtUpperLevel = -1, it means that the break from the
            upper while(true) was not valid.
             */
            Node neighborAtUpperLevel = (Node) mTopologyGenerator.mNodeSet.getNode(indexNeighborAtUpperLevel);

            /*
            Initializing left and right at upper level if search is over the Nodes
             */
            if (nodeSet instanceof Nodes)
            {
                Left = neighborAtUpperLevel.getLookup(Level, 0);
                Right = neighborAtUpperLevel.getLookup(Level, 1);
                //System.out.println("R call");
            }
            /*
            Initializing left and right at upper level if searching within the blockchain
             */
            else
            {
                /*
                Finding the most similar transaction to the right neighbor
                 */
                //Index
                int mostSimilarTXBIndex = neighborAtUpperLevel.mostSimilarTXB(nodeSet, searchTarget, m, Level);
                /*
                The transaction itself
                 */
                Transaction mostSimilatTXB = (Transaction) nodeSet.getNode(mostSimilarTXBIndex);
                /*
                Finding the left and right neighbor of the transaction in the search level
                 */
                //Left and Right Indices
                int leftTransactionNeighbor = mostSimilatTXB.getLookup(Level, 0);
                int rightTransactionNeighbor = mostSimilatTXB.getLookup(Level, 1);
                //The owner of those transactions
                if (leftTransactionNeighbor != -1)
                {
                    Left = ((Transaction) nodeSet.getNode(leftTransactionNeighbor)).getOwnerIndex();
                }
                else
                {
                    Left = -1;
                }
                if (rightTransactionNeighbor != -1)
                {
                    Right = ((Transaction) nodeSet.getNode(rightTransactionNeighbor)).getOwnerIndex();
                }
                else
                {
                    Right = -1;
                }

            }
            return SearchByNameID(searchTarget, Buffer, nodeSet, Right, Left, Level, m, resultSet);
        }
        //TODO ready to detach
//        if (Left != -1 && jumpLevel > 0)
//        {
//            Level = jumpLevel;
//            Buffer = Left;
//            Right = mTopologyGenerator.mNodeSet.getNode(Left).getLookup(Level, 1);
//            Left = mTopologyGenerator.mNodeSet.getNode(Left).getLookup(Level, 0);
//            //System.out.println("L call");
//            return SearchByNameID(searchTarget, Buffer, Right, Left, Level, m);
//        }
        return -1;
    }

    private int checkNeighborNameID(int neighborIndex, String searchTarget, Message m, int Level, SkipGraphNodes nodeSet, ArrayList<Integer> resultSet)
    {
        if (neighborIndex != -1)
        {
            Node leftNode = (Node) mTopologyGenerator.mNodeSet.getNode(neighborIndex);
                /*
                If the left neighbor is online
                 */
            if (leftNode.isOnline())
            {
                if (nodeSet instanceof Nodes && leftNode.getNameID().equals(searchTarget))
                {
                    piggyBackLookupTable(neighborIndex);
                    m.piggyback(neighborIndex, mTopologyGenerator.mNodeSet);
                    return neighborIndex;
                }
                else if (!(nodeSet instanceof Nodes))
                {
                    int mostSimilarTXBIndex = leftNode.mostSimilarTXB(nodeSet, searchTarget, m, Level);
                    /*
                    If mostSimilarTXBIndex is a valid index
                     */
                    if (mostSimilarTXBIndex != -1)
                    {
                        String mostSimilarNameID = nodeSet.getNode(mostSimilarTXBIndex).getNameID();
                        if (mostSimilarNameID.equals(searchTarget))
                        {
                            m.piggyback(neighborIndex, mTopologyGenerator.mNodeSet);
                            resultSet.add(neighborIndex);
                        }
                    }
                }
            }
        }
        return -1;
    }

    private int moveForward(int neighborIndex, int direction, int Level, String searchTarget, Message m, SkipGraphNodes nodeSet)
    {

        if (neighborIndex != -1 && ((Node) mTopologyGenerator.mNodeSet.getNode(neighborIndex)).isOnline())
        {
            Node neighbor = (Node) mTopologyGenerator.mNodeSet.getNode(neighborIndex);
            if (nodeSet instanceof Nodes)
            {
                neighborIndex = neighbor.getLookup(Level, direction);
                piggyBackLookupTable(neighborIndex);
                return neighborIndex;
            }
            else
            {
                int mostSimilarTXBIndex = neighbor.mostSimilarTXB(nodeSet, searchTarget, m, Level);
                int neighborOfNeighbor = nodeSet.getNode(mostSimilarTXBIndex).getLookup(Level, direction);
                if (neighborOfNeighbor >= 0)
                {
                    return ((Transaction) nodeSet.getNode(neighborOfNeighbor)).getOwnerIndex();
                }
            }
        }
        return -1;

    }

    /**
     * This function is invoked as a sub-routine of the search for name ID
     * This function checks the eligibility of the search for name ID for jumping to an upper level:
     * If search is over the Skip Graph of Nodes and neighbor has a most similar name ID to the search target that is
     * also greater than the current level of the search, it returns the common prefix length of name IDs as the level
     * number of the search to jump to.
     * Otherwise, if the search is conducted over the Skip Graph of blockchain, and neighbor Node owns a block/transaction
     * with more similar name ID that is also greater than the current level of the search, it again returns the
     * common prefix length of name IDs as the level number of the search to jump to.
     *
     * @param neighborIndex index of the neighbor Node from the Nodes class
     * @param searchTarget  target name ID of the search
     * @param Level         current level of the search
     * @param m             search Message
     * @param nodeSet       the Transactions instance if the search is over the Blockchain, or Nodes instance, if the search is
     *                      over the Skip Graph of Nodes
     * @return new level number to jump to, -1, if no jump condition is met.
     */
    private int jumpCheck(int neighborIndex, String searchTarget, int Level, Message m, SkipGraphNodes nodeSet)
    {
        Node neighbor = ((Node) mTopologyGenerator.mNodeSet.getNode(neighborIndex));
        /*
        Checks if the neighborIndex is a valid index (i.e., points to a real Node on nodeSet)
        AND is Online.
         */
        if (neighborIndex != -1 && neighbor.isOnline())
        {
            /*
            Piggybacks the entire lookup table of the Node on the search Message to be used by for example
            churn stabilization algorithms
             */
            piggyBackLookupTable(neighborIndex);

            /*
            piggybacks the Node's address itself on the search Message
             */
            m.piggyback(neighborIndex, mTopologyGenerator.mNodeSet);

            /*
            If the search is conducted over the Skip Graph of Nodes, and the common prefix of the neighbor's nameID, and
            the search target is greater than the current level, the function returns the common prefix length, which means
            that the search could be conducted at a higher level corresponds to the common prefix lengh.
             */
            if (nodeSet instanceof Nodes)
            {
                /*
                Computes length of common prefix between name ID of neighbor and searchTarget
                 */
                int commonPrefixLength = commonPrefixLength(neighbor.getNameID(), searchTarget);
                if (commonPrefixLength > Level)
                {
                    return commonPrefixLength;
                }
            }
            /*
            If the search is conducted over the Skip Graph of blocks and transactions, and the neighbor owns a block or transaction with
            longer common prefix with respect to the search target than the current level number, the function returns the common prefix length, which means
            that the search could be conducted at a higher level, which corresponds to the common prefix lengh.
             */
            else
            {
                /*
                Finds the index of the most similar owned block to the search target in the neighbor Node
                 */
                int mostSimilarTXBIndex = neighbor.mostSimilarTXB(nodeSet, searchTarget, m, Level);

                /*
                Retrieves the name ID of the most similar block/transaction
                 */
                String mostSimilarNameID = nodeSet.getNode(mostSimilarTXBIndex).getNameID();

                /*
                Computes the length of common prefix between name ID of the most similar block/transaction and
                search target
                 */
                int commonPrefixLength = commonPrefixLength(searchTarget, mostSimilarNameID);

                /*
                Returns the common prefix length if it is greater than the current level
                 */
                if (commonPrefixLength > Level)
                {
                    return commonPrefixLength;
                }
            }
        }
        return -1;
    }


//    public int SearchByNameID2(String name, int startIndex)
//    {
//        //System.out.println("Search for name id starts from " + startIndex + " " + SkipGraph.Nodes.nodeSet[startIndex].getNameID() + " for " + name);
//        int Left = 0;
//        int Right = 0;
//        int level = 0;
//        int before = startIndex;
//
//
//        if (mTopologyGenerator.mNodeSet.getNode(startIndex).getNameID() == name)  // UPDATED!!!!
//        {
//            return startIndex;
//        }
//
//        Left = getResolve(startIndex, 0, 0);
//        Right = getResolve(startIndex, 0, 1);
//        if (commonPrefixLength(mTopologyGenerator.mNodeSet.getNode(startIndex).getNameID(), name) > level)
//        {//goes to upper levels for search
//            level = commonPrefixLength(mTopologyGenerator.mNodeSet.getNode(startIndex).getNameID(), name);
//            Left = getResolve(startIndex, level, 0);
//            Right = getResolve(startIndex, level, 1);
//
//
//        }
//
//
//        while (true)
//        {
//            if (Left != -1)
//            {
//                if (mTopologyGenerator.mNodeSet.getNode(Left).getNameID() == name)
//                {
//                    return Left;
//                }
//                else if (commonPrefixLength(mTopologyGenerator.mNodeSet.getNode(Left).getNameID(), name) <= level)
//                {
//                    before = Left;
//                    Left = getResolve(Left, level, 0);
//                    mTopologyGenerator.mNodeSet.addTime(Left, before);
//                }
//                else if (commonPrefixLength(mTopologyGenerator.mNodeSet.getNode(Left).getNameID(), name) > level)
//                {
//                    level = commonPrefixLength(mTopologyGenerator.mNodeSet.getNode(Left).getNameID(), name);
//                    before = Left;
//                    Right = getResolve(Left, level, 1);
//                    Left = getResolve(Left, level, 0);
//                    mTopologyGenerator.mNodeSet.addMaxTime(Left, Right, before);
//                    //mTopologyGenerator.mNodeSet.addTime(Right, before);
//                    continue;
//                }
//            }
//
//            else if (Right != -1)
//            {
//                if (mTopologyGenerator.mNodeSet.getNode(Right).getNameID() == name)
//                {
//                    return Right;
//                }
//                else if (commonPrefixLength(mTopologyGenerator.mNodeSet.getNode(Right).getNameID(), name) <= level)
//                {
//                    before = Right;
//                    Right = getResolve(Right, level, 1);
//                    mTopologyGenerator.mNodeSet.addTime(Right, before);
//                }
//                else if (commonPrefixLength(mTopologyGenerator.mNodeSet.getNode(Right).getNameID(), name) > level)
//                {
//                    level = commonPrefixLength(mTopologyGenerator.mNodeSet.getNode(Right).getNameID(), name);
//                    before = Right;
//                    Right = getResolve(Right, level, 1);
//                    Left = getResolve(Right, level, 0);
//                    mTopologyGenerator.mNodeSet.addMaxTime(Left, Right, before);
//                    //mTopologyGenerator.mNodeSet.addTime(Left, before);
//                    //mTopologyGenerator.mNodeSet.addTime(Right, before);
//                    continue;
//                }
//            }
//            if (Right == -1 && Left == -1)
//            {
//                break;
//            }
//        }
//
//
//        return -1;
//
//    }

    //TODO current time needs to be added to piggybacked Message

    /**
     * Performs a search in the given skip-graph with respect to numerical ids, and returns the index of the node
     * equal to (or which has the greatest numerical id smaller than) the target numerical id.
     * @param targetNumId target numerical id that needs to be searched.
     * @param currentNode the node that the search is initiated from.
     * @param m the message.
     * @param level the level that the search is performed from.
     * @param currentTime current time.
     * @param nodeSet the set of nodes that needs to be searched on.
     * @param searchDirection the direction of search. If the target num id is greater than the initiator num id,
     *                        search direction must be RIGHT, else LEFT.
     * @return the index of the node that has a numerical id equal to targetNumId if it exists. Otherwise, the index
     * of the node with the greatest numerical id smaller than the target numerical id is returned.
     */
    public int SearchByNumID(int targetNumId, Node currentNode, Message m, int level, int currentTime, SkipGraphNodes nodeSet, int searchDirection)
    {
        if (level < 0)
        {
            System.err.println("SkipGraphOperations.java: search started with negative level");
        }


        /*
        Printing the search path information of the current Node i.e., startindex is the log is on
         */
        if (SkipSimParameters.isLog())
        {
            log.debug("Search by num ID started: target={} currentNode={}", targetNumId, currentNode.getIndex());
            currentNode.printLookup();
            m.printSearchPath(nodeSet, false);
        }

        /*
        Piggybacking the information of current peer (and not transaction or block, rather the
        transaction or block's owner) on the Message.
         */
        m.piggyback(currentNode.getIndex(), getTG().mNodeSet);

        if (SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.DYNAMIC)
                || SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.BLOCKCHAIN))
        {
            /*
            Generate an error and exits the SkipSim if the search reaches an offline Node.
             */
            if (currentNode.isOffline())
            {
                System.err.println("SkipGraphOperations.java: Offline Node is invoked search for NumID initiator");
                System.exit(0);
            }

            /*
            Increase the current Node incoming connections if the availability predictor is LUDP
             */
            if (SkipSimParameters.getAvailabilityPredictor().equalsIgnoreCase(Constants.Churn.AvailabilityPredictorAlgorithm.LUDP))
            {
                ((LUDP) (currentNode.getAvailabilityPredictor())).incrementIncomingConnections();
            }
//            else if (mTopologyGenerator.mNodeSet.getNode(startIndex).isLookupTableEmpty())
//            {
//                return startIndex;
//            }
        }


        if (nodeSet instanceof Nodes)
        {
            //Return index of the current Node if the search is within the Nodes overlay, and it is the search target
            if (currentNode.getNumID() == targetNumId)
            {
                //System.out.println("SkipGraphOperations.java: target was found in starter, search for num ID returns " + startIndex);
                //ChurnStochastics.updateAverageSuccessTimeOuts(1);
                return currentNode.getIndex();
            }
        }
        else
        {
            /*
            Return index of the current Node if the search is within the transactions or blocks, and current Node contains a copy of
            the targeted transaction.
             */
            int possessionCode = currentNode.containsTXB(nodeSet, targetNumId);
            if (possessionCode >= 0)
                return currentNode.getIndex();
        }

        /*
        Index of the closest block or transaction to the target num ID if the search is within
        the blockchain
         */
        int closestTXBIndex = -1;
        int closestTXBNumID = -1;
        //if (nodeSet instanceof Blocks || nodeSet instanceof Transactions)
        if (nodeSet instanceof Transactions)
        {
            closestTXBIndex = currentNode.mostSimilarTXB(nodeSet, targetNumId, m, searchDirection, level);
            if (closestTXBIndex != -1)
                closestTXBNumID = nodeSet.getNode(closestTXBIndex).getNumID();
            if (closestTXBNumID == targetNumId && closestTXBNumID != -1)
            {
                return currentNode.getIndex();
            }
        }
        //TODO what happens when the closestTXBNumID == -1????
        if ((nodeSet instanceof Nodes && currentNode.getNumID() < targetNumId)
                || (closestTXBIndex != -1 && !(nodeSet instanceof Nodes) && closestTXBNumID < targetNumId))
        {
            // Go right & down
            while (level >= 0)
            {
                //System.out.println("SkipGraphOperations.java: SearchByNumID level " + level);
                int Right = -1;
                if (nodeSet instanceof Nodes)
                {
                    Right = currentNode.getLookup(level, 1);
                    if (SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.DYNAMIC) && SkipSimParameters.getChurnType().equals(Constants.Churn.Type.ADVERSARIAL))
                    {
                        try
                        {
                            Right = AlgorithmInvoker.churnStabilization().resolveFailure(mTopologyGenerator.mNodeSet,
                                    Right, 1, currentNode.getIndex(), level, targetNumId, m, currentTime);
                        }
                        catch (NullPointerException ex)
                        {
                            /*
                            If churn is cooperative it throws a null pointer exception
                             */
                            Right = currentNode.getLookup(level, 1);
                        }
                    }
                }
                /*
                Search is on the blockchain ledger, and closestTXBIndex denotes a valid Node address
                 */
                else if (closestTXBIndex != -1)
                {
                    /**
                     *  int leftNeighborOwner = ((Transaction) nodeSet.getNode(leftNeighborOnBlockchain)).getOwnerIndex();
                     *                         int mostSimilarInLeftNeighborsOwner = ((Node) mTopologyGenerator.mNodeSet.getNode(leftNeighborOwner)).mostSimilarTXB(nodeSet, targetNumId, m, LEFT_SEARCH_DIRECTION, level);
                     *                         if(mostSimilarInLeftNeighborsOwner != -1) {
                     *                             Left = leftNeighborOwner;
                     *                             closestTXBIndex = mostSimilarInLeftNeighborsOwner;
                     *                         }
                     */
                    Right = -1;
                    int rightNeighborOnBlockchain = nodeSet.getNode(closestTXBIndex).getLookup(level, 1);
                    if (rightNeighborOnBlockchain != -1)
                    {
                        int rightNeighborOwner = ((Transaction) nodeSet.getNode(rightNeighborOnBlockchain)).getOwnerIndex();
                        int mostSimilarInRightNeighborsOwner = ((Node) mTopologyGenerator.mNodeSet.getNode(rightNeighborOwner))
                                .mostSimilarTXB(nodeSet, targetNumId, m, RIGHT_SEARCH_DIRECTION, level);
                        if(mostSimilarInRightNeighborsOwner != -1) {
                            Right = rightNeighborOwner;
                            closestTXBIndex = mostSimilarInRightNeighborsOwner;
                            closestTXBNumID = ((Transaction) nodeSet.getNode(closestTXBIndex)).getNumID();
                        }
                    }
                }

                /*
                There is a right neighbor
                 */
                if (Right != -1 &&
                        /*
                        searching within Skip Graph of Nodes and Right's numID is less than the search target
                         */
                        ((nodeSet instanceof Nodes && mTopologyGenerator.mNodeSet.getNode(Right).getNumID() <= targetNumId)
                                /*
                                OR searching within Skip Graph of blocks and transactions and Right Node has a closer block or transaction
                                to the search target
                                 */
                                || (closestTXBIndex != -1 && !(nodeSet instanceof Nodes) && nodeSet.getNode(closestTXBIndex).getNumID() <= targetNumId)))
                {
                    //if (Right != -1)
                    //{
                    int scanForwardStatus = scanForward(m, currentNode, (Node) mTopologyGenerator.mNodeSet.getNode(Right),
                            targetNumId, level, currentTime, searchDirection, nodeSet);
                    if (scanForwardStatus == -1)
                    {
                        continue;
                    }
                    else
                    {
                        return scanForwardStatus;
                    }
                    //}

                }

                if (Right == -1 || (mTopologyGenerator.mNodeSet.getNode(Right).getNumID() > targetNumId)
                        || (closestTXBIndex != -1 && !(nodeSet instanceof Nodes) && nodeSet.getNode(closestTXBIndex).getNumID() > targetNumId))
                {
                    level--;
                }
            }
        }
        else if ((nodeSet instanceof Nodes && currentNode.getNumID() >= targetNumId)
                || (closestTXBIndex != -1 && !(nodeSet instanceof Nodes) && closestTXBNumID >= targetNumId))
        {
            // Go left & down
            while (level >= 0)
            {
                //System.out.println("SkipGraphOperations.java: SearchByNumID level " + level);
                int Left = currentNode.getLookup(level, 0);
                if (nodeSet instanceof Nodes)
                {
                    if (SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.DYNAMIC) && SkipSimParameters.getChurnType().equals(Constants.Churn.Type.ADVERSARIAL))
                    {
                        try
                        {
                            Left = AlgorithmInvoker.churnStabilization().resolveFailure(mTopologyGenerator.mNodeSet, Left,
                                    0, currentNode.getIndex(), level, targetNumId, m, currentTime);
                        }
                        catch (NullPointerException ex)
                        {
                                                        /*
                            If churn is cooperative it throws a null pointer exception
                             */
                            Left = currentNode.getLookup(level, 0);
                        }

                    }
                }
                /*
                Search is on the blockchain ledger, and closestTXBIndex denotes a valid Node address
                 */
                else if (closestTXBIndex != -1)
                {
                    Left = -1;
                    int leftNeighborOnBlockchain = nodeSet.getNode(closestTXBIndex).getLookup(level, 0);
                    if (leftNeighborOnBlockchain != -1)
                    {
                        int leftNeighborOwner = ((Transaction) nodeSet.getNode(leftNeighborOnBlockchain)).getOwnerIndex();
                        int mostSimilarInLeftNeighborsOwner = ((Node) mTopologyGenerator.mNodeSet.getNode(leftNeighborOwner))
                                .mostSimilarTXB(nodeSet, targetNumId, m, LEFT_SEARCH_DIRECTION, level);
                        if(mostSimilarInLeftNeighborsOwner != -1) {
                            Left = leftNeighborOwner;
                            closestTXBIndex = mostSimilarInLeftNeighborsOwner;
                            closestTXBNumID = ((Transaction) nodeSet.getNode(closestTXBIndex)).getNumID();
                        }
                    }
                }

                if (Left != -1 &&
                        /*
                        searching within Skip Graph of Nodes and Left's numID is greater than the search target
                         */
                        ((nodeSet instanceof Nodes && mTopologyGenerator.mNodeSet.getNode(Left).getNumID() >= targetNumId)
                                /*
                                OR searching within Skip Graph of blocks and transactions and Left Node has a closer block or transaction
                                to the search target
                                 */
                                || (closestTXBIndex != -1 && !(nodeSet instanceof Nodes) && nodeSet.getNode(closestTXBIndex).getNumID() >= targetNumId)))

                {

                    int scanForwardStatus = scanForward(m, currentNode, (Node) mTopologyGenerator.mNodeSet.getNode(Left),
                            targetNumId, level, currentTime, searchDirection, nodeSet);
                    if (scanForwardStatus == -1)
                    {
                        continue;
                    }
                    else
                    {
                        return scanForwardStatus;
                    }


                }
                if (Left == -1 || (mTopologyGenerator.mNodeSet.getNode(Left).getNumID() < targetNumId)
                        || (closestTXBIndex != -1 && !(nodeSet instanceof Nodes) && nodeSet.getNode(closestTXBIndex).getNumID() < targetNumId))
                {
                    level--;
                }
            }
        }

        if (SkipSimParameters.isLog())
        {
            log.debug("Search stops at level {}", level);
        }
        return currentNode.getIndex();

    }

    private int scanForward(Message m, Node currentNode, Node neighborNode, int targetNumId, final int level,
                            int currentTime, int searchDirection, SkipGraphNodes nodeSet)
    {
        /*
        If the search Message is within the skip graph of Nodes
         */
        //if (nodeSet instanceof Nodes)
        //{
        if (m.contains(neighborNode.getIndex()))
        {
            if (SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.BLOCKCHAIN))
            {
                /*
                The following if statement happens when two consecutive blocks/transactions of the same owner are
                neighbor, hence on routing on the Skip Graph, moving from one Node to the next Node looks like re-visiting
                the owner Node, which violates the search and is treated as a cycle. But as long as the neighbor and current
                index both point to the same Node it does not make any problem. This is because that following the mostSimilarTXB
                function of Node, the first transaction is the most similar predecessor or successor, and the immediate neighbor of
                the first transaction on the same Node crosses over the search target, otherwise it would be the most similar
                Node in the first place. Hence, the address of their owner is returned as the search result.
                 */
                if (neighborNode.getIndex() == currentNode.getIndex())
                {
                    return currentNode.getIndex();
                }
            }
                /*
                If this neighbor has been already visited by this search Message
                 */
            if (SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.DYNAMIC))
            {
                if (SkipSimParameters.getChurnType().equalsIgnoreCase(Constants.Churn.Type.ADVERSARIAL))
                {
                    return neighborNode.getIndex();
                }
                else
                {
                    m.printSearchPath(getTG().mNodeSet, false);
                    System.err.println("SkipGraphOperations.java: cycle detected on search for num ID during non adversarial churn");
                    System.exit(0);
                }
            }
            else
            {
                System.err.println("SkipGraphOperations.java: cycle detected on search for num ID during non-dynamic simulation");
                System.exit(0);
            }

        }
        //}
//        else
//        {
//            /*
//            If the search Message is within the Skip Graph of the blockchain and owner of this transaction or block has already been visited.
//             */
//            if (m.contains(neighborNode))
//            {
//                System.err.println("SkipGraphOperations.java: cycle detected on search for num ID during blockchain simulation");
//                System.exit(0);
//            }
//        }

        if ((SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.DYNAMIC) && neighborNode.isOffline()))
        //|| (!(nodeSet instanceof Nodes) && ((Node) getTG().mNodeSet.getNode(((Transaction) nodeSet.getNode(neighbor)).getOwnerIndex())).isOffline()))
        {
            /*
            If the search is within the blockchain and owner of the neighbor Node is offline, hence the
            neighbor Node is inaccessible
             */
//            if((!(nodeSet instanceof Nodes)))
//            {
//                return currentNode;
//            }

            /*
            if the search type is cooperative and hence no offline neighbor info should reside on the overlay since
            in cooperative churn each Node notifies and connects its neighbors to each other before leaving the Skip Graph
             */
            if (SkipSimParameters.getChurnType().equals(Constants.Churn.Type.COOPERATIVE))
            {
                //new ChurnStochastics().updateAverageResolveFailureTimeOuts();
                return -1;
            }
            else
            {
                return currentNode.getIndex();
            }
        }
        //Node rightNode = getTG().mNodeSet.getNode(neighbor);//For debugging
//        if(nodeSet instanceof Nodes)
//        {
        mTopologyGenerator.mNodeSet.addTime(neighborNode.getIndex(), currentNode.getIndex());
        m.piggyback(neighborNode.getIndex(), mTopologyGenerator.mNodeSet);
        return SearchByNumID(targetNumId, neighborNode, m, level, currentTime, nodeSet, searchDirection);
//        }
//        else
//        {
//            mTopologyGenerator.mNodeSet.addTime(((Transaction) nodeSet.getNode(neighbor)).getOwnerIndex(), ((Transaction) nodeSet.getNode(currentNode)).getOwnerIndex());
//            m.piggyback(neighbor, mTopologyGenerator.mNodeSet, 0);
//            return SearchByNumID(targetNumId, (Node) mTopologyGenerator.mNodeSet.getNode(((Transaction) (nodeSet.getNode(neighbor))).getOwnerIndex()), m, level, currentTime, nodeSet);
//        }
        //ChurnStochastics.updateAverageSuccessTimeOuts(1);

    }

//TODO needs to check the functionality with SearchForNameID2
//    public int getResolve(int dst, int i, int j)
//    {
//        if (dst == -1)
//        {
//            return -1;
//        }
//        if (mTopologyGenerator.mNodeSet.getNode(dst).getLookup(i, j) == -1)
//        {
//            return -1;
//        }
//        else
//        {
//            if (mTopologyGenerator.mNodeSet.getNode(mTopologyGenerator.mNodeSet.getNode(dst).getLookup(i, j)).isDeactive() == false)
//            {
//                return (mTopologyGenerator.mNodeSet.getNode(dst).getLookup(i, j));
//            }
//            else
//            {
//                if (system.isBackup())
//                {
//                    if (mTopologyGenerator.mNodeSet.getNode(dst).getBackup(i, j) == -1)
//                    {
//                        return -1;
//                    }
//                    else
//                    {
//                        if (mTopologyGenerator.mNodeSet.getNode(mTopologyGenerator.mNodeSet.getNode(dst).getBackup(i, j)).isDeactive() == false)
//                        {
//                            return (mTopologyGenerator.mNodeSet.getNode(dst).getBackup(i, j));
//                        }
//                        else
//                        {
//                            return -1;
//                        }
//                    }
//
//                }
//                else
//                {
//                    //RecoveryEvaluation.failTransCount++;
//                    return -1;
//                }
//            }
//        }
//
//    }


    /**
     * Returns the topology generator stored in this SkipGraphOperations object.
     * @return the topology generator.
     */
    public TopologyGenerator getTG()
    {
        return mTopologyGenerator;
    }
}
