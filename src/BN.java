import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a Bayesian network and also includes queries about this network.
 * @author Itamar Ziv-On
 *
 */
public class BN {
	private ArrayList<String> variables_name;
	private Map<String, VariableNode> variables;
	private Map<String, ArrayList<String>> variables_parents;
	private ArrayList<String> queries;
	
	
	public BN() {
		variables_name = new ArrayList<String>();
		variables = new HashMap<String, VariableNode>();
		variables_parents = new HashMap<String, ArrayList<String>>();
		queries = new ArrayList<String>();
	}
	
	/**
	 * The method adds a variable to the network.
	 * @param var_name
	 * @param var
	 */
	public void addVariable(String var_name, VariableNode var) {
		variables.put(var_name, var);
		variables_name.add(var_name);
	}
	
	/**
	 * The method adds parent to the variable located in the network.
	 * @param var_name
	 * @param parent_name
	 */
	public void addParents(String var_name, String parent_name) {
		if(variables_parents.containsKey(var_name)) {
			variables_parents.get(var_name).add(parent_name);
		}
		else {
			ArrayList<String> parents_names = new ArrayList<String>();
			parents_names.add(parent_name);
			variables_parents.put(var_name, parents_names);
		}
	}
	
	public void addQuery(String query) {
		queries.add(query);
	}
	
/////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////GETTERS/////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////  
	
	public ArrayList<String> getVarsName(){
		return variables_name;
	}
	
	public Map<String, VariableNode> getVarMap(){
		return variables;
	}
	
	public VariableNode getVar(String var_name) {
		return variables.get(var_name);
	}
	
	public Map<String, ArrayList<String>> getVarParents(){
		return variables_parents;
	}
	
	public ArrayList<String> getQueries(){
		return queries;
	}
}
