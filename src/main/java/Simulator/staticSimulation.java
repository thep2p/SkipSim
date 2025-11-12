package Simulator;

import DataTypes.Constants;
import Evaluation.ReplicationEvaluation;
import LandmarkPlacement.LookupEvaluation;
import NameIDAssignment.NameIDEvaluation;
import SkipGraph.Node;
import SkipGraph.SkipGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class staticSimulation
{
    private static final Logger log = LoggerFactory.getLogger(staticSimulation.class);

    public static final int dataOwnerID = 0;
    public staticSimulation(SkipGraphOperations sgo1, boolean generateCoordination)
    {
        Simulation(sgo1, generateCoordination);
    }

    private void Simulation(SkipGraphOperations sgo, boolean generateCoordination)
    {
        log.info("Static Simulation started");


        //Generating or loading landmarks
        if (generateCoordination)
        {
            sgo.getTG().mLandmarks.generatingLandmarks();
            sgo.getTG().mNodeSet.generateNodes(true, sgo, Constants.SkipGraphOperation.STATIC_SIMULATION_TIME, true);
        }
        else
        {
            for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
            {
                sgo.getTG().mNodeSet.renewNode(i);
                if (AlgorithmInvoker.isNameIDAssignmentDynamic())
                {
                    sgo.getTG().mNodeSet.getNode(i).setNameID(AlgorithmInvoker.dynamicNameIDAssignment(((Node) sgo.getTG().mNodeSet.getNode(i)), sgo, i));
                }
                sgo.renewInsertion(i, AlgorithmInvoker.isNameIDAssignmentDynamic(), Constants.SkipGraphOperation.STATIC_SIMULATION_TIME, sgo.getTG().mNodeSet);
                //System.out.println("Loaded: Node name id is " + sgo.getTG().mNodeSet.getNode(i).nameID + " numerical id is " + sgo.getTG().mNodeSet.getNode(i).getNumID() + " Simulator.system index =  " + i
                //+ sgo.getTG().mNodeSet.getNode(i).mCoordinate.toString());
            }
        }


        if (!AlgorithmInvoker.isNameIDAssignmentDynamic())
        {
            //last parameter is a fake Node id, static algorithms do not need Node id, just to conform the prototypes
            Node n = new Node(0);
            AlgorithmInvoker.staticNameIDAssignment(n, sgo);
        }


        if (!SkipSimParameters.isDynamicReplication())
        {
            AlgorithmInvoker AI = new AlgorithmInvoker();
            AI.staticReplication(sgo, dataOwnerID);
            if(SkipSimParameters.isReplicationLocalityAwarenessEvaluation())
                ReplicationEvaluation.AverageAccessDelay(sgo.getTG().getNodeSet(),
                        SkipSimParameters.isPublicReplication(),
                        SkipSimParameters.getReplicationAlgorithm(),
                        -1,
                        false);
        }
        new AlgorithmInvoker().staticAggregation(sgo);
        LookupEvaluation le = new LookupEvaluation(sgo);

        /*
        Evaluating the locality awareness of name IDs
         */
        if(SkipSimParameters.isNameIDLocalityAwarenessEvaluatgion())
        {
            le.meanDistanceEvaluate();
            NameIDEvaluation.NameIDEvaluation(sgo);
        }
        if (SkipSimParameters.getSearchByNumericalID() > 0)
        {
            le.evaluator(SkipSimParameters.getSearchByNumericalID(), "numID");
        }

        if (SkipSimParameters.getSearchByNameID() > 0)
        {
            le.evaluator(SkipSimParameters.getSearchByNameID(), "nameID");
            //LandmarkPlacement.LookupEvaluation.evaluator(Simulator.system.searchByNameID, "nameID");
            //LandmarkPlacement.LookupEvaluation.meanDistanceEvaluate();
        }

        //Simulator.AlgorithmInvoker.saveSgoToFile(sgo);


    }
}
