package game;

public class DiceRoll{
	public boolean red;
	public boolean yellow;
	public boolean purple;
	
	public DiceRoll(boolean setRed, boolean setYellow, boolean setPurple) {
		red = setRed;
		yellow = setYellow;
		purple = setPurple;
	}
	
	public DiceRoll() {
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
	
	public static DiceRoll flagToDiceThrow(int flag) {
		switch(flag) {
			case 0: return new DiceRoll(true, false, false);
			case 1: return new DiceRoll(false, true, false);
			case 2: return new DiceRoll(true, true, false);
			case 3: return new DiceRoll(false, false, true);
			case 4: return new DiceRoll(true, false, true);
			case 5: return new DiceRoll(false, true, true);
			case 6: return new DiceRoll(true, true, true);
		}
		return null;
	}
	
	public String toString() {
		String str = "Roll ";
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
