package org.moeaframework.algorithm;

import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

public class MultithreadEvolution_NSGAII implements Runnable {

	Population offspring;
	private Variation variation;
	Solution[] parents;
	
	public MultithreadEvolution_NSGAII(Population offspring, Variation variation, Solution[] parents ) {
		// TODO Auto-generated constructor stub
		this.offspring=offspring;
		this.variation=variation;
		this.parents=parents;
		
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		offspring.addAll(variation.evolve(parents));
		
	}

}
