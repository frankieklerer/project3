import java.io.*;
import java.net.*;
import java.util.*;

public class commandingThread implements Runnable
{
	public void run()
	{
		try{

			Scanner scan = new Scanner(System.in);

			System.out.println("Enter PRINT, MSG, CHANGE commands with its correct parameters...");

			//MSG <dst-ip> <dst-port> <msg> - send message msg to a destination with the specified address.
			//CHANGE <dst-ip> <dst-port> <new-weight> - change the weight between the current node and the specified node to new-weight and update the specified node about the change.

			while(scan.hasNextLine()){

				String input = scan.next();

				// print the current nodes distance vector and the distance vectors received from the neighbors
				if(input.equals("PRINT")){

					DatagramSocket clientSocket = new DatagramSocket();
					InetAddress IPAddress = InetAddress.getByName("hostname");
					byte[] sendData = new byte[1024];
					byte[] receiveData = new byte[1024];
					sendData = input.getBytes();
					DatagramPacket sendPacket =	new DatagramPacket(sendData, sendData.length, IPAddress, 9875);
					clientSocket.send(sendPacket);

					System.out.println("sent to clientSocket");


					// DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					// clientSocket.receive(receivePacket);
					// String distanceVector = new String(receivePacket.getData());
					// System.out.println("FROM ROUTER:" +	distanceVector);
					clientSocket.close();

				}

			}
		}
		catch(IOException ioe)
		 {
		    //Your error Message here
		    System.out.println("expection yay");
		  }
	}
}