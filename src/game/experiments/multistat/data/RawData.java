package game.experiments.multistat.data;

import java.util.ArrayList;

public class RawData {
	public ArrayList<MatchData> matches = new ArrayList<MatchData>();
	
	public MatchData generateBlankMatchData() {
		MatchData blank_match = new MatchData();
		matches.add(blank_match);
		return blank_match;
	}
	
	public static RawData collectAllData(RawData...allDataRaws) {
		RawData rawCollected = new RawData();
		for (RawData data : allDataRaws) {
			rawCollected.matches.addAll(data.matches);
		}
		return rawCollected;
	}
}
