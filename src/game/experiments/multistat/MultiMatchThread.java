package game.experiments.multistat;

import game.QwintoMatch;
import game.experiments.multistat.data.AllDataRaw;
import game.experiments.multistat.matchgen.Match_Generator;

public class MultiMatchThread extends Thread {
	private AllDataRaw raw_data = null;
	private Match_Generator generator;
	private int numMatches;
	
	public MultiMatchThread(int number_of_matches, Match_Generator setGenerator) {
		numMatches = number_of_matches;
		generator = setGenerator;
	}
	
	public void setRawData(AllDataRaw raw_data_s) {
		raw_data = raw_data_s;
	}
	
	@Override
	public void run() {
		// matches
		for (int match_i = 0; match_i <  numMatches; match_i++) {
			// generate match
			QwintoMatch match = generator.generateMatch();
			match.setRawData(raw_data.generateBlankMatchData());
			// play match
			match.calculateMatch();
		}
	}
}
