import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

class Game {

	static Boolean IMPORT = true;

	static void importBase(double[] c) {
		Scanner in = null;

		try {
			in = new Scanner(new File("import.txt"));
		} catch (FileNotFoundException e) {
			System.out.println("File \"import.txt\" not found.");
			System.exit(0);
		}

		String line;
		for (int i = 0; i < c.length; i++) {
			line = in.nextLine();
			c[i] = Double.parseDouble(line);
		}
		in.close();

	}

	static void println(String s) {
		System.out.println(s);
	}

	static double[] evolveWeights() throws Exception {
		// Create a random initial population
		Random r = new Random(10101010);
		int numChrom = 50;
		Matrix population = new Matrix(numChrom, 297);
		double[] baseChromosome = new double[297];

		// set initial metaparameters that can be overwritten by import
		baseChromosome[291] = .025; // cycles
		baseChromosome[292] = .8; // mutChance
		baseChromosome[293] = .2; // deviation
		baseChromosome[294] = .15; // tourneys
		baseChromosome[295] = .9; // winnersurvives
		baseChromosome[296] = .4; // candidates

		if (IMPORT) {
			importBase(baseChromosome);
		}

		for (int i = 0; i < population.row(0).length; i++)
			population.row(0)[i] = baseChromosome[i];

		for (int i = 1; i < population.rows(); i++) {
			double[] chromosome = population.row(i);
			for (int j = 0; j < 291; j++)
				chromosome[j] = baseChromosome[j] + (0.03 * r.nextGaussian());
		}
		for (int i = 1; i < population.rows(); i++) {
			double[] chromosome = population.row(i);
			for (int j = 291; j < population.row(0).length; j++)
				chromosome[j] = baseChromosome[j];
		}
		

		// Evolve the population
		// todo: YOUR CODE WILL START HERE.
		// Please write some code to evolve this population.
		// (For tournament selection, you will need to call
		// Controller.doBattleNoGui(agent1, agent2).)
		NeuralAgent strongest = new NeuralAgent(population.row(0));
		long strength = 0;
		while (/*strength < 480*/Controller.doBattleNoGui(new ReflexAgent(), strongest) != -1) {

			double cycles = Math.max(.01, Math.min(.35, population.row(0)[291]));
			double mutChance = Math.max(.55, Math.min(.95, population.row(0)[292]));
			double deviation = Math.max(.01, Math.min(.99, population.row(0)[293]));
			double tourneys = Math.max(numChrom / 1000.0, Math.min(population.row(0)[294], numChrom / 200.0));
			double winnerSurvive = Math.max(.55, Math.min(.95, population.row(0)[295]));
			double candidates = Math.max(.2, Math.min(1, population.row(0)[296]));

			for (int c = 0; c < (int) (cycles * 1000); c++) {
				// promote diversity
				// println("cycle: " + c);
				for (int p = 0; p < population.rows(); p++) {
					if (r.nextDouble() > mutChance)
						continue;
					else {
						population.row(p)[r.nextInt(population.row(p).length)] += deviation * r.nextGaussian();
					}
				}

				cycles = Math.max(.01, Math.min(.35, population.row(0)[291]));
				mutChance = Math.max(.55, Math.min(.95, population.row(0)[292]));
				deviation = Math.max(.01, Math.min(.99, population.row(0)[293]));
				tourneys = Math.max(numChrom / 1000.0, Math.min(population.row(0)[294], numChrom / 200.0));
				winnerSurvive = Math.max(.55, Math.min(.95, population.row(0)[295]));
				candidates = Math.max(.2, Math.min(1, population.row(0)[296]));

				// natural selection
				for (int t = 0; t < (int) (tourneys * 100); t++) {
					int a, b;
					a = r.nextInt(population.rows());

					do {
						b = r.nextInt(population.rows());
					} while (a == b);

					long aFit = getFitness(new NeuralAgent(population.row(a)));
					long bFit = getFitness(new NeuralAgent(population.row(b)));

					int winner = 0;
					if (aFit > bFit)
						winner = 1;
					if (aFit < bFit)
						winner = -1;

					if (r.nextDouble() < winnerSurvive) {
						if (winner == 1 || winner == 0) {
							population.removeRow(b);
						} else if (winner == -1) {
							population.removeRow(a);
						}
					} else {
						if (winner == 1 || winner == 0) {
							population.removeRow(a);
						} else if (winner == -1) {
							population.removeRow(b);
						}
					}
				}

				// replenish population
				if (population.rows() != numChrom) {
					int existRows = population.rows();
					population.newRows(numChrom - existRows);

					for (int i = numChrom - 1; i >= numChrom - 1 - existRows; i--) {
						int index = r.nextInt(existRows);
						int[] cands = new int[(int) (candidates * 10)];

						for (int j = 0; j < candidates; j++) {
							do {
								cands[j] = r.nextInt(existRows);
							} while (cands[j] != index);
						}

						int sim = mostSimilar(population, index, cands);

						for (int j = 0; j < population.row(0).length; j++) {
							int parent = r.nextInt(2);

							if (parent == 0)
								population.row(i)[j] = population.row(index)[j];
							else
								population.row(i)[j] = population.row(sim)[j];
						}

					}
				}

			}

			for(int i = 0; i < numChrom; i++) {
				population.row(i)[291] = Math.max(.01, Math.min(.35, population.row(i)[291]));
				population.row(i)[292] = Math.max(.55, Math.min(.95, population.row(i)[292]));
				population.row(i)[293] = Math.max(.01, Math.min(.99, population.row(i)[293]));
				population.row(i)[294] = Math.max(numChrom / 1000.0, Math.min(population.row(i)[294], numChrom / 200.0));
				population.row(i)[295] = Math.max(.55, Math.min(.95, population.row(i)[295]));
				population.row(i)[296] = Math.max(.2, Math.min(1, population.row(i)[296]));
			}
			
			
			// Return arbitrary member of population
			strongest = new NeuralAgent(population.row(0));

			// display current strength rating
			strength = getFitness(strongest);
			System.out.printf("S: %d R: %d || %d %.3f %.3f %d %.3f %d\n", (int) strength, 500 - Math.abs(strength),
					(int) (1000 * cycles), mutChance, deviation, (int) (100 * tourneys), winnerSurvive,
					(int) (10 * candidates));

		}

		BufferedWriter write = new BufferedWriter(new FileWriter("results.txt"));

		for (int i = 0; i < 297; i++) {
			write.append(Double.toString(population.row(0)[i]));
			if (i != population.row(0).length - 1)
				write.newLine();
		}
		write.close();

		return population.row(0);
	}

	private static long getFitness(NeuralAgent agent) throws Exception {
		long startTime = System.currentTimeMillis();
		int result = Controller.doBattleNoGui(new ReflexAgent(), agent);
		long runTime = (new Date()).getTime() - startTime;
		if (result == 1) {
			return -500 + runTime;
		} else if (result == 0) {
			return 0;
		} else {
			return 500 - runTime;
		}
	}

	private static int mostSimilar(Matrix m, int index, int[] cands) {

		double leastVar = Double.MAX_VALUE;
		int leastVarIndex = -1;
		double[] a = m.row(index);

		for (int i = 0; i < cands.length; i++) {
			double currentVar = 0;
			for (int j = 0; j < a.length; j++) {
				currentVar += Math.sqrt(Math.pow(a[j] - m.row(cands[i])[j], 2));
			}
			if (currentVar < leastVar) {
				leastVar = currentVar;
				leastVarIndex = i;
			}
		}

		return leastVarIndex;
	}

	public static void main(String[] args) throws Exception {
		double[] w = evolveWeights();

		Controller.doBattle(new ReflexAgent(), new NeuralAgent(w));
	}
}
