package game.experiments.multistat.analyze.collect;

import game.QwinPaper;
import game.experiments.multistat.analyze.MatchStatCollecting;
import game.experiments.multistat.data.MatchData;
import game.experiments.multistat.data.PlayerData;
import game.experiments.multistat.data.TurnData;

public class Misthrow_Collector implements MatchStatCollecting{
	private double[] misthrows_player;
	private double avg_misthrows;
	
	
	@Override
	public void preMatchSetup(MatchData match) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processTurn(TurnData turn, PlayerData[] players, QwinPaper[] papers) {
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

}
