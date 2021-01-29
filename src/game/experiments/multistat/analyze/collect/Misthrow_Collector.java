package game.experiments.multistat.analyze.collect;

import game.QwinPaper;
import game.experiments.multistat.analyze.MatchStatCollecting;
import game.experiments.multistat.data.MatchData;
import game.experiments.multistat.data.PlayerData;
import game.experiments.multistat.data.TurnData;

public class Misthrow_Collector implements MatchStatCollecting{
	private double[] misthrows_player;
	private double avg_misthrows;
	
	public double[] getAvgMisthrows_player() {
		return misthrows_player;
	}
	
	public double getAvgerageMisthrows() {
		return avg_misthrows;
	}
	
	@Override
	public void preMatchSetup(MatchData match) {
		if (misthrows_player == null) {
			misthrows_player = new double[match.players.length];
		}
	}

	@Override
	public void processPostTurn(TurnData turn, PlayerData[] players, QwinPaper[] papers) {
		
	}

	@Override
	public void postMatchCalculation(MatchData match, QwinPaper[] papers) {
		for (int i = 0; i < papers.length; i++) {
			misthrows_player[i] += papers[i].getNumberOfMisthrows();
		}
	}

	@Override
	public void averageOverMatches(int number_matches) {
		for (int i = 0; i < misthrows_player.length; i++) {
			misthrows_player[i] /= number_matches;
			avg_misthrows += misthrows_player[i];
		}
		avg_misthrows /= misthrows_player.length;
	}

	@Override
	public void processPreTurn(TurnData turn, PlayerData[] players, QwinPaper[] papers) {
	}

	@Override
	public String printAllStats() {
		String str = "Misthrow - collector\naverage misthrows of all players:\t"+avg_misthrows+"\naverage misthrows of individual players\n";
		for (int i = 0; i < misthrows_player.length; i++) {
			str = str+"player "+i+":\t"+misthrows_player[i]+"\n";
		}
		return str;
	}

}
