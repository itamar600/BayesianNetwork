import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class represents algorithms for calculating queries on a Bayesian network.
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
	 * The method selects the query resolution algorithm based on the number that appears next to the query in the file.
	 *  Returns a string that includes the answers to all queries and how many addition and multiplication operations were required for each query.
	 * @return
	 */
	public String answerQueries() {
		for (String query : bN.getQueries()) {
			int algo_num = Integer.parseInt(query.substring(query.length() - 1, query.length()));
			parseQuery(query);
			if (hasDirectAnswer()) {
				output += new DecimalFormat("0.00000").format(
						bN.getVar(query_vars.get(0)).returnProb(query_vals.get(0), query_vars, query_vals)) + ",0,0\n";
				continue;
			}
			switch (algo_num) {
			case (1):
				fullJointDistribution();
				break;
			case (2):
				variableElimination(algo_num);
				break;
			case (3):
				variableElimination(algo_num);
				break;
			}
		}
		return output;
	}

	/**
	 * The method returns whether the answer to the query is explicit in the cpt of the query variable.
	 * @return
	 */
	private boolean hasDirectAnswer() {
		VariableNode var = bN.getVar(query_vars.get(0));
		// query_vars contains var
		if ((var.getParents().size() + 1) != query_vars.size())
			return false;
		for (String parent : var.getParents())
			if (!query_vars.contains(parent))
				return false;
		return true;
	}

	/**
	 * The method convert the query from a string to an array of variables and an array of values that are matched in their indexes.
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
	
	 ////////////////////////////////////////////////////////////
    //////////////////THE ALGORITHMS////////////////////////////
    ////////////////////////////////////////////////////////////

	/**
	 * The algorithm makes join between all the CPT according to evidence variables to answer the query.
	 */
	private void fullJointDistribution() {

		ArrayList<String> all_variables = bN.getVarsName();
		ArrayList<String> hidden_variables = findHiddenVars(bN.getVarsName()); 
		ArrayList<ArrayList<String>> others_values = findCombinations(hidden_variables);
		ArrayList<String> values = new ArrayList<String>();
		ArrayList<String> variables_names = new ArrayList<String>();
		variables_names.addAll(query_vars);
		variables_names.addAll(hidden_variables);
		int sum_x = 0, sum_plus = 0;
		double original_combination = 0, sum_combination, sum_combinations, sum_all = 0;
		int index_value = findValueInd(variables_names.get(0), query_vals.get(0));
		for (int i = 0; i < bN.getVar(variables_names.get(0)).getVarValues().size(); i++) {
			sum_combinations = 0;
			query_vals.set(0, bN.getVar(variables_names.get(0)).getVarValues().get(i));
			for (int j = 0; j < others_values.size(); j++) {
				sum_combination = 1;
				values.clear();
				values.addAll(query_vals);
				values.addAll(others_values.get(j));
				for (int z = 0; z < all_variables.size(); z++) {
					if (z == 0)
						sum_combination = bN.getVar(variables_names.get(z)).returnProb(values.get(z), variables_names, values);
					else {
						sum_combination *= bN.getVar(variables_names.get(z)).returnProb(values.get(z), variables_names, values);
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
	}

	/**
	 * The algorithm creates factors according to the evidence variables and joins them according to the hidden variables
	 * and makes an elimination of the factor according to the hidden variable, until it has no more hidden variables.
	 * then joins the remaining factors and makes an elimination according to the query variable and normalizes the answer to the query.
	 * The order for selecting the variables for the elimination depends on algo_num, if 2 then according to the ABC, if 3 by heuristic.
	 * @return
	 */
	private void variableElimination(int algo_num) {
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
				factor.convertCPTToFactor(bN.getVar(var_name), evidence);
				if (factor.getEvents().size() > 1)
					factors.add(factor);
			}
		}
		ArrayList<String> hidden_variables = findHiddenVars(relevant_vars);
		if(algo_num == 2)
			hidden_variables.sort(null);
		else
			heuristicSort(hidden_variables);
		ArrayList<Integer> hidden_factors_indexes;
		String hidden_name;
		ArrayList<Integer> indexes_to_join = null;
		while(!hidden_variables.isEmpty()) {
			hidden_name = hidden_variables.get(0);
			hidden_variables.remove(0);
			hidden_factors_indexes = new ArrayList<Integer>();
			int i = 0;
			while(factors.size() > 1) {
				//int j=-1;
				hidden_factors_indexes.clear();
				for( i = 0; i < factors.size(); i++ ) {
					if(factors.get(i).getVarsName().contains(hidden_name))
						hidden_factors_indexes.add(i);
				}
				if(hidden_factors_indexes.size() < 2)
					break;
				indexes_to_join = pickTwoToJoin(hidden_factors_indexes, factors);
				System.out.println("Factors to join: " + factors.get(indexes_to_join.get(0)) + ", " + factors.get(indexes_to_join.get(1)));
				factors.set(indexes_to_join.get(0), factors.get(indexes_to_join.get(0)).join(factors.get(indexes_to_join.get(1))));
				int index_to_remove = indexes_to_join.get(1);
				factors.remove(index_to_remove);
				System.out.println("Factor after join: " + factors.get(indexes_to_join.get(0)));
				}
			factors.get(indexes_to_join.get(0)).elimination(hidden_name);
			System.out.println("events, size: "+ factors.get(indexes_to_join.get(0)).getEvents() + ", " + factors.get(indexes_to_join.get(0)).getEvents().size());
			if(factors.get(indexes_to_join.get(0)).getEvents().size() == 1) {
				int index_to_remove = indexes_to_join.get(0);
				sum_x += factors.get(index_to_remove).getSumX();
				sum_plus += factors.get(index_to_remove).getSumPlus();
				factors.remove(index_to_remove);
			}
		}
			
		for(int i=0; i<factors.size()-1; i++) {
			System.out.println("Factors to join: " + factors.get(0) + ", " + factors.get(1));
			factors.set(0, factors.get(0).join(factors.get(1)));
			factors.remove(1);
			System.out.println("Factor after join: " + factors.get(0));
			
		}
		sum_x += factors.get(0).getSumX();
		sum_plus += factors.get(0).getSumPlus();
		int index_var = factors.get(0) .getVarsName().indexOf(query_vars.get(0));
		String val = query_vals.get(0);
		double prob=0 , sum=0;
		ArrayList<String> event;
		Iterator<ArrayList<String>> events = factors.get(0).eventsIter();
		//for the normalization of the probability
		while(events.hasNext()) {
			event = events.next();
			if(sum == 0) {
				sum =  factors.get(0).getProb(event);
				System.out.println("sum1 : " + sum);
			}
			else {
				sum +=  factors.get(0).getProb(event);
				sum_plus++;
				System.out.println("sum1 : " + sum);
			}
			if(event.get(index_var).equals(val)) {
				prob = factors.get(0).getProb(event);
				System.out.println("prob : " + prob);
			}
		}
		System.out.println("prob , sum : " + prob + "," +  sum);
		output += "" +new DecimalFormat("0.00000").format (prob/sum) +","+ sum_plus + "," + sum_x + "\n";
		System.out.println("" +new DecimalFormat("0.00000").format (prob/sum) +","+ sum_plus + "," + sum_x + "\n");
	}
	
	/**
	 * The method selects an order of variables in a greedy way, according to the variable whose neighbors weight is lowest.
	 * @param hidden_variables
	 */
	private void heuristicSort(ArrayList<String> hidden_variables) {
		ArrayList<String> sort_hidden_variables = new ArrayList<String>();
		ArrayList<String> evidence = new ArrayList<String>();
		evidence.addAll(query_vars);
		evidence.remove(0);
		HeuristicGraph g = new HeuristicGraph(bN);
		int min_weight;
		int weight;
		String min_name = "";
		while(sort_hidden_variables.size() < hidden_variables.size()) {
			min_weight = Integer.MAX_VALUE;
			min_name = "";
//			System.out.println("sort: " +g.getVars() );
			for(String var_name : g.getVars()) {
				weight = g.computeNeighborsWeight(var_name, evidence);
				if(weight < min_weight) {
					min_weight = weight;
					min_name = var_name;
					
				}
			}
			if(hidden_variables.contains(min_name))
				sort_hidden_variables.add(min_name);
			g.removingVar(min_name);
			
		}
		
	}

	
    ///////////////////////////////////////////////////////////////////////////////
          ///////////////////////////TOOLS/////////////////////////////////////
   ///////////////////////////////////////////////////////////////////////////////

	
	
	/**
	 * The method receives a variable name and a value and returns the index in the list of values of that variable.
	 * @param var_name
	 * @param value
	 * @return
	 */
	private int findValueInd(String var_name, String value) {
		return bN.getVar(var_name).getVarValues().indexOf(value);
	}
	
	/**
	 * The method returns an array of hidden variables that relevant_vars contains. 
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
	 * The method returns a Cartesian product of the values of the hidden variables.
	 * @param hidden_variables
	 * @return
	 */
	private ArrayList<ArrayList<String>> findCombinations(ArrayList<String> hidden_variables) {
		int combinations_number = 1;
		ArrayList<ArrayList<String>> all_combinations = new ArrayList<ArrayList<String>>();
		for (String var_name : hidden_variables) {
			combinations_number *= bN.getVar(var_name).getVarValues().size();
		}
		for (int i = 0; i < combinations_number; i++) {
			int j = 1;
			ArrayList<String> combination = new ArrayList<String>();
			for (String var_name : hidden_variables) {
				ArrayList<String> var_values = bN.getVar(var_name).getVarValues();
				combination.add(var_values.get((i / j) % var_values.size()));
				j *= var_values.size();
			}
			all_combinations.add(combination);
		}
		return all_combinations;
	}

	
	/**
	 * 
	 * @param hidden_factors_indexes
	 * @param factors
	 * @return
	 */
	private ArrayList<Integer> pickTwoToJoin(ArrayList<Integer> hidden_factors_indexes, ArrayList<Factor> factors ) {
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
			diff_vars = findDiffVars(factor_a.getVarsName(), factor_b.getVarsName());
			for(String var_name : diff_vars) {
				var = bN.getVar(var_name);
				num_lines *= var.getVarValues().size();
			}
			//computing the number that will be adding while doing join between thats factors
			num_lines = num_lines - Math.max(factor_a.getEvents().size(), factor_b.getEvents().size());
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
	

}
