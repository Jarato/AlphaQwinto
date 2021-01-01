package game.experiments;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

import game.QwintoMatch;
import game.QwintoMatchBP;
import game.qwplayer.QwinPlayerExpertETest2;
import game.qwplayer.dev.QwinPlayer;
import game.qwplayer.dev.QwinPlayerNN2;
import model.FeedForwardNetwork;
import model.learner.Learner;
import model.learner.MomentumDescent;
import pdf.util.Pair;
import pdf.util.UtilMethods;

public class NNPlayer1v1Backprop extends Thread{
	private int expNum;
	
	public NNPlayer1v1Backprop(ThreadGroup tg, int sExpNum) {
		super(tg,"experiment"+sExpNum);
		expNum = sExpNum+1;
	}
	
	private int[][] simulateMatches(QwinPlayer[] players, int iterations, boolean print) {
		if (print) System.out.println("Simulation of " + iterations + " matches between " + players.length + " players started.");
		Random init = new Random();
		int[][] scores = new int[players.length][iterations];
		for (int i = 0; i < iterations; i++) {
			for (int k = 0; k < players.length; k++) {
				players[k].reset(new Random(init.nextLong()));
			}
			QwintoMatch match = new QwintoMatch(new Random(init.nextLong()), players);
			match.calculateMatch(false);
			for (int k = 0; k < players.length; k++) {
				scores[k][i] = players[k].getPaper().calculateScore();
			}
			if (print && i % (iterations / 10) == 0) System.out.print("" + ((i * 100) / iterations) + "%\t");
		}
		if (print) System.out.println("100%");
		if (print) System.out.println("Simulations done\n");
		return scores;
	}
	
	@Override
	public void run() {
		double randomPlayPercent = 0.75;
		double regularisation = 0.01;
		int numberOfTestMatches = 2000;
		Random init = new Random();
		QwinPlayerNN2 initPlayer = new QwinPlayerNN2(new Random(init.nextLong()));
		FeedForwardNetwork diceThrowNet = initPlayer.getDiceThrowNet();
		FeedForwardNetwork actionListNet = initPlayer.getActionListNet();
		ArrayList<Pair<double[], Integer>> betterDiceThrowHistory = new ArrayList<Pair<double[], Integer>>();
		ArrayList<Pair<double[], Integer>> betterActionHistory = new ArrayList<Pair<double[], Integer>>();
		ArrayList<Pair<double[], Integer>> worseDiceThrowHistory = new ArrayList<Pair<double[], Integer>>();
		ArrayList<Pair<double[], Integer>> worseActionHistory = new ArrayList<Pair<double[], Integer>>();
		//int j = 0;
		//Learner learnerdb = new VProp85avg();
		Learner learnerdw = new MomentumDescent(0.1,0.9);
		//Learner learnerab = new VProp85avg();
		Learner learneraw = new MomentumDescent(0.1,0.9);
		int step = 0;
		while (step <= 500) {
			step++;
			int n1 = 0;
			int n2 = 0;
			while (n1 < 1000 || n2 < 1000) {
				QwinPlayerNN2[] player = new QwinPlayerNN2[2];
				int[] score = new int[2];
				// SET NEURAL NETWORKS
				 for (int i = 0; i < 2; i++) {
					 player[i] = new QwinPlayerNN2(new Random(init.nextLong()));
					 player[i].setDiceThrowNet(diceThrowNet.copy());
					 player[i].setActionListNet(actionListNet.copy());
				 }
				// player[1] = new QwinPlayerExpertETest2(new Random(init.nextLong()));
				// CALCULATE MATCH
				QwintoMatchBP match = new QwintoMatchBP(new Random(init.nextLong()), player);
				match.calculateMatch(randomPlayPercent, false);
				for (int k = 0; k < player.length; k++) {
					score[k] = player[k].getPaper().calculateScore();
				}
				// DETERMINE, WHO WON
				if (score[0] > score[1]) {
					betterDiceThrowHistory.addAll(player[0].getRndDiceThrowHistory());
					betterActionHistory.addAll(player[0].getRndActionHistory());
					worseDiceThrowHistory.addAll(player[1].getRndDiceThrowHistory());
					worseActionHistory.addAll(player[1].getRndActionHistory());
				} else if (score[1] > score[0]){
					betterDiceThrowHistory.addAll(player[1].getRndDiceThrowHistory());
					betterActionHistory.addAll(player[1].getRndActionHistory());
					worseDiceThrowHistory.addAll(player[0].getRndDiceThrowHistory());
					worseActionHistory.addAll(player[0].getRndActionHistory());
				}
				n1 = betterDiceThrowHistory.size();
				n2 = betterActionHistory.size();
			}

			QwinPlayer[] testP = new QwinPlayer[2];
			QwinPlayerNN2 testNN2 = new QwinPlayerNN2(new Random(init.nextLong()));
			testNN2.setDiceThrowNet(diceThrowNet);
			testNN2.setActionListNet(actionListNet);
			testP[0] = testNN2;
			testP[1] = new QwinPlayerExpertETest2(new Random(init.nextLong()));
			int[][] scores = simulateMatches(testP, numberOfTestMatches, false);
			double avgScore = 0;
			double winrate = 0;
			for (int i = 0; i < numberOfTestMatches; i++) {
				avgScore += scores[0][i];
				winrate += (scores[0][i] > scores[1][i] ? 1 : 0);
			}
			avgScore /= numberOfTestMatches;
			winrate /= numberOfTestMatches;
			System.out.println(step + ". Step:");
			System.out.println("vs ExpertETest2\t" + avgScore + "\t" + winrate);
			System.out.println();
			try (FileWriter fw = new FileWriter("experiment_D0.75_" + expNum + ".txt", true); BufferedWriter bw = new BufferedWriter(fw); PrintWriter out = new PrintWriter(bw)) {
				out.println(avgScore + "\t" + winrate);
			} catch (IOException e) {
				// exception handling left as an exercise for the reader
			}
			/*if (j % 10 == 0) {
				try {
					File paramFile = new File("" + j / 10 + "_" + avgScore + "_" + winrate + ".txt");
					FileWriter writer = new FileWriter(paramFile);
					double[] weightsBiasVector = diceThrowNet.copyWeightBiasVector();
					for (int i = 0; i < weightsBiasVector.length; i++) {
						writer.write(weightsBiasVector[i] + "\n");
					}
					weightsBiasVector = actionListNet.copyWeightBiasVector();
					for (int i = 0; i < weightsBiasVector.length; i++) {
						writer.write(weightsBiasVector[i] + "\n");
					}
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}*/
			//j++;
			//randomPlayPercent = (randomPlayPercent * 0.995 > 0 ? randomPlayPercent * 0.995 : randomPlayPercent);
			// learningRate = (learningRate * 0.99 > 0 ? learningRate * 0.99 :
			// learningRate);
			// BACKPROP LEARN
			for (int m = 0; m < 100; m++) {
				// DICE THROW BETTER
				int n = 0;
				double[] gradient = null;
				for (Pair<double[], Integer> pair : betterDiceThrowHistory) {
					double[] targetVector = new double[7];
					targetVector[pair.getY()] = 1;
					double[] dout = diceThrowNet.calculateDerivativeOutput(pair.getX(), targetVector);
					for (int i = 0; i < dout.length; i++) {
						if (i != pair.getY()) dout[i] = 0;
					}
					diceThrowNet.calculateGradient(dout);
					double[] tempGradient = diceThrowNet.getWeightBiasGradient();
					gradient = (gradient == null ? tempGradient : UtilMethods.vectorAddition(gradient, tempGradient));
					n++;
				}
				// if (gradient != null) {
				// gradient = learnerdb.adaptGradientVector(gradient);
				// diceThrowNet.applyWeightsBiasGradient(gradient);
				// }
				// DICE THROW WORSE
				// n = 0;
				// gradient = null;
				for (Pair<double[], Integer> pair : worseDiceThrowHistory) {
					double[] targetVector = new double[7];
					targetVector[pair.getY()] = 0;
					double[] dout = diceThrowNet.calculateDerivativeOutput(pair.getX(), targetVector);
					for (int i = 0; i < dout.length; i++) {
						if (i != pair.getY()) dout[i] = 0;
					}
					diceThrowNet.calculateGradient(dout);
					double[] tempGradient = diceThrowNet.getWeightBiasGradient();
					gradient = (gradient == null ? tempGradient : UtilMethods.vectorAddition(gradient, tempGradient));
					n++;
				}
				if (gradient != null) {
					for (int i = 0; i < gradient.length; i++) {
						gradient[i] /= n;
					}
					gradient = learnerdw.adaptGradientVector(gradient);
					diceThrowNet.applyWeightsBiasGradient(gradient, regularisation);
				}
				// ACTION LIST
				n = 0;
				gradient = null;
				for (Pair<double[], Integer> pair : betterActionHistory) {
					double[] targetVector = new double[28];
					targetVector[pair.getY()] = 1;
					double[] dout = actionListNet.calculateDerivativeOutput(pair.getX(), targetVector);
					for (int i = 0; i < dout.length; i++) {
						if (i != pair.getY()) dout[i] = 0;
					}
					actionListNet.calculateGradient(dout);
					double[] tempGradient = actionListNet.getWeightBiasGradient();
					gradient = (gradient == null ? tempGradient : UtilMethods.vectorAddition(gradient, tempGradient));
					n++;
				}
				// if (gradient != null) {
				// gradient = learnerab.adaptGradientVector(gradient);
				// actionListNet.applyWeightsBiasGradient(gradient);
				// }
				// n = 0;
				// gradient = null;
				for (Pair<double[], Integer> pair : worseActionHistory) {
					double[] targetVector = new double[28];
					targetVector[pair.getY()] = 0;
					double[] dout = actionListNet.calculateDerivativeOutput(pair.getX(), targetVector);
					for (int i = 0; i < dout.length; i++) {
						if (i != pair.getY()) dout[i] = 0;
					}
					actionListNet.calculateGradient(dout);
					double[] tempGradient = actionListNet.getWeightBiasGradient();
					gradient = (gradient == null ? tempGradient : UtilMethods.vectorAddition(gradient, tempGradient));
					n++;
				}
				if (gradient != null) {
					for (int i = 0; i < gradient.length; i++) {
						gradient[i] /= n;
					}
					gradient = learneraw.adaptGradientVector(gradient);
					actionListNet.applyWeightsBiasGradient(gradient, regularisation);
				}
			}
			
			betterDiceThrowHistory = new ArrayList<Pair<double[], Integer>>();
			betterActionHistory = new ArrayList<Pair<double[], Integer>>();
			worseDiceThrowHistory = new ArrayList<Pair<double[], Integer>>();
			worseActionHistory = new ArrayList<Pair<double[], Integer>>();
		}
	}
	
}
