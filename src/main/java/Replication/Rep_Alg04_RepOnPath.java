package Replication;

import Simulator.SkipSimParameters;
import SkipGraph.Node;
import SkipGraph.SkipGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Random;


public class Rep_Alg04_RepOnPath extends Replication
{
    private static final Logger log = LoggerFactory.getLogger(Rep_Alg04_RepOnPath.class);
    private boolean adaptiveReplication;

    public Rep_Alg04_RepOnPath(boolean adaptiveReplication)
    {
        this.adaptiveReplication = adaptiveReplication;
    }

    private void ReplicateOnPathGenerator(int dataOwnerIndex)
    {
        Random random = new Random();
        int dataOwner = random.nextInt(SkipSimParameters.getSystemCapacity());
        int searchDest = sgo.getTG().mNodeSet.getNode(dataOwner).getNumID();
        int rep = 0;
        while (rep <= SkipSimParameters.getReplicationDegree())
        {
            int searchSource = random.nextInt(SkipSimParameters.getSystemCapacity());

            while (searchSource == dataOwner)
            {
                searchSource = random.nextInt(SkipSimParameters.getSystemCapacity());
            }

            rep = RepOnPath(searchDest, searchSource, rep, false, dataOwnerIndex);

        }
        sgo.getTG().getNodeSet().setCorrespondingReplica(dataOwnerIndex);
    }

    private void AdaptiveReplicationOnPath(int dataOwnerIndex)
    {
        Arrays.fill(pathHistogram, 0);
        Random random = new Random();
        int dataOwner = random.nextInt(SkipSimParameters.getSystemCapacity());
        int searchDest = sgo.getTG().mNodeSet.getNode(dataOwner).getNumID();
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            RepOnPath(searchDest, i, 0, true, dataOwnerIndex);
        }
        int repNum = 0;
        while (repNum < SkipSimParameters.getReplicationDegree())
        {
            int MaxValue = pathHistogram[0];
            int MaxIndex = 0;
            for (int j = 0; j < SkipSimParameters.getSystemCapacity(); j++)
            {
                if (pathHistogram[j] > MaxValue)
                {
                    MaxValue = pathHistogram[j];
                    MaxIndex = j;
                }
            }
            boolean replicationResult = ((Node) sgo.getTG().mNodeSet.getNode(MaxIndex)).setAsReplica(dataOwnerIndex);
            {
                log.debug("Replica {} created at index {} with histogram value {}", repNum, MaxIndex, pathHistogram[MaxIndex]);
                pathHistogram[MaxIndex] = 0;
                repNum++;
            }

        }


        sgo.getTG().getNodeSet().setCorrespondingReplica(dataOwnerIndex);

    }


    @Override
    public void Algorithm(SkipGraphOperations inputSgo, int dataOwnerIndex)
    {
        sgo = inputSgo;
        reset();
        resetRep();
        if (adaptiveReplication)
            AdaptiveReplicationOnPath(dataOwnerIndex);
        else
            ReplicateOnPathGenerator(dataOwnerIndex);
    }
}