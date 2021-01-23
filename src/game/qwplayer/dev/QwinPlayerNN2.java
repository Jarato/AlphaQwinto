package game.qwplayer.dev;

import java.util.ArrayList;
import java.util.Random;

import game.DiceRoll;
import model.FeedForwardNetwork;
import model.functions.Activation;
import pdf.util.Pair;

public class QwinPlayerNN2 extends QwinPlayerRnd_t{
	private FeedForwardNetwork diceThrowNet;
	private FeedForwardNetwork actionListNet;
	
	private ArrayList<Pair<double[], Integer>> rndDiceThrowHistory;
	private ArrayList<Pair<double[], Integer>> rndActionHistory;
	private ArrayList<Pair<double[], Integer>> fullDiceThrowHistory;
	private ArrayList<Pair<double[], Integer>> fullActionHistory;
	
	public QwinPlayerNN2(Random rnd) {
		super(rnd);
		initNets();
		rndDiceThrowHistory = new ArrayList<Pair<double[], Integer>>();
		rndActionHistory = new ArrayList<Pair<double[], Integer>>();
		fullDiceThrowHistory = new ArrayList<Pair<double[], Integer>>(); 
		fullActionHistory = new ArrayList<Pair<double[], Integer>>();   
	}
	
	@Override
	public void reset(Random resetRnd) {
		super.reset(resetRnd);
		rndDiceThrowHistory = new ArrayList<Pair<double[], Integer>>();
		rndActionHistory = new ArrayList<Pair<double[], Integer>>();
		fullDiceThrowHistory = new ArrayList<Pair<double[], Integer>>(); 
		fullActionHistory = new ArrayList<Pair<double[], Integer>>();   
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
		diceThrowNet.addBlock(28, 60, true, Activation.SIN);
		diceThrowNet.addBlock(60, 7, true, Activation.SIGMOID);
		diceThrowNet.setAllWeightsRandom(rnd, 1);
		actionListNet = new FeedForwardNetwork();
		actionListNet.addBlock(49, 120, true, Activation.SIN);
		actionListNet.addBlock(120, 120, true, Activation.SIN);
		actionListNet.addBlock(120, 28, true,  Activation.SIGMOID);
		actionListNet.setAllWeightsRandom(rnd, 1);
	}
	
	public ArrayList<Pair<double[], Integer>> getFullDiceThrowHistory(){
		return fullDiceThrowHistory;
	}
	
	public ArrayList<Pair<double[], Integer>> getRndDiceThrowHistory(){
		return rndDiceThrowHistory;
	}
	
	public ArrayList<Pair<double[], Integer>> getFullActionHistory(){
		return fullActionHistory;
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
			vector[i] = redline[i]/18.0;
			vector[i+9] = yellowline[i]/18.0;
			vector[i+18] = purpleline[i]/18.0;
		}
		vector[27] = paper.getNumberOfMisthrows()*2.0/3.0-1;
	}
	
	private void fillWithActionListNetFeatures(double[] vector, int thrownNumber, DiceRoll thrown) {
		fillWithDiceThrowNetFeatures(vector);
		vector[28] = (thrown.red?1:-1);
		vector[29] = (thrown.yellow?1:-1);
		vector[30] = (thrown.purple?1:-1);
		vector[30+thrownNumber] = 1;
	}
	
	
	private int getFirstValidActionIndex(int[] actionList, DiceRoll lastThrown, int thrownNumber, boolean untilRethrow) {
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
	
	public DiceRoll getDiceThrowRnd(double probabilityRandom) {
		double[] diceThrowFeatures = new double[28];
		fillWithDiceThrowNetFeatures(diceThrowFeatures);
		if (rnd.nextDouble() < probabilityRandom) {
			int throwFlag = rnd.nextInt(7);
			rndDiceThrowHistory.add(new Pair<double[], Integer>(diceThrowFeatures, throwFlag));
			fullDiceThrowHistory.add(new Pair<double[], Integer>(diceThrowFeatures, throwFlag));
			return DiceRoll.flagToDiceThrow(throwFlag);
		} else {
			DiceRoll dt = getDiceRoll();
			int flag = dt.getDiceThrowFlag();
			fullDiceThrowHistory.add(new Pair<double[], Integer>(diceThrowFeatures, flag));
			return dt;
		}
		
		
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
	public DiceRoll getDiceRoll() {
		double[] input = new double[28];
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
		return DiceRoll.flagToDiceThrow(bestIndex);
	}
	
	public int[] getActionFlagListRnd(double probabilityRandom, int diceNumber, DiceRoll thrown, boolean untilRethrow) {
		double[] actionListFeatures = new double[49];
		fillWithActionListNetFeatures(actionListFeatures, diceNumber, thrown);
		if (rnd.nextDouble() < probabilityRandom) {
			int[] permutation = randomPermutation(28);
			int validAction = getFirstValidActionIndex(permutation, thrown, diceNumber, untilRethrow);
			
			// validAction == -1 bedeutet Misthrow. Das Netzwerk soll nichts lernen, wenn es die geworfene Zahl gar nicht eintragen kann
			if (validAction > -1) {
				rndActionHistory.add(new Pair<double[], Integer>(actionListFeatures, validAction));
				fullActionHistory.add(new Pair<double[], Integer>(actionListFeatures, validAction));
			}
			return permutation;
		} else {
			int[] afl = getActionFlagList(diceNumber, thrown);
			int validAction = getFirstValidActionIndex(afl, thrown, diceNumber, untilRethrow);
			if (validAction > -1) {
				rndActionHistory.add(new Pair<double[], Integer>(actionListFeatures, validAction));
				fullActionHistory.add(new Pair<double[], Integer>(actionListFeatures, validAction));
			}
			return getActionFlagList(diceNumber, thrown);
		}
		
		
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
	public int[] getActionFlagList(int diceNumber, DiceRoll thrown) {
		double[] input = new double[49];
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
