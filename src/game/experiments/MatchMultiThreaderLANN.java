package game.experiments;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import game.QwintoMatch;
import game.qwplayer.dev.QwinPlayerLA_NNEval;
import pdf.util.Pair;

public class MatchMultiThreaderLANN extends Thread {
	private ArrayList<Pair<double[], Integer>> paperScoreHistory = new ArrayList<Pair<double[], Integer>>();
	private double avgScore;
	private int numMatches;
	private Random rnd;
	private double[] wbBase;
	private int numPlayers;
	private double noise_lvl;
	
	public MatchMultiThreaderLANN(String name, ThreadGroup tg, int numberOfMatches, Random rnd, double[] weightsBiasBase, double noise_level, int numOfPlayers) {
		super(tg,name);
		numMatches = numberOfMatches;
		this.rnd = rnd;
		wbBase = weightsBiasBase;
		numPlayers = numOfPlayers;
		noise_lvl = noise_level;
	}
	
	public double getAvgScore() {
		return avgScore;
	}
	
	public ArrayList<Pair<double[], Integer>> getDataHistory(){
		return paperScoreHistory;
	}
	
	@Override
	public void run() {
		avgScore = 0;
		for (int matchCounter = 0; matchCounter < numMatches; matchCounter++) {
			// 2 different noised weight-vectors for 2 players
			double[][] noisedWBvecs = new double[numPlayers][wbBase.length];
			// add some noise to the weights and biases
			for (int i = 0; i < noisedWBvecs.length; i++) {
				for (int ii = 0; ii < wbBase.length; ii++) {
					noisedWBvecs[i][ii] = wbBase[ii] + rnd.nextGaussian() * noise_lvl;
				}
			}

			QwinPlayerLA_NNEval[] players = new QwinPlayerLA_NNEval[numPlayers];

			for (int k = 0; k < players.length; k++) {
				players[k] = new QwinPlayerLA_NNEval(new Random(rnd.nextLong()));
				players[k].getEvalNetwork().applyWeightsBiasesVector(noisedWBvecs[k]);
			}
			QwintoMatch match = new QwintoMatch(new Random(rnd.nextLong()), players);
			match.calculateMatch(false);
			// add the average score between the players
			double playeravgScore = 0;
			for (int k = 0; k < players.length; k++) {
				playeravgScore += players[k].getScore();
			}
			avgScore += playeravgScore / players.length;
			// collect the players histories
			for (int pc = 0; pc < players.length; pc++) {
				paperScoreHistory.addAll(players[pc].getPaperScoreHistory());
				players[pc].reset();
			}
		}
		avgScore /= numMatches;
	}
}
