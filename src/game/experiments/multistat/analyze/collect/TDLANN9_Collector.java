package game.experiments.multistat.analyze.collect;

import java.util.ArrayList;

import game.QwinPaper;
import game.experiments.multistat.analyze.MatchStatCollecting;
import game.experiments.multistat.data.MatchData;
import game.experiments.multistat.data.PlayerData;
import game.experiments.multistat.data.TurnData;
import pdf.util.Pair;

public class TDLANN9_Collector implements MatchStatCollecting {
	private ArrayList<Pair<double[], Double>> training_data = new ArrayList<Pair<double[], Double>>();
	private ArrayList<ArrayList<Pair<double[], Double>>> td_onegame = new ArrayList<ArrayList<Pair<double[], Double>>>();
	
	public ArrayList<Pair<double[], Double>> getTrainingData(){
		return training_data;
	}
	
	@Override
	public void preMatchSetup(MatchData match) {
		for (int i = 0; i  < match.players.length ; i++) {
			ArrayList<Pair<double[], Double>> p_p_history = new ArrayList<Pair<double[], Double>>();
			p_p_history.add(new Pair<double[], Double>(new double[83], -1.));
			td_onegame.add(p_p_history);
		}
	}

	@Override
	public void processTurn(TurnData turn, PlayerData[] players, QwinPaper papers[]) {
		for (int j = 0; j  < players.length ; j++) {
			double[] input = new double[83];
			int numOfTurns = turn.turn_number;
			int[] redline = papers[j].getRedLine();
			int[] yellowline = papers[j].getYellowLine();
			int[] purpleline = papers[j].getPurpleLine();
			boolean[][] blocked = papers[j].generateBlockedFields();
			for (int i = 0; i < 9; i++) {
				input[i] = redline[i] / 18.;
				input[i + 9] = yellowline[i] / 18.;
				input[i + 18] = purpleline[i] / 18.;
				input[i + 27] = (redline[i] > 0 ? 1. : 0.);
				input[i + 36] = (yellowline[i] > 0 ? 1. : 0.);
				input[i + 45] = (purpleline[i] > 0 ? 1. : 0.);
				input[i + 54] = ((blocked[0][i] && redline[i] == 0) ? 1. : 0.);
				input[i + 63] = ((blocked[1][i] && yellowline[i] == 0) ? 1. : 0.);
				input[i + 72] = ((blocked[2][i] && purpleline[i] == 0) ? 1. : 0.);
			}
			input[81] = papers[j].getNumberOfMisthrows() * 1. / 4.;
			input[82] = numOfTurns / (15. + Math.abs(numOfTurns));
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
	}

	@Override
	public void averageOverMatches(int number_matches) {
		
	}
	
}
