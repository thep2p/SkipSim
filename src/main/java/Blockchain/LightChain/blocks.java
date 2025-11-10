package Blockchain.LightChain;//package Blockchain.LightChain;
//
//import DataTypes.Constants;
//import Simulator.SkipSimParameters;
//import SkipGraph.SkipGraphNode;
//import SkipGraph.SkipGraphNodes;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//
//public class Blocks extends SkipGraphNodes
//{
//    /**
//     * Set of all generated blocks in system
//     */
//    private ArrayList<Block> mBlockSet;
//
//    /**
//     * points to the first available empty slot ont he mBlockSet
//     */
//    private int currentBlockSetIndex;
//
//    public int getCurrentBlockSetIndex()
//    {
//        return currentBlockSetIndex;
//    }
//
//    private Map<String, Integer> blockHashes = new HashMap<>();
////    private ArrayList searchPathLatency;
////    private int totalTime;
//
//    //***REMINDER: The field below has a getter in Nodes.java but does not have setter. So we had to include it here.
//    //private ArrayList<Integer> searchPathLatency; // don't need
//
//    public Blocks()
//    {
//        super();
//        mBlockSet = new ArrayList<>();
//        currentBlockSetIndex = 0;
////        for (int i = 0; i < blockCapacity; i++)
////        {
////            mBlockSet[i] = new Block(true, this, i, -1, rand.nextInt(system.getSystemCapacity()), mNodeSet);
////        }
////        searchPathLatency = new ArrayList<Integer>();
//
//    }
//
//    @Override
//    public void setNode(int index, SkipGraphNode skipGraphNode)
//    {
//        //TODO test for proper insertion i.e., avoid replacement
//        mBlockSet.set(currentBlockSetIndex, (Block) skipGraphNode);
//        currentBlockSetIndex++;
//        //tODO insert into bgo
//    }
//
//    @Override
//    public SkipGraphNode getNode(int index)
//    {
//        return mBlockSet.get(index);
//    }
//
//    //    public void clearBlocks()
////    {
////        mBlockSet = new Block[blockCapacity];
////    }
//
//
//
//
////    public int blockLength()
////    {
////        return mBlockSet.length;
////    }
//
////    public void renewBlock(int index)
////    {
////        if (index == 0)
////        {
////            mBlockSet[index].setNumID(0);
////        }
////        else
////        {
////            mBlockSet[index].setNumID(getRandomNumID(index));
////        }
////
////
////        for (int i = 0; i < system.getBlockLookupTableSize(); i++)
////            for (int j = 0; j < 2; j++)
////            {
////                mBlockSet[index].setLookup(i, j, -1);
////            }
////    }
//
////    public int getRandomNumID(int blockIndex)
////    {
////        /*
////         */
////        if (blockIndex == 0)
////        {
////            return 0;
////        }
////        else
////        {
////            Random numIDRandomGenerator = new Random();
////            int numID = Math.abs(numIDRandomGenerator.nextInt(10 * blockCapacity)) + 1;
////            return numID;
////        }
////
////    }
//
////    public void addTime(int destination, int source, Nodes mNodeSet)
////    {
////        if (destination != -1 && source != -1 && mBlockSet[source].getOwnerIndex() != -1 && mBlockSet[destination].getOwnerIndex() != -1)
////        {
////            double latency = mNodeSet.getNode(mBlockSet.get(source).getOwnerIndex()).mCoordinate.distance(mNodeSet.getNode(mBlockSet[destination].getOwnerIndex()).mCoordinate);
////            searchPathLatency.add((int) latency);
////            totalTime += latency;
////        }
////    }
//
//    public int commonPrefixLength(int i, int j)
//    {
//        String s1 = mBlockSet.get(i).getNameID();
//        String s2 = mBlockSet.get(j).getNameID();
//
//        int k = 0;
//
//        if (s1.length() > 0 && s2.length() > 0)
//        {
//            while (s1.charAt(k) == s2.charAt(k))
//            {
//                k++;
//                if (k >= s1.length() || k >= s2.length())// || k >= Simulator.system.nameIDsize )
//                {
//                    break;
//                }
//            }
//        }
//
//        return k;
//    }
//
//
////    public int minNameIDSize(int i, int j)
////    {
////        String s1 = mBlockSet[i].getNameID();
////        String s2 = mBlockSet[j].getNameID();
////
////        if (s1.length() < s2.length())
////        {
////            return s1.length();
////        }
////
////        else
////        {
////            return s2.length();
////        }
////    }
////
////    public int offlineBlocksCounter(Nodes mNodeSet)
////    {
////        int num = 0;
////        for (int i = 0; i < blockCapacity; i++)
////        {
////            if (!mBlockSet[i].isOnline(mNodeSet))
////            {
////                num++;
////            }
////        }
////
////        return num;
////    }
////
////    public boolean nameIDsDoubleCheck()
////    {
////        boolean flag = true;
////        for (int i = 0; i < blockCapacity; i++)
////            for (int j = 0; j < blockCapacity; j++)
////            {
////                if (i == j)
////                {
////                    continue;
////                }
////                else if (mBlockSet[i].getNameID().equals(mBlockSet[j].getNameID()) && !mBlockSet[i].getNameID().isEmpty())
////                {
////                    System.out.println("Same name id: " + i + " " + j + "\n" + mBlockSet[i].getNameID() + " " + mBlockSet[j].getNameID());
////                    flag = false;
////                }
////            }
////        if (flag)
////        {
////            System.out.println("No match was found!");
////        }
////        return true;
////    }
////
////    public int getNumberOfOnlineBlocks(Nodes mNodeSet)
////    {
////        int counter = 0;
////        int nulllls = 0;
////        for (int i = 0; i < blockCapacity; i++)
////        {
////            if (mBlockSet[i] == null)
////            {
////                nulllls++;
////            }
////            if (mBlockSet[i] != null && mBlockSet[i].isOnline(mNodeSet))
////            {
////                //topologyTotalOnlineNodes++;
////                counter++;
////            }
////        }
////        System.out.println(nulllls);
////        return counter;
////    }
//
////    public ArrayList<Integer> getIndicesOfOnlineBlocks(Nodes mNodeSet)
////    {
////        ArrayList<Integer> onlineBlocks = new ArrayList<>();
////        for (int i = 0; i < system.getBlockCapacity(); i++)
////        {
////            if (mBlockSet[i].getOwnerIndex() == -1) continue;
////            if (mBlockSet[i].isOnline(mNodeSet) && !onlineBlocks.contains(i))
////            {
////                //topologyTotalOnlineNodes++;
////                onlineBlocks.add(i);
////            }
////        }
////        return onlineBlocks;
////    }
//
////    public int getNumberOfOfflineBlocks(Nodes mNodeSet)
////    {
////        int counter = 0;
////        for (int i = 0; i < blockCapacity; i++)
////        {
////            if (!mBlockSet[i].isOnline(mNodeSet))
////            {
////                //topologyTotalOnlineNodes++;
////                counter++;
////            }
////        }
////        //System.out.println("Nodes.java: number of offline Nodes" + counter);
////        return counter;
////    }
//
//
////    public void generateBlocks(boolean shouldNumIDBeAssigned, boolean shouldBeInserted, BlockGraphOperations bgo, int numberOfBlocks, Node ownerNode, int currentTime, Nodes mNodeSet)
////    {
////        Random rand = new Random();
////        for (int i = 0; i < numberOfBlocks; i++)
////        {
////            int previousBlockIndex;
////
////            if (currentBlockSetIndex > 0)
////            {
////                previousBlockIndex = rand.nextInt(currentBlockSetIndex);
////            }
////            else
////            {
////                previousBlockIndex = -1;
////            }
////
////            Block b = new Block(true, this, i + currentBlockSetIndex, ownerNode.getIndex(), previousBlockIndex, mNodeSet);
////            SignatureLookupTable.sign(b, ownerNode);
////            if (shouldBeInserted)
////            {
////                if (AlgorithmInvoker.isNameIDAssignmentDynamic())
////                {
////                    b.setNumID(generateHash(ownerNode.getNumID(), b.getListOfTransactions(), currentTime));
////                    if (previousBlockIndex == -1)
////                    {
////                        b.setNameID(Integer.toBinaryString(0));
////                    }
////                    else
////                    {
////                        b.setNameID(Integer.toBinaryString(getBlock(b.getPreviousBlockIndex()).getNumID()));
////                    }
////
////                }
////
////                int index = currentBlockSetIndex;
////                bgo.insert(b, index, AlgorithmInvoker.isNameIDAssignmentDynamic());
////            }
////            else
////            {
////                mBlockSet[i] = b;
////            }
////
////            // System.out.println("Block.java: A Block generated: Block name id is " + b.nameID + " numerical id is " + b.getNumID() + " Simulator.system index =  " + (i+currentBlockSetIndex));
////            currentBlockSetIndex++;
////        }
////    }
//
////    public int generateHash(int ownerNodeNumID, ArrayList<Block.Transaction> listOfTransactions, int currentTime)
////    {
////        StringBuilder sb = new StringBuilder();
////
////        sb.append(ownerNodeNumID + ".");
////        for (Block.Transaction transaction : listOfTransactions)
////        {
////            sb.append(transaction + ".");
////        }
////        sb.append(currentTime);
////        String hashKey = sb.toString();
////
////        if (blockHashes.containsKey(hashKey)) return blockHashes.get(hashKey);
////
////        int hash = hashRandomGen.nextInt(hashSpaceSize);
////        while (usedHashes.contains(hash))
////        {
////            hash = hashRandomGen.nextInt(hashSpaceSize);
////        }
////
////        blockHashes.put(hashKey, hash);
////        return hash;
////    }
//
////    public void printLookupNumID(int index)
////    {
////        for (int i = system.getLookupTableSize() - 1; i >= 0; i--)
////        {
////            int right = -1;
////            int left = -1;
////            if (mBlockSet[index].getLookup(i, 0) != -1)
////                left = mBlockSet[mBlockSet[index].getLookup(i, 0)].getNumID();
////            if (mBlockSet[index].getLookup(i, 1) != -1)
////                right = mBlockSet[mBlockSet[index].getLookup(i, 1)].getNumID();
////
////            System.out.println("Level: " + i + "   Left: " + left + "   Right: " + right);
////        }
////
////    }
////
////    public void printLookupOnlineStatus(int index, Nodes mNodeSet)
////    {
////        for (int i = system.getLookupTableSize() - 1; i >= 0; i--)
////        {
////            boolean right = false;
////            boolean left = false;
////            if (mBlockSet[index].getLookup(i, 0) != -1)
////                left = mBlockSet[mBlockSet[index].getLookup(i, 0)].isOnline(mNodeSet);
////            if (mBlockSet[index].getLookup(i, 1) != -1)
////                right = mBlockSet[mBlockSet[index].getLookup(i, 1)].isOnline(mNodeSet);
////
////            System.out.println("Level: " + i + "   Left: " + left + "   Right: " + right);
////        }
////
////    }
//
////    public boolean containsBlock(int nodeIndex, int blockIndex, Nodes mNodeSet)
////    {
////
////        if (mNodeSet.getNode(nodeIndex).getTxSet().contains(blockIndex))
////            return true;
////        else
////            return false;
////    }
//
////    /**
////     * Given the index of the current Node on the search path it returns the address of another Node in the same
////     * nodeSet that is the closer in the search direction with respect to the search target
////     * @param currentIndex index of the current transaction on the search path
////     * @param targetNumID target numerical ID of the search
////     * @return
////     */
////    public int routeToNeighbot(int currentIndex, int targetNumID, SkipGraphNodes nodeSet)
////    {
////        //Num id of the current transaction on the search path
////        int currentNumID = nodeSet.getNode(currentIndex).getNumID();
////
////        //The current Node on the search path is the target, it's address is being returned
////        if(currentNumID == targetNumID)
////        {
////            return currentIndex;
////        }
////        int direction = (currentNumID < targetNumID) ? 1:0;
////
////    }
//
//
//}
//
//
//
