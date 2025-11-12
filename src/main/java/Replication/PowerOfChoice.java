package Replication;

import Simulator.SkipSimParameters;
import SkipGraph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class PowerOfChoice extends Randomized
{
    private static final Logger log = LoggerFactory.getLogger(PowerOfChoice.class);
    /**
     * Randomly chooses two replicas and replicates on the looser one in load
     * @param dataOwnerID the id of the data owner
     */
    @Override
    protected void randomReplicaGenerator(int dataOwnerID)
    {
        log.info("Randomized replication started for data owner {}", dataOwnerID);
        Random random = new Random();
        /*
        Addresses of the rep1 and rep2
         */
        int rep1;
        int rep2;
        int i = 0;
        while (i < SkipSimParameters.getReplicationDegree())
        {

            rep1 = random.nextInt(SkipSimParameters.getSystemCapacity() - 1);
            Node nodeRep1 = (Node) sgo.getTG().mNodeSet.getNode(rep1);
            while (nodeRep1.isOffline() || !nodeRep1.hasStorageCapacity() || nodeRep1.isReplica(dataOwnerID))
            {
                rep1 = random.nextInt(SkipSimParameters.getSystemCapacity() - 1);
                nodeRep1 = (Node) sgo.getTG().mNodeSet.getNode(rep1);
            }


            rep2 = random.nextInt(SkipSimParameters.getSystemCapacity() - 1);
            Node nodeRep2 = (Node) sgo.getTG().mNodeSet.getNode(rep2);
            while (nodeRep2.isOffline() || !nodeRep2.hasStorageCapacity() || nodeRep2.isReplica(dataOwnerID) || rep1 == rep2)
            {
                rep2 = random.nextInt(SkipSimParameters.getSystemCapacity() - 1);
                nodeRep2 = (Node) sgo.getTG().mNodeSet.getNode(rep2);
            }

            /*
            Replicating on the looser one in load
             */
            if (nodeRep1.getBandwidthCapacity() * nodeRep1.getNormalizedStorageCapacity() > nodeRep2.getBandwidthCapacity() * nodeRep2.getNormalizedStorageCapacity())
            {
                nodeRep1.setAsReplica(dataOwnerID);
                log.debug("Replica created at index {} - total replicas: {}", rep1, i + 1);
            }
            else
            {
                nodeRep2.setAsReplica(dataOwnerID);
                log.debug("Replica created at index {} - total replicas: {}", rep2, i + 1);
            }
            i++;

        }
        /*
        Setting the corresponding replica of the nodes with respect to the data owner
         */
        sgo.getTG().getNodeSet().setCorrespondingReplica(dataOwnerID);
    }
}
