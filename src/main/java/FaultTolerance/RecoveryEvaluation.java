//import java.io.BufferedWriter;
//import java.io.FileWriter;
//import java.util.Random;
//
//import javax.naming.TimeLimitExceededException;
//import javax.xml.ws.FaultAction;
//
//
//public class RecoveryEvaluation 
//{
//   public static int networkLifeTime = 0;
//   public static int networkLifeTimeAverage = 0;
//   final static int  recoveryRun     = 100;
//   public static int recoveryIndex  = 0;
//   
//   
//   public static boolean init = true;
//   public static int runTime = 1000;
//   public static int currentTime = 0;
//   public static int[] failTrans = new int[runTime];
//   public static int failTransCount = 0;
//   
//   public static int[][] failTransCountDataBase = new int[Simulator.system.getTopologyNumbers()][runTime];
//   
//   public static void networkLifeTime()
//   {
//     for( ; recoveryIndex < recoveryRun ; recoveryIndex++)
//     {
//       SkipGraph.Nodes.energyReset();
//	   while(true)
//       {
//     	  Random random = new Random();
//     	  String nameID = SkipGraph.Nodes.getNode(random.nextInt(Simulator.system.getSystemCapacity()-1)).nameID;
//     	  int i = random.nextInt(Simulator.system.getSystemCapacity()-1);
//     	  int result = SkipGraph.SkipGraphOperations.SearchByNameID(nameID, i, SkipGraph.Nodes.getNode(i).getLookup(0, 1), SkipGraph.Nodes.getNode(i).getLookup(0, 0), 0);
//     	  if(result < 0)
//     		  break;
//     	  String nameResult = SkipGraph.Nodes.getNode(result).nameID;
//     	  String searchStatus = new String();
//     	  if(nameResult == nameID)
//     	  {
//     		  searchStatus = "Correct Transaction";
//     		  networkLifeTime++;
//     	  }
//     	  else
//     		  searchStatus = "Failed   Transaction";
//     	  System.out.println("Serach for " + nameID + " is " + result + " with name ID  " + nameResult + "  Status:  " +  searchStatus + "Min Battery : " + 
//     		                   SkipGraph.Nodes.minBatteryStatus() + "  Battery Average  " + SkipGraph.Nodes.networkBatteryAverage() );
//     	  if(nameResult!= nameID)
//     		  break;
//       } 
//	   
//	   System.out.println(networkLifeTime);
//	   networkLifeTimeAverage += networkLifeTime; 
//	   networkLifeTime = 0;
//     }
//    
//     networkLifeTimeAverage = networkLifeTimeAverage / recoveryRun;
//     System.out.println("\n\n\nThe average of network life time is: " + networkLifeTimeAverage);
//   }
//   
//   
//   
//   public static void TimeMonitoring()
//   {
//     if(init)
//     {
//    	 for(int i = 0 ; i < runTime ; i++)
//    		 failTrans[i] = 0;
//    	 init = false;
//     }
//	 
//     
//     //SkipGraph.Nodes.energyReset();
//	 for( ; currentTime < runTime ; currentTime++)
//     {
//		 
//		 
//		  
//		  System.out.println("Time " + currentTime + " Failure SkipGraph.Nodes "  +  failTransCount +
//				 " Min Battery " + SkipGraph.Nodes.minBatteryStatus() + "  Battery Average  " + SkipGraph.Nodes.networkBatteryAverage() );
//		  Random random = new Random();
//     	  String nameID = SkipGraph.Nodes.getNode(random.nextInt(Simulator.system.getSystemCapacity()-1)).nameID;
//     	  int i = random.nextInt(Simulator.system.getSystemCapacity()-1);
//     	  int result = SkipGraph.SkipGraphOperations.SearchByNameID(nameID, i, SkipGraph.Nodes.getNode(i).getLookup(0, 1), SkipGraph.Nodes.getNode(i).getLookup(0, 0), 0);
//     	  if(result < 0)
//     	  { 
//     		  failTransCount++;
//     		  failTrans[currentTime] = failTransCount;
//     		  continue;
//     	  }
//     	  
//     	  String nameResult = SkipGraph.Nodes.getNode(result).nameID;
//     	  if(nameResult != nameID)
//     	  {
//     		  failTransCount++;
//     		  failTrans[currentTime] = failTransCount;
//     		  continue;     		 
//     	  }
//     	  
//     	  
//     	 failTrans[currentTime] = failTransCount;
//     	    
//     }
//     
//	 System.out.println("Time = [");
//	 for(int i = 0 ; i < runTime ; i+=10)
//		 System.out.print(i + " ");
//	 System.out.println("]");
//	 
//	 System.out.println("Failure = [");
//	 for(int i = 0 ; i < runTime ; i+=10)
//		 System.out.print(failTrans[i] + " ");
//	 System.out.println("]");
//	 
//   }
//   
//   public static void multiTimeMonitoring()
//   {
//     if(init)
//     {
//    	 for(int i = 0 ; i < runTime ; i++)
//    	 {
//    		 failTrans[i] = 0;
//    		 for(int j = 0 ; j < Simulator.system.getTopologyNumbers() ; j++)
//    			 failTransCountDataBase[j][i] = 0;
//    	 }
//    	 init = false;
//     }
//	 
//     failTransCount = 0;
//     //SkipGraph.Nodes.energyReset();
//	 for(currentTime = 0 ; currentTime < runTime ; currentTime++)
//     {
//		 
//		 
//		  
//		  System.out.println("Time " + currentTime + " Failure SkipGraph.Nodes "  +  failTransCount +
//				 " Min Battery " + SkipGraph.Nodes.minBatteryStatus() + "  Battery Average  " + SkipGraph.Nodes.networkBatteryAverage() );
//		  Random random = new Random();
//     	  String nameID = SkipGraph.Nodes.getNode(random.nextInt(Simulator.system.getSystemCapacity()-1)).nameID;
//     	  int i = random.nextInt(Simulator.system.getSystemCapacity()-1);
//     	  int result = SkipGraph.SkipGraphOperations.SearchByNameID(nameID, i, SkipGraph.Nodes.getNode(i).getLookup(0, 1), SkipGraph.Nodes.getNode(i).getLookup(0, 0), 0);
//     	  if(result < 0)
//     	  { 
//     		  failTransCount++;
//     		  failTransCountDataBase[Simulator.system.getCurrentTopologyIndex()-1][currentTime] = SkipGraph.Nodes.networkBatteryAverage();//failTransCount;
//     		  continue;
//     	  }
//     	  
//     	  String nameResult = SkipGraph.Nodes.getNode(result).nameID;
//     	  if(nameResult != nameID)
//     	  {
//     		  failTransCount++;
//     		  failTransCountDataBase[Simulator.system.getCurrentTopologyIndex()-1][currentTime] = SkipGraph.Nodes.networkBatteryAverage();//failTransCount;
//     		  continue;     		 
//     	  }
//     	  
//     	  
//     	 failTransCountDataBase[Simulator.system.getCurrentTopologyIndex()-1][currentTime] = SkipGraph.Nodes.networkBatteryAverage();//failTransCount;
//     	    
//     }
//	 
//	    if(Simulator.system.getCurrentTopologyIndex() == Simulator.system.getTopologyNumbers())
//	    {
//	    	try
//	    	{
//		    	System.out.println("Matlab format Recovery Evaluation  started!");    
//			    FileWriter fstream = new FileWriter("RecoveryEvaluationResult.txt");
//			    BufferedWriter out = new BufferedWriter(fstream);
//		    	int[] Mean = new int[runTime];
//		    	int[] SD   = new int[runTime]; 
//			    for(int j = 0 ; j < Simulator.system.getTopologyNumbers(); j++)
//			    {
//	    		  Mean[j] = 0;  
//	    		  SD[j] = 0;
//			    }
//		    	for(int i = 0 ; i <runTime  ; i+=10)
//		    	{
//				    for(int j = 0 ; j < Simulator.system.getCurrentTopologyIndex(); j++)
//					    {
//			    		  Mean[i] = Mean[i] + failTransCountDataBase[j][i];   		 		
//					    }
//				    
//				    Mean[i] = Mean[i] / Simulator.system.getCurrentTopologyIndex();
//		    	}
//			    
//				for(int i = 0 ; i < runTime ; i+=10)
//				{
//					for(int j = 0 ; j < Simulator.system.getCurrentTopologyIndex() ; j++)
//					{
//				
//							SD[i] = (int) (SD[i] + Math.pow((failTransCountDataBase[j][i] - Mean[i]) , 2));						    							    		
//				    }
//				}
//			    
//			    for(int i = 0 ; i < runTime ; i+=10)
//			    {
//		    		out.write(String.valueOf(i));
//	    			out.write(" ");
//	    			    		
//	    			out.write(String.valueOf((int)(Mean[i])));
//	    			 
//	    		   
//	    		    out.write(" ");
//	    		    
//	    		  if(SD[i] != 0)
//	    			  out.write(String.valueOf((int)(Math.sqrt(SD[i]/Simulator.system.getCurrentTopologyIndex())/2)));
//	    		  else 
//	    			  out.write(String.valueOf(0));
//	    			out.newLine();
//	    			   		 		
//			    }
//			    
//			    
//			    
//			    //Close the output stream
//			    out.close();
//			    System.out.println("Evaluation is done!");
//		    }
//	 	   catch (Exception e)
//	 	   {
//	 		   e.printStackTrace();
//	 	   }
//	    	
//	    }
//	    
//	 
//   }  
//   
//}
