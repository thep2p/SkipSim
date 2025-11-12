package NameIDAssignment;

import Simulator.SkipSimParameters;
import SkipGraph.Node;
import SkipGraph.SkipGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;


import java.time.LocalDateTime;
import java.util.GregorianCalendar;

public class NameIDEvaluation
{
    private static final Logger log = LoggerFactory.getLogger(NameIDEvaluation.class);

    public static double[][] Results = new double[SkipSimParameters.getTopologies()][SkipSimParameters.getLookupTableSize()];
    private static final String fileAddress = "NameID_Evaluation_" + SkipSimParameters.getNameIDAssignment() + "_" + LocalDateTime.now().getHour() + LocalDateTime.now().getMinute() + LocalDateTime.now()             .getSecond() + LocalDateTime
            .now().getDayOfMonth() + LocalDateTime.now().getMonth() + LocalDateTime.now().getYear() + ".txt";



    public static void NameIDEvaluation(SkipGraphOperations sg)
    {
        //Simulator.system.nameIDsize = 12;
        double[] SD = new double[SkipSimParameters.getLookupTableSize()];
        double[] Mean = new double[SkipSimParameters.getLookupTableSize()];
        try
        {
            // Create file


            double[] DistanceFromEachOther = new double[SkipSimParameters.getLookupTableSize()]; // sum of distances of SkipGraph.Nodes that have i (0<= i < Simulator.system.nameIDsize) from eachother
            int[] NumberOfNodes = new int[SkipSimParameters.getLookupTableSize()]; // number of the SkipGraph.Nodes that have i (0<= i < Simulator.system.nameIDsize) bits in their common prefix

//Todo change the flowing to print into screen instead of a file
//            if (SkipSimParameters.isMaltabRepAutoMeanSDEvaluationInit())
//            {
//
//                for (int i = 0; i < SkipSimParameters.getTopologyNumbers(); i++)
//                    for (int j = 0; j < SkipSimParameters.getLookupTableSize(); j++)
//                        Results[i][j] = 0;
//
//                SkipSimParameters.setMaltabRepAutoMeanSDEvaluationInit(false);
//            }

            for (int i = 0; i < SkipSimParameters.getLookupTableSize(); i++)
            {
                DistanceFromEachOther[i] = 0;
                NumberOfNodes[i] = 0;
                SD[i] = 0;
                Mean[i] = 0;
            }


            int c = 0;
            for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
            {
                //System.out.println("First for");
                for (int j = i + 1; j < SkipSimParameters.getSystemCapacity(); j++)
                {
                    c = sg.getTG().mNodeSet.commonBits(i, j);
                    //System.out.println("commonbits = " + c);
                    //					    		if(c < 2*Simulator.system.nameIDsize)
                    {
                        //System.out.println("C<size");
                        NumberOfNodes[c] = NumberOfNodes[c] + 1;
                        DistanceFromEachOther[c] = DistanceFromEachOther[c] + ((Node) sg.getTG().mNodeSet.getNode(i)).getCoordinate().distance(((Node) sg.getTG().mNodeSet.getNode(j)).getCoordinate());
                        //if(c == 4) System.out.println("Distance = " + DistanceFromEachOther[c]);
                    }
                    //					    		else
                    //					    		{
                    //					    			//System.out.println("C>size");
                    //					    			NumberOfNodes[Simulator.system.nameIDsize-1] = NumberOfNodes[Simulator.system.nameIDsize-1] + 1;
                    //					    			DistanceFromEachOther[Simulator.system.nameIDsize-1] = DistanceFromEachOther[Simulator.system.nameIDsize-1] + SkipGraph.Nodes.nodeSet[i].mCoordinate.distance(SkipGraph.Nodes.nodeSet[j].mCoordinate);
                    //
                    //					    		}

                }
            }

            for (int i = 0; i < SkipSimParameters.getLookupTableSize(); i++)
            {

                //SumMeansOfAll[i] = SumMeansOfAll[i] + (DistanceFromEachOther[i] / NumberOfNodes[i]);
                if (NumberOfNodes[i] == 0)
                {
                    Results[SkipSimParameters.getCurrentTopologyIndex() - 1][i] = 0;
                }
                else
                {
                    Results[SkipSimParameters.getCurrentTopologyIndex() - 1][i] = (DistanceFromEachOther[i] / NumberOfNodes[i]);
                }
                //if(i == 4) System.out.println("Run " + Simulator.system.simIndex + "  mean " + Results[Simulator.system.simIndex-1][i] + " " + NumberOfNodes[i] + " " +  DistanceFromEachOther[i]);
            }

            if (SkipSimParameters.getCurrentTopologyIndex() == SkipSimParameters.getTopologies())
            {
                log.info("Writing name ID evaluation results to file: {}", fileAddress);
                FileWriter fstream = new FileWriter(fileAddress);
                BufferedWriter out = new BufferedWriter(fstream);
                for (int i = 0; i < SkipSimParameters.getLookupTableSize(); i++)
                {
                    for (int j = 0; j < SkipSimParameters.getTopologies(); j++)
                    {

                        //Mean[i] = SumMeansOfAll[i] / Simulator.system.simRun;
                        Mean[i] = Mean[i] + Results[j][i];
                    }

                    Mean[i] = Mean[i] / SkipSimParameters.getTopologies();
                }

                for (int i = 0; i < SkipSimParameters.getLookupTableSize(); i++)
                {
                    for (int j = 0; j < SkipSimParameters.getTopologies(); j++)
                    {

                        SD[i] = SD[i] + Math.pow((Results[j][i] - Mean[i]), 2);
                    }
                }

                out.write("_______________________________________________________________");
                out.newLine();
                out.write("SkipSim Version 2.1");
                out.newLine();
                out.write("_______________________________________________________________");
                out.newLine();
                out.write("Yahya Hassanzadeh Nazarabadi - Amin Alizadeh - Alptekin Küpçü - Öznur Özkasap");
                out.newLine();
                out.write("IdEvaluation");
                out.newLine();
                out.write(new GregorianCalendar().getTime().toString());
                out.newLine();
                out.write("Algorithm: " + SkipSimParameters.getNameIDAssignment());
                out.newLine();
                out.write("Size: " + SkipSimParameters.getSystemCapacity());
                out.newLine();
                out.write("Landmarks: " + SkipSimParameters.getLandmarksNum());
                out.newLine();
                out.write("NameIDSize: " + SkipSimParameters.getNameIDLength());
                out.newLine();
                out.write("DomainSize: " + SkipSimParameters.getDomainSize());
                out.newLine();
                out.write("_______________________________________________________________");
                out.newLine();


                for (int i = 0; i < SkipSimParameters.getLookupTableSize(); i++)
                {
                    out.write(String.valueOf(i));
                    out.write(" ");


                    if (NumberOfNodes[i] != 0)
                    {
                        out.write(String.valueOf((int) (Mean[i])));
                    }
                    else
                    {
                        out.write(String.valueOf(0));
                    }

                    out.write(" ");

                    if (SD[i] != 0)
                    {
                        out.write(String.valueOf((int) (Math.sqrt(SD[i] / SkipSimParameters.getTopologies()))));
                    }
                    else
                    {
                        out.write(String.valueOf(0));
                    }
                    out.newLine();

                }


                //Close the output stream
                out.close();
                SkipSimParameters.getTopologies();
                SkipSimParameters.getCurrentTopologyIndex();
                log.info("Name ID evaluation completed");

                //showresult(sg);
            }


        }
        catch (Exception e)
        {

            //Catch exception if any
            e.printStackTrace();
        }


    }


    public void showresult(SkipGraphOperations sgo)
    {
        for (int i = 0; i < SkipSimParameters.getTopologies() - 1; i++)
        {
            StringBuilder row = new StringBuilder();
            for (int j = 0; j < SkipSimParameters.getLookupTableSize(); j++)
            {
                row.append((int) Results[i][j]).append(" ");
            }
            log.debug("Results[{}]: {}", i, row.toString());
        }
    }
}
