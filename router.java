import java.util.*;
import java.io.*;
import java.net.*;

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

    // every router need to know whether they can use Poisoned Reverse or not
    // if 0 , router does not use PR. if 1, router does use PR.
	private static int poisonedReverse;

    // an Array List where each index stores an Array List which contains IP address, port number and direct cost of known routers
	private static ArrayList<ArrayList<String>> neighborTable;

    // a hashmap that maps the from IP/Port to another hashmap which maps the to IP/Port to the cost associated with the from to 
	private static HashMap<String, HashMap<String,Integer>> distanceVector;


	public static void main(String[] args){
		 
		if(args.length<2){
            System.out.println("The parameter needs 2 arguments: whether the router uses Poisioned Reverse or not (a 0 or a 1) and the text file which specifies the routers direct neighbors and the cost");
            System.exit(1);
        }

        // create new router method
        router newRouter = new router(args);

        routerStatic = newRouter;

        //Creating an object of the accepting thread
        acceptingDVThread acceptingThread = new acceptingDVThread(routerStatic);

        //Starting the accepting thread
        Thread athread = new Thread(acceptingThread);
       // athread.start();

        //Creating an object of the Sending thread
        sendingDVThread sendingThread = new sendingDVThread(routerStatic);

        //Starting the sending thread
        Thread sthread = new Thread(sendingThread);
        //sthread.start();

        //Creating an object of the commanding thread
        commandingThread commandThread = new commandingThread(routerStatic);
        
        //Starting the command thread
        Thread cthread = new Thread(commandThread);
        cthread.start();
        
	}

	public router(String[] args){

        // initializes distance vector
		this.distanceVector = new HashMap<String, HashMap<String, Integer>>();

        // assigning the first argument to global variable
        this.poisonedReverse = Integer.parseInt(args[0]);

        // initializing the global array list from the method
        this.neighborTable = this.readFile(args[1]);

        //System.out.println(this.neighborTable);

	}

    // public void receiveCommands(){
    //     try{

    //         DatagramSocket serverSocket = new DatagramSocket(9875);
    //         byte[] receiveData = new byte[1024];
    //         byte[] sendData = new byte[1024];

    //         while(true)
    //         {
    //             DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
    //             serverSocket.receive(receivePacket);
    //             String sentence = new String(receivePacket.getData());
    //             InetAddress IPAddress = receivePacket.getAddress();
    //             int port = receivePacket.getPort();

    //             System.out.println(this.distanceVector);
    //             // sendData = this.distanceVector.getBytes();
    //             // DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length, IPAddress, port);
    //             // serverSocket.send(sendPacket);
    //         }
    //     }
    //     catch(IOException ioe)
    //      {
    //         //Your error Message here
    //         System.out.println("expection yay");
    //       }
    // }

	// method that updates the cost between two nodes
	 public boolean changeDVCost(String dstIP, String dstPort, int newWeight){   
	        boolean change = false;
	        String fromKey = ipAddress + ":" + portNumber;
	        HashMap<String, Integer> sourceDV = distanceVector.get(fromKey);
	        String toKey = dstIP + ":" + dstPort;

	        Integer currentWeight = sourceDV.get(toKey);
	        if(currentWeight != newWeight) {
	      	  change = true;
	        }
	        sourceDV.put(toKey, newWeight);
	        distanceVector.put(fromKey, sourceDV);
	        System.out.println("new weight to neighbor " + dstIP + ":" + dstPort + " of " + newWeight);
	        return change;
   }

   // method that changes a current routers distance vector after receiving a neighboyrs distance vector
   public boolean checkDVforChanges(String fromKey, String toKey, int newWeight){   
      boolean changes = false;
      String routerCurrentKey = ipAddress + ":" + portNumber;
  
  		// do not change anything is the from key is equal to the from key
      if(toKey.equals(routerCurrentKey)){
          return false;
      }

      // get the routers distance vector
      HashMap<String, Integer> currentRouterDV = distanceVector.get(routerCurrentKey);
      int costToNode = currentRouterDV.get(fromKey);

      // possible new weight
      int totalNewWeight = newWeight + costToNode;

      // if the router already contains the node in their distance vector, check if they can updae the cost
      if(currentRouterDV.containsKey(toKey)){

      		// if the posssible new cost is less than the current cost
          if(totalNewWeight < currentRouterDV.get(toKey)){

          	// update the cost
            currentRouterDV.put(toKey, totalNewWeight);
            changes = true;
          }

      // if the router odes not contain the node in its distance vector (not a neighbor)
      }else{
          currentRouterDV.put(toKey, totalNewWeight);
          changes = true;
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

                dv.put(toKey, Integer.parseInt(tempCost));
                

                // add arraylist to bigger arraylist
                nodeArray.add(tempRouterInfo);
            }

        }catch(Exception e){
            System.out.println("Could not open file " + e);
        }
        
        String fromKey = ipAddress + ":" + portNumber;
        distanceVector.put(fromKey, dv);
        return nodeArray;
    }

  // method that turns the routers distance vector into a readable form
  public ArrayList<String> toStringDV(){
		ArrayList<String> output = new ArrayList<String>();
		String input = "";

		Set<String> fromKeySet = distanceVector.keySet();
		ArrayList<String> fromNodes = new ArrayList<String>(fromKeySet);

		for(int i = 0; i < fromNodes.size(); i++){
			String fromKey = fromNodes.get(i);
			HashMap<String, Integer> toKeySet = distanceVector.get(fromKey);

			input = "from:" + fromKey;

			Set<String> toNodeSet = toKeySet.keySet();
			ArrayList<String> toNodes = new ArrayList<String>(toNodeSet);

			for(int j = 0; j < toNodes.size(); j++){
				String toKey = toNodes.get(j);
				int cost = toKeySet.get(toKey);
				input = input + " to:" + toKey + ":" + cost;
			}
		output.add(input);
		}
		return output;
	}

   
   // returns current cost from one node to another
  public void cost(String fromIP, int fromPort, String toIP, int toPort){
  	//distanceVector

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

  // returns routers neighbor table
  public ArrayList<ArrayList<String>> getNeighborTable(){
  	return this,neighborTable;
  }
	
}