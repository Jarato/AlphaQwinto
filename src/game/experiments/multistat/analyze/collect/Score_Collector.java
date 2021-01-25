package game.experiments.multistat.analyze.collect;

import game.QwinPaper;
import game.experiments.multistat.analyze.MatchStatCollecting;
import game.experiments.multistat.data.MatchData;
import game.experiments.multistat.data.PlayerData;
import game.experiments.multistat.data.TurnData;

public class Score_Collector implements MatchStatCollecting{
	// 0 = -20
	// 140 = 120
	private int[][] score_distribution_player;
	private double[] avg_score_player;
	private double average_score = 0;
	
	public double getAverageScore() {
		return average_score;
	}
	
	public double[] getAverageScorePlayer() {
		return avg_score_player;
	}
	
	public int[][] getScoreDistributions() {
		return score_distribution_player;
	}
	
	@Override
	public void preMatchSetup(MatchData match) {
		if (avg_score_player == null) {
			avg_score_player = new double[match.players.length];
			score_distribution_player = new int[match.players.length][141];
		}	
	}

	@Override
	public void processTurn(TurnData turn, PlayerData[] players, QwinPaper[] papers) {
		
	}

	@Override
	public void postMatchCalculation(MatchData match, QwinPaper[] papers) {
		for (int i = 0; i < papers.length; i++)  {
			double score_paper = papers[i].calculateScore();
			score_distribution_player[i][(int)score_paper+20]++;
			avg_score_player[i] += score_paper;
		}
	}

	@Override
	public void averageOverMatches(int number_matches) {
		for (int i = 0; i < avg_score_player.length; i++) {
			avg_score_player[i] /= number_matches;
			average_score += avg_score_player[i];
		}
		average_score /= avg_score_player.length;
	}

}
