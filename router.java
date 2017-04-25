import java.util.*;
import java.io.FileReader;

public class router {

	private int ipAddress;
	private int portNumber;
	private int poisonedReverse;
	private ArrayList<ArrayList<String>> currentNodeList;

	public static void main(String[] args){
		
		
		if(args.length<2){
            System.out.println("need at least 2 arguements");
            System.exit(1);
        }
        poisonedReverse = Integer.parseInt(args[0]);
        currentNodeList = readFile(args[1]);
        
	}

	public router()
	{

	}

 	

 	
    public static ArrayList<ArrayList<String>> readFile(String fileName){

        ArrayList<ArrayList<String>> nodeArray = new ArrayList<ArrayList<String>>();
        boolean first = true;
        Scanner scan = null;


        try{
            scan = new Scanner(new FileReader(fileName));
        }catch(Exception e){
            System.out.println("Could not open file " + e);}

        while(scan.hasNextLine())
        {
        	if(first)
        	{
        		String strLine = scan.nextLine();
            	String[] st = strLine.split(" ");
            	ipAddress = st[0];
            	portNumber = st[1];
        		first = false;
        		continue;
        	}
        	
            String strLine = scan.nextLine();

            String[] s = strLine.split(" ");
            String tempIP = s[0];
            String tempPort = s[1];
            String tempCost = s[2];

            ArrayList<String> tempNodeInfo = new ArrayList<String>();
            tempNodeInfo.add(tempIP);
            tempNodeInfo.add(tempPort);
            tempNodeInfo.add(tempCost);

            nodeArray.add(tempNodeInfo);
        }


        return nodeArray;
    }
	
	
}