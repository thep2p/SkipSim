package SkipGraph;
import ChurnStabilization.ChurnStochastics;
import ChurnStabilization.DKS;
import DataTypes.Constants;
import Blockchain.LightChain.Transactions;
import Simulator.SkipSimParameters;
//import org.apache.commons.math3.distribution.WeibullDistribution;
import org.apache.commons.math3.distribution.WeibullDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;



public class TopologyGenerator
{
    private static final Logger log = LoggerFactory.getLogger(TopologyGenerator.class);

    private static Random randomX = new Random();
    private static Random randomY = new Random();
    private static Random random = new Random();
    //*****************
    public Nodes mNodeSet;


    public Landmarks mLandmarks;
    private ArrayList<PointChance> pointSeed;
    private Random randomOfflinePicker;
    private boolean seedInitialization;
    private boolean[][] pointChance;
    private double[][] pointProbability;
    private double totalChance;
    private double chanceIndex;
    private double maxProb;


    //private ExponentialDistribution sessionLengthDistribution2 = new ExponentialDistribution(rg, 8, ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    //private ExponentialDistribution downtimeDistribution = new ExponentialDistribution(rg, system.getDownLambda(), ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    private WeibullDistribution[] weibullInterArrivalDistribution;
    private WeibullDistribution weibullSessionLengthDistribution;
//    private JDKRandomGenerator weibullSessionLengthJDKRandomGenerator;
    private JDKRandomGenerator weibullInterArrivalJDKRandomGenerator;


    /**
     * Temporary variable keeps track of the next arrival time
     */
    private double nextArrivalTime;

    /////////////////////
    public TopologyGenerator()
    {
        randomOfflinePicker = new Random();
        setNodeSet(new Nodes(this.getClass()));
        mLandmarks = new Landmarks();
        pointSeed = new ArrayList<>();
        seedInitialization = true;
        pointChance = new boolean[SkipSimParameters.getDomainSize()][SkipSimParameters.getDomainSize()];
        pointProbability = new double[SkipSimParameters.getDomainSize()][SkipSimParameters.getDomainSize()];
        chanceIndex = 0;
        totalChance = 0;
        nextArrivalTime = 0;
        maxProb = 0;

        if(SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.DYNAMIC)
        || SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.BLOCKCHAIN))
        {
        /*
        Churn parameters
         */
            weibullInterArrivalJDKRandomGenerator = new JDKRandomGenerator();
//        weibullSessionLengthJDKRandomGenerator = new JDKRandomGenerator();

            weibullInterArrivalDistribution = new WeibullDistribution[SkipSimParameters.getLandmarksNum()];

            for (int landmark = 0; landmark < SkipSimParameters.getLandmarksNum(); landmark++)
            {
                if (SkipSimParameters.isMultipleInterArrivalDistribution())
                {
                /*
                Every region has its own inter-arrival time
                 */
                    double interArrivalScaleParameter = weibullInterArrivalJDKRandomGenerator.nextDouble() * SkipSimParameters.getInterarrivalScaleParameter();
                    while (interArrivalScaleParameter == 0)
                        interArrivalScaleParameter = weibullInterArrivalJDKRandomGenerator.nextDouble() * SkipSimParameters.getInterarrivalScaleParameter();
                    weibullInterArrivalDistribution[landmark] = new WeibullDistribution(SkipSimParameters.getInterarrivalShapeParameter(),
                            interArrivalScaleParameter,
                            WeibullDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
                }
                else
                {
                    /*
                    All regions have only one interarrival time distribution
                     */
                    weibullInterArrivalDistribution[landmark] = new WeibullDistribution(SkipSimParameters.getInterarrivalShapeParameter(), SkipSimParameters
                            .getInterarrivalScaleParameter(), WeibullDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
                }
            }
            weibullSessionLengthDistribution = new WeibullDistribution(SkipSimParameters.getSessionLengthShapeParameter(), SkipSimParameters
                    .getSessionLengthScaleParameter(), WeibullDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
            //downtimeDistribution = new ExponentialDistribution(rg, system.getDownLambda(), ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
            //sessionLengthDistribution2 = new ExponentialDistribution(rg, 8, ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
        }
    }


    public double getMaxProb()
    {
        return maxProb;
    }

    public void setMaxProb(double i)
    {
        maxProb = i;
    }

    public double getChanceIndex()
    {
        return chanceIndex;
    }

    public void setChanceIndex(double i)
    {
        chanceIndex = i;
    }

    public double getTotalChance()
    {
        return totalChance;
    }

    public void setTotalChance(double i)
    {
        totalChance = i;
    }

    /**
     * pointProbability[i][j]
     */
    public double getPointProbability(int i, int j)
    {
        return pointProbability[i][j];
    }

    /**
     * pointProbability[i][j] = t
     */
    public void setPointProbability(int i, int j, double t)
    {
        pointProbability[i][j] = t;
    }

    /**
     * isPointChance[i][j]
     */
    public boolean isPointChance(int i, int j)
    {
        return pointChance[i][j];
    }

    /**
     * isPointChance[i][j] = t
     */
    public void setPointChance(int i, int j, boolean t)
    {
        pointChance[i][j] = t;
    }

    public boolean isSeedInitialization()
    {
        return seedInitialization;
    }

    public void setSeedInitialization(boolean i)
    {
        seedInitialization = i;
    }

    public ArrayList<PointChance> getPointSeed()
    {
        return pointSeed;
    }

    public void setPointSeed(ArrayList<PointChance> i)
    {
        pointSeed = i;
    }

    public Point UniformRandomNodeGenerator()
    {
        //Random random = new Random();
        Point p = new Point();
        //double sumOfDistancesToTheLandmarks = 0;
        // while(true)
        //{
        p.x = randomX.nextInt(SkipSimParameters.getDomainSize());
        p.y = randomY.nextInt(SkipSimParameters.getDomainSize());
        return p;
    }

    public Point LandmarkBasedSeedRandomNodeGenerator()
    {


        Point p = new Point();
        if (seedInitialization)
        {
            seedInitialization = false;
            probInit();
            pointChanceInit();
        }

        while (true)
        {

            int index = random.nextInt(1000000);


            for (int ii = 0; ii < pointSeed.size(); ii++)
            {
                //System.out.println("down " + pointSeed.get(ii).lowerChance +"  up " + pointSeed.get(ii).upperChance  + " index " + index + " SkipGraph.Nodes " + SkipGraph.Nodes.nodeIndex);
                if (pointSeed.get(ii).getLowerChance() <= index && pointSeed.get(ii).getUpperChance() >= index && pointChance[pointSeed.get(ii).getP().x][pointSeed.get(ii).getP().y])
                {
                    p.x = pointSeed.get(ii).getP().x;
                    p.y = pointSeed.get(ii).getP().y;
                    pointChance[p.x][p.y] = false;
                    return p;
                }


            }
        }

    }

    public void probInit()
    {

        for (int i = 0; i < SkipSimParameters.getDomainSize(); i += 5)
        {
            for (int j = 0; j < SkipSimParameters.getDomainSize(); j += 5)
            {

                PointChance p = new PointChance();
                p.getP().x = i;
                p.getP().y = j;

                for (int k = 0; k < SkipSimParameters.getLandmarksNum(); k++)
                {
                    if (p.getP().distance(mLandmarks.getLandmarkCoordination(k)) < SkipSimParameters.getDomainSize() / 6)
                    {
                        p.setChance(1000000 * SkipSimParameters.getLandmarksNum());
                        //System.out.println("Super " + p.Chance);
                        continue;
                    } else
                    {
                        p.setChance(p.getChance() + (1 - (p.getP().distance(mLandmarks.getLandmarkCoordination(k)) / (SkipSimParameters.getDomainSize() * 1.4))));
                    }
                }


                totalChance += p.getChance();
                pointSeed.add(p);
            }


        }


        for (int i = 0; i < pointSeed.size(); i++)
        {

            pointSeed.get(i).setProb((pointSeed.get(i).getChance() / totalChance) * 1000000);
            if (pointSeed.get(i).getProb() > maxProb)
            {
                maxProb = (pointSeed.get(i).getProb());
            }


            pointSeed.get(i).setLowerChance(chanceIndex);
            chanceIndex += pointSeed.get(i).getProb();
            pointSeed.get(i).setUpperChance(pointSeed.get(i).getLowerChance() + pointSeed.get(i).getProb());

        }


    }

    public void pointChanceInit()
    {
        for (int i = 0; i < SkipSimParameters.getDomainSize(); i++)
            for (int j = 0; j < SkipSimParameters.getDomainSize(); j++)
            {
                pointChance[i][j] = true;
            }
    }

//Availability functions------------------------------------------------------------------------------------------------------

    /**
     * @return returns the saved next arrival time
     */
    public double getNextArrivalTime()
    {
        return nextArrivalTime;
    }

    /*
    generates a new next arrival time
     */
    public void updateNextArrivalTime(Node node)
    {
        /*
        Generate a new arrival time
       */
        double interArrivalTime = weibullInterArrivalDistribution[node.getClosetLandmarkIndex(mLandmarks)].sample();
        nextArrivalTime += interArrivalTime;
        if(SkipSimParameters.isLog())
            log.debug("TopologyGenerator: next arrival time {} for node's region {}",
                nextArrivalTime, node.getClosetLandmarkIndex(mLandmarks));
    }

    /**
     * @return a random session length based on the Weibull distribution and Simulator.system parameters
     */
    public double generateSessionLength()
    {
        //double sessionLength = weibull(Simulator.system.getSessionLengthShapeParameter(), Simulator.system.getSessionLengthScaleParameter());
        //double sessionLength = sessionLengthDistribution2.sample();
        double sessionLength = weibullSessionLengthDistribution.sample();
        double sessionLengthMean = weibullSessionLengthDistribution.getNumericalMean();
        //System.out.println("TopologyGenerator: generated session length " + sessionLength);
        return sessionLength;
    }


    /**
     * Checks the whole Node set and finds the Nodes with elapsed session length and executes departure for those.
     */
    public void departureUpdate(int currentTime, Transactions transactions)
    {
        if (SkipSimParameters.getChurnStabilizationAlgorithm() != null && SkipSimParameters.getChurnStabilizationAlgorithm().equalsIgnoreCase(Constants.Churn.ChurnStabilizationAlgorithm.DKS))
        {
            DKS.resetUpdateLock();
        }
        for (int i = 1; i < SkipSimParameters.getSystemCapacity(); i++)
        {

            if(!SkipSimParameters.getAvailabilityPredictor().equals(Constants.Churn.AvailabilityPredictorAlgorithm.NONE)
            || SkipSimParameters.isDynamicReplication())// &&
                   // (system.getReplicationTime() == -1 || currentTime < system.getReplicationTime() + 10 * system.getDataOwnerNumber()))
            {
                ((Node) getNodeSet().getNode(i)).updateAvailabilityState(currentTime);
            }


            if (((Node) getNodeSet().getNode(i)).isOffline())
            {
                continue;
            }
            else if (((Node) getNodeSet().getNode(i)).getDepartureTime() <= currentTime + 1)
            {
                /*commented for the sake of time*/
                if (SkipSimParameters.isLog())
                {
                    ((Node) getNodeSet().getNode(i)).printAvailabilityInfo(currentTime, Constants.Churn.DEPARTURE);
                }
                /*
                Invoking cooperative departure on the node i.e., connects its neighbors at all level before departing
                 */
                getNodeSet().Departure(i, transactions);
                ChurnStochastics.increaseTotalAverageDepartures();
            }
        }
    }


    /**
     * Given list of Nodes' and landmarks' coordination, loads the coordination into skip graph
     *
     * @param nodesArray     array of Nodes' coordination
     * @param landmarksArray array of landmarks coordination
     */
    public void loadCoordination(Point[] nodesArray, Point[] landmarksArray)
    {
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            if (i < SkipSimParameters.getSystemCapacity())
            {
                ((Node) getNodeSet().getNode(i)).setCoordinate(nodesArray[i]);
            }
        }

        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            mLandmarks.setLandmarkCoordination(i, landmarksArray[i]);
        }
    }

    /**
     * @return a randomly chosen offline Node from the nodeSet
     */
    public int randomlyPickOffline()
    {
        int index = randomOfflinePicker.nextInt(SkipSimParameters.getSystemCapacity() - 1);
        while (((Node) getNodeSet().getNode(index)).isOnline())
        {
            index = randomOfflinePicker.nextInt(SkipSimParameters.getSystemCapacity() - 1);
        }

        return index;
    }

    /**
     * @return a randomly chosen online Node from the nodeSet
     */
    public int randomlyPickOnline()
    {
        //System.out.println("Number of online Nodes " + mNodeSet.getNumberOfOnlineNodes());
        int index = randomOfflinePicker.nextInt(SkipSimParameters.getSystemCapacity() - 1);
        while (((Node) getNodeSet().getNode(index)).isOffline())
        {
            index = randomOfflinePicker.nextInt(SkipSimParameters.getSystemCapacity() - 1);
        }

        return index;
    }

    public void printGeneratorStochastics()
    {
        log.info("------------------------------------------------------------");
        log.info("TopologyGenerator: Real generator parameters");
        for(int landmark = 0 ; landmark < SkipSimParameters.getLandmarksNum() ; landmark++)
        {
            log.info("Region {} Interarrival mean: {}",
                landmark, weibullInterArrivalDistribution[landmark].getNumericalMean());
        }
        log.info("Session length mean: {}", weibullSessionLengthDistribution.getNumericalMean());
    }


    public Nodes getNodeSet()
    {
        return mNodeSet;
    }

    public void setNodeSet(Nodes nodeSet)
    {
        this.mNodeSet = nodeSet;
    }


}
