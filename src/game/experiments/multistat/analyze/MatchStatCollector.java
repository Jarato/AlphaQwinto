package game.experiments.multistat.analyze;

import game.QwinPaper;
import game.experiments.multistat.data.MatchData;
import game.experiments.multistat.data.PlayerData;
import game.experiments.multistat.data.TurnData;

public interface MatchStatCollector {
	public abstract void preMatchSetup(MatchData match);
	public abstract void processTurn4Player(TurnData turn, PlayerData player, QwinPaper paper);
	public abstract void postMatchCalculation(MatchData match);
	public abstract void averageOverMatches(int number_matches);
}
