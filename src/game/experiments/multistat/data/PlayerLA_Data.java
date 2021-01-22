package game.experiments.multistat.data;

import java.util.ArrayList;

import game.qwplayer.dev.QwinPlayerLookahead_t;
import game.qwplayer.dev.QwinPlayer_t;

public class PlayerLA_Data extends PlayerData_S {
	public ArrayList<Integer> denoisedDecisions = new ArrayList<Integer>();
	public ArrayList<Double> paperEvaluations = new ArrayList<Double>();
	
	@Override
	public void gatherData(QwinPlayer_t player) {
		QwinPlayerLookahead_t la_player = (QwinPlayerLookahead_t)player;
		denoisedDecisions.addAll(la_player.getDenoisedDecisions());
		paperEvaluations.addAll(la_player.getpaperEval_turn());
		//noisedDecisions.addAll(((QwinPlayerLookahead_t)player).
	}

	@Override
	public String getTurnDataToString(int turn) {
		String ret = "Evaluation:\t"+paperEvaluations.get(turn)+"\n";
		return ret;
	}
	

	/**
	 * decision flags
	 * -1: RED<br>
	 * -2: YELLOW<br>
	 * -3: RED & YELLOW<br>
	 * -4: PURPLE<br>
	 * -5: RED & PURPLE<br>
	 * -6: YELLOW & PURPLE<br>
	 * -7: RED & YELLOW & PURPLE<br>
	 * 0  ReRoll<br>
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
	
}
