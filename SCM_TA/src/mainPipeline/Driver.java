package mainPipeline;
import featureTuning.*;

import java.io.File;
import java.util.Arrays;
import java.util.Scanner;

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

public class Driver {
	static Population finalPopulation;
	public static void main(String[] args) {
		//run single seed
		finalPopulation= runSeed();
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
		InitializedFeaturesProbelm problem=new InitializedFeaturesProbelm(5, 1);

        Selection selection = new TournamentSelection(2, 
        								new ParetoDominanceComparator());

        Variation variation = new GAVariation(
                new SBX(15.0, 1.0),
                new PM(20.0, 0.5));

        Initialization initialization = new RandomInitialization(problem, 100);
		AggregateObjectiveComparator comparator=new LinearDominanceComparator();
        
        GeneticAlgorithm GA=new GeneticAlgorithm(problem, comparator, initialization, selection, variation);
        
        //run GA single objective
        while (GA.getNumberOfEvaluations() < 10000) {
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
	

}
