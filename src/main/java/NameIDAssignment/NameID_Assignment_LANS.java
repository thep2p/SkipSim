package NameIDAssignment;

import Simulator.SkipSimParameters;
import SkipGraph.Node;
import SkipGraph.SkipGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by yhass on 7/10/2017.
 */
public class NameID_Assignment_LANS extends NameIDAssignment
{
    private static final Logger log = LoggerFactory.getLogger(NameID_Assignment_LANS.class);

    private static double[][] landmarksCoordination = new double[SkipSimParameters.getLandmarksNum()][SkipSimParameters.getLandmarksNum()];
    private static double[] averageSearches = new double[SkipSimParameters.getTopologies()];
    private static double   numberOfSearches;
    private boolean[] alreadyAssignedNameID;

    @Override
    public String Algorithm(Node n, SkipGraphOperations inputSGO, int index)
    {
        alreadyAssignedNameID = new boolean[(int) Math.pow(2, SkipSimParameters.getNameIDLength())];
        Arrays.fill(alreadyAssignedNameID, false);
        sgo = inputSGO;

        if (initializationLock)
        {
            numberOfSearches = 0;
            prefixAnalysisInit();
            sgo.getTG().mLandmarks.twoMeanClustringBasedOnPosition(new String(), -1);
            //sgo.getTG().mLandmarks.printDynamicPrefix();
            nameIDGenerator(SkipSimParameters.getNameIDLength());
            setCoordination();
            initializationLock = false;
        }


        int closetLandmarkIndex = ClosestLandmark(n);
        String prefix = sgo.getTG().mLandmarks.getDynamicPrefix(closetLandmarkIndex);
        int closestSlopIndex   = theClosestSlopIndex(n, closetLandmarkIndex);
        String body = sgo.getTG().mLandmarks.getDynamicPrefix(closestSlopIndex);

        int latencyToClosest = (int) sgo.getTG().mLandmarks.getLandmarkCoordination(closetLandmarkIndex).distance(n.getCoordinate());
        int latencyToSecond  = (int) sgo.getTG().mLandmarks.getLandmarkCoordination(closestSlopIndex).distance(n.getCoordinate());
        //String binaryLatencyToClosest = Integer.toBinaryString((int)Math.sqrt(Math.pow(latencyToClosest,2) + Math.pow(latencyToSecond,2)));


        /*
        Latency to the closest landmark
         */
        int totalLatency = 0;
        for(int i = 0; i < SkipSimParameters.getLandmarksNum() ; i++)
        {
            totalLatency += Math.pow(sgo.getTG().mLandmarks.getLandmarkCoordination(i).distance(n.getCoordinate()) - landmarksCoordination[closetLandmarkIndex][i], 2);
        }
        totalLatency = (int) Math.sqrt(totalLatency);
        String binaryLatencyToClosest = Integer.toBinaryString(totalLatency);

        while (binaryLatencyToClosest.length() < SkipSimParameters.getNameIDLength())
        {
            binaryLatencyToClosest  = "0" + binaryLatencyToClosest;
        }
        for(int i = 0; body.length() < SkipSimParameters.getNameIDLength() ; i++)
        {
            body = body + binaryLatencyToClosest.charAt(i);
        }


        String nameID = checkAvailability(prefix, body, closetLandmarkIndex, index);

        //System.out.println("name ID " + nameID + " prefix " + prefix + " latency to closest " + latencyToClosest);

        if (index == SkipSimParameters.getSystemCapacity() - 1)
        {
            averageSearches[SkipSimParameters.getCurrentTopologyIndex()-1] = numberOfSearches / SkipSimParameters.getSystemCapacity();
            if(SkipSimParameters.isStaticSimulation())
            {
                numberOfSearches = 0;
                //concludePrefixAnalysisOfThisTopology();
                initializationLock = true;
                reset();
            }
            if(SkipSimParameters.getCurrentTopologyIndex() == SkipSimParameters.getTopologies())
            {
                double mean = 0;
                for(int i = 0; i < SkipSimParameters.getTopologies() ; i++)
                {
                    mean += averageSearches[i];
                }

                mean /= SkipSimParameters.getTopologies();
                double sd = 0;
                for (int i = 0; i < SkipSimParameters.getTopologies() ; i++)
                {
                    sd = sd + Math.pow(averageSearches[i] - mean, 2);
                }

                sd = Math.sqrt(sd / SkipSimParameters.getTopologies());

                log.info("Average number of searches for checking availability of a name ID: {} SD {}", mean, sd);
            }
        }
        if(nameID.isEmpty())
        {
            log.error("LANS.java: generating an empty nameID for a Node");
        }

        return nameID;


    }



    private void setCoordination()
    {
        for(int i = 0; i < SkipSimParameters.getLandmarksNum() ; i++)
        {
            for(int j = 0; j < SkipSimParameters.getLandmarksNum() ; j++)
            {
                landmarksCoordination[i][j] = sgo.getTG().mLandmarks.getLandmarkCoordination(i).distance(sgo.getTG().mLandmarks.getLandmarkCoordination(j));
            }
        }
    }

    private int theClosestSlopIndex(Node n, int closestLandmarkIndex)
    {
        double[] nodeCoordination = new double[SkipSimParameters.getLandmarksNum()];
        for(int i = 0; i < SkipSimParameters.getLandmarksNum() ; i++)
        {
            nodeCoordination[i] = sgo.getTG().mLandmarks.getLandmarkCoordination(i).distance(n.getCoordinate());
        }


        double minDistance = Double.MAX_VALUE;
        int    minIndex    = 0;
        for(int i = 0; i < SkipSimParameters.getLandmarksNum() ; i++)
        {
            if(closestLandmarkIndex == i)
                continue;
            double distance = 0;
            double[] nodeSlop = new double[SkipSimParameters.getLandmarksNum()];
            double[] closestLandmarkSlop = new double[SkipSimParameters.getLandmarksNum()];
            double sumNodeSlop = 0;
            double sumClosestLandmarkSlope  = 0;
            for(int j = 0; j < SkipSimParameters.getLandmarksNum() ; j++)
            {
                nodeSlop[j] = nodeCoordination[j] - landmarksCoordination[i][j];
                sumNodeSlop += Math.abs(nodeSlop[j]);
                closestLandmarkSlop[j] = landmarksCoordination[closestLandmarkIndex][j] - landmarksCoordination[i][j];
                sumClosestLandmarkSlope += Math.abs(closestLandmarkSlop[j]);
            }

            for(int j = 0; j < SkipSimParameters.getLandmarksNum() ; j++)
            {
                nodeSlop[j] /= sumNodeSlop;
                closestLandmarkSlop[j] /= sumClosestLandmarkSlope;
                distance += Math.pow(nodeSlop[j] - closestLandmarkSlop[j], 2);
            }

            if(distance < minDistance)
            {
                minDistance = distance;
                minIndex = i;
            }

        }
        //System.out.println("Closest " + closestLandmarkIndex + " landmarksCoordination " + minIndex);
        return minIndex;
    }

    private String checkAvailability(String prefix, String body, int closestLandmarkIndex, int index)
    {
        int right = Integer.parseInt(body, 2) + 1;
        int left = Integer.parseInt(body, 2) - 1;
        print("Enters the while true");
        int localNumberOfSearch = 0;
        while (true)
        {
            if (right < Math.pow(2, SkipSimParameters.getNameIDLength()))
            {
                localNumberOfSearch ++;
                numberOfSearches++;
                if (isAvailable((prefix + nameSpace[right]), index)) //If name id was available, return that
                {
                    //nameID = nameSpace[right];
                    //nameID = prefix + nameID;
                    //System.out.println("Num of search " + localNumberOfSearch);
                    prefixAnalysis(closestLandmarkIndex, nameSpace[right]);
                    return prefix + nameSpace[right];
                }
                else
                {
                    ArrayList<String> nameIDsOnPath = sgo.SearchForNameIDPath(prefix + nameSpace[right]);
                    for(String nameID: nameIDsOnPath)
                    {
                        if(nameID.length() > 0)
                            alreadyAssignedNameID[Integer.parseInt(extractBody(nameID, sgo), 2)] = true;
                    }
                    right++;
                    while (right < Math.pow(2, SkipSimParameters.getNameIDLength()) - 1 && alreadyAssignedNameID[right])
                        right++;
                }

            }
            if (left >= 0)
            {
                localNumberOfSearch++;
                numberOfSearches++;
                if (isAvailable((prefix + nameSpace[left]), index)) //If name id was available, return that
                {
                    //nameID = nameSpace[left];
                    //nameID = prefix + nameID;
                    //System.out.println("Num of search " + localNumberOfSearch);
                    prefixAnalysis(closestLandmarkIndex, nameSpace[left]);
                    return prefix + nameSpace[left];
                }
                else
                {
                    ArrayList<String> nameIDsOnPath = sgo.SearchForNameIDPath(prefix + nameSpace[left]);
                    for(String nameID: nameIDsOnPath)
                    {
                        if(nameID.length() > 0)
                            alreadyAssignedNameID[Integer.parseInt(extractBody(nameID, sgo), 2)] = true;
                    }
                    left--;
                    while (left > 0 && alreadyAssignedNameID[left])
                        left--;
                }
            }
            if (right >= Math.pow(2, SkipSimParameters.getNameIDLength()) && left < 0)
            {
                log.error("LANS check availability falls into infinite loop on region {} with {} nodes",
                    closestLandmarkIndex, sgo.getTG().mLandmarks.numberOfNodesInRegion(closestLandmarkIndex, sgo));
                System.exit(0);
            }
        }
    }

}
