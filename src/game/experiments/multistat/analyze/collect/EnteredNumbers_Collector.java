package game.experiments.multistat.analyze.collect;

import game.QwinPaper;
import game.experiments.multistat.analyze.MatchStatCollecting;
import game.experiments.multistat.data.MatchData;
import game.experiments.multistat.data.PlayerData;
import game.experiments.multistat.data.TurnData;

public class EnteredNumbers_Collector implements MatchStatCollecting {
	private double avgEnteredNumbers;
	private double[] avgEnteredNumbers_player;
	
	public double getAvgEnteredNumbers() {
		return avgEnteredNumbers;
	}
	
	public double[] getAvgEnteredNumbers_player() {
		return avgEnteredNumbers_player;
	}
	
	@Override
	public void preMatchSetup(MatchData match) {
		if (avgEnteredNumbers_player == null) {
			avgEnteredNumbers_player = new double[match.players.length];
		}
	}

	@Override
	public void processPostTurn(TurnData turn, PlayerData[] players, QwinPaper[] papers) {
		
	}

	@Override
	public void postMatchCalculation(MatchData match, QwinPaper[] papers) {
		for (int i = 0; i < avgEnteredNumbers_player.length; i++) {
			avgEnteredNumbers_player[i] += papers[i].getNumberOfEnteredNumbers();
		}
	}

	@Override
	public void averageOverMatches(int number_matches) {
		for (int i = 0; i < avgEnteredNumbers_player.length; i++) {
			avgEnteredNumbers_player[i] /= number_matches;
			avgEnteredNumbers += avgEnteredNumbers_player[i];
		}
		avgEnteredNumbers /= avgEnteredNumbers_player.length;
	}

	@Override
	public void processPreTurn(TurnData turn, PlayerData[] players, QwinPaper[] papers) {
		// TODO Auto-generated method stub
		
	}

}
