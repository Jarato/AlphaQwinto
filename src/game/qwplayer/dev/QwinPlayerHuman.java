package game.qwplayer.dev;

import java.util.Random;

import game.DiceRoll;

public class QwinPlayerHuman extends QwinPlayer_t{

	public QwinPlayerHuman(Random initRnd) {
		super(initRnd);
		// TODO Auto-generated constructor stub
	}

	@Override
	public DiceRoll getDiceRoll() {
		System.out.println("Human player input: \"R\", \"Y\", \"P\", \"RY\", \"RP\", \"YP\", \"RYP\"");
		return null;
	}

	@Override
	protected int[] getActionFlagList(int diceNumber, DiceRoll thrown) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override 
	public int getActionFlag(int diceNumber, DiceRoll roll, boolean reThrowable, boolean rejectable) {
		
		return 0;
	}

}
