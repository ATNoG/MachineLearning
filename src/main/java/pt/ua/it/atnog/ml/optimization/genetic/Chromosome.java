package pt.ua.it.atnog.ml.optimization.genetic;

import pt.ua.it.atnog.ml.utils.workers.Parallel;

public abstract class Chromosome  extends Parallel implements Comparable<Chromosome> {
	private double fitness;
	private boolean computeFitness = false;

	protected abstract double computeFitness();

	public double fitness() {
		if (!computeFitness) {
			fitness = computeFitness();
			computeFitness = true;
		}
		return fitness;
	}
	
	public void mutation() {
		computeFitness = false;
		computeMutation();
	}
	
	public abstract void computeMutation();

	public abstract Chromosome crossover(Chromosome parent);

	public int compareTo(Chromosome o) {
		return Double.compare(o.fitness, this.fitness);
	}
}
