package game.experiments.multistat.matchgen;

import java.util.Random;

import game.qwplayer.QwinPlayerExpertETest2;
import game.qwplayer.QwinPlayerNN2Test;
import game.qwplayer.QwinPlayerNNTest;
import game.qwplayer.QwinPlayerNNTestOld;
import game.qwplayer.dev.QwinPlayerLA_NNEval;
import game.qwplayer.dev.QwinPlayer_t;

public class Test_Gen extends Match_Generator {

	public Test_Gen(Random init) {
		super(init);
	}

	@Override
	protected QwinPlayer_t[] generatePlayers() {
		QwinPlayer_t[] player = new QwinPlayer_t[2];
		player[0] = new QwinPlayerExpertETest2(new Random(rnd.nextLong()));
		player[1] = new QwinPlayerLA_NNEval(new Random(rnd.nextLong()), 10, "LANNEVAL10_weights.txt");
		//player[2] = new QwinPlayerNN2Test(new Random(rnd.nextLong()));
		//player[3] = new QwinPlayerNNTestOld(new Random(rnd.nextLong()));
		return player;
	}

	
	
}
