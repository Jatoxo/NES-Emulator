package emu.nes.mappers;

import emu.nes.Nes;
import emu.parsing.ROM;

public abstract class Mapper {
	public static final int NROM = 0;
	public static final int MMC1 = 1;
	public static final int UXROM = 2;
    public static final int CNROM = 3;
    public static final int MMC3 = 4;
	public static final int AXROM = 7;

    public static final int SIZE_1KiB  = 1 << 10;
    public static final int SIZE_2KiB  = 1 << 11;
    public static final int SIZE_4KiB  = 1 << 12;
	public static final int SIZE_8KiB  = 1 << 13;
	public static final int SIZE_16KiB = 1 << 14;
	public static final int SIZE_32KiB = 1 << 15;


	public final int mapperId;

    // Program and Character Memory
    public byte[] programROM;
    //Might be RAM
    public byte[] chrMem;

    public final boolean usesChrRam;

    Nes nes;

    // Describes the standard configuration of the CIRAM A10 mapping
    MirrorMode mirrorMode;

	public Mapper(byte[] programROM, byte[] chrMem, MirrorMode initialMirrorMode, int id) {
		mapperId = id;
        this.programROM = programROM;
        this.mirrorMode = initialMirrorMode;

        if(chrMem == null) {
            this.chrMem = new byte[SIZE_8KiB];
            usesChrRam = true;
        } else {
            this.chrMem = chrMem;
            usesChrRam = false;
        }

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


	public MirrorMode getMirrorMode() {
        return mirrorMode;
    }

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


	public static int readBank(byte[] rom, int bankSize, int bankIndex, int address) {
		//Mask out bits for bank address
		address &= bankSize-1;

        //Todo: Avoid calculating this every time
        int bankCount = rom.length / bankSize;
		int bankStart = (bankIndex & (bankCount - 1)) * bankSize;

		return Byte.toUnsignedInt(rom[bankStart + address]);
	}

	public static void writeBank(byte[] rom, int bankSize, int bankIndex, int address, int value) {
		//Mask out bits for bank address
		address &= bankSize-1;

        int bankCount = rom.length / bankSize;
        int bankStart = (bankIndex & (bankCount - 1)) * bankSize;


		rom[bankStart + address] = (byte) value;
	}

    public void scanlineTick() {};


    public void nesConnected(Nes nes) {
        this.nes = nes;
    }
}
