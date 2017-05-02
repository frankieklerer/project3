import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.*;
/**
* Thread to accept incoming messages of a router from its neighbors d perform the necessary operations
**/
public class acceptingDVThread implements Runnable{

	// instance of the router that the thread is spawning from
	private router instanceRouter;

	// IP address of the router that the thread is spawning from
	private String ipAddress;

	// port number of the router that the thread is spawning from
	private int portNumber;

	// constructor 
	public acceptingDVThread(router r){

		// initializing instance of router
		this.instanceRouter = r;

		// initializing instance of port number of router
		this.portNumber = Integer.parseInt(instanceRouter.getRouterPort());

		// initializing instance of IP address of router
		this.ipAddress = instanceRouter.getRouterIP();
	}

	// run method that the thread operates
	public void run(){
		try{
			
			// starts a server socket to communicate
		   	DatagramSocket serverSocket = new DatagramSocket(this.portNumber);

			while(true){
				byte[] receiveData = new byte[1024];
				byte[] sendData = new byte[1024];

				// accepting thread receives a packet
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);

				// extracts message and parses it
				String incomingMessage = new String(receivePacket.getData());
				System.out.println("Router " + this.ipAddress + ":" + this.portNumber +  " has received message " + incomingMessage);
			
				boolean changed = this.parsePacket(incomingMessage);

				// if the message received changes the distance vector
				if(changed){

					// for every neighbor
					ArrayList<ArrayList<String>> neighborTable = instanceRouter.getNeighborTable();

					for(ArrayList<String> neighborRouterInfo: neighborTable){

						InetAddress IPAddress = receivePacket.getAddress();
						int port = receivePacket.getPort();
						ArrayList<String> distanceVectors = instanceRouter.toStringDV();
						String data = "DVU//";

						for(String tempRouterInfo : distanceVectors){
							data += tempRouterInfo + "//";
						}

						// send them the Dv update
						sendData = data.getBytes();
						DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length, IPAddress, port);
						serverSocket.send(sendPacket);
						System.out.println("from accepting state, sent " + data);
					}

				// if the message does not change the distance vector
				}else{

						// send a blank message
						String data = "";
					  sendData = data.getBytes();
					  InetAddress IPAddress = receivePacket.getAddress();
						int port = receivePacket.getPort();
						DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length, IPAddress, port);
						serverSocket.send(sendPacket);
						//System.out.println("from accepting state, sent " + data);
				}
			}

		// catch any exception if something goes wrong in the reader
		}catch(IOException ioe){
		    System.out.println("Exception: " + ioe + " caught in accepting thread of router " + this.ipAddress);
		 }
	}

	// method to parse packet receive to analyze message in morder to determine how to nperform necessary oprtations
	public boolean parsePacket(String sentence){

		boolean changes = false;

		// split message up
		String[] data = sentence.split("//");
		String packetType = data[0];
		//System.out.println("DATA0= " + Arrays.toString(data));
	
		// if the message is just a message from a router
		if(packetType.equals("MSG")){

		System.out.println("Message received: " + data[1] );

		// else if the message is a distance vector update
		}else if(packetType.equals("DVU")){

			//System.out.println("Router " + this.ipAddress + " has received a distance vector update.");
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
			
		// else if the message is a weight update
		}else if(packetType.equals("WU")){
			//System.out.println("DATA" + Arrays.toString(data));
			String[] changeInfo = data[1].split(":");
			//System.out.println(Arrays.toString(changeInfo));
			String fromKey = changeInfo[0] + ":" + changeInfo[1];
			String toKey = changeInfo[2] + ":" + changeInfo[3];
			Integer newcost = Integer.parseInt(changeInfo[4].trim());
			System.out.println("new weight update from nieghbor " + fromKey + " to " + toKey + " of " + newcost );

			changes = instanceRouter.checkDVforChanges(fromKey, toKey, newcost);
			//if true send dv update to nieghbors
				
			// change weight in routers distance vector
			// poisoned reverse ot not

		}

		return changes;
	}
}














