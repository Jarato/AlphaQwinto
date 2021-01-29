package game.experiments.multistat.analyze.collect;

import game.DiceRoll;
import game.QwinPaper;
import game.experiments.multistat.analyze.MatchStatCollecting;
import game.experiments.multistat.data.MatchData;
import game.experiments.multistat.data.PlayerData;
import game.experiments.multistat.data.TurnData;

public class Reject_Collector implements MatchStatCollecting {
	private double[] trueRejectRate_player;
	private int[] rejectableDecisions_player;
	private double trueRejectRate;
	
	public double getTrueRejectRate() {
		return trueRejectRate;
	}
	
	public double[] getTrueRejectRate_player() {
		return trueRejectRate_player;
	}
	
	@Override
	public void preMatchSetup(MatchData match) {
		if (trueRejectRate_player == null) {
			trueRejectRate_player = new double[match.players.length];
			rejectableDecisions_player = new int[match.players.length];
		}
	}

	@Override
	public void processPostTurn(TurnData turn, PlayerData[] players, QwinPaper[] papers) {
	}

	@Override
	public void postMatchCalculation(MatchData match, QwinPaper[] papers) {
	}

	@Override
	public void averageOverMatches(int number_matches) {
		for (int i = 0; i < trueRejectRate_player.length; i++) {
			trueRejectRate_player[i] /= rejectableDecisions_player[i];
			trueRejectRate += trueRejectRate_player[i];
		}
		trueRejectRate /= trueRejectRate_player.length;
	}

	@Override
	public void processPreTurn(TurnData turn, PlayerData[] players, QwinPaper[] papers) {
		DiceRoll dr = DiceRoll.flagToDiceThrow(turn.diceroll_flag);
		int rolled_number = turn.rolledNumbers[turn.rolledNumbers.length-1];
		for (int i = 0; i < players.length; i++) {
			if (turn.turn_of_player_idx != i) {
				// not this players turn, so can reject
				if (papers[i].canEnterNumberAnywhere(rolled_number, dr)) {
					// possible true reject
					rejectableDecisions_player[i]++;
					if (turn.players_action[i] == 1) {
						// actually rejected
						trueRejectRate_player[i] += 1.;
					}
				}
			}
		}
	}

	@Override
	public String printAllStats() {
		String str = "Reject rate - collector\naverage reject rate of all players:\t"+trueRejectRate+"\naverage reject rate of individual players\n";
		for (int i = 0; i < trueRejectRate_player.length; i++) {
			str = str+"player "+i+":\t"+trueRejectRate_player[i]+"\n";
		}
		return str;
	}

}
