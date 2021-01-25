package game.experiments.multistat.analyze.collect;

import game.QwinPaper;
import game.experiments.multistat.analyze.MatchStatCollecting;
import game.experiments.multistat.data.MatchData;
import game.experiments.multistat.data.PlayerData;
import game.experiments.multistat.data.TurnData;

public class GameEndCondition_Collector implements MatchStatCollecting {
	private double gamesEnd_misthrow;
	private double gamesEnd_2fulllanes;
	
	public double getProportionGameEnd_misthrow() {
		return gamesEnd_misthrow;
	}
	
	public double getProportionGameEnd_2fulllanes() {
		return gamesEnd_2fulllanes;
	}
	
	@Override
	public void preMatchSetup(MatchData match) {
	}

	@Override
	public void processPostTurn(TurnData turn, PlayerData[] players, QwinPaper[] papers) {
	}

	@Override
	public void postMatchCalculation(MatchData match, QwinPaper[] papers) {
		for (int i = 0; i < papers.length; i++) {
			if (papers[i].getNumberOfFullLanes() == 2) gamesEnd_2fulllanes += 1.;
			if (papers[i].getNumberOfMisthrows() == 4) gamesEnd_misthrow += 1.;
		}
	}

	@Override
	public void averageOverMatches(int number_matches) {
		gamesEnd_2fulllanes /= number_matches;
		gamesEnd_misthrow /= number_matches;
	}

	@Override
	public void processPreTurn(TurnData turn, PlayerData[] players, QwinPaper[] papers) {
		// TODO Auto-generated method stub
		
	}

}
