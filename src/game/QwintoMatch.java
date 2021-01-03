package game;

import java.util.Random;

import game.qwplayer.QwinPlayerRandom;
import game.qwplayer.dev.QwinPlayer_t;

public class QwintoMatch {
	private QwinPlayer_t[] player;
	private QwinDice dice;
	private int currentPlayerIndex;
	private boolean matchEnd;
	
	public QwintoMatch(QwinPlayer_t... initPlayer) {
		this(new Random(), initPlayer);
	}
	
	public QwintoMatch(Random rnd, QwinPlayer_t... initPlayer) {
		player = initPlayer;
		dice = new QwinDice(rnd);
		currentPlayerIndex = rnd.nextInt(player.length);
		matchEnd = false;
	}
	
	public void calculateMatch(boolean print) {
		int turn = 1;
		while(!matchEnd) {
			if (print) System.out.println("TURN number "+turn);
			// the players turn
			DiceRoll roll = currentPlayerTurn(print);
			// go through all the other players and they can decide to enter or not to enter the number
			for (int i = 0; i < player.length; i++) {
				if (i!=currentPlayerIndex) {
					if (print) System.out.println("Player number "+i+" ("+player[i].getName()+") thinks about entering the number.");
					QwinPlayer_t p = player[i];
					int lastThrown = dice.getLastThrownNumber();
					int action = p.getActionFlag(lastThrown, dice.getLastThrown(), false, true);
					if (action != 1) paperEnterNumber(player[i].getPaper(),roll, lastThrown, action, print);
					if (print && action == 1) System.out.println("\"I don't enter the number "+lastThrown+".\"");
					if (print) System.out.println("Paper: \n"+p.getPaper()+"\n");
				}
				//System.out.println(player[0].getPaper()+"\n");
			}
			if (print) System.out.println("\n");
			currentPlayerIndex = (currentPlayerIndex+1)%player.length;
			turn++;
			if (matchEnd) {
				for (int k = 0; k < player.length; k++) {
					player[k].gameEndWrapUp();
				}
			}
			if (print && matchEnd) System.out.println("The match has ended. Here are the final papers: \n\n");
		}
	}
	
	public DiceRoll currentPlayerTurn(boolean print) {
		if (print) System.out.println("Turn for player "+currentPlayerIndex+" ("+player[currentPlayerIndex].getName()+")");
		QwinPlayer_t p = player[currentPlayerIndex];
		//Throw Dice
		DiceRoll th = p.getDiceThrow();
		if (print) System.out.println(th);
		int thrown = dice.throwDice(th);
		if (print) System.out.println("the sum of the "+th.getNumberOfDice()+" dice is "+thrown+".");
		int action = p.getActionFlag(thrown, dice.getLastThrown(), true, false);
		if (action < 0 || action == 1  || action == 2 || action > 29) throw new IllegalArgumentException("player chose an illegal action!");
		if (action == 0) { // reroll dice
			if (print) System.out.println("\"I want to reroll the dice!\"");
			thrown = dice.rethrowDice();
			if (print) System.out.println("the new sum of the "+th.getNumberOfDice()+" dice is "+thrown+".");
			action = p.getActionFlag(thrown, dice.getLastThrown(), false, false);
			if (action < 2 || action > 29) throw new IllegalArgumentException("player chose an illegal action!");
			paperEnterNumber(p.getPaper(), th, thrown, action, print);
			if (print && action == 2) System.out.println("\"I can't enter the number "+thrown+". It was a misthrow!\"");
		} else {
			// if not reroll
			paperEnterNumber(p.getPaper(), th, thrown, action, print);
		}
		if (print) System.out.println("Paper: \n"+p.getPaper()+"\n");
		return th;
	}
	
	
	/**
	 * 0  ReRoll<br>
	 * 1  Reject<br>
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
	
	/**
	 * 
	 * @return <br>
	 * 0 - if rethrow dice
	 * 1 - if the number was entered
	 * 2 - if end of list, means misthrow
	 */
	private void paperEnterNumber(QwinPaper paper, DiceRoll roll, int number, int action_flag, boolean print) {
		if (action_flag == 2) paper.misthrow();
		else {
			int color = (action_flag-3)/9;
			int pos = (action_flag-3)%9;
			boolean colorOkay = (color == 0 && roll.red) || (color == 1 && roll.yellow) || (color == 2 && roll.purple) ;
			if (!colorOkay ||!paper.isPositionValidForNumber(color, pos, number)) throw new IllegalArgumentException("player chose an illegal action!");
			if (print) System.out.println("\"I enter the number in the "+(color==0?"RED":(color==1?"YELLOW":"PURPLE"))+" lane on position "+(pos+1)+".\"");
			paper.enterNumber(color, pos, number);
		}
		if (!matchEnd) matchEnd = paper.isEndCondition();
	}
	
}
