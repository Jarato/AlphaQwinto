package game.experiments.multistat.data;

import java.util.ArrayList;

import game.QwinPaper;
import game.qwplayer.dev.QwinPlayer_t;

public class MatchData {
	public ArrayList<TurnData> turns = new ArrayList<TurnData>();
	public TurnData last_turn = null;
	public PlayerData[] players = null;

	public void generateBlankePlayerDatas(QwinPlayer_t[] players_s) {
		players = new PlayerData[players_s.length];
		for (int i = 0; i < players_s.length; i++) {
			players[i] = new PlayerData();
			players[i].special_data = players_s[i].generatePlayerDataCollector();
			players[i].name = players_s[i].getName();
			players[i].player_index = i;
		}
	}
	
	public TurnData generateBlankTurnData() {
		last_turn = new TurnData();
		turns.add(last_turn);
		return last_turn;
	}
	
	public QwinPaper calculatePaperForPlayerIndex(int player_index, int turn_number) {
		return null;
	}
	
}
