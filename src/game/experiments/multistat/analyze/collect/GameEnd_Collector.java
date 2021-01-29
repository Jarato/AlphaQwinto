package game.experiments.multistat.analyze.collect;

import game.QwinPaper;
import game.experiments.multistat.analyze.MatchStatCollecting;
import game.experiments.multistat.data.MatchData;
import game.experiments.multistat.data.PlayerData;
import game.experiments.multistat.data.TurnData;

public class GameEnd_Collector implements MatchStatCollecting {
	private double gamesEnd_misthrow;
	private double gamesEnd_2fulllanes;
	private double[] proportion_endconditions_player;
	private double[][] endconditions_player;
	private double[] true_winrate_player;
	private double[] shared_winrate_player;
	
	public double getProportionGameEnd_misthrow() {
		return gamesEnd_misthrow;
	}
	
	public double getProportionGameEnd_2fulllanes() {
		return gamesEnd_2fulllanes;
	}
	
	@Override
	public void preMatchSetup(MatchData match) {
		if (proportion_endconditions_player == null) {
			proportion_endconditions_player = new double[match.players.length];
			endconditions_player = new double[match.players.length][2];
			true_winrate_player = new double[match.players.length];
			shared_winrate_player = new double[match.players.length];
		}
	}

	@Override
	public void processPostTurn(TurnData turn, PlayerData[] players, QwinPaper[] papers) {
	}

	@Override
	public void postMatchCalculation(MatchData match, QwinPaper[] papers) {
		boolean full_lines = false;
		boolean misthrow = false;
		int max_score = -20;
		
		for (int i = 0; i < papers.length; i++) {
			int score = papers[i].calculateScore();
			max_score = Math.max(max_score, score);
			if (papers[i].getNumberOfMisthrows() == 4) {
				proportion_endconditions_player[i] += 1.;
				endconditions_player[i][0] += 1.;
				misthrow = true;
			}
			if (papers[i].getNumberOfFullLanes() == 2) {
				proportion_endconditions_player[i] += 1.;
				endconditions_player[i][1] += 1.;
				full_lines = true;
			}
		}
		int count_max = 0;
		for (int i = 0; i < papers.length; i++) {
			if (papers[i].calculateScore() == max_score) count_max++;
		}
		for (int i = 0; i < papers.length; i++) {
			if (papers[i].calculateScore() == max_score) {
				if (count_max > 1) {
					shared_winrate_player[i] += 1.;
				} else {
					true_winrate_player[i] += 1.;
				}
			}
		}
		if (full_lines) gamesEnd_2fulllanes += 1.;
		if (misthrow) gamesEnd_misthrow += 1.;
	}

	@Override
	public void averageOverMatches(int number_matches) {
		gamesEnd_2fulllanes /= number_matches;
		gamesEnd_misthrow /= number_matches;
		for (int i = 0; i < proportion_endconditions_player.length; i++) {
			endconditions_player[i][0] /= proportion_endconditions_player[i];
			endconditions_player[i][1] /= proportion_endconditions_player[i];
		}
		for (int i = 0; i < proportion_endconditions_player.length; i++) {
			proportion_endconditions_player[i] /= number_matches;
			true_winrate_player[i] /= number_matches;
			shared_winrate_player[i] /= number_matches;
		}
	}

	@Override
	public void processPreTurn(TurnData turn, PlayerData[] players, QwinPaper[] papers) {
	}

	@Override
	public String printAllStats() {
		String str = "Game end condition - collector\nproportion of games where a player has 4 misthrows:\t"+gamesEnd_misthrow+"\nproportion of games where a player has 2 full rows:\t"+gamesEnd_2fulllanes+"\nproportion of games where this player has an end condition:\n";
		for (int i = 0; i < proportion_endconditions_player.length; i++) {
			str = str+"player "+i+":\t"+proportion_endconditions_player[i]+"\n";
		}
		str = str+"proportion of end conditions for players where they had an end condition\n";
		for (int i = 0; i < endconditions_player.length; i++) {
			str = str + "player "+i+"\nend condition 4 misthrows:\t"+endconditions_player[i][0];
			str = str + "\nend condition 2 full rows:\t"+endconditions_player[i][1]+"\n";
		}
		str = str+"winrate of individual players\n";
		for (int i = 0; i < true_winrate_player.length; i++) {
			str = str+"player "+i+"\ntrue winrate:\t"+true_winrate_player[i]+"\nshared winrate:\t"+shared_winrate_player[i]+"\n";
		}
		return str;
	}

}
