package NameIDAssignment;

import DataTypes.Constants;
import Simulator.SkipSimParameters;
import SkipGraph.Node;
import SkipGraph.SkipGraphOperations;
import mdsj.ClassicalScaling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

public abstract class NameIDAssignment
{
    private static final Logger log = LoggerFactory.getLogger(NameIDAssignment.class);

    protected static int[][] B = new int[SkipSimParameters.getLandmarksNum()][SkipSimParameters.getLandmarksNum()]; //binary matrix
    protected static double[] M = new double[SkipSimParameters.getLandmarksNum()]; //Mean of distances to the landmarks
    protected static int nodeIndex = 0; //Number of SkipGraph.Nodes arriving to the Simulator.system so far
    protected static ArrayList<String> extendSet = new ArrayList<String>();
    protected static boolean initializationLock = true;
    protected static String[] nameSpace = new String[(int) Math.pow(2, SkipSimParameters.getNameIDLength())];
    protected static int nameSpaceIndex = 0;
    //protected static double[][] zeroPrefixHistorgam;
    //private   static double[] zeroPrefixHistogramTotal = new double[Simulator.system.getLandmarksNum()];
    private static Hashtable<String, Double>[] oneTopologyPrefixProbabilities;
    private static Hashtable<String, Double> allTopologyPrefixProbabilities;
    private static int[] numberOfNodes;
    public double[] D = new double[SkipSimParameters.getLandmarksNum()]; //Distances to the landmarks
    public double[] LMDS = new double[SkipSimParameters.getSystemCapacity()];
    public String nameID = new String();
    protected double[][] adj;
    protected SkipGraphOperations sgo;

    /**
     * This function is getting called externally, at the end of each topology, free up the initialization lock, and initialize
     * the settings that need to be done only once per topology
     */
    public static void initialize()
    {
        initializationLock = true;
    }
    protected void prefixAnalysis(int landmarkIndex, String bodyPart)
    {

        try
        {
            numberOfNodes[landmarkIndex]++;
            //System.out.println("body part is " + bodyPart);
            for (int i = 0; i < SkipSimParameters.getNameIDLength(); i++)
            {
                String prefix = bodyPart.substring(0, i + 1);
                //System.out.println("prefix " + prefix);
                oneTopologyPrefixProbabilities[landmarkIndex].putIfAbsent(prefix, (double) 0);
                double oldProb = oneTopologyPrefixProbabilities[landmarkIndex].get(prefix);
                oneTopologyPrefixProbabilities[landmarkIndex].put(prefix, oldProb + 1);
            }
            //System.out.println("-----------------------------");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    //    protected void concludeZeroPrefixHistogramOfThisTopology()
//        {
//            for(int j = 0 ; j < Simulator.system.getLandmarksNum() ; j++)
//                {
//                    double averagePrefixProb = 0;
//                    for(int i = 0 ; i < Simulator.system.getLandmarksNum() ; i++)
//                        {
//                            averagePrefixProb += (double) zeroPrefixHistorgam[i][j] / numberOfNodes[i];
//                        }
//                    zeroPrefixHistogramTotal[j] += averagePrefixProb / Simulator.system.getLandmarksNum();
//                    if(Simulator.system.getCurrentTopologyIndex() == Simulator.system.getTopologyNumbers())
//                        {
//                            zeroPrefixHistogramTotal[j] /= Simulator.system.getTopologyNumbers();
//                            System.out.println("Probability of having prefix length of " + j + " zero(s) " + zeroPrefixHistogramTotal[j]);
//                        }
//                }
//
//
//        }

    /**
     *
     * @param nameID the input name ID which contains the prefix and body
     * @return the body part of name ID
     */
    protected static String extractBody(String nameID, SkipGraphOperations sgo)
    {
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            if (nameID.startsWith(sgo.getTG().mLandmarks.getDynamicPrefix(i)))
            {
                //System.out.println("extract body name ID" + nameID + " prefix " + sgo.getTG().mLandmarks.getDynamicPrefix(i) + " body " + nameID.substring(sgo.getTG().mLandmarks.getDynamicPrefix(i).length()) + " body size " + nameID.substring(sgo.getTG().mLandmarks.getDynamicPrefix(i).length()).length());
                return nameID.substring(sgo.getTG().mLandmarks.getDynamicPrefix(i).length());
            }
        }
        System.err.println("NameIDAssignment.java: extract body could not find the landmark prefix for nameID " + nameID);
        System.exit(0);
        return null;
    }

    protected void concludePrefixAnalysisOfThisTopology()
    {
        Hashtable<String, Double> regionsAveragePrefixProbability = new Hashtable<>();
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            for (Map.Entry<String, Double> entry : oneTopologyPrefixProbabilities[i].entrySet())
            {
                String key = entry.getKey();
                double value = entry.getValue();
                regionsAveragePrefixProbability.putIfAbsent(key, (double) 0);
                double oldProb = regionsAveragePrefixProbability.get(key);
                regionsAveragePrefixProbability.put(key, (oldProb + (value / numberOfNodes[i])));
            }
        }
        for (Map.Entry<String, Double> entry : regionsAveragePrefixProbability.entrySet())
        {
            String key = entry.getKey();
            double value = entry.getValue();
            allTopologyPrefixProbabilities.putIfAbsent(key, (double) 0);
            double oldProb = allTopologyPrefixProbabilities.get(key);
            allTopologyPrefixProbabilities.put(key, oldProb + (value / SkipSimParameters.getLandmarksNum()));
        }


        if (SkipSimParameters.getCurrentTopologyIndex() == SkipSimParameters.getTopologies())
        {
            for (int i = 1; i <= SkipSimParameters.getLandmarksNum(); i++)
            {
                for (Map.Entry<String, Double> entry : allTopologyPrefixProbabilities.entrySet())
                {
                    String key = entry.getKey();
                    if (key.length() == i)
                    {
                        double value = entry.getValue();
                        log.info("Prefix probability [length={}, prefix={}]: {}", i, key, (value / SkipSimParameters.getTopologies()));
                    }

                }
            }

        }

    }

    //    protected void zeroPrefixAnalysisInit()
//        {
//            numberOfNodes = new int[Simulator.system.getLandmarksNum()];
//            zeroPrefixHistorgam = new double[Simulator.system.getLandmarksNum()][Simulator.system.getLandmarksNum()];
//        }
    protected void prefixAnalysisInit()
    {
        numberOfNodes = new int[SkipSimParameters.getLandmarksNum()];
        oneTopologyPrefixProbabilities = new Hashtable[SkipSimParameters.getLandmarksNum()];
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            oneTopologyPrefixProbabilities[i] = new Hashtable<>();
        }
        if (SkipSimParameters.getCurrentTopologyIndex() == 1)
        {
            allTopologyPrefixProbabilities = new Hashtable<>();
        }
    }

    public void reset()
    {

    }

    public void nameIDGenerator(int n)
    {
        String B;
        for (int i = 0; i < Math.pow(2, n); i++)
        {
            B = "";
            int temp = i;
            for (int j = 0; j < n; j++)
            {
                if (temp % 2 == 1) B = '1' + B;
                else B = '0' + B;
                temp = temp / 2;
            }
            //System.out.println(B);
            addToNamespace(B);
        }
        nameSpaceIndex = 0;

    }

    public void addToNamespace(String B)
    {

        nameSpace[nameSpaceIndex] = B;
        nameSpaceIndex++;

    }

    public void UpdatingM()
    {
        for (int i = 0; i < SkipSimParameters.getNameIDLength(); i++)
        {
            M[i] = M[i] * (nodeIndex - 1) + D[i];
            M[i] = M[i] / nodeIndex;
        }
    }

    public int ClosestLandmark(Node n)
    {
        double min = Double.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            if (n.getCoordinate().distance(sgo.getTG().mLandmarks.getLandmarkCoordination(i)) < min)
            {
                min = n.getCoordinate().distance(sgo.getTG().mLandmarks.getLandmarkCoordination(i));
                index = i;
            }
        }
        return index;
    }

    public void GeneratingD(Node n, SkipGraphOperations sg)
    {
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
            D[i] = 0;
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            for (int j = 0; j < SkipSimParameters.getLandmarksNum(); j++)
            {
                D[i] += n.getCoordinate().distance(sg.getTG().mLandmarks.getLandmarkCoordination(i)) * B[j][i];
            }
        }
        nodeIndex++;
    }

    public void InitializingD()
    {
        for (int i = 0; i < SkipSimParameters.getNameIDLength(); i++)
            D[i] = 0;
    }

    public void LMDS()
    {
        double[][] output = new double[2][SkipSimParameters.getSystemCapacity()];
        ClassicalScaling.lmds(adj, output);
        for (int i = 0; i < LMDS.length; i++) //Converting from two dimension to one dimension
        {
            LMDS[i] = Math.sqrt(Math.pow(output[0][i], 2) + Math.pow(output[1][i], 2));
            //System.out.println("LMDS " + i + " " +LMDS[i]);
        }
    }

    /**
     *
     * @param nameID
     * @return
     */
    public boolean isAvailable(String nameID)
    {
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            log.debug("Checking name ID availability: node={}", sgo.getTG().mNodeSet.getNode(i));
            if (!((Node) sgo.getTG().mNodeSet.getNode(i)).getNameID().isEmpty()
                    && nameID.equals(sgo.getTG().mNodeSet.getNode(i).getNameID()))
            {
                log.debug("Name ID {} is not available", nameID);
                return false;
            }
        }
        return true;
    }

    /**
     * Checks the availability of a name ID. By availability we mean that no other node possesess the same name ID.
     * @param nameID the name ID that we want to check for its availability
     * @param index the number of nodes arrived to the system till now. It only is effective in Static simulations, otherwise
     *              can be left as -1.
     * @return TRUE if the name ID is available, FALSE otherwise
     */
    public boolean isAvailable(String nameID, int index)
    {
        /*
        All the nodes came to the system, it is going to assign a name id to a reterived node
        OR
        Simulation is dynamic or blockchain, and nodes may arrive out of their index order
         */
        if (index == -1
        || SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.DYNAMIC)
                || SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.BLOCKCHAIN))
            index = SkipSimParameters.getSystemCapacity();
        for (int i = 0; i < index; i++)
        {
            // System.out.println("name id = " + sg.getTG().mNodeSet.getNode(i).nameID);
            Node node = (Node) sgo.getTG().mNodeSet.getNode(i);
            if (!node.getNameID().isEmpty() && nameID.equals(sgo.getTG().mNodeSet.getNode(i).getNameID()))
            {
                //System.out.println(nameID + " is not available: DPAD");
                return false;
            }
        }

        return true;
    }

    public void print(String message)
    {
        if (SkipSimParameters.isLog()) log.debug("DPAD: {}", message);
    }


    public void PrintB()
    {
        if (log.isDebugEnabled()) {
            StringBuilder matrix = new StringBuilder("B matrix: [");
            for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
            {
                matrix.append("Row").append(i).append("=[");
                for (int j = 0; j < SkipSimParameters.getLandmarksNum(); j++)
                {
                    matrix.append(B[i][j]);
                }
                matrix.append("] ");
            }
            matrix.append("]");
            log.debug("{}", matrix.toString());
        }
    }

    public void nameIDGeneration(SkipGraphOperations sg)
    {
        sgo = sg;
    }

    public void nameIDGeneration()
    {

    }

    public int findMin()
    {
        int index = 0;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < LMDS.length; i++)
        {
            if (min > LMDS[i])
            {
                min = LMDS[i];
                index = i;
            }
        }
        LMDS[index] = Double.MAX_VALUE;
        return index;
    }

    public void printADJ()
    {
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            StringBuilder row = new StringBuilder();
            for (int j = 0; j < SkipSimParameters.getSystemCapacity(); j++)
            {
                row.append((int) adj[i][j]).append("  ");
            }
            log.debug("ADJ[{}]: {}", i, row.toString());
        }
    }

    public void makingAdj()
    {
        adj = new double[SkipSimParameters.getSystemCapacity()][SkipSimParameters.getSystemCapacity()];
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
            for (int j = 0; j < SkipSimParameters.getSystemCapacity(); j++)
            {
                adj[i][j] = ((Node) sgo.getTG().mNodeSet.getNode(i)).getCoordinate().distance(((Node) sgo.getTG().mNodeSet.getNode(j)).getCoordinate());
            }
    }

    public String findRegion(Point p)
    {
        if (p.x < SkipSimParameters.getDomainSize() * 0.5)
        {
            if (p.y < SkipSimParameters.getDomainSize() * 0.25) return "000";
            else if (p.y < SkipSimParameters.getDomainSize() * 0.5) return "001";
            else if (p.y < SkipSimParameters.getDomainSize() * 0.75) return "010";
            else return "011";
        }
        else
        {
            if (p.y < SkipSimParameters.getDomainSize() * 0.25) return "100";
            else if (p.y < SkipSimParameters.getDomainSize() * 0.5) return "101";
            else if (p.y < SkipSimParameters.getDomainSize() * 0.75) return "110";
            else return "111";
        }
    }


    public void InitializingB()
    {
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            for (int j = 0; j < (SkipSimParameters.getLandmarksNum() - i); j++)
            {
                B[j][i] = 1;
            }
            for (int j = SkipSimParameters.getLandmarksNum() - i; j < SkipSimParameters.getLandmarksNum(); j++)
            {
                B[j][i] = 0;
            }
        }

    }

    public String NameIDGenerating(int closestLandmarkIndex, SkipGraphOperations sg)
    {
        return new String();
    }

    public String NameIDGenerating(int closestLandmarkIndex, int index)
    {
        return new String();
    }

    public String NameIDGenerating()
    {
        return new String();
    }

    public void InitilizingM()
    {
        for (int i = 0; i < SkipSimParameters.getNameIDLength(); i++)
            M[i] = 0;
    }


    public String extend(String nameID)
    {
        String[] Split = nameID.split("_");
        nameID = new String();
        for (int i = 0; i < Split.length; i++)
        {
            if (!extendSet.contains(Split[i] + "0")) return Split[i] + "0";
            else if (!extendSet.contains(Split[i] + "1"))
            {
                return Split[i] + "1";
            }
            else
            {
                nameID = nameID + Split[i] + "0_";
                nameID = nameID + Split[i] + "1";
                if (i != Split.length - 1) nameID = nameID + "_";
            }
        }
        return extend(nameID);
    }


    public String Algorithm(Node n, SkipGraphOperations sg)
    {
        return new String();
    }

    public String Algorithm(Node n, SkipGraphOperations sg, int index)
    {
        return new String();
    }

    public String RandomNameIDAssignment1()
    {
        return new String();
    }

    public String RandomNameIDAssignment2(int closestLandmarkIndex, SkipGraphOperations sg)
    {
        return new String();
    }

    public String RandomNameIDAssignment3(Node n)
    {
        return new String();
    }

}
