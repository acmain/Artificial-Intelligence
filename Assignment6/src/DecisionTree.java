import java.util.Random;

abstract class Node {
	abstract boolean isLeaf();
}

class InteriorNode extends Node {
	int attribute; // which attribute to divide on
	double pivot; // which value to divide on
	boolean continuous; // if values are continuous
	Node a;
	Node b;

	InteriorNode(Node a, Node b, int attr, double pivot, boolean continuous) {
		this.a = a;
		this.b = b;
		this.attribute = attr;
		this.pivot = pivot;
		this.continuous = continuous;
	}

	boolean isLeaf() {
		return false;
	}
}

class LeafNode extends Node {
	double label;
	boolean continuous;

	LeafNode(Matrix lab) {
		if (lab.valueCount(0) == 0) {
			label = lab.columnMean(0);
			continuous = true;
		} else {
			label = lab.mostCommonValue(0);
		}
	}

	boolean isLeaf() {
		return true;
	}
}

class DecisionTree extends SupervisedLearner {
	Node root;
	static Random rand = new Random(101190);

	@Override
	String name() {
		return "DecisionTree";
	}

	@Override
	void train(Matrix features, Matrix labels) {
		root = buildTree(features, labels);
	}

	Node buildTree(Matrix features, Matrix labels) {
		if (features.rows() != labels.rows())
			throw new RuntimeException("mismatching data");

		Matrix feat, featA = null, featB = null, lab, labA = null, labB = null;
		int attr = 0;
		double pivot = 0;
		boolean continuous = false;
		for (int attempts = 0; attempts < 10; attempts++) {
			attr = rand.nextInt(features.cols());
			pivot = features.row(rand.nextInt(features.rows()))[attr];

			if (features.valueCount(attr) == 0)
				continuous = true;
			else
				continuous = false;

			feat = new Matrix(features);
			feat.copy(features);
			lab = new Matrix(labels);
			lab.copy(labels);
			featA = new Matrix(feat);
			featB = new Matrix(feat);
			labA = new Matrix(lab);
			labB = new Matrix(lab);

			while (feat.rows() != 0) {
				if (continuous) {
					if (feat.row(0)[attr] <= pivot) {
						featA.takeRow(feat.removeRow(0));
						labA.takeRow(lab.removeRow(0));
					} else {
						featB.takeRow(feat.removeRow(0));
						labB.takeRow(lab.removeRow(0));
					}
				} else {
					if (feat.row(0)[attr] == pivot) {
						featA.takeRow(feat.removeRow(0));
						labA.takeRow(lab.removeRow(0));
					} else {
						featB.takeRow(feat.removeRow(0));
						labB.takeRow(lab.removeRow(0));
					}
				}
			}
			if (featA.rows() != 0 && featB.rows() != 0) {
				break;
			}
		}

		if (featA.rows() == 0 || featB.rows() == 0) {
			if (featA.rows() == 0)
				return new LeafNode(labB);
			else
				return new LeafNode(labA);
		}

		Node a = buildTree(featA, labA);
		Node b = buildTree(featB, labB);
		return new InteriorNode(a, b, attr, pivot, continuous);

	}

	@Override
	void predict(double[] in, double[] out) {
		for (int i = 0; i < out.length; i++) {
			Node n = root;
			while (true) {
				if (n.isLeaf()) {
					out[i] = ((LeafNode) n).label;
					// System.out.println(out[i]);
					break;
				} else {
					if (((InteriorNode) n).continuous) {
						if (in[((InteriorNode) n).attribute] <= ((InteriorNode) n).pivot)
							n = ((InteriorNode) n).a;
						else
							n = ((InteriorNode) n).b;
					} else {
						if (in[((InteriorNode) n).attribute] == ((InteriorNode) n).pivot)
							n = ((InteriorNode) n).a;
						else
							n = ((InteriorNode) n).b;
					}
				}
			}
		}
	}

}