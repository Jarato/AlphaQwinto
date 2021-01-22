package game.qwplayer.dev;

import java.util.Random;

import game.DiceRoll;
import game.QwinPaper;
import game.experiments.multistat.data.PlayerData_S;

public abstract class QwinPlayer_t {
	protected QwinPaper paper;
	protected int numOfTurns;
	protected Random rnd;
	
	public QwinPlayer_t(Random initRnd) {
		paper = new QwinPaper();
		reset(initRnd);
	}
	
	public void reset(Random resetRnd) {
		rnd = resetRnd;
		numOfTurns = 0;
		paper.clear();
	}
	
	public PlayerData_S generatePlayerDataCollector() {
		return new PlayerData_S();
	}
	
	public int getScore() {
		return paper.calculateScore();
	}
	
	public void matchEndWrapUp(QwinPaper[] allPapers) {
		
	}
	
	public void turnEndWrapUp() {
		numOfTurns++;
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
	public abstract DiceRoll getDiceRoll();
	
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
	 * 1  RedPos1<br>
	 * 2  RedPos2<br>
	 * 3  RedPos3<br>
	 * 4  RedPos4<br>
	 * 5  RedPos5<br>
	 * 6  RedPos6<br>
	 * 7  RedPos7<br>
	 * 8 RedPos8<br>
	 * 9  RedPos9<br>
	 * 10 YellowPos1<br>
	 * 11 YellowPos2<br>
	 * 12 YellowPos3<br>
	 * 13 YellowPos4<br>
	 * 14 YellowPos5<br>
	 * 15 YellowPos6<br>
	 * 16 YellowPos7<br>
	 * 17 YellowPos8<br>
	 * 18 YellowPos9<br>
	 * 19 PurplePos1<br>
	 * 20 PurplePos2<br>
	 * 21 PurplePos3<br>
	 * 22 PurplePos4<br>
	 * 23 PurplePos5<br>
	 * 24 PurplePos6<br>
	 * 25 PurplePos7<br>
	 * 26 PurplePos8<br>
	 * 27 PurplePos9<br>
	 */
	protected abstract int[] getActionFlagList(int diceNumber, DiceRoll thrown);
	
	
	public int getActionFlag(int diceNumber, DiceRoll roll, boolean reThrowable, boolean rejectable) {
		int[] actionFlagList = getActionFlagList(diceNumber, roll);
		// go through the action flag list and check if the action is a legal move, return the first legal action
		for (int i = 0; i < actionFlagList.length; i++) {
			if (actionFlagList[i] == 0 && reThrowable) return 0;
			if (actionFlagList[i] == 0 && rejectable) return 1;
			if (actionFlagList[i] > 0) {
				int color = (actionFlagList[i]-1)/9;
				int pos = (actionFlagList[i]-1)%9;
				boolean colorOkay = (color == 0 && roll.red) || (color == 1 && roll.yellow) || (color == 2 && roll.purple) ;
				if (colorOkay && paper.isPositionValidForNumber(color, pos, diceNumber)) return actionFlagList[i]+2;
			}
		}
		return 2;
	}
	
	public abstract String getName();
}
