package main.nes;

import main.Audio;
import main.gui.GUI;
import main.input.Controller;
import main.input.ControllerPorts;
import main.input.StandardController;
import main.nes.apu.APU;
import main.nes.parsing.RomParser;
import main.nes.parsing.UnsupportedRomException;

import java.io.IOException;

import static java.lang.Thread.sleep;


public class Nes {
	public boolean paused = false;

	public Cartridge cartridge;

	public Jtx6502 cpu;
	public PPU ppu;
	public APU apu;
	private MasterClock clock;
	public ControllerPorts controllerPorts;
	public GUI gui;

	public boolean limitSpeed = true;

	public static void main(String[] args) throws InterruptedException {


		//Nes nes = new Nes();

		System.out.println("oof");
	}


	Audio audio;

	public Nes(GUI gui) throws IOException, UnsupportedRomException {
		this.gui = gui;

		cpu = new Jtx6502(this);
		ppu = new PPU(this);
		apu = new APU(this);

		audio = new Audio(apu);


		controllerPorts = new ControllerPorts();
		cpu.bus.addBusDevice(controllerPorts);
		connectController(new StandardController(), 0);

		cpu.bus.addBusDevice(ppu);
		cpu.bus.addBusDevice(apu);

		/*
		clock = new Clock(Clock.NTSC_MASTER_CLOCK_SPEED);
		clock.addListener(cpu, 12);
		clock.addListener(apu, 12);
		clock.addListener(audio, 487);

		clock.addListener(ppu, 4); //ppu go brrrrrr
		*/
		clock = new MasterClock(cpu, ppu, apu, audio);

		//apu.setSequencerClock(clock);

		//insertCartridge("D:\\Users\\Jatoxo\\Downloads\\nestest.nes");

		Cartridge cart = RomParser.parseRom("D:\\GamesSoftware\\ZZ Emulators\\NES\\Games\\mbr.nes");
		//Cartridge cart = RomParser.parseRom("D:\\GamesSoftware\\ZZ Emulators\\NES\\Games\\Micro Mages.nes");
		insertCartridge(cart);




	}

	public void start() {

		while(!paused) {
			long lastTime = System.nanoTime();

			advanceFrame();

			//Todo this isn't accurate at all
			while(limitSpeed && System.nanoTime() - lastTime < 16666666);
		}
	}

	public void advanceFrame() {
		while(!ppu.frameComplete) {
			clock.tick();
		}
		ppu.frameComplete = false;
	}

	public void reset() {
		cpu.reset();
		ppu.reset();
	}

	public void insertCartridge(Cartridge cart) {
		clock.paused = true;

		try {
			sleep(50);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}

		cpu.bus.removeBusDevice(cartridge);
		cartridge = cart;
		cpu.bus.addBusDevice(cart);


		ppu.connectCartridge(cartridge);

		cpu.reset();
		ppu.reset();

		try {
			sleep(50);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}

		clock.paused = false;
	}

	public void connectController(Controller controller, int port) {
		controllerPorts.connectController(controller, port);
	}
}
