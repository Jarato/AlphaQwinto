package game.experiments.multistat.data;

import java.util.ArrayList;

public class AllDataRaw {
	public ArrayList<MatchData> matches = new ArrayList<MatchData>();
	
	public MatchData generateBlankMatchData() {
		MatchData blank_match = new MatchData();
		matches.add(blank_match);
		return blank_match;
	}
	
	public static AllDataRaw collectAllData(AllDataRaw...allDataRaws) {
		AllDataRaw rawCollected = new AllDataRaw();
		for (AllDataRaw data : allDataRaws) {
			rawCollected.matches.addAll(data.matches);
		}
		return rawCollected;
	}
}
