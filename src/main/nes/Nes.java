package main.nes;

import main.input.Controller;
import main.input.ControllerPorts;
import main.input.StandardController;


public class Nes {
	public Cartridge cartridge;

	private Jtx6502 cpu;
	private PPU ppu;
	private Clock clock;
	private ControllerPorts controllerPorts;

	public static void main(String[] args) throws InterruptedException {




		Nes nes = new Nes();

		System.out.println("oof");
	}



	public Nes() {

		cpu = new Jtx6502();
		ppu = new PPU();

		//insertCartridge("D:\\Users\\Jatoxo\\Downloads\\nestest.nes");
		insertCartridge("D:\\Users\\Jatoxo\\Downloads\\nestest.nes");

		controllerPorts = new ControllerPorts();
		cpu.bus.addBusDevice(controllerPorts);
		connectController(new StandardController(), 0);

		cpu.bus.addBusDevice(ppu);

		clock = new Clock(Clock.NTSC_MASTER_CLOCK_SPEED);
		clock.addListener(cpu, 12);
		clock.addListener(ppu, 4); //ppu go brrrrrr
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
