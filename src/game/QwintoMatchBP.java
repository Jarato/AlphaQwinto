package game;

import java.util.Random;
import game.qwplayer.dev.QwinPlayerRnd_t;

public class QwintoMatchBP {
	private QwinPlayerRnd_t[] player;
	private QwinDice dice;
	private int currentPlayerIndex;
	private boolean matchEnd;
	
	public QwintoMatchBP(QwinPlayerRnd_t... initPlayer) {
		this(new Random(), initPlayer);
	}
	
	public QwintoMatchBP(Random rnd, QwinPlayerRnd_t... initPlayer) {
		player = initPlayer;
		dice = new QwinDice(rnd);
		currentPlayerIndex = rnd.nextInt(player.length);
		matchEnd = false;
	}
	
	public void calculateMatch(double randomPlayPercent, boolean print) {
		int turn = 1;
		while(!matchEnd) {
			if (print) System.out.println("TURN number "+turn);
			currentPlayerTurn(randomPlayPercent, print);
			//System.out.println();
			for (int i = 0; i < player.length; i++) {
				if (i!=currentPlayerIndex) {
					if (print) System.out.println("Player number "+i+" ("+player[i].getName()+") thinks about entering the number.");
					QwinPlayerRnd_t p = player[i];
					int lastThrown = dice.getLastRolledNumber();
					int[] flagList = p.getActionFlagListRnd(randomPlayPercent, lastThrown, dice.getLastThrown(), true);
					int res = paperEnterNumber(player[i].getPaper(), lastThrown, flagList, true, print);
					if (print && res == 0) System.out.println("\"I don't want to enter the number "+lastThrown+".\"");
					if (print) System.out.println("Paper: \n"+p.getPaper()+"\n");
				}
				//System.out.println(player[0].getPaper()+"\n");
			}
			if (print) System.out.println("\n");
			currentPlayerIndex = (currentPlayerIndex+1)%player.length;
			turn++;
		}
	}
	
	public void currentPlayerTurn(double randomPlayPercent, boolean print) {
		if (print) System.out.println("Turn for player "+currentPlayerIndex+" ("+player[currentPlayerIndex].getName()+")");
		QwinPlayerRnd_t p = player[currentPlayerIndex];
		//Throw Dice
		DiceRoll th = p.getDiceThrowRnd(randomPlayPercent);
		if (print) System.out.println(th);
		int thrown = dice.rollDice(th);
		if (print) System.out.println("the sum of the "+th.getNumberOfDice()+" dice is "+thrown+".");
		int[] actionList = p.getActionFlagListRnd(randomPlayPercent, thrown, dice.getLastThrown(), true);
		if (paperEnterNumber(p.getPaper(), thrown, actionList, true, print) == 0) { // rethrow dice
			if (print) System.out.println("\"I want to RETHROW!\"");
			thrown = dice.rerollDice();
			if (print) System.out.println("the sum of the "+th.getNumberOfDice()+" dice is "+thrown+".");
			actionList = p.getActionFlagListRnd(randomPlayPercent, thrown, dice.getLastThrown(), false);
			int res = paperEnterNumber(p.getPaper(), thrown, actionList, false, print);
			if (print && res == 2) System.out.println("\"I can't enter the number "+thrown+". It was a misthrow!\"");
		}
		if (print) System.out.println("Paper: \n"+p.getPaper()+"\n");
	}
	
	/**
	 * 
	 * @return <br>
	 * 0 - if rethrow dice
	 * 1 - if the number was entered
	 * 2 - if end of list, means misthrow
	 */
	private int paperEnterNumber(QwinPaper paper, int number, int[] flaglist, boolean untilRethrow, boolean print) {
		int i = 0;
		boolean isValidPos = false;
		boolean isRethrow = false;
		int color = -1;
		int pos = -1;
		boolean loopEndCondition = false;
		do {
			if (flaglist[i] == 0) {
				if (untilRethrow) {
					isRethrow = true;
				}
			} else {
				color = (flaglist[i]-1)/9;
				boolean colorThrown = true;
				DiceRoll thrown = dice.getLastThrown();
				switch(color) {
				case 0: colorThrown = thrown.red;
					break;
				case 1: colorThrown = thrown.yellow;
					break;
				case 2: colorThrown = thrown.purple;
					break;
				}
				if (colorThrown) {
					pos = (flaglist[i]-1)%9;
					isValidPos = paper.isPositionValidForNumber(color, pos, number);
				}
			}
			i++;
			loopEndCondition = isRethrow || isValidPos;
			if (!loopEndCondition) loopEndCondition = (i >= flaglist.length);
		} while(!loopEndCondition);
		// misthrow or enter number
		if (isRethrow) {
			return 0;
		}
		if (isValidPos) {
			if (print) System.out.println("\"I enter the number "+number+" in line "+(color+1)+" at position "+(pos+1)+".\"");
			paper.enterNumber(color, pos, number);
			if (!matchEnd) matchEnd = paper.isEndCondition();
			return 1;
		} else {
			paper.misthrow();
			if (!matchEnd) matchEnd = paper.isEndCondition();
			return 2;
		}
	}
	
}
