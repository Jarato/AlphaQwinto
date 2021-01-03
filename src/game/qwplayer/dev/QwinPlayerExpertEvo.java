package game.qwplayer.dev;

import java.util.Random;

import game.DiceRoll;
import game.QwinDice;
import pdf.ai.dna.DNA;
import pdf.ai.dna.Evolutionizable;

public class QwinPlayerExpertEvo extends QwinPlayerEvo_t  {
	private int[] lp;
	private int[] hp;
	private int[] fromPos;
	private int[] toPos;
	private int holeThreshold;

	
	public QwinPlayerExpertEvo(Random rnd) {
		super(rnd);
	}
	
	public QwinPlayerExpertEvo(Random rnd, DNA initDNA) {
		super(rnd, initDNA);
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
		DiceRoll t = DiceRoll.flagToDiceThrow(rnd.nextInt(7));
		int[] flaglist = randomPermutation(7);
		for (int k = holeThreshold; k > 0; k--) {
			for (int i = 0; i < 7; i++) {
				t = DiceRoll.flagToDiceThrow(flaglist[i]);
				int fromI = fromPos[t.getNumberOfDice()-1];
				int toI = toPos[t.getNumberOfDice()-1];
				if (numberOfHoles(t, fromI, toI) >= k) return t;
			}
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
		return "Expert with DNA";
	}

	@Override
	public void compoundDNA() {
		lp = new int[18];
		hp = new int[18];
		fromPos = new int[3];
		toPos = new int[3];
		double[] dnaValues = dna.getGeneValues();
		for (int i = 0; i < 18; i++) {
			if (dnaValues[i*2]>dnaValues[i*2+1]) {
				hp[i] = (int) dna.getNormedGene(i*2, 0, 8.99999);
				lp[i] = (int) dna.getNormedGene(i*2+1, 0, 8.99999);
			} else {
				hp[i] = (int) dna.getNormedGene(i*2+1, 0, 8.99999);
				lp[i] = (int) dna.getNormedGene(i*2, 0, 8.99999);
			}
		}
		for (int j = 0; j < 3; j++) {
			int i = 36;
			if (i+j*2 == 42 || i+j*2+1 == 42) throw new IllegalArgumentException();
			if (dnaValues[i+j*2]>dnaValues[i+j*2+1]) {
				fromPos[j] = (int) dna.getNormedGene(i+j*2+1, 0, 8.99999);
				toPos[j] = (int) dna.getNormedGene(i+j*2, 0, 8.99999);
			} else {
				fromPos[j] = (int) dna.getNormedGene(i+j*2, 0, 8.99999);
				toPos[j] = (int) dna.getNormedGene(i+j*2+1, 0, 8.99999);
			}
		}
		holeThreshold = (int)  dna.getNormedGene(42, 0, 9.99999);
	}

	@Override
	public void compoundDNA(DNA newDNA) {
		this.dna = newDNA;
		compoundDNA();
	}

	@Override
	public DNA getDNA() {
		return dna;
	}

	@Override
	public int getNumberOfNeededGenes() {
		return 43;
	}
	
	public String getSettingsCode() {
		String strLP = "lp = new int[] {";
		String strHP = "hp = new int[] {";
		for (int i = 0; i < lp.length-1; i++) {
			strLP+=lp[i]+", ";
			strHP+=hp[i]+", ";
		}
		strLP+=lp[lp.length-1]+"};\n";
		strHP+=hp[hp.length-1]+"};\n";
		String strFP = "fromPos = new int[] {";
		String strTP = "toPos = new int[] {";
		for (int i = 0; i < fromPos.length-1; i++) {
			strFP+=fromPos[i]+", ";
			strTP+=toPos[i]+", ";
		}
		strFP+=fromPos[fromPos.length-1]+"};\n";
		strTP+=toPos[fromPos.length-1]+"};\n";
		String strHT = "holeThreshold = "+holeThreshold+";";
		return strLP+strHP+strFP+strTP+strHT;
	}

}
