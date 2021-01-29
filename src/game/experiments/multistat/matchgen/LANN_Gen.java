package game.experiments.multistat.matchgen;

import java.util.Random;

import game.qwplayer.dev.QwinPlayerLA_NNEval;
import game.qwplayer.dev.QwinPlayer_t;

public class LANN_Gen extends Match_Generator{
	private double noise;
	private double[] weights;
	private int number_of_players;
	
	public LANN_Gen(Random init, double noise_level, double[] weights, int numPlayers) {
		super(init);
		this.noise = noise_level;
		this.weights = weights;
		this.number_of_players = numPlayers;
	}

	@Override
	protected QwinPlayer_t[] generatePlayers() {
		QwinPlayer_t[] players = new QwinPlayerLA_NNEval[number_of_players];
		for (int i = 0; i < players.length; i++) {
			Random newInit = new Random(rnd.nextLong());
			QwinPlayerLA_NNEval p = new QwinPlayerLA_NNEval(newInit);
			p.getEvalNetwork().applyWeightsBiasesVector(weights);
			p.setNoiseLevel(noise);
			players[i] = p;
		}
		return players;
	}

}
