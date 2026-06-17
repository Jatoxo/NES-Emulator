package net.jatoxo.emu.gui;

import com.formdev.flatlaf.FlatLightLaf;
import net.jatoxo.emu.nes.Cartridge;
import emu.EmulationListener;
import net.jatoxo.emu.nes.Nes;
import emu.parsing.RomParser;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Objects;



public class GUI extends JFrame implements EmulationListener {
	public final String EMU_NAME = "COCK";

	private final NesPanel nesScreen;
	private final FPSThread fpsThread;

	private final PatternViewWindow patternViewWindow;

	public final Nes nes;


	static void main() throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		try {
			UIManager.setLookAndFeel(new FlatLightLaf());
		} catch(Exception e) {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}

		GUI gui = new GUI();


        //insertCartridge("D:\\Users\\Jatoxo\\Downloads\\nestest.nes");
        //MM 2869
        //Cartridge cart = RomParser.parseRom("D:\\Emulators\\NES\\ROMs\\Best NES Games\\Mario\\Super Mario Bros. (World).nes");

        Cartridge cart = null;
        try {
            cart = RomParser.parseRom("rom/AccuracyCoin.nes");
        } catch (Exception e) {
            System.out.println("Couldn't load rom");
        }


        gui.nes.insertCartridge(cart);

		gui.nes.start();
	}






	public GUI() {
		setTitle(EMU_NAME);

        this.nes = new Nes(this);

        nesScreen = new NesPanel(this);

		fpsThread = new FPSThread(this, 400);
		fpsThread.start();


		getContentPane().add(nesScreen, BorderLayout.CENTER);

		requestFocus();


		setupMenus();
		setupFrame();



		patternViewWindow = new PatternViewWindow(nes);
		addKeyListener(new PhysicalInput(nes));
	}



	private void setupMenus() {
		JMenuBar menuBar = new JMenuBar();

		// ------------- FILE MENU -------------------
		JMenu fileMenu = new JMenu("File");
		JMenuItem openRomItem = new JMenuItem("Load ROM");
		fileMenu.add(openRomItem);
		JMenuItem exitRom = new JMenuItem("Exit ROM");
		fileMenu.add(exitRom);
		// --------------------------------------------


		// ------------- EMULATION MENU ---------------
		JMenu emuMenu = new JMenu("Emulation");
		JMenuItem resetOption = new JMenuItem("Reset");
		JMenuItem pauseItem = new JMenuItem("Pause");
		JMenuItem speedItem = new JMenuItem("Set maximum speed...");

		emuMenu.add(pauseItem);
		emuMenu.add(speedItem);
		emuMenu.add(resetOption);

		resetOption.addActionListener(e -> nes.reset());
        pauseItem.addActionListener(e -> nes.paused = !nes.paused);
		// --------------------------------------------

		// --------------- TOOLS MENU -----------------
		JMenu toolsMenu = new JMenu("Tools");
		JMenuItem patternViewer = new JMenuItem("View Pattern Tables..");
		toolsMenu.add(patternViewer);

		patternViewer.addActionListener((e) -> viewPatternTablesClicked());
		// --------------------------------------------


		menuBar.add(fileMenu);
		menuBar.add(emuMenu);
		menuBar.add(toolsMenu);


		setJMenuBar(menuBar);
	}

	private void viewPatternTablesClicked() {
		patternViewWindow.setVisible(true);
	}

	private void setupFrame() {
        setFocusTraversalKeysEnabled(false);

        try {
            Image appIcon = ImageIO.read(Objects.requireNonNull(getClass().getResource("/Nes_controller_square.png")));
            setIconImage(appIcon);
        } catch (IOException e) {
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
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        new DropTarget(this, new EmulatorDropTarget(nes));
    }


    @Override
    public void frameComplete() {
        nesScreen.updateScreen();

        if(patternViewWindow.isVisible()) {
            patternViewWindow.update();
        }

        fpsThread.frameCompleted();
    }
}

