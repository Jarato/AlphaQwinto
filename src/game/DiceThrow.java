package game;

public class DiceThrow{
	public boolean red;
	public boolean yellow;
	public boolean purple;
	
	public DiceThrow(boolean setRed, boolean setYellow, boolean setPurple) {
		red = setRed;
		yellow = setYellow;
		purple = setPurple;
	}
	
	public DiceThrow() {
		red = false;
		yellow = false;
		purple = false;
	}
	
	public int getNumberOfDice() {
		int num = 0;
		if (red) num++;
		if (yellow) num++;
		if (purple) num++;
		return num;
	}
	
	public int getDiceThrowFlag() {
		if (red && !yellow && !purple) return 0;
		if (!red && yellow && !purple) return 1;
		if (red && yellow && !purple) return 2;
		if (!red && !yellow && purple) return 3;
		if (red && !yellow && purple) return 4;
		if (!red && yellow && purple) return 5;
		if (red && yellow && purple) return 6;
		return -1;
	}
	
	public static DiceThrow flagToDiceThrow(int flag) {
		switch(flag) {
			case 0: return new DiceThrow(true, false, false);
			case 1: return new DiceThrow(false, true, false);
			case 2: return new DiceThrow(true, true, false);
			case 3: return new DiceThrow(false, false, true);
			case 4: return new DiceThrow(true, false, true);
			case 5: return new DiceThrow(false, true, true);
			case 6: return new DiceThrow(true, true, true);
		}
		return null;
	}
	
	public String toString() {
		String str = "Throw ";
		switch (getNumberOfDice()) {
		case 1: {
			if (red) return str+"RED!";
			if (yellow) return str+"YELLOW!";
			if (purple) return str+"PURPLE!";
		}
		break;
		case 2: {
			if (red) {
				str += "RED and ";
				if (yellow) return str+"YELLOW!";
				if (purple) return str+"PURPLE!";
			} else return str+"YELLOW and PURPLE!";
		}
		break;
		case 3: return str+"RED, YELLOW and PURPLE!";
		}
		return str+"ERROR!";
	}
	
	public boolean isValid() {
		return (red || yellow || purple);
	}
}
