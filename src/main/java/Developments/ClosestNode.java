//import java.awt.Point;
//
//
///**
// * 
// * @author Yahya Hassanzadeh
// * @version 1.0
// * 
// * This class tries to find the closest SkipGraph.Node in the skip graph to an external SkipGraph.Node
// * that is not a member of the skip graph
// * The main function of this class is the Algorithm(SkipGraph.Node n) which receives a SkipGraph.Node "n" and
// * will return the index of the closest SkipGraph.Node to this SkipGraph.Node (SkipGraph.Node n)
// *
// */
//class ClosestNode 
//{
//  /**
//   * The distance of the closest SkipGraph.Node to the SkipGraph.Node n
//   */
//  static double minDistance = Double.MAX_VALUE;
//  
//  /**
//   * The index of the closest SkipGraph.Node that will finally be returned by the Algorithm function
//   */
//  static int    minIndex    = -1;
//  
//  /**
//   * The boolean array that holds the visited SkipGraph.Node
//   * each SkipGraph.Node has a corresponding boolean value true or flase
//   * True = Visisted
//   * False = not visisted yet
//   */
//   static boolean[] nodeVisit = new boolean[Simulator.system.getSystemCapacity()];
//  
//   /**
//    * How far the startIndex SkipGraph.Node (The introducer of the SkipGraph.Node n)
//    * should go far to check for the closest SkipGraph.Node
//    */
//   static final int depthLimit = 7;
//   
//   static double totalTime = 0;
//   static double[] timeRecords = new double[depthLimit+1];
//   static int faildEstimationNumber = 0;
//   
//   static double finalAverageFailedEstimation = 0;
//   static double finalTotalTime = 0;
//   
//  /**
//   * the reset() function will reset the minDistance and minIndex and also
//   * nodeVisit[] to their default values. This function will only be called
//   * when the algorithm function starts.
//   */
//  static void internalReset()
//  {
//	  totalTime = 0;
//	  minDistance = Double.MAX_VALUE;
//      minIndex    = -1;
//	  for(int i = 0 ; i < Simulator.system.getSystemCapacity() ; i++)
//		  nodeVisit[i] = true;
//	  for(int i = 0 ; i < depthLimit+1 ; i++)
//		  timeRecords[i] = 0;
//  }
//  
//  /**
//   * to reset the failedEstimateNumber for each topology
//   */
//  static void externalReset()
//  {
//	  faildEstimationNumber = 0;
//  }
//  
//  static void FindTheClosest(Point P, int startIndex)
//  {
//	if(Simulator.system.getNodeIndex() != 0)
//		FindTheClosest(P, startIndex, startIndex, 0);
//  }
//  
//  static void FindTheClosest(Point P, int index, int before , int depth)
//  {
//	  nodeVisit[index] = false;  
//	  if(depth <= depthLimit)
//	  {
//		  if(timeRecords[depth] <  SkipGraph.Nodes.getNode(index).mCoordinate.distance(SkipGraph.Nodes.getNode(before).mCoordinate))
//			  timeRecords[depth] = SkipGraph.Nodes.getNode(index).mCoordinate.distance(SkipGraph.Nodes.getNode(before).mCoordinate);
//	  }
//	  if(P.distance(SkipGraph.Nodes.getNode(index).mCoordinate) < minDistance)
//	  {
//		  minDistance = P.distance(SkipGraph.Nodes.getNode(index).mCoordinate);
//		  minIndex = index;
//	  }
//	  
//	  depth++;
//	  
//	  if(depth <= depthLimit)
//	  {
//		  for(int i = 0 ; i < Simulator.system.getNameIDLength() ; i++)
//			  for(int j = 0 ; j < 2 ; j++)
//			  {
//				  if(SkipGraph.Nodes.getNode(index).getLookup(i, j)!= -1)
//				  {
//					  before = index;
//					  FindTheClosest(P, SkipGraph.Nodes.getNode(index).getLookup(i, j), before , depth);
//				  }
//					  
//			  }
//	  }
//	  else
//	  {
//		  return;
//	  }
//		  
//  }
//  
//  public static int Algorithm(SkipGraph.Node n)
//  {
//	  internalReset();
//	  FindTheClosest(n.mCoordinate, n.introducer);
//	  for(int i = 0 ; i < depthLimit+1 ; i++)
//		  totalTime += timeRecords[i];
//	  evaluation(n, minIndex);
//	  return minIndex;
//	  
//  }
//  
//  static int findTheReal(SkipGraph.Node n)
//  {
//	 double realMinDistance = Double.MAX_VALUE;
//	 int    realMinIndex    = 0;
//	 
//	 for(int i = 0 ; i < Simulator.system.getNodeIndex()+ 1 ; i++)
//	 {
//		 //if the distance of the SkipGraph.Node n to the SkipGraph.Node number i is less than the realMinDistance
//		 if(SkipGraph.Nodes.getNode(i).mCoordinate.distance(n.mCoordinate) < realMinDistance)
//			 {
//			   realMinDistance = SkipGraph.Nodes.getNode(i).mCoordinate.distance(n.mCoordinate);
//			   realMinIndex = i;
//			 }
//		 
//	 }
//	 
//	 return realMinIndex;
//  }
//  
//  public static void evaluation(SkipGraph.Node n, int index)
//  {
//	if(index != findTheReal(n))
//		faildEstimationNumber++;
//	if(Simulator.system.getNodeIndex() == Simulator.system.getSystemCapacity()-1)
//	{
//		double accuracy = Simulator.system.getSystemCapacity() - faildEstimationNumber;
//		accuracy = accuracy / Simulator.system.getSystemCapacity();
//		accuracy = accuracy * 100;
//		System.out.println("\n\n The accuracy of the simulation is: " 
//	    +  accuracy);
//		finalAverageFailedEstimation += accuracy;
//		finalTotalTime += totalTime;
//		
//		if(Simulator.system.getTopologyNumbers() == Simulator.system.getCurrentTopologyIndex())
//		{
//			finalAverageFailedEstimation /= Simulator.system.getTopologyNumbers();
//			finalTotalTime /= Simulator.system.getSystemCapacity();
//			System.out.println("The overal accuracy of the estimation for " + Simulator.system.getTopologyNumbers()
//					+ " runs is " + finalAverageFailedEstimation + 
//					"\nThe total average time to find the closest SkipGraph.Node " + finalTotalTime);
//		}
//	}
//	
//		
//  }
//  
//
//  
//  
//  
//}
