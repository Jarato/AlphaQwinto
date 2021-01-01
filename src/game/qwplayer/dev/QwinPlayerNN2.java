package game.qwplayer.dev;

import java.util.ArrayList;
import java.util.Random;

import game.DiceThrow;
import model.FeedForwardNetwork;
import model.functions.AF_ReLU;
import model.functions.AF_Sigmoid;
import model.functions.Activation;
import pdf.util.Pair;

public class QwinPlayerNN2 extends QwinPlayerRnd{
	private FeedForwardNetwork diceThrowNet;
	private FeedForwardNetwork actionListNet;
	
	private ArrayList<Pair<double[], Integer>> rndDiceThrowHistory;
	private ArrayList<Pair<double[], Integer>> rndActionHistory;
	
	public QwinPlayerNN2(Random rnd) {
		super(rnd);
		initNets();
		rndDiceThrowHistory = new ArrayList<Pair<double[], Integer>>();
		rndActionHistory = new ArrayList<Pair<double[], Integer>>();
	}
	
	@Override
	public void reset(Random resetRnd) {
		super.reset(resetRnd);
		rndDiceThrowHistory = new ArrayList<Pair<double[], Integer>>();
		rndActionHistory = new ArrayList<Pair<double[], Integer>>();
	}
	
	private void initNets() {
		/*diceThrowNet = new FeedForwardNetwork();
		diceThrowNet.addBlock(55, 60, true, new AF_ReLU(1.0/55.0));
		diceThrowNet.addBlock(60, 7, true, new AF_ReLU(1.0/60.0));
		diceThrowNet.setAllWeightsRandom(rnd);
		actionListNet = new FeedForwardNetwork();
		actionListNet.addBlock(76, 100, true, new AF_ReLU(1.0/76.0));
		actionListNet.addBlock(100, 100, true, new AF_ReLU(1.0/100.0));
		actionListNet.addBlock(100, 28, true, new AF_ReLU(1.0/100.0));
		actionListNet.setAllWeightsRandom(rnd);*/
		diceThrowNet = new FeedForwardNetwork();
		diceThrowNet.addBlock(55, 60, true, Activation.RELU);
		diceThrowNet.addBlock(60, 7, true, Activation.SIGMOID);
		diceThrowNet.setAllWeightsRandom(rnd);
		actionListNet = new FeedForwardNetwork();
		actionListNet.addBlock(76, 120, true, Activation.RELU);
		actionListNet.addBlock(120, 120, true, Activation.RELU);
		actionListNet.addBlock(120, 28, true,  Activation.SIGMOID);
		actionListNet.setAllWeightsRandom(rnd);
	}
	
	public ArrayList<Pair<double[], Integer>> getRndDiceThrowHistory(){
		return rndDiceThrowHistory;
	}
	
	public ArrayList<Pair<double[], Integer>> getRndActionHistory(){
		return rndActionHistory;
	}
	
	public FeedForwardNetwork getDiceThrowNet() {
		return diceThrowNet;
	}
	
	public void setDiceThrowNet(FeedForwardNetwork setDiceThrowNet) {
		diceThrowNet = setDiceThrowNet;
	}
	
	public FeedForwardNetwork getActionListNet() {
		return actionListNet;
	}
	
	public void setActionListNet(FeedForwardNetwork setActionListNet) {
		actionListNet = setActionListNet;
	}
	
	private void fillWithDiceThrowNetFeatures(double[] vector) {
		int[] redline = paper.getRedLine();
		int[] yellowline = paper.getYellowLine();
		int[] purpleline = paper.getPurpleLine();
		for (int i = 0; i < 9; i++) {
			vector[i] = redline[i]/9.0-1;
			vector[i+9] = yellowline[i]/9.0-1;
			vector[i+18] = purpleline[i]/9.0-1;
			vector[i+28] = (redline[i]==0?1:-1);
			vector[i+37] = (yellowline[i]==0?1:-1);
			vector[i+46] = (purpleline[i]==0?1:-1);
		}
		vector[27] = paper.getNumberOfMisthrows()*2.0/3.0-1;
	}
	
	private void fillWithActionListNetFeatures(double[] vector, int thrownNumber, DiceThrow thrown) {
		fillWithDiceThrowNetFeatures(vector);
		vector[55] = (thrown.red?1:-1);
		vector[56] = (thrown.yellow?1:-1);
		vector[57] = (thrown.purple?1:-1);
		vector[57+thrownNumber] = 1;
	}
	
	
	private int getFirstValidActionIndex(int[] actionList, DiceThrow lastThrown, int thrownNumber, boolean untilRethrow) {
		int i = 0;
		do {
			if (actionList[i] == 0) {
				if (untilRethrow) {
					return 0;
				}
			} else {
				int color = (actionList[i]-1)/9;
				boolean colorThrown = true;
				switch(color) {
				case 0: colorThrown = lastThrown.red;
					break;
				case 1: colorThrown = lastThrown.yellow;
					break;
				case 2: colorThrown = lastThrown.purple;
					break;
				}
				if (colorThrown) {
					int pos = (actionList[i]-1)%9;
					if(paper.isPositionValidForNumber(color, pos, thrownNumber)) return actionList[i];
				}
			}
			i++;
		} while(i < actionList.length);
		return -1;
	}
	
	public DiceThrow getDiceThrowRnd(double probabilityRandom) {
		
		if (rnd.nextDouble() < probabilityRandom) {
			int throwFlag = rnd.nextInt(7);
			double[] diceThrowFeatures = new double[55];
			fillWithDiceThrowNetFeatures(diceThrowFeatures);
			rndDiceThrowHistory.add(new Pair<double[], Integer>(diceThrowFeatures, throwFlag));
			return DiceThrow.flagToDiceThrow(throwFlag);
		} else return getDiceThrow();
		
		
		/*
		DiceThrow ret = null;
		if (rnd.nextDouble() < probabilityRandom) {
			int throwFlag = rnd.nextInt(7);
			ret = DiceThrow.flagToDiceThrow(throwFlag);
		} else ret = getDiceThrow();
		double[] diceThrowFeatures = new double[55];
		fillWithDiceThrowNetFeatures(diceThrowFeatures);
		rndDiceThrowHistory.add(new Pair<double[], Integer>(diceThrowFeatures, ret.getDiceThrowFlag()));
		return ret;
		*/
	}

	@Override
	public DiceThrow getDiceThrow() {
		double[] input = new double[55];
		fillWithDiceThrowNetFeatures(input);
		diceThrowNet.prozessInput(input);
		double[] res = diceThrowNet.getLastOutputVector();
		int bestIndex = 0;
		double maxValue = res[0];
		for (int i = 1; i < res.length; i++) {
			if (res[i] > maxValue) {
				maxValue = res[i];
				bestIndex = i;
			}
		}
		return DiceThrow.flagToDiceThrow(bestIndex);
	}
	
	public int[] getActionFlagListRnd(double probabilityRandom, int diceNumber, DiceThrow thrown, boolean untilRethrow) {
		if (rnd.nextDouble() < probabilityRandom) {
			int[] permutation = randomPermutation(28);
			int validAction = getFirstValidActionIndex(permutation, thrown, diceNumber, untilRethrow);
			double[] actionListFeatures = new double[76];
			fillWithActionListNetFeatures(actionListFeatures, diceNumber, thrown);
			// validAction == -1 bedeutet Misthrow. Das Netzwerk soll nichts lernen, wenn es die geworfene Zahl gar nicht eintragen kann
			if (validAction > -1) rndActionHistory.add(new Pair<double[], Integer>(actionListFeatures, validAction));
			return permutation;
		} else return getActionFlagList(diceNumber, thrown);
		
		
		/*int[] permutation = null;
		if (rnd.nextDouble() < probabilityRandom) {
			permutation = randomPermutation(28);
		} else permutation = getActionFlagList(diceNumber, thrown);
		int validAction = getFirstValidActionIndex(permutation, thrown, diceNumber, untilRethrow);
		double[] actionListFeatures = new double[76];
		fillWithActionListNetFeatures(actionListFeatures, diceNumber, thrown);
		// validAction == -1 bedeutet Misthrow. Das Netzwerk soll nichts lernen, wenn es die geworfene Zahl gar nicht eintragen kann
		if (validAction > -1) rndActionHistory.add(new Pair<double[], Integer>(actionListFeatures, validAction));
		return permutation;*/
	}

	@Override
	public int[] getActionFlagList(int diceNumber, DiceThrow thrown) {
		double[] input = new double[76];
		fillWithActionListNetFeatures(input, diceNumber, thrown);
		actionListNet.prozessInput(input);
		double[] res = actionListNet.getLastOutputVector();
		int[] priolist = new int[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27};
		for (int i = 0; i < res.length-1; i++) {
			double max = res[i];
			int maxindex = i;
			for (int j = i+1; j < res.length; j++) {
				if (res[j] > max) {
					max = res[j];
					maxindex = j;
				}
			}
			if (i != maxindex) {
				double resT = res[maxindex];
				res[maxindex] = res[i];
				res[i] = resT;
				int indexT = priolist[maxindex];
				priolist[maxindex] = priolist[i];
				priolist[i] = indexT;
			}
		}
		return priolist;
	}

	@Override
	public String getName() {
		return "Neural Network 2 (BackProp)";
	}

}
