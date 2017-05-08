import java.util.*;
import java.io.*;
import java.net.*;
import java.lang.*;

/**
* The router class responsibe for updating 
**/
public class router {

	// static version of router version
	static router routerStatic; 

	// every router has a globally unique IP address
	private static String ipAddress;

	// every router has a globally unique port number
	private static String portNumber;

	// this routers key which is the ip address with the port number
  private static String routerKey;

  // every router need to know whether they can use Poisoned Reverse or not
  // if 0 , router does not use PR. if 1, router does use PR.
	private static boolean poisonedReverse = false;

  // an Array List where each index stores an Array List which contains IP address, port number and direct cost of known routers
	private static ArrayList<String> neighborTable;

  // a hashmap that maps the from IP/Port to another hashmap which maps the to IP/Port to the cost associated with the from to 
	private static HashMap<String, HashMap<String,Integer>> distanceVector;

  // forwarding table for router so router knows where to send the packet to
  private static HashMap<String, String> forwardingTable;

  private HashMap<String,Integer> dvUpdatesReceived;

  /**
  * Main method that initializes the router class with threads
  **/
	public static void main(String[] args){
		 
        
        // create new router method
        router newRouter = new router(args);

        // static instance of the router class that gets passed into the threads
        routerStatic = newRouter;

        //Creating an object of the accepting thread
        acceptingDVThread acceptingThread = new acceptingDVThread(routerStatic);

        //Starting the accepting thread
        Thread athread = new Thread(acceptingThread);
        athread.start();

        //Creating an object of the Sending thread
        sendingDVThread sendingThread = new sendingDVThread(routerStatic);

        //Starting the sending thread
        Thread sthread = new Thread(sendingThread);
        sthread.start();

        //Creating an object of the commanding thread
        commandingThread commandThread = new commandingThread(routerStatic);
        
        //Starting the command thread
        Thread cthread = new Thread(commandThread);
        cthread.start(); 
	 }

   /**
   * Router constructor
   **/
	 public router(String[] args){
     
      // initializes distance vector
      this.distanceVector = new HashMap<String, HashMap<String, Integer>>();

      this.forwardingTable = new HashMap<String, String>();
      // initializing the global array list from the method

      if(args.length == 1){
        this.neighborTable = this.readFile(args[0]);
      }else{
        // assigning the first argument to global variable
        this.poisonedReverse = true;
        this.neighborTable = this.readFile(args[1]);
      }


      this.dvUpdatesReceived = new HashMap<String, Integer>();

      for(int i = 0; i < neighborTable.size(); i++){
        dvUpdatesReceived.put(neighborTable.get(i),0);
     }

      //System.out.println(this.neighborTable);
	 }

   
	// method that updates the cost between two nodes
	 public boolean updateCost(String dstKey, int neww){   

	 			 // turns to true if cost if new in distance vector
	       boolean change = false;
         Integer newWeight = new Integer(neww);
          // source node and its distance vector
         HashMap<String, Integer> currentRouterDV = distanceVector.get(this.routerKey);

          // to node
	        String toKey = dstKey;

          if(neighborTable.contains(toKey)){ 
            // current weight from the source node to destination node
            Integer currentWeight = currentRouterDV.get(toKey);

            // if the weights are different, boolean variable it true
            if(currentWeight != newWeight) {
              change = true;  // ** needs to recalculate distance vector
            }
            // update the source nodes weight with new cost
            currentRouterDV.put(toKey, newWeight);

            // update distance vector
            distanceVector.put(this.routerKey, currentRouterDV);
          }else if(!(neighborTable.contains(toKey))){
            String forwardingKey = forwardingTable.get(toKey);
            Integer partCost = currentRouterDV.get(forwardingKey);
            Integer finalCost = partCost + newWeight;
            // update the source nodes weight with new cost
            currentRouterDV.put(toKey, finalCost);

            // update distance vector
            distanceVector.put(this.routerKey, currentRouterDV);
            change = true;
          }
          

          // gets all the destination nodes in its DV
          Set<String> currentToNodes = currentRouterDV.keySet();
          ArrayList<String> currentToNodeList = new ArrayList<String>(currentToNodes);
          
          for(int i = 0; i < currentToNodeList.size(); i++){

            String destKey = currentToNodeList.get(i);
            String forwardthruKey = forwardingTable.get(destKey);
            int costToForwardKey = currentRouterDV.get(forwardthruKey);
          //  System.out.println("CHECKING for changes from " + destKey + " if uses " + toKey + " as its forwarding key (" + forwardthruKey + ") look for new routes");
            if(destKey.equals(this.routerKey)){
              continue;
            } else if(destKey.equals(toKey)){
              change = true;
              // already updates
              continue;
            } else if(forwardthruKey.equals(toKey)){
              
              int costToDestFromForward = routeCostAtoB(forwardthruKey, destKey);

              int currentFinalCost = costToForwardKey + costToDestFromForward;

              ArrayList<String> newPath = this.possibleLeastCostPath(this.routerKey, destKey, forwardthruKey);
              String newForwardKey = newPath.get(0);
              int newCost = (int)Integer.parseInt(newPath.get(1));

             // if(newCost < currentFinalCost){

                // update the distance vector of the router
                currentRouterDV.put(destKey, currentFinalCost);

                // set this as true
                change = true;

                // update the routers forwarding table
                forwardingTable.put(destKey, newForwardKey);
              //}              
            }
          }
          change = true;
	        System.out.println("new dv calculated: ");
          ArrayList<String> toPrintDV = this.toStringforAmirsPrints();
          for(int i = 0; i < toPrintDV.size(); i++){
              System.out.println(toPrintDV.get(i));        
          }
	     return change;
   }

   // method that updates the cost between two nodes
   public boolean updateCostToFrom(String srckey, String dstKey, int neww){   

         // turns to true if cost if new in distance vector
         boolean change = false;
         Integer newWeight = new Integer(neww);
          // source node and its distance vector
         HashMap<String, Integer> currentRouterDV = distanceVector.get(this.routerKey);
          
         Integer costToSrc = currentRouterDV.get(srckey);
          // gets all the destination nodes in its DV
          Set<String> currentToNodes = currentRouterDV.keySet();
          ArrayList<String> currentToNodeList = new ArrayList<String>(currentToNodes);
          
          for(int i = 0; i < currentToNodeList.size(); i++){

            String currentKey = currentToNodeList.get(i);

             if(currentKey.equals(this.routerKey)){
              continue;
            } else if(currentKey.equals(srckey)){
              continue;
            } else if(currentKey.equals(dstKey)){

                Integer newCost = costToSrc + newWeight;

                // update the distance vector of the router
                currentRouterDV.put(dstKey, newCost);

                // set this as true
                change = true;

                // update the routers forwarding table
                forwardingTable.put(dstKey, srckey);
            } else {
                Integer tempw = new Integer(currentRouterDV.get(currentKey)-1);
                Integer newCost = newWeight + tempw;
               // System.out.println("TRYING to change weight to " + currentKey + " to new cost of " + newCost);
                // update the distance vector of the router
                currentRouterDV.put(currentKey, newCost);

                // set this as true
                change = true;

                // update the routers forwarding table
                forwardingTable.put(currentKey, srckey);
            }
                 
            
          }
          change = true;
          System.out.println("new dv calculated: ");
          ArrayList<String> toPrintDV = this.toStringforAmirsPrints();
          for(int i = 0; i < toPrintDV.size(); i++){
              System.out.println(toPrintDV.get(i));        
          }
       return change;
   }

   // method that changes a current routers distance vector after receiving a neighboyrs distance vector
   public boolean checkDVforChanges(String sourceKey, String destKey, int newWeight){ 

   		// boolean turns true if there is a change in DV as a result of new weight  
      boolean changes = false;

      // get the routers distance vector
      HashMap<String, Integer> currentRouterDV = distanceVector.get(routerKey);
      
  		// do not change anything is the from key is equal to the from key
      if(destKey.equals(this.routerKey)){

      		// if the current router contains the from key and not the new weight
          if(currentRouterDV.containsKey(sourceKey) && (currentRouterDV.get(sourceKey) != newWeight)){

          	// update the DV
            currentRouterDV.put(sourceKey, newWeight);
          }
          return false;

      // if it equals itself
      } else if(sourceKey.equals(this.routerKey)){
          return false;
      }

     
      // cost to node is current cost
     int costToNode = currentRouterDV.get(sourceKey);

      // possible new weight
      int totalNewWeight = newWeight + costToNode;
      
      // if the router odes not contain the node in its distance vector (not a neighbor)
     if(!(currentRouterDV.containsKey(destKey))){

     	// update the forwarding table
      forwardingTable.put(destKey, sourceKey);

      // update the distance vector
      currentRouterDV.put(destKey, totalNewWeight);
      changes = true;
      
      // if the router already contains the node in their distance vector, check if they can updae the cost
      }else{

         int currentWeightToDST = currentRouterDV.get(destKey);

          if(totalNewWeight < currentWeightToDST){

          // update the cost in DV
          currentRouterDV.put(destKey, totalNewWeight);

          // set boolean as true
          changes = true;

          // update forwardng table
          forwardingTable.put(destKey, sourceKey);
         // System.out.println("ROUTER " + this.ipAddress + ":" + this.portNumber + " has changed its route to " + destKey);
          
          //check other nodes to see if update helps
          int leastCostPath = totalNewWeight;
          String tempLCPKey = "";

          Set<String> currentToNodes = currentRouterDV.keySet();
          ArrayList<String> currentToNodeList = new ArrayList<String>(currentToNodes);
          for(int z = 0; z < currentToNodeList.size(); z++){

            String tempKey = forwardingTable.get(currentToNodeList.get(z));
            if(hasRouteAtoB(tempKey, sourceKey)){
              int temp1 = currentRouterDV.get(tempKey);
              int temp2 = routeCostAtoB(tempKey, sourceKey);
              int totalPossibleCost = temp1 + temp2;

              if(totalPossibleCost < leastCostPath){
                leastCostPath = totalPossibleCost;
                tempLCPKey = tempKey;
              }
             }
           }

          if(leastCostPath < totalNewWeight){
            currentRouterDV.put(tempLCPKey, leastCostPath);
            changes = true;
            forwardingTable.put(tempLCPKey,sourceKey);
          }
      	}
    	}
  
      // update the routers entire distance vector 
      if(distanceVector.containsKey(sourceKey)){
          HashMap<String, Integer> tempFromRouter = distanceVector.get(sourceKey);
          tempFromRouter.put(destKey, newWeight);
          distanceVector.put(sourceKey, tempFromRouter);
      } else {
          HashMap<String, Integer> toInsert = new HashMap<String, Integer>();
          toInsert.put(destKey, newWeight);
          distanceVector.put(sourceKey, toInsert);
      }

 
      return changes;
  }


  // method that turns the routers distance vector into a readable form
  public ArrayList<String> toStringDV(){
		ArrayList<String> output = new ArrayList<String>();
		String input = "";

    String routerKey = this.ipAddress + ":" + this.portNumber;
		
		HashMap<String, Integer> currentRouterDV = distanceVector.get(routerKey);

		input = "from:" + routerKey;

		Set<String> toNodeSet = currentRouterDV.keySet();
		ArrayList<String> toNodes = new ArrayList<String>(toNodeSet);

		for(int j = 0; j < toNodes.size(); j++){
			String toKey = toNodes.get(j);
			int cost = currentRouterDV.get(toKey);
			input += " to:" + toKey + ":" + cost;
		}
		output.add(input);

		return output;
	}

  // method that parses a nodes distace vetor for a specific program print
  public ArrayList<String> toStringforAmirsPrints(){
    ArrayList<String> output = new ArrayList<String>();
    String input = "";
    String fromKey = ipAddress + ":" + portNumber;

    HashMap<String, Integer> currentRouterDV = distanceVector.get(fromKey);

      Set<String> toNodeSet = currentRouterDV.keySet();
      ArrayList<String> toNodes = new ArrayList<String>(toNodeSet);

      for(int j = 0; j < toNodes.size(); j++){
        String toKey = toNodes.get(j);
        int cost = currentRouterDV.get(toKey);
        input = toKey + " " + cost;
	        output.add(input);
      }
    
    return output;
  }

  // checks is there is a route to the destination key
  public boolean hasRouteto(String destKey){
    return this.forwardingTable.containsKey(destKey);
  }

  // returns true or false depending on if a specific source node has a path to specific destination node
  public boolean hasRouteAtoB(String sourceKey, String destKey){

  	// true if there is a route
    boolean returnVal = false;

    // get the from nodes distance vector
   
    if(distanceVector.get(sourceKey) == null)
    {
      return false;
    }
     HashMap<String, Integer> tempDV = distanceVector.get(sourceKey);
    // where the node goes
    Set<String> toNodeSet = tempDV.keySet();
    ArrayList<String> toNodes = new ArrayList<String>(toNodeSet);

    // for every router the node goes to
    for(int i = 0; i < toNodes.size(); i++){

    	// if the node goes to the destination key
    	if(toNodes.get(i).equals(destKey)){
        returnVal = true;
    	}
    }
     return returnVal;
  }

  // returns the ip address and port number of the node that gives a shorter path by routing through it
  public ArrayList<String> possibleLeastCostPath(String sourceKey, String destKey, String currentForwardKey){
  // System.out.println("BEFORE: Router " + sourceKey + " routes thru " + currentForwardKey + " to get to " + destKey);

	 HashMap<String, Integer> sourceDV = distanceVector.get(sourceKey);
	 Set<String> toNodeSet = sourceDV.keySet();
	 ArrayList<String> toNodes = new ArrayList<String>(toNodeSet);
	 int finalCost = 100000;
	 String finalForwardKey = "";

	 // for every possible router the source router is connected to
	 for(int i = 0; i < toNodes.size(); i++){

	 	// get its key
	 	String toNodeKey = toNodes.get(i);
	 // System.out.println("current to node key " + toNodeKey);

	  // if the possible forward key is the current key or the current forward key, do nothing
	  if(toNodeKey.equals(this.routerKey)){
    	continue;
    }else if(toNodeKey.equals(currentForwardKey)){
      continue;

    // for all other nodes
    }else{

    	// get the cost from the source to the destination node
	    int costToNode = sourceDV.get(toNodeKey);
      if(distanceVector.containsKey(toNodeKey))
      {
	    HashMap<String, Integer> forwardKeyDV = distanceVector.get(toNodeKey);
	    //System.out.println(forwardKeyDV);
	    int forwardToDstCost = forwardKeyDV.get(destKey);
	    int totalPossibleCost = costToNode + forwardToDstCost;
	    if(totalPossibleCost < finalCost){
	      finalCost = totalPossibleCost;
	      finalForwardKey = toNodeKey;
	    }
	   }
	  }
  }
    ArrayList<String> returnList = new ArrayList<String>();
    returnList.add(finalForwardKey);
    returnList.add(String.valueOf(finalCost));
 //   System.out.println("AFTER: Router " + sourceKey + " will route thru " + finalForwardKey + " to get to " + destKey);
    return returnList;
  }

  // method that returns the cost from a to b if there is a route from a to b
  public Integer routeCostAtoB(String sourceKey, String destKey){

   // initial cost is something like nifnity
   Integer cost = -100;

   // get the from nodes distance vector
   HashMap<String, Integer> tempDV = distanceVector.get(sourceKey);
   Set<String> toNodeSet = tempDV.keySet();
   ArrayList<String> toNodes = new ArrayList<String>(toNodeSet);

   // for every router that the from node goes to
   for(int i = 0; i < toNodes.size(); i++){

   	// get the cost of the desitnation
    if(toNodes.get(i).equals(destKey))
      cost = tempDV.get(destKey);
   }
   // return cost
   return cost;
  }

  // returns the key destination
  public String getForwardingKeyto(String toKey){
    return this.forwardingTable.get(toKey);
  }

  // returns routers hashmpa
  public HashMap<String, HashMap<String,Integer>> getDV(){
 	 return this.distanceVector;
  }

  // returns routers IP address
  public String getRouterIP(){
  	return this.ipAddress;
  }

  // returns routers port number
	public String getRouterPort(){
  	return this.portNumber;
  }
 
  // returns routers key which is ip + port
  public String getRouterKey(){
    return this.routerKey;
  }

  // returns routers neighbor table
  public ArrayList<String> getNeighborTable(){
  	return this.neighborTable;
  }

  // method that replace the value of an existing key and will create it if doesn't exist.
  public void addNeighborDV(String neighborKey, HashMap<String,Integer> neighborDV){
  		this.distanceVector.put(neighborKey, neighborDV);
  }

  /**
    * Method that reads in the neighbors.txt file for each router
    **/
    public ArrayList<String> readFile(String fileName){

        // temporary array list
        ArrayList<String> nodeArray = new ArrayList<String>();
        HashMap<String, Integer> dv = new HashMap<String, Integer>();

        // tells the reader if its the first nline of the text file or not
        boolean firstLine = true;
        Scanner scan = null;

        try{

            scan = new Scanner(new FileReader(fileName));

            while(scan.hasNextLine()){
            
                // if it is the first line of the text file
                if(firstLine){

                    String firstStr = scan.nextLine();
                    String[] myInfo = firstStr.split(" ");
                    this.ipAddress = myInfo[0];
                    this.portNumber = myInfo[1];

                    System.out.println("Router has been created with IP address " + this.ipAddress + " and port number " + this.portNumber);

                    // already read first line
                    firstLine = false;
                    continue;
                }
            
                // all of its neighbors
                String finalStr = scan.nextLine();

                // extract node info
                String[] myNeighbors = finalStr.split(" ");
                String tempIP = myNeighbors[0];
                String tempPort = myNeighbors[1];
                String tempCost = myNeighbors[2];

                // add to an array list
                String tempRouterInfo = tempIP + ":" + tempPort;
                
               
                String toKey = tempIP + ":" + tempPort;
                forwardingTable.put(toKey,toKey);
                dv.put(toKey, Integer.parseInt(tempCost));

                // add arraylist to bigger arraylist
                nodeArray.add(tempRouterInfo);
            }
        }catch(Exception e){
            System.out.println("Could not open file " + e);
        }
        
        this.routerKey = this.ipAddress + ":" + this.portNumber;
        distanceVector.put(routerKey, dv);
        System.out.println("Router " + routerKey + " has been intialized with neighbors " + dv + " and a forwarding table " + this.forwardingTable);
        return nodeArray;
    }

   public boolean dropNeighbor(String neighborKey)
   {

       // gets all the destination nodes in its DV
      HashMap<String, Integer> currentRouterDV = distanceVector.get(routerKey);

      distanceVector.remove(neighborKey);
      currentRouterDV.remove(neighborKey);
      neighborTable.remove(neighborKey);
  
      Set<String> currentToNodes = currentRouterDV.keySet();
      ArrayList<String> currentToNodeList = new ArrayList<String>(currentToNodes);
      
      for(int i = 0; i < currentToNodeList.size(); i++){
        int t = 1000;
        Integer totalPossCost = new Integer(t);
        String destKey = currentToNodeList.get(i);
        String forwardthruKey = forwardingTable.get(destKey);
        if(forwardthruKey.equals(neighborKey))
        {
          for(int j = 0; j < neighborTable.size(); j++)
          {
            String tempNeighborKey = neighborTable.get(j);
            if(hasRouteAtoB(tempNeighborKey,destKey))
            {
              Integer costToNeighbor = currentRouterDV.get(tempNeighborKey);
              Integer costToNode = routeCostAtoB(tempNeighborKey,destKey);
              Integer tempweight = costToNeighbor + costToNode;
              if(tempweight < totalPossCost)
              {
                totalPossCost = costToNeighbor + costToNode;
                currentRouterDV.put(destKey, totalPossCost);
                forwardingTable.put(destKey, tempNeighborKey);
              }
            }
          } 
        }
      }
      
      distanceVector.put(this.routerKey, currentRouterDV);
      return true;
   }
   public Integer getRouterDVUpdates(String fromKey)
   {
    if(neighborTable.contains(fromKey))
    {
     Integer updates = dvUpdatesReceived.get(fromKey);
     return updates;
    }
    else
    {
      int i = 0;
      Integer toreturn = new Integer(i);
      return toreturn;
    }
   }

   public HashMap<String,Integer> getDVUpdatesReceived()
   {
    return dvUpdatesReceived;
   }
	
   public void dvUpdateReceived(String fromKey)
   {
    if(neighborTable.contains(fromKey))
    {
      Integer current = new Integer(dvUpdatesReceived.get(fromKey) + 1);
      dvUpdatesReceived.put(fromKey,current);
    }
   }
}