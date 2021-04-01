package game.experiments;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import game.QwintoMatch;
import game.qwplayer.dev.QwinPlayerLA_NNEval;
import pdf.util.Pair;

public class MatchMultiThreaderLANN extends Thread {
	private ArrayList<Pair<double[], Double>> paperScoreHistory = new ArrayList<Pair<double[], Double>>();
	private double avgGameLength;
	private double avgMisThrowsAtEnd;
	private double avgNumFullLanesAtEnd;
	private double avgNumNumbersAtEnd;
	private double avgProportionReject;
	private double avgFullPentagonColumns;
	private double avgPropNoisedActions;
	private double avgScore;
	private int numMatches;
	private Random rnd;
	private double[] wbBase;
	private int numPlayers;
	private double noise_lvl;
	private int ai_version;
	
	public MatchMultiThreaderLANN(String name, ThreadGroup tg, int numberOfMatches, Random rnd, int version, double[] weightsBiasBase, double noise_level, int numOfPlayers) {
		super(tg,name);
		numMatches = numberOfMatches;
		this.rnd = rnd;
		wbBase = weightsBiasBase;
		numPlayers = numOfPlayers;
		noise_lvl = noise_level;
		ai_version = version;
	}
	
	public double getAvgScore() {
		return avgScore;
	}
	
	public double getAvgPropNoisedActions() {
		return avgPropNoisedActions;
	}
	
	public double getAvgGameLength() {
		return avgGameLength;
	}
	
	public double getAvgMisthrowsAtEnd() {
		return avgMisThrowsAtEnd;
	}
	
	public double getNumFullLanesAtEnd() {
		return avgNumFullLanesAtEnd;
	}
	
	public double getNumNumbersAtEnd() {
		return avgNumNumbersAtEnd;
	}
	
	public double getAvgPropReject() {
		return avgProportionReject;
	}
	
	public double getAvgFullPentagonCols() {
		return avgFullPentagonColumns;
	}

	
	public ArrayList<Pair<double[], Double>> getDataHistory(){
		return paperScoreHistory;
	}
	
	@Override
	public void run() {
		avgScore = 0;
		avgGameLength = 0;        
		avgMisThrowsAtEnd = 0;    
		avgNumFullLanesAtEnd = 0;
		avgNumNumbersAtEnd = 0;   
		avgGameLength = 0;
		avgProportionReject = 0;
		avgFullPentagonColumns = 0;
		avgPropNoisedActions = 0;
		for (int matchCounter = 0; matchCounter < numMatches; matchCounter++) {
			// 2 different noised weight-vectors for 2 players
			double[][] noisedWBvecs = new double[numPlayers][wbBase.length];
			// add some noise to the weights and biases
			/*for (int i = 0; i < noisedWBvecs.length; i++) {
				for (int ii = 0; ii < wbBase.length; ii++) {
					noisedWBvecs[i][ii] = wbBase[ii] + rnd.nextGaussian() * noise_lvl;
				}
			}*/

			QwinPlayerLA_NNEval[] players = new QwinPlayerLA_NNEval[numPlayers];

			for (int k = 0; k < players.length; k++) {
				players[k] = new QwinPlayerLA_NNEval(new Random(rnd.nextLong()),ai_version);
				players[k].getEvalNetwork().applyWeightsBiasesVector(wbBase);
				players[k].setNoiseLevel(noise_lvl);
			}
			QwintoMatch match = new QwintoMatch(new Random(rnd.nextLong()), players);
			match.calculateMatch();
			// add the average score between the players
			double playeravgScore = 0;
			double playeravgMisthrows = 0;
			double playeravgNumFullLanes = 0;
			double playeravgEnteredNumbers = 0;
			double playeravgReject = 0;
			double playeravgFullPentagonColumns = 0;
			double playeravgPropNoisedActions = 0;
			for (int k = 0; k < players.length; k++) {
				playeravgScore += players[k].getScore();
				playeravgMisthrows += players[k].getPaper().getNumberOfMisthrows();
				playeravgNumFullLanes += players[k].getPaper().getNumberOfFullLanes();
				playeravgEnteredNumbers += players[k].getPaper().getNumberOfEnteredNumbers();
				playeravgReject += players[k].getProportionOfRejects();
				playeravgFullPentagonColumns += players[k].getPaper().getNumberOfFullPentagonColumns();
			}
			avgScore += playeravgScore / players.length;
			avgGameLength += match.getNumberOfTurns();
			avgMisThrowsAtEnd += playeravgMisthrows / players.length;
			avgNumFullLanesAtEnd += playeravgNumFullLanes / players.length;
			avgNumNumbersAtEnd += playeravgEnteredNumbers / players.length;
			avgProportionReject += playeravgReject / players.length;
			avgFullPentagonColumns += playeravgFullPentagonColumns / players.length;
			avgPropNoisedActions += playeravgPropNoisedActions / players.length;
			// collect the players histories
			for (int pc = 0; pc < players.length; pc++) {
				paperScoreHistory.addAll(players[pc].getPaperScoreHistory());
				players[pc].reset();
			}
		}
		avgScore /= numMatches;
		avgGameLength /= numMatches;
		avgMisThrowsAtEnd /= numMatches;
		avgNumFullLanesAtEnd /= numMatches;
		avgNumNumbersAtEnd /= numMatches;
		avgProportionReject /= numMatches;
		avgFullPentagonColumns /= numMatches;
		avgPropNoisedActions /= numMatches;
	}
}
