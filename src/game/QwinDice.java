package game;

import java.util.Random;

public class QwinDice {
	private Random rnd;
	private DiceRoll diceThrow;
	private int lastRolledNumber;
	
	public QwinDice(Random initRnd) {
		rnd = initRnd;
		diceThrow = new DiceRoll();
		diceThrow.red = false;
		diceThrow.yellow = false;
		diceThrow.purple = false;
	}
	
	public QwinDice() {
		rnd = new Random();
	}
	
	public DiceRoll getLastThrown() {
		return diceThrow;
	}
	
	public int getLastRolledNumber() {
		return lastRolledNumber;
	}
	
	public int rollDice(DiceRoll droll) {
		if (!droll.isValid()) throw new IllegalArgumentException();
		diceThrow.red = droll.red;
		diceThrow.yellow = droll.yellow;
		diceThrow.purple = droll.purple;
		int res = 0;
		for (int i = 0; i < droll.getNumberOfDice(); i++) {
			res += rnd.nextInt(6)+1;
		}
		lastRolledNumber = res;
		return res;
	}
	
	public int rerollDice() {
		return rollDice(diceThrow);
	}
}
