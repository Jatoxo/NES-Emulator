package main.nes.mappers;

public abstract class Mapper {
	public static final int NROM = 0;

	public final int mapperId;

	public Mapper(int id) {
		mapperId = id;
	}


	public enum MirrorMode {
		VERTICAL, //VRAM A10 is connected to CIRAM A10
		HORIZONTAL //VRAM A11 is conected to CIRAM A10
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
