package LandmarkPlacement;

import Simulator.SkipSimParameters;
import SkipGraph.Node;
import SkipGraph.SkipGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

/**
 * Created by Yahya on 03/04/2017.
 */
public abstract class LandmarkPlacementEvaluation
    {
        private static final Logger log = LoggerFactory.getLogger(LandmarkPlacementEvaluation.class);

        private static double[][] averageDistanceToCloset = new double[SkipSimParameters.getTopologies()][SkipSimParameters.getLandmarksNum()];
        private static int[][] totalRelocation = new int[SkipSimParameters.getTopologies()][SkipSimParameters.getLandmarksNum()];
        protected  double[] distanceToLandmark = new double[SkipSimParameters.getLandmarksNum()];
        protected  double[] maxDistanceToLandmark = new double[SkipSimParameters.getLandmarksNum()];
        protected  int[] coveringNodes = new int[SkipSimParameters.getLandmarksNum()];
        public LandmarkPlacementEvaluation()
            {
//                averageDistanceToCloset = new double[Simulator.system.getTopologyNumbers()][Simulator.system.getLandmarksNum()];
//                totalRelocation = new int[Simulator.system.getTopologyNumbers()][Simulator.system.getLandmarksNum()];
            }
        public void landmarkEvaluation(SkipGraphOperations sgo, String algName)
            {
                int[] coveringNodes = new int[SkipSimParameters.getLandmarksNum()];
                for(int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
                    {
                        averageDistanceToCloset[SkipSimParameters.getCurrentTopologyIndex()-1][((Node) sgo.getTG().mNodeSet.getNode(i)).getClosetLandmarkIndex(sgo.getTG().mLandmarks)] +=
                                ((Node) sgo.getTG().mNodeSet.getNode(i)).getCoordinate().distance(sgo.getTG().mLandmarks.getLandmarkCoordination(((Node) sgo.getTG().mNodeSet.getNode(i)).getClosetLandmarkIndex(sgo.getTG().mLandmarks)).getLocation());
                        coveringNodes[((Node) sgo.getTG().mNodeSet.getNode(i)).getClosetLandmarkIndex(sgo.getTG().mLandmarks)]++;
                    }

                for(int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
                    {
                        averageDistanceToCloset[SkipSimParameters.getCurrentTopologyIndex()-1][i] =  averageDistanceToCloset[SkipSimParameters.getCurrentTopologyIndex()-1][i] / coveringNodes[i];
                    }

                if(SkipSimParameters.getCurrentTopologyIndex() == SkipSimParameters.getTopologies())
                    {
                        double[] totalAverageDistance = new double[SkipSimParameters.getLandmarksNum()];
                        for(int i = 0; i < SkipSimParameters.getTopologies() ; i++)
                            for(int j = 0; j < SkipSimParameters.getLandmarksNum() ; j++)
                            {
                                totalAverageDistance[j] += averageDistanceToCloset[i][j];
                            }
                        log.info("Landmark placement evaluation for {} completed", algName);
                        StringBuilder distances = new StringBuilder();
                        for(int j = 0; j < SkipSimParameters.getLandmarksNum() ; j++)
                            {
                                totalAverageDistance[j] /= SkipSimParameters.getTopologies();
                                distances.append(totalAverageDistance[j]).append(" ");
                            }
                        log.info("Total average distances: {}", distances.toString().trim());
                    }
            }
        public void relocationEvaluation(int landmarkIndex, int relocationCounter, String algName)
            {
                totalRelocation[SkipSimParameters.getCurrentTopologyIndex()-1][landmarkIndex] = relocationCounter;
                if(SkipSimParameters.getCurrentTopologyIndex() == SkipSimParameters.getTopologies())
                    {
                        double[] averageRelocation = new double[SkipSimParameters.getLandmarksNum()];
                        for(int i = 0; i < SkipSimParameters.getTopologies() ; i++)
                            for(int j = 0; j < SkipSimParameters.getLandmarksNum() ; j++)
                                {
                                    averageRelocation[j] += totalRelocation[i][j];
                                }
                        StringBuilder relocations = new StringBuilder();
                        for(int j = 0; j < SkipSimParameters.getLandmarksNum() ; j++)
                            {
                                averageRelocation[j] /= SkipSimParameters.getTopologies();
                                relocations.append(averageRelocation[j]).append(" ");
                            }
                        log.info("Number of relocations for {}: {}", algName, relocations.toString().trim());
                    }
            }

        protected boolean terminationCheck(SkipGraphOperations sgo, Point[] newLandmark)
            {
                for(int i = 0; i < SkipSimParameters.getLandmarksNum() ; i++)
                    {
                        if(!newLandmark[i].equals( sgo.getTG().mLandmarks.getLandmarkCoordination(i).getLocation()))
                            {
                                return false;
                            }
                    }
                return true;
            }
        protected void updateAndComputeClosest(SkipGraphOperations sgo)
            {
                distanceToLandmark = new double[SkipSimParameters.getLandmarksNum()];
                maxDistanceToLandmark = new double[SkipSimParameters.getLandmarksNum()];
                coveringNodes = new int[SkipSimParameters.getLandmarksNum()];

                for(int i = 0; i < SkipSimParameters.getLandmarksNum() ; i++)
                    {
                        maxDistanceToLandmark[i] = Double.MIN_VALUE;
                    }
                /*
                Updating the closest landmark distance
                 */
                sgo.getTG().mNodeSet.updateClosestLandmark(sgo.getTG().mLandmarks);

                /*
                Computing the average distance to the closest landmark
                 */
                for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
                    {
                        double nodeDistance = ((Node) sgo.getTG().mNodeSet.getNode(i)).getCoordinate().distance(sgo.getTG().mLandmarks.getLandmarkCoordination(((Node) sgo.getTG().mNodeSet.getNode(i)).getClosetLandmarkIndex(sgo.getTG().mLandmarks)).getLocation());
                        int closetLandmark = ((Node) sgo.getTG().mNodeSet.getNode(i)).getClosetLandmarkIndex(sgo.getTG().mLandmarks);
                        if(maxDistanceToLandmark[closetLandmark] < nodeDistance)
                            maxDistanceToLandmark[closetLandmark] = nodeDistance;
                        distanceToLandmark[closetLandmark] += nodeDistance;
                        coveringNodes[((Node) sgo.getTG().mNodeSet.getNode(i)).getClosetLandmarkIndex(sgo.getTG().mLandmarks)]++;
                    }

                StringBuilder avgDistances = new StringBuilder();
                for(int j = 0; j < SkipSimParameters.getLandmarksNum() ; j++)
                    {
                        if(coveringNodes[j] == 0)
                            avgDistances.append("0 ");
                        else
                            avgDistances.append((int) distanceToLandmark[j] / coveringNodes[j]).append(" ");
                    }
                log.debug("Average distance to landmarks: {}", avgDistances.toString().trim());

                StringBuilder maxDistances = new StringBuilder();
                for(int j = 0; j < SkipSimParameters.getLandmarksNum() ; j++)
                    {
                        maxDistances.append((int) maxDistanceToLandmark[j]).append(" ");
                    }
                log.debug("Max distance to landmarks: {}", maxDistances.toString().trim());
            }
    }
