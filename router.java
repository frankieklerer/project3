import java.util.*;
import java.io.*;
import java.net.*;

/**
* The router class responsibe for updating 
**/
public class router {

    // every router has a globally unique IP address
	private static String ipAddress;

    // every router has a globally unique port number
	private static int portNumber;

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

        //Creating an object of the accepting thread
        acceptingDVThread acceptingThread = new acceptingDVThread(router.class);

        //Starting the accepting thread
        Thread athread = new Thread(acceptingThread);
       // athread.start();

        //Creating an object of the Sending thread
        sendingDVThread sendingThread = new sendingDVThread(router.class);

        //Starting the sending thread
        Thread sthread = new Thread(sendingThread);
        //sthread.start();

        //Creating an object of the commanding thread
        commandingThread commandThread = new commandingThread(router.class);
        
        //Starting the command thread
        Thread cthread = new Thread(commandThread);
        cthread.start();
        
	}

	public router(String[] args){
		distanceVector = new HashMap<String, HashMap<String, Integer>>();

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

    public HashMap<String, HashMap<String,Integer>> getDV(){
        return this.distanceVector;
    }
 	

 	/**
    * Method that reads in the neighbors.txt file for each router
    **/
    public ArrayList<ArrayList<String>> readFile(String fileName){

        // temporary array list
        ArrayList<ArrayList<String>> nodeArray = new ArrayList<ArrayList<String>>();

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
                    this.portNumber = Integer.parseInt(myInfo[1]);

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
                String fromKey = ipAddress + " " + portNumber;
                String toKey = tempIP + " " + tempPort;

                HashMap<String, Integer> dv = new HashMap<String, Integer>();
                dv.put(toKey, Integer.parseInt(tempCost));
                distanceVector.put(fromKey, dv);

                // add arraylist to bigger arraylist
                nodeArray.add(tempRouterInfo);
            }

        }catch(Exception e){
            System.out.println("Could not open file " + e);
        }

        return nodeArray;
    }



    public void cost(String fromIP, int fromPort, String toIP, int toPort)
    {
    	//distanceVector

    }
	
	
}