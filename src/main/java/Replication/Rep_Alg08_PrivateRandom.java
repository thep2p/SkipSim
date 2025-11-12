package Replication;

import Simulator.SkipSimParameters;
import SkipGraph.Node;
import SkipGraph.SkipGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;


public class Rep_Alg08_PrivateRandom extends Replication
{
    private static final Logger log = LoggerFactory.getLogger(Rep_Alg08_PrivateRandom.class);


    public void randomReplicaGenerator(int dataOwnerID)
    {
        Random random = new Random();
        int repNum = 0;
        while (repNum < SkipSimParameters.getReplicationDegree())
        {
            int index = random.nextInt(SkipSimParameters.getSystemCapacity() - 1);
            while (((Node) sgo.getTG().mNodeSet.getNode(index)).isReplica(dataOwnerID))
                index = random.nextInt(SkipSimParameters.getSystemCapacity() - 1);
            boolean replicationResult = ((Node) sgo.getTG().mNodeSet.getNode(index)).setAsReplica(dataOwnerID);
            if (replicationResult)
            {
                repNum++;
            }
        }
        sgo.getTG().getNodeSet().setCorrespondingReplica(dataOwnerID);

    }

    @Override
    public void Algorithm(SkipGraphOperations inputSgo, int dataOwnerID)
    {
        sgo = inputSgo;
        log.info("Private randomized replication started for data owner {}", dataOwnerID);
        reset();
        resetRep();
        randomReplicaGenerator(dataOwnerID);
    }
}