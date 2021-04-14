package game.experiments.multistat.analyze.collect;

import java.util.ArrayList;

import game.QwinPaper;
import game.experiments.multistat.analyze.MatchStatCollecting;
import game.experiments.multistat.data.MatchData;
import game.experiments.multistat.data.PlayerData;
import game.experiments.multistat.data.TurnData;
import pdf.util.Pair;

public class TDLANN10_Collector implements MatchStatCollecting{
	private ArrayList<Pair<double[], Double>> training_data = new ArrayList<Pair<double[], Double>>();
	private ArrayList<ArrayList<Pair<double[], Double>>> td_onegame = new ArrayList<ArrayList<Pair<double[], Double>>>();
	
	public ArrayList<Pair<double[], Double>> getTrainingData(){
		return training_data;
	}
	
	@Override
	public void preMatchSetup(MatchData match) {
		for (int i = 0; i  < match.players.length ; i++) {
			ArrayList<Pair<double[], Double>> p_p_history = new ArrayList<Pair<double[], Double>>();
			p_p_history.add(new Pair<double[], Double>(new double[488], -1.));
			td_onegame.add(p_p_history);
		}
	}

	@Override
	public void processPostTurn(TurnData turn, PlayerData[] players, QwinPaper papers[]) {
		for (int j = 0; j  < players.length ; j++) {
			double[] input = new double[488];
			int numOfTurns = turn.turn_number;
			QwinPaper paper = papers[j];
			int[] redline = paper.getRedLine();
			int[] yellowline = paper.getYellowLine();
			int[] purpleline = paper.getPurpleLine();
			for (int i = 0; i < 9; i++) {
				if (redline[i] != 0) {
					input[i * 18 + redline[i] - 1] = 1;
				}
				if (yellowline[i] != 0) {
					input[18 * (9 + i) + yellowline[i] - 1] = 1;
				}
				if (purpleline[i] != 0) {
					input[18 * (18 + i) + purpleline[i] - 1] = 1;
				}
			}
			input[486] = paper.getNumberOfMisthrows() / 4.;
			input[487] = numOfTurns / (15. + Math.abs(numOfTurns));
			td_onegame.get(j).add(new Pair<double[], Double>(input, -1.));
		}
	}

	@Override
	public void postMatchCalculation(MatchData match, QwinPaper[] papers) {
		for (int i = 0; i <td_onegame.size(); i++) {
			ArrayList<Pair<double[], Double>> train_data_game = td_onegame.get(i);
			for (Pair<double[], Double> instance : train_data_game) {
				instance.setY((double)papers[i].calculateScore());
				training_data.add(instance);
			}
		}
		td_onegame = new ArrayList<ArrayList<Pair<double[], Double>>>();
	}

	@Override
	public void averageOverMatches(int number_matches) {
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
