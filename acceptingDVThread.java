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
				//method that take sentence, parse DV, check dv alg, change if needed
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
}