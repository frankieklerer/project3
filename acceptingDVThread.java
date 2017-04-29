import java.io.*;
import java.net.*;
import java.lang.*;

public class acceptingDVThread implements Runnable
{
	router instanceRouter;
	String ipAddress;
	Integer portNumber;

	public acceptingDVThread(router r)
	{
		instanceRouter = r;
		portNumber = Integer.parseInt(instanceRouter.getRouterPort());
		ipAddress = instanceRouter.getRouterIP();
	}
	public void run()
	{
		try{
		   	DatagramSocket serverSocket = new DatagramSocket(portNumber);
			byte[] receiveData = new byte[1024];
			byte[] sendData = new byte[1024];
			while(true)
			{
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				String sentence = new String(receivePacket.getData());

				parsePacket(sentence);
				//method that take sentence, parse DV, do dv alg, change instancerouter.dv if needed
				//if change return boolean true, and send out new dv to neighbors


				InetAddress IPAddress = receivePacket.getAddress();
				int port = receivePacket.getPort();
		//		sendData = sentence.getBytes();

				//sends back same data
				DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length, IPAddress, port);
				serverSocket.send(sendPacket);
			}
		}
		catch(IOException ioe)
		 {
		    //Your error Message here
		    System.out.println("expection yay");
		  }
		}

	public void parsePacket(String sentence)
	{
		String[] data = sentence.split("//");
		String packetType = data[0];
		if(packetType.equals("MSG"))
		{

		}
		else if(packetType.equals("DVU"))
		{
			for(String tempNode: data)
			{
				String[] fromtosplitting = tempNode.split(" ");
				String[] fromNode = fromtosplitting[0].split(":");
				String fromKey = fromNode[1] + ":" + fromNode[2];

				for(int i = 1; i < fromtosplitting.length; i++)
				{
					String[] tempToNode = fromtosplitting[i].split(":");
					String toKey = tempToNode[1] + ":" + tempToNode[2];
					int cost = (int)Integer.parseInt(tempToNode[3]);

					boolean dvChange = instanceRouter.checkDVforChanges(fromKey, toKey, cost);
					
					//if true, send dv update to neighbors
				}
			}
			

		}
		else if(packetType.equals("WU"))
		{

		}
	}
}














