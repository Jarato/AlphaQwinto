package game.qwplayer.dev;

import java.util.Random;

import game.DiceThrow;
import game.QwinDice;
import game.QwinPaper;

public abstract class QwinPlayer {
	protected QwinPaper paper;
	protected Random rnd;
	
	public QwinPlayer(Random initRnd) {
		paper = new QwinPaper();
		reset(initRnd);
	}
	
	public void reset(Random resetRnd) {
		rnd = resetRnd;
		paper.clear();
	}
	
	public int getScore() {
		return paper.calculateScore();
	}
	
	public QwinPaper getPaper() {
		return paper;
	}
	
	/**
	 *	0 - Red<br>
	 *	1 - Yellow<br>
	 *	2 - Red+Yellow<br>
	 *	3 - Purple<br>
	 * 	4 - Purple+Red<br>
	 * 	5 - Purple+Yellow<br>
	 * 	6 - purple+Yellow+Red<br>
	 */
	public abstract DiceThrow getDiceThrow();
	
	protected int[] randomPermutation(int n) {
		int[] res = new int[n];
		for (int i = 0; i < n; i++) {
			int d = rnd.nextInt(i+1);
			res[i] = res[d];
			res[d] = i;
		}
		return res;
	}


	/**
	 * ReThrowDice<br>
	 * RedPos1<br>
	 * RedPos2<br>
	 * RedPos3<br>
	 * RedPos4<br>
	 * RedPos5<br>
	 * RedPos6<br>
	 * RedPos7<br>
	 * RedPos8<br>
	 * RedPos9<br>
	 * YellowPos1<br>
	 * YellowPos2<br>
	 * YellowPos3<br>
	 * YellowPos4<br>
	 * YellowPos5<br>
	 * YellowPos6<br>
	 * YellowPos7<br>
	 * YellowPos8<br>
	 * YellowPos9<br>
	 * PurplePos1<br>
	 * PurplePos2<br>
	 * PurplePos3<br>
	 * PurplePos4<br>
	 * PurplePos5<br>
	 * PurplePos6<br>
	 * PurplePos7<br>
	 * PurplePos8<br>
	 * PurplePos9<br>
	 */
	public abstract int[] getActionFlagList(int diceNumber, DiceThrow thrown);
	
	public abstract String getName();
}
