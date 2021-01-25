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

import game.QwinPaper;
import game.QwintoMatch;
import game.QwintoMatchBP;
import game.experiments.multistat.MultiMatchThread;
import game.experiments.multistat.analyze.RawDataAnalyzer;
import game.experiments.multistat.analyze.collect.GameLength_Collector;
import game.experiments.multistat.analyze.collect.Score_Collector;
import game.experiments.multistat.analyze.collect.TDLANN9_Collector;
import game.experiments.multistat.data.RawData;
import game.experiments.multistat.matchgen.LANN_Gen;
import game.experiments.multistat.matchgen.Match_Generator;
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
		//simpleMatch();
		test_multimatches();
		// QwinPlayerLA_1v1_Backprop_MT();
	}

	public static void simpleMatch() {
		Random init = new Random();
		RawData raw_data = new RawData();
		QwinPlayer_t[] qPlayers = new QwinPlayer_t[2];
		qPlayers[0] = new QwinPlayerLA_NNEval(init, "LANNEVAL8_weights.txt");// new QwinPlayerLA_NNEval(init);
		((QwinPlayerLA_NNEval) qPlayers[0]).setNoiseLevel(.5);
		qPlayers[1] = new QwinPlayerLA_NNEval(init, "LANNEVAL8_weights.txt");
		((QwinPlayerLA_NNEval) qPlayers[1]).setNoiseLevel(1.);
		// qPlayers[2] = new QwinPlayerNN2Test(init);
		// qPlayers[3] = new QwinPlayerNNTest(init);
		// qPlayers[4] = new QwinPlayerNNTestOld(init);
		QwintoMatch match = new QwintoMatch(new Random(init.nextLong()), qPlayers);
		match.setRawData(raw_data.generateBlankMatchData());
		match.calculateMatch();
		RawDataAnalyzer.printMatch(raw_data.matches.get(0));
		TDLANN9_Collector td_col = new TDLANN9_Collector();
		Score_Collector avgscore_col = new Score_Collector();
		RawDataAnalyzer.extractDataFromSimulations(raw_data, td_col, avgscore_col);
		System.out.println("average score\t" + avgscore_col.getAverageScore());
	}
	
	public static RawData multithread_matches(int number_of_matches, Match_Generator match_gen) {
		int numThreads = Runtime.getRuntime().availableProcessors();
		int usedThreads = numThreads - 4;
		if (usedThreads < 1) usedThreads = 1;
		RawData[] data_raw = new RawData[usedThreads];
		MultiMatchThread[] threads = new MultiMatchThread[usedThreads];
		int split_base = number_of_matches / numThreads;
		// the rest of the datapoints
		int split_rest = number_of_matches % numThreads;
		for (int i = 0; i < usedThreads; i++) {
			data_raw[i] = new RawData();
			threads[i] = new MultiMatchThread(split_base + (split_rest > 0 ? 1 : 0), match_gen);
			threads[i].setRawData(data_raw[i]);
			split_rest--;
			threads[i].start();
		}
		for (int i = 0; i < usedThreads; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return RawData.collectAllData(data_raw);
	}
	
	public static void test_multimatches() {
		Random init = new Random(42);
		QwinPlayerLA_NNEval init_player  =new QwinPlayerLA_NNEval(init, "LANNEVAL8_weights.txt");
		RawData raw_data = multithread_matches(2, new LANN_Gen(init, 0.0, init_player.getEvalNetwork().copyWeightBiasVector(), 2));
		Score_Collector score_col = new Score_Collector();
		GameLength_Collector gamelength_col = new GameLength_Collector();
		RawDataAnalyzer.printMatch(raw_data.matches.get(0));
		RawDataAnalyzer.printMatch(raw_data.matches.get(1));
		RawDataAnalyzer.extractDataFromSimulations(raw_data, score_col, gamelength_col);
		System.out.println("test");
	}

	public static void QwinPlayerLA_1v1_Backprop_MT() {
		double regularisation = 0;
		int numTrainingMatches_per_thread = 100;
		int numOfMinLearningSteps = 10;
		// int highestAcceptedLoss = 80;
		double VERSION = 8.0;
		Random generator = new Random();
		long seed = generator.nextLong();
		System.out.println(seed);
		Random init = new Random(seed);
		QwinPlayerLA_NNEval initPlayer = new QwinPlayerLA_NNEval(init, "LANNEVAL8_weights.txt");// new
																								// QwinPlayerLA_NNEval(new
																								// Random(init.nextLong()));//
																								// new
																								// QwinPlayerLA_NNEval(init,"LANNEVAL7_weights.txt",false);
																								//

		Learner learner = new VProp85avg();// new MomentumDescent(0.01, 0.9);

		ArrayList<Pair<double[], Double>> fullPaperScoreHistory = new ArrayList<Pair<double[], Double>>();

		// STEP START
		int step = 830;
		if (step == 0) {
			try {
				File statFile = new File("LANN" + VERSION + "_train_statistic.txt");
				FileWriter sWriter = new FileWriter(statFile, true);
				BufferedWriter bw = new BufferedWriter(sWriter);
				bw.write(
						"step\tscore\tgame length\tmisthrows\tfull lanes\tfull pentagon columns\tentered numbers\ttrue reject rate\tnoised decision rate");
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		while (true) {
			// create 2 players with some noise in their network
			FeedForwardNetwork evalNetwork = initPlayer.getEvalNetwork();
			double[] weights_biases_copy = evalNetwork.copyWeightBiasVector();
			// we have 2 players with the same network but each with a bit of noise added
			// we let them play matches and record
			double averageScore = 0;
			double averageGameLength = 0;
			double averageMisthrows = 0;
			double averageFullLanes = 0;
			double averageEnteredNumbers = 0;
			double avgProportionReject = 0;
			double avgFullPentagonCols = 0;
			double avgPropNoisedActions = 0;
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
						numTrainingMatches_per_thread, new Random(init.nextLong()), weights_biases_copy,
						(step == 0 ? 10. : (10. / Math.pow(step, .5))), 5);
				threads[th_i].start();
			}

			// avg. p. noised actions/p: 0.0077001116900273945 with 20/pow(1)
			// avg. p. noised actions/p: 0.03820185379679242 with 20/pow(0.75)

			// wait for all the threads to finish their matches
			for (int th_i = 0; th_i < numThreads; th_i++) {
				try {
					threads[th_i].join();
					fullPaperScoreHistory.addAll(threads[th_i].getDataHistory());
					averageScore += threads[th_i].getAvgScore();
					averageGameLength += threads[th_i].getAvgGameLength();
					averageMisthrows += threads[th_i].getAvgMisthrowsAtEnd();
					averageFullLanes += threads[th_i].getNumFullLanesAtEnd();
					averageEnteredNumbers += threads[th_i].getNumNumbersAtEnd();
					avgProportionReject += threads[th_i].getAvgPropReject();
					avgFullPentagonCols += threads[th_i].getAvgFullPentagonCols();
					avgPropNoisedActions += threads[th_i].getAvgPropNoisedActions();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// the threads are done
			// we calculate the average score for these threads
			averageScore /= numThreads;
			averageGameLength /= numThreads;
			averageMisthrows /= numThreads;
			averageFullLanes /= numThreads;
			averageEnteredNumbers /= numThreads;
			avgProportionReject /= numThreads;
			avgFullPentagonCols /= numThreads;
			avgPropNoisedActions /= numThreads;
			System.out.println("step " + step + "\navg. score:\t\t\t" + averageScore);
			System.out.println("avg. game length:\t\t" + averageGameLength);
			System.out.println("avg. misthrows/player:\t\t" + averageMisthrows);
			System.out.println("avg. full-lanes/player:\t\t" + averageFullLanes);
			System.out.println("avg. entered-nums/player:\t" + averageEnteredNumbers);
			System.out.println("avg. p. true rej./player:\t" + avgProportionReject);
			System.out.println("avg. f. pentagons/player:\t" + avgFullPentagonCols);
			System.out.println("number of training data:\t" + fullPaperScoreHistory.size());
			System.out.println("avg. p. noised actions/p:\t" + avgPropNoisedActions);
			try {
				File paramFile = new File("" + step + "_" + averageScore + "_LANN" + VERSION + ".txt");
				FileWriter pWriter = new FileWriter(paramFile);
				double[] weightsBiasVector = evalNetwork.copyWeightBiasVector();
				for (int i = 0; i < weightsBiasVector.length; i++) {
					pWriter.write(weightsBiasVector[i] + "\n");
				}
				pWriter.close();

				File statFile = new File("LANN" + VERSION + "_train_statistic.txt");
				FileWriter sWriter = new FileWriter(statFile, true);
				BufferedWriter bw = new BufferedWriter(sWriter);
				bw.write("\n" + step + "\t" + averageScore + "\t" + averageGameLength + "\t" + averageMisthrows + "\t"
						+ averageFullLanes + "\t" + avgFullPentagonCols + "\t" + averageEnteredNumbers + "\t"
						+ avgProportionReject + "\t" + avgPropNoisedActions);
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
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
					Pair<double[], Double> data_point = fullPaperScoreHistory.get(data_copy_pos);
					// increase the data copy position because we just copy the previous data point
					data_copy_pos++;
					splittedData[split_i][0] = data_point.getX();
					splittedData[split_i][1] = new double[] { data_point.getY() };
				}
				// start the thread
				threads_gr[th_i] = new GradientMultiThreader(splittedData);
			}
			// LEARNING
			int lstep = 0;
			// double currentLoss = Double.MAX_VALUE;
			while (lstep < numOfMinLearningSteps) {
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
//				currentLoss = loss;
				System.out.println((lstep + 1) + ".loss\t" + loss);
				// calculate the average of the gradient
				for (int i = 0; i < gradient.length; i++) {
					gradient[i] /= numThreads;
				}
				// apply this gradient vector via the learner
				evalNetwork.applyWeightsBiasGradient(learner.adaptGradientVector(gradient), regularisation);
				lstep++;
			}
			// clear the data because we get new and hopefully better decision making and
			// therefore all the scores recorded are invalid
			fullPaperScoreHistory.clear();
			step++;
		}

	}

}
