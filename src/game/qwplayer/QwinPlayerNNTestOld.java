package game.qwplayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

import game.DiceRoll;
import game.qwplayer.dev.QwinPlayerEvo_t;
import pdf.ai.dna.DNA;
import pdf.ai.nnetwork.NeuralLayerNet;

public class QwinPlayerNNTestOld extends QwinPlayerEvo_t{
	private NeuralLayerNet diceThrowNet;
	private NeuralLayerNet actionListNet;
	
	public QwinPlayerNNTestOld(Random rnd) {
		super(rnd);
		double[] geneValues = new double[getNumberOfNeededGenes()];
		File geneFile = new File("NNTestGenesOld.txt");
		try {
			BufferedReader br = new BufferedReader(new FileReader(geneFile));
			String line;
			int i = 0; 
		    while ((line = br.readLine()) != null) {
		       geneValues[i] = new Double(line);
		       i++;
		    }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dna.setValues(geneValues);
		compoundDNA();
	}
	
	public QwinPlayerNNTestOld(Random rnd, DNA init) {
		super(rnd, init);
	}
	
	private void initNets() {
		diceThrowNet = new NeuralLayerNet(NeuralLayerNet.RELU,28, 100, 7);
		actionListNet = new NeuralLayerNet(NeuralLayerNet.RELU,32, 200, 200, 28);
	}
	
	@Override
	public int getNumberOfNeededGenes() {
		return 28*100+100*7 + 32*200+200*200+200*28;//diceThrowNet.getNumberOfNeededGenes()+actionListNet.getNumberOfNeededGenes();
	}

	@Override
	public DiceRoll getDiceThrow() {
		double[] input = new double[28];
		int[] redline = paper.getRedLine();
		int[] yellowline = paper.getYellowLine();
		int[] purpleline = paper.getPurpleLine();
		for (int i = 0; i < 9; i++) {
			input[i] = redline[i]/18.0;
			input[i+9] = yellowline[i]/18.0;
			input[i+18] = purpleline[i]/18.0;
		}
		input[27] = paper.getNumberOfMisthrows()/3.0;
		diceThrowNet.setInputValues(input);
		diceThrowNet.calculateNet();
		double[] res = diceThrowNet.getOutputValues();
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

	@Override
	public int[] getActionFlagList(int diceNumber, DiceRoll thrown) {
		double[] input = new double[32];
		int[] redline = paper.getRedLine();
		int[] yellowline = paper.getYellowLine();
		int[] purpleline = paper.getPurpleLine();
		for (int i = 0; i < 9; i++) {
			input[i] = redline[i]/18.0;
			input[i+9] = yellowline[i]/18.0;
			input[i+18] = purpleline[i]/18.0;
		}
		input[27] = paper.getNumberOfMisthrows()/3.0;
		input[28] = diceNumber/18.0;
		input[29] = (thrown.red?1:0);
		input[30] = (thrown.yellow?1:0);
		input[31] = (thrown.purple?1:0);
		actionListNet.setInputValues(input);
		actionListNet.calculateNet();
		double[] res = actionListNet.getOutputValues();
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
		return "Neural Network";
	}

	@Override
	public void compoundDNA() {
		initNets();
		diceThrowNet.compoundDNA(dna.getSequence(0, diceThrowNet.getNumberOfNeededGenes()));
		actionListNet.compoundDNA(dna.getSequence(diceThrowNet.getNumberOfNeededGenes(), actionListNet.getNumberOfNeededGenes()));
	}

	

	@Override
	public String getSettingsCode() {
		String strThrowNetDna = "DNA values for throw net:\n";
		String strActionLestDna = "DNA values for the action list net:\n";
		return strThrowNetDna+diceThrowNet.getDNA()+"\n"+strActionLestDna+actionListNet.getDNA();
	}

}
