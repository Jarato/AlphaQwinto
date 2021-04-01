package game.human_match;

import java.awt.Canvas;
import java.awt.Frame;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import game.QwinPaper;

public class QwinPaperGUI {
	private JFrame f;
	private QwintoPaperCanvas paperCanvas;
	private BufferedImage img;
	
	public QwinPaperGUI() {
		f = new JFrame("AlphaQwinto Paper");
		f.setSize(615, 400);
		f.setLayout(null);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setResizable(false);
		
		try {
			img = ImageIO.read(new File("./img/QwintoPaper.jpg"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		paperCanvas = new QwintoPaperCanvas(img);
		f.add(paperCanvas);
		paperCanvas.setVisible(true);
		paperCanvas.setSize(600,364);
		f.setVisible(true);
		paperCanvas.setVisible(true);
		f.setAlwaysOnTop(true);
	}

	public void update(QwinPaper paper) {
		paperCanvas.updatePaper(paper);
		f.repaint();
		paperCanvas.repaint();
	}
	
	public void game_has_ended() {
		paperCanvas.gameEnded();
	}
	
	public static void main(String[] args) {
		QwinPaperGUI gui = new QwinPaperGUI();
		QwinPaper paper = new QwinPaper();
		for (int c = 0; c < 3; c++) {
			for (int p = 0; p < 9; p++) {
				paper.enterNumber(c, p, p+6);
			}
		}
		paper.misthrow();
		gui.update(paper);
	}

}

