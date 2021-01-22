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

	private ArrayList<Pair<double[], Double>> paperScoreHistory = new ArrayList<Pair<double[], Double>>();

	public QwinPlayerLA_NNEval(Random rnd) {
		super(rnd);
		initNet();
	}

	public QwinPlayerLA_NNEval(Random rnd, String weights_file_path) {
		this(rnd);
		double[] weights = read_weights_data(weights_file_path);
		scoreEvalNetwork.applyWeightsBiasesVector(weights);
	}

	@Override
	public void turnEndWrapUp() {
		super.turnEndWrapUp();
		recordData();
		//System.out.println("Evaluation:\t" + evaluatePaper(this.paper));
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

	private void recordData() {
		double[] array = new double[scoreEvalNetwork.getInputLength()];
		fillInputVector(paper, array);
		paperScoreHistory.add(new Pair<double[], Double>(array, -20.));
	}

	@Override
	public void matchEndWrapUp(QwinPaper[] allPapers) {
		super.matchEndWrapUp(allPapers);
		/*int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < allPapers.length; i++) {
			int score = allPapers[i].calculateScore();
			if (score > max) {
				max = score;
			} 
			if (score < min) {
				min = score;
			}
		}
		double predictValue = 0;
		if (max == min) {
			predictValue = 0.5;
		} else {
			predictValue = (paper.calculateScore()-min)/(double)(max-min);
		}*/
		fillHistoryScoreData((double) this.paper.calculateScore());
		//if (print) {
		//	for (Pair<double[], Double> dat : paperScoreHistory) {
			//	System.out.println(UtilMethods.arrayToString(dat.getX()) + "\t" + dat.getY());
		//	}
		//	System.out.println(numOfTurns);
		//}
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

	private void initNet6() {
		scoreEvalNetwork = new FeedForwardNetwork();
		scoreEvalNetwork.addBlock(488, 100, true, Activation.TANH);
		scoreEvalNetwork.addBlock(100, 100, true, Activation.TANH);
		scoreEvalNetwork.addBlock(100, 1, true, Activation.SIGMOID);
		scoreEvalNetwork.setAllWeightsRandom(rnd, 1);
	}
	
	private void initNet7() {
		scoreEvalNetwork = new FeedForwardNetwork();
		scoreEvalNetwork.addBlock(515, 100, true, Activation.TANH);
		scoreEvalNetwork.addBlock(100, 50, true, Activation.TANH);
		scoreEvalNetwork.addBlock(50, 20, true, Activation.TANH);
		scoreEvalNetwork.addBlock(20, 1, true, Activation.SIGMOID);
		scoreEvalNetwork.setAllWeightsRandom(rnd, 1);
	}
	
	// 8
	private void initNet() {
		scoreEvalNetwork = new FeedForwardNetwork();
		scoreEvalNetwork.addBlock(515, 100, true, Activation.TANH);
		scoreEvalNetwork.addBlock(100, 50, true, Activation.TANH);
		scoreEvalNetwork.addBlock(50, 20, true, Activation.TANH);
		scoreEvalNetwork.addBlock(20, 1, true, Activation.NONE);
		scoreEvalNetwork.setAllWeightsRandom(rnd, 1);
	}
	
	private void fillInputVector(QwinPaper paper, double[] input) {
		int[] redline = paper.getRedLine();
		int[] yellowline = paper.getYellowLine();
		int[] purpleline = paper.getPurpleLine();
		for (int i = 0; i < 9; i++) {
			if (redline[i] != 0) {
				input[i * 18 + redline[i] - 1] = 1;
				input[488+i] = 1;
			}	
			if (yellowline[i] != 0) {
				input[18 * (9 + i) + yellowline[i] - 1] = 1;
				input[497+i] = 1;
			}
			if (purpleline[i] != 0) {
				input[18 * (18 + i) + purpleline[i] - 1] = 1;
				input[506+i] = 1;
			}
		}
		input[486] = paper.getNumberOfMisthrows() * 1. / 4.;
		input[487] = numOfTurns / (15. + Math.abs(numOfTurns));
	}
	
	// fill input vector for 
	/**
	 * 0	r1
	 * 1	r2
	 * 2	r3
	 * 3	r4
	 * 4	r5
	 * 5	r6
	 * 6	r7
	 * 7	r8
	 * 8	r9
	 * 9	y1
	 * 10	y2
	 * 11	y3
	 * 12	y4
	 * 13	y5
	 * 14	y6
	 * 15	y7
	 * 16	y8
	 * 17	y9
	 * 18	p1
	 * 19	p2
	 * 20	p3
	 * 21	p4
	 * 22	p5
	 * 23	p6
	 * 24	p7
	 * 25	p8
	 * 26	p9
	 * 27	mis
	 * 28	rou
	 * 
	 * 
	 * @param paper
	 * @param input
	 */
	private void fillInputVector2(QwinPaper paper, double[] input) {
		int[] redline = paper.getRedLine();
		int[] yellowline = paper.getYellowLine();
		int[] purpleline = paper.getPurpleLine();
		for (int i = 0; i < 9; i++) {
			input[i] = redline[i]/18.;
			input[i+9] = yellowline[i]/18.;
			input[i+18] = purpleline[i]/18.;
		}
		input[27] = paper.getNumberOfMisthrows() * 1. / 3.;
		input[28] = numOfTurns / (15. + Math.abs(numOfTurns));
	}
	
	
	private void initNet2() {
		scoreEvalNetwork = new FeedForwardNetwork();
		scoreEvalNetwork.addBlock(488, 100, true, Activation.TANH);
		scoreEvalNetwork.addBlock(100, 100, true, Activation.TANH);
		scoreEvalNetwork.addBlock(100, 100, true, Activation.TANH);
		scoreEvalNetwork.addBlock(100, 100, true, Activation.TANH);
		scoreEvalNetwork.addBlock(100, 1, true, Activation.ELU);
		scoreEvalNetwork.setAllWeightsRandom(rnd, 1);
	}

	public FeedForwardNetwork getEvalNetwork() {
		return scoreEvalNetwork;
	}

	public void setEvalNetwork(FeedForwardNetwork network) {
		scoreEvalNetwork = network;
	}

	@Override
	public double evaluatePaper(QwinPaper paper) {
		//if (paper.isEndCondition())
		//	return paper.calculateScore();
		double[] input = new double[scoreEvalNetwork.getInputLength()];
		fillInputVector(paper, input);
		scoreEvalNetwork.prozessInput(input);
		double ret = scoreEvalNetwork.getLastOutputVector()[0];
		return ret;
	}

	@Override
	public String getName() {
		return "LANNEVAL("+UtilMethods.roundTo(noiselevel, 2)+")";
	}

}
