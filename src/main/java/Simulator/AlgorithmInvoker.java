package Simulator;

import Aggregation.Agg_Alg1_ELATS;
import Aggregation.Agg_Alg2_BroadcastTree;
import Aggregation.Agg_Alg3_PrefixTree;
import ChurnStabilization.*;
import DataTypes.Constants;
import LandmarkPlacement.LandmarkAlg02_OneByOne;
import NameIDAssignment.*;
import Replication.*;
import SkipGraph.Node;
import SkipGraph.SkipGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

/**
 * Created by Yahya on 8/25/2016.
 */
public class AlgorithmInvoker
{
    private static final Logger log = LoggerFactory.getLogger(AlgorithmInvoker.class);

    public AlgorithmInvoker()
    {

    }

    public static void saveSgoToFile(SkipGraphOperations sgo)
    {
        String s;
        try
        {

            if (SkipSimParameters.getCurrentTopologyIndex() == 1)
            {
                s = "resaved" + ".skipsim";
                FileOutputStream fout = new FileOutputStream(s);
                ObjectOutputStream oos = new ObjectOutputStream(fout);
            }
            s = "resaved_" + SkipSimParameters.getCurrentTopologyIndex() + ".txt";
            FileOutputStream fout = new FileOutputStream(s);
            ObjectOutputStream oos = new ObjectOutputStream(fout);

            log.debug("Saving to file: {}", s);

            /**
             * save SkipGraph.Nodes
             */
            for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
            {
                oos.writeObject(((Node) sgo.getTG().mNodeSet.getNode(i)).getCoordinate());
            }
            /**
             * save landmarks
             */
            for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
            {
                oos.writeObject(sgo.getTG().mLandmarks.getLandmarkCoordination(i));
            }

            //Extra improvements
            fout.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }


    //Replicate based on the configured replication algorithm
    public void dynamicReplication(int dataOwnerIndex, int replicationDegree, SkipGraphOperations sgo)
    {
        switch (SkipSimParameters.getReplicationAlgorithm())
        {
            case Constants.Replication.Algorithms.RANDOMIZED:
                new Randomized().Algorithm(sgo, dataOwnerIndex);
                    //new AvailabilityRep_Public_Alg01_Randomized().Algorithm(dataOwnerIndex, replicationDegree, sgo);
                break;
            case Constants.Replication.Algorithms.POWER_OF_CHOICE:
                new PowerOfChoice().Algorithm(sgo, dataOwnerIndex);
                break;
            case Constants.Replication.Algorithms.GLARAS:
                new GLARAS().Algorithm(sgo, dataOwnerIndex);
                break;
            case Constants.Replication.Algorithms.PYRAMID:
                new Pyramid().Algorithm(sgo, dataOwnerIndex);
                break;
            case Constants.Replication.Algorithms.CLUSTER:
                new ClusterBased().Algorithm(sgo, dataOwnerIndex);
                break;
            case Constants.Replication.Algorithms.CORRELATION:
                new CorrelationBased().Algorithm(sgo, dataOwnerIndex);
                break;
//            case "LARASLP":
//                //	    		if(Simulator.system.getReplicationType().equals("public"))
//                //	    			AvailabilityRep_Public_Alg04_LPPartOfLARASOnAvailability.Algorithm();
//                //	    		else
//                //	    			System.exit(0);
//                break;
//            case "RepLP":
//                //	    		if(Simulator.system.getReplicationType().equals("public"))
//                //	    			AvailabilityRep_Public_Alg05_LPOnMinimizingNumberOfRepsGivenAvailability.Algorithm();
//                //	    		else
//                //	    			System.exit(0);
//                break;
//            case "hourLP":
//                //				if(Simulator.system.getReplicationType().equals("public"))
//                //					AvailabilityRep_Public_Alg06_LPOnMaximizingAvailabilityPerHour.Algorithm();
//                //				else
//                //					System.exit(0);
//                //				break;
//
//            case "awake":
//                //				if(Simulator.system.getReplicationType().equals("public"))
//                //					AvailabilityRep_Public_Alg08_Awake.Algorithm();
//                //				else
//                //					System.exit(0);
//                break;

            default:
                log.error("No dynamic replication algorithm found in config.txt that matches SkipSim's ones");
                System.exit(0);

        }

    }

    public void staticReplication(SkipGraphOperations sgo, int dataOwnerIndex)
    {
        switch (SkipSimParameters.getReplicationAlgorithm())
        {
//                    case "LP":
//                        //if (Simulator.system.getReplicationType().equals("public")) new Rep_Alg01_LP().Algorithm(sgo);
//                        //else new Rep_Alg06_PrivateLP().Algorithm(sgo);
//                        break;
            case "randomized":
                if (SkipSimParameters.getReplicationType().equals("public"))
                    new Randomized().Algorithm(sgo, dataOwnerIndex);
                else
                {
                    if (SkipSimParameters.isDelayBasedSimulaton())
                        new Rep_Alg08_PrivateRandom().delayBasedReplication(SkipSimParameters.getDelayBound(), SkipSimParameters.getInitialReplicationDegree(), sgo, " randomized ", dataOwnerIndex);
                    else new Rep_Alg08_PrivateRandom().Algorithm(sgo, dataOwnerIndex);
                }

                break;
            case "onneighbors":
                if (SkipSimParameters.getReplicationType().equals("public"))
                    new Rep_Alg03_RepOnNeighbors().Algorithm(sgo, dataOwnerIndex);
                else
                {
                    if (SkipSimParameters.isDelayBasedSimulaton())
                        new Rep_Alg09_PrivateRepOnNeighbors().delayBasedReplication(SkipSimParameters.getDelayBound(), SkipSimParameters.getInitialReplicationDegree(), sgo, " on neighbors", dataOwnerIndex);
                    else new Rep_Alg09_PrivateRepOnNeighbors().Algorithm(sgo, dataOwnerIndex);
                }
                break;
            case "onpath":
                if (SkipSimParameters.getReplicationType().equals("public"))
                {
                    Rep_Alg04_RepOnPath reponPath = new Rep_Alg04_RepOnPath(false);
                    reponPath.Algorithm(sgo, dataOwnerIndex);
                }
                else
                {
                    if (SkipSimParameters.isDelayBasedSimulaton())
                    {
                        Rep_Alg10_PrivateRepOnPath reponPath = new Rep_Alg10_PrivateRepOnPath(false);
                        reponPath.delayBasedReplication(SkipSimParameters.getDelayBound(), SkipSimParameters.getInitialReplicationDegree(), sgo, " on path", dataOwnerIndex);
                    }
                    else
                    {
                        Rep_Alg10_PrivateRepOnPath reponPath = new Rep_Alg10_PrivateRepOnPath(false);
                        reponPath.Algorithm(sgo, dataOwnerIndex);
                    }
                }
                break;
            case "adaptiveonpath":
                if (SkipSimParameters.getReplicationType().equals("public"))
                {
                    Rep_Alg04_RepOnPath reponPath = new Rep_Alg04_RepOnPath(true);
                    reponPath.Algorithm(sgo, dataOwnerIndex);
                }
                else
                {
                    if (SkipSimParameters.isDelayBasedSimulaton())
                    {
                        Rep_Alg10_PrivateRepOnPath reponPath = new Rep_Alg10_PrivateRepOnPath(true);
                        reponPath.delayBasedReplication(SkipSimParameters.getDelayBound(), SkipSimParameters.getInitialReplicationDegree(), sgo, " adaptive on path", dataOwnerIndex);
                    }
                    else
                    {
                        Rep_Alg10_PrivateRepOnPath reponPath = new Rep_Alg10_PrivateRepOnPath(true);
                        reponPath.Algorithm(sgo, dataOwnerIndex);
                    }
                }
                break;
            case Constants.Replication.Algorithms.GLARAS:
                if (SkipSimParameters.isDelayBasedSimulaton())
                    new GLARAS().delayBasedReplication(SkipSimParameters.getDelayBound(), SkipSimParameters.getInitialReplicationDegree(), sgo, Constants.Replication.Algorithms.GLARAS.toUpperCase(), dataOwnerIndex);
                else
                    new GLARAS().Algorithm(sgo, dataOwnerIndex);
                break;
            case "LARAS":
                if (SkipSimParameters.getReplicationType().equals("public"))
                    new LARAS().Algorithm(sgo, dataOwnerIndex);
                else
                {
                    if (SkipSimParameters.isDelayBasedSimulaton())
                        new LARAS().delayBasedReplication(SkipSimParameters.getDelayBound(), SkipSimParameters.getInitialReplicationDegree(), sgo, " LARAS ", dataOwnerIndex);
                    else new LARAS().Algorithm(sgo, dataOwnerIndex);
                }
                break;
            case "onrequesters":
                //Rep_Alg15_PrivateRepOnRequesters.Algorithm();
        }

    }

    public void staticAggregation(SkipGraphOperations sgo)
    {
        switch (SkipSimParameters.getAggregationAlgorithm())
        {
            case "ELATS":
                new Agg_Alg1_ELATS(sgo);
                break;
            case "broadcast":
                new Agg_Alg2_BroadcastTree(sgo);
                break;
            case "prefix":
                new Agg_Alg3_PrefixTree(sgo);
                break;
        }


    }

    public void landmarkPlacement(SkipGraphOperations sgo)
    {
        //LandmarkPlacement.LandmarkAlg01_Movements alg = new LandmarkPlacement.LandmarkAlg01_Movements();
        LandmarkAlg02_OneByOne alg = new LandmarkAlg02_OneByOne();
        alg.Algorithm(sgo, SkipSimParameters.getSystemCapacity() / SkipSimParameters.getLandmarksNum());
    }

    public static Boolean isNameIDAssignmentDynamic()
    {
        switch (SkipSimParameters.getNameIDAssignment())
        {
            case Constants.NameID.LAND:
                return true;
            case Constants.NameID.LANS:
                return true;
            case Constants.NameID.DPAD:
                return true;
            case Constants.NameID.LDHT:
                return true;
            case Constants.NameID.HIREARCHIAL:
                return true;
            case Constants.NameID.LMDS:
                return false;
            case Constants.NameID.DPLMDS:
                return false;
            default:
                return false;
        }
    }

    public static String staticNameIDAssignment(Node n, SkipGraphOperations sgo)
    {
        switch (SkipSimParameters.getNameIDAssignment())
        {
            case Constants.NameID.LMDS:
                new NameID_Assignment_LMDS().Algorithm(n, sgo);
                return null;
            case Constants.NameID.DPLMDS:
                new NameID_Assignment_DPLMDS().Algorithm(n, sgo);
                return null;
            default:
                return null;

        }
    }

    public static String dynamicNameIDAssignment(Node n, SkipGraphOperations sgo, int index)
    {
        //System.out.println(Simulator.system.getNameIDAssignment());
        switch (SkipSimParameters.getNameIDAssignment())
        {
            case Constants.NameID.LAND:
                return new NameID_Assignment_LAND().randomizedAssingment(index);

            case Constants.NameID.DPAD:
                return new NameID_Assignment_DPAD().Algorithm(n, sgo, index);

            case Constants.NameID.LANS:
                return new NameID_Assignment_LANS().Algorithm(n, sgo, index);

            case Constants.NameID.LDHT:
                return new NameID_Assingment_LDHT().Algorithm(n, sgo, index);

            case Constants.NameID.HIREARCHIAL:
                return new NameID_Assignment_Hirearchical().Algorithm(n, sgo, index);
            default:
                return null;

        }
    }

    public static ChurnStabilization churnStabilization()
    {
        if (SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.STATIC))
            return null;
        if (SkipSimParameters.getChurnStabilizationAlgorithm() == null || SkipSimParameters.getChurnStabilizationAlgorithm().isEmpty())
        {
            if (SkipSimParameters.getChurnType().equalsIgnoreCase(Constants.Churn.Type.ADVERSARIAL))
                throw new IllegalStateException("No churn stabilization is found for the adversarial churn type. Either switch to cooperative churn type, or select a churn stabilization");
            else
                return null;
        }

        switch (SkipSimParameters.getChurnStabilizationAlgorithm().toLowerCase())
        {
            case Constants.Churn.ChurnStabilizationAlgorithm.KADEMLIA:
                return new Kademlia();
            case Constants.Churn.ChurnStabilizationAlgorithm.INTERLLACED:
                return new Interlace();
            case Constants.Churn.ChurnStabilizationAlgorithm.Tornado:
                return new Tornado();
            case Constants.Churn.ChurnStabilizationAlgorithm.DKS:
                return new DKS();
            default:
                return null;
        }
    }
}
