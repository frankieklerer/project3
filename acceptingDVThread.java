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
		 
			byte[] receiveData = new byte[1024];
			byte[] sendData = new byte[1024];


			while(true){

				// accepting thread receives a packet
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);

				// extracts message and parses it
				String incomingMessage = new String(receivePacket.getData());
				boolean changed = this.parsePacket(incomingMessage);

				System.out.println("Router " + this.ipAddress + ":" + this.portNumber +  " has received message " + incomingMessage);
				
				if(changed){

				ArrayList<ArrayList<String>> neighborTable = instanceRouter.getNeighborTable();

					for(ArrayList<String> neighborRouterInfo: neighborTable){

						InetAddress IPAddress = receivePacket.getAddress();
						int port = receivePacket.getPort();
						ArrayList<String> distanceVectors = instanceRouter.toStringDV();
						String data = "DVU//";

						for(String tempRouterInfo : distanceVectors){
							data += tempRouterInfo + "//";
						}
						sendData = data.getBytes();
						DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length, IPAddress, port);
						serverSocket.send(sendPacket);
						System.out.println("from accepting state, sent " + data);
					}
				}else{
						String data = "";
					    sendData = data.getBytes();
					    InetAddress IPAddress = receivePacket.getAddress();
						int port = receivePacket.getPort();
						DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length, IPAddress, port);
						serverSocket.send(sendPacket);
						System.out.println("from accepting state, sent " + data);
				}
			}
		}catch(IOException ioe){
		    System.out.println("Exception caught in accepting thread of router " + this.ipAddress);
		 }
	}

	// method to parse packet receive to analyze message in morder to determine how to nperform necessary oprtations
	public boolean parsePacket(String sentence){

		boolean changes = false;
		// split message up
		String[] data = sentence.split("//");
		String packetType = data[0];

		// if the message is just a message from a router
		if(packetType.equals("MSG")){

			System.out.println("Router " + this.ipAddress + " has received a message.");




		// else if the message is a distance vector update
		}else if(packetType.equals("DVU")){

			System.out.println("Router " + this.ipAddress + " has received a distance vector update.");

			// syntax is "from:ip:port to:ip:port:cost ..."

			// for every node in the update
			for(String temp: data){

				// split each message by node
				String[] splitNodes = temp.split(" ");

				// from node is the first node, extract its information
				String[] fromNode = splitNodes[0].split(":");
				String fromKey = fromNode[1] + ":" + fromNode[2];
				System.out.println("DV update from " + fromKey);

				// split each node by ip address, port, cost
				for(int i = 1; i < splitNodes.length; i++){

					String[] toNode = splitNodes[i].split(":");
					String toKey = toNode[1] + ":" + toNode[2];
					int cost = (int)Integer.parseInt(toNode[3]);

					// check if THIS router has made any changes to its DV update as a result of the received DV update
					// if true, must send its DV update to neighbors
					changes = instanceRouter.checkDVforChanges(fromKey, toKey, cost);
					
					//if true, send dv update to neighbors
				}
			}
			
		// else if the message is a weight update
		}else if(packetType.equals("WU")){
			String[] changeInfo = data[1].split(":");
			String fromKey = changeInfo[0] + ":" + changeInfo[1];
			String toKey = changeInfo[2] + ":" + changeInfo[3];
			int newcost = (int)Integer.parseInt(changeInfo[4]);

			changes = instanceRouter.checkDVforChanges(fromKey, toKey, newcost);
			//if true send dv update to nieghbors
				
			// change weight in routers distance vector
			// poisoned reverse ot not

		}

		return changes;
	}
}














