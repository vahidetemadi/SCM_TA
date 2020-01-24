package mainPipeline;
import featureTuning.*;

import java.util.Arrays;
import java.util.List;

import org.moeaframework.Executor;
import org.moeaframework.algorithm.AbstractEvolutionaryAlgorithm;
import org.moeaframework.algorithm.single.GeneticAlgorithm;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
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
	
	public static void main(String[] args) {
		//run single seed
		runSeed();
	}
	
	
	public static void runSeed() {
		//create object of type "FeatureInitialization"
		FeatureInitialization featureInitializatin=FeatureInitializationV1.getInstance();
		featureInitializatin.initializeAllFeatures();
		
		//Run GA for InitializedfFeatureProblem
		
		InitializedFeaturesProbelm problem=new InitializedFeaturesProbelm(5, 1);

        Selection selection = new TournamentSelection(2, 
                new ParetoDominanceComparator());

        Variation variation = new GAVariation(
                new SBX(15.0, 1.0),
                new PM(20.0, 0.5));

        Initialization initialization = new RandomInitialization(problem, 100);
		
        GeneticAlgorithm GA=new GeneticAlgorithm(problem, null, initialization, selection, variation);
        
        //run GA single objective
        while (GA.getNumberOfEvaluations() < 10000) {
            GA.step();
        }
        
        NondominatedPopulation result=GA.getResult();
        Population p=GA.getPopulation();
        for(int i=0; i<p.size(); i++)
        	System.out.println(p.get(i));
        
        for (Solution solution : result) {
            System.out.println(Arrays.toString(EncodingUtils.getReal(solution)) +
                    " => " + solution.getObjective(0));
        }
		
	}
	
}
