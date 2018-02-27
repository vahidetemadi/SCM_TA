package SCM_TA_V1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Queue;

import org.jgrapht.graph.DefaultEdge;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;


public class Test1 {
	public static HashMap<Integer,Developer> developers=new HashMap<Integer,Developer>();
	static HashMap<Integer,Bug> bugs=new HashMap<Integer,Bug>();
	static Queue<Bug> orderdBugs; 
	static Solution solution=null;
	static HashMap<Integer , Zone> columns=new HashMap<Integer, Zone>();
	static Project project=new Project();
	static int roundnum=0;
	
	public static void main(String[] args) throws IOException, NoSuchElementException, URISyntaxException{	
		for(int runNum=0;runNum<50;runNum++){
			roundnum++;
			double[] costs=new double[2];
			developers.clear();
			bugs.clear();
			devInitialization();
			int roundNum=5;
			for(int i=1;i<=roundNum;i++){
			bugInitialization(i);
			GA_Problem_Parameter.candidateSolutonGeneration();
			NondominatedPopulation[] results = new NondominatedPopulation[2]; 
			results=Assigning(results);
			solution=results[1].get(results[1].size()/2);
			writeResult(runNum,i,results);
			System.out.println("finished writing");
			afterRoundUpdating(solution);
			removeDevelopers();
			}
		}
	}
	
	public static void devInitialization() throws IOException,NoSuchElementException, URISyntaxException{
		//initialize developers
				System.out.println("enter the developrs file");
				Developer developer = null;
				Scanner sc=new Scanner(System.in);
				sc=new Scanner(new File(System.getProperty("user.dir")+"\\src\\SCM_TA_V1\\bug-data\\bug-data\\JDTDeveloper.txt"));
				System.out.println("enter the devlopers wage file");
				Scanner scan=new Scanner(System.in);
				scan=new Scanner(new File(System.getProperty("user.dir")+"\\src\\SCM_TA_V1\\bug-data\\bug-data\\JDTDeveloperWithWage.txt"));
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
						String[] items=sc.nextLine().split("\t",-1);
						String[] wage_items=scan.nextLine().split("\t",-1);
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
				GA_Problem_Parameter.developers=developers;
				
				
				
	}
	
	public static void bugInitialization(int roundNum) throws IOException,NoSuchElementException{		
		Scanner sc=new Scanner(System.in);
		int i=0;
		int j=0;
		/*generate bug objects*/
		System.out.println("enter the bugs files");
		Bug bug=null;
		//sc=new Scanner(System.in);
		sc=new Scanner(System.getProperty("user.dir")+"\\src\\SCM_TA_V1\\bug-data\\JDT\\efforts");
		Scanner sc1=null;
		int n=1;
		for(File fileEntry:new File(sc.nextLine()).listFiles()){
			sc1=new Scanner(new File(fileEntry.toURI()));
			i=0;
			j=0;
			while(sc1.hasNextLine() && roundNum==n){
				//counter "i" has set to record the name of each zone (the header of each file)
				if(i==0){
						String[] items=sc1.nextLine().split("\t",-1);
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

		System.out.println(bugs.keySet());
					
		/*set bug dependencies*/
		
		System.out.println("enter the bug dependency files");
		sc=new Scanner(System.getProperty("user.dir")+"\\src\\SCM_TA_V1\\bug-data\\JDT\\dependencies");
		String[] columns_bug=null;
		for(Bug b:bugs.values()){
			b.DB.clear();
		}
		for(File fileEntry:new File(sc.nextLine()).listFiles()){
			sc1=new Scanner(new File(fileEntry.toURI()));
			i=0;
			while(sc1.hasNextLine()){
				if(i>0){
					String s=sc1.nextLine();
					columns_bug=s.split(",");
					int k=1;
					if(columns_bug[k].toString().length()>1 && columns_bug[k].trim().length() > 0){
						try{
							bugs.get(Integer.parseInt(columns_bug[k-1])).DB.add(bugs.get(Integer.parseInt(columns_bug[k])));
							System.out.println(bugs.get(Integer.parseInt(columns_bug[k])));
						}
						catch(NullPointerException e){
							
						}
					}
				}
				else{
					sc1.nextLine();
				}
				i++;
			}
		}
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
		GA_Problem_Parameter.Num_of_functions_Multi=2;
		GA_Problem_Parameter.Num_of_variables=0;
		for(Entry<Integer, Bug>  b:bugs.entrySet()){
			for(Map.Entry<Zone, Double>  zone:b.getValue().BZone_Coefficient.entrySet()){
				GA_Problem_Parameter.Num_of_variables++;
			}
			}
		GA_Problem_Parameter.population=100;
		
	}
	
	
	
	
	
	
	
	//find solution to assign tasks to the developers
	public static NondominatedPopulation[] Assigning(NondominatedPopulation[] results){
		for(Entry<Integer, Developer> d:developers.entrySet()){
			for(Entry<Zone, Double> f:d.getValue().DZone_Wage.entrySet()){
				System.out.print(f.getKey().zName+": "+f.getValue()+"---");
			}
			System.out.println();
		}
		
		
		
		
		
		NondominatedPopulation result_Karim=new Executor().withProblemClass(CompetenceMulti2_problem.class).withAlgorithm("NSGAII")
				.withMaxEvaluations(1000).withProperty("populationSize",GA_Problem_Parameter.population)
				.withProperty("sbx.rate", GA_Problem_Parameter.sbx_rate).withProperty("sbx.distributionIndex", GA_Problem_Parameter.sbx_distribution_index)
				.withProperty("pm.rate", GA_Problem_Parameter.pm_rate).withProperty("pm.distributionIndex", GA_Problem_Parameter.pm_distribution_index)
				.run();
		results[0]=result_Karim;
		
		System.out.println("finished first one");
		
	    NondominatedPopulation result_me=new Executor().withProblemClass(InformationDifussion.class).withAlgorithm("NSGAII")
				.withMaxEvaluations(1000).withProperty("populationSize",GA_Problem_Parameter.population)
				.withProperty("sbx.rate", GA_Problem_Parameter.sbx_rate).withProperty("sbx.distributionIndex", GA_Problem_Parameter.sbx_distribution_index)
				.withProperty("pm.rate", GA_Problem_Parameter.pm_rate).withProperty("pm.distributionIndex", GA_Problem_Parameter.pm_distribution_index)
				.run();
	    results[1]=result_me;

		System.out.println("finished second one");
	    return results;
	    
	}
	
	//write results for testing
	public static void writeResult(int runNum,int roundNum, NondominatedPopulation[] result) throws FileNotFoundException{
		//write results to CSV for each round
		PrintWriter pw=new PrintWriter(new File(System.getProperty("user.dir")+"\\results\\solutions_Karim_round "+runNum+"_"+roundNum+".csv"));
		StringBuilder sb=new StringBuilder();
		for(Solution solution:result[0]){
			sb.append(solution.getObjective(0)+","+solution.getObjective(1));
			sb.setLength(sb.length()-1);
			sb.append("\n");
		}
		pw.write(sb.toString());
		pw.close();
		
		pw=new PrintWriter(new File(System.getProperty("user.dir")+"\\results\\solutions_Me_round "+runNum+"_"+roundNum+".csv"));
		sb.setLength(0);
		for(Solution solution:result[1]){
			sb.append(solution.getObjective(0)+","+solution.getObjective(1));
			sb.setLength(sb.length()-1);
			sb.append("\n");
		}
		pw.write(sb.toString());
		pw.close();
		
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
	
}
