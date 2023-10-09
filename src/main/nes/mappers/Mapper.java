package main.nes.mappers;

public abstract class Mapper {
	public static final int NROM = 0;
	public static final int MMC1 = 1;

	public final int mapperId;

	public Mapper(int id) {
		mapperId = id;
	}


	public enum MirrorMode {
		VERTICAL, //VRAM A10 is connected to CIRAM A10
		HORIZONTAL, //VRAM A11 is connected to CIRAM A10
		ONE_SCREEN_FIRST, //CIRAM A10 always low
		ONE_SCREEN_SECOND, //CIRAM A10 always high
	}

	//16-Bit address, 8-Bit data
	public abstract int cpuRead(int address);
	public abstract void cpuWrite(int address, int data);

	public abstract int ppuRead(int address);
	public abstract void ppuWrite(int address, int data);


	public abstract MirrorMode getMirrorMode(int address);

	//whether the CIRAM is enabled when accessing this address
	public abstract boolean isCIRAMEnabled(int address);

	//whether the CHR memory of the cart is enabled when accessing this address
	public abstract boolean isChrRomEnabled(int address);
}
