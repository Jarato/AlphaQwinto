package game.qwplayer.dev;

import java.util.Random;

import game.DiceThrow;
import game.QwinDice;
import pdf.ai.dna.DNA;
import pdf.ai.dna.Evolutionizable;

public abstract class QwinPlayerEvo extends QwinPlayer implements Evolutionizable  {
	protected DNA dna;
	
	public QwinPlayerEvo(Random rnd) {
		super(rnd);
		dna = new DNA(getNumberOfNeededGenes());
		dna.setRandom(rnd);
		compoundDNA();
	}
	
	public QwinPlayerEvo(Random rnd, DNA initDNA) {
		super(rnd);
		if (initDNA.getNumberOfGenes()!=getNumberOfNeededGenes()) throw new IllegalArgumentException();
		compoundDNA();
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
	public abstract DiceThrow getDiceThrow();


	@Override
	public abstract int[] getActionFlagList(int diceNumber, DiceThrow thrown);

	@Override
	public abstract String getName();

	@Override
	public abstract void compoundDNA() ;

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
	public abstract int getNumberOfNeededGenes();
	
	public abstract String getSettingsCode() ;

}
