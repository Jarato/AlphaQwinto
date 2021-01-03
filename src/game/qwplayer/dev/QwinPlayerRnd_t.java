package game.qwplayer.dev;

import java.util.Random;

import game.DiceRoll;

public abstract class QwinPlayerRnd_t extends QwinPlayer_t{

	public QwinPlayerRnd_t(Random initRnd) {
		super(initRnd);
		// TODO Auto-generated constructor stub
	}
	
	public abstract DiceRoll getDiceThrowRnd(double probabilityRandom);

	public abstract int[] getActionFlagListRnd(double probabilityRandom, int diceNumber, DiceRoll thrown, boolean untilRethrow);

}
