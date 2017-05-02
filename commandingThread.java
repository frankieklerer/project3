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

	// constructor
	public commandingThread(router r){
		
		// initializing instance of router
		this.instanceRouter = r;

		// initializing instance of port number of router
		this.portNumber = Integer.parseInt(instanceRouter.getRouterPort());

		// initializing instance of IP address of router
		this.ipAddress = instanceRouter.getRouterIP();
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
					String dstIP = inputList[1];
					String dstPort = inputList[2];
					int cost = Integer.parseInt(inputList[3]);

					// ask router if this weight change will cause a change in distance vector
					boolean change = instanceRouter.changeDVCost(dstIP, dstPort, cost);
					String dstData = dstIP + ":" + dstPort + ":" + cost;
					String srcData = ipAddress + ":" + portNumber + ":";

					// if the change will affect the routers distance vector
					if(change){

						// for every neighbor 
						ArrayList<ArrayList<String>> neighborTable = instanceRouter.getNeighborTable();
						for(ArrayList<String> neighborRouterInfo: neighborTable){

							// get neighbors IP and port
							String neighborIP = neighborRouterInfo.get(0);
							Integer neighborPort = Integer.parseInt(neighborRouterInfo.get(1));

							try{
								// send weight update to neighbor
								DatagramSocket clientSocket = new DatagramSocket();
								InetAddress IPAddress = InetAddress.getByName(neighborIP);
								byte[] sendData = new byte[1024];
								byte[] receiveData = new byte[1024];
								String data = "WU//";
<<<<<<< HEAD
								data += srcData + dstData;

								// format: WU//fromIP:fromPort:
								
=======
								data = data + srcData + dstData;
								System.out.println("SENDING DATA: " + data);
>>>>>>> bc7e99d1f1dd90e3212c4c3b1c51a001f8247a3b
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

					String dstIP = inputList[1];
					Integer dstPort = Integer.parseInt(inputList[2]);
					String message = inputList[3];
					
					//send message?
					try{
						DatagramSocket clientSocket = new DatagramSocket();
						InetAddress IPAddress = InetAddress.getByName(dstIP);
						byte[] sendData = new byte[1024];
						byte[] receiveData = new byte[1024];
						String data = "MSG//" + message;
						sendData = data.getBytes();
						DatagramPacket sendPacket =	new DatagramPacket(sendData, sendData.length, IPAddress, dstPort);
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
				}
			}
		}
	}
}