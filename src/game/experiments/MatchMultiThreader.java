package game.experiments;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import game.QwintoMatchBP;
import game.qwplayer.dev.QwinPlayerNN2;
import model.FeedForwardNetwork;
import pdf.util.Pair;

public class MatchMultiThreader extends Thread {
	public static int count = 0;
	public static ArrayList<Pair<double[], Integer>> betterDiceThrowHistory = new ArrayList<Pair<double[], Integer>>();
	public static ArrayList<Pair<double[], Integer>> betterActionHistory = new ArrayList<Pair<double[], Integer>>();
	public static ArrayList<Pair<double[], Integer>> worseDiceThrowHistory = new ArrayList<Pair<double[], Integer>>();
	public static ArrayList<Pair<double[], Integer>> worseActionHistory = new ArrayList<Pair<double[], Integer>>();
	public static FeedForwardNetwork diceThrowNet;
	public static FeedForwardNetwork actionListNet;
	public static double randomPlayPercent;
	public static ReentrantLock lock = new ReentrantLock(true);

	public MatchMultiThreader(String name, ThreadGroup tg) {
		super(tg,name);
	}
	
	public static void reset() {
		lock = new ReentrantLock(true);
		count = 0;
		betterDiceThrowHistory = new ArrayList<Pair<double[], Integer>>();
		betterActionHistory = new ArrayList<Pair<double[], Integer>>();
		worseDiceThrowHistory = new ArrayList<Pair<double[], Integer>>();
		worseActionHistory = new ArrayList<Pair<double[], Integer>>();
	}

	@Override
	public void run() {
		Random init = new Random();
		while (count < 1000) {
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
			boolean noRandomAction = player[0].getRndDiceThrowHistory().isEmpty() && player[0].getRndActionHistory().isEmpty() && player[1].getRndDiceThrowHistory().isEmpty() && player[1].getRndActionHistory().isEmpty();
			
			// DETERMINE, WHO WON
			
			if (!noRandomAction) {
				score[0] = player[0].getPaper().calculateScore();
				score[1] = player[1].getPaper().calculateScore();
				lock.lock();
				try {
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
					count = betterDiceThrowHistory.size();
				} finally {
					lock.unlock();
				}
			}
		}
	}
}
