import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 
 * @author Itamar Ziv-On
 *
 */
public class Algorithms {
	BN bN;
	ArrayList<String> query_vars;
	ArrayList<String> query_vals;
	String output;

	public Algorithms(BN bN) {
		this.bN = bN;
		query_vars = new ArrayList<String>();
		query_vals = new ArrayList<String>();
		output = "";
	}

	/**
	 * 
	 * @return
	 */
	public String answerQueries() {
		String[] event;
		// System.out.println("size: " + bN.getQueries().size());
		for (String query : bN.getQueries()) {
			int algo_num = Integer.parseInt(query.substring(query.length() - 1, query.length()));
			parseQuery(query);
			if (hasDirectAnswer()) {
				output += new DecimalFormat("0.00000").format(
						bN.getVar(query_vars.get(0)).returnProb(query_vals.get(0), query_vars, query_vals)) + ",0,0\n";
				// System.out.println("Direct answer: "+ output);
				continue;
			}
			switch (algo_num) {
			case (1):
				fullJointDistribution();
//				fullJointDistribution(query.substring(0, query.length() - 3));
				break;
			case (2):
				variableElimination();
//				variableElimination(query.substring(0, query.length() - 3));
				break;
			case (3):
				variableEliminationHeuristic();
//				variableEliminationHeuristic(query.substring(0, query.length() - 3));
				break;
			}
		}
		return output;
	}

	/**
	 * 
	 * @return
	 */
	private boolean hasDirectAnswer() {
		VariableNode var = bN.getVar(query_vars.get(0));
		// query_vars contains var.
		if ((var.getParents().size() + 1) != query_vars.size())
			return false;
		for (String parent : var.getParents())
			if (!query_vars.contains(parent))
				return false;
		return true;
	}

	/**
	 * 
	 * @param query
	 */
	private void parseQuery(String query) {
		query = query.substring(0, query.length() - 3).substring(2);
		String[] var = query.split("\\|")[0].split("=");
		String[] evidences = query.split("\\|")[1].split(",");
		String[] parseEvidence;
		query_vars.clear();
		query_vals.clear();
		query_vars.add(var[0]);
		query_vals.add(var[1]);

		for (int i = 0; i < evidences.length; i++) {
			parseEvidence = evidences[i].split("=");
			query_vars.add(parseEvidence[0]);
			query_vals.add(parseEvidence[1]);
		}

	}

	/**
	 * 
	 */
	private void fullJointDistribution() {

//		ArrayList<String> hidden_variables = new ArrayList<String>();
		ArrayList<String> all_variables = bN.getVarsName();
//		for (int i = 0; i < all_variables.size(); i++) {
//			if (!query_vars.contains(all_variables.get(i)))
//				others_variables.add(all_variables.get(i));
//		}
//		System.out.println("variables:" + query_vars);
//		System.out.println("all_variables:" + all_variables);
//		System.out.println("others_variables:" + others_variables);
		ArrayList<String> hidden_variables = findHiddenVars(bN.getVarsName()); 
		ArrayList<ArrayList<String>> others_values = findCombinations(hidden_variables);
		ArrayList<String> values = new ArrayList<String>();
		query_vars.addAll(hidden_variables);
		int sum_x = 0, sum_plus = 0;
		double original_combination = 0, sum_combination, sum_combinations, sum_all = 0;
		int index_value = findValueInd(query_vars.get(0), query_vals.get(0));
		for (int i = 0; i < bN.getVar(query_vars.get(0)).getVarDomain().size(); i++) {
			sum_combinations = 0;
			query_vals.set(0, bN.getVar(query_vars.get(0)).getVarDomain().get(i));
			for (int j = 0; j < others_values.size(); j++) {
				sum_combination = 1;
				values.clear();
				values.addAll(query_vals);
				values.addAll(others_values.get(j));
				for (int z = 0; z < all_variables.size(); z++) {
					if (z == 0)
						sum_combination = bN.getVar(query_vars.get(z)).returnProb(values.get(z), query_vars, values);
					else {
						sum_combination *= bN.getVar(query_vars.get(z)).returnProb(values.get(z), query_vars, values);
						sum_x++;
					}
				}
				if (j == 0)
					sum_combinations = sum_combination;
				else {
					sum_combinations += sum_combination;
					sum_plus++;
				}
			}
			if (i == index_value)
				original_combination = sum_combinations;
			if (i == 0)
				sum_all = sum_combinations;
			else {
				sum_all += sum_combinations;
				sum_plus++;
			}
		}
		output += new DecimalFormat("0.00000").format(original_combination / sum_all) + "," + sum_plus + "," + sum_x + "\n";
//		System.out.println(output);
//		System.out.println(
//				"The answer is : " + original_combination / sum_all + ", num of plus: " + sum_plus + ", num of x: " + sum_x);
//		for(int i = 0; i< all_variables.size(); i++) {
//			System.out.println(bN.getVar().get(variables.get(i)).returnProb(domains.get(i), variables, domains));
//		}
	}

	/**
	 * 
	 * @param var_name
	 * @param value
	 * @return
	 */
	private int findValueInd(String var_name, String value) {
		return bN.getVar(var_name).getVarDomain().indexOf(value);
	}
	
	/**
	 * 
	 * @param relevant_vars 
	 * @return
	 */
	private ArrayList<String> findHiddenVars(ArrayList<String> relevant_vars){
		ArrayList<String> hidden_vars = new ArrayList<String>();
		for (int i = 0; i < bN.getVarsName().size(); i++) {
			if (!query_vars.contains(bN.getVarsName().get(i)) && relevant_vars.contains(bN.getVarsName().get(i)) )
				hidden_vars.add(bN.getVarsName().get(i));
		}
		return hidden_vars;
	}
	
	/**
	 * 
	 * @param others_variables
	 * @return
	 */
	private ArrayList<ArrayList<String>> findCombinations(ArrayList<String> others_variables) {
		int combinations_number = 1;
		ArrayList<ArrayList<String>> all_combinations = new ArrayList<ArrayList<String>>();
		for (String var_name : others_variables) {
			combinations_number *= bN.getVar(var_name).getVarDomain().size();
		}
//		System.out.println(combinations_number);
		for (int i = 0; i < combinations_number; i++) {
			int j = 1;
			ArrayList<String> combination = new ArrayList<String>();
			for (String var_name : others_variables) {
				ArrayList<String> var_values = bN.getVar(var_name).getVarDomain();
				combination.add(var_values.get((i / j) % var_values.size()));
				j *= var_values.size();
			}
//			System.out.println("List: " + combination + " j: " + j);
			all_combinations.add(combination);
		}
		return all_combinations;
	}

	/**
	 * 
	 * @return
	 */
	private String variableElimination() {
		ArrayList<Factor> factors = new ArrayList<Factor>();
		Map<String, String> evidence = new HashMap<String, String>();
		int sum_x = 0;
		int sum_plus = 0;
		for (String variable : query_vars) {
			if(!query_vars.get(0).equals(variable))
				evidence.put(variable, query_vals.get(query_vars.indexOf(variable)));
		}
		ArrayList<String> relevant_vars = new ArrayList<String>();
		for (String var_name : bN.getVarsName()) {
			if(isRelevant(var_name)) {
				relevant_vars.add(var_name);	
				Factor factor = new Factor();
//				factor.convertCPTToFactor(bN.getVar(var_name), query_vars, query_vals);
				factor.convertCPTToFactor(bN.getVar(var_name), evidence);
				if (factor.getEvents().size() > 1)
					factors.add(factor);
			}
		}
//		System.out.println("relevant: "+ relevant_vars );
//		System.out.println("Before join, factors: " + factors);
		ArrayList<String> hidden_variables = findHiddenVars(relevant_vars);
		hidden_variables.sort(null);
//		ArrayList<Factor> hidden_factors;
		ArrayList<Integer> hidden_factors_indexes;
		String hidden_name;
		ArrayList<Integer> indexes_to_join = null;
		while(!hidden_variables.isEmpty()) {
			hidden_name = hidden_variables.get(0);
			hidden_variables.remove(0);
//			hidden_factors = new ArrayList<Factor>();
			hidden_factors_indexes = new ArrayList<Integer>();
			int i = 0;
			while(factors.size() > 1) {
				//int j=-1;
				hidden_factors_indexes.clear();
				for( i = 0; i < factors.size(); i++ ) {
					if(factors.get(i).getVarsName().contains(hidden_name))
						hidden_factors_indexes.add(i);
						//break;
				}
//				for( int z = i+1; z < factors.size(); z++ ) {
//					if(factors.get(z).getVarsName().contains(hidden_name)) {
//						j = z;
//						break;
//					}
//				}
				if(hidden_factors_indexes.size() < 2)
					break;
				indexes_to_join = pickTwoToElimination(hidden_factors_indexes, factors);
//				if (j == -1)
//					break;
//				System.out.println("indexes: "+ indexes_to_join+ "factors indexes: " + hidden_factors_indexes);
//				System.out.println("Factors to join: " + factors.get(indexes_to_join.get(0)) + ", " + factors.get(indexes_to_join.get(1)));
//				factors.set(i, factors.get(i).join(factors.get(j)));
//				factors.remove(j);
				factors.set(indexes_to_join.get(0), factors.get(indexes_to_join.get(0)).join(factors.get(indexes_to_join.get(1))));
				int index_to_remove = indexes_to_join.get(1);
				factors.remove(index_to_remove);
//				System.out.println("Factor after join: " + factors.get(indexes_to_join.get(0)));
				}
			//ArrayList<Integer> indexes_to_elimination = pickTwoToElimination(hidden_factors_indexes, factors);
			
//				factors.set(0, factors.get(0).join(factors.get(1)));
//			factors.remove(1);
			factors.get(indexes_to_join.get(0)).elimination(hidden_name);
			}
			
		for(int i=0; i<factors.size()-1; i++) {
			factors.set(0, factors.get(0).join(factors.get(1)));
			factors.remove(1);
			
		}
		sum_x += factors.get(0).sum_x;
		sum_plus += factors.get(0).sum_plus;
		int index_var = factors.get(0) .getVarsName().indexOf(query_vars.get(0));
		String val = query_vals.get(0);
		double prob=0 , sum=0;
		ArrayList<String> event;
		Iterator<ArrayList<String>> events = factors.get(0).eventsIter();
		while(events.hasNext()) {
			event = events.next();
			if(sum == 0)
				sum =  factors.get(0).getProb(event);
			else {
				sum +=  factors.get(0).getProb(event);
				sum_plus++;
			}
			if(event.get(index_var).equals(val))
				prob = factors.get(0).getProb(event);
		}
//		for(ArrayList<String> event : factors.get(0).getEvents())
		output += "" +new DecimalFormat("0.00000").format (prob/sum) +","+ sum_plus + "," + sum_x + "\n";
//		System.out.println("After join, factors: " + factors);
		
//		System.out.println("answer 2 = " + output + ","+ sum_plus + "," + sum_x + "\n");
		return null;
	}
	
	/**
	 * 
	 * @param hidden_factors_indexes
	 * @param factors
	 * @return
	 */
	private ArrayList<Integer> pickTwoToElimination(ArrayList<Integer> hidden_factors_indexes, ArrayList<Factor> factors ) {
		int min_num_lines = Integer.MAX_VALUE;
		//int num_lines = 1;
		ArrayList<String> diff_vars; 
		ArrayList<Integer> indexes_to_put;
		Map<Integer, ArrayList<Integer>> num_lines_and_indexes = new HashMap<Integer, ArrayList<Integer>>();
		VariableNode var;
		Factor factor_a, factor_b;
		for(int i = 0; i < hidden_factors_indexes.size()-1; i++ ) {
			int num_lines = 1;
			factor_a = factors.get(hidden_factors_indexes.get(i));
			factor_b = factors.get(hidden_factors_indexes.get(i+1));
//			System.out.println("factor_a: " + factor_a + "\nfactor_b: "+ factor_b);
			//check if this hidden variable or the query variable and its not exist in the diff_vars yet
//			diff_vars.clear();
//			for(String var_name : factor_a.getVarsName()) {
//				if((!query_vars.contains(var_name) || query_vars.get(0).equals(var_name)) && !diff_vars.contains(var_name) )
//					diff_vars.add(var_name);
//			}
//			for(String var_name : factor_b.getVarsName()) {
//				if((!query_vars.contains(var_name) || query_vars.get(0).equals(var_name)) && !diff_vars.contains(var_name) )
//					diff_vars.add(var_name);
//			}
			diff_vars = findDiffVars(factor_a.getVarsName(), factor_b.getVarsName());
//			System.out.println("diff: "+diff_vars );
			for(String var_name : diff_vars) {
				var = bN.getVar(var_name);
				num_lines *= var.getVarDomain().size();
			}
//			System.out.println("num_lines: "+num_lines );
			//computing the number that will be adding while doing join between thats factors
			num_lines = num_lines - Math.max(factor_a.getEvents().size(), factor_b.getEvents().size());
//			System.out.println("num_lines: " + num_lines);
			if(num_lines<min_num_lines)
				min_num_lines = num_lines;
			
			if(num_lines_and_indexes.containsKey(num_lines)) {
				ArrayList<Integer> indexes = num_lines_and_indexes.get(num_lines);
				indexes.add(hidden_factors_indexes.get(i));
				indexes.add(hidden_factors_indexes.get(i+1));
			}
			else {
				indexes_to_put = new ArrayList<Integer>();
				indexes_to_put.add(hidden_factors_indexes.get(i));
				indexes_to_put.add(hidden_factors_indexes.get(i+1));
				num_lines_and_indexes.put(num_lines, indexes_to_put);
				
			}
		}
		if(num_lines_and_indexes.get(min_num_lines).size() == 2)
			return num_lines_and_indexes.get(min_num_lines);
		else {
			int min_ascii = Integer.MAX_VALUE;
//			int sum_ascii = 0;
			ArrayList<Integer> indexes_to_return = new ArrayList<Integer>();
			for(int i = 0; i < num_lines_and_indexes.get(min_num_lines).size()-1; i+=2 ) {
				int sum_ascii = 0;
				for(String var_name : findDiffVars(factors.get(num_lines_and_indexes.get(min_num_lines).get(i)).getVarsName(), factors.get(num_lines_and_indexes.get(min_num_lines).get(i+1)).getVarsName())) {
					for(int j = 0; j < var_name.length(); j++)
						sum_ascii += (int)var_name.charAt(j);
				}
				if(sum_ascii < min_ascii) {
					min_ascii = sum_ascii;
					indexes_to_return.clear();
					indexes_to_return.add(num_lines_and_indexes.get(min_num_lines).get(i));
					indexes_to_return.add(num_lines_and_indexes.get(min_num_lines).get(i+1));
				}
			}
			return indexes_to_return;
		}
		
	} 

	/**
	 * 
	 * @param vars_name_a
	 * @param vars_name_b
	 * @return
	 */
	private ArrayList<String> findDiffVars(ArrayList<String> vars_name_a, ArrayList<String> vars_name_b) {
		ArrayList<String> diff_vars = new ArrayList<String>();
		for(String var_name : vars_name_a) {
			if((!query_vars.contains(var_name) || query_vars.get(0).equals(var_name)) && !diff_vars.contains(var_name) )
				diff_vars.add(var_name);
		}
		for(String var_name : vars_name_b) {
			if((!query_vars.contains(var_name) || query_vars.get(0).equals(var_name)) && !diff_vars.contains(var_name) )
				diff_vars.add(var_name);
		}
		return diff_vars;
	}
	
	/**
	 * 
	 * @param var_name
	 * @return
	 */
	private boolean isRelevant(String var_name) {
		if(query_vars.contains(var_name))
			return true;
//		for(String parent_name : bN.getVar(var_name).getParents()) {
//			if(isAncestor(var_name, bN.getVar(parent_name).getParents()))
		if(isAncestor(var_name, query_vars))
				return true;
		
		return false;
	}
	
	/**
	 * 
	 * @param var_name
	 * @param parents_name
	 * @return
	 */
	private boolean isAncestor(String var_name, ArrayList<String> parents_name) {
		if(parents_name == null)
			return false;
		if(parents_name.contains(var_name))
			return true;
		for(String parent_name : parents_name) {
			if(isAncestor(var_name, bN.getVar(parent_name).getParents()))
				return true;
		}
		return false;
	}
//	private int computeAscii(ArrayList<String> vars_name) {
//		for(String var_name : vars_name) {
//			if(diff_vars.contains(var_name)) {
//				for(int j = 0; j < var_name.length(); j++)
//					sum_ascii += (int)var_name.charAt(j);
//	}
	/**
	 * 
	 * @return
	 */
	private String variableEliminationHeuristic() {
		// TODO Auto-generated method stub
		return null;
	}

}
