import java.util.*;
import java.io.FileReader;

public class router {

	private int ipAddress;
	private int portNumber;
	private int poisonedReverse;

	public static void main(String[] args){

	}

	public router()
	{

	}

 	

 	
    public static ArrayList<ArrayList<Integer>> readFile(String fileName){

        ArrayList<ArrayList<Integer>> nodeArray = new ArrayList<ArrayList<Integer>>();
        int count = 0;
        Scanner scan = null;


        try{
            scan = new Scanner(new FileReader(fileName));
        }catch(Exception e){
            System.out.println("Could not open file " + e);}

        while(scan.hasNextLine())
        {
        	if(count == 0)
        	{
        		count++;
        		continue;
        	}
            String strLine = scan.nextLine();
            String[] s = strLine.split(" ");
            
        }


        return nodeArray;
    }
	
	
}