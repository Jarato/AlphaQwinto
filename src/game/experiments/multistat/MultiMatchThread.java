package game.experiments.multistat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import game.QwintoMatch;
import game.experiments.multistat.data.RawData;
import game.experiments.multistat.matchgen.Match_Generator;
import game.qwplayer.dev.QwinPlayer_t;

public class MultiMatchThread extends Thread {
	private RawData raw_data = null;
	private Match_Generator generator;
	private int numMatches;
	private List<MatchPresetSingleNumber> presets;
	private int id;

	public static class MatchPresetSingleNumber {
		public int color;
		public int pos;
		public int number;
		
		public MatchPresetSingleNumber(int c, int p, int n) {
			color = c;
			pos = p;
			number = n;
		}
	}

	public MultiMatchThread(int sid, int number_of_matches, List<MatchPresetSingleNumber> spresets,
			Match_Generator setGenerator) {
		numMatches = number_of_matches;
		generator = setGenerator;
		id = sid;
		presets = spresets;
	}

	public void setRawData(RawData raw_data_s) {
		raw_data = raw_data_s;
	}

	@Override
	public void run() {
		if (presets != null) {
			for (MatchPresetSingleNumber p : presets) {
				// generate match
				QwintoMatch match = generator.generateMatch();
				match.setRawData(raw_data.generateBlankMatchData());
				match.setPresetNumberAllPlayers(p.color, p.pos, p.number);
				// play match
				match.calculateMatch();
			}
		}
		// matches
		for (int match_i = 0; match_i < numMatches; match_i++) {
			// generate match
			QwintoMatch match = generator.generateMatch();
			match.setRawData(raw_data.generateBlankMatchData());
			// play match
			match.calculateMatch();
		}
	}
}
