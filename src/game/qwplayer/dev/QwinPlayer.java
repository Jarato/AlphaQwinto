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
	 * 0  ReThrowDice<br>
	 * 1  NotEntering<br>
	 * 2  Misthrow<br>
	 * 3  RedPos1<br>
	 * 4  RedPos2<br>
	 * 5  RedPos3<br>
	 * 6  RedPos4<br>
	 * 7  RedPos5<br>
	 * 8  RedPos6<br>
	 * 9  RedPos7<br>
	 * 10 RedPos8<br>
	 * 11  RedPos9<br>
	 * 12 YellowPos1<br>
	 * 13 YellowPos2<br>
	 * 14 YellowPos3<br>
	 * 15 YellowPos4<br>
	 * 16 YellowPos5<br>
	 * 17 YellowPos6<br>
	 * 18 YellowPos7<br>
	 * 19 YellowPos8<br>
	 * 20 YellowPos9<br>
	 * 21 PurplePos1<br>
	 * 22 PurplePos2<br>
	 * 23 PurplePos3<br>
	 * 24 PurplePos4<br>
	 * 25 PurplePos5<br>
	 * 26 PurplePos6<br>
	 * 27 PurplePos7<br>
	 * 28 PurplePos8<br>
	 * 29 PurplePos9<br>
	 */
	public abstract int[] getActionFlagList(int diceNumber, DiceThrow thrown);
	
	
	public int getActionFlag(int diceNumber, DiceThrow roll, boolean reThrowable) {
		int[] actionFlagList = getActionFlagList(diceNumber, roll);
		
		
		return 0;
	}
	
	public abstract String getName();
}
