import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

/**
* Sending thread is a thread for sending a DV update (that should happen every n seconds). 
**/

public class sendingDVThread implements Runnable {

	// instance of the router that the thread is spawning from
	private router instanceRouter;

	// IP address of the router that the thread is spawning from
	private String ipAddress;

	// port number of the router that the thread is spawning from
	private int portNumber;

	private long timerVar = 5000; //5 seconds

	private ArrayList<String> neighborTable;

	private HashMap<String,Integer> dvUpdatesReceived;

	Timer timer;

	// Constructor
	public sendingDVThread(router r){	

		// initializing instance of router
		this.instanceRouter = r;

		// initializing instance of port number of router
		this.portNumber = Integer.parseInt(instanceRouter.getRouterPort());

		// initializing instance of IP address of router
		this.ipAddress = instanceRouter.getRouterIP();

		this.dvUpdatesReceived = new HashMap<String, Integer>();

		this.neighborTable = instanceRouter.getNeighborTable();
		timer = new Timer();
		timer.schedule(new RemindTask(), 0, timerVar);

		for(int i = 0; i < neighborTable.size(); i++){
			dvUpdatesReceived.put(neighborTable.get(i),0);
		}

	}
	@Override
	public void run() {}
	//must send dv update to all neighbors
	public void sendDVUpdate(){

		System.out.println("Router " + this.ipAddress+":"+this.portNumber+ " is sending DV update to neighbors");
 		// fetch the routers neighbor table
		ArrayList<String> neighborTable = this.instanceRouter.getNeighborTable();

		// for every router in its neighbor table, send them an update
		for(String neighborRouterInfo: neighborTable){

			String[] tempinfo = neighborRouterInfo.split(":");
			// get neighbors IP and port
			String neighborIP = tempinfo[0];
			
			Integer neighborPort = Integer.parseInt(tempinfo[1]);
			try{

				//BufferedReader inFromUser =new BufferedReader(new InputStreamReader(System.in));
				DatagramSocket clientSocket = new DatagramSocket();
				InetAddress IPaddress = InetAddress.getByName("127.0.0.1");
				//System.out.println("Router " + this.ipAddress + ":" + this.portNumber + " has sent a packet to " + clientSocket.getPort() + ":" + clientSocket.getInetAddress());

				byte[] sendData = new byte[1024];
				byte[] receiveData = new byte[1024];
				ArrayList<String> distanceVectors = this.instanceRouter.toStringDV();
				String data = "DVU//";

				for(String tempRouterInfo : distanceVectors){
					data += tempRouterInfo + "//";
				}

				sendData = data.getBytes();
				DatagramPacket sendPacket =	new DatagramPacket(sendData, sendData.length, IPaddress, neighborPort);
				clientSocket.send(sendPacket);
				System.out.println("Router " + this.ipAddress + ":" + this.portNumber + " sending DV update to " + neighborIP + ":" + neighborPort + " data sent: " + data);

				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				clientSocket.receive(receivePacket);
				String sentence = new String(receivePacket.getData());
				System.out.println("received message " + sentence);
				parsePacket(sentence);
				clientSocket.close();

			}catch(IOException ioe){
			    System.out.println("Exception caught in sending thread of router " + this.ipAddress + ":" + this.portNumber);
		    }
		}
	}

	// method to parse packet receive to analyze message in morder to determine how to nperform necessary oprtations
	public void parsePacket(String sentence){

		boolean changes = false;
		// split message up
		String[] data = sentence.split("//");
		String packetType = data[0];

		if(packetType.equals("DVU")){

			System.out.println("Router " + this.ipAddress + " has received a distance vector update.");
			// syntax is "from:ip:port to:ip:port:cost ..."

			// for every node in the update
			for(int k = 1; k < data.length-1; k++){
				String temp = data[k];

				// split each message by node
				String[] splitNodes = temp.split(" ");

				// from node is the first node, extract its information
				String[] fromNode = splitNodes[0].split(":");

				String fromKey = fromNode[1] + ":" + fromNode[2];
				System.out.println("new DV update received from " + fromKey + " with the following distances: ");
				Integer current = dvUpdatesReceived.get(fromKey);
				dvUpdatesReceived.put(fromKey,current++);
				// split each node by ip address, port, cost
				for(int i = 1; i < splitNodes.length; i++){

					String[] toNode = splitNodes[i].split(":");
					String toKey = toNode[1] + ":" + toNode[2];
					int cost = (int)Integer.parseInt(toNode[3]);
					System.out.println(toKey + " " + cost);
					// check if THIS router has made any changes to its DV update as a result of the received DV update
					// if true, must send its DV update to neighbors
					changes = instanceRouter.checkDVforChanges(fromKey, toKey, cost);
					
					//if true, send dv update to neighbors
				}
			}
		}
	}

	class RemindTask extends TimerTask {
		int countDown = 3;
	 	// run method that the thread operates
		
		public void run() {

			sendDVUpdate();

			if(countDown > 0){
				countDown--;
			}else{
			   // for every neighbor
			   for(int i = 0; i < neighborTable.size(); i++){
			   		String currentKey = neighborTable.get(i);
			   		if(dvUpdatesReceived.get(currentKey) == 0){
			   			//drop neighbor
			   		}
			   }
			}
		}
    }

}
