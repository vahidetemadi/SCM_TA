package SCM_TA_V1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Scanner;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.moeaframework.Analyzer.AnalyzerResults;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;


public class Test2 {
	public static HashMap<Integer,Developer> developers=new HashMap<Integer,Developer>();
	static HashMap<Integer,Bug> bugs=new HashMap<Integer,Bug>();
	static Queue<Bug> orderdBugs; 
	//static Solution solution=null;
	static HashMap<Integer , Zone> columns=new HashMap<Integer, Zone>();
	static Project project=new Project();
	static int roundnum=0;
	//DevMetrics devMetric=new DevMetrics();
	

	public static void run( String datasetName, int fileNumber, int portion) throws NoSuchElementException, IOException, URISyntaxException{	
		
		/*int numOfFiles=0;
			if(dataset_name=="Platform")
				numOfFiles=10;
			else 
				numOfFiles=9;*/
			
			runExperiment(fileNumber, datasetName, portion);
		
	}
	
	public static void runExperiment(int fileNumber,String datasetName, int portion) throws NoSuchElementException, IOException, URISyntaxException{
		GA_Problem_Parameter.createPriorityTable();
		for(int runNum=1;runNum<=1;runNum++){
			//developers.clear();
			bugs.clear();
			starting(fileNumber, runNum, datasetName, portion);
			System.gc();
		}
		
	}
	
	public static void starting(int fileNumber, int runNum, String datasetName, int portion) throws IOException{
		bugInitialization(fileNumber, datasetName, portion);
		//Assigning(actionSet.get(0),runNum,fileNumber, datasetName);
		//solution=results[1].get(results[1].size()/2);
		//writeResult(runNum,i,results);
		//System.out.println("finished writing");
		//afterRoundUpdating(solution);
		//removeDevelopers();
	}
		
	public static void changeRepresentation(String datasetName) throws FileNotFoundException{
		
		changeRepresentation cr=new changeRepresentation(datasetName);
		cr.txtToCSV();
	}
	
	// initialize the developer objects  
	public static void devInitialization(String datasetName, int portion) throws IOException,NoSuchElementException, URISyntaxException{
		//initialize developers
				System.out.println("enter the developrs file");
				Developer developer = null;
				 System.out.println(System.getProperty("user.dir"));
				Scanner sc=new Scanner(System.in);
				sc=new Scanner(new File(System.getProperty("user.dir")+"//src//SCM_TA_V1//bug-data//bug-data//"+datasetName+"Developer.txt"));
				System.out.println("enter the devlopers wage file");
				Scanner scan=new Scanner(System.in);
				scan=new Scanner(new File(System.getProperty("user.dir")+"//src//SCM_TA_V1//bug-data//bug-data//"+datasetName+"DeveloperWithWage.txt"));
				int i=0;
				int j=0;
				while(sc.hasNextLine() && scan.hasNextLine()){
					if(i==0){
						String[] items=sc.nextLine().split("\t",-1);
						scan.nextLine();
							for(int k=0;k<items.length;k++){
								if(j!=0){
								Zone zone=new Zone(j, items[k]);
								project.zones.put(j, zone);
								columns.put(j,zone);
								}
								j++;
							}
					}
					else{
						String[] items=sc.nextLine().split("\t|\\ ",-1);
						String[] wage_items=scan.nextLine().split("\t|\\ ",-1);
						double sumOfPro=0.0;
						for(int k=0;k<items.length;k++){
							sumOfPro+=Double.parseDouble(items[k]);
						}
						for(int k=0;k<items.length;k++){
							if(j!=0){
								//developer.DZone_Coefficient.put(columns.get(j), Double.parseDouble(items[k]));
								//System.out.println(columns.get(j));
								developer.DZone_Coefficient.put(project.zones.get(j), (Double.parseDouble(items[k])/sumOfPro));
								developer.DZone_Wage.put(project.zones.get(j), Double.parseDouble(wage_items[k])*Double.parseDouble(wage_items[wage_items.length-1]));
								developer.hourlyWage=Double.parseDouble(wage_items[wage_items.length-1]);
								//System.out.println(Double.parseDouble(wage_items[k]));
							}
							else{
								developer=new Developer(0);
								developer.setID(i);
							}
							j++;
						}
					developers.put(developer.getID(), developer);
					}
					i++;
					j=0;
				}
				/*assign GA_Problem_Parameter DevList*/
				for(Map.Entry<Integer, Developer> dev:developers.entrySet()){
					GA_Problem_Parameter.DevList.add(dev.getKey());
				}
				GA_Problem_Parameter.developers_all=(HashMap<Integer, Developer>) developers.clone();
				GA_Problem_Parameter.developers=developers;
				
				for(Developer d:GA_Problem_Parameter.developers.values()){
					for(Map.Entry<Zone, Double> entry:d.DZone_Coefficient.entrySet()){
						if(entry.getValue()==0)
							d.DZone_Coefficient.put(entry.getKey(),getNonZeroMin(d.DZone_Coefficient));
					}
				}
				//cut randomly portion of developers
				GA_Problem_Parameter.cutDevs(portion);
		
	}
	
	// initialize the bugs objects for task assignment
	public static void bugInitialization(int fileNumber, String datasetName, int portion) throws IOException,NoSuchElementException{	
		bugs.clear();
		Scanner sc;//=new Scanner(System.in);
		int i=0;
		int j=0;
		/*generate bug objects*/
		System.out.println("enter the bugs files");
		Bug bug=null;
		//sc=new Scanner(System.in);
		//sc=new Scanner("/bug-data/JDT/efforts");
		//System.out.println(sc.nextLine());
		Scanner sc1=null;
		int n=1;
		for(File fileEntry:new File(System.getProperty("user.dir")+"//src//SCM_TA_V1//bug-data//"+datasetName+"//efforts").listFiles()){
			sc1=new Scanner(new File(fileEntry.toURI()));
			i=0;
			j=0;
			if(fileNumber==n)
				System.out.println(fileEntry.getPath());
			while(sc1.hasNextLine() && fileNumber==n){
				//counter "i" has set to record the name of each zone (the header of each file)
				if(i==0){
						String[] items=sc1.nextLine().split("\t|\\ ",-1);
							for(int k=0;k<items.length;k++){
								if(j>2){
									Zone zone=new Zone(j, items[k]);
									columns.put(j,zone);
								}
								j++;
							}	
					}
					else{
						String[] items=sc1.nextLine().split("\t",-1);
						for(int k=0;k<items.length;k++){
							if(j>2 && Double.parseDouble(items[k])!=0){
								bug.BZone_Coefficient.put(project.zones.get(j-2),Double.parseDouble(items[k]));
							}
							else if(j==0){
								bug=new Bug();
								bug.setID(Integer.parseInt(items[k]));
							}
							else if(j==2){
								bug.setTotalEstimatedEffort(Double.parseDouble(items[k]));
							}
							j++;
						}
					//create DAG for zoneItems 	
					bug.setZoneDEP();	
					//add bug to bugset
					bugs.put(bug.getID(), bug);
					}
					i++;
					j=0;
			}
			n++;
		}
				

		
		//cut portion of tasks randomly
		GA_Problem_Parameter.cutTasks(portion, bugs);
		
		//prune bug list
		GA_Problem_Parameter.splitBugList(bugs);
		
	}
	
	public static void initializeGAParameter(HashMap<Integer,Bug> bugList){
		//initialize GA parameters
		GA_Problem_Parameter.Num_of_variables=bugList.size();
		
		int b_index=0;
		GA_Problem_Parameter.bugs=new Bug[bugList.size()];
		for(Entry<Integer, Bug> b2:bugList.entrySet()){
			GA_Problem_Parameter.bugs[b_index]=b2.getValue();
			b_index++;
		}
		
		//GA_Problem_Parameter
		GA_Problem_Parameter.Num_of_Bugs=bugs.size();
		GA_Problem_Parameter.Num_of_Active_Developers=developers.size();
		GA_Problem_Parameter.upperDevId=developers.size()+1;
		GA_Problem_Parameter.Num_of_functions_Multi=2;
		GA_Problem_Parameter.Num_of_variables=0;
		for(Entry<Integer, Bug>  b:bugs.entrySet()){
			for(Map.Entry<Zone, Double>  zone:b.getValue().BZone_Coefficient.entrySet()){
				GA_Problem_Parameter.Num_of_variables++;
			}
		}
		
		System.out.println("size of bug list: "+ GA_Problem_Parameter.bugs.length);
		GA_Problem_Parameter.population=500;
	}
	
	public static void setBugDependencies(String datasetName, HashMap<Integer,Bug> bugList) throws FileNotFoundException{
		/*set bug dependencies*/
		int f = 0,i=0;
		Scanner sc,sc1=null;
		System.out.println("enter the bug dependency files");
		sc=new Scanner(System.getProperty("user.dir")+"//src//SCM_TA_V1//bug-data//"+datasetName+"//dependencies");
		String[] columns_bug=null;
		for(Bug b:bugList.values()){
			b.DB.clear();
		}
		for(File fileEntry:new File(System.getProperty("user.dir")+"//src//SCM_TA_V1//bug-data//"+datasetName+"//dependencies").listFiles()){
			sc1=new Scanner(new File(fileEntry.toURI()));
			i=0;
			while(sc1.hasNextLine()){
				if(i>0){
					int l=6;
					int m=5;

					String s=sc1.nextLine();
					columns_bug=s.split(",");
					//if(columns_bug[l]=="P3")
						//System.out.println(columns_bug[l]);
					int k=1;
					try{
						for(String st:columns_bug){
							if(st.contains("P3")){
								bugList.get(Integer.parseInt(columns_bug[k-1])).priority="P3";
							}
							if(st.contains("P2")){
								bugList.get(Integer.parseInt(columns_bug[k-1])).priority="P2";
							}
							if(st.contains("P1")){
								bugList.get(Integer.parseInt(columns_bug[k-1])).priority="P1";
							}
						}
						
						if(columns_bug[k].trim().length() > 0){
							try{
								Integer ID_1=Integer.parseInt(columns_bug[k-1]);
								System.out.println(bugList.get(ID_1).ID);
								if(bugList.get(Integer.parseInt(columns_bug[k-1]))!=null && bugList.get(Integer.parseInt(columns_bug[k]))!=null){
									bugList.get(Integer.parseInt(columns_bug[k-1])).DB.add(bugList.get(Integer.parseInt(columns_bug[k])));
									f++;
								}
								
								//System.out.println(bugs.get(Integer.parseInt(columns_bug[k])));
							}
							catch(Exception e){
								
							}
						}	
					}
					catch(Exception e){
						
					}
					
				}
				else{
					sc1.nextLine();
				}
				i++;
			}
		}
		System.out.println("real F:"+f);
		/* end setting bug dependencies*/
		
		/* set zone dependencies */
		
		/* end setting zone dependencies */
		System.out.println("end of setting dependencies");
	}
	
	//find solution to assign tasks to the developers
	public static void Assigning(String action, int runNum, int fileNum, String datasetName, Double TCT, Double TID) throws IOException{
		GA_Problem_Parameter.setArrivalTasks();
		GA_Problem_Parameter.setDevelopersIDForRandom();
		
		/*NondominatedPopulation result_Karim=new Executor().withProblemClass(CompetenceMulti2_problem.class).withAlgorithm("NSGAII")
				.withMaxEvaluations(30000).withProperty("populationSize",GA_Problem_Parameter.population).withProperty("operator", "UX")
				.withProperty("UX.rate", 0.6).withProperty("pm.rate", 0.1).run();
		results[0]=result_Karim;
		
		System.out.println("finished first one");
		
		NondominatedPopulation result_me=new Executor().withProblemClass(InformationDifussion.class).withAlgorithm("NSGAII")
				.withMaxEvaluations(30000).withProperty("populationSize",GA_Problem_Parameter.population).withProperty("operator", "UX")
				.withProperty("UX.rate", 0.6).withProperty("pm.rate", 0.1).run();
	    results[1]=result_me;*/
		
		//try{
		try(PrintWriter pw=new PrintWriter(new FileOutputStream(new File(System.getProperty("user.dir")+"//results//"+datasetName+"_result_adaptive.yml"),true)))
		{
			switch(action){
			case "cost":
				
				
				Population result_normal=new Executor().withProblemClass(normal_assignment.class).withAlgorithm("NSGAII")
				.withMaxEvaluations(30000).withProperty("populationSize",GA_Problem_Parameter.population).withProperty("operator", "UX")
				.withProperty("UX.rate", 0.9).withProperty("operator", "UM").withProperty("pm.rate", 0.05).run();
		
				System.out.println("finished cost-based assignment");
				
				//cost based///
				////
				Solution NormalSolution=null;
				for(Solution s:result_normal)
					NormalSolution=s;
				int c=0;
				
				
				
				while(GA_Problem_Parameter.tso.hasNext()){
					Bug b=GA_Problem_Parameter.tso.next();
					TopologicalOrderIterator<Zone, DefaultEdge> tso_Zone=new TopologicalOrderIterator<Zone, DefaultEdge>(b.Zone_DEP);
					while(tso_Zone.hasNext()){
						Developer d=developers.get(NormalSolution.getVariable(c));
						updateDevProfile(b, tso_Zone.next(), d);
						c++;
					}
				}
				
				//report the cost
				/*System.out.println("knowlwdge and cost of cost-based approach (state Dynamic)"+
						"\n\n	amount of diffused knowledge:"+ NormalSolution.getAttribute("diffusedKnowledge")
						+"\n	the total cost:" + NormalSolution.getObjective(0));*/
				
				//write down the results in yaml format
				
				YamlMapping yaml_Dynamic=Yaml.createYamlMappingBuilder()
						.add("state name", "Dynamic")
						.add("ID", NormalSolution.getAttribute("diffusedKnowledge").toString())
						.add("Cost",Double.toString(NormalSolution.getObjective(0)))
						.build();
				
				//add to total cost ove time and total information diffusion
				TCT+=NormalSolution.getObjective(0);
				TID+=(Double)NormalSolution.getAttribute("diffusedKnowledge");
				
				System.out.println(yaml_Dynamic.toString());
				
				//log in a file
				pw.write(yaml_Dynamic.toString());
				pw.append("\n");
				pw.append("\n");
				pw.append("\n");
				pw.close();
				
				PrintWriter pw2=new PrintWriter(new FileOutputStream(new File(System.getProperty("user.dir")+"//results//"+datasetName+"_result_adaptive.csv"),true));
				StringBuilder sb=new StringBuilder();
				sb.append(fileNum +", Dynamic, Cost, "+ Double.toString(NormalSolution.getObjective(0))+","+NormalSolution.getAttribute("diffusedKnowledge").toString());
				sb.append('\n');
				pw2.write(sb.toString());
				pw2.close();
				
				
				
				break;
			
			case "diffusion":
				Population result_ID=new Executor().withProblemClass(InformationDifussion_adaptive.class).withAlgorithm("NSGAII")
				.withMaxEvaluations(30000).withProperty("populationSize",GA_Problem_Parameter.population).withProperty("operator", "UX")
				.withProperty("UX.rate", 0.9).withProperty("operator", "UM").withProperty("pm.rate", 0.05).run();
		
				System.out.println("finished diffusion-based assignment");
				//Performing the update
				Solution IDSolution=null;
				for(Solution s:result_ID)
					IDSolution=s;
				int c2=0;
				while(GA_Problem_Parameter.tso.hasNext()){
					Bug b=GA_Problem_Parameter.tso.next();
					TopologicalOrderIterator<Zone, DefaultEdge> tso_Zone=new TopologicalOrderIterator<Zone, DefaultEdge>(b.Zone_DEP);
					while(tso_Zone.hasNext()){
						Developer d=developers.get(IDSolution.getVariable(0));
						updateDevProfile(b, tso_Zone.next(), d);
						c2++;
					}
				}
				//report the cost---logging the cost
				/*System.out.println("knowlwdge and cost of diffusion-based approach (state Steady)"+
						"\n\n	amount of diffused knowledge:" + (-1*IDSolution.getObjective(0))
						+"\n"+
						"	the total cost:"+ IDSolution.getAttribute("cost"));*/
				
				YamlMapping yaml_Steady=Yaml.createYamlMappingBuilder()
						.add("state name", "Steady")
						.add("ID", Double.toString(-1*IDSolution.getObjective(0)))
						.add("Cost", IDSolution.getAttribute("cost").toString())
						.build();
				
				//add to total cost over time and total information diffusion
				TID+=(-1)*IDSolution.getObjective(0);
				TCT+=(Double) IDSolution.getAttribute("cost");
				System.out.println(yaml_Steady.toString());
				
				//log in YAML format
				pw.write(yaml_Steady.toString());
				pw.append("\n");
				pw.append("\n");
				pw.append("\n");
				pw.close();
				
				//write down to CSV file
				PrintWriter pw3=new PrintWriter(new FileOutputStream(new File(System.getProperty("user.dir")+"//results//"+datasetName+"_result_adaptive.csv"),true));
				StringBuilder sb2=new StringBuilder();
				sb2.append(fileNum +", Steady, Diffusion, "+IDSolution.getAttribute("cost").toString()+","+Double.toString(-1*IDSolution.getObjective(0)));
				sb2.append('\n');
				pw3.write(sb2.toString());
				pw3.close();
				pw3.close();
				break;	
			}
		}
		
		
		
			
		/*}
		catch(Exception e){
			starting(fileNum, runNum);
		}*/
		//return results;
	    
	}
	
	//write the results for testing
	public static void writeResult(int runNum,int roundNum, NondominatedPopulation[] result) throws FileNotFoundException{
		//write results to CSV for each round
		System.out.println("result of the expriment for Karim approach");
		PrintWriter pw=new PrintWriter(new File(System.getProperty("user.dir")+"//results//solutions_Karim_round "+roundnum+"_"+roundNum+".csv"));
		StringBuilder sb=new StringBuilder();
		for(Solution solution:result[0]){
			for(int i=0; i<solution.getNumberOfVariables();i++){
				System.out.print(EncodingUtils.getInt(solution.getVariable(i))+",");
			}
			System.out.println();
			sb.append(solution.getObjective(0)+","+solution.getObjective(1));
			sb.setLength(sb.length()-1);
			sb.append("\n");
		}
		pw.write(sb.toString());
		pw.close();
		System.out.println("result of the expriment for proposed approach");
		pw=new PrintWriter(new File(System.getProperty("user.dir")+"//results//solutions_Me_round "+runNum+"_"+roundNum+".csv"));
		sb.setLength(0);
		for(Solution solution:result[1]){
			for(int i=0; i<solution.getNumberOfVariables();i++){
				System.out.print(EncodingUtils.getInt(solution.getVariable(i))+",");
			}
			System.out.println();
			sb.append(solution.getObjective(0)+","+solution.getObjective(1));
			sb.setLength(sb.length()-1);
			sb.append("\n");
		}
		pw.write(sb.toString());
		pw.close();
		
	}
	
	public static void writeAnalyzingResults(AnalyzerResults ar, int runNum, int roundNum) throws FileNotFoundException{
		//PrintWriter pw=new PrintWriter(new File(System.getProperty("user.dir")+"//results//AnalyzerResults"+runNum+"_"+roundNum+".csv"));
		StringBuilder sb=new StringBuilder();
		for(String AN:ar.getAlgorithms()){
			System.out.println(ar.get(AN));
		}
	}
	
	public static void afterRoundUpdating(Solution solution){
		//update developers' zone
		int variableNum=0;
		for(Map.Entry<Integer, Bug> bug:bugs.entrySet()){
			for(Map.Entry<Zone, Double> zone:bug.getValue().BZone_Coefficient.entrySet()){
				updateDeveloperSkills(EncodingUtils.getInt(solution.getVariable(variableNum)),zone);
				variableNum++;
			}
		}
		//remove 2 top developers
		
	}
	
	public static void removeDevelopers(){
		int devId=-1;
		double devScore=-1.0;
		//select dev with the max of sum of competencies 
		for(Map.Entry<Integer, Developer> dev:developers.entrySet()){
			if(devCompetenciesMeasurement(dev.getValue())>devScore){
				devId=dev.getKey();
				devScore=devCompetenciesMeasurement(dev.getValue());
			}
		}
		try{
			
		//GA_Problem_Parameter.DevList.remove(devId);
		developers.remove(devId);
		System.out.println(devId);
		}
		catch(Exception e){
		}
	
	}
	
	public static double devCompetenciesMeasurement(Developer dev){
		double CumulativeSkillLevel=0.0;
		for(Map.Entry<Zone,Double> zone:dev.getDZone_Coefficient().entrySet())
			CumulativeSkillLevel+=zone.getValue();
		return CumulativeSkillLevel;
	}
	
	public static void updateDeveloperSkills(int dev, Map.Entry<Zone, Double> zone){
		for(Map.Entry<Zone, Double> devZone:developers.get(dev).DZone_Coefficient.entrySet()){
			if(devZone.getKey().zId==zone.getKey().zId)
				developers.get(dev).DZone_Coefficient.put(devZone.getKey(),devZone.getValue()+zone.getValue());
		}
	}
	
	public static void writeResultsforRuns(){
		
	}
	
	private static double getNonZeroMin(HashMap<Zone, Double> entrySet){
		double min=300;
		for(Double d:entrySet.values()){
			if(min>d && d>0){
				min=d;
			}
		}
		return min;
	}
	
	//update the profile of developers
	public static void updateDevProfile(Bug b,Zone z, Developer d){
		d.getDZone_Coefficient().put(z, Math.max(d.getDZone_Coefficient().get(z), b.BZone_Coefficient.get(z)));
	}

}
