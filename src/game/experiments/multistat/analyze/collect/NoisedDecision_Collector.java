package game.experiments.multistat.analyze.collect;

import game.DiceRoll;
import game.QwinPaper;
import game.experiments.multistat.analyze.MatchStatCollecting;
import game.experiments.multistat.data.MatchData;
import game.experiments.multistat.data.PlayerData;
import game.experiments.multistat.data.PlayerLA_Data;
import game.experiments.multistat.data.TurnData;

public class NoisedDecision_Collector implements MatchStatCollecting {
	private double[] noisedDecisions_player;
	private double[] noisedDecisions_player_match;
	private double noisedDecisions;
	private PlayerLA_Data[] la_data;
	private int[] la_data_currentIndex;

	public double[] getProportionNoisedDecisions_player() {
		return noisedDecisions_player;
	}

	public double getProportionNoisedDecision() {
		return noisedDecisions;
	}

	@Override
	public void preMatchSetup(MatchData match) {
		if (noisedDecisions_player == null) {
			noisedDecisions_player = new double[match.players.length];
		}
		noisedDecisions_player_match = new double[match.players.length];
		la_data = new PlayerLA_Data[match.players.length];
		la_data_currentIndex = new int[match.players.length];
		for (int i = 0; i < la_data.length; i++) {
			if (match.players[i].special_data instanceof PlayerLA_Data)
				la_data[i] = (PlayerLA_Data) match.players[i].special_data;
		}
	}

	@Override
	public void processPostTurn(TurnData turn, PlayerData[] players, QwinPaper[] papers) {
		if (la_data[turn.turn_of_player_idx] != null) {
			// the player of this turn can have noised decisions
			int idx = turn.turn_of_player_idx;
			// check if the chosen dice to roll are the same
			if (la_data[idx].denoisedDecisions.get(la_data_currentIndex[idx]) != -(1+turn.diceroll_flag)) {
				noisedDecisions_player_match[idx] += 1.;
			}
			la_data_currentIndex[idx]++;
			// reroll decision
			if (turn.rolledNumbers.length == 2) {
				// actual reroll action
				if (la_data[idx].denoisedDecisions.get(la_data_currentIndex[idx]) != 0)
					noisedDecisions_player_match[idx] += 1.;
				la_data_currentIndex[idx]++;
			}
		}
		for (int i = 0; i < players.length; i++) {
			if (la_data[i] != null) {
				if (la_data[i].denoisedDecisions.get(la_data_currentIndex[i]) != turn.players_action[i])
					noisedDecisions_player_match[i] += 1.;
				la_data_currentIndex[i]++;
			}
		}
	}

	@Override
	public void postMatchCalculation(MatchData match, QwinPaper[] papers) {
		for (int i = 0; i < noisedDecisions_player.length; i++) {
			if (la_data[i] != null) {
				noisedDecisions_player_match[i] /= la_data[i].denoisedDecisions.size();
			}
			noisedDecisions_player[i] += noisedDecisions_player_match[i];
		}
	}

	@Override
	public void averageOverMatches(int number_matches) {
		noisedDecisions = 0;
		for (int i = 0; i < noisedDecisions_player.length; i++) {
			noisedDecisions_player[i] /= number_matches;
			noisedDecisions += noisedDecisions_player[i];
		}
		noisedDecisions /= noisedDecisions_player.length;
	}

	@Override
	public void processPreTurn(TurnData turn, PlayerData[] players, QwinPaper[] papers) {
	}

	@Override
	public String printAllStats() {
		// TODO Auto-generated method stub
		return "";
	}

}
