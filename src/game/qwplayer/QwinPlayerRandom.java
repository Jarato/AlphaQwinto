package game.qwplayer;

import java.util.Random;

import game.DiceRoll;
import game.qwplayer.dev.QwinPlayer_t;
import game.qwplayer.dev.QwinPlayerRnd_t;

public class QwinPlayerRandom extends QwinPlayerRnd_t{	
	public QwinPlayerRandom(Random initRnd) {
		super(initRnd);
	}
	
	@Override
	public DiceRoll getDiceRoll() {
		int t = rnd.nextInt(7);
		return DiceRoll.flagToDiceThrow(t);
	}

	@Override
	public int[] getActionFlagList(int diceNumber, DiceRoll thrown) {
		return randomPermutation(28);
	}

	@Override
	public String getName() {
		return "Random";
	}

	@Override
	public DiceRoll getDiceThrowRnd(double probabilityRandom) {
		return getDiceRoll();
	}

	@Override
	public int[] getActionFlagListRnd(double probabilityRandom, int diceNumber, DiceRoll thrown, boolean untilRethrow) {
		return getActionFlagList(diceNumber, thrown);
	}

	
	
}
