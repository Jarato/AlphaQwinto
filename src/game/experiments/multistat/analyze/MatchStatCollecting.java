package game.experiments.multistat.analyze;

import game.QwinPaper;
import game.experiments.multistat.data.MatchData;
import game.experiments.multistat.data.PlayerData;
import game.experiments.multistat.data.TurnData;

public interface MatchStatCollecting {
	public abstract void preMatchSetup(MatchData match);
	public abstract void processTurn(TurnData turn, PlayerData[] players, QwinPaper[] papers);
	public abstract void postMatchCalculation(MatchData match, QwinPaper[] papers);
	public abstract void averageOverMatches(int number_matches);
}
