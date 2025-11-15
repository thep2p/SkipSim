package SkipGraph;

import DataTypes.Pair;
import Simulator.SkipSimParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;


public class Landmarks implements Serializable
{
    private static final Logger log = LoggerFactory.getLogger(Landmarks.class);

    private Point[] landmarkSet;
    private String[] prefix = {"0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011", "1100", "1101", "1110", "1111"};
    private String[] dynamicPrefix;
    private int[] freq;
    private String[] dynamicPrefixExcludingIndex;

    //////////////////////////////
    public Landmarks()
    {
        if(SkipSimParameters.isLog())
            log.debug("Landmarks: Number of landmarks: {}, Size of landmarks set: {}",
                SkipSimParameters.getLandmarksNum(), SkipSimParameters.getLandmarksNum());
        landmarkSet = new Point[SkipSimParameters.getLandmarksNum()];
        freq = new int[SkipSimParameters.getLandmarksNum()];
        dynamicPrefix = new String[SkipSimParameters.getLandmarksNum()];
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            dynamicPrefix[i] = new String();
        }
    }

    public int[] getFreq()
    {
        return freq;
    }

    /**
     * freq[i] = value
     *
     * @param i
     * @param value
     */
    public void setFreq(int i, int value)
    {
        freq[i] = value;
    }

    public String getDynamicPrefix(int i)
    {
        return dynamicPrefix[i];
    }

    public void setDynamicPrefix(int i, String s)
    {
        dynamicPrefix[i] = s;
    }

    public int dynamicPrefixLength(int i)
    {
        return dynamicPrefix[i].length();
    }

    public String getPrefix(int i)
    {
        return prefix[i];
    }

    public void setPrefix(int i, String s)
    {
        prefix[i] = s;
    }

    public int prefixLength()
    {
        return prefix.length;
    }

    public Point getLandmarkCoordination(int i)
    {
        return landmarkSet[i];
    }

    public void setLandmarkCoordination(int i, Point value)
    {
        landmarkSet[i] = value;
    }

    public int setLength()
    {
        return landmarkSet.length;
    }

    public void reset()
    {
        //LandmarkSet = new Point[10];
        landmarkSet = new Point[SkipSimParameters.getLandmarksNum()];
    }

    public void landmarkGeneration()
    {
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            Random random = new Random();
            Point p = new Point();
            p.x = random.nextInt((int) (SkipSimParameters.getDomainSize()));
            p.y = random.nextInt((int) (SkipSimParameters.getDomainSize()));
            setLandmarkCoordination(i, p);

        }
    }

    public void updateLandmarks(Point[] newLandmarks)
    {
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            landmarkSet[i] = newLandmarks[i];
        }
    }

    public int numberOfNodesInRegion(int regionIndex, SkipGraphOperations sgo)
    {
        int number = 0;
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            if (((Node) sgo.getTG().mNodeSet.getNode(i)).getClosetLandmarkIndex(this) == regionIndex)
            {
                number++;
            }
        }
        return number;

    }

    public void upDateDynamicPrefix()
    {

        sumOfDistances();
        //distanceToTheMostDense();
    }

    private void sumOfDistances()
    {
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
            freq[i] = 0;


        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            for (int j = 0; j < SkipSimParameters.getLandmarksNum(); j++)
            {
                freq[i] += Math.pow(landmarkSet[i].distance(landmarkSet[j]), 2);
            }
        }

//          int min = Integer.MAX_VALUE;
//          int minIndex = 0;
//          int max = Integer.MIN_VALUE;
//          int maxIndex = 0;
//
//          for(int i = 0 ; i < Simulator.system.getLandmarksNum() ; i++)
//              {
//                  if(freq[i] < min)
//                      {
//                          min = freq[i];
//                          minIndex = i;
//
//                      }
//                   else if(freq[i] > max)
//                   {
//                       max = freq[i];
//                       maxIndex = i;
//                   }
//              }
//
//          for(int i = 0 ; i < Simulator.system.getLandmarksNum() ; i++)
//              freq[i] = max - freq[i];
    }

    public void printDynamicPrefix()
    {
        if (log.isDebugEnabled()) {
            StringBuilder prefixes = new StringBuilder();
            for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
            {
                prefixes.append(String.format("[%d:%s] ", i, dynamicPrefix[i]));
            }
            log.debug("Dynamic prefixes: {}", prefixes.toString().trim());
        }
    }

    public void resetDynamicPrefixExcludingIndex()
    {
        dynamicPrefixExcludingIndex = new String[SkipSimParameters.getLandmarksNum()];
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            dynamicPrefixExcludingIndex[i] = new String();
        }
    }

    public String getDynamicPrefixExcludingIndex(int index)
    {
        return dynamicPrefixExcludingIndex[index];
    }

    public void printDynamicPrefixExcludingIndex()
    {
        if (log.isDebugEnabled()) {
            StringBuilder prefixes = new StringBuilder();
            for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
            {
                prefixes.append(String.format("[%d:%s] ", i, dynamicPrefix[i]));
            }
            log.debug("Dynamic prefixes (excluding index): {}", prefixes.toString().trim());
        }
    }

    public void twoMeanClustringBasedOnPosition(String prefix, int excludingIndex)
    {
        //Random randomX = new Random();
        //Random randomY = new Random();
        Point zeroClusterCenter = new Point(0, 0);
        Point oneClusterCenter;


        if (prefix.length() == 0)
        {
            zeroClusterCenter = new Point(0, 0);
            oneClusterCenter = new Point(SkipSimParameters.getDomainSize() - 100, SkipSimParameters.getDomainSize() - 100);
        }

        else
        {
            int i;
            for (i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
            {

                if ((excludingIndex < 0 && dynamicPrefix[i].startsWith(prefix)) || (excludingIndex >= 0 && i != excludingIndex && dynamicPrefixExcludingIndex[i].startsWith(prefix)))
                {
                    zeroClusterCenter = landmarkSet[i];
                    break;
                }
            }

            Double maxDistance = Double.MIN_VALUE;
            oneClusterCenter = zeroClusterCenter;

            for (int j = 0; j < SkipSimParameters.getLandmarksNum(); j++)
            {
                if ((excludingIndex < 0 && dynamicPrefix[j].startsWith(prefix))
                        || (excludingIndex >= 0 && j != excludingIndex && dynamicPrefixExcludingIndex[i].startsWith(prefix)))
                {
                    if (j != i && landmarkSet[j].distance(oneClusterCenter) > maxDistance)
                    {
                        oneClusterCenter = landmarkSet[j];
                        maxDistance = landmarkSet[j].distance(oneClusterCenter);
                    }

                }
            }


        }

        int[] clusterIndex = new int[SkipSimParameters.getLandmarksNum()];
        Arrays.fill(clusterIndex, -1);
        int zeroClusterSize = 0;
        int oneClusterSize = 0;
        boolean isAtLeastOneClusterUpdated;

        do
        {
            isAtLeastOneClusterUpdated = false;
            for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
            {
                if (prefix.length() == 0 || dynamicPrefix[i].startsWith(prefix) || (excludingIndex >= 0 && i != excludingIndex && dynamicPrefixExcludingIndex[i].startsWith(prefix)))
                {
                    if (landmarkSet[i].distance(zeroClusterCenter) < landmarkSet[i].distance(oneClusterCenter) && clusterIndex[i] != 0)
                    {
                        isAtLeastOneClusterUpdated = true;
                        clusterIndex[i] = 0;
                    }
                    else if (landmarkSet[i].distance(zeroClusterCenter) >= landmarkSet[i].distance(oneClusterCenter) && clusterIndex[i] != 1)
                    {
                        isAtLeastOneClusterUpdated = true;
                        clusterIndex[i] = 1;
                    }
                }
            }


            zeroClusterCenter = new Point(0, 0);
            oneClusterCenter = new Point(0, 0);
            for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
            {
                if (i == excludingIndex)
                {
                    continue;
                }
                if (clusterIndex[i] == 0)
                {
                    zeroClusterSize++;
                    zeroClusterCenter.x += landmarkSet[i].x;
                    zeroClusterCenter.y += landmarkSet[i].y;
                }
                else if (clusterIndex[i] == 1)
                {
                    oneClusterCenter.x += landmarkSet[i].x;
                    oneClusterCenter.y += landmarkSet[i].y;
                    oneClusterSize++;
                }
            }

            if (!isAtLeastOneClusterUpdated)
            {
                for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
                {
                    if (clusterIndex[i] == 0)
                    {
                        if (excludingIndex < 0)
                        {
                            if (dynamicPrefix[i].length() == 0)
                            {
                                dynamicPrefix[i] = "0";
                            }
                            else
                            {
                                dynamicPrefix[i] = dynamicPrefix[i] + "0";
                            }
                        }

                        else
                        {
                            if (i == excludingIndex)
                            {
                                continue;
                            }
                            else
                            {
                                if (dynamicPrefixExcludingIndex[i].length() == 0)
                                {
                                    dynamicPrefixExcludingIndex[i] = "0";
                                }
                                else
                                {
                                    dynamicPrefixExcludingIndex[i] = dynamicPrefixExcludingIndex[i] + "0";
                                }
                            }

                        }


                    }
                    else if (clusterIndex[i] == 1)
                    {
                        if (excludingIndex < 0)
                        {
                            if (dynamicPrefix[i].length() == 0)
                            {
                                dynamicPrefix[i] = "1";
                            }
                            else
                            {
                                dynamicPrefix[i] = dynamicPrefix[i] + "1";
                            }
                        }

                        else
                        {
                            if (i == excludingIndex)
                            {
                                continue;
                            }
                            else
                            {
                                if (dynamicPrefixExcludingIndex[i].length() == 0)
                                {
                                    dynamicPrefixExcludingIndex[i] = "1";
                                }
                                else
                                {
                                    dynamicPrefixExcludingIndex[i] = dynamicPrefixExcludingIndex[i] + "1";
                                }
                            }

                        }
                    }
                }
                if (zeroClusterSize > 1)
                {
                    //System.out.println("on call " + prefix);
                    //printDynamicPrefix();
                    twoMeanClustringBasedOnPosition(prefix + "0", excludingIndex);
                }
                if (oneClusterSize > 1)
                {
                    //System.out.println("on call " + prefix);
                    //printDynamicPrefix();
                    twoMeanClustringBasedOnPosition(prefix + "1", excludingIndex);
                }
                return;
            }


            zeroClusterCenter.x /= (double) zeroClusterSize;
            zeroClusterCenter.y /= (double) zeroClusterSize;

            oneClusterCenter.x /= (double) oneClusterSize;
            oneClusterCenter.y /= (double) oneClusterSize;

            zeroClusterSize = 0;
            oneClusterSize = 0;
        } while (isAtLeastOneClusterUpdated);

    }


    private void distanceToTheMostDense()
    {
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
            freq[i] = 0;


        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            for (int j = 0; j < SkipSimParameters.getLandmarksNum(); j++)
            {
                freq[i] += Math.pow(landmarkSet[i].distance(landmarkSet[j]), 2);
            }
        }

        int min = Integer.MAX_VALUE;
        int minIndex = 0;
//	int max = Integer.MIN_VALUE;
//	int maxIndex = 0;

        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            if (freq[i] < min)
            {
                min = freq[i];
                minIndex = i;

            }
//       else if(freq[i] > max)
//       {
//    	   max = freq[i];
//    	   maxIndex = i;
//       }
        }


        //System.out.println(" ");
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {

            //freq[i] = (int)(max - landmarkSet[i].distance(landmarkSet[minIndex]));
            freq[i] = (int) landmarkSet[i].distance(landmarkSet[minIndex]);
            if (freq[i] == 0)
            {
                freq[i] = 1;
            }
            //System.out.print(freq[i] + " ");


        }


        //System.out.println(" ");

    }

    public double[] totalPairwiseLatencies()
    {
        double[] totalLatencies = new double[SkipSimParameters.getLandmarksNum()];
        Arrays.fill(totalLatencies, 0);

        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
            for (int j = 0; j < SkipSimParameters.getLandmarksNum(); j++)
            {
                totalLatencies[i] += landmarkSet[i].distance(landmarkSet[j]);
            }
        return totalLatencies;
    }

    /**
     * @param pairwiseLatency
     * @return sorted indices of landmarks based on their pairwise latencies
     */
    public Pair[] sortedIndexOfLandmarks(double[] pairwiseLatency)
    {
        Pair[] landmarksAndTotalLatency = new Pair[SkipSimParameters.getLandmarksNum()];

        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            landmarksAndTotalLatency[i] = new Pair(i, pairwiseLatency[i]);
        }

        Arrays.sort(landmarksAndTotalLatency);
        return landmarksAndTotalLatency;
    }

    /**
     * @param index
     * @return sorted indices of landmarks based on their latencies with the input landmark index
     */
    public Pair[] sortedIndexOfLandmarks(int index)
    {
        Pair[] landmarksAndTotalLatency = new Pair[SkipSimParameters.getLandmarksNum()];


        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            landmarksAndTotalLatency[i] = new Pair(i, landmarkSet[i].distance(landmarkSet[index]));
        }

        Arrays.sort(landmarksAndTotalLatency);
        return landmarksAndTotalLatency;
    }

    /**
     * Generates random landmarks
     */
    public void generatingLandmarks()
    {
        Random randomX = new Random();
        Random randomY = new Random();
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            Point p = new Point();
            p.x = randomX.nextInt((int) (SkipSimParameters.getDomainSize()));
            p.y = randomY.nextInt((int) (SkipSimParameters.getDomainSize()));
            setLandmarkCoordination(i, p);
            //System.out.println("Landmarks.java: Generating landmark " + i + " " + getLandmarkCoordination(i).getLocation().toString());
        }
    }


}

