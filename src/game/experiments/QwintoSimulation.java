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
		//avgCompare();
		//scoreDistribution();
		//simpleMatch();
		QwinPlayerLA_1v1_Backprop_MT();
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
		MatchMultiThreaderNN2.randomPlayPercent = 0.5;
		double regularisation = 0.005;
		int numberOfTestMatches = 2000;
		Random init = new Random();
		QwinPlayerNN2 initPlayer = new QwinPlayerNN2(new Random(init.nextLong()));
		MatchMultiThreaderNN2.diceThrowNet = initPlayer.getDiceThrowNet();
		MatchMultiThreaderNN2.actionListNet = initPlayer.getActionListNet();
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
			Thread th1 = new MatchMultiThreaderNN2("1", tg);
			th1.start();
			Thread th2 = new MatchMultiThreaderNN2("2", tg);
			th2.start();
			Thread th3 = new MatchMultiThreaderNN2("3", tg);
			th3.start();
			Thread th4 = new MatchMultiThreaderNN2("4", tg);
			th4.start();
			QwinPlayer_t[] testP = new QwinPlayer_t[2];
			QwinPlayerNN2 testNN2 = new QwinPlayerNN2(new Random(init.nextLong()));
			testNN2.setDiceThrowNet(MatchMultiThreaderNN2.diceThrowNet.copy());
			testNN2.setActionListNet(MatchMultiThreaderNN2.actionListNet.copy());
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
				for (Pair<double[], Integer> pair : MatchMultiThreaderNN2.betterDiceThrowHistory) {
					double[] targetVector = new double[7];
					targetVector[pair.getY()] = 1;
					double[] dout = MatchMultiThreaderNN2.diceThrowNet.calculateDerivativeOutput(pair.getX(),
							targetVector);
					for (int i = 0; i < dout.length; i++) {
						if (i != pair.getY())
							dout[i] = 0;
					}
					MatchMultiThreaderNN2.diceThrowNet.calculateGradient(dout);
					double[] tempGradient = MatchMultiThreaderNN2.diceThrowNet.getWeightBiasGradient();
					gradient = (gradient == null ? tempGradient : UtilMethods.vectorAddition(gradient, tempGradient));
					n++;
				}

				// DICE THROW WORSE

				for (Pair<double[], Integer> pair : MatchMultiThreaderNN2.worseDiceThrowHistory) {
					double[] targetVector = new double[7];
					targetVector[pair.getY()] = 0;
					double[] dout = MatchMultiThreaderNN2.diceThrowNet.calculateDerivativeOutput(pair.getX(),
							targetVector);
					for (int i = 0; i < dout.length; i++) {
						if (i != pair.getY())
							dout[i] = 0;
					}
					MatchMultiThreaderNN2.diceThrowNet.calculateGradient(dout);
					double[] tempGradient = MatchMultiThreaderNN2.diceThrowNet.getWeightBiasGradient();
					gradient = (gradient == null ? tempGradient : UtilMethods.vectorAddition(gradient, tempGradient));
					n++;
				}
				if (gradient != null) {
					for (int i = 0; i < gradient.length; i++) {
						gradient[i] /= n;
					}
					gradient = learnerdw.adaptGradientVector(gradient);
					MatchMultiThreaderNN2.diceThrowNet.applyWeightsBiasGradient(gradient, regularisation);
				}
				// ACTION LIST
				n = 0;
				gradient = null;
				for (Pair<double[], Integer> pair : MatchMultiThreaderNN2.betterActionHistory) {
					double[] targetVector = new double[28];
					targetVector[pair.getY()] = 1;
					double[] dout = MatchMultiThreaderNN2.actionListNet.calculateDerivativeOutput(pair.getX(),
							targetVector);
					for (int i = 0; i < dout.length; i++) {
						if (i != pair.getY())
							dout[i] = 0;
					}
					MatchMultiThreaderNN2.actionListNet.calculateGradient(dout);
					double[] tempGradient = MatchMultiThreaderNN2.actionListNet.getWeightBiasGradient();
					gradient = (gradient == null ? tempGradient : UtilMethods.vectorAddition(gradient, tempGradient));
					n++;
				}
				// if (gradient != null) {
				// gradient = learnerab.adaptGradientVector(gradient);
				// actionListNet.applyWeightsBiasGradient(gradient);
				// }
				// n = 0;
				// gradient = null;
				for (Pair<double[], Integer> pair : MatchMultiThreaderNN2.worseActionHistory) {
					double[] targetVector = new double[28];
					targetVector[pair.getY()] = 0;
					double[] dout = MatchMultiThreaderNN2.actionListNet.calculateDerivativeOutput(pair.getX(),
							targetVector);
					for (int i = 0; i < dout.length; i++) {
						if (i != pair.getY())
							dout[i] = 0;
					}
					MatchMultiThreaderNN2.actionListNet.calculateGradient(dout);
					double[] tempGradient = MatchMultiThreaderNN2.actionListNet.getWeightBiasGradient();
					gradient = (gradient == null ? tempGradient : UtilMethods.vectorAddition(gradient, tempGradient));
					n++;
				}
				if (gradient != null) {
					for (int i = 0; i < gradient.length; i++) {
						gradient[i] /= n;
					}
					gradient = learneraw.adaptGradientVector(gradient);
					MatchMultiThreaderNN2.actionListNet.applyWeightsBiasGradient(gradient, regularisation);
				}
			}

			MatchMultiThreaderNN2.reset();
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
		qPlayers[0] = new QwinPlayerLA_NNEval(new Random(init.nextLong()));//QwinPlayerLA_NNEval(init,"LANNEVAL2_weights.txt",true);
		qPlayers[1] = new QwinPlayerExpertETest2(init);
		QwintoMatch match = new QwintoMatch(new Random(init.nextLong()), qPlayers);
		match.calculateMatch(true);
	}

	public static void QwinPlayerLA_1v1_Backprop_MT() {
		double regularisation = 0;
		int numTrainingMatches_per_thread = 30;
		int numLearningSteps = 10;
		Random generator = new Random();
		long seed = generator.nextLong();
		System.out.println(seed);
		Random init = new Random(seed);
		QwinPlayerLA_NNEval initPlayer = new QwinPlayerLA_NNEval(new Random(init.nextLong()));

		Learner learner = new VProp85avg();//new MomentumDescent(0.01, 0.9);

		ArrayList<Pair<double[], Integer>> fullPaperScoreHistory = new ArrayList<Pair<double[], Integer>>();
		double bestAvgScore = Double.NEGATIVE_INFINITY;
		int step = 0;
		while (true) {
			// create 2 players with some noise in their network
			FeedForwardNetwork evalNetwork = initPlayer.getEvalNetwork();
			double[] weights_biases_copy = evalNetwork.copyWeightBiasVector();
			// we have 2 players with the same network but each with a bit of noise added
			// we let them play matches and record
			double averageScore = 0;
			// -1 to have one free process
			int numThreads = Runtime.getRuntime().availableProcessors() - 4;
			// for 1 process we fix the number of threads to 1
			if (numThreads < 1)
				numThreads = 1;
			ThreadGroup tg_matches = new ThreadGroup("LANN-Matches");
			MatchMultiThreaderLANN[] threads = new MatchMultiThreaderLANN[numThreads];
			// start the threads
			for (int th_i = 0; th_i < numThreads; th_i++) {
				threads[th_i] = new MatchMultiThreaderLANN("LANN machtes " + th_i, tg_matches,
						numTrainingMatches_per_thread, new Random(init.nextLong()), weights_biases_copy, (step==0?2.:(2./Math.pow(step,0.6))), 5);
				threads[th_i].start();
			}
			// wait for all the threads to finish their matches
			for (int th_i = 0; th_i < numThreads; th_i++) {
				try {
					threads[th_i].join();
					fullPaperScoreHistory.addAll(threads[th_i].getDataHistory());
					averageScore += threads[th_i].getAvgScore();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// the threads are done
			// we calculate the average score for these threads
			averageScore /= numThreads;
			System.out.println("step " + step + "\tavg-score:\t" + averageScore);

			if (averageScore > bestAvgScore) {
				try {
					bestAvgScore = averageScore;
					File paramFile = new File("" + step + "_" + averageScore + "_LANN4.txt");
					FileWriter writer = new FileWriter(paramFile);
					double[] weightsBiasVector = evalNetwork.copyWeightBiasVector();
					for (int i = 0; i < weightsBiasVector.length; i++) {
						writer.write(weightsBiasVector[i] + "\n");
					}
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			int numData = fullPaperScoreHistory.size();
			// System.out.println("full data length\t"+numData);
			// calculate splitting parameters
			int split_base = numData / numThreads;
			// the rest of the datapoints
			int split_rest = numData % numThreads;
			// Multi-Threading for Gradient calculation
			GradientMultiThreader[] threads_gr = new GradientMultiThreader[numThreads];
			Thread[] gradient_threads = new Thread[threads_gr.length];
			// this index indicates the position where we're currently copying the data into
			// the splitted parts for the threads
			int data_copy_pos = 0;
			// start the threads
			for (int th_i = 0; th_i < numThreads; th_i++) {
				// split the data for the different threads
				double[][][] splittedData = new double[split_base + (split_rest > 0 ? 1 : 0)][2][];
				// System.out.println(splittedData.length);
				// decrease the split rest counter, because we distributed one of the rest
				split_rest--;
				for (int split_i = 0; split_i < splittedData.length; split_i++) {
					Pair<double[], Integer> data_point = fullPaperScoreHistory.get(data_copy_pos);
					// increase the data copy position because we just copy the previous data point
					data_copy_pos++;
					splittedData[split_i][0] = data_point.getX();
					splittedData[split_i][1] = new double[] { data_point.getY() };
				}
				// start the thread
				threads_gr[th_i] = new GradientMultiThreader(splittedData);
			}
			// LEARNING
			for (int lstep = 0; lstep < numLearningSteps; lstep++) {
				// initiate
				double loss = 0;
				double[] gradient = new double[evalNetwork.getNumberWeightsBiases()];
				// start the threads
				for (int th_i = 0; th_i < numThreads; th_i++) {
					// we copy the network because we update the weights and bias vectors inside
					// every learning step
					threads_gr[th_i].reset(evalNetwork.copy());
					gradient_threads[th_i] = new Thread(threads_gr[th_i]);
					gradient_threads[th_i].start();
				}
				for (int th_i = 0; th_i < numThreads; th_i++) {
					try {
						gradient_threads[th_i].join();
						loss += threads_gr[th_i].getLoss();
						gradient = UtilMethods.vectorAddition(gradient, threads_gr[th_i].getGradient());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				// the threads have calculated the gradient
				loss /= numThreads;
				System.out.println((lstep + 1) + ".loss\t" + UtilMethods.roundTo(loss, 2));
				// calculate the average of the gradient
				for (int i = 0; i < gradient.length; i++) {
					gradient[i] /= numThreads;
				}
				// apply this gradient vector via the learner
				evalNetwork.applyWeightsBiasGradient(learner.adaptGradientVector(gradient), regularisation);
			}
			// clear the data because we get new and hopefully better decision making and
			// therefore all the scores recorded are invalid
			fullPaperScoreHistory.clear();
			step++;
		}

	}

	public static void QwinPlayerLA_1v1_Backprop() {
		double regularisation = 0;
		int numTrainingMatches = 250;
		int numLearningSteps = 20;
		Random generator = new Random();
		long seed = generator.nextLong();
		System.out.println(seed);
		Random init = new Random(seed);
		QwinPlayerLA_NNEval initPlayer = new QwinPlayerLA_NNEval(new Random(init.nextLong()));

		Learner learner = new GradientDescent(0.01, 1.0);

		ArrayList<Pair<double[], Integer>> fullPaperScoreHistory = new ArrayList<Pair<double[], Integer>>();

		int step = 0;
		while (true) {
			step++;
			// create 2 players with some noise in their network
			FeedForwardNetwork evalNetwork = initPlayer.getEvalNetwork();
			double[] weights_biases_copy = evalNetwork.copyWeightBiasVector();

			// we have 2 players with the same network but each with a bit of noise added
			// we let them play matches and record
			double averageScore = 0;
			for (int matchCounter = 0; matchCounter < numTrainingMatches; matchCounter++) {
				// 2 different noised weight-vectors for 2 players
				double[][] noisedWBvecs = new double[2][weights_biases_copy.length];
				// add some noise to the weights and biases
				for (int i = 0; i < noisedWBvecs.length; i++) {
					for (int ii = 0; ii < weights_biases_copy.length; ii++) {
						noisedWBvecs[i][ii] = weights_biases_copy[ii] + init.nextGaussian() * 0.2;
					}
				}

				QwinPlayerLA_NNEval[] players = new QwinPlayerLA_NNEval[2];

				for (int k = 0; k < players.length; k++) {
					players[k] = new QwinPlayerLA_NNEval(new Random(init.nextLong()));
					players[k].getEvalNetwork().applyWeightsBiasesVector(noisedWBvecs[k]);
				}
				QwintoMatch match = new QwintoMatch(new Random(init.nextLong()), players);
				match.calculateMatch(false);
				// add the average score between the players
				double playeravgScore = 0;
				for (int k = 0; k < players.length; k++) {
					playeravgScore += players[k].getScore();
				}
				averageScore += playeravgScore / players.length;
				// collect the players histories
				for (int pc = 0; pc < players.length; pc++) {
					fullPaperScoreHistory.addAll(players[pc].getPaperScoreHistory());
					players[pc].reset();
				}
				if (matchCounter % (numTrainingMatches / 10) == 0)
					System.out.print("" + ((matchCounter * 100) / numTrainingMatches) + "%\t");
				// System.out.println(""+(matchCounter+1)+"\t"+fullPaperScoreHistory.size());
			}
			System.out.println("100%");
			averageScore /= numTrainingMatches;
			System.out.println("Step: " + step + "\tAverage Score of the players: " + averageScore);
			// LEARNING
			for (int lstep = 0; lstep < numLearningSteps; lstep++) {
				// how many collected datapoints do we have?
				int numData = fullPaperScoreHistory.size();
				if (numData == 0)
					throw new IllegalArgumentException();
				double[] gradient = null;
				double loss = 0;
				for (Pair<double[], Integer> dat : fullPaperScoreHistory) {
					// the target is an array, so we put this one value (the score) into an array
					double[] targetValue = new double[] { dat.getY() }; // dat.getY()
					double[] derivOut = evalNetwork.calculateDerivativeOutput(dat.getX(), targetValue);
					loss += derivOut[0] * derivOut[0];
					evalNetwork.calculateGradient(derivOut);
					// this is the gradient for this one datapoint
					double[] tempGradient = evalNetwork.getWeightBiasGradient();
					// we add the gradients for all the datapoints in the gradient
					gradient = (gradient == null ? tempGradient : UtilMethods.vectorAddition(gradient, tempGradient));
					if (Double.isNaN(gradient[0]))
						throw new IllegalArgumentException();
				}
				loss = loss / numData;
				System.out.println("loss:\t" + loss);
				// calculate the average of the gradient
				if (gradient != null) {
					for (int i = 0; i < gradient.length; i++) {
						gradient[i] /= numData;
						if (Double.isNaN(gradient[i]))
							throw new IllegalArgumentException();
					}
					// apply this gradient vector via the learner
					evalNetwork.applyWeightsBiasGradient(learner.adaptGradientVector(gradient), regularisation);
				}

			}
			// clear the data because we get new and hopefully better decision making and
			// therefore all the scores recorded are invalid
			fullPaperScoreHistory.clear();
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
				// exception handling is left as an exercise for the reader
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
		int numberOfIterations = 1000;
		Random init = new Random();
		QwinPlayer_t[] qPlayers = new QwinPlayer_t[2];
		qPlayers[0] = new QwinPlayerLA_NNEval(new Random(init.nextLong()), "LANNEVAL_weights.txt", false);
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
		int numberOfIterations = 1000;
		Random init = new Random();
		QwinPlayer_t[] qPlayers = new QwinPlayer_t[2];
		qPlayers[0] = new QwinPlayerLA_NNEval(new Random(init.nextLong()), "LANNEVAL_weights.txt", false);
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
