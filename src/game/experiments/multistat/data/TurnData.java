package game.experiments.multistat.data;

public class TurnData {
	public int turn_number;
	public int turn_of_player_idx;
	public int diceroll_flag;
	public int[] rolledNumbers;
	// index fits to the index of the player, this turns player decision will be set to the final action, not the reroll-decision.
	public int[] players_action;
}
