package game.experiments.multistat.analyze.collect;

import game.QwinPaper;
import game.experiments.multistat.analyze.MatchStatCollecting;
import game.experiments.multistat.data.MatchData;
import game.experiments.multistat.data.PlayerData;
import game.experiments.multistat.data.TurnData;

public class FullRowsColumns_Collector implements MatchStatCollecting {
	private double[] avgFullLanes_player;
	private double avgFullLanes;
	private double[] avgFullPentaColumns_player;
	private double avgFullPentaColumns;
	
	public double getAvgFullLanes() {
		return avgFullLanes;
	}
	
	public double[] getAvgFullLanes_Players() {
		return avgFullLanes_player;
	}
	
	public double getAvgFullPentaColumns() {
		return avgFullPentaColumns;
	}
	
	public double[] getAvgFullPentaColumns_player() {
		return avgFullPentaColumns_player;
	}
	
	@Override
	public void preMatchSetup(MatchData match) {
		if (avgFullLanes_player == null) {
			avgFullLanes_player = new double[match.players.length];
			avgFullPentaColumns_player = new double[match.players.length];
		}
	}

	@Override
	public void processPostTurn(TurnData turn, PlayerData[] players, QwinPaper[] papers) {
	}

	@Override
	public void postMatchCalculation(MatchData match, QwinPaper[] papers) {
		for (int i = 0; i < papers.length; i++) {
			avgFullLanes_player[i] += papers[i].getNumberOfFullLanes();
			avgFullPentaColumns_player[i] += papers[i].getNumberOfFullPentagonColumns();
		}
	}

	@Override
	public void averageOverMatches(int number_matches) {
		for (int i = 0; i < avgFullLanes_player.length; i++) {
			avgFullLanes_player[i] /= number_matches;
			avgFullLanes += avgFullLanes_player[i];
			avgFullPentaColumns_player[i] /= number_matches;
			avgFullPentaColumns += avgFullPentaColumns_player[i];
		}
		avgFullLanes /= avgFullLanes_player.length;
		avgFullPentaColumns /= avgFullPentaColumns_player.length;
	}

	@Override
	public void processPreTurn(TurnData turn, PlayerData[] players, QwinPaper[] papers) {
		// TODO Auto-generated method stub
		
	}

}
