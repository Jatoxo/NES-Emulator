package main.gui;

import com.formdev.flatlaf.FlatLightLaf;
import main.input.StandardController;
import main.nes.Nes;
import main.nes.Palette;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;



public class GUI extends JFrame implements DropTargetListener {


	Color palette[] = Palette.defaultPalette().colors;

	private Dimension screenSize = new Dimension(256, 240);

	public BufferedImage img;

	private JLabel render;

	private Nes nes;

	private final String EMU_NAME = "COCK";

	private long lastFrame = 0;
	final ArrayList<Long> fpsBuffer = new ArrayList<>();

	public GUI() {
		setTitle(EMU_NAME);

		JFrame f = this;
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					long cumulativeFPS = 0;
					int size = 0;
					synchronized(fpsBuffer) {
						size = fpsBuffer.size();
						for(long fps : fpsBuffer) {
							cumulativeFPS += fps;
						}
						fpsBuffer.clear();
					}

					int val = Math.round(cumulativeFPS / (float)size);
					f.setTitle(EMU_NAME + " - FPS: " + val);
					try {
						Thread.sleep(500);
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		thread.start();


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
					case KeyEvent.VK_TAB:
						nes.limitSpeed = false;
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
					case KeyEvent.VK_TAB:
						nes.limitSpeed = true;
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

		setFocusTraversalKeysEnabled(false);

		new DropTarget(this, this);

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




		//Image newImg = img.getScaledInstance(render.getWidth(), render.getHeight(), Image.SCALE_DEFAULT);
		Image newImg = img.getScaledInstance(render.getWidth(), render.getHeight(), Image.SCALE_FAST);
		render.setIcon(new ImageIcon(newImg));

	}

	public void renderScreen(BufferedImage screen) {

		img = screen;
		repaint();


		long elapsed = System.nanoTime() - lastFrame;
		elapsed = Math.round(elapsed / 1000000.0);

		if(elapsed != 0) {
			long fps = Math.round(1000.0 / elapsed);

			synchronized (fpsBuffer) {
				fpsBuffer.add(fps);
			}
		}

		lastFrame = System.nanoTime();
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
		int i = 0;
		while(i < 999999) {
			Thread.sleep(500);
			System.out.println("Bop");


			byte[] b = new byte[200];
			for(int j = 0; j < b.length; j += 4) {
				b[j] = (byte) 0xFF;
				b[j + 1] = (byte) 0x7F;
			}
			System.out.println(Arrays.toString(b));

			System.out.println(line.available());
			line.write(b, 0, 200);
			i++;
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


	@Override
	public void dragEnter(DropTargetDragEvent event) {
		//event.acceptDrag(DnDConstants.ACTION_LINK);
	}

	@Override
	public void dragOver(DropTargetDragEvent event) {
		event.acceptDrag(DnDConstants.ACTION_LINK);
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent event) {
		//event.acceptDrag(DnDConstants.ACTION_LINK);
	}

	@Override
	public void dragExit(DropTargetEvent dte) {

	}

	@Override
	public void drop(DropTargetDropEvent event) {
		// Accept copy drops
		event.acceptDrop(DnDConstants.ACTION_LINK);

		// Get the transfer which can provide the dropped item data
		Transferable transferable = event.getTransferable();

		try {
			// If the drop items are files
			if (event.getCurrentDataFlavors()[0].isFlavorJavaFileListType()) {
				// Get all of the dropped files
				java.util.List<File> files = (java.util.List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
				// Loop them through
				for (File file : files) {
					nes.insertCartridge(file.getPath());
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Inform that the drop is complete
		event.dropComplete(true);
	}
}
