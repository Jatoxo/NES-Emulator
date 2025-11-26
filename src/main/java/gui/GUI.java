package gui;

import com.formdev.flatlaf.FlatLightLaf;
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
import java.util.Objects;



public class GUI extends JFrame {
	public final String EMU_NAME = "COCK";

	private final NesPanel nesScreen = new NesPanel();
	private final FPSThread fpsThread;
	private long lastFrame = 0;

	private final Nes nes;


	public static void main(String[] args) throws IOException, UnsupportedRomException, UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		try {
			UIManager.setLookAndFeel(new FlatLightLaf());
		} catch(Exception e) {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}

		GUI gui = new GUI();
		gui.nes.start();
	}






	public GUI() throws IOException, UnsupportedRomException {
		setTitle(EMU_NAME);

		fpsThread = new FPSThread(this, 400);
		fpsThread.start();

		getContentPane().add(nesScreen, BorderLayout.CENTER);

		requestFocus();

		setupMenus();
		setupFrame();

		this.nes = new Nes(this);

		addKeyListener(new PhysicalInput(nes));
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
		JMenuItem resetOption = new JMenuItem("Reset");
		emuMenu.add(resetOption);
		JMenuItem pauseItem = new JMenuItem("Pause");
		emuMenu.add(pauseItem);
		JMenuItem speedItem = new JMenuItem("Set maximum speed...");
		emuMenu.add(speedItem);

		resetOption.addActionListener(e -> nes.reset());

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
			int fps = (int) Math.round(1000.0 / elapsed);
			fpsThread.addFPSValue(fps);
		}

		lastFrame = System.nanoTime();
	}


}

