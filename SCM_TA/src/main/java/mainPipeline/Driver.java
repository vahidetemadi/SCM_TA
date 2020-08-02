package main.java.mainPipeline;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.*;
import java.util.stream.Collectors;

import org.moeaframework.algorithm.single.AggregateObjectiveComparator;
import org.moeaframework.algorithm.single.GeneticAlgorithm;
import org.moeaframework.algorithm.single.LinearDominanceComparator;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Selection;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.operator.GAVariation;
import org.moeaframework.core.operator.OnePointCrossover;
import org.moeaframework.core.operator.RandomInitialization;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.core.operator.real.PM;
import org.moeaframework.core.variable.EncodingUtils;

import com.opencsv.CSVWriter;

import main.java.SCM_TA_V1.Developer;
import main.java.SCM_TA_V1.GA_Problem_Parameter;
import main.java.SCM_TA_V1.Zone;
import main.java.featureTuning.FeatureInitialization;
import main.java.featureTuning.FeatureInitializationV1;
import main.java.featureTuning.FeatureSetV1;
import main.java.featureTuning.Stubs;


public class Driver {
	static Population finalPopulation;
	static HashMap<String, Object> allMaps = new HashMap<String, Object>();
	public static void main(String[] args) throws IOException {
		//get dataset name 
		System.out.println("Enter the dataset name:");
		Scanner sc=new Scanner(System.in);
		FeatureInitializationV1.datasetName=sc.next();
		
		//runMultiObjective();
		for(int i=1; i<=1; i++) {
			finalPopulation= runSeed(); 		/* call the run for single seed */
			writeResutls(finalPopulation, FeatureInitializationV1.datasetName, i); 		/* write down the results to the csv file */
			sendResultsToServer(finalPopulation);				/* send the results to the central server */
		}
	}
	
	
	public static Population runSeed() {
		//create object of type "FeatureInitialization"
		FeatureInitialization featureInitializatin=FeatureInitializationV1.getInstance();
		featureInitializatin.initializeAllFeatures();
		
		//fill the temp lists
		Stubs.createTempStateSequence();
		Stubs.fillChurnsSequence();
		
		//Run GA for InitializedfFeatureProblem
		InitializedFeaturesProblem problem=new InitializedFeaturesProblem(5, 1);

        Selection selection = new TournamentSelection(2, 
        								new ParetoDominanceComparator());

        Variation variation = new GAVariation(
                new OnePointCrossover(0.9),
                new PM(0.05, 0.5));

        Initialization initialization = new RandomInitialization(problem, 1);
		AggregateObjectiveComparator comparator=new LinearDominanceComparator();
        
        GeneticAlgorithm GA=new GeneticAlgorithm(problem, comparator, initialization, selection, variation);
        
        //run GA single objective
        while (GA.getNumberOfEvaluations() < 2) {
            GA.step();
        }
        
        NondominatedPopulation result=GA.getResult();
        Population p=GA.getPopulation();
        
        for (Solution solution : result) {
            System.out.println(Arrays.toString(EncodingUtils.getReal(solution)) +
                    " => " + solution.getObjective(0));
        }
        return p;
	}
	
	/*
	public static HashMap<String, Object> runMultiObjective(){
		
		AdaptiveAssignmentPipline adaptive=AdaptiveAssignmentPipline.getInstance();
		HashMap<String, Double> totals=new HashMap<String, Double>();
		HashMap<String, ArrayList<Double>> totalsOverTime=new HashMap<String, ArrayList<Double>>();
		HashMap<Integer, HashMap<Integer, Developer>> devsProfileOverTime=new HashMap<Integer, HashMap<Integer,Developer>>();
		
		try {
			adaptive.run(totals, totalsOverTime, devsProfileOverTime);
		} catch (NoSuchElementException | ClassNotFoundException | IOException | URISyntaxException
				| CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//add the maps to the main map list
		allMaps.put("TCT_adaptive", totals.get("TCT_adaptive"));
		allMaps.put("TCT_static", totals.get("TCT_static"));
		allMaps.put("TID_static", totals.get("TID_static"));
		allMaps.put("TID_adaptive", totals.get("TID_adaptive"));
		allMaps.put("CoT_static", totalsOverTime.get("CoT_static"));
		allMaps.put("CoT_adaptive", totalsOverTime.get("CoT_adaptive"));
		allMaps.put("IDoT_static", totalsOverTime.get("IDoT_static"));
		allMaps.put("IDoT_adaptive", totalsOverTime.get("IDoT_adaptive"));
		allMaps.put("SoT", totalsOverTime.get("SoT"));
		allMaps.put("devsProfile0", devsProfileOverTime.get(0));
		allMaps.put("devsProfile1", devsProfileOverTime.get(1));
		allMaps.put("costPerRound_static", totalsOverTime.get("costPerRound_static"));
		allMaps.put("costPerRound_adaptive", totalsOverTime.get("costPerRound_adaptive"));
		allMaps.put("EoT_adaptive", totalsOverTime.get("EoT_adaptive"));
		
		return null;
	}
	*/
	
	
	
	/**
	 * Write the results into the file according to the dataset name under analysis
	 * all the experiment runs output to a unique location
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public static void writeResutls(Population p, String datasetName, int runNum) throws IOException {
		File file=new File(System.getProperty("user.dir")+File.separator+"results"+ File.separator+ "self-adaptive"
				+File.separator+ datasetName+"_"+runNum+".csv");
		File file_developersProfile_static, file_developersProfile_adaptive;
		PrintWriter pw_devProfile_static, pw_devProfile_adaptive;
		HashMap<Integer, Developer> devList;
	
		file.getParentFile().mkdir(); 				/* make missed dirs*/
		PrintWriter printWriter=new PrintWriter(file);
		CSVWriter csvWriter=new CSVWriter(printWriter);
		String[] csvFileOutputHeader= {"solution","totalCostID", "totalCostStatic", "totalIDStatic", "totalIDID", "CoT_static", "CoT_adaptive", "IDoT_static", "IDoT_adaptive", "SoT"
					, "costPerRound_static", "idPerRound_static", "idPerRound_adaptive", "costPerRound_adaptive", "EoT_static", "EoT_adaptive, ExoTperRound_adaptive"};
		csvWriter.writeNext(csvFileOutputHeader);		//write the header of the csv file
		Solution tempSolution;
		
		for(int i=0; i<p.size(); i++) {
			tempSolution=p.get(i);
			csvWriter.writeNext(new String[] {Arrays.toString(EncodingUtils.getInt(tempSolution)),  String.format("%.2f", tempSolution.getObjective(0)),
												String.format("%.2f", tempSolution.getAttribute("TCT_static")), String.format("%.2f", tempSolution.getAttribute("TID_static")),
												String.format("%.2f", tempSolution.getAttribute("TID_adaptive")),
												((ArrayList<Double>)tempSolution.getAttribute("CoT_static")).stream().map(x -> String.format("%.2f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("CoT_adaptive")).stream().map(x -> String.format("%.2f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("IDoT_static")).stream().map(x -> String.format("%.4f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("IDoT_adaptive")).stream().map(x -> String.format("%.4f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("SoT")).stream().map(x -> String.format("%.0f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("costPerRound_static")).stream().map(x -> String.format("%.2f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("costPerRound_adaptive")).stream().map(x -> String.format("%.2f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("idPerRound_static")).stream().map(x -> String.format("%.2f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("idPerRound_adaptive")).stream().map(x -> String.format("%.2f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("EoT_static")).stream().map(x -> String.format("%.4f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("EoT_adaptive")).stream().map(x -> String.format("%.4f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("ExoTperRound_adaptive")).stream().map(x -> String.format("%.4f", x)).collect(Collectors.toList()).toString()
												});
			
			//deserialize dev lists
			//iterate up to 
			int devCount=0;
			for(int j=0; j<GA_Problem_Parameter.numberOfTimesMakingProfileComparison; j++) {
				devCount++;
				
				file_developersProfile_static=new File(System.getProperty("user.dir")+File.separator+"results"+ File.separator+ "self-adaptive"
						+File.separator+ "devProfiles"+ File.separator + i+"_static_"+j+".txt");
				file_developersProfile_adaptive=new File(System.getProperty("user.dir")+File.separator+"results"+ File.separator+ "self-adaptive"
						+File.separator+ "devProfiles"+ File.separator + i+"_adaptive_"+j+".txt");
				file_developersProfile_static.getParentFile().mkdir();
				file_developersProfile_adaptive.getParentFile().mkdir();
				pw_devProfile_static=new PrintWriter(file_developersProfile_static);
				pw_devProfile_adaptive=new PrintWriter(file_developersProfile_adaptive);
				

				devList=(HashMap<Integer, Developer>)tempSolution.getAttribute("devsProfile"+j);
				
				//create the header for the files
				pw_devProfile_static.append("Dev#");
				pw_devProfile_adaptive.append("Dev#");
				int size=devList.entrySet().size();
				for(Map.Entry<Zone, Double> entry:devList.get(1).getDZone_Coefficient().entrySet()) {
					pw_devProfile_static.append("\t"+entry.getKey().zName);
					pw_devProfile_adaptive.append("\t"+entry.getKey().zName);
				}
				
				//add new line
				pw_devProfile_static.append("\n");
				pw_devProfile_adaptive.append("\n");
				
				//write the devs' profile
				String line_static, line_adaptive;
				for(Map.Entry<Integer, Developer> dev:devList.entrySet()) {
					line_static="";
					line_adaptive="";
					line_static+=dev.getKey()+"\t";
					line_adaptive+=dev.getKey()+"\t";
					for(Map.Entry<Zone, Double> zoneItem:dev.getValue().getDZone_Coefficient_static().entrySet()) {
						line_static+=dev.getValue().getDZone_Coefficient_static().get(zoneItem.getKey())+"\t";
						line_adaptive+=dev.getValue().getDZone_Coefficient().get(zoneItem.getKey())+"\t";
					}
					
					//trim to remove the unwanted tab and then add new line
					line_static.trim();
					line_adaptive.trim();
					if(devCount<GA_Problem_Parameter.developers_all.size()) {
						line_adaptive+="\n";
						line_static+="\n";
					}
					
					//add the line to the printwriter
					pw_devProfile_static.append(line_static);
					pw_devProfile_adaptive.append(line_adaptive);
				}
				
				//close the opened printwriters
				pw_devProfile_adaptive.close();
				pw_devProfile_static.close();
				
			}
			
			
			
			
			//decoding the solution
			System.out.println("TCR");
			System.out.println(FeatureInitializationV1.getInstance().getTCR().get((EncodingUtils.getInt(tempSolution))[2]));
			System.out.println("EM");
			System.out.println(Arrays.deepToString(FeatureInitializationV1.getInstance().getEm().get((EncodingUtils.getInt(tempSolution))[3])));
			System.out.println("TM");
			System.out.println(Arrays.deepToString(FeatureInitializationV1.getInstance().getTm().get((EncodingUtils.getInt(tempSolution))[4])));
			
		}
		
		//logging the end of running
		Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, "Just finished the sample run");
		
		
		//close the writers
		csvWriter.close();
		printWriter.close();
	}
	/**
	 * Sending the results to the server over the network--- a service over in the server update the results set  
	 * @param p
	 */
	public static void sendResultsToServer(Population p) {

		
		
	}


}
