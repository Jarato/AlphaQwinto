package game.qwplayer.dev;

import java.util.ArrayList;
import java.util.Random;

import game.QwinPaper;
import model.FeedForwardNetwork;
import model.functions.Activation;
import pdf.util.Pair;

public class QwinPlayerLA_NNEval extends QwinPlayerLookahead_t {

	private FeedForwardNetwork scoreEvalNetwork;
	
	
	private ArrayList<Pair<double[], Integer>> paperScoreHistory = new ArrayList<Pair<double[], Integer>>();
	
	public QwinPlayerLA_NNEval(Random rnd) {
		super(rnd);
		initNet();
	}
	
	@Override
	public void recordData() {
		double[] array = new double[28];
		fillInputVector(paper, array);
		paperScoreHistory.add(new Pair<double[], Integer>(array, -100));
	}
	
	@Override
	public void gameEndWrapUp() {
		fillHistoryScoreData(paper.calculateScore());
	}
	
	@Override
	public void fillHistoryScoreData(Integer score) {
		for (Pair<double[], Integer> pair : paperScoreHistory) {
			if (pair.getY() == -100) pair.setY(score);
		}
	}

	private void initNet() {
		scoreEvalNetwork = new FeedForwardNetwork();
		scoreEvalNetwork.addBlock(28, 200, true, Activation.SIGMOID);     
		scoreEvalNetwork.addBlock(200, 200, true, Activation.RELU);
		scoreEvalNetwork.addBlock(200, 1, true,  Activation.NONE);
		scoreEvalNetwork.setAllWeightsRandom(rnd, 1);
	}
	
	public FeedForwardNetwork getEvalNetwork() {
		return scoreEvalNetwork;
	}
	
	private void fillInputVector(QwinPaper paper, double[] input) {
		int[] redline = paper.getRedLine();
		int[] yellowline = paper.getYellowLine();
		int[] purpleline = paper.getPurpleLine();
		for (int i = 0; i < 9; i++) {
			input[i] = redline[i]/18.0;
			input[i+9] = yellowline[i]/18.0;
			input[i+18] = purpleline[i]/18.0;
		}
		input[27] = paper.getNumberOfMisthrows()*1./4.;
	}
	
	@Override
	protected double evaluatePaper(QwinPaper paper) {
		if (paper.isEndCondition()) return paper.calculateScore();
		double[] input = new double[28];
		fillInputVector(paper, input);
		scoreEvalNetwork.prozessInput(input);
		return scoreEvalNetwork.getLastOutputVector()[0];
	}

	@Override
	public String getName() {
		return "LA.N.N.Eval";
	}

}
