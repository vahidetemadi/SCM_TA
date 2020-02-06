package main.java.mainPipeline;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
import org.moeaframework.core.operator.RandomInitialization;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.core.operator.real.PM;
import org.moeaframework.core.operator.real.SBX;
import org.moeaframework.core.variable.EncodingUtils;

import com.opencsv.CSVWriter;

import main.java.featureTuning.FeatureInitialization;
import main.java.featureTuning.FeatureInitializationV1;


public class Driver {
	static Population finalPopulation;
	public static void main(String[] args) throws IOException {
		finalPopulation= runSeed(); 		/* call the run for single seed */
		writeResutls(finalPopulation, FeatureInitializationV1.datasetName); 		/* write down the results to the csv file */
		sendResultsToServer(finalPopulation);				/* send the results to the central server */
	}
	
	
	public static Population runSeed() {
		//create object of type "FeatureInitialization"
		FeatureInitialization featureInitializatin=FeatureInitializationV1.getInstance();
		featureInitializatin.initializeAllFeatures();
		
		//get dataset name 
		System.out.println("Enter the dataset name:");
		Scanner sc=new Scanner(System.in);
		FeatureInitializationV1.datasetName=sc.next();
		
		//Run GA for InitializedfFeatureProblem
		InitializedFeaturesProblem problem=new InitializedFeaturesProblem(5, 1);

        Selection selection = new TournamentSelection(2, 
        								new ParetoDominanceComparator());

        Variation variation = new GAVariation(
                new SBX(15.0, 1.0),
                new PM(20.0, 0.5));

        Initialization initialization = new RandomInitialization(problem, 10);
		AggregateObjectiveComparator comparator=new LinearDominanceComparator();
        
        GeneticAlgorithm GA=new GeneticAlgorithm(problem, comparator, initialization, selection, variation);
        
        //run GA single objective
        while (GA.getNumberOfEvaluations() < 100) {
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
	
	
	/**
	 * Write the results into the file according to the dataset name under analysis
	 * all the experiment runs output to a unique location
	 * @throws IOException 
	 */
	public static void writeResutls(Population p, String datasetName) throws IOException {
		File file=new File(System.getProperty("user.dir")+File.separator+"results"+ File.separator+ "self-adaptive"
				+File.separator+ datasetName+".csv");
		file.getParentFile().mkdir(); 				/* make missed dirs*/
		
		PrintWriter printWriter=new PrintWriter(file);
		CSVWriter csvWriter=new CSVWriter(printWriter);
		String[] csvFileOutputHeader= {"solution","totalCostStatic", "totalCostID", "totalIDID", "CoT_static", "CoT_adaptive", "IDoT_adaptive"};
		csvWriter.writeNext(csvFileOutputHeader);		//write the header of the csv file
		Solution tempSolution;
		
		for(int i=0; i<p.size(); i++) {
			tempSolution=p.get(i);
			csvWriter.writeNext(new String[] {Arrays.toString(EncodingUtils.getInt(tempSolution)) ,Double.toString(tempSolution.getObjective(0)),
												tempSolution.getAttribute("TCT_adaptive").toString(), tempSolution.getAttribute("TID_adaptive").toString(),
												Arrays.toString(((ArrayList<Double>)tempSolution.getAttribute("CoT_static")).toArray()),
												Arrays.toString(((ArrayList<Double>)tempSolution.getAttribute("CoT_adaptive")).toArray()),
												Arrays.toString(((ArrayList<Double>)tempSolution.getAttribute("IDoT_adaptive")).toArray())});
		}
		
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
