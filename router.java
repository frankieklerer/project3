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

  private static String routerKey;

  // every router need to know whether they can use Poisoned Reverse or not
  // if 0 , router does not use PR. if 1, router does use PR.
	private static int poisonedReverse;

  // an Array List where each index stores an Array List which contains IP address, port number and direct cost of known routers
	private static ArrayList<ArrayList<String>> neighborTable;

  // a hashmap that maps the from IP/Port to another hashmap which maps the to IP/Port to the cost associated with the from to 
	private static HashMap<String, HashMap<String,Integer>> distanceVector;

  // forwarding table for router so router knows where to send the packet to
  private static HashMap<String, String> forwardingTable;

  /**
  * Main method that initializes the router class with threads
  **/
	public static void main(String[] args){
		 
		    if(args.length<2){
            System.out.println("The parameter needs 2 arguments: whether the router uses Poisioned Reverse or not (a 0 or a 1) and the text file which specifies the routers direct neighbors and the cost");
            System.exit(1);
        }

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

        long timerVar = 500; //5 seconds
        Timer timer = new Timer();
        //timer.scheduleAtFixedRate(new sendingDVThread(routerStatic),0,timerVar);

        // timer.schedule(new TimerTask() {
        //     @Override
        //     public void run() {
        //         sendingThread.sendDVUpdate();
        //     }
        // }, 0, timerVar);
      
	 }

   /**
   * Router constructor
   **/
	 public router(String[] args){

       // initializes distance vector
		  this.distanceVector = new HashMap<String, HashMap<String, Integer>>();

      // assigning the first argument to global variable
      this.poisonedReverse = Integer.parseInt(args[0]);
      
      this.forwardingTable = new HashMap<String, String>();
      // initializing the global array list from the method
      this.neighborTable = this.readFile(args[1]);

      //System.out.println(this.neighborTable);
	 }

   
	// method that updates the cost between two nodes
	 public boolean updateCost(String dstIP, String dstPort, int newWeight){   

	 			 // turns to true if cost if new in distance vector
	       boolean change = false;

          // source node and its distance vector
         HashMap<String, Integer> currentRouterDV = distanceVector.get(this.routerKey);

          // to node
	        String toKey = dstIP + ":" + dstPort;

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
          

          //if any nodes in this routers DV router must change and look for better paths
          int costToNode = currentRouterDV.get(toKey);

           // possible new weight is the new cost plus the cost to the node
          int totalNewWeight = newWeight + costToNode;

          // current cost to destination from harshmap
          int currentWeightToDST = currentRouterDV.get(toKey);

          // current least cost path is the total weight
          int leastCostPath = totalNewWeight;
          String tempLCPKey = "";

          Set<String> currentToNodes = currentRouterDV.keySet();
          ArrayList<String> currentToNodeList = new ArrayList<String>(currentToNodes);

          // for every node in its list
          for(int z = 0; z < currentToNodeList.size(); z++){ 

          	// get the node it uses to forward to from table
            String tempKey = forwardingTable.get(currentToNodeList.get(z));

            // if there is a coute from A to B between the two nodes
            if(hasRouteAtoB(tempKey, toKey)){

            	// get the current cost from current node to intermedaite node
              int temp1 = currentRouterDV.get(tempKey);

              // get the current ocst from intermedaite node to desintation node
              int temp2 = routeCostAtoB(tempKey, toKey);

              // add them
              int totalPossibleCost = temp1 + temp2;

              // if this proposed path is less than the current cost
              if(totalPossibleCost < leastCostPath){

              	// update cost
                leastCostPath = totalPossibleCost;

                // set the new least ccost key
                tempLCPKey = tempKey;
              }
            }
          }

          // if the least cost path is les than the total new weight
          if(leastCostPath < totalNewWeight){

          	// update the distance vector of the router
            currentRouterDV.put(toKey, leastCostPath);

            // set this as true
            change = true;

            // update the routers forwarding table
            forwardingTable.put(toKey, tempLCPKey);
          }
        

	        System.out.println("new dv calculated: ");
          ArrayList<String> toPrintDV = this.toStringforAmirsPrints();
          for(int i = 0; i < toPrintDV.size(); i++){
              System.out.println(toPrintDV.get(i));
          
          }
	        return change;
   }

   // method that changes a current routers distance vector after receiving a neighboyrs distance vector
   public boolean checkDVforChanges(String fromKey, String toKey, int newWeight){ 

   		// boolean turns true if there is a change in DV as a result of new weight  
      boolean changes = false;

      // get the routers distance vector
      HashMap<String, Integer> currentRouterDV = distanceVector.get(routerKey);
      
  		// do not change anything is the from key is equal to the from key
      if(toKey.equals(routerKey)){

      		// if the current router contains the from key and not the new weight
          if(currentRouterDV.containsKey(fromKey) && (currentRouterDV.get(fromKey) != newWeight)){

          	// update the DV
            currentRouterDV.put(fromKey, newWeight);
          }
          return false;

      // if it equals itself
      } else if(fromKey.equals(routerKey)){
          return false;
      }

     
      // cost to node is current cost
     int costToNode = currentRouterDV.get(fromKey);

      // possible new weight
      int totalNewWeight = newWeight + costToNode;
      
      // if the router odes not contain the node in its distance vector (not a neighbor)
     if(!(currentRouterDV.containsKey(toKey))){

     	// update the forwarding table
      forwardingTable.put(toKey, fromKey);

      // update the distance vector
      currentRouterDV.put(toKey, totalNewWeight);
      changes = true;
      
      // if the router already contains the node in their distance vector, check if they can updae the cost
      }else{

         int currentWeightToDST = currentRouterDV.get(toKey);

          if(totalNewWeight < currentWeightToDST){

          // update the cost in DV
          currentRouterDV.put(toKey, totalNewWeight);

          // set boolean as true
          changes = true;

          // update forwardng table
          forwardingTable.put(toKey, fromKey);
          System.out.println("ROUTER " + this.ipAddress + ":" + this.portNumber + " has changed its route to " + toKey);
          
          //check other nodes to see if update helps
          int leastCostPath = totalNewWeight;
          String tempLCPKey = "";

          Set<String> currentToNodes = currentRouterDV.keySet();
          ArrayList<String> currentToNodeList = new ArrayList<String>(currentToNodes);
          for(int z = 0; z < currentToNodeList.size(); z++){

            String tempKey = forwardingTable.get(currentToNodeList.get(z));
            if(hasRouteAtoB(tempKey, fromKey)){
              int temp1 = currentRouterDV.get(tempKey);
              int temp2 = routeCostAtoB(tempKey, fromKey);
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
            forwardingTable.put(tempLCPKey,fromKey);
          }
      	}
    	}
  
      // update the routers entire distance vector 
      if(distanceVector.containsKey(fromKey)){
          HashMap<String, Integer> tempFromRouter = distanceVector.get(fromKey);
          tempFromRouter.put(toKey, newWeight);
          distanceVector.put(fromKey, tempFromRouter);
      } else {
          HashMap<String, Integer> toInsert = new HashMap<String, Integer>();
          toInsert.put(toKey, newWeight);
          distanceVector.put(fromKey, toInsert);
      }

      if(changes){
          System.out.println("new dv calculated: ");
          ArrayList<String> toPrintDV = this.toStringforAmirsPrints();
          for(int i = 0; i < toPrintDV.size(); i++){
              System.out.println(toPrintDV.get(i));
          }
      }

      return changes;
  }
	

 		/**
    * Method that reads in the neighbors.txt file for each router
    **/
    public ArrayList<ArrayList<String>> readFile(String fileName){

        // temporary array list
        ArrayList<ArrayList<String>> nodeArray = new ArrayList<ArrayList<String>>();
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
                ArrayList<String> tempRouterInfo = new ArrayList<String>();
                tempRouterInfo.add(tempIP);
                tempRouterInfo.add(tempPort);
                tempRouterInfo.add(tempCost);
               
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
        System.out.println("Router " + routerKey + " has been intialized with neighbors " + dv);
        return nodeArray;
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
			input = input + " to:" + toKey + ":" + cost;
      output.add(input);
		}
		
		return output;
	}

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


  public boolean hasRouteto(String toKey){
    return this.forwardingTable.containsKey(toKey);
  }

  public boolean hasRouteAtoB(String fromKey, String toKey){

  	// true if there is a route
    boolean returnVal = false;

    // get the from nodes distance vector
    HashMap<String, Integer> tempDV = distanceVector.get(fromKey);

    // where the node goes
    Set<String> toNodeSet = tempDV.keySet();
    ArrayList<String> toNodes = new ArrayList<String>(toNodeSet);

    // for every router the node goes to
    for(int i = 0; i < toNodes.size(); i++){

    	// if the node goes to the destination key
    	if(toNodes.get(i).equals(toKey)){
        returnVal = true;
    	}
    }
     return returnVal;
  }

  // method that returns the cost from a to b if there is a route from a to b
  public int routeCostAtoB(String fromKey, String toKey){

   // initial cost is something like nifnity
   int cost = -100;

   // get the from nodes distance vector
   HashMap<String, Integer> tempDV = distanceVector.get(fromKey);
   Set<String> toNodeSet = tempDV.keySet();
   ArrayList<String> toNodes = new ArrayList<String>(toNodeSet);

   // for every router that the from node goes to
   for(int i = 0; i < toNodes.size(); i++){

   	// get the cost of the desitnation
    if(toNodes.get(i).equals(toKey))
      cost = tempDV.get(toKey);
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

  public String getRouterKey(){
    String r = this.ipAddress + ":" + this.portNumber;
    return r;
  }

  // returns routers neighbor table
  public ArrayList<ArrayList<String>> getNeighborTable(){
  	return this.neighborTable;
  }

  public void addNeighborDV(String neighborKey, HashMap<String,Integer> neighborDV){
    this.distanceVector.put(neighborKey, neighborDV);
  }

	
}