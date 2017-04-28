import java.io.*;
import java.net.*;
import java.util.*;

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

						instanceRouter.changeDVCost(dstIP, dstPort, cost);

					}
					else if(inputList[0].equals("MSG"))
					{

					}

				}

		}
		
	}
}