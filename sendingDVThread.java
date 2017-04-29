import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;


public class sendingDVThread implements Runnable
{
	router instanceRouter;
	String ipAddress;
	Integer portNumber;
	long timerVar = 5000; //5 seconds

	public sendingDVThread(router r)
	{	

		instanceRouter = r;
		portNumber = Integer.parseInt(instanceRouter.getRouterPort());
		ipAddress = instanceRouter.getRouterIP();
	}
	public void run() 
	{
		System.out.println("initial DV sent");
		sendDVUpdate();
		
		
		// Timer timer = new Timer();

		// timer.schedule(new TimerTask(){ @Override
  //           public void run() {
  //               sendDVUpdate();
  //           }
  //       }, 0, timerVar);
		
	}

//must send dv update to all neighbors
	public void sendDVUpdate()
	{
		System.out.println("router " + this.ipAddress+":"+this.portNumber+ " sending dv update to neighbors");
		ArrayList<ArrayList<String>> neighborTable = instanceRouter.getNeighborTable();

		for(ArrayList<String> neighborRouterInfo: neighborTable)
		{
			String neighborIP = neighborRouterInfo.get(0);
			Integer neighborPort = Integer.parseInt(neighborRouterInfo.get(1));

			try{
				DatagramSocket clientSocket = new DatagramSocket();
				InetAddress IPAddress = InetAddress.getByName(neighborIP);
				byte[] sendData = new byte[1024];
				byte[] receiveData = new byte[1024];
				ArrayList<String> distanceVectors = instanceRouter.toStringDV();
				String data = "DVU//";
				for(String tempRouterInfo : distanceVectors)
				{
					data = data + tempRouterInfo + "//";
				}
				sendData = data.getBytes();
				DatagramPacket sendPacket =	new DatagramPacket(sendData, sendData.length, IPAddress, neighborPort);
				clientSocket.send(sendPacket);
				System.out.println("sending threadto " + neighborIP + ":" + neighborPort + " data sent: " + data);

				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				clientSocket.receive(receivePacket);
				String sentence = new String(receivePacket.getData());
				System.out.println("received message " + sentence);
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