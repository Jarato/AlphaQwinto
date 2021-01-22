package game;

import java.util.Random;

import game.experiments.multistat.data.MatchData;
import game.experiments.multistat.data.TurnData;
import game.qwplayer.dev.QwinPlayer_t;

public class QwintoMatch {
	private MatchData match_data;
	private QwinPlayer_t[] player;
	private QwinDice dice;
	private int currentPlayerIndex;
	private int turn;
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

	public void setRawData(MatchData match_data_s) {
		match_data = match_data_s;
		match_data.generateBlankePlayerDatas(player);
	}

	public void calculateMatch() {
		turn = 1;
		while (!matchEnd) {
			TurnData turn_data = match_data.generateBlankTurnData();
			turn_data.turn_number = turn;
			turn_data.turn_of_player_idx = currentPlayerIndex;
			turn_data.players_action = new int[player.length];
			// the players turn
			DiceRoll roll = currentPlayerTurn();
			// go through all the other players and they can decide to enter or not to enter
			// the number
			for (int i = 0; i < player.length; i++) {
				if (i != currentPlayerIndex) {
					QwinPlayer_t p = player[i];
					int lastThrown = dice.getLastRolledNumber();
					int action = p.getActionFlag(lastThrown, dice.getLastThrown(), false, true);
					if (action != 1)
						paperEnterNumber(player[i].getPaper(), roll, lastThrown, action);
					turn_data.players_action[i] = action;
					p.turnEndWrapUp();
				}
				// System.out.println(player[0].getPaper()+"\n");
			}
			currentPlayerIndex = (currentPlayerIndex + 1) % player.length;
			turn++;
			if (matchEnd) {
				QwinPaper[] allPaper = new QwinPaper[player.length];
				for (int k = 0; k < player.length; k++) {
					allPaper[k] = player[k].getPaper();
				}
				for (int k = 0; k < player.length; k++) {
					player[k].matchEndWrapUp(allPaper);
					if (match_data.players[k].special_data != null)
						match_data.players[k].special_data.gatherData(player[k]);
				}
				turn--;
			}
		}
	}

	public int getNumberOfTurns() {
		return turn;
	}

	public QwinPlayer_t[] getPlayers() {
		return player;
	}

	public DiceRoll currentPlayerTurn() {
		TurnData turn_data = match_data.last_turn;
		QwinPlayer_t p = player[currentPlayerIndex];
		// Throw Dice
		DiceRoll th = p.getDiceRoll();
		turn_data.diceroll_flag = th.getDiceThrowFlag();

		int rolled_number = dice.rollDice(th);
		int action = p.getActionFlag(rolled_number, dice.getLastThrown(), true, false);
		if (action < 0 || action == 1 || action == 2 || action > 29)
			throw new IllegalArgumentException("player chose an illegal action!");
		if (action == 0) { // reroll dice
			// prepare to collect and collect raw data
			turn_data.rolledNumbers = new int[] { rolled_number, -1 };
			rolled_number = dice.rerollDice();
			// collect raw data
			turn_data.rolledNumbers[1] = rolled_number;
			action = p.getActionFlag(rolled_number, dice.getLastThrown(), false, false);
			if (action < 2 || action > 29)
				throw new IllegalArgumentException("player chose an illegal action!");
			turn_data.players_action[currentPlayerIndex] = action;
			paperEnterNumber(p.getPaper(), th, rolled_number, action);

		} else {
			// if not reroll
			// collect raw data
			turn_data.rolledNumbers = new int[] { rolled_number };
			turn_data.players_action[currentPlayerIndex] = action;
			paperEnterNumber(p.getPaper(), th, rolled_number, action);
		}
		p.turnEndWrapUp();
		return th;
	}

	/**
	 * action flag 0 ReRoll<br>
	 * 1 Reject<br>
	 * 2 Misthrow<br>
	 * 3 RedPos1<br>
	 * 4 RedPos2<br>
	 * 5 RedPos3<br>
	 * 6 RedPos4<br>
	 * 7 RedPos5<br>
	 * 8 RedPos6<br>
	 * 9 RedPos7<br>
	 * 10 RedPos8<br>
	 * 11 RedPos9<br>
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
	 *         0 - if rethrow dice 1 - if the number was entered 2 - if end of list,
	 *         means misthrow
	 */
	private void paperEnterNumber(QwinPaper paper, DiceRoll roll, int number, int action_flag) {
		if (action_flag == 2)
			paper.misthrow();
		else {
			int color = (action_flag - 3) / 9;
			int pos = (action_flag - 3) % 9;
			boolean colorOkay = (color == 0 && roll.red) || (color == 1 && roll.yellow) || (color == 2 && roll.purple);
			if (!colorOkay || !paper.isPositionValidForNumber(color, pos, number))
				throw new IllegalArgumentException("player chose an illegal action!");
			paper.enterNumber(color, pos, number);
		}
		if (!matchEnd)
			matchEnd = paper.isEndCondition();
	}

}
