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

	private String routerKey;

	// constructor 
	public acceptingDVThread(router r){

		// initializing instance of router
		this.instanceRouter = r;

		// initializing instance of port number of router
		this.portNumber = Integer.parseInt(instanceRouter.getRouterPort());

		// initializing instance of IP address of router
		this.ipAddress = instanceRouter.getRouterIP();

		this.routerKey = instanceRouter.getRouterKey();
 	}

	// run method that the thread operates
	public void run(){
		try{
			
			// starts a server socket to communicate
		  DatagramSocket serverSocket = new DatagramSocket(this.portNumber);
		  System.out.println("Accepting thread created for router " + this.routerKey);

			while(true){
				byte[] receiveData = new byte[1024];
				byte[] sendData = new byte[1024];

				// accepting thread receives a packet
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);

				// extracts message and parses it
				String incomingMessage = new String(receivePacket.getData());
				System.out.println("Router " + this.routerKey +  " has received message " + incomingMessage);
			
				boolean changed = this.parsePacket(incomingMessage);
				System.out.println("new dv calculated: ");
				ArrayList<String> toPrintDV = instanceRouter.toStringforAmirsPrints();
				for(int i = 0; i < toPrintDV.size(); i++){
				  	System.out.println(toPrintDV.get(i));
				}
				// if the message received changes the distance vector
				if(changed){
					
					// for every neighbor
					ArrayList<String> neighborTable = instanceRouter.getNeighborTable();

					for(String neighborRouterInfo: neighborTable){

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
						System.out.println("from accepting state, sent to " + neighborRouterInfo + ": " + data);
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
		//System.out.println("DATA= " + Arrays.toString(data));
	
		// if the message is just a message from a router
		if(packetType.equals("MSG")){
			String toKey = data[1];
			String message = data[2];
			String srcKeys = data[3];
			String forwardKey = "";
			String[] toSplit = toKey.split(":");
			Integer finaldstPort = Integer.parseInt(toSplit[1]);

			if(instanceRouter.hasRouteto(toKey))
			{
				forwardKey = instanceRouter.getForwardingKeyto(toKey);
				String[] keysplit = forwardKey.split(":");
			    finaldstPort = Integer.parseInt(keysplit[1]);

			}

			if(toKey.equals(instanceRouter.getRouterKey()))
			{
				System.out.println("Message " + message + " received from " + srcKeys);
			}
			else{
			
			System.out.println("Message " + message + " from " + srcKeys + " to " + toKey + " forwarded to " + forwardKey);

					try{
						DatagramSocket clientSocket = new DatagramSocket();
						InetAddress IPAddress = InetAddress.getByName("127.0.0.1");
						byte[] sendData = new byte[1024];
						byte[] receiveData = new byte[1024];
						String newnewData = "MSG//" + toKey + "//"+ message + "//"+ srcKeys + " " + instanceRouter.getRouterKey();
						sendData = newnewData.getBytes();
						DatagramPacket sendPacket =	new DatagramPacket(sendData, sendData.length, IPAddress, finaldstPort);
						clientSocket.send(sendPacket);
						DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
						clientSocket.receive(receivePacket);
						String tempsentence = new String(receivePacket.getData());
						parsePacket(tempsentence);

						clientSocket.close();
					}catch(IOException ioe){
					    //Your error Message here
					    System.out.println("Exception caught in msg section of commanding thread");
				    }
				}
		// else if the message is a distance vector update
		}else if(packetType.equals("DVU")){

			//System.out.println("Router " + this.ipAddress + " has received a distance vector update.");
			// syntax is "from:ip:port to:ip:port:cost ..."

			// for every node in the update
			for(int k = 1; k < data.length-1; k++){
				String temp = data[k];

				// split each message by node
				String[] splitNodes = temp.split(" ");
				//System.out.println("nodes " + Arrays.toString(splitNodes));
				// from node is the first node, extract its information
				String[] sourceNode = splitNodes[0].split(":");
				//System.out.println("received message from " + Arrays.toString(sourceNode));
				String sourceKey = sourceNode[1] + ":" + sourceNode[2];
				instanceRouter.dvUpdateReceived(sourceKey);

				System.out.println("new DV update received from " + sourceKey + " with the following distances: ");
				HashMap<String,Integer> neighborDV = new HashMap<String,Integer>();

				// split each node by ip address, port, cost
				for(int i = 1; i < splitNodes.length; i++){

					String[] destNode = splitNodes[i].split(":");
					String destKey = destNode[1] + ":" + destNode[2];

					int cost = (int)Integer.parseInt(destNode[3]);

					System.out.println(destKey + " " + cost);
					neighborDV.put(destKey,cost);
					// check if THIS router has made any changes to its DV update as a result of the received DV update
					// if true, must send its DV update to neighbors
					changes = instanceRouter.checkDVforChanges(sourceKey, destKey, cost);
				}
				instanceRouter.addNeighborDV(sourceKey, neighborDV);
			}
			
		// else if the message is a weight update
		}else if(packetType.equals("WU")){

			//System.out.println("DATA" + Arrays.toString(data));
			String[] changeInfo = data[1].split(":");
			//System.out.println(Arrays.toString(changeInfo));
			String sourceKey = changeInfo[0] + ":" + changeInfo[1];
			String destKey = changeInfo[2] + ":" + changeInfo[3];
			Integer newcost = Integer.parseInt(changeInfo[4].trim());
			System.out.println("new weight update to neighbor " + sourceKey + " to " + destKey + " of " + newcost );
			if(destKey.equals(instanceRouter.getRouterKey()))
			{
				changes = instanceRouter.updateCost(sourceKey, newcost);
			}
			else
			{
				changes = instanceRouter.updateCost(destKey, newcost);
			}
			
			//if true send dv update to nieghbors
				
			// change weight in routers distance vector
			// poisoned reverse ot not

		}

		return changes;
	}
}














