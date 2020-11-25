import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Factor {

	private ArrayList<String> vars;
	private Map<ArrayList<String>, Map<ArrayList<String>,Double>> factor;
	
	public Factor() {
		vars = new ArrayList<String>();
		factor = new  HashMap<ArrayList<String>, Map<ArrayList<String>,Double>>();
	}
	
	public Map<ArrayList<String>, Map<ArrayList<String>,Double>> getFactor(){
		return factor;
	}
	
	public ArrayList<String> getVars(){
		return vars;
	}
	
	public void convertCPTToFactor(VariableNode var, ArrayList<String> evidance_vars, ArrayList<String> evidance_vals){
		Map<String, String> common_vars = new HashMap<String, String>();
		ArrayList<String> common_vars_name= new ArrayList<String>();
		for(String variable : evidance_vars ) {
			if((var.getParents().contains(variable)) || var.getName().equals(variable)) {
				common_vars.put(variable, evidance_vals.get(evidance_vars.indexOf(variable)));
				common_vars_name.add(variable);
			}
		}
		
		
	}
	
	public void join(Factor f) {
		
	}
		
	
}
