package NameIDAssignment;

import DataTypes.HuffmanCode;
import DataTypes.Pair;
import Simulator.SkipSimParameters;
import SkipGraph.Node;
import SkipGraph.SkipGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class NameID_Assignment_DPAD extends NameIDAssignment
{
    private static final Logger log = LoggerFactory.getLogger(NameID_Assignment_DPAD.class);

    static Pair[] landmarksAndLatencies;

    @Override
    public void reset()
    {
        B = new int[SkipSimParameters.getLandmarksNum()][SkipSimParameters.getLandmarksNum()]; //binary matrix
        D = new double[SkipSimParameters.getNameIDLength()]; //Distances to the landmarks
        M = new double[SkipSimParameters.getNameIDLength()]; //Mean of distances to the landmarks
        nodeIndex = 0; //Number of SkipGraph.Nodes arriving to the Simulator.system so far
        nameSpace = new String[(int) Math.pow(2, SkipSimParameters.getLandmarksNum())];
        nameSpaceIndex = 0;
        initializationLock = true;
        nameID = new String();
    }

    @Override
    public void GeneratingD(Node n, SkipGraphOperations sg)
    {
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
            D[i] = n.getCoordinate().distance(sg.getTG().mLandmarks.getLandmarkCoordination(i));
        nodeIndex++;
    }


    public void InitializingB(int closestLandmarkIndex)
    {
        landmarksAndLatencies = sgo.getTG().mLandmarks.sortedIndexOfLandmarks(closestLandmarkIndex);
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            Arrays.fill(B[i], 0);
        }
        for (int j = 0; j < SkipSimParameters.getLandmarksNum(); j++)
        {
            for (int i = j; i < SkipSimParameters.getLandmarksNum(); i++)
            {
                B[landmarksAndLatencies[i].index][j] = 1;
            }
        }
        //PrintB();
    }

    @Override
    public String NameIDGenerating(int closestLandmarkIndex, int index)
    {
        nameID = new String();
        String prefix = sgo.getTG().mLandmarks.getDynamicPrefix(closestLandmarkIndex);


        for (int i = 0; i < SkipSimParameters.getNameIDLength(); i++)
        {
            if (D[i] > M[i]) nameID = nameID + "1";
            else nameID = nameID + "0";
        }


        UpdatingM();

        if (isAvailable((prefix + nameID), index)) //if name id is available
        {
            //nameID = nameSpace[Integer.parseInt(nameID, 2)];
            //nameID = SkipGraph.Landmarks.dynamicPrefix[closestLandmarkIndex] + nameID;
            prefixAnalysis(closestLandmarkIndex, nameID);
            return prefix + nameID;
        }
        else//if name id is not available
        {
            int right = Integer.parseInt(nameID, 2) + 1;
            int left = Integer.parseInt(nameID, 2) - 1;
            print("Enters the while true");
            while (true)
            {
                if (right < SkipSimParameters.getSystemCapacity())
                {
                    if (isAvailable((prefix + nameSpace[right]), index)) //If name id was available, return that
                    {
                        //nameID = nameSpace[right];
                        //nameID = prefix + nameID;
                        print("Exits that while true");
                        prefixAnalysis(closestLandmarkIndex, nameSpace[right]);
                        return prefix + nameSpace[right];
                    }
                    else right++;
                }
                if (left >= 0)
                {
                    if (isAvailable((prefix + nameSpace[left]), index)) //If name id was available, return that
                    {
                        //nameID = nameSpace[left];
                        //nameID = prefix + nameID;
                        print("Exits that while true");
                        prefixAnalysis(closestLandmarkIndex, nameSpace[left]);
                        return prefix + nameSpace[left];
                    }
                    else left--;
                }
                if (right >= SkipSimParameters.getSystemCapacity() && left < 0)
                {
                    log.error("DPAD infinite loop detected");
                    System.exit(0);
                }
            }
        }
    }

    public void InitilizingM(int closestLandmarkIndex)
    {
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
            M[i] = 0;
    }

    @Override
    public String Algorithm(Node n, SkipGraphOperations sg, int index)
    {

        sgo = sg;

        if (initializationLock)
        {
            //landmarksAndLatencies = sgo.getTG().mLandmarks.sortedIndexOfLandmarks(sgo.getTG().mLandmarks.totalPairwiseLatencies());
            //zeroPrefixAnalysisInit();
            prefixAnalysisInit();
            //sg.getTG().mLandmarks.twoMeanClustringBasedOnPosition(new String(), -1);
            //sg.getTG().mLandmarks.printDynamicPrefix();

            sg.getTG().mLandmarks.upDateDynamicPrefix();
            new HuffmanCode().buildThePrefix(sg.getTG().mLandmarks.getFreq(), sg);
            nameIDGenerator(SkipSimParameters.getNameIDLength());
            initializationLock = false;
            //InitializingB();
            //InitilizingM();
        }
        InitilizingM(ClosestLandmark(n));
        //InitializingB(ClosestLandmark(n));
        InitializingD();
        GeneratingD(n, sg);
        String nameid = NameIDGenerating(ClosestLandmark(n), index);
        if (SkipSimParameters.isStaticSimulation() && index == SkipSimParameters.getSystemCapacity() - 1)
        {
            //concludePrefixAnalysisOfThisTopology();
            initializationLock = true;
            reset();
        }

        return nameid;


    }


}
