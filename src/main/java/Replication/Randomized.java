package Replication;

import Simulator.SkipSimParameters;
import SkipGraph.Node;
import SkipGraph.SkipGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;


public class Randomized extends Replication
{
    private static final Logger log = LoggerFactory.getLogger(Randomized.class);

    protected void randomReplicaGenerator(int dataOwnerID)
    {
        log.info("Randomized replication started for data owner {}", dataOwnerID);
        Random random = new Random();
        int i = 0;
        while (i < SkipSimParameters.getReplicationDegree())
        {
            int index = random.nextInt(SkipSimParameters.getSystemCapacity() - 1);
            Node node = ((Node) sgo.getTG().mNodeSet.getNode(index));
            while (node.isReplica(dataOwnerID) || node.isOffline())
                index = random.nextInt(SkipSimParameters.getSystemCapacity() - 1);
            boolean replicationResult = ((Node) sgo.getTG().mNodeSet.getNode(index)).setAsReplica(dataOwnerID);
            if (replicationResult)
            {
                log.debug("Replica created at index {} - total replicas: {}", index, i);
                i++;
            }
        }
        sgo.getTG().getNodeSet().setCorrespondingReplica(dataOwnerID);
    }

    @Override
    public void Algorithm(SkipGraphOperations sgo, int dataOwnerID)
    {
        this.sgo = sgo;
        reset();
        resetRep();
        randomReplicaGenerator(dataOwnerID);
    }

}