package game.qwplayer;

import java.util.Random;

import game.DiceRoll;
import game.QwinDice;
import game.qwplayer.dev.QwinPlayer_t;
import pdf.ai.dna.DNA;
import pdf.ai.dna.Evolutionizable;

public class QwinPlayerExpertETest extends QwinPlayer_t  {
	private int[] lp;
	private int[] hp;
	private int[] fromPos;
	private int[] toPos;
	private int holeThreshold;
	
	
	public QwinPlayerExpertETest(Random rnd) {
		super(rnd);
		lp = new int[] {0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 5, 6, 7, 7, 8, 8, 8, 8};
		hp = new int[] {0, 0, 0, 0, 1, 1, 2, 3, 4, 5, 6, 6, 7, 7, 8, 8, 8, 8};
		fromPos = new int[] {4, 0, 1};
		toPos = new int[] {5, 1, 8};
		holeThreshold = 2; 
	/*
		
		Generation 302
		Best avg score	50.25689655172414
		lp = new int[] {3, 0, 0, 0, 0, 1, 2, 3, 4, 5, 5, 6, 7, 7, 8, 8, 8, 2};
		hp = new int[] {6, 0, 0, 0, 1, 1, 2, 3, 4, 5, 6, 6, 7, 7, 8, 8, 8, 8};
		fromPos = new int[] {4, 0, 1};
		toPos = new int[] {5, 1, 8};
		holeThreshold = 2; 
		*/
	}


	/**
	 * 0 - Red<br>
	 * 1 - Yellow<br>
	 * 2 - Red+Yellow<br>
	 * 3 - Purple<br>
	 * 4 - Purple+Red<br>
	 * 5 - Purple+Yellow<br>
	 * 6 - purple+Yellow+Red<br>
	 */
	@Override
	public DiceRoll getDiceThrow() {
		DiceRoll t = new DiceRoll(true, true, true);
		int[] flaglist = randomPermutation(7);
		for (int i = 0; i < 7; i++) {
			t = DiceRoll.flagToDiceThrow(flaglist[i]);
			int fromI = fromPos[t.getNumberOfDice()-1];
			int toI = toPos[t.getNumberOfDice()-1];
			if (numberOfHoles(t, fromI, toI) > holeThreshold) return t;
		}
		return t;
	}

	private int numberOfHoles(DiceRoll dThrow, int fromI, int toI) {
		int number = 0;
		if (dThrow.red) {
			int[] line = paper.getRedLine();
			for (int i = fromI; i < toI+1; i++) {
				if (line[i] == 0) number++;
			}
		}
		if (dThrow.yellow) {
			int[] line = paper.getYellowLine();
			for (int i = fromI; i < toI+1; i++) {
				if (line[i] == 0) number++;
			}
		}
		if (dThrow.purple) {
			int[] line = paper.getPurpleLine();
			for (int i = fromI; i < toI+1; i++) {
				if (line[i] == 0) number++;
			}
		}
		return number;
	}

	@Override
	public int[] getActionFlagList(int diceNumber, DiceRoll thrown) {
		int[] perm = randomPermutation(28);
		// printActionFlagList(perm);
		for (int i = 0; i < lp.length; i++) {
			if (lp[i]>hp[i]) throw new IllegalArgumentException();
		}
		// System.out.println("from "+(lp+1)+" to "+(hp+1));
		for (int i = 0; i < perm.length - 1; i++) {
			for (int j = i + 1; j < perm.length; j++) {
				int pos = (perm[j] - 1) % 9;
				if (perm[j] != 0 && pos >= lp[diceNumber - 1] && pos <= hp[diceNumber - 1]) {
					int t = perm[j];
					perm[j] = perm[i];
					perm[i] = t;
				}
			}
		}
		for (int i = 0; i < perm.length - 1; i++) {
			if (perm[i] == 0) {
				int t = perm[perm.length - 1];
				perm[perm.length - 1] = perm[i];
				perm[i] = t;
			}
		}
		// printActionFlagList(perm);
		return perm;
	}

	@Override
	public String getName() {
		return "Expert evolved 1";
	}

	
}
