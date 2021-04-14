package game.experiments;

import java.util.ArrayList;
import java.util.Random;

import model.FeedForwardNetwork;
import pdf.util.Pair;
import pdf.util.UtilMethods;

public class GradientMultiThreader implements Runnable {
	private FeedForwardNetwork network;
	private double[][][] data;
	private int data_size;
	private double[] gradient;
	private double loss;

	public GradientMultiThreader(double[][][] inputOutputPairs) {
		loss = 0;
		network = null;
		data = inputOutputPairs;
		data_size = inputOutputPairs.length;
		gradient = null;
	}

	public void reset(FeedForwardNetwork updatedNetwork) {
		network = updatedNetwork;
		gradient = null;
		loss = 0;
	}

	public double[] getGradient() {
		return gradient;
	}

	public double getLoss() {
		return loss;
	}

	@Override
	public void run() {
		// go through all the datapoints
		for (int i = 0; i < data.length; i++) {
			double[][] instance = data[i];
			double[] derivOut = network.calculateDerivativeOutput(instance[0], instance[1]);
			loss += derivOut[0] * derivOut[0];
			network.calculateGradient(derivOut);
			// this is the gradient for this one datapoint
			double[] tempGradient = network.getWeightBiasGradient();
			// we add the gradients for all the datapoints in the gradient
			gradient = (gradient == null ? tempGradient : UtilMethods.vectorAddition(gradient, tempGradient));
		}
		loss /= (double)data_size;
		if (gradient != null) {
			for (int i = 0; i < gradient.length; i++) {
				gradient[i] /= data_size;
			}
		}
	}

}
