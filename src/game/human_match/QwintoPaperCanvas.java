package game.human_match;

import java.awt.Canvas;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import game.QwinPaper;

public class QwintoPaperCanvas extends Canvas {
	private BufferedImage background;
	private QwinPaper paper;
	
	public QwintoPaperCanvas(BufferedImage background_image) {
		background = background_image;
	}
	
	@Override
	public void paint(Graphics g) {
		g.drawImage(background, 0, 0, null);
		g.setFont(new Font("Roboto",1,25));
		if (paper != null) {
			int[] red = paper.getRedLine();
			int[] yellow = paper.getYellowLine();
			int[] purple = paper.getPurpleLine();
			for (int i = 0; i < red.length; i++) {
				g.drawString(""+(red[i] > 0?red[i]:""), 135+45*(i>2?i+1:i)-(red[i]>9?8:0), 62);
				g.drawString(""+(yellow[i] > 0?yellow[i]:""), 90+45*(i>4?i+1:i)-(yellow[i]>9?8:0), 124);
				g.drawString(""+(purple[i] > 0?purple[i]:""), 45+45*(i>3?i+1:i)-(purple[i]>9?8:0), 186);
			}
			for (int i = 0; i < paper.getNumberOfMisthrows(); i++) {
				g.drawString("X", 223+i*44, 252);
			}
			if (paper.isEndCondition()) {
				// match over, display the score calculation aswell as the score
				for (int i = 0; i < 3; i++) {
					int lscore = paper.getLineScore(i);
					g.drawString(""+lscore, 42 + i*50-(lscore>9?8:0), 315);
				}
				for (int i = 0; i < 5; i++) {
					int pscore = paper.getPentaColumnScore(i);
					g.drawString(""+pscore, 201 + i*44-(pscore>9?8:0), 315);
				}
				int misthrowSubtract = paper.getNumberOfMisthrows()*5;
				g.drawString(""+misthrowSubtract, 437-(misthrowSubtract>9?8:0), 315);
				g.drawString(""+paper.calculateScore(), 510, 315);
			}
		}
	}
	
	public void updatePaper(QwinPaper paper) {
		this.paper = paper;
	}
}
