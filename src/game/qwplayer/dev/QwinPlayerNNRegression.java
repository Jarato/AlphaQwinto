package game.qwplayer.dev;

import java.util.Random;

import game.DiceThrow;
import model.FeedForwardNetwork;

public class QwinPlayerNNRegression extends QwinPlayerRnd {

	private FeedForwardNetwork scoreEvalNetwork;
	
	public QwinPlayerNNRegression(Random initRnd) {
		super(initRnd);
		initNet();
	}

	private void initNet(){
		scoreEvalNetwork = new FeedForwardNetwork();
		//scoreEvalNetwork.addBlock(inputLength, outputLength, withBias, funcEnum);
	}
	
	@Override
	public DiceThrow getDiceThrowRnd(double probabilityRandom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] getActionFlagListRnd(double probabilityRandom, int diceNumber, DiceThrow thrown,
			boolean untilRethrow) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DiceThrow getDiceThrow() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] getActionFlagList(int diceNumber, DiceThrow thrown) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
