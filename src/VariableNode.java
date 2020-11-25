import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class VariableNode {
	private final String name; 
	private ArrayList<String> domain_of_var;
	private ArrayList<String> parents;
	private ArrayList<String> cpt;
	private Map<String, ArrayList<String>> domain_of_parents;
	//Map<ArrayList<String>, ArrayList<Integer>> events;
	private Map<ArrayList<String>, Map<String,Double>> events_prob;
	
	public VariableNode(String name) 
	{
		this.name=name;
		domain_of_var = new ArrayList<String>();
		parents = new ArrayList<String>();
		cpt = new ArrayList<String>();
		domain_of_parents = new HashMap<String, ArrayList<String>>();
		events_prob= new HashMap<ArrayList<String>, Map<String,Double>>();
		
	}
	public void fillEvents() {
		ArrayList<String> parents_values = new ArrayList<String>();
		ArrayList<String> var_values = new ArrayList<String>();
		ArrayList<Double> prob = new ArrayList<Double>();
		Map<String, Double> values_prob= new HashMap<String, Double>();
		String[] event;
		int i,j;
		for(i=0; i<cpt.size(); i++) {
			event=cpt.get(i).split(",");
			var_values.clear();
			prob.clear();
			values_prob.clear();
			parents_values.clear();
			for(j=0;j<parents.size(); j++) {
				parents_values.add(event[j]);
			}
			for(; j<event.length;j++) {
				if(event[j].contains("="))
					var_values.add(event[j].substring(1));
				else
					prob.add(Double.parseDouble(event[j]));
			}
			for(int z=0; z<prob.size(); z++) {
				values_prob.put(var_values.get(z), prob.get(z));
			}
			double prob_sum=0;
			for(int z=0; z<domain_of_var.size(); z++) {
				if(!(values_prob.containsKey(domain_of_var.get(z)))){
					for(int x=0; x<values_prob.size(); x++) {
						if(values_prob.containsKey(domain_of_var.get(x)))
							prob_sum+=values_prob.get(domain_of_var.get(x));
					}
					values_prob.put(domain_of_var.get(z), (1-prob_sum));
					break;//only one value can be missing 
				}
			}
			if(parents.size()==0)
				events_prob.put(null,new HashMap<String, Double>(values_prob) );
			else
				events_prob.put(new ArrayList<String>(parents_values), new HashMap<String, Double>(values_prob));
			
		}
			
	}
	public double returnProb(String value, ArrayList<String> vars, ArrayList<String> vars_values ) {
		if(parents.size()==0) {
			System.out.println("name size 0: " + name);
			return events_prob.get(null).get(value);
		}
		int indexes_of_parents[]= new int[parents.size()];
		System.out.println("var name: "+ name + ", value: " + value + ", vars: " + vars + ", vars_values: "+ vars_values);
		for(int i = 0; i<vars.size(); i++) {
			if(parents.contains(vars.get(i))) {
				indexes_of_parents[parents.indexOf(vars.get(i))] = i;
				System.out.println("i: " + i + ", " + parents.indexOf(vars.get(i)));
			}
		}
		ArrayList<String> parents_values= new ArrayList<String>();
		for(int i = 0; i<parents.size(); i++) {
			parents_values.add(vars_values.get(indexes_of_parents[i]));
		}
		return(returnProb(value, parents_values));
	}
	public ArrayList<Map<ArrayList<String>,Double>> getEventsWith(Map<String, String> evidence){
		ArrayList<Map<ArrayList<String>,Double>> newCPT = new ArrayList<Map<ArrayList<String>,Double>>();
		Map<ArrayList<String>,Double> event_with_prob = new HashMap<ArrayList<String>,Double>();
		ArrayList<String> event;
		Iterator<ArrayList<String>> iter = parentsValsIter();
		boolean toAdd = true;
		ArrayList<String> event_vals = new ArrayList<String>();
		while((event = iter.next()) != null) {
			for(String var_name: evidence.keySet()) {
				if(parents.contains(var_name) && !event.get(parents.indexOf(var_name)).equals(evidence.get(var_name)))
					toAdd = false;
			}
			if(toAdd) {
				event_vals.addAll(event);
				if(evidence.containsKey(name)) {
					event_vals.add(evidence.get(name));
					event_with_prob.put(new ArrayList<String>(event_vals), events_prob.get(event).get(evidence.get(name)));
					newCPT.add(new HashMap<ArrayList<String>,Double>(event_with_prob));
				}
				else {
					int event_vals_size =  event_vals.size();
					for(String value : domain_of_var) {
						if(event_vals_size > event_vals.size()) {
							event_vals.remove(event_vals_size);
						}
						event_vals.add(value);
						event_with_prob.put(new ArrayList<String>(event_vals), events_prob.get(event).get(value));
						newCPT.add(new HashMap<ArrayList<String>,Double>(event_with_prob));
					}
				}
			}
		}
		System.out.println("newCPT: " + newCPT);
		return newCPT;
	}
	public Iterator<ArrayList<String>> parentsValsIter(){
		return events_prob.keySet().iterator();
	}
	public double returnProb(String query_value, ArrayList<String> evidences_values){
		return events_prob.get(evidences_values).get(query_value);
		
	}
	public String getName() {
		return name;
	}
	public ArrayList<String> getVarDomain(){
		return domain_of_var;
	}
	
	public ArrayList<String> getParents(){
		return parents;
	}
	
	public ArrayList<String> getCPT(){
		return cpt;
	}
	
	public Map<String, ArrayList<String>> getParentsDomain(){
		return domain_of_parents;
	}
	
	public String toString() {
		String string="";
		for(int i=0; i<parents.size(); i++) {
			string =string + "parents" + "_" + i + ": " + parents.get(i) + "   ";
		}
		string+= "var_name: " + name + "\n\n";
		string+=events_prob.toString();
		return string;
			  
		
	}

}
