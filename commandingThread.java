import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;


public class commandingThread implements Runnable
{
	router instanceRouter;

	public commandingThread(router r)
	{
		instanceRouter = r;
	}

	public void run()
	{

			Scanner scan = new Scanner(System.in);

			System.out.println("Enter PRINT, MSG, CHANGE commands with its correct parameters...");

			//MSG <dst-ip> <dst-port> <msg> - send message msg to a destination with the specified address.
			//CHANGE <dst-ip> <dst-port> <new-weight> - change the weight between the current node and the specified node to new-weight and update the specified node about the change.

			while(scan.hasNextLine()){

				String input = scan.nextLine();

				// print the current nodes distance vector and the distance vectors received from the neighbors
				if(input.equals("PRINT")){
					ArrayList<String> toPrintDV = instanceRouter.toStringDV();

					for(int i = 0; i < toPrintDV.size(); i++)
					{
						System.out.println(toPrintDV.get(i));
					}
				}
				else{
					String[] inputList = input.split(" ");
					if(inputList[0].equals("CHANGE"))
					{
						String dstIP = inputList[1];
						String dstPort = inputList[2];
						int cost = Integer.parseInt(inputList[3]);

						boolean change = instanceRouter.changeDVCost(dstIP, dstPort, cost);
						String updateData = dstIP + ":" + dstPort + ":" + cost;

						if(change)
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
									String data = "WU//";
									data = data + updateData;
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
					else if(inputList[0].equals("MSG"))
					{
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
		
	}
}