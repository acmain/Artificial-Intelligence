import java.util.ArrayList;
import java.util.Arrays;

public class RandomForest extends SupervisedLearner {

	int numTrees;
	ArrayList<DecisionTree> trees;

	RandomForest(int numTrees) {
		this.numTrees = numTrees;
		trees = new ArrayList<DecisionTree>();
	}

	@Override
	String name() {
		return "RandomForest";
	}

	@Override
	void train(Matrix features, Matrix labels) {
		// add original training data
		trees.add(new DecisionTree());
		trees.get(0).train(features, labels);

		// sample with replacement for the rest
		for (int i = 0; i < numTrees; i++) {
			Matrix newFeat = new Matrix(features);
			Matrix newLab = new Matrix(labels);
			
			while (newFeat.rows() != features.rows()) {
				int index = DecisionTree.rand.nextInt(features.rows());
				newFeat.takeRow(features.row(index));
				newLab.takeRow(labels.row(index));
			}

			trees.add(new DecisionTree());
			trees.get(i).train(newFeat, newLab);
		}

	}

	@Override
	void predict(double[] in, double[] out) {
		double[] outs = new double[numTrees + 1];
		for (int i = 0; i < numTrees; i++) {
			double[] outTemp = new double[1];
			trees.get(i).predict(in, outTemp);
			outs[i] = outTemp[0];
		}

		Arrays.sort(outs);

		double val = outs[0];
		double mostVal = Integer.MIN_VALUE;
		int length = 0;
		int maxLen = Integer.MIN_VALUE;
		for (int i = 0; i < outs.length; i++) {
			if (outs[i] != val || i == outs.length - 1) {
				if (length > maxLen) {
					maxLen = length;
					mostVal = val;
				}
				val = outs[i];
				length = 1;
			} else {
				length++;
			}
		}

		out[0] = mostVal;
	}

}
