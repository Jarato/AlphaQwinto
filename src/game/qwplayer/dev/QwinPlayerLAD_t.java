package game.qwplayer.dev;

import java.util.ArrayList;
import java.util.Random;

import game.DiceRoll;
import game.QwinPaper;
import game.experiments.multistat.data.PlayerData_S;
import game.experiments.multistat.data.PlayerLA_Data;

public abstract class QwinPlayerLAD_t extends QwinPlayer_t{
	protected int numOfPossibleRejects;
	protected int numOfActualRejects;
	protected double noiselevel = 0;
	protected double lastNoise = 0;
	protected double[] actionForRolledNumber = new double[18];
	protected double[] actionForRerolledNumber = new double[18];
	protected double[] actionRolledNumber_denoised = new double[18];
	protected double[] actionRerolledNumber_denoised = new double[18];
	protected ArrayList<Integer> denoisedDecisions = new ArrayList<Integer>();
	protected ArrayList<Double> paperEval_turn = new ArrayList<Double>();
	protected static double[][] diceProbabilityTable = new double[][] {
			{ 1. / 6., 1. / 6., 1. / 6., 1. / 6., 1. / 6., 1. / 6., 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 1. / 36., 2. / 36., 3. / 36., 4. / 36., 5. / 36., 6. / 36., 5. / 36., 4. / 36., 3. / 36., 2. / 36.,
					1. / 36., 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 1. / 216., 3. / 216., 6. / 216., 10. / 216., 15. / 216., 21. / 216., 25. / 216., 27. / 216.,
					27. / 216., 25. / 216., 21. / 216., 15. / 216., 10. / 216., 6. / 216., 3. / 216., 1. / 216. } };

	public QwinPlayerLAD_t(Random rnd) {
		super(rnd);
	}
	
	public ArrayList<Double> getpaperEval_turn(){
		return paperEval_turn;
	}
	
	@Override
	public PlayerData_S generatePlayerDataCollector() {
		return new PlayerLA_Data();
	}
	
	@Override
	public void matchEndWrapUp(QwinPaper[] allPaper) {
		super.matchEndWrapUp(allPaper);
		/*if (print) {
			System.out.println("all denoised decision flags:");
			for (Integer den_flag : denoisedDecisions) {
				System.out.println(den_flag);
			}
			System.out.println("end of recording of denoised decisions");
		}*/
	}
	
	@Override
	public void turnEndWrapUp() {
		paperEval_turn.add(evaluatePaper(this.paper));
	}

	public void setNoiseLevel(double setNoise) {
		noiselevel = setNoise;
	}
	
	public ArrayList<Integer> getDenoisedDecisions(){
		return denoisedDecisions;
	}

	public abstract double evaluatePaper(QwinPaper paper);

	public double getProportionOfRejects() {
		return numOfActualRejects / ((double) numOfPossibleRejects);
	}

	private double noisedEval(QwinPaper paper) {
		lastNoise = rnd.nextGaussian() * noiselevel;
		double noisedEval = evaluatePaper(paper) + lastNoise;
		return noisedEval;
	}

	private double evaluateSimulatedMisthrow() {
		// temporarily misthrow to evalute a misthrow
		this.paper.misthrow();
		numOfTurns++;
		double misthrowEval = noisedEval(paper);
		this.paper.removeMisthrow();
		numOfTurns--;
		return misthrowEval;
	}

	private double evaluateSimulatedReject() {
		numOfTurns++;
		double rejectEval = noisedEval(paper);
		numOfTurns--;
		return rejectEval;
	}

	private double evaluateSimulatedEnteringOfNumber(int lane, int position, int number) {
		this.paper.enterNumber(lane, position, number);
		numOfTurns++;
		double enterNumberEval = noisedEval(paper);
		this.paper.enterNumber(lane, position, 0);
		numOfTurns--;
		return enterNumberEval;
	}
	
	private int diceRollFlag_to_denoisedDecisionFlag(int diceRollFlag) {
		return -(1+diceRollFlag);
	}

	@Override
	public DiceRoll getDiceRoll() {
		// temporarily misthrow to calculate the evaluation when we get a misthrow
		double misthrowPaperEval = evaluateSimulatedMisthrow();
		double misthrowPaperEval_denoised = misthrowPaperEval - lastNoise;
		// save for all three lanes (red, yellow, purple) the best position and
		// predicted end-score for all numbers (1-18) we can roll.
		double[][][] lane_number_bestEvalNAction = new double[3][18][2];
		double[][][] no_noise_table = new double[3][18][2];
		// go through every possible number we can achieve on dicerolls
		for (int number = 1; number < 19; number++) {
			// calculate optimal position for the number on each of the three lanes (red,
			// yellow, purple)
			// go through every lane
			for (int lane = 0; lane < 3; lane++) {
				// add misthrow eval and -1 as position to flag the misthrow
				lane_number_bestEvalNAction[lane][number - 1] = new double[] { misthrowPaperEval, 2 };
				no_noise_table[lane][number - 1] = new double[] { misthrowPaperEval_denoised, 2};
				// go through every position
				for (int pos = 0; pos < 9; pos++) {
					// check if we can put in the number at the position in this lane
					if (paper.isPositionValidForNumber(lane, pos, number)) {
						// get eval for the paper with the new number
						double paperEval = evaluateSimulatedEnteringOfNumber(lane, pos, number);
						double paperEval_denoised = paperEval - lastNoise;
						if (paperEval_denoised > no_noise_table[lane][number - 1][0]) {
							no_noise_table[lane][number - 1] = new double[] {paperEval_denoised, 3+ lane*9 + pos};
						}
						// if our evaluation with that new number is better than the previous
						// evaluation, replace it
						if (paperEval > lane_number_bestEvalNAction[lane][number - 1][0]) {
							lane_number_bestEvalNAction[lane][number - 1] = new double[] { paperEval,
									3 + lane * 9 + pos };
						}
					}
				}
			}
		}
		// calculate expected predicted value chosing different sets of dice
		double[][] res = calculateBestDiceRollandTables(lane_number_bestEvalNAction, misthrowPaperEval);
		actionForRerolledNumber = res[1];
		actionForRolledNumber = res[2];
		// same with the denoised predictions
		double[][] res_denoised = calculateBestDiceRollandTables(no_noise_table, misthrowPaperEval_denoised);
		actionRerolledNumber_denoised = res_denoised[1];
		actionRolledNumber_denoised = res_denoised[2];
		// add the decision to the denoised decisions
		denoisedDecisions.add(diceRollFlag_to_denoisedDecisionFlag((int)res_denoised[0][0]));
		// return the best found dice roll combination
		return DiceRoll.flagToDiceThrow((int)res[0][0]);
	}

	private double[][] calculateBestDiceRollandTables(double[][][] ln_table_EvalPos, double misthrowEval) {
		double[][] returnTable = new double[3][];
		// we find the best dice roll combination by looking at their expected value
		double bestDRExpectedValue = Double.NEGATIVE_INFINITY;
		// flag expected value distribution
		double[] flag_expected_value = new double[7];
		// sampled actions from distribution
		double[][] sampled_actions_per_number = new double[7][18];
		// go through all dice combinations via the dice-flag 0,1,2,3,4,5,6
		for (int flag = 0; flag < 7; flag++) {
			DiceRoll roll = DiceRoll.flagToDiceThrow(flag);
			// number of dice we roll
			int numDiceOfRoll = roll.getNumberOfDice();
			double diceReRollExpectedValue = 0;
			double[] actionForRerolledNumber_temp = new double[18];
			// go through every number for a first round, where we calculate the expected
			// value of these dice, when we roll once
			for (int number = 1; number < 19; number++) {
				double bestEval = misthrowEval;
				actionForRerolledNumber_temp[number - 1] = 2;
				// if we roll more than one dice, we take the highest expected value over all
				// chosen lanes for a rolled number
				if (roll.red && ln_table_EvalPos[0][number - 1][0] > bestEval) {
					bestEval = ln_table_EvalPos[0][number - 1][0];
					actionForRerolledNumber_temp[number - 1] = (int) ln_table_EvalPos[0][number - 1][1];
				}
				if (roll.yellow && ln_table_EvalPos[1][number - 1][0] > bestEval) {
					bestEval = ln_table_EvalPos[1][number - 1][0];
					actionForRerolledNumber_temp[number - 1] = (int) ln_table_EvalPos[1][number - 1][1];
				}
				if (roll.purple && ln_table_EvalPos[2][number - 1][0] > bestEval) {
					bestEval = ln_table_EvalPos[2][number - 1][0];
					actionForRerolledNumber_temp[number - 1] = (int) ln_table_EvalPos[2][number - 1][1];
				}
				// predicted value for that number TIMES probability of roll
				diceReRollExpectedValue += bestEval * diceProbabilityTable[numDiceOfRoll - 1][number - 1];
			}
			double diceRollExpectedValue = 0;
			double[] actionForRolledNumber_temp = new double[18];
			// We expect to reroll the rolled number, when the expected score of the rolled
			// number is lower than the expected value of a reroll
			for (int number = 1; number < 19; number++) {
				// we initialize the second round of calculation with the expected value of the
				// reroll-action of the chosen dice.
				double bestEval = diceReRollExpectedValue;
				actionForRolledNumber_temp[number - 1] = 0;
				// if we roll more than one dice, we take the highest expected value over all
				// chosen lanes for a rolled number
				if (roll.red && ln_table_EvalPos[0][number - 1][0] > bestEval) {
					bestEval = ln_table_EvalPos[0][number - 1][0];
					actionForRolledNumber_temp[number - 1] = (int) ln_table_EvalPos[0][number - 1][1];
				}
				if (roll.yellow && ln_table_EvalPos[1][number - 1][0] > bestEval) {
					bestEval = ln_table_EvalPos[1][number - 1][0];
					actionForRolledNumber_temp[number - 1] = (int) ln_table_EvalPos[1][number - 1][1];
				}
				if (roll.purple && ln_table_EvalPos[2][number - 1][0] > bestEval) {
					bestEval = ln_table_EvalPos[2][number - 1][0];
					actionForRolledNumber_temp[number - 1] = (int) ln_table_EvalPos[2][number - 1][1];
				}
				if (actionForRolledNumber_temp[number - 1] == 2) {
					actionForRolledNumber_temp[number - 1] = 0;
					bestEval = diceReRollExpectedValue;
				}
				// predicted value for that number TIMES probability of roll
				diceRollExpectedValue += bestEval * diceProbabilityTable[numDiceOfRoll - 1][number - 1];
			}
			flag_expected_value[flag] = diceRollExpectedValue;
			// SAMPLE THE DICE FLAG
			
			// if the dice roll has a higher expected value, replace it with the current
			// looked at dice roll combination
			if (diceRollExpectedValue > bestDRExpectedValue) {
				// 0 = flag
				returnTable[0] = new double[] {flag};
				bestDRExpectedValue = diceRollExpectedValue;
				// 1 = reroll
				returnTable[1] = actionForRerolledNumber_temp;
				// 2 = roll
				returnTable[2] = actionForRolledNumber_temp;
			}
		}
		return returnTable;
	}

	@Override
	public int[] getActionFlagList(int diceNumber, DiceRoll thrown) {
		return null;
	}

	@Override
	public int getActionFlag(int diceNumber, DiceRoll roll, boolean reRollable, boolean rejectable) {
		if (reRollable) {
			denoisedDecisions.add((int)actionRolledNumber_denoised[diceNumber - 1]);
			return (int)actionForRolledNumber[diceNumber - 1];
		}
		// if this rolled number is neither rerollabled nor rejectable, then it's the
		// rerolled number and it's our turn, so we could misthrow
		if (!reRollable && !rejectable) {
			denoisedDecisions.add((int)actionRerolledNumber_denoised[diceNumber - 1]);
			return (int)actionForRerolledNumber[diceNumber - 1];
			// bestEval = misthrowPaperEval;
			// action flag 2 is the misthrow action
			// bestAction = 2;
		}
		double bestEval = Double.NEGATIVE_INFINITY;
		double bestEval_denoised = Double.NEGATIVE_INFINITY;
		boolean trueRejectPossible = false;
		int bestAction = -1;
		// if this rolled number is rejectable, then it's not our turn, we check if we
		// want to enter a number by evaluating the paper without changing anything
		if (rejectable) {
			double rejectEval = evaluateSimulatedReject();
			
			// System.out.println("evaluation just now: "+rejectEval);
			if (rejectEval > bestEval) {
				bestEval = rejectEval;
				// action flag 1 is the reject action
				bestAction = 1;
			}
			double rejectEval_denoised = rejectEval - lastNoise;
			if (rejectEval_denoised > bestEval_denoised) {
				bestEval_denoised = rejectEval_denoised;
			}
		}
		// go through all actions
		for (int action_f = 3; action_f < 30; action_f++) {
			int color = (action_f - 3) / 9;
			// check if we are even allowed to enter the number in this color lane
			if ((color == 0 && roll.red) || (color == 1 && roll.yellow) || (color == 2 && roll.purple)) {
				// calculate position where to enter
				int pos = (action_f - 3) % 9;
				if (paper.isPositionValidForNumber(color, pos, diceNumber)) {
					// set the boolean true, this means, this player can actually choose to reject
					// instead of being forced to reject
					trueRejectPossible = true;
					double paperEval = evaluateSimulatedEnteringOfNumber(color, pos, diceNumber);
					if (paperEval > bestEval) {
						bestEval = paperEval;
						bestAction = action_f;
					}
					// calculate the best action without noise
					double paperEval_denoised = paperEval - lastNoise;
					if (paperEval_denoised > bestEval_denoised) {
						bestEval_denoised = paperEval_denoised;
					}
				}
			}
		}
		if (bestAction == -1)
			throw new IllegalArgumentException();
		if (rejectable && trueRejectPossible)
			numOfPossibleRejects++;
		if (trueRejectPossible && bestAction == 1)
			numOfActualRejects++;
		denoisedDecisions.add(bestAction);
		return bestAction;
	}

}
