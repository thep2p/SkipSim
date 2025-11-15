package AvailabilityBasedReplication;

import Simulator.SkipSimParameters;
import SkipGraph.Node;
import SkipGraph.Nodes;
import SkipGraph.SkipGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class AvailabilityRep_Public_Alg01_Randomized extends AvailavilityBasedReplication
{
    private static final Logger log = LoggerFactory.getLogger(AvailabilityRep_Public_Alg01_Randomized.class);

    @Override
    public void Algorithm(int dataOwnerIndex, int replicationDegree, SkipGraphOperations sgo)
    {
        Random random = new Random();

        int repCounter = 0;
        while (true)
        {
            int replica = random.nextInt(SkipSimParameters.getSystemCapacity() - 1);
            if (!((Node) sgo.getTG().getNodeSet().getNode(replica)).isReplica(dataOwnerIndex) && ((Node) sgo.getTG().getNodeSet().getNode(replica)).isOnline())
            {
                boolean replicationResult = ((Node) sgo.getTG().getNodeSet().getNode(replica)).setAsReplica(dataOwnerIndex);
                if(replicationResult)
                {
                    log.debug("Randomized replication: data owner index {} selected replica index {}", dataOwnerIndex, replica);
                    repCounter++;
                    if (repCounter >= replicationDegree) break;
                }
            }
        }

    }
}