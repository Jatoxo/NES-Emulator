package gui;

import com.formdev.flatlaf.FlatLightLaf;
import input.StandardController;
import nes.Nes;
import nes.parsing.RomParser;
import nes.parsing.UnsupportedRomException;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Objects;


public class GUI extends JFrame {


	public static void main(String[] args) throws IOException, UnsupportedRomException, UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		try {
			UIManager.setLookAndFeel(new FlatLightLaf());
		} catch(Exception e) {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}

		GUI gui = new GUI();
		gui.nes.start();
	}

	private final Nes nes;

	private final NesPanel nesScreen = new NesPanel();

	private final String EMU_NAME = "COCK";


	private long lastFrame = 0;
	final LinkedList<Long> fpsBuffer = new LinkedList<>();

	public GUI() throws IOException, UnsupportedRomException {
		setTitle(EMU_NAME);

		Thread fpsThread = getFPSThread();
		fpsThread.start();


		Dimension screenSize = new Dimension(256, 240);


		getContentPane().add(nesScreen, BorderLayout.CENTER);

		requestFocus();
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				StandardController controller = (StandardController) nes.controllerPorts.player1;
				switch(e.getKeyCode()) {
					case KeyEvent.VK_TAB:
						nes.limitSpeed = false;

						break;
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
						break;
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
			}
		});


		setupMenus();
		setupFrame();


		this.nes = new Nes(this);
	}


	private Thread getFPSThread() {
		JFrame f = this;

		//Thread for measuring FPS
		return new Thread(() -> {
			while(true) {
				long cumulativeFPS = 0;
				int size;

				synchronized(fpsBuffer) {
					size = fpsBuffer.size();
					for(long fps : fpsBuffer) {
						cumulativeFPS += fps;
					}
					fpsBuffer.clear();
				}

				int val = Math.round(cumulativeFPS / (float) size);
				f.setTitle(EMU_NAME + " - FPS: " + val);
				try {
					Thread.sleep(500);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
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


		try {
			Image appIcon = ImageIO.read(Objects.requireNonNull(getClass().getResource("/Nes_controller_square.png")));
			setIconImage(appIcon);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}


		GraphicsDevice gd = getGraphicsConfiguration().getDevice();
		double height = gd.getDisplayMode().getHeight() / 1.5;

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(256, 260));
		setSize((int) Math.round((height) / 0.9375), (int) Math.round(height));
		setLocationRelativeTo(null);
		setVisible(true);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					nes.cartridge.storePersistentData();
				} catch(IOException ex) {
					throw new RuntimeException(ex);
				}
			}
		});

		new DropTarget(this, new DropTargetAdapter() {
			@Override
			public void dragOver(DropTargetDragEvent event) {
				event.acceptDrag(DnDConstants.ACTION_LINK);
			}

			@Override
			public void drop(DropTargetDropEvent event) {
				event.acceptDrop(DnDConstants.ACTION_LINK);

				// Get the transfer which can provide the dropped item data
				Transferable transferable = event.getTransferable();

				try {
					// If the drop items are files
					if(event.getCurrentDataFlavors()[0].isFlavorJavaFileListType()) {
						// Get all the dropped files
						java.util.List<File> droppedFiles = (java.util.List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
						// If there are multiple, load only one
						File file = droppedFiles.get(0);
						nes.insertCartridge(RomParser.parseRom(file.getPath()));
					}

				} catch(Exception e) {
					e.printStackTrace();
				}

				// Inform that the drop is complete
				event.dropComplete(true);
			}
		});


	}


	public void renderScreen(byte[] screen) {
		nesScreen.updateScreen(screen);

		long elapsed = System.nanoTime() - lastFrame;
		elapsed = Math.round(elapsed / 1000000.0);

		if(elapsed != 0) {
			long fps = Math.round(1000.0 / elapsed);

			synchronized(fpsBuffer) {
				fpsBuffer.add(fps);
			}
		}

		lastFrame = System.nanoTime();
	}


}

