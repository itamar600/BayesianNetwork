import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class represents a graph of heuristics for the order of selecting the variables.
 * It allows removing a variable from the graph, adding a neighbor to a variable in the graph,
 * calculating the weight of the neighbors to a particular variable, i.e.
 * summing the product of the number of values of all the neighbors.
 * @author Itamar Ziv-On
 *
 */
public class HeuristicGraph {
	private Map<String, ArrayList<String>> neighbors;
	private BN bN;
	private ArrayList<String> relevant_vars;
	
	HeuristicGraph(BN bN, ArrayList<String> relevant_vars){
		neighbors = new HashMap<String, ArrayList<String>>();
		this.bN = bN;
		this.relevant_vars = relevant_vars;
		build();
	}
	
	/**
	 * The method turns a Bayesian network to an undirected graph that has a edge 
	 * between a child and his parent and between parents with the same child.
	 */
	public void build() {
		for(String var_name : bN.getVarsName()) {
			if(!relevant_vars.contains(var_name))
				continue;
			ArrayList<String> neighbors_name = new ArrayList<String>();
			neighbors_name.addAll(bN.getVar(var_name).getParents());
//			neighbors_name.addAll(bN.getVar(var_name).getChildren());
			for(String child :bN.getVar(var_name).getChildren()){
				if(!relevant_vars.contains(child))
					continue;
				neighbors_name.add(child);
				for(String parent : bN.getVar(child).getParents()) {
					if(!neighbors_name.contains(parent) && !var_name.equals(parent) && relevant_vars.contains(parent))
						neighbors_name.add(parent);
				}
			}
			neighbors.put(var_name, neighbors_name);
		}
	}
	
	/**
	 * The method remove a variable from the graph.
	 * @param var_name
	 */
	public void removingVar(String var_name) {
		if(neighbors.size()==1)
			return;
		if(getNeighbors(var_name).size() < 2) {
			removeNeighbor(getNeighbors(var_name).get(0), var_name);
			neighbors.remove(var_name);
			return;
		}
		for(String nei_a : getNeighbors(var_name)) {
			for(String nei_b : getNeighbors(var_name)) {
				if(!nei_a.equals(nei_b) && !getNeighbors(nei_a).contains(nei_b))
					addNeighbor(nei_a, nei_b);
			}
			
		}
		neighbors.remove(var_name);
		for(String var : neighbors.keySet()) {
			if(getNeighbors(var).contains(var_name))
				removeNeighbor(var, var_name);
		}
	}
	
	/**
	 * The method calculates the weight of the neighbors of the variable with the name var_name. 
	 * @param var_name
	 * @return
	 */
	public int computeNeighborsWeight(String var_name) {
		int sum_neibhors_multi_vals = 1;
		for(String nei : getNeighbors(var_name)) {
			sum_neibhors_multi_vals *= bN.getVar(nei).getVarValues().size();
		}
		return sum_neibhors_multi_vals;
	}
	
	/**
	 * The method calculates the weight of the neighbors of the variable with the name var_name
	 * according to evidence whose number of relevant values equals 1.
	 * @param var_name
	 * @param evidence
	 * @return
	 */
	public int computeNeighborsWeight(String var_name, ArrayList<String> evidence ) {
		int sum_neibhors_multi_vals = 1;
		for(String nei : getNeighbors(var_name)) {
			if(evidence.contains(nei))
				continue;
			sum_neibhors_multi_vals *= bN.getVar(nei).getVarValues().size();
		}
		return sum_neibhors_multi_vals;
	}
	
	/**
	 * The method adds a neighbor with the name nei_name to the variable with the name var_name.
	 * @param var_name
	 * @param nei_name
	 */
	public void addNeighbor(String var_name, String nei_name) {
		getNeighbors(var_name).add(nei_name);
	}
	
	/**
	 * The method removes the neighbor with the name nei_name from the variable with the name var_name.
	 * @param var_name
	 * @param nei_name
	 */
	public void removeNeighbor(String var_name, String nei_name) {
		getNeighbors(var_name).remove(nei_name);
	}
		
/////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////GETTERS///////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////  
	
	public ArrayList<String> getNeighbors(String var_name){
		return neighbors.get(var_name);
	}
	
	public Set<String> getVars(){
		return  neighbors.keySet();
	}
	
	
}
