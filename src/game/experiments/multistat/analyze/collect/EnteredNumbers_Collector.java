package game.experiments.multistat.analyze.collect;

import game.QwinPaper;
import game.experiments.multistat.analyze.MatchStatCollecting;
import game.experiments.multistat.data.MatchData;
import game.experiments.multistat.data.PlayerData;
import game.experiments.multistat.data.TurnData;

public class EnteredNumbers_Collector implements MatchStatCollecting {
	private double avgEnteredNumbers;
	private double[] avgEnteredNumbers_player;
	private int[][][] number_position_distribution_player;
	
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
			number_position_distribution_player = new int[match.players.length][27][19];
		}
	}

	@Override
	public void processPostTurn(TurnData turn, PlayerData[] players, QwinPaper[] papers) {
		
	}

	@Override
	public void postMatchCalculation(MatchData match, QwinPaper[] papers) {
		for (int i = 0; i < avgEnteredNumbers_player.length; i++) {
			avgEnteredNumbers_player[i] += papers[i].getNumberOfEnteredNumbers();
			int[] red = papers[i].getRedLine();
			int[] yellow = papers[i].getYellowLine();
			int[] purple = papers[i].getPurpleLine();
			for (int j = 0; j < red.length; j++) {
				number_position_distribution_player[i][j][red[j]]++;
				number_position_distribution_player[i][9+j][yellow[j]]++;
				number_position_distribution_player[i][18+j][purple[j]]++;
			}
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
	}

	@Override
	public String printAllStats() {
		String str = "Entered numbers - collector\naverage all players:\t"+avgEnteredNumbers+"\n";// TODO Auto-generated method stub
		str = str+"average entered numbers of individual players:\n";
		for (int i = 0; i <avgEnteredNumbers_player.length; i++) {
			str = str+"player "+i+":\t"+avgEnteredNumbers_player[i]+"\n";
		}
		str = str+"number distribution of individual players\n";
		//for (int i = 0; i < )
		return str;
	}

}
