package main.nes;

public class Nes {
	private Jtx6502 cpu;
	private Clock clock;
	private Cartridge cartridge;

	public static void main(String[] args) throws InterruptedException {
		Nes nes = new Nes();

		System.out.println("oof");
	}




	public Nes() {

		cpu = new Jtx6502();

		insertCartridge("D:\\Users\\Jatoxo\\Downloads\\nestest.nes");

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
}
