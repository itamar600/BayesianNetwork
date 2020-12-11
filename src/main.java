import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Map;

public class main {

	public static void main(String[] args) {
		int i;
		for(i=1; i<=11; i++) {
			LoadBN lb=new LoadBN("input" + i +".txt");
			BN bn=lb.getBN();
		//System.out.println(var.toString());
		//System.out.println(var.get("A"));
			Algorithms algo = new Algorithms(bn);
			saveToFile("output" + i + ".txt",algo.answerQueries());
		}
		
	}
	
	private static void saveToFile(String fileName, String solves) {
    	try 
		{
			PrintWriter pw = new PrintWriter(fileName); 
			pw.write(solves);
			pw.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
    }

}
