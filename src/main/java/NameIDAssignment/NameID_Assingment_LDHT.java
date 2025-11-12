package NameIDAssignment;

import Simulator.SkipSimParameters;
import SkipGraph.Node;
import SkipGraph.SkipGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NameID_Assingment_LDHT extends NameIDAssignment
{
    private static final Logger log = LoggerFactory.getLogger(NameID_Assingment_LDHT.class);

    @Override
    public void reset()
    {
        nameSpace = new String[SkipSimParameters.getSystemCapacity()];
        nameSpaceIndex = 0;
        initializationLock = true;
    }


    public String RandomNameIDAssignment2(int closestLandmarkIndex, int nodeIndex)
    {
        String nameID = nameSpace[nodeIndex];
//		nameSpace[index] = null;
        String prefix = sgo.getTG().mLandmarks.getPrefix(closestLandmarkIndex).
                substring((int) (sgo.getTG().mLandmarks.getPrefix(closestLandmarkIndex).length() - Math.ceil(Math.log(SkipSimParameters.getLandmarksNum()))) - 1, sgo.getTG().mLandmarks.getPrefix(closestLandmarkIndex).length());
        nameID = prefix + nameID;
        log.debug("Generated name ID: {} with prefix: {} for node index: {}", nameID, prefix, nodeIndex);
        return nameID;
    }

    @Override
    public String Algorithm(Node n, SkipGraphOperations sg, int index)
    {
        sgo = sg;
        if (initializationLock)
        {
            nameIDGenerator(SkipSimParameters.getLandmarksNum());
            initializationLock = false;
        }
        String nameID = RandomNameIDAssignment2(ClosestLandmark(n), index);

        if (SkipSimParameters.isStaticSimulation() && index == SkipSimParameters.getSystemCapacity() - 1)
        {
            initializationLock = true;
            reset();
        }
        return nameID;
    }
}