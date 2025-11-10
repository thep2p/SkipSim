//package Developments;
//
//import Simulator.SkipSimParameters;
//
///**
// * Created by Yahya on 6/13/2016.
// */
//public class clustering
//    {
//        /*
//        if cluster[i] = true, i is marked as high available SkipGraph.Node, else a low available SkipGraph.Node
//         */
//        private static boolean[] cluster = new boolean[SkipSimParameters.getSystemCapacity()];
//
//
//        public static boolean getCluster(int i)
//            {
//                return cluster[i];
//            }
//        public static void reset()
//            {
//                for(int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
//                    cluster[i] = true;
//            }
//
//        public static void clustering(double A[])
//            {
//                System.out.println("Clustering has been started");
//                double minClusterScore = Double.MAX_VALUE;
//                double bestThreshold   = 0;
//                for(double t = 1; t < 5; t = t + 0.1)
//                    {
//                        //Clustering based on the threshold t
//                        for (int i = 0; i < A.length; i++)
//                            {
//                                if(A[i] > t)
//                                    {
//                                        cluster[i] = true;
//                                    }
//                                else
//                                    cluster[i] = false;
//                            }
//
//                        //Finding the average of each group
//                        double average1 = 0, average2 = 0;
//                        int    groupSize1 = 0, groupSize2 = 0;
//                        for(int i = 0; i < A.length; i++)
//                            {
//                                if(cluster[i])
//                                    {
//                                        average1 += A[i];
//                                        groupSize1++;
//                                    }
//                                else
//                                    {
//                                        average2 += A[i];
//                                        groupSize2++;
//                                    }
//
//                            }
//
//                        if(groupSize1 != 0)
//                            average1 /= groupSize1;
//                        if(groupSize2 != 0)
//                            average2 /= groupSize2;
//
//                        //Finding the standard deviations
//                        double sd1 = 0 , sd2 = 0;
//                        for(int i = 0; i < A.length; i++)
//                            {
//                                if(cluster[i])
//                                    {
//                                        sd1 += Math.pow(average1 - A[i],2);
//                                    }
//                                else
//                                    {
//                                        sd1 += Math.pow(average2 - A[i],2);
//                                    }
//                            }
//
//
//                        double score = sd1 + sd2;
//                        if(score < minClusterScore)
//                            {
//                                bestThreshold = t;
//                                minClusterScore = score;
//                                System.out.println("Min cluster score has been updated to " + score);
//                            }
//
//                    }
//
//                //Final Developments.clustering based on the best threshold
//                int finalSize = 0;
//                for (int i = 0; i < A.length; i++)
//                    {
//                        if(A[i] > bestThreshold)
//                            {
//                                cluster[i] = true;
//                                finalSize++;
//                            }
//                        else
//                            cluster[i] = false;
//                    }
//
//                if(finalSize < SkipSimParameters.getReplicationDegree())
//                    {
//                        for (int i = 0; i < A.length; i++)
//                            {
//                                cluster[i] = true;
//                            }
//                    }
//
//                System.out.println("Final Developments.clustering has been done with threshold = " + bestThreshold + " size of the highly available set is: " + finalSize);
//            }
//    }
