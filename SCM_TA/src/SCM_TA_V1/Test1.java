package SCM_TA_V1;

import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Queue;

import org.apache.commons.math3.filter.KalmanFilter;
import org.jgrapht.graph.DefaultEdge;
import org.moeaframework.Analyzer;
import org.moeaframework.Analyzer.AnalyzerResults;
import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.algorithm.PeriodicAction.FrequencyType;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.core.Indicator;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.apache.commons.io.FilenameUtils;


public class Test1 {
	public static HashMap<Integer,Developer> developers=new HashMap<Integer,Developer>();
	static HashMap<Integer,Bug> bugs=new HashMap<Integer,Bug>();
	static Queue<Bug> orderdBugs; 
	//static Solution solution=null;
	static HashMap<Integer , Zone> zoneList=new HashMap<Integer, Zone>();
	static Project project=new Project();
	static int roundnum=0;
	static String fileName;
	//DevMetrics devMetric=new DevMetrics();
	

	public static void main(String[] args) throws NoSuchElementException, IOException, URISyntaxException{
		Scanner sc=new Scanner(System.in);
		System.out.println("please insert the number of desired schedules:");
		GA_Problem_Parameter.numOfEvaluationLocalSearch=sc.nextInt();
		String mode="running";
		if(mode=="running"){
			runExperiment();
		}
		else if (mode=="representatoin"){
			changeRepresentation();
		}
		
	}
	
	public static void runExperiment() throws NoSuchElementException, IOException, URISyntaxException{
		GA_Problem_Parameter.createPriorityTable();
		for(int runNum=21;runNum<=30;runNum++){
			double[] costs=new double[2];
			developers.clear();
			bugs.clear();
			devInitialization();
			int roundNum=10;
			for(int i=1;i<=roundNum;i++){
				starting(i, runNum);
			}
			System.gc();
		}
		
	}

	public static void starting(int roundNum, int runNum) throws IOException{
		//set the threshold to initialize the population 
		GA_Problem_Parameter.thresoldForPopulationGeneration=0;
		bugInitialization(roundNum);
		GA_Problem_Parameter.generateModelofBugs();
		GA_Problem_Parameter.candidateSolutonGeneration();
		NondominatedPopulation[] results = new NondominatedPopulation[2]; 
		Assigning(results,runNum,roundNum);
		//solution=results[1].get(results[1].size()/2);
		//writeResult(runNum,i,results);
		//System.out.println("finished writing");
		//afterRoundUpdating(solution);
		//removeDevelopers();
	}
		
	public static void changeRepresentation() throws FileNotFoundException{
		
		changeRepresentation cr=new changeRepresentation();
		cr.txtToCSV();
	}
	
	// initialize the developer objects  
	public static void devInitialization() throws IOException,NoSuchElementException, URISyntaxException{
		//initialize developers
				System.out.println("enter the developrs file");
				Developer developer = null;
				 System.out.println(System.getProperty("user.dir"));
				Scanner sc=new Scanner(System.in);
				sc=new Scanner(new File(System.getProperty("user.dir")+"//src//SCM_TA_V1//bug-data//bug-data//JDTDeveloper.txt"));
				System.out.println("enter the devlopers wage file");
				Scanner scan=new Scanner(System.in);
				scan=new Scanner(new File(System.getProperty("user.dir")+"//src//SCM_TA_V1//bug-data//bug-data//JDTDeveloperWithWage.txt"));
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
								zoneList.put(j,zone);
								}
								j++;
							}
					}
					else{
						String[] items=sc.nextLine().split("\t|\\ ",-1);
						String[] wage_items=scan.nextLine().split("\t|\\ ",-1);
						double sumOfPro=0.0;
						for(int k=1;k<items.length;k++){
							sumOfPro+=Double.parseDouble(items[k]);
						}
						double f=sumOfPro;
						for(int k=0;k<items.length;k++){
							if(j!=0){
								//developer.DZone_Coefficient.put(columns.get(j), Double.parseDouble(items[k]));
								//System.out.println(columns.get(j));
								developer.DZone_Coefficient.put(project.zones.get(j), (Double.parseDouble(items[k])));
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
				//prune devs
				/*assign GA_Problem_Parameter DevList*/
				for(Map.Entry<Integer, Developer> dev:developers.entrySet()){
					GA_Problem_Parameter.DevList.add(dev.getKey());
				}
				GA_Problem_Parameter.developers=developers;
				
				for(Developer d:GA_Problem_Parameter.developers.values()){
					for(Map.Entry<Zone, Double> entry:d.DZone_Coefficient.entrySet()){
						if(entry.getValue()==0)
							d.DZone_Coefficient.put(entry.getKey(),getNonZeroMin(d.DZone_Coefficient));
					}
				}
				

				ArrayList<Ranking<Developer, Double>> Devs=new ArrayList<Ranking<Developer,Double>>();
				for(Developer d:developers.values()){
					Devs.add(DevMetrics.computeMetric(d));
				}
				DevMetrics.sortByMetric(Devs);
				for(Ranking<Developer, Double> r:Devs){
					System.out.println(r.getEntity()+"---"+r.getMetric());
				}

				//GA_Problem_Parameter.pruneDevList(developers);
				GA_Problem_Parameter.pruneDevList(developers,Devs,100);
	}
	
	// initialize the bugs objects for task assignment  
	public static void bugInitialization(int roundNum) throws IOException,NoSuchElementException{	
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
		for(File fileEntry:new File(System.getProperty("user.dir")+"//src//SCM_TA_V1//bug-data//JDT//efforts").listFiles()){
			sc1=new Scanner(new File(fileEntry.toURI()));
			i=0;
			j=0;
			if(roundNum==n){
				fileName=FilenameUtils.removeExtension(fileEntry.getName());
				System.out.println(fileEntry.getPath());
			}
			while(sc1.hasNextLine() && roundNum==n){
				//counter "i" has set to record the name of each zone (the header of each file)
				if(i==0){
						String[] items=sc1.nextLine().split("\t|\\ ",-1);
						for(int k=0;k<items.length;k++){
							if(j>2){
								/*Zone zone=new Zone(j-2, items[k]);
								zoneList.put(j-2,zone);*/
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
					//add bug to bugset
					bugs.put(bug.getID(), bug);
					}
					i++;
					j=0;
			}
			n++;
			
		}
		//assign zone dep	
		BufferedReader in=new BufferedReader(new FileReader(new File(System.getProperty("user.dir")+"//src//SCM_TA_V1//bug-data//bug-data//CompetencyGraph.txt")));
		String line;
		StringTokenizer tokens;
		String dependee, depender;	
		line=in.readLine();
		tokens=new StringTokenizer(line);
		dependee=tokens.nextToken();
		while(tokens.hasMoreTokens()){
			depender=tokens.nextToken();
			zoneList.get(Integer.parseInt(depender)).DZ.add(zoneList.get(Integer.parseInt(dependee)));
		}
		
		for(Map.Entry<Integer, Bug> bugdep:bugs.entrySet()){
			//create DAG for zoneItems 	
			bugdep.getValue().setZoneDEP();	
		}
		
		
		//prune bug list
		//GA_Problem_Parameter.pruneList(bugs);
		
		
		
		/*set bug dependencies*/
		int f=0;
		System.out.println("enter the bug dependency files");
		sc=new Scanner(System.getProperty("user.dir")+"//src//SCM_TA_V1//bug-data//JDT//dependencies");
		String[] columns_bug=null;
		for(Bug b:bugs.values()){
			b.DB.clear();
		}
		for(File fileEntry:new File(System.getProperty("user.dir")+"//src//SCM_TA_V1//bug-data//JDT//dependencies").listFiles()){
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
								bugs.get(Integer.parseInt(columns_bug[k-1])).priority="P3";
							}
							if(st.contains("P2")){
								bugs.get(Integer.parseInt(columns_bug[k-1])).priority="P2";
							}
							if(st.contains("P1")){
								bugs.get(Integer.parseInt(columns_bug[k-1])).priority="P1";
							}
						}
						
						if(columns_bug[k].trim().length() > 0){
							try{
								Integer ID_1=Integer.parseInt(columns_bug[k-1]);
								System.out.println(bugs.get(ID_1).ID);
								if(bugs.get(Integer.parseInt(columns_bug[k-1]))!=null && bugs.get(Integer.parseInt(columns_bug[k]))!=null){
									bugs.get(Integer.parseInt(columns_bug[k-1])).DB.add(bugs.get(Integer.parseInt(columns_bug[k])));
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
		
		//initialize GA parameters
		GA_Problem_Parameter.Num_of_variables=bugs.size();
		GA_Problem_Parameter.developers=developers;
		GA_Problem_Parameter.DevList=new ArrayList<Integer>();
		for(Entry<Integer, Developer> dev:developers.entrySet()){
			GA_Problem_Parameter.DevList.add(dev.getKey());
		}
		
		int b_index=0;
		GA_Problem_Parameter.bugs=new Bug[bugs.size()];
		for(Entry<Integer, Bug> b2:bugs.entrySet()){
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
		GA_Problem_Parameter.population=500;
		
	}
	
	//find solution to assign tasks to the developers
	public static void Assigning(NondominatedPopulation[] results, int runNum, int fileNum) throws IOException{
		GA_Problem_Parameter.setArrivalTasks();
		
		String path=System.getProperty("user.dir")+"\\PS\\"+fileName+".ps";
	    Instrumenter instrumenter_1=new Instrumenter().withProblem("KRRGZCompetenceMulti2").withReferenceSet(new File(path)).withFrequency(50000).attachAll()
	    		.withFrequencyType(FrequencyType.EVALUATIONS);
	    Instrumenter instrumenter_2=new Instrumenter().withProblem("SchedulignDriven").withReferenceSet(new File(path)).withFrequency(50000).attachAll()
	    		.withFrequencyType(FrequencyType.EVALUATIONS);;
		//try{
			NondominatedPopulation result_Karim=new Executor().withProblemClass(KRRGZCompetenceMulti2.class).withAlgorithm("NSGAII")
					.withMaxEvaluations(250000).withProperty("populationSize",GA_Problem_Parameter.population).withProperty("operator", "ux+um")
					.withProperty("ux.rate", 0.9).withProperty("um.rate", 0.05).withInstrumenter(instrumenter_1).run();
			
			System.out.println("finished KRRGZ");
			NondominatedPopulation result_me=new Executor().withProblemClass(SchedulingDriven.class).withAlgorithm("NSGAII")
					.withMaxEvaluations(250000).withProperty("populationSize",GA_Problem_Parameter.population).withProperty("operator", "ux+um")
					.withProperty("ux.rate", 0.9).withProperty("um.rate", 0.05).withInstrumenter(instrumenter_2).run();
			
			System.out.println("finished Competence-multi2 one");
			
			
		    System.out.println("finished Schedule-based one");
		 
		   
		    //int SD=result_me.size();
		    //int KRRGZ=result_Karim.size();
		    
		   Accumulator accumulator = instrumenter_1.getLastAccumulator();
		   for (int i=0; i<accumulator.size("NFE"); i++) {
			   System.out.println(accumulator.get("NFE", i) + "\t" +
					   accumulator.get("GenerationalDistance", i));
			   System.out.println();
			   ArrayList<Solution> solutions = (ArrayList<Solution>)accumulator.get("Approximation Set", i);
			   for(Solution s:solutions)
				   System.out.println(s.getObjective(0)+ "  "+s.getObjective(1));
			   System.out.println();
			   System.out.println();
			   accumulator.saveCSV(new File(System.getProperty("user.dir")+"\\paretos\\ParetoFront_KRRGZ_"+fileName+".csv"));
			  }
		   
		   accumulator=instrumenter_2.getLastAccumulator();
		   for (int i=0; i<accumulator.size("NFE"); i++) {
			   System.out.println(accumulator.get("NFE", i) + "\t" +
					   accumulator.get("GenerationalDistance", i));
			   System.out.println();
			   ArrayList<Solution> solutions = (ArrayList<Solution>)accumulator.get("Approximation Set", i);
			   for(Solution s:solutions)
				   System.out.println(s.getObjective(0)+ "  "+s.getObjective(1));
			   System.out.println();
			   System.out.println();
			   accumulator.saveCSV(new File(System.getProperty("user.dir")+"\\paretos\\ParetoFront_SD_"+fileName+".csv"));
			  }
		   
		    Analyzer analyzer=new Analyzer().includeAllMetrics();
			
		    analyzer.add("KRRGZ", result_Karim);
		    analyzer.add("Scheduling", result_me);
		    NondominatedPopulation rs=analyzer.getReferenceSet();
		    int test=rs.size();
		    /*File targetRefSet=new File(System.getProperty("user.dir")+"//PS//"+fileName+".ps");
		    analyzer.saveReferenceSet(targetRefSet);*/
		    
			PrintStream ps_ID=new PrintStream(new File(System.getProperty("user.dir")+"//results//AnalyzerResults_"+runNum+"_"+fileNum+".txt"));
			analyzer.withProblemClass(SchedulingDriven.class).printAnalysis(ps_ID);
			ps_ID.close();
			analyzer.saveData(new File(System.getProperty("user.dir")+"//results//AnalyzerResults"),Integer.toString(runNum) , Integer.toString(fileNum));
		//}
		//catch(Exception e){
			//starting(fileNum, runNum);
		//}
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
	
}
