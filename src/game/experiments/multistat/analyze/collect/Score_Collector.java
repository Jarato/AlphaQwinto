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
	public void processPostTurn(TurnData turn, PlayerData[] players, QwinPaper[] papers) {
		
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

	@Override
	public void processPreTurn(TurnData turn, PlayerData[] players, QwinPaper[] papers) {
	}

	@Override
	public String printAllStats() {
		String str = "Score - collector\naverage score of all players:\t"+average_score+"\naverage score of individual players:\n";
		for (int i = 0; i < avg_score_player.length; i++) {
			str = str+"player "+i+":\t"+avg_score_player[i]+"\n";
		}
		str = str+"score distribution\nscore";
		for (int i = 0; i < score_distribution_player.length; i++) {
			str = str+"\tplayer "+i;
		}
		str = str+"\n";
		for (int j = 0; j < score_distribution_player[0].length; j++) {
			str = str+(j-20);
			for (int i = 0; i < score_distribution_player.length; i++) {
				str = str+"\t"+score_distribution_player[i][j];
			}
			str = str+"\n";
		}
		return str;
	}

}
