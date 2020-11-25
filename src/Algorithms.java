import java.text.DecimalFormat;
import java.util.ArrayList;

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

	public String answerQueries() {
		String[] event;
		System.out.println("size: " + bN.getQueries().size());
		for (String query : bN.getQueries()) {
			int algo_num = Integer.parseInt(query.substring(query.length() - 1, query.length()));
			parseQuery(query);
			if(hasDirectAnswer()) {
				output += new DecimalFormat("0.00000").format(bN.getVar(query_vars.get(0)).returnProb(query_vals.get(0), query_vars, query_vals))+ ",0,0\n";
				System.out.println("Direct answer: "+ output);
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

	private boolean hasDirectAnswer() {
		VariableNode var = bN.getVar(query_vars.get(0));
		//query_vars contains var.
		if((var.getParents().size()+1)!=query_vars.size())
			return false;
		for(String parent : var.getParents() )
			if(!query_vars.contains(parent))
				return false;
		return true;
	}

	private void parseQuery(String query) {
		query = query.substring(0, query.length() - 3).substring(2);
		String[] var = query.split("\\|")[0].split("=");
		String[] evidences = query.split("\\|")[1].split(",");
		String[] parseEvidence;
		query_vars.clear();
		query_vals.clear();
		System.out.println(var);
		query_vars.add(var[0]);
		query_vals.add(var[1]);

		for (int i = 0; i < evidences.length; i++) {
			parseEvidence = evidences[i].split("=");
			query_vars.add(parseEvidence[0]);
			query_vals.add(parseEvidence[1]);
		}

	}

	private void fullJointDistribution() {

		ArrayList<String> others_variables = new ArrayList<String>();
		ArrayList<String> all_variables = bN.getVarsName();
		for (int i = 0; i < all_variables.size(); i++) {
			if (!query_vars.contains(all_variables.get(i)))
				others_variables.add(all_variables.get(i));
		}
		System.out.println("variables:" + query_vars);
		System.out.println("all_variables:" + all_variables);
		System.out.println("others_variables:" + others_variables);
		ArrayList<ArrayList<String>> others_values = findCombinations(others_variables);
		ArrayList<String> values = new ArrayList<String>();
		query_vars.addAll(others_variables);
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
		System.out.println(output);
//		System.out.println(
//				"The answer is : " + original_combination / sum_all + ", num of plus: " + sum_plus + ", num of x: " + sum_x);
//		for(int i = 0; i< all_variables.size(); i++) {
//			System.out.println(bN.getVar().get(variables.get(i)).returnProb(domains.get(i), variables, domains));
//		}
	}

	private int findValueInd(String var_name, String value) {
		return bN.getVar(var_name).getVarDomain().indexOf(value);
	}

	private ArrayList<ArrayList<String>> findCombinations(ArrayList<String> others_variables) {
		int combinations_number = 1;
		ArrayList<ArrayList<String>> all_combinations = new ArrayList<ArrayList<String>>();
		for (String var_name : others_variables) {
			combinations_number *= bN.getVar(var_name).getVarDomain().size();
		}
		System.out.println(combinations_number);
		for (int i = 0; i < combinations_number; i++) {
			int j = 1;
			ArrayList<String> combination = new ArrayList<String>();
			for (String var_name : others_variables) {
				ArrayList<String> var_values = bN.getVar(var_name).getVarDomain();
				combination.add(var_values.get((i / j) % var_values.size()));
				j *= var_values.size();
			}
			System.out.println("List: " + combination + " j: " + j);
			all_combinations.add(combination);
		}
		return all_combinations;
	}

	private String variableElimination() {
		ArrayList<Factor> factors = new ArrayList<Factor>();
		for(String var_name : query_vars) {
			
		}
		return null;
	}

	private String variableEliminationHeuristic() {
		// TODO Auto-generated method stub
		return null;
	}

}
