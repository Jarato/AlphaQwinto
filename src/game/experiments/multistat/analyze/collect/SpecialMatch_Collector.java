package game.experiments.multistat.analyze.collect;

import game.QwinPaper;
import game.experiments.multistat.analyze.MatchStatCollecting;
import game.experiments.multistat.data.MatchData;
import game.experiments.multistat.data.PlayerData;
import game.experiments.multistat.data.TurnData;

public class SpecialMatch_Collector implements MatchStatCollecting{
	private MatchData highest_score_match;
	private MatchData lowest_score_match;
	private MatchData max_scorediff_match;
	private MatchData highest_reject_match;
	
	
	@Override
	public void preMatchSetup(MatchData match) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processPreTurn(TurnData turn, PlayerData[] players, QwinPaper[] papers) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processPostTurn(TurnData turn, PlayerData[] players, QwinPaper[] papers) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postMatchCalculation(MatchData match, QwinPaper[] papers) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void averageOverMatches(int number_matches) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String printAllStats() {
		// TODO Auto-generated method stub
		return null;
	}

}
