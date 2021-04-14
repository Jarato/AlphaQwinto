package game.experiments.multistat.analyze.collect;

import game.QwinPaper;
import game.experiments.multistat.analyze.MatchStatCollecting;
import game.experiments.multistat.data.MatchData;
import game.experiments.multistat.data.PlayerData;
import game.experiments.multistat.data.TurnData;

public class DeadCells_Collector implements MatchStatCollecting {
	private double[] avgDeadCells_player;
	private double avgDeadCells;
	
	public double getAvgDeadCells() {
		return avgDeadCells;
	}
	
	public double[] getAvgDeadCells_Players() {
		return avgDeadCells_player;
	}
	
	@Override
	public void preMatchSetup(MatchData match) {
		if (avgDeadCells_player == null) {
			avgDeadCells_player = new double[match.players.length];
		}
	}

	@Override
	public void processPostTurn(TurnData turn, PlayerData[] players, QwinPaper[] papers) {
	}

	@Override
	public void postMatchCalculation(MatchData match, QwinPaper[] papers) {
		for (int i = 0; i < papers.length; i++) {
			avgDeadCells_player[i] += papers[i].countDeadFields();
		}
	}

	@Override
	public void averageOverMatches(int number_matches) {
		for (int i = 0; i < avgDeadCells_player.length; i++) {
			avgDeadCells_player[i] /= number_matches;
			avgDeadCells += avgDeadCells_player[i];
		}
		avgDeadCells /= avgDeadCells_player.length;
	}

	@Override
	public void processPreTurn(TurnData turn, PlayerData[] players, QwinPaper[] papers) {
	}

	@Override
	public String printAllStats() {
		String str = "Count of dead cells - collector\naverage dead cells of all players:\t"+avgDeadCells+"\naverage dead cells of individual players\n";
		for (int i = 0; i < avgDeadCells_player.length; i++) {
			str = str + "player "+i+":\t"+avgDeadCells_player[i]+"\n";
		}
		return str;
	}

}
