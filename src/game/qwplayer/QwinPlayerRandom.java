package game.qwplayer;

import java.util.Random;

import game.DiceThrow;
import game.qwplayer.dev.QwinPlayer;
import game.qwplayer.dev.QwinPlayerRnd;

public class QwinPlayerRandom extends QwinPlayerRnd{	
	public QwinPlayerRandom(Random initRnd) {
		super(initRnd);
	}
	
	@Override
	public DiceThrow getDiceThrow() {
		int t = rnd.nextInt(7);
		return DiceThrow.flagToDiceThrow(t);
	}

	@Override
	public int[] getActionFlagList(int diceNumber, DiceThrow thrown) {
		return randomPermutation(28);
	}

	@Override
	public String getName() {
		return "Random";
	}

	@Override
	public DiceThrow getDiceThrowRnd(double probabilityRandom) {
		return getDiceThrow();
	}

	@Override
	public int[] getActionFlagListRnd(double probabilityRandom, int diceNumber, DiceThrow thrown, boolean untilRethrow) {
		return getActionFlagList(diceNumber, thrown);
	}

	
	
}
