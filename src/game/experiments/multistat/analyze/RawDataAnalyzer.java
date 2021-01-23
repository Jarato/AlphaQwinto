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
	
	public static ArrayList<Pair<double[], Double>> generateTrainingData_LANNEVAL9(AllDataRaw all_matches) {
		ArrayList<Pair<double[], Double>> training_data = new ArrayList<Pair<double[], Double>>();
		for (MatchData match : all_matches.matches) {
			for (int j = 0; j < match.players.length; j++) {
				QwinPaper paper = new QwinPaper();
				ArrayList<Pair<double[], Double>> training_one_game = new ArrayList<Pair<double[], Double>>();
				training_one_game.add(new Pair<double[], Double>(new double[83], -1.));
				for (TurnData turn : match.turns) {
					applyActionOnPaper(paper, turn.rolledNumbers[turn.rolledNumbers.length-1], turn.players_action[j]);
					double[] input = new double[83];
					int numOfTurns = turn.turn_number;
					int[] redline = paper.getRedLine();
					int[] yellowline = paper.getYellowLine();
					int[] purpleline = paper.getPurpleLine();
					boolean[][] blocked = paper.generateBlockedFields();
					for (int i = 0; i < 9; i++) {
						input[i] = redline[i]/18.;
						input[i+9] = yellowline[i]/18.;
						input[i+18] = purpleline[i]/18.;
						input[i+27] = (redline[i]>0?1.:0.);
						input[i+36] = (yellowline[i]>0?1.:0.);
						input[i+45] = (purpleline[i]>0?1.:0.);
						input[i+54] = ((blocked[0][i]&&redline[i]==0)?1.:0.);
						input[i+63] = ((blocked[1][i]&&yellowline[i]==0)?1.:0.);
						input[i+72] = ((blocked[2][i]&&purpleline[i]==0)?1.:0.);
					}
					input[81] = paper.getNumberOfMisthrows() * 1. / 4.;
					input[82] = numOfTurns / (15. + Math.abs(numOfTurns));
					training_one_game.add(new Pair<double[], Double>(input, -1.));
				}
				double final_score = paper.calculateScore();
				for (Pair<double[], Double> instance : training_one_game) {
					instance.setY(final_score);
					training_data.add(instance);
				}
			}
		}
		return training_data;
	}
	
	public static QwinPaper generatePaperOfPlayerOnTurn(MatchData match, int player_index, int turn_index) {
		QwinPaper paper = new QwinPaper();
		for (int i = 0; i < turn_index; i++) {
			TurnData td = match.turns.get(i);
			applyActionOnPaper(paper, td.rolledNumbers[td.rolledNumbers.length-1], td.players_action[player_index]);
		}
		return paper;
	}
	
	public static QwinPaper generateFinalPaperOfPlayer(MatchData match, int player_index) {
		return generatePaperOfPlayerOnTurn(match, player_index, match.last_turn.turn_number-1);
	}
	
	public static double calculateAverageScore_ofIndex(AllDataRaw all_matches, int player_index) {
		double avgScore = 0;
		for (MatchData match : all_matches.matches) {
			QwinPaper paper = generateFinalPaperOfPlayer(match, player_index);
			avgScore += paper.calculateScore();
		}
		return avgScore/all_matches.matches.size();
	}
	
	public static double calculateAverageScore_allPlayers(AllDataRaw all_matches) {
		int num_players = all_matches.matches.get(0).players.length;
		double avg_score = 0;
		for (int i = 0; i < num_players; i++) {
			avg_score += calculateAverageScore_ofIndex(all_matches, i);
		}
		return avg_score / num_players;
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
