import java.util.Map;

public class main {

	public static void main(String[] args) {
		LoadBN lb=new LoadBN("input2.txt");
		BN bn=lb.getBN();
		Map<String,VariableNode> var= bn.getVarMap();
		System.out.println(var.toString());
		//System.out.println(var.get("A"));
		Algorithms algo = new Algorithms(bn);
		algo.answerQueries();
		
	}

}
