package game.experiments.multistat.analyze.collect;

import game.QwinPaper;
import game.experiments.multistat.analyze.MatchStatCollecting;
import game.experiments.multistat.data.MatchData;
import game.experiments.multistat.data.PlayerData;
import game.experiments.multistat.data.TurnData;

public class GameLength_Collector implements MatchStatCollecting{
	private double avg_gamelength;

	public double getAvgGamelength() {
		return avg_gamelength;
	}
	
	@Override
	public void preMatchSetup(MatchData match) {
	}

	@Override
	public void processTurn(TurnData turn, PlayerData[] players, QwinPaper[] papers) {
	}

	@Override
	public void postMatchCalculation(MatchData match, QwinPaper[] papers) {
		avg_gamelength += match.last_turn.turn_number;
	}

	@Override
	public void averageOverMatches(int number_matches) {
		avg_gamelength /= number_matches;
	}

}
