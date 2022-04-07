package main.gui;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLightLaf;
import main.input.StandardController;
import main.nes.Memory;
import main.nes.Nes;
import main.nes.Palette;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class GUI extends JFrame {


	Color palette[] = Palette.defaultPalette().colors;

	private Dimension screenSize = new Dimension(256, 240);

	public BufferedImage img;

	private JLabel render;

	private Nes nes;

	public GUI() {
		super("COCK");


		//screenSize = new Dimension(1, 1);




		img = new BufferedImage(screenSize.width, screenSize.height, BufferedImage.TYPE_INT_RGB);

		render = new JLabel(new ImageIcon(img));

		getContentPane().add(render, BorderLayout.CENTER);


		render.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
				repaint();
			}
		});

		requestFocus();
		addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {

			}

			@Override
			public void keyPressed(KeyEvent e) {
				StandardController controller = (StandardController) nes.controllerPorts.player1;
				switch(e.getKeyCode()) {
					case KeyEvent.VK_W:
						controller.dpadUp = true;
						break;
					case KeyEvent.VK_A:
						controller.dpadLeft = true;
						break;
					case KeyEvent.VK_S:
						controller.dpadDown = true;
						break;
					case KeyEvent.VK_D:
						controller.dpadRight = true;
						break;
					case KeyEvent.VK_SPACE:
						controller.buttonA = true;
						break;
					case KeyEvent.VK_SHIFT:
						controller.buttonB = true;
						break;
					case KeyEvent.VK_MINUS:
						controller.buttonSelect = true;
						break;
					case KeyEvent.VK_ENTER:
						controller.buttonStart = true;
						break;
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				StandardController controller = (StandardController) nes.controllerPorts.player1;
				switch(e.getKeyCode()) {
					case KeyEvent.VK_W:
						controller.dpadUp = false;
						break;
					case KeyEvent.VK_A:
						controller.dpadLeft = false;
						break;
					case KeyEvent.VK_S:
						controller.dpadDown = false;
						break;
					case KeyEvent.VK_D:
						controller.dpadRight = false;
						break;
					case KeyEvent.VK_SPACE:
						controller.buttonA = false;
						break;
					case KeyEvent.VK_SHIFT:
						controller.buttonB = false;
						break;
					case KeyEvent.VK_MINUS:
						controller.buttonSelect = false;
						break;
					case KeyEvent.VK_ENTER:
						controller.buttonStart = false;
						break;
				}
				//System.out.println(KeyEvent.getKeyText(e.getKeyCode()));

			}
		});







		setupMenus();
		setupFrame();


		this.nes = new Nes(this);
	}

	private void setupMenus() {
		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		JMenuItem openRomItem = new JMenuItem("Load ROM");
		fileMenu.add(openRomItem);
		JMenuItem exitRom = new JMenuItem("Exit ROM");
		fileMenu.add(exitRom);

		menuBar.add(fileMenu);

		JMenu emuMenu = new JMenu("Emulation");
		JMenuItem pauseItem = new JMenuItem("Pause");
		emuMenu.add(pauseItem);
		JMenuItem speedItem = new JMenuItem("Set maximum speed...");
		emuMenu.add(speedItem);


		emuMenu.add(pauseItem);


		menuBar.add(emuMenu);

		setJMenuBar(menuBar);

	}
	private void setupFrame() {


		//Theres 9129012 different ways of doing this but this worky
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/Nes_controller_square.png")));

		GraphicsDevice gd = getGraphicsConfiguration().getDevice();
		double height = gd.getDisplayMode().getHeight() / 1.5;

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(500, 469));
		setSize((int) Math.round((height) / 0.9375), (int) Math.round(height));
		setLocationRelativeTo(null);
		setVisible(true);

	}

	public void recock() {
		repaint();
	}

	@Override
	public void repaint() {
		super.repaint();

		/*
		Random r = new Random();
		for(int y = 0; y < screenSize.height; y++) {
			for(int x = 255; x < screenSize.width; x++) {
				int i = r.nextInt(palette.length);
				img.setRGB(x,y,palette[i].getRGB());
			}
		}
		 */




		Image newImg = img.getScaledInstance(render.getWidth(), render.getHeight(), Image.SCALE_DEFAULT);
		render.setIcon(new ImageIcon(newImg));

	}

	public void renderScreen(BufferedImage screen) {
		img = screen;
		repaint();
	}

	public static void main(String[] args) throws InterruptedException, LineUnavailableException {
		try {
			UIManager.setLookAndFeel(new FlatLightLaf());

		} catch(Exception e) {
			e.printStackTrace();
		}

		//JFrame.setDefaultLookAndFeelDecorated(true);
		/*
		AudioFormat format = new AudioFormat(
				500,
				16,//bit
				1,//channel
				true,//signed
				false //little endian
		);
		SourceDataLine line = AudioSystem.getSourceDataLine(format);
		line.open();
		line.start();
		while(true) {
			Thread.sleep(3);
			System.out.println("Bop");


			byte[] b = new byte[2];
			b[0] = 0x40;
			b[1] = 0x40;

			System.out.println(line.available());
			line.write(b, 0, 2);

		}

		 */

		GUI gui = new GUI();
		gui.nes.start();


		/*
		Random r = new Random();
		while(true) {
			try {
				Thread.sleep(16 * (r.nextInt(2) + 1) );
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			gui.recock();
		}
		 */
	}


}
