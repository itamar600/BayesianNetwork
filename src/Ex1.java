import java.io.PrintWriter;

public class Ex1 {

	public static void main(String[] args) {
		LoadBN lb=new LoadBN("input.txt");
		BN bn=lb.getBN();
	
		Algorithms algo = new Algorithms(bn);
		saveToFile("output.txt",algo.answerQueries());

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
