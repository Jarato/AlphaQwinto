package game;

public class QwinPaper {
	public static final int LINE_LENGTH = 9;
	private int[] lineRed;
	private int[] lineYellow;
	private int[] linePurple;
	private int numMisthrows;
	private boolean reCalculateScore;
	private int lastScore;
	
	public QwinPaper() {
		clear();
	}
	
	public QwinPaper(QwinPaper paper) {
		for (int i = 0; i < LINE_LENGTH; i++) {
			lineRed[i] = paper.lineRed[i];
			lineYellow[i] = paper.lineYellow[i];
			linePurple[i] = paper.linePurple[i];
		}
		numMisthrows = paper.numMisthrows;
		reCalculateScore = paper.reCalculateScore;
		lastScore = paper.lastScore;
	}
	
	public void clear() {
		lineRed = new int[LINE_LENGTH];
		lineYellow = new int[LINE_LENGTH];
		linePurple = new int[LINE_LENGTH];
		numMisthrows = 0;
		reCalculateScore = true;
	}
	
	public int getNumberOfMisthrows() {
		return numMisthrows;
	}
	
	public int[] getRedLine() {
		int[] copyRes = new int[lineRed.length];
		for (int i = 0; i < lineRed.length; i++) {
			copyRes[i] = lineRed[i];
		}
		return copyRes;
	}
	
	public int[] getYellowLine() {
		int[] copyRes = new int[lineYellow.length];
		for (int i = 0; i < lineYellow.length; i++) {
			copyRes[i] = lineYellow[i];
		}
		return copyRes;
	}
	
	public int[] getPurpleLine() {
		int[] copyRes = new int[linePurple.length];
		for (int i = 0; i < linePurple.length; i++) {
			copyRes[i] = linePurple[i];
		}
		return copyRes;
	}
	
	public boolean isEndCondition() {
		if (numMisthrows >= 4) return true;
		int numberOfFullLines = 0;
		int i = 0;
		while(i < LINE_LENGTH && lineRed[i]>0) i++;
		if (i == LINE_LENGTH) numberOfFullLines++;
		i = 0;
		while(i < LINE_LENGTH && lineYellow[i]>0) i++;
		if (i == LINE_LENGTH) numberOfFullLines++;
		if (numberOfFullLines < 1) return false;
		i = 0;
		while(i < LINE_LENGTH && linePurple[i]>0) i++;
		if (i == LINE_LENGTH) numberOfFullLines++;
		return (numberOfFullLines > 1);
	}
	
	public void enterNumber(int color, int pos, int number) {
		switch(color) {
		case 0: lineRed[pos] = number;
			break;
		case 1: lineYellow[pos] = number;
			break;
		case 2: linePurple[pos] = number;
			break;
		}
		reCalculateScore = true;
	}
	
	public void misthrow() {
		numMisthrows++;
		reCalculateScore = true;
	}
	
	public void removeMisthrow() {
		numMisthrows--;
		reCalculateScore = true;
	}
	
	public boolean isColorPositionBlocked(int color, int pos) {
		int[] lane = (color == 0? lineRed : (color == 1? lineYellow : linePurple));
		if (lane[pos] > 0) return true;
		int i = pos;
		while (i-1 >= 0 && lane[i] == 0) i--;
		int numberOnLeft = lane[i];
		int j = pos;
		while (j+1 <= LINE_LENGTH-1 && lane[j] == 0) j++;
		int numberOnRight = lane[j];
		if (numberOnRight - numberOnLeft == 1) return true;
		if (numberOnRight - numberOnLeft == 2) return !isPositionValidForNumber(color, pos, numberOnLeft+1);
		if (numberOnRight - numberOnLeft == 3) return !(isPositionValidForNumber(color, pos, numberOnLeft+1) || isPositionValidForNumber(color, pos, numberOnRight-1));
		return false;
	}
	
	public boolean[][] generateBlockedFields() {
		boolean[][] blocked = new boolean[3][LINE_LENGTH];
		for (int i = 0; i < blocked.length; i++) {
			for (int ii = 0; ii < LINE_LENGTH; ii++) {
				blocked[i][ii] = isColorPositionBlocked(i, ii);
			}
		}
		return blocked;
	}
	
	public int calculateScore() {
		if (!reCalculateScore) return lastScore;
		int score = 0;
		int zs = 0;
		for (int i = 0; i < LINE_LENGTH; i++) {
			if (lineRed[i]>0) zs++;
		}
		score += (zs==LINE_LENGTH?lineRed[LINE_LENGTH-1]:zs);
		zs = 0;
		for (int i = 0; i < LINE_LENGTH; i++) {
			if (lineYellow[i]>0) zs++;
		}
		score += (zs==LINE_LENGTH?lineYellow[LINE_LENGTH-1]:zs);
		zs = 0;
		for (int i = 0; i < LINE_LENGTH; i++) {
			if (linePurple[i]>0) zs++;
		}
		score += (zs==LINE_LENGTH?linePurple[LINE_LENGTH-1]:zs);
		if (lineRed[0] > 0 && lineYellow[1] > 0 && linePurple[2] > 0) score += linePurple[2];
		if (lineRed[1] > 0 && lineYellow[2] > 0 && linePurple[3] > 0) score += lineRed[1];
		if (lineRed[4] > 0 && lineYellow[5] > 0 && linePurple[6] > 0) score += lineRed[4];
		if (lineRed[5] > 0 && lineYellow[6] > 0 && linePurple[7] > 0) score += lineYellow[6];
		if (lineRed[6] > 0 && lineYellow[7] > 0 && linePurple[8] > 0) score += linePurple[8];
		reCalculateScore = false;
		lastScore = score-5*numMisthrows;
		return lastScore;
	}
	
	public boolean isPositionValidForNumber(int color, int pos, int number) {
		if (color == 0) {
			//aktuelle Position
			if (lineRed[pos] > 0) return false; 
			//Spalten
			switch (pos) {
			case 0: if (lineYellow[pos+1]==number || linePurple[pos+2]==number) return false;
				break;
			case 1:	if (lineYellow[pos+1]==number || linePurple[pos+2]==number) return false;
				break;
			case 2:	if (lineYellow[pos+1]==number) return false;
				break;
			case 3:	if (linePurple[pos+2]==number) return false;
				break;
			case 4:	if (lineYellow[pos+1]==number || linePurple[pos+2]==number) return false;
				break;
			case 5:	if (lineYellow[pos+1]==number || linePurple[pos+2]==number) return false;
				break;
			case 6:	if (lineYellow[pos+1]==number || linePurple[pos+2]==number) return false;
				break;
			case 7:	if (lineYellow[pos+1]==number) return false;
				break;
			}
			//Zeilen
			int i = pos-1;
			while (i >= 0 && lineRed[i] == 0) i--;
			if (i >= 0 && lineRed[i] >= number) return false;
			i = pos+1;
			while (i < LINE_LENGTH && lineRed[i] == 0) i++;
			if (i < LINE_LENGTH && lineRed[i] <= number) return false;
		} else if (color == 1) {
			//aktuelle Position
			if (lineYellow[pos] > 0) return false;
			//Spalten
			switch (pos) {
			case 0:	if (linePurple[pos+1]==number) return false;
				break;
			case 1:	if (lineRed[pos-1]==number || linePurple[pos+1]==number) return false;
				break;
			case 2:	if (lineRed[pos-1]==number || linePurple[pos+1]==number) return false;
				break;
			case 3:	if (lineRed[pos-1]==number) return false;
				break;
			case 4:	if (linePurple[pos]==number) return false;
				break;
			case 5:	if (lineRed[pos-1]==number || linePurple[pos+1]==number) return false;
				break;
			case 6:	if (lineRed[pos-1]==number || linePurple[pos+1]==number) return false;
				break;
			case 7:	if (lineRed[pos-1]==number || linePurple[pos+1]==number) return false;
				break;
			case 8: if (lineRed[pos-1]==number) return false;
				break;
			}
			//Zeilen
			int i = pos-1;
			while (i >= 0 && lineYellow[i] == 0) i--;
			if (i >= 0 && lineYellow[i] >= number) return false;
			i = pos+1;
			while (i < LINE_LENGTH && lineYellow[i] == 0) i++;
			if (i < LINE_LENGTH && lineYellow[i] <= number) return false;
		} else if (color == 2) {
			//aktuelle Position
			if (linePurple[pos] > 0) return false;
			//Spalten
			switch (pos) { 
			case 1:	if (lineYellow[pos-1]==number) return false;
				break;
			case 2:	if (lineRed[pos-2]==number || lineYellow[pos-1]==number) return false;
				break;
			case 3:	if (lineRed[pos-2]==number || lineYellow[pos-1]==number) return false;
				break;
			case 4: if (lineYellow[pos]==number) return false;
				break;
			case 5:	if (lineRed[pos-2]==number) return false;
				break;
			case 6:	if (lineRed[pos-2]==number || lineYellow[pos-1]==number) return false;
				break;
			case 7:	if (lineRed[pos-2]==number || lineYellow[pos-1]==number) return false;
				break;
			case 8: if (lineRed[pos-2]==number || lineYellow[pos-1]==number) return false;
				break;
			}
			//Zeilen
			int i = pos-1;
			while (i >= 0 && linePurple[i] == 0) i--;
			if (i >= 0 && linePurple[i] >= number) return false;
			i = pos+1;
			while (i < LINE_LENGTH && linePurple[i] == 0) i++;
			if (i < LINE_LENGTH && linePurple[i] <= number) return false;
		} else throw new IllegalArgumentException("illegal color int");
		return true;
	}
	
	public int getNumberOfFullLanes() {
		boolean redFull = true;
		boolean yellowFull = true;
		boolean purpleFull = true;
		for (int i = 0; i < LINE_LENGTH; i++) {
			if (lineRed[i] == 0) redFull=false;
			if (lineYellow[i] == 0) yellowFull=false;
			if (linePurple[i] == 0) purpleFull=false;
		}
		int num = 0;
		if (redFull) num++;
		if (yellowFull) num++;
		if (purpleFull) num++;
		return num;
	}
	
	public int getNumberOfFullPentagonColumns() {
		int numPent = 0;
		if (lineRed[0] > 0 && lineYellow[1] > 0 && linePurple[2] > 0) numPent++;
		if (lineRed[1] > 0 && lineYellow[2] > 0 && linePurple[3] > 0) numPent++;
		if (lineRed[4] > 0 && lineYellow[5] > 0 && linePurple[6] > 0) numPent++;
		if (lineRed[5] > 0 && lineYellow[6] > 0 && linePurple[7] > 0) numPent++;
		if (lineRed[6] > 0 && lineYellow[7] > 0 && linePurple[8] > 0) numPent++;
		return numPent;
	}
	
	public int getNumberOfEnteredNumbers() {
		int num = 0;
		for (int i = 0; i < LINE_LENGTH; i++) {
			if (lineRed[i] != 0) num++;
			if (lineYellow[i] != 0) num++;
			if (linePurple[i] != 0) num++;
		}
		return num;
	}
	
	
	
	private String ntS(int num) {
		if (num == 0) return "__";
		if (num < 10) return " "+num;
		return ""+num;
	}
	
	public String toString() {
		String strRed = "        "+ntS(lineRed[0])+" ["+ntS(lineRed[1])+"] "+ntS(lineRed[2])+"      "+ntS(lineRed[3])+" ["+ntS(lineRed[4])+"] "+ntS(lineRed[5])+"  "+ntS(lineRed[6])+"  "+ntS(lineRed[7])+"  "+ntS(lineRed[8]);
		String strYellow = "    "+ntS(lineYellow[0])+"  "+ntS(lineYellow[1])+"  "+ntS(lineYellow[2])+"  "+ntS(lineYellow[3])+"  "+ntS(lineYellow[4])+"      "+ntS(lineYellow[5])+" ["+ntS(lineYellow[6])+"] "+ntS(lineYellow[7])+"  "+ntS(lineYellow[8])+"   ";
		String strPurple = ""+ntS(linePurple[0])+"  "+ntS(linePurple[1])+" ["+ntS(linePurple[2])+"] "+ntS(linePurple[3])+"      "+ntS(linePurple[4])+"  "+ntS(linePurple[5])+"  "+ntS(linePurple[6])+"  "+ntS(linePurple[7])+" ["+ntS(linePurple[8])+"]     ";
		String strMisthrow = "Misthrows = "+numMisthrows;
		/*
		 *  #  #  0 [0] 0     0 [0] 0  0  0  0
		 *  #  0  0  0  0  0     0 [0] 0  0  #
		 *  0  0 [0] 0     0  0  0  0 [0] #  #
		 *  XXXX
		 */
		
		return strRed+"\n"+strYellow+"\n"+strPurple+"\n"+strMisthrow;
		
	}
}
