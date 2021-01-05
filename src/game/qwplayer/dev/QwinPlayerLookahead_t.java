package game.qwplayer.dev;

import java.util.Random;

import game.DiceRoll;
import game.QwinPaper;
import model.FeedForwardNetwork;
import model.functions.Activation;

public abstract class QwinPlayerLookahead_t extends QwinPlayer_t {
	
	public QwinPlayerLookahead_t(Random rnd) {
		super(rnd);
	}

	protected abstract double evaluatePaper(QwinPaper paper);

	/**
	 * 
	 * @return three probability distribution tables for the three different number of dice we can roll
	 */
	protected double[][] generateDiceProbTables() {
		return new double[][] {{ 1. / 6., 1. / 6., 1. / 6., 1. / 6., 1. / 6., 1. / 6., 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0 }, { 0, 1. / 36., 2. / 36., 3. / 36., 4. / 36., 5. / 36., 6. / 36., 5. / 36., 4. / 36.,
					3. / 36., 2. / 36., 1. / 36., 0, 0, 0, 0, 0, 0 }, { 0, 0, 1. / 216., 3. / 216., 6. / 216., 10. / 216., 15. / 216., 21. / 216., 25. / 216.,
						27. / 216., 27. / 216., 25. / 216., 21. / 216., 15. / 216., 10. / 216., 6. / 216., 3. / 216.,
						1. / 216. }};
	}

	public abstract void recordData();

	public abstract void fillHistoryScoreData(Integer value);
	
	@Override
	public DiceRoll getDiceThrow() {
		// temporarily misthrow to calculate the evaluation when we get a misthrow
		this.paper.misthrow();
		double misthrowPaperEval = evaluatePaper(paper);
		this.paper.removeMisthrow();
		// save for all three lanes (red, yellow, purple) the best position and predicted end-score for all numbers (1-18) we can roll.
		double[][] lane_number_bestPosEval = new double[3][18];
		// go through every possible number we can achieve on dicerolls
		for (int number = 1; number < 19; number++) {
			// add misthrow eval and -1 as position to flag the misthrow
			lane_number_bestPosEval[0][number - 1] = misthrowPaperEval;
			// calculate optimal position for the number on each of the three lanes (red, yellow, purple)
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
						// if our evaluation with that new number is better than the previous evaluation, replace it
						if (paperEval > lane_number_bestPosEval[lane][number-1]) {
							lane_number_bestPosEval[lane][number-1] = paperEval;
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
		double bestDRExpectedValue = Double.NEGATIVE_INFINITY;
		// go through all dice combinations via the dice-flag 0,1,2,3,4,5,6
		for (int flag = 0; flag < 7; flag++) {
			DiceRoll roll = DiceRoll.flagToDiceThrow(flag);
			// number of dice we roll
			int numDiceOfRoll = roll.getNumberOfDice();
			double diceRollExpectedValue = 0;
			// go through every number
			for (int number = 1; number < 19; number++) {
				double bestEval = -20;
				// if we roll more than one dice, we take the highest expected value over all chosen lanes for a rolled number
				if (roll.red && lane_number_bestPosEval[0][number-1]>bestEval) bestEval = lane_number_bestPosEval[0][number-1];
				if (roll.yellow && lane_number_bestPosEval[1][number-1]>bestEval) bestEval = lane_number_bestPosEval[1][number-1];
				if (roll.purple && lane_number_bestPosEval[2][number-1]>bestEval) bestEval = lane_number_bestPosEval[2][number-1];
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
		return DiceRoll.flagToDiceThrow(bestDiceRoll_flag);
	}

	@Override
	public int[] getActionFlagList(int diceNumber, DiceRoll thrown) {
		return null;
	}
	
	@Override
	public int getActionFlag(int diceNumber, DiceRoll roll, boolean reRollable, boolean rejectable) {
		// temporarily add a misthrow to get the evaluation of the paper with a misthrow
		this.paper.misthrow();
		double misthrowPaperEval = evaluatePaper(paper);
		// undo the misthrow
		this.paper.removeMisthrow();
		double bestEval = Double.NEGATIVE_INFINITY;
		int bestAction = -1;
		// if this rolled number is neither rerollabled nor rejectable, then it's the rerolled number and it's our turn, so we could misthrow
		if (!reRollable && !rejectable) {
			bestEval = misthrowPaperEval;
			// action flag 2 is the misthrow action
			bestAction = 2;
		}
		// if this rolled number is rejectable, then it's not our turn, we check if we want to enter a number by evaluating the paper without changing anything
		if (rejectable) {
			double rejectEval = evaluatePaper(paper);
			//System.out.println("evaluation just now: "+rejectEval);
			if (rejectEval > bestEval) {
				bestEval = rejectEval;
				// action flag 1 is the reject action
				bestAction = 1;
			}
		}
		// go through all actions
		for (int action_f = 3; action_f < 30; action_f++) {
			int color = (action_f-3)/9;
			// check if we are even allowed to enter the number in this color lane
			if ((color == 0 && roll.red) || (color == 1 && roll.yellow) || (color == 2 && roll.purple)) {
				//calculate position where to enter
				int pos = (action_f-3)%9;
				if (paper.isPositionValidForNumber(color, pos, diceNumber)) {
					// enter the number temporarilty
					paper.enterNumber(color, pos, diceNumber);
					double paperEval = evaluatePaper(paper);
					if (paperEval > bestEval) {
						bestEval = paperEval;
						bestAction = action_f;
					}
					//undo the number addition
					paper.enterNumber(color, pos, 0);
				}
			}
		}
		// if this roll is reRollable, we have the option to reroll. We evaluate that option by calculating the expected predicted score when we reroll using the same dice.
		if (reRollable) {
			// number of dice 1, 2, 3. => the DiceProbTables consists of three tables positioned at index 0, 1, 2 for the three different number of dice we can use (1,2,3)
			double[] rollProbDistribution = generateDiceProbTables()[roll.getNumberOfDice()-1];
			double rerollEvaluation = 0;
			// go over all 18 numbers
			for (int checkNumber = 0; checkNumber<rollProbDistribution.length; checkNumber++) {
				// only look at the numbers for which we can roll (probability > 0)
				if (rollProbDistribution[checkNumber] > 0) {
					// we want to find the best position for this possible number!
					double bestEvalforNumberInReroll = misthrowPaperEval;
					int putInNumber = checkNumber+1;
					// for this number, go over all 9 positions on the paper and over all the lanes for which we can roll the color
					for (int pos = 0; pos < 9; pos++) {
						// check all three colors, if we are able to roll this color and enter the number in the position, we evaluate the position:
						// check red
						if (roll.red && paper.isPositionValidForNumber(0, pos, putInNumber)) {
							// temporarily put the number into that position
							paper.enterNumber(0, pos, putInNumber);
							// evaluate the paper and update our
							double eval = evaluatePaper(paper);
							if (eval > bestEvalforNumberInReroll) {
								bestEvalforNumberInReroll = eval;
							}
							// undo the temporary number
							paper.enterNumber(0, pos, 0);
						}
						// check yellow
						if (roll.yellow && paper.isPositionValidForNumber(1, pos, putInNumber)) {
							// temporarily put the number into that position
							paper.enterNumber(1, pos, putInNumber);
							// evaluate the paper and update our
							double eval = evaluatePaper(paper);
							if (eval > bestEvalforNumberInReroll) {
								bestEvalforNumberInReroll = eval;
							}
							// undo the temporary number
							paper.enterNumber(1, pos, 0);
						}
						// check purple
						if (roll.purple && paper.isPositionValidForNumber(2, pos, putInNumber)) {
							// temporarily put the number into that position
							paper.enterNumber(2, pos, putInNumber);
							// evaluate the paper and update our
							double eval = evaluatePaper(paper);
							if (eval > bestEvalforNumberInReroll) {
								bestEvalforNumberInReroll = eval;
							}
							// undo the temporary number
							paper.enterNumber(2, pos, 0);
						}
					}
					// multiply the probability with the best possible predicted eval for this number, calculating the expected value
					rerollEvaluation += bestEvalforNumberInReroll*rollProbDistribution[checkNumber];
				}
			}
			// check if we want to reroll
			if (rerollEvaluation > bestEval) {
				bestEval = rerollEvaluation;
				bestAction = 0;
			}
		}
		if (bestAction != 0) recordData();
		if (bestAction == -1) throw new IllegalArgumentException();
		return bestAction;
	}

}
