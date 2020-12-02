import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * This class reci 
 * @author Itamar Ziv-On
 *
 */
public class LoadBN {
	BN bN;
	
	/**
	 * 
	 * @param file_name
	 */
	public LoadBN(String file_name) {
		bN= new BN();
		BufferedReader br;
		String line, variable_line;
		try {
			br = new BufferedReader(new FileReader(file_name));
			line = br.readLine();
//			System.out.println(line);
			variable_line = br.readLine();
//			System.out.println(variable_line);
			buildBN(variable_line);
			while(!(line=br.readLine()).equals("Queries")){
				if(line.length()>1) {
					String[] line2= line.split(" ");
//					System.out.println("line2 "+ line2[0]);
					if(line2[0].equals("Var"))
						loadVar(br, line2[1]);
				}
			}
			loadQueries(br);
			br.close();
		}
		catch(Exception e) {
			throw new RuntimeException(e);	
		}
	}
	
	/**
	 * 
	 * @param br
	 * @param var_name
	 */
	private void loadVar(BufferedReader br, String var_name) {
		
		String line;
		String[] line2;
		try {
			while(!(line=br.readLine()).equals("CPT:")){
				line = line.replace(" ", "");
				line2 = line.split(":");
				if(line2[0].equals("Values"))
					loadValues(br, var_name, line2[1]);
				else if(line2[0].equals("Parents"))
					loadParents(br, var_name, line2[1]);
			}
			loadCPT(br, var_name);
		}
		catch(Exception e) {
			throw new RuntimeException(e);	
		}
			
	}
	
	/**
	 * 
	 * @param br
	 * @param var_name
	 */
	private void loadCPT(BufferedReader br, String var_name) {
		String line;
		try {
			while((line=br.readLine()).length()>1) {
				bN.getVar(var_name).getCPT().add(line);	
			}
			bN.getVar(var_name).fillEvents();
		}
		catch(Exception e) {
			throw new RuntimeException(e);		
			}
		
	}
	/**
	 * 
	 * @param br
	 * @param var_name
	 * @param parents_name
	 */
	private void loadParents(BufferedReader br, String var_name, String parents_name) {
		ArrayList<String> parents = bN.getVar(var_name).getParents();
		if(!(parents_name.equals("none"))){
			for(String parent: parents_name.split(",")) {
				parents.add(parent);
//				System.out.println("parent: " + parent);
			}
		}
		
	}

	/**
	 * 
	 * @param br
	 * @param var_name
	 * @param values
	 */
	private void loadValues(BufferedReader br, String var_name, String values) {
		ArrayList<String> domain = bN.getVar(var_name).getVarDomain();
		for(String val: values.split(",")) {
			domain.add(val);
//			System.out.println("value: "+ val);
		}
		
	}
	
	/**
	 * 
	 * @param br
	 */
	private void loadQueries(BufferedReader br) {
		String line;
		try {
			while((line=br.readLine())!=null) {
				bN.addQuery(line);
//				System.out.println("query: " + line);
			}
		}
		catch(Exception e) {
			throw new RuntimeException(e);	
		}
	}
	public BN getBN() {
		return bN;
	}
	
	/**
	 * 
	 * @param line
	 */
	private void buildBN(String line) {
		line = line.replace(" ", "");
		String[] variables = line.split(":")[1].split(",");
		for (String var : variables) {
			bN.addVariable(var, new VariableNode(var));
			
		}
		
	}
}
