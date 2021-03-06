package game.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import game.QwinPaper;
import game.QwintoMatch;
import game.QwintoMatchBP;
import game.experiments.multistat.MultiMatchThread;
import game.experiments.multistat.MultiMatchThread.MatchPresetSingleNumber;
import game.experiments.multistat.analyze.RawDataAnalyzer;
import game.experiments.multistat.analyze.collect.DeadCells_Collector;
import game.experiments.multistat.analyze.collect.EnteredNumbers_Collector;
import game.experiments.multistat.analyze.collect.FullRowsColumns_Collector;
import game.experiments.multistat.analyze.collect.GameEnd_Collector;
import game.experiments.multistat.analyze.collect.GameLength_Collector;
import game.experiments.multistat.analyze.collect.Misthrow_Collector;
import game.experiments.multistat.analyze.collect.NoisedDecision_Collector;
import game.experiments.multistat.analyze.collect.Reject_Collector;
import game.experiments.multistat.analyze.collect.Score_Collector;
import game.experiments.multistat.analyze.collect.TDLANN10_Collector;
import game.experiments.multistat.analyze.collect.TDLANN11_Collector;
import game.experiments.multistat.analyze.collect.TDLANN12_Collector;
import game.experiments.multistat.analyze.collect.TDLANN9_Collector;
import game.experiments.multistat.data.RawData;
import game.experiments.multistat.matchgen.LANN_Gen;
import game.experiments.multistat.matchgen.Match_Generator;
import game.experiments.multistat.matchgen.Test_Gen;
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
		test_multimatches();
		//QwinPlayerLA_1v1_Backprop_new();
	}
	
	public static RawData multithread_matches(int number_of_matches, Match_Generator match_gen, boolean single_preset_matches) {
		int numThreads = Runtime.getRuntime().availableProcessors();
		int usedThreads = numThreads - 1;
		if (usedThreads < 1) usedThreads = 1;
		RawData[] data_raw = new RawData[usedThreads];
		MultiMatchThread[] threads = new MultiMatchThread[usedThreads];
		// preset games
		List<MultiMatchThread.MatchPresetSingleNumber> presets = new ArrayList<MultiMatchThread.MatchPresetSingleNumber>();
		if (single_preset_matches) {
			for (int color = 0; color < 3; color++) {
				for (int pos = 0; pos < 9; pos++) {
					for (int number = 1; number < 19; number++) {
						presets.add(new MultiMatchThread.MatchPresetSingleNumber(color, pos, number));
					}
				}
			}
		}
		//
		int split_base = number_of_matches / usedThreads;
		// the rest of the datapoints
		int split_rest = number_of_matches % usedThreads;
		// splitting the preset matches
		int pres_match_num = presets.size();
		int pres_split_base = pres_match_num / usedThreads;
		int pres_split_rest = pres_match_num % usedThreads;
		for (int i = 0; i < usedThreads; i++) {
			data_raw[i] = new RawData();
			// splitting the preset matches
			List<MultiMatchThread.MatchPresetSingleNumber> pres_part = presets.subList((i==0?0:pres_split_rest)+pres_split_base*i, pres_split_rest+pres_split_base*(i+1));
			threads[i] = new MultiMatchThread(i, split_base + (split_rest > 0 ? 1 : 0), (single_preset_matches?pres_part:null), match_gen);
			threads[i].setRawData(data_raw[i]);
			split_rest--;
			threads[i].start();
		}
		for (int i = 0; i < usedThreads; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return RawData.collectAllData(data_raw);
	}
	
	public static void test_multimatches() {
		Random init = new Random();
		//QwinPlayerLA_NNEval init_player = new QwinPlayerLA_NNEval(init,10, "LANNEVAL10_weights.txt");
		Match_Generator gen = new Test_Gen(init);//new LANN_Gen(init, 0., init_player.getEvalNetwork().copyWeightBiasVector(), 5);
		RawData raw_data = multithread_matches(50000, gen, false);
		System.out.println("number of games recorded: "+raw_data.matches.size());
		Score_Collector score_col = new Score_Collector();
		GameLength_Collector gamelength_col = new GameLength_Collector();
		GameEnd_Collector gameend_col = new GameEnd_Collector();
		Misthrow_Collector misthrow_col = new Misthrow_Collector();
		FullRowsColumns_Collector rowcolumn_col = new FullRowsColumns_Collector();
		NoisedDecision_Collector noised_col = new NoisedDecision_Collector();
		DeadCells_Collector deadceall_col = new DeadCells_Collector();
		EnteredNumbers_Collector number_col = new EnteredNumbers_Collector();
		Reject_Collector reject_col = new Reject_Collector();
		RawDataAnalyzer.printMatch(raw_data.matches.get(0));
		RawDataAnalyzer.extractDataFromSimulations(raw_data, score_col, gamelength_col, gameend_col, number_col, deadceall_col, misthrow_col, rowcolumn_col, noised_col, reject_col);
		System.out.println(gamelength_col.printAllStats()+"\n"+gameend_col.printAllStats()+"\n"+score_col.printAllStats()+"\n"+misthrow_col.printAllStats()+"\n"+rowcolumn_col.printAllStats()+"\n"+number_col.printAllStats()+"\n"+deadceall_col.printAllStats()+"\n"+reject_col.printAllStats());
	}
	
	public static void QwinPlayerLA_1v1_Backprop_new() {
		double regularisation = 0.1;
		int numTrainingMatches = 500;
		int numOfMinLearningSteps = 3;
		// int highestAcceptedLoss = 80;
		int VERSION = 13;
		Random generator = new Random();
		long seed = generator.nextLong();
		System.out.println(seed);
		Random init = new Random(seed);
		QwinPlayerLA_NNEval initPlayer = new QwinPlayerLA_NNEval(new Random(init.nextLong()), VERSION);//, "LANNEVAL12_weights.txt");
		double step_size = 0.01;

		// STEP START
		int step = 0;
		if (step == 0) {
			try {
				File statFile = new File("LANN" + VERSION + "_train_statistic.txt");
				FileWriter sWriter = new FileWriter(statFile, true);
				BufferedWriter bw = new BufferedWriter(sWriter);
				bw.write(
						"step\tscore\tgame length\tmisthrows\tfull lanes\tfull pentagon columns\tentered numbers\tdead cells\ttrue reject rate\tnoised decision rate\tgames ended w. full lanes");
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
			RawData raw_data = multithread_matches(numTrainingMatches, new LANN_Gen(new Random(init.nextLong()), 1, weights_biases_copy, 5, 13), false);
			System.out.println("num matches = "+raw_data.matches.size());
			Score_Collector score_col = new Score_Collector();
			DeadCells_Collector deadcell_col = new DeadCells_Collector();
			GameLength_Collector gamelength_col = new GameLength_Collector();
			GameEnd_Collector gameend_col = new GameEnd_Collector();
			Misthrow_Collector misthrow_col = new Misthrow_Collector();
			FullRowsColumns_Collector rowcolumn_col = new FullRowsColumns_Collector();
			EnteredNumbers_Collector numbers_col = new EnteredNumbers_Collector();
			NoisedDecision_Collector noised_col = new NoisedDecision_Collector();
			Reject_Collector reject_col = new Reject_Collector();
			TDLANN12_Collector train_data_col = new TDLANN12_Collector();
			RawDataAnalyzer.extractDataFromSimulations(raw_data, numbers_col, score_col, gamelength_col, gameend_col, misthrow_col, rowcolumn_col, deadcell_col, noised_col, reject_col, train_data_col);
			
			System.out.println("step " + step + "\navg. score:\t\t\t" + score_col.getAverageScore());
			System.out.println("avg. game length:\t\t" + gamelength_col.getAvgGamelength());
			System.out.println("games ended w. full lanes\t" + gameend_col.getProportionGameEnd_2fulllanes());
			System.out.println("avg. misthrows/player:\t\t" + misthrow_col.getAvgerageMisthrows());
			System.out.println("avg. full-lanes/player:\t\t" + rowcolumn_col.getAvgFullLanes());
			System.out.println("avg. f. pentagons/player:\t" + rowcolumn_col.getAvgFullPentaColumns());
			System.out.println("avg. entered-nums/player:\t" + numbers_col.getAvgEnteredNumbers());
			System.out.println("avg. dead cells/player:\t\t" + deadcell_col.getAvgDeadCells());
			System.out.println("avg. p. true rej./player:\t" + reject_col.getTrueRejectRate());
			System.out.println("avg. p. noised actions/p:\t" + noised_col.getProportionNoisedDecision());
			System.out.println("number of training data:\t" + train_data_col.getTrainingData().size());
			
			try {
				File paramFile = new File("" + step + "_" + score_col.getAverageScore() + "_LANN" + VERSION + ".txt");
				FileWriter pWriter = new FileWriter(paramFile);
				double[] weightsBiasVector = evalNetwork.copyWeightBiasVector();
				for (int i = 0; i < weightsBiasVector.length; i++) {
					pWriter.write(weightsBiasVector[i] + "\n");
				}
				pWriter.close();

				File statFile = new File("LANN" + VERSION + "_train_statistic.txt");
				FileWriter sWriter = new FileWriter(statFile, true);
				BufferedWriter bw = new BufferedWriter(sWriter);
				bw.write("\n" + step + "\t" + score_col.getAverageScore() + "\t" + gamelength_col.getAvgGamelength() + "\t" + misthrow_col.getAvgerageMisthrows() + "\t"
						+ rowcolumn_col.getAvgFullLanes() + "\t" + rowcolumn_col.getAvgFullPentaColumns() + "\t" +  numbers_col.getAvgEnteredNumbers() + "\t" + deadcell_col.getAvgDeadCells()+ "\t"
						+ reject_col.getTrueRejectRate() + "\t" + noised_col.getProportionNoisedDecision() + "\t" + gameend_col.getProportionGameEnd_2fulllanes());
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			int numThreads = 12;
				
			int numData = train_data_col.getTrainingData().size();
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
					Pair<double[], Double> data_point =  train_data_col.getTrainingData().get(data_copy_pos);
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
			double previousLoss = 0;
			Learner learner = new GradientDescent(step_size, 10.);
			System.out.println("lstep size:\t"+step_size);
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
				if (lstep > 0 && loss > previousLoss) {
					step_size *= 0.5;
					learner = new GradientDescent(step_size, 10.);
				}
				previousLoss = loss;
				
//				currentLoss = loss;
				System.out.println((lstep + 1) + ".loss\t" + loss);
				// calculate the average of the gradient
				for (int i = 0; i < gradient.length; i++) {
					gradient[i] /= numThreads;
				}
				// apply this gradient vector via the learner
				evalNetwork.applyWeightsBiasGradient(learner.adaptGradientVector(gradient), regularisation*step_size);
				lstep++;
			}
			//step_size *= 0.98;
			// clear the data because we get new and hopefully better decision making and
			// therefore all the scores recorded are invalid
			step++;
		}

	}
}
