package game.experiments.multistat.analyze;

import java.util.ArrayList;

import game.QwinPaper;
import game.experiments.multistat.data.MatchData;
import game.experiments.multistat.data.PlayerData;
import game.experiments.multistat.data.TurnData;
import pdf.util.Pair;

public class TrainingData_LANNEVAL9_Collector implements MatchStatCollector {
	ArrayList<Pair<double[], Double>> training_data = new ArrayList<Pair<double[], Double>>();
	
	@Override
	public void preMatchSetup(MatchData match) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processTurn4Player(TurnData turn, PlayerData player, QwinPaper paper) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postMatchCalculation(MatchData match) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void averageOverMatches(int number_matches) {
		// TODO Auto-generated method stub
		
	}
	
}
