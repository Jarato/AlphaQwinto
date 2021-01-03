package game;

import java.util.Random;

public class QwinDice {
	private Random rnd;
	private DiceRoll diceThrow;
	private int lastThrownNumber;
	
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
	
	public int getLastThrownNumber() {
		return lastThrownNumber;
	}
	
	public int throwDice(DiceRoll dThrow) {
		if (!dThrow.isValid()) throw new IllegalArgumentException();
		diceThrow.red = dThrow.red;
		diceThrow.yellow = dThrow.yellow;
		diceThrow.purple = dThrow.purple;
		int res = 0;
		for (int i = 0; i < dThrow.getNumberOfDice(); i++) {
			res += rnd.nextInt(6)+1;
		}
		lastThrownNumber = res;
		return res;
	}
	
	public int rethrowDice() {
		return throwDice(diceThrow);
	}
}
