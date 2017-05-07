import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

/**
* Commanding thread to read commands from the command line and perform the necessary operations
**/
public class commandingThread implements Runnable{

	// instance of the router that the thread is spawning from
	private router instanceRouter;

	// IP address of the router that the thread is spawning from
	private String ipAddress;

	// port number of the router that the thread is spawning from
	private Integer portNumber;

	private String routerKey;

	// constructor
	public commandingThread(router r){
		
		// initializing instance of router
		this.instanceRouter = r;

		// initializing instance of port number of router
		this.portNumber = Integer.parseInt(instanceRouter.getRouterPort());

		// initializing instance of IP address of router
		this.ipAddress = instanceRouter.getRouterIP();
		routerKey = instanceRouter.getRouterKey();
	}

	// run method that the thread operates
	public void run(){

		// scanner that thread uses to read command lines
		Scanner scan = new Scanner(System.in);
		//System.out.println("Enter PRINT, MSG, CHANGE commands with its correct parameters...");

		while(scan.hasNextLine()){

			String input = scan.nextLine();

			// print the current nodes distance vector and the distance vectors received from the neighbors
			if(input.equals("PRINT")){

				// fetch the routers distance vector in a readable array list
				ArrayList<String> toPrintDV = instanceRouter.toStringDV();

				// for every element in array list, print it out
				for(int i = 0; i < toPrintDV.size(); i++){
					System.out.println(toPrintDV.get(i));
				}

			// parse input to see if the command was CHANGE or MSG
			}else{

				String[] inputList = input.split(" ");

				//CHANGE <dst-ip> <dst-port> <new-weight> 
				//change the weight between the current node and the specified node to new-weight and update the specified node about the change.
				if(inputList[0].equals("CHANGE")){

					// get destination IP and port
					String dstKey = inputList[1] + ":" + inputList[2];
					int cost = Integer.parseInt(inputList[3]);

					// ask router if this weight change will cause a change in distance vector and change the weight in distance vector
					boolean change = instanceRouter.updateCost(dstKey, cost);

					// formatting source and destination
					String dstData = dstKey + ":" + cost;
					String srcData = ipAddress + ":" + portNumber + ":";

					// if the change will affect the routers distance vector
					if(change){

						// for every neighbor 
						ArrayList<String> neighborTable = instanceRouter.getNeighborTable();
						for(String neighborRouterInfo: neighborTable){
							String[] tempinfo = neighborRouterInfo.split(":");
							// get neighbors IP and port
							String neighborIP = tempinfo[0];
							
							Integer neighborPort = Integer.parseInt(tempinfo[1]);

							try{
								// send weight update to neighbor
								DatagramSocket clientSocket = new DatagramSocket();
								InetAddress IPAddress = InetAddress.getByName(neighborIP);
								byte[] sendData = new byte[1024];
								byte[] receiveData = new byte[1024];
								String data = "WU//";
								data += srcData + dstData;

								// format: WU//fromIP:fromPort:
								
								System.out.println("SENDING DATA: " + data);
								sendData = data.getBytes();
								DatagramPacket sendPacket =	new DatagramPacket(sendData, sendData.length, IPAddress, neighborPort);
								clientSocket.send(sendPacket);
								DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
								clientSocket.receive(receivePacket);
								String sentence = new String(receivePacket.getData());
								parsePacket(sentence);

								clientSocket.close();
							}
							catch(IOException ioe)
							{
							    //Your error Message here
							    System.out.println("expection yay");
						    }
						}
					}


				//MSG <dst-ip> <dst-port> <msg> - send message msg to a destination with the specified address.
				}else if(inputList[0].equals("MSG")){
					// get destination IP and port
					String dstIP = inputList[1];
					Integer dstPort = Integer.parseInt(inputList[2]);
					String destKey = dstIP + ":" + dstPort;
					Integer finaldstPort = dstPort;
					String finaldstKey = "";

					// get the message
					String message = inputList[3];
					
					// if the router has a route key to the destination key
					if(instanceRouter.hasRouteto(destKey)){
						// get he key
						finaldstKey = instanceRouter.getForwardingKeyto(destKey);
						String[] keysplit = finaldstKey.split(":");
						finaldstPort = Integer.parseInt(keysplit[1]);
					}


				   System.out.println("Message " + message + " from " + this.routerKey + " to " + destKey + " forwarded to " + finaldstKey);

					try{
						DatagramSocket clientSocket = new DatagramSocket();
						InetAddress IPAddress = InetAddress.getByName("127.0.0.1");
						byte[] sendData = new byte[1024];
						byte[] receiveData = new byte[1024];
						String data = "MSG//" + destKey + "//"+ message + "//"+ instanceRouter.getRouterKey();
						sendData = data.getBytes();
						DatagramPacket sendPacket =	new DatagramPacket(sendData, sendData.length, IPAddress, finaldstPort);
						clientSocket.send(sendPacket);
						DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
						clientSocket.receive(receivePacket);
						String sentence = new String(receivePacket.getData());
						parsePacket(sentence);

						clientSocket.close();
					}catch(IOException ioe){
					    //Your error Message here
					    System.out.println("Exception caught in msg section of commanding thread");
				    }
				}
			}
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
			String message = data[1];


		System.out.println("Message " + message + " from " );

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
				instanceRouter.dvUpdateReceived(fromKey);
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