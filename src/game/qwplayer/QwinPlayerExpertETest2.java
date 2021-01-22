package game.qwplayer;

import java.util.Random;

import game.DiceRoll;
import game.QwinDice;
import game.qwplayer.dev.QwinPlayer_t;
import game.qwplayer.dev.QwinPlayerRnd_t;
import pdf.ai.dna.DNA;
import pdf.ai.dna.Evolutionizable;

public class QwinPlayerExpertETest2 extends QwinPlayerRnd_t {
	private int[] lp;
	private int[] hp;
	private int[] fromPos;
	private int[] toPos;
	private int holeThreshold;

	public QwinPlayerExpertETest2(Random rnd) {
		super(rnd);
		lp = new int[] { 0, 0, 1, 1, 1, 2, 2, 4, 5, 6, 6, 6, 7, 7, 8, 8, 8, 8 };
		hp = new int[] { 0, 1, 1, 2, 4, 4, 6, 4, 5, 6, 7, 7, 7, 8, 8, 8, 8, 8 };
		fromPos = new int[] { 0, 2, 5 };
		toPos = new int[] { 2, 4, 8 };
		holeThreshold = 9;

		/*
		 * Generation 211 Best avg score 49.4222372651887 0.506346251480792 other 1:
		 * 49.397529192756814 0.5058385513623287 other 2: 49.356067016415636
		 * 0.5088847520731088
		 * 
		 * lp = new int[] {0, 0, 1, 1, 1, 2, 2, 4, 5, 6, 6, 6, 7, 7, 8, 8, 8, 8}; hp =
		 * new int[] {0, 1, 1, 2, 4, 4, 6, 4, 5, 6, 7, 7, 7, 8, 8, 8, 8, 8}; fromPos =
		 * new int[] {0, 2, 5}; toPos = new int[] {2, 2, 8}; holeThreshold = 9;
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
	public DiceRoll getDiceRoll() {
		DiceRoll t = DiceRoll.flagToDiceThrow(rnd.nextInt(7));
		int[] flaglist = randomPermutation(7);
		for (int k = holeThreshold; k > 0; k--) {
			for (int i = 0; i < 7; i++) {
				t = DiceRoll.flagToDiceThrow(flaglist[i]);
				int fromI = fromPos[t.getNumberOfDice() - 1];
				int toI = toPos[t.getNumberOfDice() - 1];
				if (numberOfHoles(t, fromI, toI) >= k) return t;
			}
		}
		return t;
	}

	private int numberOfHoles(DiceRoll dThrow, int fromI, int toI) {
		int number = 0;
		if (dThrow.red) {
			int[] line = paper.getRedLine();
			for (int i = fromI; i < toI + 1; i++) {
				if (line[i] == 0) number++;
			}
		}
		if (dThrow.yellow) {
			int[] line = paper.getYellowLine();
			for (int i = fromI; i < toI + 1; i++) {
				if (line[i] == 0) number++;
			}
		}
		if (dThrow.purple) {
			int[] line = paper.getPurpleLine();
			for (int i = fromI; i < toI + 1; i++) {
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
			if (lp[i] > hp[i]) throw new IllegalArgumentException();
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
		return "EE2";
	}

	@Override
	public DiceRoll getDiceThrowRnd(double probabilityRandom) {
		return getDiceRoll();
	}

	@Override
	public int[] getActionFlagListRnd(double probabilityRandom, int diceNumber, DiceRoll thrown, boolean untilRethrow) {
		return getActionFlagList(diceNumber, thrown);
	}

}
