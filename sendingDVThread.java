import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;


public class sendingDVThread implements Runnable{

	private router instanceRouter;
	private String ipAddress;
	private Integer portNumber;
	private long timerVar = 5000; //5 seconds

	public sendingDVThread(router r){	

		this.instanceRouter = r;
		this.portNumber = Integer.parseInt(instanceRouter.getRouterPort());
		this.ipAddress = instanceRouter.getRouterIP();
	}

	public void run() {
		this.sendDVUpdate();
				
		// Timer timer = new Timer();

		// timer.schedule(new TimerTask(){ @Override
  //           public void run() {
  //               sendDVUpdate();
  //           }
  //       }, 0, timerVar);
		
	}

	//must send dv update to all neighbors
	public void sendDVUpdate(){

		System.out.println("Router " + this.ipAddress+":"+this.portNumber+ " is sending DV update to neighbors");

		// fetch the routers neighbor table
		ArrayList<ArrayList<String>> neighborTable = this.instanceRouter.getNeighborTable();

		// for every router in its neighbor table, send them an update
		for(ArrayList<String> neighborRouterInfo: neighborTable){

			String neighborIP = neighborRouterInfo.get(0);
			System.out.println("NEIGHBOR IP " + neighborIP);
			Integer neighborPort = Integer.parseInt(neighborRouterInfo.get(1));

			try{
				DatagramSocket clientSocket = new DatagramSocket();
				// InetAddress routerIP = InetAddress.getByName(this.ipAddress);
				// clientSocket.connect(routerIP, this.portNumber);
				InetAddress routerIP = InetAddress.getByName(neighborIP);
				clientSocket.connect(routerIP, neighborPort);
				//System.out.println("Router " + clientSocket.getPort() + ":" + clientSocket.getInetAddress() + " has a sending thread.");

				InetAddress IPaddress = InetAddress.getByName(neighborIP);
	

				byte[] sendData = new byte[1024];
				byte[] receiveData = new byte[1024];
				ArrayList<String> distanceVectors = this.instanceRouter.toStringDV();
				String data = "DVU//";

				for(String tempRouterInfo : distanceVectors){
					data = data + tempRouterInfo + "//";
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
			    //Your error Message here
			    System.out.println("expection yay");
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