import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class represents a factor, and allows to make a join between two factors and elimination.
 * @author Itamar Ziv-On
 *
 */
public class Factor {

	private ArrayList<String> factor_vars_name;
	private Map<ArrayList<String>, Map<ArrayList<String>, Double>> factor;
	private int sum_x, sum_plus;

	/**
	 * sum_x: to calculate the number of multiplications made with this factor.
	 * sum_plus: to calculate the number of adds made with this factor.
	 */
	public Factor() {
		factor_vars_name = new ArrayList<String>();
		factor = new HashMap<ArrayList<String>, Map<ArrayList<String>, Double>>();
		sum_x = 0;
		sum_plus = 0;
	}

	/**
	 * The method converts CPT to factor according to the variable and the evidence.
	 * @param var
	 * @param evidance
	 */
	public void convertCPTToFactor(VariableNode var, Map<String, String> evidence) {
		factor_vars_name.addAll(var.getParents());
		factor_vars_name.add(var.getName());
		factor.put(factor_vars_name, var.getEventsWith(evidence));
	}
	
	/**
	 * The method joins between this factor and factor f
	 * @param f
	 * @return
	 */
	public Factor join(Factor f) {
		Factor new_factor = new Factor();
		ArrayList<String> vars_name = new ArrayList<String>(factor_vars_name);
		Map<Integer, Integer> same_vars_indexes = new HashMap<Integer, Integer>();
		for (String var_name : f.getVarsName()) {
			if (!vars_name.contains(var_name))
				//to put the the vars names in the new factor.
				vars_name.add(var_name);
			else
				same_vars_indexes.put(factor_vars_name.indexOf(var_name), f.getVarsName().indexOf(var_name));
		}
		Map<ArrayList<String>, Double> new_events = new HashMap<ArrayList<String>, Double>();
		ArrayList<String> new_event = new ArrayList<String>();
		ArrayList<String> event_factor_a, event_factor_b;
		Iterator<ArrayList<String>> factor_a_iter = this.eventsIter();
		Iterator<ArrayList<String>> factor_b_iter;
		while (factor_a_iter.hasNext()) {
			event_factor_a = factor_a_iter.next();
			factor_b_iter = f.eventsIter();
			while (factor_b_iter.hasNext()) {
				event_factor_b = factor_b_iter.next();
				boolean toAdd = true;
				for (int i : same_vars_indexes.keySet()) {
					if (!event_factor_a.get(i).equals(event_factor_b.get(same_vars_indexes.get(i)))) {
						toAdd = false;
						break;
					}
				}
				if (toAdd) {
					new_event = new ArrayList<String>(event_factor_a);
					for (int i = 0; i < event_factor_b.size(); i++) {
						if (!same_vars_indexes.containsValue(i))
							new_event.add(event_factor_b.get(i));
					}
					new_events.put(new_event, this.getProb(event_factor_a) * f.getProb(event_factor_b));
					sum_x++;

				}

			}
		}
		new_factor.put(vars_name, new_events);
		new_factor.setSumX(sum_x);
		new_factor.setSumPlus(sum_plus);
		new_factor.setSumX(f.sum_x);
		new_factor.setSumPlus(f.sum_plus);
		return new_factor;

	}
	
	/**
	 * The method does elimination on the factor according to the variable with hidden_name.
	 * @param hidden_name
	 */
	public void elimination(String hidden_name) {
		Map<ArrayList<String>, Map<ArrayList<String>, Double>> new_factor = new HashMap<ArrayList<String>, Map<ArrayList<String>, Double>>();
		Iterator<ArrayList<String>> events_iter_a = eventsIter();
		int index_hidden = getVarsName().indexOf(hidden_name);
		ArrayList<String> event_a;
		ArrayList<String> new_event;
		Map<ArrayList<String>,Double> elimination = new HashMap<ArrayList<String>, Double>();
		events_iter_a = eventsIter();
		while (events_iter_a.hasNext()) {
			event_a = events_iter_a.next();
			new_event = new ArrayList<String> (event_a);
			new_event.remove(index_hidden);
			if(elimination.containsKey(new_event)) {
				elimination.replace(new_event, elimination.get(new_event), elimination.get(new_event) + getProb(event_a));
				sum_plus++;
			}
			else 
				elimination.put(new_event, getProb(event_a));
		}
		factor_vars_name.remove(index_hidden);
		new_factor.put(factor_vars_name, elimination);
		factor = new_factor;
	}
	
	/**
	 * The method adds a factor. 
	 * @param factor_vars_name
	 * @param factor
	 */
	public void put(ArrayList<String> factor_vars_name, Map<ArrayList<String>, Double> factor) {
		this.factor_vars_name = factor_vars_name;
		this.factor.put(factor_vars_name, factor);
	}
	
	
	public Iterator<ArrayList<String>> eventsIter() {
		return getEvents().keySet().iterator();
	}
	
/////////////////////////////////////////////////////////////////////////////////
/////////////////////////GETTERS, SETTERS AND TOSTRING///////////////////////////
/////////////////////////////////////////////////////////////////////////////////  
	
	public Map<ArrayList<String>, Map<ArrayList<String>, Double>> getFactor() {
		return factor;
	}

	
	public ArrayList<String> getVarsName() {
		return factor_vars_name;
	}

	
	public double getProb(ArrayList<String> event) {
		return getEvents().get(event);
	}

	
	public Map<ArrayList<String>, Double> getEvents() {
		return factor.get(factor_vars_name);
	}

	public int getSumX() {
		return sum_x;	
	}
	
	public int getSumPlus() {
		return sum_plus;	
	}

	public void setSumX(int sum_x) {
		this.sum_x += sum_x;
		
	}
	
	public void setSumPlus(int sum_plus) {
		this.sum_plus += sum_plus;
		
	}
	
	public String toString() {
		return "\n\n" + factor_vars_name + "\n" + getEvents();
	}

}

