package game.experiments.multistat.analyze;

import java.util.ArrayList;

import game.DiceRoll;
import game.QwinPaper;
import game.experiments.multistat.data.AllDataRaw;
import game.experiments.multistat.data.MatchData;
import game.experiments.multistat.data.PlayerData;
import game.experiments.multistat.data.TurnData;
import pdf.util.Pair;

public class RawDataAnalyzer {
	private static class ColorPosition {
		public int color;
		public int position;
	}
	
	public static ArrayList<Pair<double[], Double>> generateTrainingData_v9(AllDataRaw all_matches) {
		
		
		return null;
	}
	
	public static void printMatch(MatchData match_data) {
		QwinPaper[] player_papers = new QwinPaper[match_data.players.length];
		System.out.println("A Qwinto match between "+match_data.players.length+" players.");
		System.out.print("Player names: "+match_data.players[0].name);
		player_papers[0] = new QwinPaper();
		for (int j = 1; j < match_data.players.length; j++) {
			player_papers[j] = new QwinPaper();
			System.out.print(", "+match_data.players[j].name);
		}
		System.out.println("\n");
		for (TurnData td : match_data.turns) {
			System.out.println("turn "+td.turn_number);
			System.out.println("player with index "+td.turn_of_player_idx+": "+match_data.players[td.turn_of_player_idx].name+" has to roll.");
			DiceRoll dc = DiceRoll.flagToDiceThrow(td.diceroll_flag);
			System.out.println("roll "+dc+" => "+td.rolledNumbers[0]);
			if (td.rolledNumbers.length > 1) System.out.println("the player rerolls the dice => "+td.rolledNumbers[1]);
			int rolled_number = td.rolledNumbers[td.rolledNumbers.length-1];
			int action = td.players_action[td.turn_of_player_idx];
			if (action == 2) {
				System.out.println("doesn't enter the number "+rolled_number+" => misthrow!");
			} else {
				ColorPosition cp = actionflag_to_colorposition(action);
				System.out.println("enter number "+td.rolledNumbers[td.rolledNumbers.length-1]+" in the "+(cp.color==0?"RED":(cp.color==1?"YELLOW":"PURPLE"))+" lane on position "+(cp.position+1));
			}
			applyActionOnPaper(player_papers[td.turn_of_player_idx], rolled_number, action);
			System.out.println(player_papers[td.turn_of_player_idx]);
			System.out.println(match_data.players[td.turn_of_player_idx].special_data.getTurnDataToString(td.turn_number-1));
			for (int i = 0; i < td.players_action.length; i++) {
				if (i != td.turn_of_player_idx) {
					System.out.println("player with index "+i+": "+match_data.players[i].name+" decides on entering the rolled number.");
					if (td.players_action[i] == 1) {
						System.out.println("doesn't enter the number "+rolled_number+".");
					} else {
						ColorPosition cpo = actionflag_to_colorposition(td.players_action[i]);
						System.out.println("enter number "+rolled_number+" in the "+(cpo.color==0?"RED":(cpo.color==1?"YELLOW":"PURPLE"))+" lane on position "+(cpo.position+1));
					}
					applyActionOnPaper(player_papers[i], rolled_number, td.players_action[i]);
					System.out.println(player_papers[i]);
					System.out.println(match_data.players[i].special_data.getTurnDataToString(td.turn_number-1));
				}
			}
			System.out.println();
		}
		System.out.println("\nthe match ended with the last turn.\n\nfinal papers:\n");
		for (int i = 0; i < player_papers.length; i++) {
			System.out.println("index "+i+" "+match_data.players[i].name);
			System.out.println(player_papers[i]);
			System.out.println("score:\t"+player_papers[i].calculateScore()+"\n");	
		}
	}

	/* 0  ReRoll<br>
	 * 1  Reject<br>
	 * 2  Misthrow<br>
	 * 3  RedPos1<br>
	 * 4  RedPos2<br>
	 * 5  RedPos3<br>
	 * 6  RedPos4<br>
	 * 7  RedPos5<br>
	 * 8  RedPos6<br>
	 * 9  RedPos7<br>
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
	
	public static QwinPaper applyActionOnPaper(QwinPaper paper, int number, int action) {
		if (action == 2) {
			paper.misthrow();
			return paper;
		}
		if (action == 1) return paper;
		ColorPosition cp = actionflag_to_colorposition(action);
		paper.enterNumber(cp.color, cp.position, number);
		return paper;
	}
		
	public static ColorPosition actionflag_to_colorposition(int action_flag) {
		ColorPosition cp = new ColorPosition();
		cp.color = (action_flag-3)/9;
		cp.position = (action_flag-3)%9;
		return cp;
	}
	
}
