import java.io.*;
import java.net.*;

public class acceptingDVThread implements Runnable
{
	router instanceRouter;
	public acceptingDVThread(router r)
	{
		instanceRouter = r;
	}
	public void run()
	{
		try{
		   	DatagramSocket serverSocket = new DatagramSocket(9876);
			byte[] receiveData = new byte[1024];
			byte[] sendData = new byte[1024];
			while(true)
			{
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				String sentence = new String(receivePacket.getData());
				InetAddress IPAddress = receivePacket.getAddress();
				int port = receivePacket.getPort();
				String capitalizedSentence = sentence.toUpperCase();
				sendData = capitalizedSentence.getBytes();
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