import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class represent a factor and including the operations join between to factors and elimination.
 * @author Itamar Ziv-On
 *
 */
public class Factor {

	private ArrayList<String> factor_vars_name;
	private Map<ArrayList<String>, Map<ArrayList<String>, Double>> factor;
	int sum_x, sum_plus;

	/**
	 * sum_x: compute the sum of multiplications that happened while doing join between this factor to another factor. 
	 * sum_plus: compute the sum
	 */
	public Factor() {
		factor_vars_name = new ArrayList<String>();
		factor = new HashMap<ArrayList<String>, Map<ArrayList<String>, Double>>();
		sum_x = 0;
		sum_plus = 0;
	}

	/**
	 * 
	 * @return
	 */
	public Map<ArrayList<String>, Map<ArrayList<String>, Double>> getFactor() {
		return factor;
	}

	/**
	 * 
	 * @return
	 */
	public ArrayList<String> getVarsName() {
		return factor_vars_name;
	}

	/**
	 * 
	 * @param event
	 * @return
	 */
	public double getProb(ArrayList<String> event) {
		return getEvents().get(event);
	}

	/**
	 * 
	 * @return
	 */
	public Map<ArrayList<String>, Double> getEvents() {
		return factor.get(factor_vars_name);
	}

	/**
	 * 
	 * @param factor_vars_name
	 * @param factor
	 */
	public void put(ArrayList<String> factor_vars_name, Map<ArrayList<String>, Double> factor) {
		this.factor_vars_name = factor_vars_name;
		this.factor.put(factor_vars_name, factor);
	}

	/**
	 * 
	 * @param var
	 * @param evidance
	 */
	public void convertCPTToFactor(VariableNode var, Map<String, String> evidence) {
//		Map<String, String> common_vars = new HashMap<String, String>();
//		Map<String, String> evidance = new HashMap<String, String>();
//		ArrayList<String> common_vars_name= new ArrayList<String>();
//		ArrayList<String> factor_vars_name = new ArrayList<String>();
//		for (String variable : evidance_vars) {
//			evidance.put(variable, evidance_vals.get(evidance_vars.indexOf(variable)));
//			if((var.getParents().contains(variable)) || var.getName().equals(variable)) {
//				common_vars.put(variable, evidance_vals.get(evidance_vars.indexOf(variable)));
//				common_vars_name.add(variable);
//			}
//		}
		factor_vars_name.addAll(var.getParents());
		factor_vars_name.add(var.getName());
		factor.put(factor_vars_name, var.getEventsWith(evidence));
//		factor.put(new ArrayList<String>(var.getParents()), var.getEventsWith(common_vars));
//		System.out.println("var: " + var.getName() + ", factor: " + factor);

	}

	/**
	 * 
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
//		System.out.println("same_vars_indexes: " + same_vars_indexes);
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
//						System.out.println("In false join:  " + factor_vars_name + ", event_factor_a: " + event_factor_a +", event_factor_b: " + event_factor_b);
						toAdd = false;
						break;
					}
				}
				if (toAdd) {
					// new_event.addAll(event_factor_a);
//					System.out.println("In true join:  " + factor_vars_name + ", event_factor_a: " + event_factor_a +", event_factor_b: " + event_factor_b);
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
//		System.out.println("New factor: " + new_factor);
		return new_factor;

	}

	private void setSumX(int sum_x) {
		this.sum_x += sum_x;
		
	}
	
	private void setSumPlus(int sum_plus) {
		this.sum_plus += sum_plus;
		
	}

	/**
	 * 
	 * @param hidden_name
	 */
	public void elimination(String hidden_name) {
		Map<ArrayList<String>, Map<ArrayList<String>, Double>> new_factor = new HashMap<ArrayList<String>, Map<ArrayList<String>, Double>>();
		Iterator<ArrayList<String>> events_iter_a = eventsIter();
//		Iterator<ArrayList<String>> events_iter_b;
		int index_hidden = getVarsName().indexOf(hidden_name);
		ArrayList<String> event_a;
		ArrayList<String> new_event;
		Map<ArrayList<String>,Double> elimination = new HashMap<ArrayList<String>, Double>();
//		System.out.println("Factor to eliminate: "+ this);
		//pass on the events and delete the hidden event
//		while (events_iter_a.hasNext()) {
//			event_a = events_iter_a.next();
//			event_a.remove(index_hidden);
//		}
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
		
		//System.out.println("Factor to eliminate: "+ this);
//		while (events_iter_a.hasNext()) {
//			event_a = events_iter_a.next();
//			if(event_a.size() < factor_vars_name.size())
//				continue;
//			events_iter_b = eventsIter();
//			events_to_delete.clear();
//			while (events_iter_b.hasNext()) {
////			for(int i = 0; i < getEvents().size(); i++ ) {
//				event_b = events_iter_b.next();
//				if (event_a.equals(event_b) || event_b.size() < factor_vars_name.size())
//					continue;
//				boolean toAdd = true;
//				for (int i = 0; i < event_b.size(); i++) {
//					if (i == index_hidden)
//						continue;
//					System.out.println("Before false: event_a: " + event_a +", event_b: " + event_b);
//					if (!event_a.get(i).equals(event_b.get(i))) {
////						System.out.println("In false: hidden: " + hidden_name + ", factor name: " + factor_vars_name + ", event_a: " + event_a +", event_b: " + event_b);
//						toAdd = false;
//						break;
//					}
//				}
//				if (toAdd) {
//					System.out.println("event_a: " + event_a +", event_b: " + event_b);
//					getEvents().replace( event_a, getProb(event_a) + getProb(event_b)) ;
//					System.out.println("getEvents: "+ getEvents()+ ", factor: "+ factor);
//					sum_plus++;
//					event_b.remove(index_hidden);
//					events_to_delete.add(event_b);
//				}
//			}
////			events_iter_b = eventsIter();
////			while (events_iter_b.hasNext()) {
////				if((event_b = events_iter_b.next()).size() < factor_vars_name.size())
////			for(ArrayList<String> event : events_to_delete) {
////				System.out.println("Event to delete: " + event + "\nbefore remove: " + factor );
////				factor.get(factor_vars_name).remove(event);
////				System.out.println("After remove: " + factor);
////			}
//			//event_a.remove(index_hidden);
//			
//		}
//		for(ArrayList<String> event : events_to_delete) {
//			System.out.println("Event to delete: " + event + "\nbefore remove: " + factor );
//			factor.get(factor_vars_name).remove(event);
//			System.out.println("After remove: " + factor);
//		}
		//factor_vars_name.remove(index_hidden);
//		System.out.println("Factor after elimination: " + this);
	}

	/**
	 * 
	 * @return
	 */
	public Iterator<ArrayList<String>> eventsIter() {
//		System.out.println("In iterator: " + factor + getEvents() );
//		if (getEvents()==null)
//			return null;
		return getEvents().keySet().iterator();
	}

	/**
	 * 
	 */
	public String toString() {
		return "\n\n" + factor_vars_name + "\n" + getEvents();
	}

}
