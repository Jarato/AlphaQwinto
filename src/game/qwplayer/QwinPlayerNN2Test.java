package game.qwplayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import game.DiceRoll;
import game.qwplayer.dev.QwinPlayer_t;
import model.FeedForwardNetwork;
import model.functions.AF_ReLU;
import model.functions.AF_Sigmoid;
import model.functions.Activation;
import pdf.util.Pair;

public class QwinPlayerNN2Test extends QwinPlayer_t {
	private FeedForwardNetwork diceThrowNet;
	private FeedForwardNetwork actionListNet;

	public QwinPlayerNN2Test(Random rnd) {
		super(rnd);
		initNets();
		double[] weightsDT = new double[diceThrowNet.getNumberWeightsBiases()];
		double[] weightsAL = new double[actionListNet.getNumberWeightsBiases()];
		File geneFile = new File("NN2TestWeights.txt");
		try {
			BufferedReader br = new BufferedReader(new FileReader(geneFile));
			String line;
			int i = 0;
			while ((line = br.readLine()) != null) {
				if (i < weightsDT.length) {
					weightsDT[i] = new Double(line);
				} else {
					weightsAL[i-weightsDT.length] = new Double(line);
				}
				i++;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		diceThrowNet.applyWeightsBiasesVector(weightsDT);
		actionListNet.applyWeightsBiasesVector(weightsAL);
	}

	private void initNets() {
		diceThrowNet = new FeedForwardNetwork();
		diceThrowNet.addBlock(55, 100, false, Activation.TANH);
		diceThrowNet.addBlock(100, 7, false, Activation.SIGMOID);
		diceThrowNet.setAllWeightsRandom(rnd, 1);
		actionListNet = new FeedForwardNetwork();
		actionListNet.addBlock(76, 100, false, Activation.TANH);
		actionListNet.addBlock(100, 28, false, Activation.SIGMOID);
		actionListNet.setAllWeightsRandom(rnd, 1);
	}

	public FeedForwardNetwork getDiceThrowNet() {
		return diceThrowNet;
	}

	public void setDiceThrowNet(FeedForwardNetwork setDiceThrowNet) {
		diceThrowNet = setDiceThrowNet;
	}

	public FeedForwardNetwork getActionListNet() {
		return actionListNet;
	}

	public void setActionListNet(FeedForwardNetwork setActionListNet) {
		actionListNet = setActionListNet;
	}

	private void fillWithDiceThrowNetFeatures(double[] vector) {
		int[] redline = paper.getRedLine();
		int[] yellowline = paper.getYellowLine();
		int[] purpleline = paper.getPurpleLine();
		for (int i = 0; i < 9; i++) {
			vector[i] = redline[i] / 9.0 - 1;
			vector[i + 9] = yellowline[i] / 9.0 - 1;
			vector[i + 18] = purpleline[i] / 9.0 - 1;
			vector[i + 28] = (redline[i] == 0 ? 1 : -1);
			vector[i + 37] = (yellowline[i] == 0 ? 1 : -1);
			vector[i + 46] = (purpleline[i] == 0 ? 1 : -1);
		}
		vector[27] = paper.getNumberOfMisthrows() * 2.0 / 3.0 - 1;
	}

	private void fillWithActionListNetFeatures(double[] vector, int thrownNumber, DiceRoll thrown) {
		fillWithDiceThrowNetFeatures(vector);
		vector[55] = (thrown.red ? 1 : -1);
		vector[56] = (thrown.yellow ? 1 : -1);
		vector[57] = (thrown.purple ? 1 : -1);
		vector[57 + thrownNumber] = 1;
	}

	@Override
	public DiceRoll getDiceThrow() {
		double[] input = new double[55];
		fillWithDiceThrowNetFeatures(input);
		diceThrowNet.prozessInput(input);
		double[] res = diceThrowNet.getLastOutputVector();
		int bestIndex = 0;
		double maxValue = res[0];
		for (int i = 1; i < res.length; i++) {
			if (res[i] > maxValue) {
				maxValue = res[i];
				bestIndex = i;
			}
		}
		return DiceRoll.flagToDiceThrow(bestIndex);
	}

	@Override
	public int[] getActionFlagList(int diceNumber, DiceRoll thrown) {
		double[] input = new double[76];
		fillWithActionListNetFeatures(input, diceNumber, thrown);
		actionListNet.prozessInput(input);
		double[] res = actionListNet.getLastOutputVector();
		int[] priolist = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27 };
		for (int i = 0; i < res.length - 1; i++) {
			double max = res[i];
			int maxindex = i;
			for (int j = i + 1; j < res.length; j++) {
				if (res[j] > max) {
					max = res[j];
					maxindex = j;
				}
			}
			if (i != maxindex) {
				double resT = res[maxindex];
				res[maxindex] = res[i];
				res[i] = resT;
				int indexT = priolist[maxindex];
				priolist[maxindex] = priolist[i];
				priolist[i] = indexT;
			}
		}
		return priolist;
	}

	@Override
	public String getName() {
		return "Neural Network 2";
	}

}
