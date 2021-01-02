package game.qwplayer.dev;

import java.util.Random;

import game.DiceThrow;
import game.QwinPaper;
import model.FeedForwardNetwork;

public class QwinPlayerNNRegression extends QwinPlayer {

	private FeedForwardNetwork scoreEvalNetwork;

	public QwinPlayerNNRegression(Random initRnd) {
		super(initRnd);
		initNet();
	}

	private void initNet() {
		scoreEvalNetwork = new FeedForwardNetwork();
		// scoreEvalNetwork.addBlock(inputLength, outputLength, withBias, funcEnum);
	}

	private double evaluatePaper(QwinPaper paper) {
		if (paper.isEndCondition()) {
			return paper.calculateScore();
		}
		return 0;
	}

	public static void main(String[] args) {
		QwinPlayerNNRegression bla = new QwinPlayerNNRegression(null);
		double[][] test = bla.generateDiceProbTables();
		System.out.println("length: " + test.length);
		for (int i = 0; i < test[0].length; i++) {
			System.out.println(test[0][i] + "\t"+ test[1][i] + "\t" + test[2][i]);
		}
	}

	protected double[][] generateDiceProbTables() {
		return new double[][] {{ 1. / 6., 1. / 6., 1. / 6., 1. / 6., 1. / 6., 1. / 6., 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0 }, { 0, 1. / 36., 2. / 36., 3. / 36., 4. / 36., 5. / 36., 6. / 36., 5. / 36., 4. / 36.,
					3. / 36., 2. / 36., 1. / 36., 0, 0, 0, 0, 0, 0 }, { 0, 0, 1. / 216., 3. / 216., 6. / 216., 10. / 216., 15. / 216., 21. / 216., 25. / 216.,
						27. / 216., 27. / 216., 25. / 216., 21. / 216., 15. / 216., 10. / 216., 6. / 216., 3. / 216.,
						1. / 216. }};
	}

	@Override
	public DiceThrow getDiceThrow() {
		// temporarily misthrow to calculate the evaluation when we get a misthrow
		this.paper.misthrow();
		double misthrowPaperEval = evaluatePaper(paper);
		this.paper.removeMisthrow();
		double[][][] lane_number_bestPosEval = new double[3][18][2];
		// go through every possible number we can achieve on dicerolls
		for (int number = 1; number < 19; number++) {
			// add misthrow eval and -1 as position to flag the misthrow
			lane_number_bestPosEval[0][number - 1][0] = -1;
			lane_number_bestPosEval[0][number - 1][1] = misthrowPaperEval;
			// calculate optimal position for the number on each of the three lanes (red,
			// yellow, purple)
			// go through every lane
			for (int lane = 0; lane < 3; lane++) {
				// go through every position
				for (int pos = 0; pos < 9; pos++) {
					// check if we can put in the number at the position in this lane
					if (paper.isPositionValidForNumber(lane, pos, number)) {
						// temporarily put the number there
						paper.enterNumber(lane, pos, number);
						// get eval for the paper with the new number
						double paperEval = evaluatePaper(paper);
						if (paperEval > lane_number_bestPosEval[lane][pos][1]) {
							lane_number_bestPosEval[lane][pos][1] = paperEval;
							lane_number_bestPosEval[lane][pos][0] = pos;
						}
						// undo the number by putting in a 0 at the same position
						paper.enterNumber(lane, pos, 0);
					}
				}
			}
		}
		// calculate expected predicted value chosing different sets of dice
		// probability distributions for rolling 1, 2 and all 3 dices
		double[][] diceprobs = generateDiceProbTables();
		// we find the best dice roll combination by looking at their expected value
		int bestDiceRoll_flag = -1;
		double bestDRExpectedValue = -20;
		// go through all dice combinations via the dice-flag 0,1,2,3,4,5,6
		for (int flag = 0; flag < 7; flag++) {
			DiceThrow roll = DiceThrow.flagToDiceThrow(flag);
			// number of dice rolled
			int numDiceOfRoll = roll.getNumberOfDice();
			double diceRollExpectedValue = 0;
			// go through every number
			for (int number = 1; number < 19; number++) {
				double bestEval = -20;
				// if we roll more than one dice, we take the highest expected value over all chosen lanes for a rolled number
				if (roll.red && lane_number_bestPosEval[0][number-1][1]>bestEval) bestEval = lane_number_bestPosEval[0][number-1][1];
				if (roll.yellow && lane_number_bestPosEval[1][number-1][1]>bestEval) bestEval = lane_number_bestPosEval[1][number-1][1];
				if (roll.purple && lane_number_bestPosEval[2][number-1][1]>bestEval) bestEval = lane_number_bestPosEval[2][number-1][1];
				// predicted value for that number TIMES probability of roll
				diceRollExpectedValue += bestEval*diceprobs[numDiceOfRoll-1][number-1];
			}
			// if the dice roll has a higher expected value, replace it with the current looked at dice roll combination
			if (diceRollExpectedValue > bestDRExpectedValue) {
				bestDiceRoll_flag = flag;
				bestDRExpectedValue = diceRollExpectedValue;
			}
		}
		// return the best found dice roll combination 
		return DiceThrow.flagToDiceThrow(bestDiceRoll_flag);
	}

	@Override
	public int[] getActionFlagList(int diceNumber, DiceThrow thrown) {

		return null;
	}

	@Override
	public String getName() {
		return "State Evaluate Lookahead - NN";
	}

}
