package main.nes;

import java.awt.image.BufferedImage;

public class PPU extends BusDevice implements Tickable {
	public static final int SCREEN_WIDTH = 256;
	public static final int SCREEN_HEIGHT = 240;

	BufferedImage output;
	Palette palette;

	long totalCycles = 0;

	//Inclusive
	public static final int PALLETE_RAM_INDEX_START = 0x3F00;
	//Inclusive
	public static final int PALLETE_RAM_INDEX_END = 0x3FFF;

	private Cartridge cartridge;
	private PPUBus ppuBus;
	private byte[] palleteRam;
	private byte[] OAM;



	public PPU() {
		//CPU can access PPU through memory mapped registers between 0x2000 and 0x2007, which are then mirrored all
		//the way until 0x3FFF
		super(0x2000, 0x3FFF);

		output = new BufferedImage(SCREEN_WIDTH, SCREEN_HEIGHT, BufferedImage.TYPE_INT_RGB);
		palette = Palette.defaultPalette();

		palleteRam = new byte[1<<5]; //32 bytes
		OAM = new byte[1<<8]; //256 bytes

		ppuBus = new PPUBus();

	}

	private void clock() {
		totalCycles++;

	}

	public void reset() {
		//Todo: Do the whole PPU powerup state
		totalCycles = 0;
	}

	//14-Bit address
	public int ppuRead(int addr) {
		addr &= 0x3FFF;

		//Palette RAM does not use the bus
		if(addr >= PALLETE_RAM_INDEX_START) {
			//Mask out 5 bits for mirroring
			addr &= 0x1F;
			return palleteRam[addr] & 0xFF;
		}

		return ppuBus.read(addr) & 0xFF;
	}

	//14-Bit address, 8-Bit data
	public void ppuWrite(int addr, int data) {
		addr &= 0x3FFF;
		data &= 0xFF;

		//Palette RAM does not use the bus
		if(addr >= PALLETE_RAM_INDEX_START) {
			//Mask out 5 bits for mirroring
			addr &= 0x1F;
			palleteRam[addr] = (byte) data;

			return;
		}

		ppuBus.write(addr, data);
	}



	//CPU Read
	@Override
	public int read(int addr) {
		//TODO: Implement memory mapped registers
		return 0;
	}

	//CPU Write
	@Override
	public void write(int addr, int data) {

	}


	@Override
	public void tick() {
		clock();
	}

	public void connectCartridge(Cartridge cartridge) {
		if(this.cartridge != null) {
			this.ppuBus.removeBusDevice(this.cartridge);
		}

		this.cartridge = cartridge;
		ppuBus.addBusDevice(cartridge);
	}
}

