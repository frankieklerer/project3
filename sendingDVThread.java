import java.io.*;
import java.net.*;


public class sendingDVThread implements Runnable
{
	router instanceRouter;
	public sendingDVThread(router r)
	{
		instanceRouter = r;
	}
	public void run() 
	{
		// try{
		// 	//take DV table as parameter
		//     //BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		// 	DatagramSocket clientSocket = new DatagramSocket();
		// 	InetAddress IPAddress = InetAddress.getByName("hostname");
		// 	byte[] sendData = new byte[1024];
		// 	byte[] receiveData = new byte[1024];
		// 	String sentence = inFromUser.readLine();
		// 	sendData = sentence.getBytes();
		// 	DatagramPacket sendPacket =	new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
		// 	clientSocket.send(sendPacket);
		// 	DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		// 	clientSocket.receive(receivePacket);
		// 	String modifiedSentence = new String(receivePacket.getData());
		// 	System.out.println("FROM SERVER:" +	modifiedSentence);
		// 	clientSocket.close();
		// }
		// catch(IOException ioe)
		//  {
		//     //Your error Message here
		//     System.out.println("expection yay");
		//   }
		
	}
}