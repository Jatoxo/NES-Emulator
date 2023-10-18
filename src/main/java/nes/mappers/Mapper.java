package nes.mappers;

public abstract class Mapper {
	public static final int NROM = 0;
	public static final int MMC1 = 1;
	public static final int AXROM = 7;

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
	public boolean isCIRAMEnabled(int address) {
		//Default implementation assigns last half to CHR ROM and first half to CIRAM
		//When bit 13 of the address is set, the CIRAM should be enabled
		return (address & (1 << 13)) > 0;
	}

	//whether the CHR memory of the cart is enabled when accessing this address
	public boolean isChrRomEnabled(int address) {
		//Default implementation assigns last half to CHR ROM and first half to CIRAM
		//CHR ROM will be disabled when bit 13 of the address is set (CIRAM is enabled)
		return (address & (1 << 13)) == 0;
	}

	/**
	 * If this cartridge contains PRG RAM, returns the current contents of it
	 * If there are multiple banks, all banks are returned starting with the lowest.
	 * @return The PGR RAM, or an empty array if non is present
	 */
	public byte[] getProgramRAM() {
		//By default, return empty array since not all boards have prg ram
		return new byte[0];
	}

	public void setProgramRAM(byte[] newProgramRam) {}
}
