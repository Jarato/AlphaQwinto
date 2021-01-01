package game.qwplayer.dev;

import java.util.Random;

import game.DiceThrow;

public abstract class QwinPlayerRnd extends QwinPlayer{

	public QwinPlayerRnd(Random initRnd) {
		super(initRnd);
		// TODO Auto-generated constructor stub
	}
	
	public abstract DiceThrow getDiceThrowRnd(double probabilityRandom);

	public abstract int[] getActionFlagListRnd(double probabilityRandom, int diceNumber, DiceThrow thrown, boolean untilRethrow);

}
