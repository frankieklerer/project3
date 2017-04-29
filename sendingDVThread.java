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

		sendDVUpdate();
		System.out.println("initial DV sent");
		
		Timer timer = new Timer();

		timer.schedule(new TimerTask(){ @Override
            public void run() {
                sendDVUpdate();
            }
        }, 0, timerVar);
		
	}

//must send dv update to all neighbors
	public void sendDVUpdate()
	{
		
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
				// DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				// clientSocket.receive(receivePacket);
				// String modifiedSentence = new String(receivePacket.getData());
				
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