package game.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import game.QwintoMatch;
import game.QwintoMatchBP;
import game.qwplayer.QwinPlayerExpert;
import game.qwplayer.QwinPlayerExpertETest;
import game.qwplayer.QwinPlayerExpertETest2;
import game.qwplayer.QwinPlayerNN2Test;
import game.qwplayer.QwinPlayerNNTest;
import game.qwplayer.QwinPlayerNNTestOld;
import game.qwplayer.QwinPlayerRandom;
import game.qwplayer.dev.QwinPlayer_t;
import game.qwplayer.dev.QwinPlayerEvo_t;
import game.qwplayer.dev.QwinPlayerExpertEvo;
import game.qwplayer.dev.QwinPlayerLA_NNEval;
import game.qwplayer.dev.QwinPlayerNN;
import game.qwplayer.dev.QwinPlayerNN2;
import game.qwplayer.dev.QwinPlayerRnd_t;
import model.BaseBlock;
import model.FeedForwardNetwork;
import model.learner.GradientDescent;
import model.learner.Learner;
import model.learner.MomentumDescent;
import model.learner.VProp85avg;
import pdf.ai.dna.DNA;
import pdf.ai.dna.EvolutionMethods;
import pdf.util.Pair;
import pdf.util.UtilMethods;

public class QwintoSimulation {
	public static void main(String[] args) {
		simpleMatch();
		//NNPlayer1v1MultiThreadMatcher(1);
		// runNNPlayer1v1Experiments();
		// playerEvolutionAvA();
		//NN2Player1v1Backprop(4);
	}

	public static String combinationFormatter(final long millis) {
		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis)
				- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
				- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis));
		long hours = TimeUnit.MILLISECONDS.toHours(millis);

		StringBuilder b = new StringBuilder();
		b.append(hours == 0 ? "00" : hours < 10 ? String.valueOf("0" + hours) : String.valueOf(hours));
		b.append(":");
		b.append(minutes == 0 ? "00" : minutes < 10 ? String.valueOf("0" + minutes) : String.valueOf(minutes));
		b.append(":");
		b.append(seconds == 0 ? "00" : seconds < 10 ? String.valueOf("0" + seconds) : String.valueOf(seconds));
		return b.toString();
	}

	public static void NNPlayer1v1MultiThreadMatcher(int experimentNumber) {
		MatchMultiThreader.randomPlayPercent = 0.5;
		double regularisation = 0.005;
		int numberOfTestMatches = 2000;
		Random init = new Random();
		QwinPlayerNN2 initPlayer = new QwinPlayerNN2(new Random(init.nextLong()));
		MatchMultiThreader.diceThrowNet = initPlayer.getDiceThrowNet();
		MatchMultiThreader.actionListNet = initPlayer.getActionListNet();
		// int j = 0;
		// Learner learnerdb = new VProp85avg();
		Learner learnerdw = new MomentumDescent(0.1, 0.9);
		// Learner learnerab = new VProp85avg();
		Learner learneraw = new MomentumDescent(0.1, 0.9);
		int step = 0;
		double stepMillis = 0;
		while (true) {
			long start = System.currentTimeMillis();
			step++;
			ThreadGroup tg = new ThreadGroup("main");
			Thread th1 = new MatchMultiThreader("1", tg);
			th1.start();
			Thread th2 = new MatchMultiThreader("2", tg);
			th2.start();
			Thread th3 = new MatchMultiThreader("3", tg);
			th3.start();
			Thread th4 = new MatchMultiThreader("4", tg);
			th4.start();
			QwinPlayer_t[] testP = new QwinPlayer_t[2];
			QwinPlayerNN2 testNN2 = new QwinPlayerNN2(new Random(init.nextLong()));
			testNN2.setDiceThrowNet(MatchMultiThreader.diceThrowNet.copy());
			testNN2.setActionListNet(MatchMultiThreader.actionListNet.copy());
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
			try (FileWriter fw = new FileWriter("experiment_r0.1_0.01_" + experimentNumber + ".txt", true);
					BufferedWriter bw = new BufferedWriter(fw);
					PrintWriter out = new PrintWriter(bw)) {
				out.println(avgScore + "\t" + winrate);
			} catch (IOException e) {
				// exception handling left as an exercise for the reader
			}
			/*
			 * if (j % 10 == 0) { try { File paramFile = new File("" + j / 10 + "_" +
			 * avgScore + "_" + winrate + ".txt"); FileWriter writer = new
			 * FileWriter(paramFile); double[] weightsBiasVector =
			 * diceThrowNet.copyWeightBiasVector(); for (int i = 0; i <
			 * weightsBiasVector.length; i++) { writer.write(weightsBiasVector[i] + "\n"); }
			 * weightsBiasVector = actionListNet.copyWeightBiasVector(); for (int i = 0; i <
			 * weightsBiasVector.length; i++) { writer.write(weightsBiasVector[i] + "\n"); }
			 * writer.close(); } catch (IOException e) { e.printStackTrace(); } }
			 */
			// j++;
			// randomPlayPercent = (randomPlayPercent * 0.995 > 0 ? randomPlayPercent *
			// 0.995 : randomPlayPercent);
			// learningRate = (learningRate * 0.99 > 0 ? learningRate * 0.99 :
			// learningRate);
			try {
				th1.join();
				th2.join();
				th3.join();
				th4.join();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			System.out.println("Simulations done.");
			// BACKPROP LEARN
			for (int m = 0; m < 10; m++) {
				// DICE THROW BETTER
				int n = 0;
				double[] gradient = null;
				for (Pair<double[], Integer> pair : MatchMultiThreader.betterDiceThrowHistory) {
					double[] targetVector = new double[7];
					targetVector[pair.getY()] = 1;
					double[] dout = MatchMultiThreader.diceThrowNet.calculateDerivativeOutput(pair.getX(),
							targetVector);
					for (int i = 0; i < dout.length; i++) {
						if (i != pair.getY())
							dout[i] = 0;
					}
					MatchMultiThreader.diceThrowNet.calculateGradient(dout);
					double[] tempGradient = MatchMultiThreader.diceThrowNet.getWeightBiasGradient();
					gradient = (gradient == null ? tempGradient : UtilMethods.vectorAddition(gradient, tempGradient));
					n++;
				}

				// DICE THROW WORSE

				for (Pair<double[], Integer> pair : MatchMultiThreader.worseDiceThrowHistory) {
					double[] targetVector = new double[7];
					targetVector[pair.getY()] = 0;
					double[] dout = MatchMultiThreader.diceThrowNet.calculateDerivativeOutput(pair.getX(),
							targetVector);
					for (int i = 0; i < dout.length; i++) {
						if (i != pair.getY())
							dout[i] = 0;
					}
					MatchMultiThreader.diceThrowNet.calculateGradient(dout);
					double[] tempGradient = MatchMultiThreader.diceThrowNet.getWeightBiasGradient();
					gradient = (gradient == null ? tempGradient : UtilMethods.vectorAddition(gradient, tempGradient));
					n++;
				}
				if (gradient != null) {
					for (int i = 0; i < gradient.length; i++) {
						gradient[i] /= n;
					}
					gradient = learnerdw.adaptGradientVector(gradient);
					MatchMultiThreader.diceThrowNet.applyWeightsBiasGradient(gradient, regularisation);
				}
				// ACTION LIST
				n = 0;
				gradient = null;
				for (Pair<double[], Integer> pair : MatchMultiThreader.betterActionHistory) {
					double[] targetVector = new double[28];
					targetVector[pair.getY()] = 1;
					double[] dout = MatchMultiThreader.actionListNet.calculateDerivativeOutput(pair.getX(),
							targetVector);
					for (int i = 0; i < dout.length; i++) {
						if (i != pair.getY())
							dout[i] = 0;
					}
					MatchMultiThreader.actionListNet.calculateGradient(dout);
					double[] tempGradient = MatchMultiThreader.actionListNet.getWeightBiasGradient();
					gradient = (gradient == null ? tempGradient : UtilMethods.vectorAddition(gradient, tempGradient));
					n++;
				}
				// if (gradient != null) {
				// gradient = learnerab.adaptGradientVector(gradient);
				// actionListNet.applyWeightsBiasGradient(gradient);
				// }
				// n = 0;
				// gradient = null;
				for (Pair<double[], Integer> pair : MatchMultiThreader.worseActionHistory) {
					double[] targetVector = new double[28];
					targetVector[pair.getY()] = 0;
					double[] dout = MatchMultiThreader.actionListNet.calculateDerivativeOutput(pair.getX(),
							targetVector);
					for (int i = 0; i < dout.length; i++) {
						if (i != pair.getY())
							dout[i] = 0;
					}
					MatchMultiThreader.actionListNet.calculateGradient(dout);
					double[] tempGradient = MatchMultiThreader.actionListNet.getWeightBiasGradient();
					gradient = (gradient == null ? tempGradient : UtilMethods.vectorAddition(gradient, tempGradient));
					n++;
				}
				if (gradient != null) {
					for (int i = 0; i < gradient.length; i++) {
						gradient[i] /= n;
					}
					gradient = learneraw.adaptGradientVector(gradient);
					MatchMultiThreader.actionListNet.applyWeightsBiasGradient(gradient, regularisation);
				}
			}

			MatchMultiThreader.reset();
			long end = System.currentTimeMillis();
			if (stepMillis == 0) {
				stepMillis = end - start;
			} else {
				stepMillis = stepMillis * 0.95 + (end - start) * 0.05;
			}
			System.out.println(end - start);
			System.out.println("approx. time left: " + combinationFormatter((long) (stepMillis * (501 - step))));
		}
	}

	public static void runNNPlayer1v1Experiments() {
		ThreadGroup tg = new ThreadGroup("main");
		int np = Runtime.getRuntime().availableProcessors() - 1; // -1 for leaving one processor free

		NNPlayer1v1Backprop[] experiments = new NNPlayer1v1Backprop[10];
		for (int i = 0; i < experiments.length; i++) {
			experiments[i] = new NNPlayer1v1Backprop(tg, i);
		}
		int k = 0;
		while (k < 10) {
			if (tg.activeCount() < np) {
				Thread experiment = experiments[k];
				experiment.start();
				k++;
			} else {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		while (tg.activeCount() > 0) {

			try {
				Thread.sleep(10000);
			}

			catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}

	private static int[][] simulateMatches(QwinPlayer_t[] players, int iterations, boolean print) {
		if (print)
			System.out.println(
					"Simulation of " + iterations + " matches between " + players.length + " players started.");
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
			if (print && i % (iterations / 10) == 0)
				System.out.print("" + ((i * 100) / iterations) + "%\t");
		}
		if (print)
			System.out.println("100%");
		if (print)
			System.out.println("Simulations done\n");
		return scores;
	}

	public static void simpleMatch() {
		Random init = new Random();
		QwinPlayer_t[] qPlayers = new QwinPlayer_t[2];
		qPlayers[0] = new QwinPlayerLA_NNEval(init);
		qPlayers[1] = new QwinPlayerExpertETest2(init);
		QwintoMatch match = new QwintoMatch(new Random(init.nextLong()), qPlayers);
		match.calculateMatch(true);
		for (int i = 0; i < qPlayers.length; i++) {
			System.out.println("" + (i + 1) + ". Player (" + qPlayers[i].getName() + ")");
			System.out.println(qPlayers[i].getPaper());
			System.out.println(qPlayers[i].getScore());
			System.out.println();
		}
	}

	public static void QwinPlayerLA_1v1_Backprop() {
		double regularisation = 0.001;
		int numberOfMatches = 10000;
		int numberOfLearningSteps = 10;
		Random init = new Random();
		QwinPlayerLA_NNEval initPlayer = new QwinPlayerLA_NNEval(new Random(init.nextLong()));	
		FeedForwardNetwork evalNetwork = initPlayer.getEvalNetwork();
		ArrayList<Pair<double[], Integer>> fullPaperScoreHistory = new ArrayList<Pair<double[], Integer>>();
		
		Learner learner = new MomentumDescent(0.01, 0.9);
		
		int step = 0;
		while(true) {
			
		}
	}
	
	public static void NN2Player1v1Backprop(int experimentNumber) {
		double randomPlayPercent = 0.5;
		double regularisation = 0.0005;
		int numberOfTestMatches = 1000;
		int numberOfLearningSteps = 10;
		Random init = new Random();
		QwinPlayerNN2 initPlayer = new QwinPlayerNN2(new Random(init.nextLong()));
		FeedForwardNetwork diceThrowNet = initPlayer.getDiceThrowNet();
		FeedForwardNetwork actionListNet = initPlayer.getActionListNet();
		ArrayList<Pair<double[], Integer>> betterDiceThrowHistory = new ArrayList<Pair<double[], Integer>>();
		ArrayList<Pair<double[], Integer>> betterActionHistory = new ArrayList<Pair<double[], Integer>>();
		ArrayList<Pair<double[], Integer>> worseDiceThrowHistory = new ArrayList<Pair<double[], Integer>>();
		ArrayList<Pair<double[], Integer>> worseActionHistory = new ArrayList<Pair<double[], Integer>>();
		// int j = 0;
		// Learner learnerdb = new VProp85avg();
		Learner learnerdw = new MomentumDescent(0.1, 0.9);
		// Learner learnerab = new VProp85avg();
		Learner learneraw = new MomentumDescent(0.1, 0.9);
		int step = 0;
		while (true) {
			step++;
			int n1 = 0;
			int n2 = 0;
			while (n1 < 2000 || n2 < 2000) {
				QwinPlayerNN2[] player = new QwinPlayerNN2[2];
				int[] score = new int[2];
				// SET NEURAL NETWORKS
				for (int i = 0; i < 2; i++) {
					player[i] = new QwinPlayerNN2(new Random(init.nextLong()));
					player[i].setDiceThrowNet(diceThrowNet);
					player[i].setActionListNet(actionListNet);
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
				} else if (score[1] > score[0]) {
					betterDiceThrowHistory.addAll(player[1].getRndDiceThrowHistory());
					betterActionHistory.addAll(player[1].getRndActionHistory());
					worseDiceThrowHistory.addAll(player[0].getRndDiceThrowHistory());
					worseActionHistory.addAll(player[0].getRndActionHistory());
				}
				n1 = betterDiceThrowHistory.size();
				n2 = betterActionHistory.size();
			}

			QwinPlayer_t[] testP = new QwinPlayer_t[2];
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
			try (FileWriter fw = new FileWriter("experiment_0.5_" + experimentNumber + ".txt", true);
					BufferedWriter bw = new BufferedWriter(fw);
					PrintWriter out = new PrintWriter(bw)) {
				out.println(avgScore + "\t" + winrate);
			} catch (IOException e) {
				// exception handling left as an exercise for the reader
			}
			try {
				File paramFile = new File("" + step + "_" + avgScore + "_" + winrate + ".txt");
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
			// j++;
			// randomPlayPercent = (randomPlayPercent * 0.995 > 0 ? randomPlayPercent *
			// 0.995 : randomPlayPercent);
			// learningRate = (learningRate * 0.99 > 0 ? learningRate * 0.99 :
			// learningRate);
			// BACKPROP LEARN
			for (int p = 0; p < numberOfLearningSteps; p++) {
				// DICE THROW BETTER
				int n = 0;
				double[] gradient = null;
				for (Pair<double[], Integer> pair : betterDiceThrowHistory) {
					double[] targetVector = new double[7];
					targetVector[pair.getY()] = 1;
					double[] dout = diceThrowNet.calculateDerivativeOutput(pair.getX(), targetVector);
					for (int i = 0; i < dout.length; i++) {
						if (i != pair.getY())
							dout[i] = 0;
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
						if (i != pair.getY())
							dout[i] = 0;
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
						if (i != pair.getY())
							dout[i] = 0;
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
						if (i != pair.getY())
							dout[i] = 0;
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

	/*
	 * Idee: Für jeden Zug gibt es eine gewisse Wahrscheinlichkeit, dass dieser
	 * komplett zufällig gewählt wird. Diese zufälligen Züge werden vollständig mit
	 * Input und Output für das neuronale Netz gespeichert. Jedes Modell mit
	 * zufälligen Zügen wird gegen das Modell ohne diese Züge trainiert. Die
	 * zufälligen Züge des besten Modells, falls es besser als das Modell ohne den
	 * Zufall war, werden benutzt, um per Backpropagation und gradient-descent das
	 * Modell zu trainieren. Am Anfang spielen die neuronalen Netze mit 100%
	 * Zufallszügen und lernen die besseren Zufallszüge, d.h. die Züge von dem
	 * gewinnenden Spieler eines Matches. Die Lernrate kann von der Differenz des
	 * Scores des Gewinners zum Verlierer abhängig gemacht werden Dann wird langsam
	 * die Wahrscheinlichkeit erhöht, bei einem Zug die Ausgabe des neuronalen
	 * Netzes zu verwenden, bis am Ende die neuronalen Netze zu 100% spielen.
	 */

	public static void playerEvolutionVstatic() {
		int numberOfMatchesInit = 100;
		int populationSize = 30;
		int royalClubSize = 15;
		int generation = 0;
		Random init = new Random();
		QwinPlayerEvo_t[] players = new QwinPlayerEvo_t[populationSize];
		double mutationrate = 0.01;
		// init population
		for (int i = 0; i < populationSize; i++) {
			players[i] = new QwinPlayerNN(new Random(init.nextLong()));
		}
		while (true) {
			int numberOfMatches = numberOfMatchesInit; // + generation / 10;// + generation*2;
			if (numberOfMatches > 10000)
				numberOfMatches = 10000;
			double[] mean_scores = new double[players.length];
			double[] win_rate = new double[players.length];
			double[][] mean_scores_vs = new double[players.length][3];
			double[][] win_rate_vs = new double[players.length][3];
			for (int i = 0; i < players.length; i++) {
				QwinPlayer_t[] qPlayers = new QwinPlayer_t[] { players[i],
						new QwinPlayerExpertETest2(new Random(init.nextLong())) };
				int[][] scores = simulateMatches(qPlayers, numberOfMatches, false);
				double sum = 0;
				double wins = 0;
				for (int m = 0; m < numberOfMatches; m++) {
					sum += scores[0][m];
					wins += (scores[0][m] > scores[1][m] ? 1 : 0);
				}
				win_rate[i] += wins / numberOfMatches;
				win_rate_vs[i][0] += wins / numberOfMatches;
				mean_scores_vs[i][0] += sum / numberOfMatches;
				mean_scores[i] += sum / numberOfMatches;

				qPlayers = new QwinPlayer_t[] { players[i], new QwinPlayerRandom(new Random(init.nextLong())) };
				scores = simulateMatches(qPlayers, numberOfMatches, false);
				sum = 0;
				wins = 0;
				for (int m = 0; m < numberOfMatches; m++) {
					sum += scores[0][m];
					wins += (scores[0][m] > scores[1][m] ? 1 : 0);
				}
				win_rate[i] += wins / numberOfMatches;
				win_rate_vs[i][1] += wins / numberOfMatches;
				mean_scores_vs[i][1] += sum / numberOfMatches;
				mean_scores[i] += sum / numberOfMatches;

				qPlayers = new QwinPlayer_t[] { players[i], new QwinPlayerExpertETest(new Random(init.nextLong())) };
				scores = simulateMatches(qPlayers, numberOfMatches, false);
				sum = 0;
				wins = 0;
				for (int m = 0; m < numberOfMatches; m++) {
					sum += scores[0][m];
					wins += (scores[0][m] > scores[1][m] ? 1 : 0);
				}
				win_rate[i] += wins / numberOfMatches;
				win_rate_vs[i][2] += wins / numberOfMatches;
				mean_scores_vs[i][2] += sum / numberOfMatches;
				mean_scores[i] += sum / numberOfMatches;
			}

			for (int i = 0; i < mean_scores.length; i++) {
				mean_scores[i] = mean_scores[i] / 3.0;
				win_rate[i] = win_rate[i] / 3.0;
			}
			// Search for avg best for vs-stats
			double max = win_rate[0];
			int maxid = 0;
			for (int i = 1; i < populationSize; i++) {
				if (max < win_rate[i]) {
					max = win_rate[i];
					maxid = i;
				}
			}
			mean_scores_vs[0] = mean_scores_vs[maxid];
			win_rate_vs[0] = win_rate_vs[maxid];
			// Sort All
			for (int i = 0; i < royalClubSize; i++) {
				double maxValue = win_rate[i];
				int maxvIndex = i;
				for (int j = i + 1; j < populationSize; j++) {
					if (maxValue < win_rate[j]) {
						maxvIndex = j;
						maxValue = win_rate[j];
					}
				}
				if (maxvIndex != i) {
					// swap win_rate
					double swapValue = win_rate[maxvIndex];
					win_rate[maxvIndex] = win_rate[i];
					win_rate[i] = swapValue;
					// swap mean_score
					swapValue = mean_scores[maxvIndex];
					mean_scores[maxvIndex] = mean_scores[i];
					mean_scores[i] = swapValue;
					// swap player
					QwinPlayerEvo_t swapPlayer = players[maxvIndex];
					players[maxvIndex] = players[i];
					players[i] = swapPlayer;
				}
			}

			DNA betterDNA = players[0].getDNA();
			DNA worseDNA = players[1].getDNA();
			DNA gsDNA = EvolutionMethods.recombineVector(worseDNA, betterDNA, 1.5);
			players[royalClubSize].compoundDNA(gsDNA);
			for (int i = royalClubSize + 1; i < players.length; i++) {
				int firstIndex = init.nextInt(royalClubSize);
				int secondIndex = init.nextInt(royalClubSize - 1);
				if (firstIndex <= secondIndex)
					secondIndex++;
				DNA firstDNA = players[firstIndex].getDNA();
				DNA secondDNA = players[secondIndex].getDNA();
				DNA newDNA = EvolutionMethods.recombineXOR(firstDNA, secondDNA);
				mutationrate = (mutationrate * 0.999 > 0 ? mutationrate * 0.999 : mutationrate);
				newDNA = EvolutionMethods.mutate(newDNA, mutationrate);
				players[i].compoundDNA(newDNA);
			}
			for (int i = 0; i < players.length; i++) {
				players[i].reset(new Random(init.nextLong()));
			}
			System.out.println("Generation " + generation);
			System.out.println("Best NeuralNetwork Stats:");
			System.out.println("on average\t" + mean_scores[0] + "\t" + win_rate[0]);
			System.out.println("vs Random\t" + mean_scores_vs[0][1] + "\t" + win_rate_vs[0][1]);
			System.out.println("vs Expert1\t" + mean_scores_vs[0][2] + "\t" + win_rate_vs[0][2]);
			System.out.println("vs Expert2\t" + mean_scores_vs[0][0] + "\t" + win_rate_vs[0][0]);
			if (generation % 10 == 0) {
				try {
					players[0].getDNA().saveGeneValuesToFile(
							new File("Generation" + generation + "_" + mean_scores[0] + ".txt"),
							"Generation " + generation + "\nBest avg score\t" + mean_scores[0] + "\nNN-Setup\n"
									+ players[0].getName() + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			// System.out.println(((QwinPlayerExpertEvo) players[0]).getSettingsCode());
			System.out.println();
			generation++;
		}
	}

	public class PlayerStats {
		double avgScore;
		double avgWinrate;
		double[] other;
	}

	public static void playerEvolutionAvA() {
		int numberOfMatchesInit = 50;
		int populationSize = 20;
		int royalClubSize = 5;
		int generation = 0;
		Random init = new Random();
		QwinPlayerEvo_t[] players = new QwinPlayerEvo_t[populationSize];
		double mutationrate = 0.01;
		// init population
		for (int i = 0; i < populationSize; i++) {
			players[i] = new QwinPlayerNN(new Random(init.nextLong()));
		}
		while (true) {
			int numberOfMatches = numberOfMatchesInit + generation / 2;// + generation*2;
			if (numberOfMatches > 10000)
				numberOfMatches = 10000;
			double[] mean_scores = new double[players.length];
			double[] win_rate = new double[players.length];
			for (int i = 0; i < players.length; i++) {
				for (int j = i + 1; j < players.length; j++) {
					QwinPlayer_t[] qPlayers = new QwinPlayer_t[] { players[i], players[j] };
					int[][] scores = simulateMatches(qPlayers, numberOfMatches, false);
					int[] sum = new int[qPlayers.length];
					int[] wins = new int[qPlayers.length];
					for (int m = 0; m < numberOfMatches; m++) {
						sum[0] += scores[0][m];
						sum[1] += scores[1][m];
						wins[0] += (scores[0][m] > scores[1][m] ? 1 : 0);
						wins[1] += (scores[0][m] < scores[1][m] ? 1 : 0);
					}
					win_rate[i] += ((double) wins[0]) / numberOfMatches;
					win_rate[j] += ((double) wins[1]) / numberOfMatches;
					mean_scores[i] += ((double) sum[0]) / numberOfMatches;
					mean_scores[j] += ((double) sum[1]) / numberOfMatches;
				}
			}
			for (int i = 0; i < mean_scores.length; i++) {
				mean_scores[i] = mean_scores[i] / (populationSize - 1);
				win_rate[i] = win_rate[i] / (populationSize - 1);
			}
			// SORT after avg winrate
			for (int i = 0; i < royalClubSize; i++) {
				double maxValue = win_rate[i];
				int maxvIndex = i;
				for (int j = i + 1; j < populationSize; j++) {
					if (maxValue < win_rate[j]) {
						maxvIndex = j;
						maxValue = win_rate[j];
					}
				}
				if (maxvIndex != i) {
					// swap winrate
					double swapValue = win_rate[maxvIndex];
					win_rate[maxvIndex] = win_rate[i];
					win_rate[i] = swapValue;
					// swap mean_score
					swapValue = mean_scores[maxvIndex];
					mean_scores[maxvIndex] = mean_scores[i];
					mean_scores[i] = swapValue;
					// swap player
					QwinPlayerEvo_t swapPlayer = players[maxvIndex];
					players[maxvIndex] = players[i];
					players[i] = swapPlayer;
				}
			}
			DNA betterDNA = players[0].getDNA();
			DNA worseDNA = players[1].getDNA();
			DNA gsDNA = EvolutionMethods.recombineVector(worseDNA, betterDNA, 1.5);
			players[royalClubSize].compoundDNA(gsDNA);
			for (int i = royalClubSize + 1; i < players.length; i++) {
				int firstIndex = init.nextInt(royalClubSize);
				int secondIndex = init.nextInt(royalClubSize - 1);
				if (firstIndex <= secondIndex)
					secondIndex++;
				DNA firstDNA = players[firstIndex].getDNA();
				DNA secondDNA = players[secondIndex].getDNA();
				DNA newDNA = EvolutionMethods.recombineXOR(firstDNA, secondDNA);
				mutationrate = (mutationrate * 0.999 > 0 ? mutationrate * 0.999 : mutationrate);
				newDNA = EvolutionMethods.mutate(newDNA, mutationrate);
				players[i].compoundDNA(newDNA);
			}
			for (int i = 0; i < players.length; i++) {
				players[i].reset(new Random(init.nextLong()));
			}
			System.out.println("Generation " + generation);
			System.out.println("first\t" + mean_scores[0] + "\t" + win_rate[0]);
			System.out.println("second\t" + mean_scores[1] + "\t" + win_rate[1]);
			System.out.println("third\t" + mean_scores[2] + "\t" + win_rate[2]);
			if (generation % 10 == 0) {
				try {
					players[0].getDNA().saveGeneValuesToFile(
							new File("V2 Generation" + generation + "_" + mean_scores[0] + ".txt"),
							"Generation " + generation + "\nBest avg score\t" + mean_scores[0] + "\nNN-Setup\n"
									+ players[0].getName() + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			// System.out.println(((QwinPlayerExpertEvo) players[0]).getSettingsCode());
			System.out.println();
			generation++;
		}
	}

	public static void avgCompare() {
		int numberOfIterations = 100000;
		Random init = new Random();
		QwinPlayer_t[] qPlayers = new QwinPlayer_t[2];
		qPlayers[0] = new QwinPlayerNN2Test(new Random(init.nextLong()));
		qPlayers[1] = new QwinPlayerExpertETest2(new Random(init.nextLong()));
		int[][] scores = simulateMatches(qPlayers, numberOfIterations, true);
		double[][] res = new double[qPlayers.length][3];
		double draws = 0;
		for (int i = 0; i < numberOfIterations; i++) {
			int winnerId = 0;
			int winScore = scores[0][i];
			boolean draw = true;
			for (int k = 1; k < qPlayers.length; k++) {
				if (scores[k][i] != winScore)
					draw = false;
				if (scores[k][i] > winScore) {
					winScore = scores[k][i];
					winnerId = k;
				}
			}
			if (draw)
				draws++;
			else
				res[winnerId][2]++;
		}
		for (int k = 0; k < qPlayers.length; k++) {
			System.out.println("" + (k + 1) + ". Player (" + qPlayers[k].getName() + ")");
			System.out.println("mean\tstd\twinrate");
			for (int i = 0; i < numberOfIterations; i++) {
				res[k][0] += scores[k][i];
			}
			res[k][0] = res[k][0] / numberOfIterations;
			for (int i = 0; i < numberOfIterations; i++) {
				res[k][1] += Math.pow(scores[k][i] - res[k][0], 2);
			}
			res[k][1] = Math.sqrt(res[k][1] / (numberOfIterations - 1)); // -1 because of Bessel's correction
			res[k][2] = res[k][2] / numberOfIterations;
			System.out.println(res[k][0] + "\t" + res[k][1] + "\t" + res[k][2]);
			System.out.println();
		}
		System.out.println("draw-rate\t" + draws / numberOfIterations);
	}

	public static void scoreDistribution() {
		int numberOfIterations = 1000000;
		Random init = new Random();
		QwinPlayer_t[] qPlayers = new QwinPlayer_t[2];
		qPlayers[0] = new QwinPlayerExpertETest(new Random(init.nextLong()));
		qPlayers[1] = new QwinPlayerExpertETest2(new Random(init.nextLong()));
		int[][] scores = simulateMatches(qPlayers, numberOfIterations, true);
		HashMap<Integer, Integer> scoreCountP1 = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> scoreCountP2 = new HashMap<Integer, Integer>();
		for (int i = 0; i < numberOfIterations; i++) {
			int p1score = scores[0][i];
			if (!scoreCountP1.containsKey(p1score)) {
				scoreCountP1.put(p1score, 1);
			} else {
				scoreCountP1.put(p1score, scoreCountP1.get(p1score) + 1);
			}
			int p2score = scores[1][i];
			if (!scoreCountP2.containsKey(p2score)) {
				scoreCountP2.put(p2score, 1);
			} else {
				scoreCountP2.put(p2score, scoreCountP2.get(p2score) + 1);
			}

		}
		System.out.println("first player (" + qPlayers[0].getName() + ")score distribution");
		TreeMap<Integer, Integer> sortedp1 = new TreeMap<>();
		sortedp1.putAll(scoreCountP1);
		for (Map.Entry<Integer, Integer> count : sortedp1.entrySet()) {
			System.out.println(count.getKey() + "\t" + count.getValue());
		}
		System.out.println();
		System.out.println("second player (" + qPlayers[1].getName() + ") score distribution");
		TreeMap<Integer, Integer> sortedp2 = new TreeMap<>();
		sortedp2.putAll(scoreCountP2);
		for (Map.Entry<Integer, Integer> count : sortedp2.entrySet()) {
			System.out.println(count.getKey() + "\t" + count.getValue());
		}
	}

}
