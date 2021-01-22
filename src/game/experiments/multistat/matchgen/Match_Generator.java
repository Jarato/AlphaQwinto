package game.experiments.multistat.matchgen;

import java.util.Random;

import game.QwintoMatch;
import game.qwplayer.dev.QwinPlayer_t;

public abstract class Match_Generator {
	protected Random rnd;
	
	public Match_Generator(Random init) {
		rnd = init;
	}
	
	protected abstract QwinPlayer_t[] generatePlayers();
	
	public QwintoMatch generateMatch() {
		QwintoMatch match = new QwintoMatch(generatePlayers());
		return match;
	}
	
}
