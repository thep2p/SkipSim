package Replication;

import Simulator.SkipSimParameters;
import SkipGraph.SkipGraphOperations;
import SkipGraph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Random;


public class Rep_Alg10_PrivateRepOnPath extends Replication
{
    private static final Logger log = LoggerFactory.getLogger(Rep_Alg10_PrivateRepOnPath.class);

    Random random = new Random();
    private boolean adaptiveReplication;

    public Rep_Alg10_PrivateRepOnPath(boolean adaptiveReplication)
        {
            this.adaptiveReplication = adaptiveReplication;
        }

    public void ReplicateOnPathGenerator(SkipGraphOperations sgo, int dataOwnerIndex)
        {
            int rep = 0;
            int dataOwner = random.nextInt(SkipSimParameters.getSystemCapacity() - 1);
            int dataRequerter = 0;
            while (rep <= SkipSimParameters.getReplicationDegree())
                {
                    dataRequerter = random.nextInt(SkipSimParameters.getDataRequesterNumber() - 1);
                    while (dataOwner == dataRequerter)
                        {
                            dataRequerter = random.nextInt(SkipSimParameters.getDataRequesterNumber() - 1);
                        }

                    //System.out.println(SkipGraph.Nodes.nodeSet.length + " " + Simulator.system.size + " " + searchDest);

                    rep = RepOnPath(sgo.getTG().mNodeSet.getNode(dataOwner).getNumID(), dataRequerter, rep, false, dataOwnerIndex);
                }

            sgo.getTG().getNodeSet().setCorrespondingReplica(dataOwnerIndex);

        }

    private void AdaptiveReplicationOnPath(int dataOwnerID)
        {
            Arrays.fill(pathHistogram, 0);
            Random random = new Random();
            int dataOwner = random.nextInt(SkipSimParameters.getSystemCapacity());
            int searchDest = sgo.getTG().mNodeSet.getNode(dataOwner).getNumID();
            for (int i = 0; i < SkipSimParameters.getDataRequesterNumber(); i++)
                {
                    RepOnPath(searchDest, i, 0, true, dataOwnerID);
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
                    boolean replicationResult = ((Node) sgo.getTG().mNodeSet.getNode(MaxIndex)).setAsReplica(dataOwnerID);
                    if(replicationResult)
                    {
                        log.debug("Replica {} created at index {} with histogram value {}", repNum, MaxIndex, pathHistogram[MaxIndex]);
                        pathHistogram[MaxIndex] = 0;
                        repNum++;
                    }
                }

            sgo.getTG().getNodeSet().setCorrespondingReplica(dataOwnerID);

        }


    @Override
    public void Algorithm(SkipGraphOperations inputSgo, int dataOwnerIndex)
        {
            sgo = inputSgo;
            reset();
            resetRep();
            if (adaptiveReplication) AdaptiveReplicationOnPath(dataOwnerIndex);
            else ReplicateOnPathGenerator(sgo, dataOwnerIndex);
        }
}