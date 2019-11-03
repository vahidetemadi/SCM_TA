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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Queue;
import java.io.PrintWriter;

import org.apache.commons.math3.filter.KalmanFilter;
import org.jgrapht.graph.DefaultEdge;
import org.moeaframework.Analyzer;
import org.moeaframework.Analyzer.AnalyzerResults;
import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.algorithm.NSGAIIITest;
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


import org.apache.log4j.BasicConfigurator;
import org.jppf.client.JPPFClient;
import org.jppf.client.concurrent.JPPFExecutorService;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;


public class Assignment {
	public static HashMap<Integer,Developer> developers=new HashMap<Integer,Developer>();
	static HashMap<Integer,Bug> bugs=new HashMap<Integer,Bug>();
	static Queue<Bug> orderdBugs; 
	//static Solution solution=null;
	static HashMap<Integer , Zone> zoneList=new HashMap<Integer, Zone>();
	static Project project=new Project();
	static int roundnum=0;
	static String fileName;
	static StringBuilder sb=new StringBuilder();
	static PrintWriter pw;  
	//DevMetrics devMetric=new DevMetrics();
	
	public static void main(String[] args) throws NoSuchElementException, IOException, URISyntaxException, NumberFormatException, CloneNotSupportedException{
		Scanner sc=new Scanner(System.in);
		System.out.println("please insert the number of desired schedules:");
		GA_Problem_Parameter.numOfEvaluationLocalSearch=sc.nextInt();
		System.out.println("Specify the name of your project:");
		GA_Problem_Parameter.pName=sc.next();
		System.out.println("The file number you want to launch the run from:");
		GA_Problem_Parameter.fileNum=sc.nextInt();
		System.out.println("The run number you want to launch the run from:");
		GA_Problem_Parameter.runNum=sc.nextInt();
		String mode="running";
		if(mode=="running"){
			runExperiment();
		}
		else if (mode=="representatoin"){
			changeRepresentation();
		}
		
	}
	
	public static void runExperiment() throws NoSuchElementException, IOException, URISyntaxException, NumberFormatException, CloneNotSupportedException{
		GA_Problem_Parameter.createPriorityTable();
		for(int runNum=GA_Problem_Parameter.runNum;runNum<=GA_Problem_Parameter.runNum;runNum++){
			double[] costs=new double[2];
			developers.clear();
			bugs.clear();
			
			//load developers into the system
			devInitialization();
			
			//set the round number upon the project name
			int numOfFiles=9;
			if(GA_Problem_Parameter.pName.equals("JDT")){
				numOfFiles=9;
				GA_Problem_Parameter.numOfDevs=20;
			}
			else {
				numOfFiles=10;
				GA_Problem_Parameter.numOfDevs=78;
			}
			
			//iterate over the under experiment files
			for(int i=GA_Problem_Parameter.fileNum;i<=GA_Problem_Parameter.fileNum;i++){
				GA_Problem_Parameter.fileNum=i;
				if(i==numOfFiles)
					GA_Problem_Parameter.fileNum=1;
				starting(i, runNum);
			}
			System.gc();
		}
		
	}

	public static void starting(int fileNum, int runNum) throws IOException, NumberFormatException, NoSuchElementException, CloneNotSupportedException{
		//set the threshold to initialize the population 
		GA_Problem_Parameter.thresoldForPopulationGeneration=0;
		bugInitialization(fileNum);
		GA_Problem_Parameter.generateModelofBugs();
		GA_Problem_Parameter.candidateSolutonGeneration();
		NondominatedPopulation[] results = new NondominatedPopulation[2]; 
		Assigning(results,runNum,fileNum);
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
				sc=new Scanner(new File(System.getProperty("user.dir")+"//src//SCM_TA_V1//bug-data//bug-data//"+GA_Problem_Parameter.pName+"Developer.txt"));
				System.out.println("enter the devlopers wage file");
				Scanner scan=new Scanner(System.in);
				scan=new Scanner(new File(System.getProperty("user.dir")+"//src//SCM_TA_V1//bug-data//bug-data//"+GA_Problem_Parameter.pName+"DeveloperWithWage.txt"));
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
							//d.DZone_Coefficient.put(entry.getKey(),getNonZeroMin(d.DZone_Coefficient));
							d.DZone_Coefficient.put(entry.getKey(),0.05);
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
	public static void bugInitialization(int roundNum) throws IOException,NoSuchElementException, NumberFormatException, CloneNotSupportedException{	
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
		File[] files=new File(System.getProperty("user.dir")+"//src//SCM_TA_V1//bug-data//"+GA_Problem_Parameter.pName+"//efforts").listFiles();
		Arrays.sort(files);
		for(File fileEntry:files){
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
								double d=Double.parseDouble(items[k]);
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
		sc=new Scanner(System.getProperty("user.dir")+"//src//SCM_TA_V1//bug-data//"+GA_Problem_Parameter.pName+"//dependencies");
		String[] columns_bug=null;
		for(Bug b:bugs.values()){
			b.DB.clear();
		}
		for(File fileEntry:new File(System.getProperty("user.dir")+"//src//SCM_TA_V1//bug-data//"+GA_Problem_Parameter.pName+"//dependencies").listFiles()){
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
			b.getValue().setTopo();
		}
		GA_Problem_Parameter.population=500;
		GA_Problem_Parameter.evaluation=250000;
		
	}
	
	//find solution to assign tasks to the developers
	public static void Assigning(NondominatedPopulation[] results, int runNum, int fileNum) throws IOException, NumberFormatException, NoSuchElementException, CloneNotSupportedException{
		GA_Problem_Parameter.setArrivalTasks();
		
		String path=System.getProperty("user.dir")+File.separator+"PS"+File.separator+GA_Problem_Parameter.pName+File.separator+fileName+".ps";
	    Instrumenter instrumenter_KRRGZ=new Instrumenter().withProblem("KRRGZCompetenceMulti2").withReferenceSet(new File(path)).withFrequency(GA_Problem_Parameter.evaluation/5).attachAll()
	    		.withFrequencyType(FrequencyType.EVALUATIONS);
	    Instrumenter instrumenter_NSGAIIITA=new Instrumenter().withProblem("NSGAIIITAGLS").withReferenceSet(new File(path)).withFrequency(GA_Problem_Parameter.evaluation/5).attachAll()
	    		.withFrequencyType(FrequencyType.EVALUATIONS);
	    Instrumenter instrumenter_RS=new Instrumenter().withProblem("RandomSearch").withReferenceSet(new File(path)).withFrequency(GA_Problem_Parameter.evaluation/5).attachAll()
	    		.withFrequencyType(FrequencyType.EVALUATIONS);
		//try{
	    
		    GA_Problem_Parameter.flag=1;
			NondominatedPopulation NDP_SD=new Executor().withProblemClass(NSGAIIITAGLS.class).withAlgorithm("NSGAII")
					.withMaxEvaluations(GA_Problem_Parameter.evaluation).withProperty("populationSize",GA_Problem_Parameter.population).withProperty("operator", "1x+um")
					.withProperty("1x.rate", 0.9).withProperty("um.rate", 0.01).withInstrumenter(instrumenter_NSGAIIITA).run();
			
			System.out.println("finished NSGAIITAGLS");
	    
	    	GA_Problem_Parameter.flag=1;
	    	NondominatedPopulation NDP_RS=new Executor().withProblemClass(RandomSearch.class).withAlgorithm("Random")
	    			.withProperty("populationSize", GA_Problem_Parameter.population).withMaxEvaluations(GA_Problem_Parameter.evaluation)
	    			.withInstrumenter(instrumenter_RS).run();	

	    	System.out.println("finished RS");
	    	
	    	GA_Problem_Parameter.flag=1;
			NondominatedPopulation NDP_KRRGZ=new Executor().withProblemClass(KRRGZCompetenceMulti2.class).withAlgorithm("NSGAII")
					.withMaxEvaluations(GA_Problem_Parameter.evaluation).withProperty("populationSize",GA_Problem_Parameter.population).withProperty("operator", "1x+um")
					.withProperty("1x.rate", 0.9).withProperty("um.rate", 0.05).withInstrumenter(instrumenter_KRRGZ).run();
			
			System.out.println("finished KRRGZ");
			
			
			
			
			
			/*//instantiation of target JPPF
			//BasicConfigurator.configure();
			JPPFClient jppfClient = null;
			JPPFExecutorService jppfExecutor = null;
			NondominatedPopulation NDP_SD=null;
			BasicConfigurator.configure();
			
			try{
				jppfClient=new JPPFClient();
				jppfExecutor=new JPPFExecutorService(jppfClient);
				jppfExecutor.setBatchSize(GA_Problem_Parameter.population);
				
				
				NDP_SD=new Executor().withProblemClass(NSGAIIITA_paralleled.class).withAlgorithm("NSGAIII")
						.withMaxEvaluations(250000).withProperty("populationSize",GA_Problem_Parameter.population).withProperty("operator", "sbx+pm")
						.withProperty("sbx.rate", 1.0).withProperty("pm.rate", 0.01).withProperty("divisions",4)
						.withProperty("sbx.distributionIndex", 30).withProperty("pm.distributionIndex", 40).withInstrumenter(instrumenter_2)
						.distributeWith(jppfExecutor).run();
			}
			catch(Exception e){
				e.printStackTrace();
			}finally {
				if (jppfExecutor != null) {
					jppfExecutor.shutdown();
				}
			
				if (jppfClient != null) {
					jppfClient.close();
				}
		        
			}    
*/
		    //pareto front of KRRGZ gets saved in csv format
		    sb.setLength(0);
		    //create string builder to include the nonDominated for KRRGZ
		    for(Solution s:NDP_KRRGZ){
				   sb.append(s.getObjective(0)+ ","+s.getObjective(1));
				   sb.append("\n");
		    }
		    pw=new PrintWriter(new File(System.getProperty("user.dir")+File.separator+"paretoFronts"+File.separator+"KRRGZ_"+fileName+"_"+runNum+".csv"));
		    pw.write(sb.toString());
		    pw.close();
		   
		    
		    //pareto front of SD gets saved in csv format
		    sb.setLength(0);
		    //create string builder to include the nonDominated for KRRGZ
		    for(Solution s:NDP_SD){
				   sb.append(s.getObjective(0)+ ","+s.getObjective(1));
				   sb.append("\n");
				   if(s.getSchedule()!=null)
					   System.out.println(s.getSchedule());
		    }
		    pw=new PrintWriter(new File(System.getProperty("user.dir")+File.separator+"paretoFronts"+File.separator+"SD_"+fileName+"_"+runNum+".csv"));
		    pw.write(sb.toString());
		    pw.close();
		    
		    
		    //pareto front for RS method   
		    for(Solution s:NDP_RS){
				   sb.append(s.getObjective(0)+ ","+s.getObjective(1));
				   sb.append("\n");
		    }
		    pw=new PrintWriter(new File(System.getProperty("user.dir")+File.separator+"paretoFronts"+File.separator+"RS_"+fileName+"_"+runNum+".csv"));
		    pw.write(sb.toString());
		    pw.close();
		    
		    //write down instrumenters results
		    updateArchive(instrumenter_KRRGZ, instrumenter_NSGAIIITA,instrumenter_RS, runNum);
		    
		    
		    //write down the analyzer results
		    Analyzer analyzer=new Analyzer().includeAllMetrics();
		    try{
			    analyzer.add("KRRGZ", NDP_KRRGZ);
			    analyzer.add("NSGAIIITAGLS", NDP_SD);
		    	analyzer.add("RS", NDP_RS);
		    }
		    catch(Exception e){
		    	starting(fileNum, runNum);
		    	return;
		    }
		   
		    
		    //generate the pareto set in favor of archiving	    
		    /*File targetRefSet=new File(System.getProperty("user.dir")+"//PS//"+GA_Problem_Parameter.pName+fileName+".ps");
		     *
		     *
		     *
		    analyzer.saveReferenceSet(targetRefSet);*/
		    File f=new File(System.getProperty("user.dir")+File.separator+"results"+File.separator+GA_Problem_Parameter.pName+File.separator+"AnalyzerResults_"+fileName+"_"+runNum+"_"+fileNum+".yaml");
		    f.getParentFile().mkdirs();
			PrintStream ps_ID=new PrintStream(f);
			try{
				analyzer.withProblemClass(NSGAIIITAGLS.class).printAnalysis(ps_ID);
			}
			catch(Exception e){
				starting(fileNum, runNum);
		    	return;
			}
			finally{
				ps_ID.close();
			}
			analyzer.saveData(new File(System.getProperty("user.dir")+File.separator+"results"+File.separator+GA_Problem_Parameter.pName+File.separator+"AnalyzerResults"),Integer.toString(runNum) , Integer.toString(fileNum));
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
		/*double min=300;
		for(Double d:entrySet.values()){
			if(min>d && d>0){
				min=d;
			}
		}
		return min;*/
		return 0.05;
	}
	
	private static void updateArchive(Instrumenter instrumenter_KRRGZ, Instrumenter instrumenter_NSGAIIITA, Instrumenter instrumenter_RS , int runNum ) throws FileNotFoundException{
		 PrintWriter pw=null;
		//instrumenter for KRRGZ 
		   Accumulator accumulator = instrumenter_KRRGZ.getLastAccumulator();
		   for (int i=0; i<accumulator.size("NFE"); i++) {
			   System.out.println(accumulator.get("NFE", i) + "\t" +
					   accumulator.get("GenerationalDistance", i));
			   System.out.println();
			   ArrayList<Solution> solutions = (ArrayList<Solution>)accumulator.get("Approximation Set", i);
			   sb.setLength(0);
			   sb.append("Time,Cost");
			   sb.append("\n");
			   for(Solution s:solutions){
				   System.out.println(s.getObjective(0)+ "  "+s.getObjective(1));
				   sb.append(s.getObjective(0)+ ","+s.getObjective(1));
				   sb.append("\n");
			   }
			   System.out.println();
			   System.out.println();
			   File f=new File(System.getProperty("user.dir")+File.separator+"archives"+File.separator+runNum+File.separator+GA_Problem_Parameter.pName+File.separator+fileName+File.separator
					   +File.separator+"KRRGZ"+File.separator+fileName+"_"+runNum+"_KRRGZ_"+accumulator.get("NFE", i)+".csv");
			   f.getParentFile().mkdirs();
			   pw=new PrintWriter(f);
			   pw.write(sb.toString());
			   pw.close();
			   //accumulator.saveCSV(new File(System.getProperty("user.dir")+"\\paretos\\ParetoFront_KRRGZ_"+fileName+".csv"));
			  }
		   
		   
		   
		   
		   //Instrumenter for SD
		   accumulator=instrumenter_NSGAIIITA.getLastAccumulator();
		   for (int i=0; i<accumulator.size("NFE"); i++) {
			   System.out.println(accumulator.get("NFE", i) + "\t" +
					   accumulator.get("GenerationalDistance", i));
			   System.out.println();
			   ArrayList<Solution> solutions = (ArrayList<Solution>)accumulator.get("Approximation Set", i);
			   sb.setLength(0);
			   sb.append("Time,Cost");
			   sb.append("\n");
			   for(Solution s:solutions){
				   System.out.println(s.getObjective(0)+ "  "+s.getObjective(1));
				   sb.append(s.getObjective(0)+ ","+s.getObjective(1));
				   sb.append("\n");
			   }
			   System.out.println();
			   System.out.println();
			   File f=new File(System.getProperty("user.dir")+File.separator+"archives"+File.separator+runNum+File.separator+GA_Problem_Parameter.pName+File.separator+fileName+File.separator
					   +File.separator+"SD"+File.separator+fileName+"_"+runNum+"_SD_"+accumulator.get("NFE", i)+".csv");
			   f.getParentFile().mkdirs();
			   pw=new PrintWriter(f);
			   pw.write(sb.toString());
			   pw.close();
			   //accumulator.saveCSV(new File(System.getProperty("user.dir")+"\\paretos\\ParetoFront_SD_"+fileName+".csv"));
			  }
		   accumulator=instrumenter_RS.getLastAccumulator();
		   for (int i=0; i<accumulator.size("NFE"); i++) {
			   System.out.println(accumulator.get("NFE", i) + "\t" +
					   accumulator.get("GenerationalDistance", i));
			   System.out.println();
			   ArrayList<Solution> solutions = (ArrayList<Solution>)accumulator.get("Approximation Set", i);
			   sb.setLength(0);
			   sb.append("Time,Cost");
			   sb.append("\n");
			   for(Solution s:solutions){
				   System.out.println(s.getObjective(0)+ "  "+s.getObjective(1));
				   sb.append(s.getObjective(0)+ ","+s.getObjective(1));
				   sb.append("\n");
			   }
			   System.out.println();
			   System.out.println();
			   File f=new File(System.getProperty("user.dir")+File.separator+"archives"+File.separator+runNum+File.separator+GA_Problem_Parameter.pName+File.separator+fileName+File.separator
					   +File.separator+"RS"+File.separator+fileName+"_"+runNum+"_RS_"+accumulator.get("NFE", i)+".csv");
			   f.getParentFile().mkdirs();
			   pw=new PrintWriter(f);
			   pw.write(sb.toString());
			   pw.close();
			   //accumulator.saveCSV(new File(System.getProperty("user.dir")+"\\paretos\\ParetoFront_SD_"+fileName+".csv"));
			  }
	}
}
