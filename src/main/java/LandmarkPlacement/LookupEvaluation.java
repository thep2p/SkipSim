package LandmarkPlacement;

import DataTypes.Constants;
import DataTypes.Message;
import Simulator.SkipSimParameters;
import SkipGraph.Node;
import SkipGraph.SkipGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;


public class LookupEvaluation
{
    private static final Logger log = LoggerFactory.getLogger(LookupEvaluation.class);

    public static double[] DistanceMeans = new double[SkipSimParameters.getTopologies()];
    private static double[] NameIDsMeans = new double[SkipSimParameters.getTopologies()];
    private static double[] NumIDsMeans = new double[SkipSimParameters.getTopologies()];
    private double averageSearchPathOFThisTopology;
    private static double averageSearchPath = 0;
    private SkipGraphOperations sgo;


//		public int randomLookUpTest()
//		{
//
//     	  Random random = new Random();
//     	  int randomIndex = random.nextInt(Simulator.system.getSystemCapacity()-1);
//     	  String nameID = sgo.getTG().mNodeSet.getNode(randomIndex).nameID;
//     	  int startIndex = random.nextInt(Simulator.system.getSystemCapacity()-1);
//
//
//
//
//     	  while(sgo.commonPrefixLength(nameID, sgo.getTG().mNodeSet.getNode(startIndex).nameID)  < 1)
//     		 nameID = sgo.getTG().mNodeSet.getNode(random.nextInt(Simulator.system.getSystemCapacity()-1)).nameID;
//
//
//
//     	  int result = sgo.SearchByNameID(nameID, startIndex, sgo.getTG().mNodeSet.getNode(startIndex).getLookup(0, 1), sgo.getTG().mNodeSet.getNode(startIndex).getLookup(0, 0), 0, new DataTypes.Message());
//     	  if(result < 0)
//     	  {
//     		 System.out.println("Serach for " + nameID + " failed!!");
//     		 return Integer.MAX_VALUE;
//     	  }
//     	  String nameResult = sgo.getTG().mNodeSet.getNode(result).nameID;
//
//     	  if(nameResult == nameID)
//     	  {
//     	    //System.out.println("Serach for " + nameID + " is " + result + " with name ID  " + nameResult);
//     	    return sgo.commonPrefixLength(nameID, sgo.getTG().mNodeSet.getNode(startIndex).nameID);
//     	  }
//
//     	 return Integer.MAX_VALUE;
//
//
//		}


    public LookupEvaluation(SkipGraphOperations sgo)
    {
        this.sgo = sgo;
        averageSearchPathOFThisTopology = 0;
    }

    public void randomLookUpTest(int iterations)
    {
        for (int i = 0; i < iterations; i++)
        {
            Random random = new Random();
            int target = random.nextInt(SkipSimParameters.getSystemCapacity() - 1);
            while (((Node) sgo.getTG().mNodeSet.getNode(target)).isOffline()) target = random.nextInt(SkipSimParameters.getSystemCapacity() - 1);
            String nameID = ((Node) sgo.getTG().mNodeSet.getNode(target)).getNameID();

            int initiator = random.nextInt(SkipSimParameters.getSystemCapacity() - 1);
            while (((Node) sgo.getTG().mNodeSet.getNode(initiator)).isOffline() || initiator == target) initiator = random.nextInt(SkipSimParameters.getSystemCapacity() - 1);
            //System.out.println("-----------------------------------------------------------");
            //System.out.println("Search for " + nameID + " from " + SkipGraph.Nodes.nodeSet[j].nameID);
            Node nodeSearchInitiator = (Node) sgo.getTG().mNodeSet.getNode(initiator);
            int result = sgo.SearchByNameID(nameID,
                    nodeSearchInitiator,
                    sgo.getTG().mNodeSet,
                    sgo.getTG().mNodeSet.getNode(initiator).getLookup(0, 1),
                    sgo.getTG().mNodeSet.getNode(initiator).getLookup(0, 0),
                    0,
                    new Message(),
                    new ArrayList<>());
//            int result2 = sgo.SearchByNameID2(nameID, initiator);
//            if (result != result2)
//            {
//                System.out.println("Serach for " + nameID + " failed!!" + "Result: " + sgo.getTG().mNodeSet.getNode(result).nameID + " Result2: " + sgo.getTG().mNodeSet
//                        .getNode(result2).nameID + "\n index: " + result + " index2: " + result2 + "\n" + sgo.getTG().mNodeSet.getNode(result).isOffline() + " " + sgo.getTG().mNodeSet.getNode(result2)
//                                                                                                                                                                                       .isOffline());
//                System.exit(0);
//            }
            //else
            //	 System.out.println("Serach for " + nameID + " Match!!!" + "Result: " + sgo.getTG().mNodeSet.getNode(result).nameID + " Result2: " + sgo.getTG().mNodeSet.getNode(result2).nameID);
            String nameResult = sgo.getTG().mNodeSet.getNode(result).getNameID();


            //System.out.println("Serach for " + nameID + " is " + result + " with name ID  " + nameResult);

        }

    }

    private double randomLookUpTest(int searchIndex, int startIndex, String Operation)
    {

        /*
        reseting the total time
         */
        sgo.getTG().mNodeSet.resetTotalTime();

        if (Operation.contains("nameID"))
        {
            String nameID = sgo.getTG().mNodeSet.getNode(searchIndex).getNameID();
            //int result = sgo.SearchByNameID(nameID, startIndex, sgo.getTG().mNodeSet.getNode(startIndex).getLookup(0, 1), sgo.getTG().mNodeSet.getNode(startIndex).getLookup(0, 0), 0, new DataTypes.Message());
            Node nodeSearchInitiator = (Node) sgo.getTG().mNodeSet.getNode(startIndex);
            int result = sgo.SearchByNameID(nameID,
                    nodeSearchInitiator,
                    sgo.getTG().mNodeSet,
                    sgo.getTG().mNodeSet.getNode(startIndex).getLookup(0,1),
                    sgo.getTG().mNodeSet.getNode(startIndex).getLookup(0,0),
                    0,
                    new Message(),
                    new ArrayList<>());
            if (result < 0)
            {
                log.warn("Name ID search failed for nameID: {}", nameID);
            }
        }
        else
        {
            Message m = new Message();
            //int numID = sgo.getTG().mNodeSet.getNode(searchIndex).getNumID();
            int numID = sgo.getTG().mNodeSet.getNode(searchIndex).getNumID();
            Node nodeSearchInitiator = (Node) sgo.getTG().mNodeSet.getNode(startIndex);

            /*
            determining the search direction
             */
            int searchDirection = SkipGraphOperations.LEFT_SEARCH_DIRECTION;
            if(nodeSearchInitiator.getNumID() >= numID)
            {
                searchDirection = SkipGraphOperations.RIGHT_SEARCH_DIRECTION;
            }
            int result = sgo.SearchByNumID(numID,
                    nodeSearchInitiator,
                    m,
                    SkipSimParameters.getLookupTableSize() - 1,
                    Constants.SkipGraphOperation.STATIC_SIMULATION_TIME,
                    sgo.getTG().mNodeSet,
                    searchDirection);
            if (sgo.getTG().mNodeSet.getNode(result).getNumID() != numID)
            {
                log.warn("Numerical ID search failed - target: {}, initiator: {}, result: {} with numID: {}",
                    numID, searchIndex, result, sgo.getTG().mNodeSet.getNode(result).getNumID());
                m.printSearchPath(sgo.getTG().mNodeSet, true);
            }
        }

        return sgo.getTG().mNodeSet.getTotalTime();
    }

    public void evaluator(int iterations, String Operation)
    {

        final String fileName = "LookupRandomSeed" + SkipSimParameters.getSystemCapacity() + "_" + Math.max(SkipSimParameters.getSearchByNameID(), SkipSimParameters.getSearchByNumericalID()) + ".txt";
        if (!FileCheck(fileName))
        {
            //System.out.println("FileCheckFalse");
            SeedGenerator(iterations, fileName);
        }
        try
        {
            Random random = new Random();
            BufferedReader in = new BufferedReader(new FileReader(fileName));
            double Mean = 0;
            double SD = 0;
            for (int i = 0; i < iterations; i++)
            {
                int a = Integer.parseInt(in.readLine());
                int b = Integer.parseInt(in.readLine());
                //int a = random.nextInt(Simulator.system.getSystemCapacity() - 1);
                //int b = random.nextInt(Simulator.system.getSystemCapacity() - 1);
                //while (sgo.getTG().mNodeSet.getNode(a).closestLandmark(sgo.getTG().mLandmarks) == sgo.getTG().mNodeSet.getNode(b).closestLandmark(sgo.getTG().mLandmarks))
                 //   b = random.nextInt(Simulator.system.getSystemCapacity() - 1);
                Mean += randomLookUpTest(a, b, Operation);
                averageSearchPathOFThisTopology += sgo.getTG().mNodeSet.getSearchPathLatency().size();

            }

            //in.close();
            Mean /= iterations;
            averageSearchPathOFThisTopology /= iterations;
            averageSearchPath += averageSearchPathOFThisTopology;
            if (Operation.contains("nameID")) NameIDsMeans[SkipSimParameters.getCurrentTopologyIndex() - 1] = Mean;
            else NumIDsMeans[SkipSimParameters.getCurrentTopologyIndex() - 1] = Mean;
            log.info("Average latency of {} random searches {} for topology {}: {} (avg search path: {})",
                iterations, Operation, SkipSimParameters.getCurrentTopologyIndex(), (int) Mean, averageSearchPathOFThisTopology);


            if (SkipSimParameters.getTopologies() == SkipSimParameters.getCurrentTopologyIndex())
            {
                Mean = 0;
                for (int i = 0; i < SkipSimParameters.getTopologies(); i++)
                {
                    if (Operation.contains("nameID")) Mean += NameIDsMeans[i];
                    else Mean += NumIDsMeans[i];
                }
                Mean = Mean / SkipSimParameters.getTopologies();

                for (int i = 0; i < SkipSimParameters.getTopologies(); i++)
                {
                    if (Operation.contains("nameID")) SD += Math.pow(Mean - NameIDsMeans[i], 2);
                    else SD += Math.pow(Mean - NumIDsMeans[i], 2);

                    //System.out.println(Means[i]);
                }
                SD = Math.sqrt(SD / SkipSimParameters.getTopologies());

                Mean = Double.parseDouble(new DecimalFormat("##.##").format(Mean));
                SD = Double.parseDouble(new DecimalFormat("##.##").format(SD));
                log.info("Total average latency for {}: {} (SD: {}, avg search path: {})",
                    Operation, Mean, SD, averageSearchPath/SkipSimParameters.getTopologies());

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
//        catch (FileNotFoundException e)
//        {
//            // TODO Auto-generated catch Block
//            e.printStackTrace();
//        }
//        catch (NumberFormatException e)
//        {
//            // TODO Auto-generated catch Block
//            e.printStackTrace();
//        }
//        catch (IOException e)
//        {
//            // TODO Auto-generated catch Block
//            e.printStackTrace();
//        }


    }

    private void SeedGenerator(int number, String fileName)
    {
        FileWriter fstream;
        try
        {
            Random random = new Random();
            fstream = new FileWriter(fileName);
            BufferedWriter out = new BufferedWriter(fstream);
            for (int i = 0; i < number; i++)
            {
                int pervious = random.nextInt(SkipSimParameters.getSystemCapacity() - 1);
                out.write(String.valueOf(pervious));
                out.newLine();
                int next = random.nextInt(SkipSimParameters.getSystemCapacity() - 1);
                while (next == pervious) next = random.nextInt(SkipSimParameters.getSystemCapacity() - 1);
                out.write(String.valueOf(next));
                out.newLine();
            }


            out.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch Block
            e.printStackTrace();
        }

    }

    private boolean FileCheck(String fileName)
    {

        boolean flag = true;
        try
        {

            File myFile = new File(fileName);

            BufferedReader in = new BufferedReader(new FileReader(myFile));
        }
        catch (IOException e)
        {
            //return false;
            flag = false;
            //	System.out.println("Not find!");
        }


        return flag;
    }

    private double meanDistanceToNeighbors(int index)
    {
        ArrayList<Integer> neighborsList = new ArrayList<>();
        double mean = 0;
        int number = 0;
        for (int i = SkipSimParameters.getLookupTableSize() - 1; i > -1; i--)
        {
            for (int j = 0; j < 2; j++)
            {
                int neighbor = sgo.getTG().mNodeSet.getNode(index).getLookup(i, j);
                if (neighbor != -1)// && !neighborsList.contains(neighbor))
                {
                    neighborsList.add(neighbor);
                    mean += ((Node) sgo.getTG().mNodeSet.getNode(index)).getCoordinate().distance(((Node) sgo.getTG().mNodeSet.getNode(neighbor)).getCoordinate());
                    number++;
                }
            }

        }

        return mean / number;
    }

    public void meanDistanceEvaluate()
    {
        int mean = 0;
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            mean += meanDistanceToNeighbors(i);
        }

        mean /= SkipSimParameters.getSystemCapacity();
        DistanceMeans[SkipSimParameters.getCurrentTopologyIndex() - 1] = mean;
        //System.out.println("Mean Distance to neighbors " + mean);


        if (SkipSimParameters.getTopologies() == SkipSimParameters.getCurrentTopologyIndex())
        {
            mean = 0;
            int SD = 0;
            for (int i = 0; i < SkipSimParameters.getTopologies(); i++)
                mean += DistanceMeans[i];

            mean = mean / SkipSimParameters.getTopologies();

            for (int i = 0; i < SkipSimParameters.getTopologies(); i++)
            {
                SD += Math.pow(mean - DistanceMeans[i], 2);
                //System.out.println(Means[i]);
            }
            SD = (int) Math.sqrt(SD / SkipSimParameters.getTopologies());

            log.info("Average distance of each node to its neighbors: {} (SD: {})", mean, SD);
        }


    }


}
