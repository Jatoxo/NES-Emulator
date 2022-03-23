package main.nes;

import main.input.Controller;
import main.input.ControllerPorts;
import main.input.StandardController;

public class Nes {
	private Jtx6502 cpu;
	private Clock clock;
	private Cartridge cartridge;
	private ControllerPorts controllerPorts;

	public static void main(String[] args) throws InterruptedException {



		Nes nes = new Nes();

		System.out.println("oof");
	}




	public Nes() {

		cpu = new Jtx6502();

		//insertCartridge("D:\\Users\\Jatoxo\\Downloads\\nestest.nes");
		insertCartridge("D:\\Users\\Jatoxo\\Downloads\\Donkey Kong.nes");

		controllerPorts = new ControllerPorts();
		cpu.bus.addBusDevice(controllerPorts);
		connectController(new StandardController(), 0);

		clock = new Clock(Clock.NTSC_MASTER_CLOCK_SPEED);
		clock.addListener(cpu, 12);
		clock.start();

	}

	public void insertCartridge(String filePath) {
		insertCartridge(new Cartridge(filePath));
	}
	public void insertCartridge(Cartridge cart) {
		cpu.bus.removeBusDevice(cartridge);
		cartridge = cart;
		cpu.bus.addBusDevice(cart);
		cpu.reset();
	}

	public void connectController(Controller controller, int port) {
		controllerPorts.connectController(controller, port);
	}
}
