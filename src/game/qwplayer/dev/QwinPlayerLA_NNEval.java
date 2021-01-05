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
	private boolean print;
	
	private ArrayList<Pair<double[], Integer>> paperScoreHistory = new ArrayList<Pair<double[], Integer>>();
	
	public QwinPlayerLA_NNEval(Random rnd) {
		super(rnd);
		initNet();
		print = false;
	}
	
	public QwinPlayerLA_NNEval(Random rnd, String weights_file_path, boolean print) {
		this(rnd);
		this.print = print;
		double[] weights = read_weights_data(weights_file_path);
		scoreEvalNetwork.applyWeightsBiasesVector(weights);
	}
	
	public void roundEndWrapUp(boolean print) {
		if (print) System.out.println("Evaluation:\t"+evaluatePaper(this.paper));
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
	
	@Override
	public void recordData() {
		double[] array = new double[scoreEvalNetwork.getInputLength()];
		fillInputVector(paper, array);
		paperScoreHistory.add(new Pair<double[], Integer>(array, -100));
	}
	
	@Override
	public void gameEndWrapUp(boolean print) {
		fillHistoryScoreData(paper.calculateScore());
	}
	
	@Override
	public void fillHistoryScoreData(Integer score) {
		for (Pair<double[], Integer> pair : paperScoreHistory) {
			if (pair.getY() == -100) pair.setY(score);
		}
	}
	
	public ArrayList<Pair<double[], Integer>> getPaperScoreHistory(){
		return paperScoreHistory;
	}

	private void initNet2() {
		scoreEvalNetwork = new FeedForwardNetwork();
		scoreEvalNetwork.addBlock(487, 150, true, Activation.TANH);     
		scoreEvalNetwork.addBlock(150, 150, true, Activation.TANH);
		scoreEvalNetwork.addBlock(150, 1, true,  Activation.NONE);
		scoreEvalNetwork.setAllWeightsRandom(rnd, 1);
	}
	
	private void initNet() {
		scoreEvalNetwork = new FeedForwardNetwork();
		scoreEvalNetwork.addBlock(488, 150, true, Activation.TANH);     
		scoreEvalNetwork.addBlock(150, 150, true, Activation.TANH);
		scoreEvalNetwork.addBlock(150, 1, true,  Activation.NONE);
		scoreEvalNetwork.setAllWeightsRandom(rnd, 1);
	}
	
	
	private void fillInputVector2_3(QwinPaper paper, double[] input) {
		int[] redline = paper.getRedLine();
		int[] yellowline = paper.getYellowLine();
		int[] purpleline = paper.getPurpleLine();
		for (int i = 0; i < 9; i++) {
			if (redline[i] != 0) input[i*18+redline[i]-1] = 1;
			if (yellowline[i] != 0) input[18*(9+i)+yellowline[i]-1] = 1;
			if (purpleline[i] != 0) input[18*(18+i)+purpleline[i]-1] = 1;
		}
		input[486] = paper.getNumberOfMisthrows()*1./3.;
	}
	
	
	private void fillInputVector(QwinPaper paper, double[] input) {
		int[] redline = paper.getRedLine();
		int[] yellowline = paper.getYellowLine();
		int[] purpleline = paper.getPurpleLine();
		for (int i = 0; i < 9; i++) {
			if (redline[i] != 0) input[i*18+redline[i]-1] = 1;
			if (yellowline[i] != 0) input[18*(9+i)+yellowline[i]-1] = 1;
			if (purpleline[i] != 0) input[18*(18+i)+purpleline[i]-1] = 1;
		}
		input[486] = paper.getNumberOfMisthrows()*1./3.;
		input[487] = (numOfRounds==0?0:numOfRounds/(1.+Math.abs(numOfRounds)));
	}
	
	private void initNet1() {
		scoreEvalNetwork = new FeedForwardNetwork();
		scoreEvalNetwork.addBlock(28, 200, true, Activation.TANH);     
		scoreEvalNetwork.addBlock(200, 200, true, Activation.TANH);
		scoreEvalNetwork.addBlock(200, 1, true,  Activation.NONE);
		scoreEvalNetwork.setAllWeightsRandom(rnd, 1);
	}
	
	public FeedForwardNetwork getEvalNetwork() {
		return scoreEvalNetwork;
	}
	
	public void setEvalNetwork(FeedForwardNetwork network) {
		scoreEvalNetwork = network;
	}
	
	private void fillInputVector1(QwinPaper paper, double[] input) {
		int[] redline = paper.getRedLine();
		int[] yellowline = paper.getYellowLine();
		int[] purpleline = paper.getPurpleLine();
		for (int i = 0; i < 9; i++) {
			input[i] = redline[i]/18.0;
			input[i+9] = yellowline[i]/18.0;
			input[i+18] = purpleline[i]/18.0;
		}
		input[27] = paper.getNumberOfMisthrows()*1./3.;
	}
	
	@Override
	protected double evaluatePaper(QwinPaper paper) {
		if (paper.isEndCondition()) return paper.calculateScore();
		double[] input = new double[scoreEvalNetwork.getInputLength()];
		fillInputVector(paper, input);
		scoreEvalNetwork.prozessInput(input);
		double ret = scoreEvalNetwork.getLastOutputVector()[0];
		if (Double.isNaN(ret)) throw new IllegalArgumentException();
		return ret;
	}

	@Override
	public String getName() {
		return "LANNEVAL";
	}

}
