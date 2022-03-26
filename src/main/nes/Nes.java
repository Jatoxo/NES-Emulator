package main.nes;

import main.gui.GUI;
import main.input.Controller;
import main.input.ControllerPorts;
import main.input.StandardController;


public class Nes {
	public Cartridge cartridge;

	public Jtx6502 cpu;
	public PPU ppu;
	private Clock clock;
	public ControllerPorts controllerPorts;
	public GUI gui;

	public static void main(String[] args) throws InterruptedException {




		//Nes nes = new Nes();

		System.out.println("oof");
	}



	public Nes(GUI gui) {
		this.gui = gui;

		cpu = new Jtx6502();
		ppu = new PPU(this);

		//insertCartridge("D:\\Users\\Jatoxo\\Downloads\\nestest.nes");
		insertCartridge("D:\\GamesSoftware\\ZZ Emulators\\NES\\Games\\mbr.nes");

		controllerPorts = new ControllerPorts();
		cpu.bus.addBusDevice(controllerPorts);
		connectController(new StandardController(), 0);

		cpu.bus.addBusDevice(ppu);

		clock = new Clock(Clock.NTSC_MASTER_CLOCK_SPEED);
		clock.addListener(cpu, 12);
		clock.addListener(ppu, 4); //ppu go brrrrrr


	}

	public void start() {
		clock.start();
	}

	public void insertCartridge(String filePath) {
		insertCartridge(new Cartridge(filePath));
	}
	public void insertCartridge(Cartridge cart) {
		cpu.bus.removeBusDevice(cartridge);
		cartridge = cart;
		cpu.bus.addBusDevice(cart);

		ppu.connectCartridge(cartridge);

		cpu.reset();
		ppu.reset();
	}

	public void connectController(Controller controller, int port) {
		controllerPorts.connectController(controller, port);
	}
}
