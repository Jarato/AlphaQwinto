package game.human_match;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.sun.tools.javac.Main;

import game.DiceRoll;
import game.QwinPaper;
import game.experiments.multistat.analyze.RawDataAnalyzer;
import game.experiments.multistat.analyze.RawDataAnalyzer.ColorPosition;
import game.qwplayer.dev.QwinPlayerLA_NNEval;
import game.qwplayer.dev.QwinPlayer_t;

public class QwintoHumanMatch {
	private QwinPlayer_t ai_player;
	private int turn;
	private boolean matchEnd;
	private QwinPaperGUI gui;
	private QwinAudioSystem audiosystem;
	private Random rnd;
	private Pattern intPattern = Pattern.compile("^[0-9]+$");
	private Pattern playerPattern = Pattern.compile("^ai|h|end$");
	private Pattern diceColorPattern = Pattern.compile("^[ryp]|ry|rp|yp|yr|pr|py|ryp|yrp|rpy|ypr|pry|pyr$");

	public static void main(String[] args) {
		Random rnd = new Random();
		QwintoHumanMatch match = new QwintoHumanMatch(new QwinPlayerLA_NNEval(rnd, 10, "LANNEVAL10_weights.txt"));
		match.calculateMatch();
	}

	public QwintoHumanMatch(QwinPlayer_t initAIPlayer) {
		this(new Random(), initAIPlayer);
	}

	public QwintoHumanMatch(Random rnd, QwinPlayer_t initAIPlayer) {
		audiosystem = new QwinAudioSystem();
		ai_player = initAIPlayer;
		matchEnd = false;
		gui = new QwinPaperGUI();
		this.rnd = rnd;
	}

	public void calculateMatch() {
		audiosystem.playAudio_Gamestart();
		turn = 1;
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try {
			while (!matchEnd) {
				System.out.println("Turn "+turn);
				System.out.print("Put in the player indicator (\"h\" for human, \"ai\" for computer or \"end\" for end of the match): ");
				// Reading data using readLine
				String input = reader.readLine();;
				while (!playerPattern.matcher(input).matches()) {
					System.out.println(input + " is not in the right format! Try again: ");
					input = reader.readLine();
				}
				if (input.equals("h")) {
					// human
					int rndwhere = rnd.nextInt(3);
					if (rndwhere == 0) audiosystem.playAudio_Random();
					System.out.println("Put in the chosen colors (\"y\" for yellow, \"r\" for red and \"p\" for purple): ");
					String color_str = reader.readLine();
					while (!diceColorPattern.matcher(color_str).matches()) {
						System.out.println(color_str + " is not a valid color code. Try again: ");
						color_str = reader.readLine();
					}
					DiceRoll dc = new DiceRoll();
					if (rndwhere == 1) audiosystem.playAudio_Random();
					// transform color_str into a DiceRoll object
					for (int i = 0; i < color_str.length(); i++){
					    char c = color_str.charAt(i);
					    if (c == 'y') dc.yellow = true;
					    if (c == 'r') dc.red = true;
					    if (c == 'p') dc.purple = true;
					}
					System.out.println("Human wants to roll " + dc);
					System.out.println("Put in the rolled number: ");
					String rolled_number_str = reader.readLine();
					while (!intPattern.matcher(rolled_number_str).matches() || !dc.canRollNumber(Integer.parseInt(rolled_number_str))) {
						System.out.println(rolled_number_str + " is not a rollable number with these dice! Try again: ");
						rolled_number_str = reader.readLine();
					}
					if (rndwhere == 2) audiosystem.playAudio_Random();
					int rolled_number = Integer.parseInt(rolled_number_str);
					int action = ai_player.getActionFlag(rolled_number, dc, false, true);
					if (action == 1) {
						// misthrow!
						System.out.println("AlphaQwinto rejects this number!");
					} else {
						RawDataAnalyzer.ColorPosition cp = RawDataAnalyzer.actionflag_to_colorposition(action);
						System.out.println("AlphaQwinto enters the number " + rolled_number + " in the "
								+ (cp.color == 0 ? "RED" : (cp.color == 1 ? "YELLOW" : "PURPLE")) + " lane on position "
								+ (cp.position + 1));
						paperEnterNumber(ai_player.getPaper(), dc, rolled_number, action);
					}
					System.out.println(ai_player.getPaper());
				}
				if (input.equals("ai")) {
					DiceRoll dc = ai_player.getDiceRoll();
					audiosystem.playAudio_DiceRollChoice(dc);
					System.out.println("\nAlphaQwinto wants to roll " + dc);
					System.out.println("Put in the rolled number: ");
					String rolled_number_str = reader.readLine();
					while (!intPattern.matcher(rolled_number_str).matches() || !dc.canRollNumber(Integer.parseInt(rolled_number_str))) {
						System.out.println(rolled_number_str + " is not a rollable number with these dice! Try again: ");
						rolled_number_str = reader.readLine();
					}
					int rolled_number = Integer.parseInt(rolled_number_str);
					int action = ai_player.getActionFlag(rolled_number, dc, true, false);
					if (action == 0) {
						// ai wants to reroll
						audiosystem.playAudio_Reroll();
						System.out.println("AlphaQwinto wants to reroll that number! Enter the new number: ");
						rolled_number_str = reader.readLine();
						while (!intPattern.matcher(rolled_number_str).matches() || !dc.canRollNumber(Integer.parseInt(rolled_number_str))) {
							System.out.println(rolled_number_str + " is not a rollable number with these dice! Try again: ");
							rolled_number_str = reader.readLine();
						}
						rolled_number = Integer.parseInt(rolled_number_str);
					}
					// action on rerolled number
					action = ai_player.getActionFlag(rolled_number, dc, false, false);
					if (action == 2) {
						// misthrow!
						audiosystem.playAudio_Misthrow(ai_player.getPaper().getNumberOfMisthrows()+1);
						System.out.println("AlphaQwinto does not enter this number, it's a misthrow!");
					} else {
						RawDataAnalyzer.ColorPosition cp = RawDataAnalyzer.actionflag_to_colorposition(action);
						System.out.println("AlphaQwinto enters the number " + rolled_number + " in the "
								+ (cp.color == 0 ? "RED" : (cp.color == 1 ? "YELLOW" : "PURPLE")) + " lane on position "
								+ (cp.position + 1));
					}
					paperEnterNumber(ai_player.getPaper(), dc, rolled_number, action);
					System.out.println(ai_player.getPaper());
				}
				if (input.equals("end")) {
					matchEnd = true;
					gui.game_has_ended();
				}
				ai_player.turnEndWrapUp();
				gui.update(ai_player.getPaper());
				turn++;
			}
			audiosystem.playAudio_Gameend(ai_player.getPaper());
			System.out.println("score of AlphaQwinto's paper: "+ai_player.getPaper().calculateScore());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getNumberOfTurns() {
		return turn;
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
