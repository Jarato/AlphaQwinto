package game.qwplayer;

import java.util.Random;

import game.DiceThrow;
import game.QwinDice;
import game.qwplayer.dev.QwinPlayer;

public class QwinPlayerExpert extends QwinPlayer {

	public QwinPlayerExpert(Random rnd) {
		super(rnd);
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
	public DiceThrow getDiceThrow() {
		DiceThrow t = new DiceThrow(true, true, true);
		int[] flaglist = randomPermutation(7);
		for (int i = 0; i < 7; i++) {
			t = DiceThrow.flagToDiceThrow(flaglist[i]);
			int fromI = 0;
			int toI = 8;
			switch (t.getNumberOfDice()) {
			case 1: {
				fromI = 0;
				toI = 2;
			}
				break;
			case 2: {
				fromI = 3;
				toI = 3;
			}
				break;
			case 3: {
				fromI = 2;
				toI = 8;
			}
				break;
			}
			if (numberOfHoles(t, fromI, toI) > 1) return t;
		}
		return t;
	}

	private int numberOfHoles(DiceThrow dThrow, int fromI, int toI) {
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

	private void printActionFlagList(int[] list) {
		for (int i = 0; i < list.length; i++) {
			if (list[i] == 0)
				System.out.print("rethrow \t");
			else {
				int color = (list[i] - 1) / 9 + 1;
				int pos = (list[i] - 1) % 9 + 1;
				System.out.print("|c=" + color + " p=" + pos + "\t");
			}
		}
		System.out.println();
	}

	@Override
	public int[] getActionFlagList(int diceNumber, DiceThrow thrown) {
		int[] perm = randomPermutation(28);
		// printActionFlagList(perm);
		int[] lp = new int[18]; // lowest position for that number
		int[] hp = new int[18]; // highest position for that number
		lp[0] = 0;
		lp[1] = 0;
		lp[2] = 1;
		lp[3] = 1;
		lp[4] = 1;
		lp[5] = 2;
		lp[6] = 2;
		lp[7] = 3;
		lp[8] = 3;
		lp[9] = 4;
		lp[10] = 5;
		lp[11] = 5;
		lp[12] = 5;
		lp[13] = 5;
		lp[14] = 6;
		lp[15] = 6;
		lp[16] = 7;
		lp[17] = 8;
		hp[0] = 0;
		hp[1] = 1;
		hp[2] = 1;
		hp[3] = 2;
		hp[4] = 3;
		hp[5] = 3;
		hp[6] = 4;
		hp[7] = 5;
		hp[8] = 6;
		hp[9] = 6;
		hp[10] = 7;
		hp[11] = 7;
		hp[12] = 7;
		hp[13] = 8;
		hp[14] = 8;
		hp[15] = 8;
		hp[16] = 8;
		hp[17] = 8;
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
		return "Expert";
	}

}
