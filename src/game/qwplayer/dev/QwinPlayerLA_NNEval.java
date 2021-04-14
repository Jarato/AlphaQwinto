package game.qwplayer.dev;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import game.QwinPaper;
import model.FeedForwardNetwork;
import model.functions.Activation;
import pdf.util.Pair;
import pdf.util.UtilMethods;

public class QwinPlayerLA_NNEval extends QwinPlayerLookahead_t {
	private FeedForwardNetwork scoreEvalNetwork;
	private String weights_file = "";
	private ArrayList<Pair<double[], Double>> paperScoreHistory = new ArrayList<Pair<double[], Double>>();
	private int version;

	public QwinPlayerLA_NNEval(Random rnd, int version) {
		super(rnd);
		this.version = version;
		initNet(version);
	}

	public QwinPlayerLA_NNEval(Random rnd, int version, String weights_file_path) {
		this(rnd, version);
		weights_file = weights_file_path;
		double[] weights = read_weights_data(weights_file_path);
		scoreEvalNetwork.applyWeightsBiasesVector(weights);
	}

	@Override
	public void turnEndWrapUp() {
		super.turnEndWrapUp();
		recordData();
	}

	private double[] read_weights_data(String weights_file) {
		double[] weightsVector = new double[scoreEvalNetwork.getNumberWeightsBiases()];
		File weightsF = new File(weights_file);
		try {
			BufferedReader br = new BufferedReader(new FileReader(weightsF));
			String line;
			// i indicates the position in the weights vector that we're writing
			int i = 0;
			while ((line = br.readLine()) != null) {
				weightsVector[i] = Double.parseDouble(line);
				i++;
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return weightsVector;
	}

	public void reset() {
		super.reset(this.rnd);
		paperScoreHistory.clear();
	}

	private void recordData() {
		double[] array = new double[scoreEvalNetwork.getInputLength()];
		fillInputVector(paper, array);
		paperScoreHistory.add(new Pair<double[], Double>(array, -20.));
	}

	@Override
	public void matchEndWrapUp(QwinPaper[] allPapers) {
		super.matchEndWrapUp(allPapers);
		/*
		 * int min = Integer.MAX_VALUE; int max = Integer.MIN_VALUE; for (int i = 0; i <
		 * allPapers.length; i++) { int score = allPapers[i].calculateScore(); if (score
		 * > max) { max = score; } if (score < min) { min = score; } } double
		 * predictValue = 0; if (max == min) { predictValue = 0.5; } else { predictValue
		 * = (paper.calculateScore()-min)/(double)(max-min); }
		 */
		fillHistoryScoreData((double) this.paper.calculateScore());
	}

	public void fillHistoryScoreData(Double score) {
		for (Pair<double[], Double> pair : paperScoreHistory) {
			if (pair.getY() == -20.)
				pair.setY(score);
		}
	}

	public ArrayList<Pair<double[], Double>> getPaperScoreHistory() {
		return paperScoreHistory;
	}

	// init
	private void initNet(int version) {
		if (version == 10)
			initNet10();
		if (version == 11)
			initNet11();
		if (version == 12)
			initNet12();
		if (version == 13)
			initNet13();
	}

	// fill
	private void fillInputVector(QwinPaper paper, double[] input) {
		if (version == 10)
			fillInputVector10(paper, input);
		if (version == 11)
			fillInputVector11(paper, input);
		if (version == 12 || version == 13)
			fillInputVector12(paper, input);
	}

	// 10 init
	private void initNet10() {
		scoreEvalNetwork = new FeedForwardNetwork();
		scoreEvalNetwork.addBlock(488, 30, true, Activation.TANH);
		scoreEvalNetwork.addBlock(30, 1, true, Activation.NONE);
		scoreEvalNetwork.setAllWeightsRandom(rnd, 1);
	}

	// 10 fill
	private void fillInputVector10(QwinPaper paper, double[] input) {
		int[] redline = paper.getRedLine();
		int[] yellowline = paper.getYellowLine();
		int[] purpleline = paper.getPurpleLine();
		for (int i = 0; i < 9; i++) {
			if (redline[i] != 0) {
				input[i * 18 + redline[i] - 1] = 1;
			}
			if (yellowline[i] != 0) {
				input[18 * (9 + i) + yellowline[i] - 1] = 1;
			}
			if (purpleline[i] != 0) {
				input[18 * (18 + i) + purpleline[i] - 1] = 1;
			}
		}
		input[486] = paper.getNumberOfMisthrows() / 4.;
		input[487] = numOfTurns / (15. + Math.abs(numOfTurns));
	}

	// 11 init
	private void initNet11() {
		scoreEvalNetwork = new FeedForwardNetwork();
		scoreEvalNetwork.addBlock(488, 50, true, Activation.TANH);
		scoreEvalNetwork.addBlock(50, 1, true, Activation.NONE);
		scoreEvalNetwork.setAllWeightsRandom(rnd, 1);
	}

	// 11 fill
	private void fillInputVector11(QwinPaper paper, double[] input) {
		int[] redline = paper.getRedLine();
		int[] yellowline = paper.getYellowLine();
		int[] purpleline = paper.getPurpleLine();
		for (int i = 0; i < 9; i++) {
			if (redline[i] != 0) {
				input[i * 18 + redline[i] - 1] = 1;
			}
			if (yellowline[i] != 0) {
				input[18 * (9 + i) + yellowline[i] - 1] = 1;
			}
			if (purpleline[i] != 0) {
				input[18 * (18 + i) + purpleline[i] - 1] = 1;
			}
		}
		input[486] = paper.getNumberOfMisthrows() / 4.;
		input[487] = Math.tanh(numOfTurns / 25.);
	}

	// 12 init
	private void initNet12() {
		scoreEvalNetwork = new FeedForwardNetwork();
		scoreEvalNetwork.addBlock(488, 60, true, Activation.TANH);
		scoreEvalNetwork.addBlock(60, 60, true, Activation.ELU);
		scoreEvalNetwork.addBlock(60, 1, true, Activation.NONE);
		scoreEvalNetwork.setAllWeightsRandom(rnd, 1);
	}

	// 12 fill
	private void fillInputVector12(QwinPaper paper, double[] input) {
		fillInputVector11(paper, input);
		input[487] = Math.tanh(numOfTurns / 20.);
	}

	// 12 init
	private void initNet13() {
		scoreEvalNetwork = new FeedForwardNetwork();
		scoreEvalNetwork.addBlock(488, 100, true, Activation.ELU);
		scoreEvalNetwork.addBlock(100, 100, true, Activation.ELU);
		scoreEvalNetwork.addBlock(100, 100, true, Activation.ELU);
		scoreEvalNetwork.addBlock(100, 1, true, Activation.NONE);
		scoreEvalNetwork.setAllWeightsRandom(rnd, 1);
	}

	// fill input vector for
	/**
	 * 0 r1 1 r2 2 r3 3 r4 4 r5 5 r6 6 r7 7 r8 8 r9 9 y1 10 y2 11 y3 12 y4 13 y5 14
	 * y6 15 y7 16 y8 17 y9 18 p1 19 p2 20 p3 21 p4 22 p5 23 p6 24 p7 25 p8 26 p9 27
	 * mis 28 rou
	 * 
	 * 
	 * @param paper
	 * @param input
	 */
	public FeedForwardNetwork getEvalNetwork() {
		return scoreEvalNetwork;
	}

	public void setEvalNetwork(FeedForwardNetwork network) {
		scoreEvalNetwork = network;
	}

	@Override
	public double evaluatePaper(QwinPaper paper) {
		// if (paper.isEndCondition())
		// return paper.calculateScore();
		double[] input = new double[scoreEvalNetwork.getInputLength()];
		fillInputVector(paper, input);
		scoreEvalNetwork.prozessInput(input);
		double ret = scoreEvalNetwork.getLastOutputVector()[0];
		return ret;
	}

	@Override
	public String getName() {
		return "LANNEVAL" + version + "(" + UtilMethods.roundTo(noiselevel, 2) + "," + weights_file + ")";
	}

}
