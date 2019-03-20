// ----------------------------------------------------------------
// The contents of this file are distributed under the CC0 license.
// See http://creativecommons.org/publicdomain/zero/1.0/
// ----------------------------------------------------------------

import java.util.Random;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.File;
import javax.imageio.ImageIO;


abstract class Layer
{
	double[] activation;
	double[] error;

	static final int t_linear = 0;
	static final int t_tanh = 1;


	/// General-purpose constructor
	Layer(int outputs)
	{
		activation = new double[outputs];
		error = new double[outputs];
	}


	/// Copy constructor
	Layer(Layer that)
	{
		activation = Vec.copy(that.activation);
		error = Vec.copy(that.error);
	}


	/// Unmarshal from a JSON DOM
	Layer(Json n)
	{
		int units = (int)n.getLong("units");
		activation = new double[units];
		error = new double[units];
	}


	void computeError(double[] target)
	{
		if(target.length != activation.length)
			throw new IllegalArgumentException("size mismatch. " + Integer.toString(target.length) + " != " + Integer.toString(activation.length));
		for(int i = 0; i < activation.length; i++)
		{
			error[i] = target[i] - activation[i];
		}
	}


	int outputCount()
	{
		return activation.length;
	}


	static Layer unmarshal(Json n)
	{
		int t = (int)n.getLong("type");
		switch(t)
		{
			case t_linear: return new LayerLinear(n);
			case t_tanh: return new LayerTanh(n);
			default: throw new RuntimeException("Unrecognized type");
		}
	}


	protected abstract Layer clone();
	abstract Json marshal();
	abstract int type();
	abstract int inputCount();
	abstract void initWeights(Random r);
	abstract double[] forwardProp(double[] in);
	abstract void backProp(Layer upStream);
	abstract void scaleGradient(double momentum);
	abstract void updateGradient(double[] in);
	abstract void step(double stepSize);
	abstract int countWeights();
	abstract int setWeights(double[] w, int start);
	abstract void regularizeWeights(double lambda);
}



class LayerLinear extends Layer
{
	Matrix weights; // rows are inputs, cols are outputs
	Matrix weightsGrad;
	double[] bias;
	double[] biasGrad;


	/// General-purpose constructor
	LayerLinear(int inputs, int outputs)
	{
		super(outputs);
		weights = new Matrix();
		weights.setSize(inputs, outputs);
		weightsGrad = new Matrix();
		weightsGrad.setSize(inputs, outputs);
		bias = new double[outputs];
		biasGrad = new double[outputs];
	}


	/// Copy constructor
	LayerLinear(LayerLinear that)
	{
		super(that);
		weights = new Matrix(that.weights);
		weightsGrad = new Matrix(that.weightsGrad);
		bias = Vec.copy(that.bias);
		biasGrad = Vec.copy(that.biasGrad);
		weightsGrad = new Matrix();
		weightsGrad.setSize(weights.rows(), weights.cols());
		weightsGrad.setAll(0.0);
		biasGrad = new double[weights.cols()];
		Vec.setAll(biasGrad, 0.0);
	}


	/// Unmarshal from a JSON DOM
	LayerLinear(Json n)
	{
		super(n);
		weights = new Matrix(n.get("weights"));
		bias = Vec.unmarshal(n.get("bias"));
	}


	protected LayerLinear clone()
	{
		return new LayerLinear(this);
	}


	/// Marshal into a JSON DOM
	Json marshal()
	{
		Json ob = Json.newObject();
		ob.add("units", (long)outputCount()); // required in all layers
		ob.add("weights", weights.marshal());
		ob.add("bias", Vec.marshal(bias));
		return ob;
	}


	void copy(LayerLinear src)
	{
		if(src.weights.rows() != weights.rows() || src.weights.cols() != weights.cols())
			throw new IllegalArgumentException("mismatching sizes");
		weights.copyBlock(0, 0, src.weights, 0, 0, src.weights.rows(), src.weights.cols());
		for(int i = 0; i < bias.length; i++)
		{
			bias[i] = src.bias[i];
		}
	}


	int type() { return t_linear; }
	int inputCount() { return weights.rows(); }


	void initWeights(Random r)
	{
		double dev = Math.max(0.3, 1.0 / weights.rows());
		for(int i = 0; i < weights.rows(); i++)
		{
			double[] row = weights.row(i);
			for(int j = 0; j < weights.cols(); j++)
			{
				row[j] = dev * r.nextGaussian();
			}
		}
		for(int j = 0; j < weights.cols(); j++) {
			bias[j] = dev * r.nextGaussian();
		}
		weightsGrad.setAll(0.0);
		Vec.setAll(biasGrad, 0.0);
	}


	int countWeights()
	{
		return weights.rows() * weights.cols() + bias.length;
	}


	int setWeights(double[] w, int start)
	{
		int oldStart = start;
		for(int i = 0; i < bias.length; i++)
			bias[i] = w[start++];
		for(int i = 0; i < weights.rows(); i++)
		{
			double[] row = weights.row(i);
			for(int j = 0; j < weights.cols(); j++)
				row[j] = w[start++];
		}
		return start - oldStart;
	}


	double[] forwardProp(double[] in)
	{
		if(in.length != weights.rows())
			throw new IllegalArgumentException("size mismatch. " + Integer.toString(in.length) + " != " + Integer.toString(weights.rows()));
		for(int i = 0; i < activation.length; i++)
			activation[i] = bias[i];
		for(int j = 0; j < weights.rows(); j++)
		{
			double v = in[j];
			double[] w = weights.row(j);
			for(int i = 0; i < weights.cols(); i++)
				activation[i] += v * w[i];
		}
		return activation;
	}


	double[] forwardProp2(double[] in1, double[] in2)
	{
		if(in1.length + in2.length != weights.rows())
			throw new IllegalArgumentException("size mismatch. " + Integer.toString(in1.length) + " + " + Integer.toString(in2.length) + " != " + Integer.toString(weights.rows()));
		for(int i = 0; i < activation.length; i++)
			activation[i] = bias[i];
		for(int j = 0; j < in1.length; j++)
		{
			double v = in1[j];
			double[] w = weights.row(j);
			for(int i = 0; i < weights.cols(); i++)
				activation[i] += v * w[i];
		}
		for(int j = 0; j < in2.length; j++)
		{
			double v = in2[j];
			double[] w = weights.row(in1.length + j);
			for(int i = 0; i < weights.cols(); i++)
				activation[i] += v * w[i];
		}
		return activation;
	}


	void backProp(Layer upStream)
	{
		if(upStream.outputCount() != weights.rows())
			throw new IllegalArgumentException("size mismatch");
		for(int j = 0; j < weights.rows(); j++)
		{
			double[] w = weights.row(j);
			double d = 0.0;
			for(int i = 0; i < weights.cols(); i++)
			{
				d += error[i] * w[i];
			}
			upStream.error[j] = d;
		}
	}


	void refineInputs(double[] inputs, double learningRate)
	{
		if(inputs.length != weights.rows())
			throw new IllegalArgumentException("size mismatch");
		for(int j = 0; j < weights.rows(); j++)
		{
			double[] w = weights.row(j);
			double d = 0.0;
			for(int i = 0; i < weights.cols(); i++)
			{
				d += error[i] * w[i];
			}
			inputs[j] += learningRate * d;
		}
	}


	void scaleGradient(double momentum)
	{
		weightsGrad.scale(momentum);
		Vec.scale(biasGrad, momentum);
	}


	void updateGradient(double[] in)
	{
		for(int i = 0; i < bias.length; i++)
		{
			biasGrad[i] += error[i];
		}
		for(int j = 0; j < weights.rows(); j++)
		{
			double[] w = weightsGrad.row(j);
			double x = in[j];
			for(int i = 0; i < weights.cols(); i++)
			{
				w[i] += x * error[i];
			}
		}
	}


	void step(double stepSize)
	{
		weights.addScaled(weightsGrad, stepSize);
		Vec.addScaled(bias, biasGrad, stepSize);
	}


	// Applies both L2 and L1 regularization to the weights and bias values
	void regularizeWeights(double lambda)
	{
		for(int i = 0; i < weights.rows(); i++)
		{
			double[] row = weights.row(i);
			for(int j = 0; j < row.length; j++)
			{
				row[j] *= (1.0 - lambda);
				if(row[j] < 0.0)
					row[j] += lambda;
				else
					row[j] -= lambda;
			}
		}
		for(int j = 0; j < bias.length; j++)
		{
			bias[j] *= (1.0 - lambda);
			if(bias[j] < 0.0)
				bias[j] += lambda;
			else
				bias[j] -= lambda;
		}
	}
}





class LayerTanh extends Layer
{
	/// General-purpose constructor
	LayerTanh(int nodes)
	{
		super(nodes);
	}


	/// Copy constructor
	LayerTanh(LayerTanh that)
	{
		super(that);
	}


	/// Unmarshal from a JSON DOM
	LayerTanh(Json n)
	{
		super(n);
	}


	protected LayerTanh clone()
	{
		return new LayerTanh(this);
	}


	/// Marshal into a JSON DOM
	Json marshal()
	{
		Json ob = Json.newObject();
		ob.add("units", (long)outputCount()); // required in all layers
		return ob;
	}


	void copy(LayerTanh src)
	{
	}


	int type() { return t_tanh; }
	int inputCount() { return activation.length; }


	void initWeights(Random r)
	{
	}


	int countWeights()
	{
		return 0;
	}


	int setWeights(double[] w, int start)
	{
		return 0;
	}


	double[] forwardProp(double[] in)
	{
		if(in.length != outputCount())
			throw new IllegalArgumentException("size mismatch. " + Integer.toString(in.length) + " != " + Integer.toString(outputCount()));
		for(int i = 0; i < activation.length; i++)
		{
			activation[i] = Math.tanh(in[i]);
		}
		return activation;
	}


	void backProp(Layer upStream)
	{
		if(upStream.outputCount() != outputCount())
			throw new IllegalArgumentException("size mismatch");
		for(int i = 0; i < activation.length; i++)
		{
			upStream.error[i] = error[i] * (1.0 - activation[i] * activation[i]);
		}
	}


	void scaleGradient(double momentum)
	{
	}


	void updateGradient(double[] in)
	{
	}


	void step(double stepSize)
	{
	}


	// Applies both L2 and L1 regularization to the weights and bias values
	void regularizeWeights(double lambda)
	{
	}
}





public class NeuralNet
{
	public ArrayList<Layer> layers;


	/// General-purpose constructor. (Starts with no layers. You must add at least one.)
	NeuralNet()
	{
		layers = new ArrayList<Layer>();
	}


	/// Copy constructor
	NeuralNet(NeuralNet that)
	{
		layers = new ArrayList<Layer>();
		for(int i = 0; i < that.layers.size(); i++)
		{
			layers.add(that.layers.get(i).clone());
		}
	}


	/// Unmarshals from a JSON DOM.
	NeuralNet(Json n)
	{
		layers = new ArrayList<Layer>();
		Json l = n.get("layers");
		for(int i = 0; i < l.size(); i++)
			layers.add(Layer.unmarshal(l.get(i)));
	}


	/// Marshal this neural network into a JSON DOM.
	Json marshal()
	{
		Json ob = Json.newObject();
		Json l = Json.newList();
		ob.add("layers", l);
		for(int i = 0; i < layers.size(); i++)
			l.add(layers.get(i).marshal());
		return ob;
	}


	/// Initializes the weights and biases with small random values
	void init(Random r)
	{
		for(int i = 0; i < layers.size(); i++)
		{
			layers.get(i).initWeights(r);
		}
	}


	/// Feeds "in" into this neural network and propagates it forward to compute predicted outputs.
	double[] forwardProp(double[] in)
	{
		for(int i = 0; i < layers.size(); i++)
		{
			in = layers.get(i).forwardProp(in);
		}
		return in;
	}


	/// Feeds the concatenation of "in1" and "in2" into this neural network and propagates it forward to compute predicted outputs.
	double[] forwardProp2(double[] in1, double[] in2)
	{
		double[] in = ((LayerLinear)layers.get(0)).forwardProp2(in1, in2);
		for(int i = 1; i < layers.size(); i++)
		{
			in = layers.get(i).forwardProp(in);
		}
		return in;
	}


	/// Backpropagates the error to the upstream layer.
	void backProp(double[] target)
	{
		int i = layers.size() - 1;
		Layer l = layers.get(i);
		l.computeError(target);
		for(i--; i >= 0; i--)
		{
			Layer upstream = layers.get(i);
			l.backProp(upstream);
			l = upstream;
		}
	}


	/// Backpropagates the error from another neural network. (This is used when training autoencoders.)
	void backPropFromDecoder(NeuralNet decoder)
	{
		int i = layers.size() - 1;
		Layer l = decoder.layers.get(0);
		Layer upstream = layers.get(i);
		l.backProp(upstream);
		l = upstream;
		for(i--; i >= 0; i--)
		{
			upstream = layers.get(i);
			l.backProp(upstream);
			l = upstream;
		}
	}


	/// Updates the weights and biases
	void descendGradient(double[] in, double learningRate)
	{
		for(int i = 0; i < layers.size(); i++)
		{
			Layer l = layers.get(i);
			l.scaleGradient(0.0);
			l.updateGradient(in);
			l.step(learningRate);
			in = l.activation;
		}
	}


	/// Keeps the weights and biases from getting too big
	void regularize(double amount)
	{
		for(int i = 0; i < layers.size(); i++)
		{
			Layer lay = layers.get(i);
			lay.regularizeWeights(amount);
		}
	}


	/// Refines the weights and biases with on iteration of stochastic gradient descent.
	void trainIncremental(double[] in, double[] target, double learningRate)
	{
		forwardProp(in);
		backProp(target);
		//backPropAndBendHinge(target, learningRate);
		descendGradient(in, learningRate);
	}


	/// Refines "in" with one iteration of stochastic gradient descent.
	void refineInputs(double[] in, double[] target, double learningRate)
	{
		forwardProp(in);
		backProp(target);
		((LayerLinear)layers.get(0)).refineInputs(in, learningRate);
	}


	static void testMath()
	{
		NeuralNet nn = new NeuralNet();
		LayerLinear l1 = new LayerLinear(2, 3);
		l1.weights.row(0)[0] = 0.1;
		l1.weights.row(0)[1] = 0.0;
		l1.weights.row(0)[2] = 0.1;
		l1.weights.row(1)[0] = 0.1;
		l1.weights.row(1)[1] = 0.0;
		l1.weights.row(1)[2] = -0.1;
		l1.bias[0] = 0.1;
		l1.bias[1] = 0.1;
		l1.bias[2] = 0.0;
		nn.layers.add(l1);
		nn.layers.add(new LayerTanh(3));

		LayerLinear l2 = new LayerLinear(3, 2);
		l2.weights.row(0)[0] = 0.1;
		l2.weights.row(0)[1] = 0.1;
		l2.weights.row(1)[0] = 0.1;
		l2.weights.row(1)[1] = 0.3;
		l2.weights.row(2)[0] = 0.1;
		l2.weights.row(2)[1] = -0.1;
		l2.bias[0] = 0.1;
		l2.bias[1] = -0.2;
		nn.layers.add(l2);
		nn.layers.add(new LayerTanh(2));

		System.out.println("l1 weights:" + l1.weights.toString());
		System.out.println("l1 bias:" + Vec.toString(l1.bias));
		System.out.println("l2 weights:" + l2.weights.toString());
		System.out.println("l2 bias:" + Vec.toString(l2.bias));

		System.out.println("----Forward prop");
		double in[] = new double[2];
		in[0] = 0.3;
		in[1] = -0.2;
		double[] out = nn.forwardProp(in);
		System.out.println("activation:" + Vec.toString(out));

		System.out.println("----Back prop");
		double targ[] = new double[2];
		targ[0] = 0.1;
		targ[1] = 0.0;
		nn.backProp(targ);
		System.out.println("error 2:" + Vec.toString(l2.error));
		System.out.println("error 1:" + Vec.toString(l1.error));
		
		nn.descendGradient(in, 0.1);
		System.out.println("----Descending gradient");
		System.out.println("l1 weights:" + l1.weights.toString());
		System.out.println("l1 bias:" + Vec.toString(l1.bias));
		System.out.println("l2 weights:" + l2.weights.toString());
		System.out.println("l2 bias:" + Vec.toString(l2.bias));

		if(Math.abs(l1.weights.row(0)[0] - 0.10039573704287) > 0.0000000001)
			throw new IllegalArgumentException("failed");
		if(Math.abs(l1.weights.row(0)[1] - 0.0013373814241446) > 0.0000000001)
			throw new IllegalArgumentException("failed");
		if(Math.abs(l1.bias[1] - 0.10445793808048) > 0.0000000001)
			throw new IllegalArgumentException("failed");
		System.out.println("passed");
	}
}
