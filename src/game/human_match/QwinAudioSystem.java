package game.human_match;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import game.DiceRoll;
import game.QwinPaper;

public class QwinAudioSystem {
	private static boolean SOUND = false;
	private ArrayList<Clip> introAudio = new ArrayList<Clip>();
	private ArrayList<Clip> outroAudio = new ArrayList<Clip>();
	private ArrayList<Clip> dicerollAudio = new ArrayList<Clip>();
	private ArrayList<Clip> rerollAudio = new ArrayList<Clip>();
	private ArrayList<Clip> misthrowAudio = new ArrayList<Clip>();
	private ArrayList<Clip> randomAudio = new ArrayList<Clip>();
	private boolean[] alreadyPlayedRnd;
	private int playedRandomClips;
	private Random rnd;

	public QwinAudioSystem() {
		rnd = new Random();
		playedRandomClips = 0;
		try {
			// intro
			File f = new File("./audio/intro2.wav");
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
			Clip clip = AudioSystem.getClip();
			clip.open(audioIn);
			introAudio.add(clip);
			// diceroll red
			f = new File("./audio/dr_red.wav");
			audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
			clip = AudioSystem.getClip();
			clip.open(audioIn);
			dicerollAudio.add(clip);
			// diceroll yellow
			f = new File("./audio/dr_yellow.wav");
			audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
			clip = AudioSystem.getClip();
			clip.open(audioIn);
			dicerollAudio.add(clip);
			// diceroll red and yellow
			f = new File("./audio/dr_redyellow.wav");
			audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
			clip = AudioSystem.getClip();
			clip.open(audioIn);
			dicerollAudio.add(clip);
			// diceroll purple
			f = new File("./audio/dr_purple.wav");
			audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
			clip = AudioSystem.getClip();
			clip.open(audioIn);
			dicerollAudio.add(clip);
			// diceroll red and purple
			f = new File("./audio/dr_redpurple.wav");
			audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
			clip = AudioSystem.getClip();
			clip.open(audioIn);
			dicerollAudio.add(clip);
			// diceroll yellow and purple
			f = new File("./audio/dr_yellowpurple.wav");
			audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
			clip = AudioSystem.getClip();
			clip.open(audioIn);
			dicerollAudio.add(clip);
			// diceroll all three
			f = new File("./audio/dr_all.wav");
			audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
			clip = AudioSystem.getClip();
			clip.open(audioIn);
			dicerollAudio.add(clip);
			// reroll
			f = new File("./audio/reroll.wav");
			audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
			clip = AudioSystem.getClip();
			clip.open(audioIn);
			rerollAudio.add(clip);
			// misthrow first
			f = new File("./audio/mt_first.wav");
			audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
			clip = AudioSystem.getClip();
			clip.open(audioIn);
			misthrowAudio.add(clip);
			// misthrow second
			f = new File("./audio/mt_second.wav");
			audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
			clip = AudioSystem.getClip();
			clip.open(audioIn);
			misthrowAudio.add(clip);
			// misthrow third
			f = new File("./audio/mt_third.wav");
			audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
			clip = AudioSystem.getClip();
			clip.open(audioIn);
			misthrowAudio.add(clip);
			// end misthrow
			f = new File("./audio/end_ai_misthrow.wav");
			audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
			clip = AudioSystem.getClip();
			clip.open(audioIn);
			outroAudio.add(clip);
			// end 2 rows
			f = new File("./audio/end_ai_2rows.wav");
			audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
			clip = AudioSystem.getClip();
			clip.open(audioIn);
			outroAudio.add(clip);
			// end human
			f = new File("./audio/end_human.wav");
			audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
			clip = AudioSystem.getClip();
			clip.open(audioIn);
			outroAudio.add(clip);
			// random - 19
			f = new File("./audio/rnd_19.wav");
			audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
			clip = AudioSystem.getClip();
			clip.open(audioIn);
			randomAudio.add(clip);
			// random - bewerten
			f = new File("./audio/rnd_bewerten.wav");
			audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
			clip = AudioSystem.getClip();
			clip.open(audioIn);
			randomAudio.add(clip);
			// random - dominieren
			f = new File("./audio/rnd_dominieren.wav");
			audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
			clip = AudioSystem.getClip();
			clip.open(audioIn);
			randomAudio.add(clip);
			// random - ruckseite
			f = new File("./audio/rnd_ruckseite.wav");
			audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
			clip = AudioSystem.getClip();
			clip.open(audioIn);
			randomAudio.add(clip);
			// random - tauschen
			f = new File("./audio/rnd_tauschen.wav");
			audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
			clip = AudioSystem.getClip();
			clip.open(audioIn);
			randomAudio.add(clip);
			// random - wahrscheinlichkeit
			f = new File("./audio/rnd_wahrscheinlichkeit.wav");
			audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
			clip = AudioSystem.getClip();
			clip.open(audioIn);
			randomAudio.add(clip);
		} catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		alreadyPlayedRnd = new boolean[randomAudio.size()];
	}

	public void playAudio_Gamestart() {
		if (SOUND) {
			introAudio.get(0).setMicrosecondPosition(0);
			introAudio.get(0).start();
		}
	}

	public void playAudio_Gameend(QwinPaper paper) {
		if (SOUND) {
			if (paper.isEndCondition()) {
				if (paper.getNumberOfMisthrows() == 4) {
					outroAudio.get(0).setMicrosecondPosition(0);
					outroAudio.get(0).start();
				} else {
					outroAudio.get(1).setMicrosecondPosition(0);
					outroAudio.get(1).start();
				}
			} else {
				outroAudio.get(2).setMicrosecondPosition(0);
				outroAudio.get(2).start();
			}
		}
	}

	public void playAudio_DiceRollChoice(DiceRoll diceroll) {
		if (SOUND) {
			if (diceroll.red && !diceroll.yellow && !diceroll.purple) {
				dicerollAudio.get(0).setMicrosecondPosition(0);
				dicerollAudio.get(0).start();
			}
			if (!diceroll.red && diceroll.yellow && !diceroll.purple) {
				dicerollAudio.get(1).setMicrosecondPosition(0);
				dicerollAudio.get(1).start();
			}
			if (diceroll.red && diceroll.yellow && !diceroll.purple) {
				dicerollAudio.get(2).setMicrosecondPosition(0);
				dicerollAudio.get(2).start();
			}
			if (!diceroll.red && !diceroll.yellow && diceroll.purple) {
				dicerollAudio.get(3).setMicrosecondPosition(0);
				dicerollAudio.get(3).start();
			}
			if (diceroll.red && !diceroll.yellow && diceroll.purple) {
				dicerollAudio.get(4).setMicrosecondPosition(0);
				dicerollAudio.get(4).start();
			}
			if (!diceroll.red && diceroll.yellow && diceroll.purple) {
				dicerollAudio.get(5).setMicrosecondPosition(0);
				dicerollAudio.get(5).start();
			}
			if (diceroll.red && diceroll.yellow && diceroll.purple) {
				dicerollAudio.get(6).setMicrosecondPosition(0);
				dicerollAudio.get(6).start();
			}
		}
	}

	public void playAudio_Reroll() {
		if (SOUND) {
			rerollAudio.get(0).setMicrosecondPosition(0);
			rerollAudio.get(0).start();
		}
	}

	public void playAudio_Misthrow(int numMisthrow) {
		if (SOUND) {
			System.out.println("misthrow audio with num " + numMisthrow);
			if (numMisthrow < 4) {
				misthrowAudio.get(numMisthrow - 1).setMicrosecondPosition(0);
				misthrowAudio.get(numMisthrow - 1).start();
			}
		}
	}

	public void playAudio_Random() {
		if (SOUND) {
			if (alreadyPlayedRnd.length > playedRandomClips) {
				if (rnd.nextDouble() < 0.3) {
					int clipnum = rnd.nextInt(alreadyPlayedRnd.length - playedRandomClips);
					for (int i = 0; i < alreadyPlayedRnd.length; i++) {
						if (!alreadyPlayedRnd[i]) {
							if (clipnum == 0) {
								randomAudio.get(i).setMicrosecondPosition(0);
								randomAudio.get(i).start();
								alreadyPlayedRnd[i] = true;
								playedRandomClips++;
							}
							clipnum--;
						}
					}
				}
			}
		}
	}

}
