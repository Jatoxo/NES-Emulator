package emu.nes.mappers;


public class NROM extends Mapper {
	//https://www.nesdev.org/wiki/NROM

	private int bankSize;


	public NROM(byte[] prg, byte[] chr, MirrorMode mirrorMode) {
		super(prg, chr, mirrorMode, NROM);

		bankSize = SIZE_16KiB;
		if(programROM.length > SIZE_16KiB) {
			bankSize = SIZE_32KiB;
		}

		this.mirrorMode = mirrorMode;
	}

	@Override
	public int cpuRead(int address) {
		//Prg Rom starts at 0x8000
		if(address < 0x8000) {
			return -1;
		}

		return readBank(programROM, bankSize, 0, address);
	}

	@Override
	public void cpuWrite(int address, int data) {
		//Prg Rom starts at 0x8000
		if(address < 0x8000) {
			return;
		}

		System.out.printf("NROM: Trying to write to ROM at %s... Yea...\n", Integer.toHexString(address));
	}

	@Override
	public int ppuRead(int address) {
		//CHR ROM starts at 0x0000, and is 8kb in size (0x0000 - 0x1FFF)
		//CHR ROM will be disabled when bit 13 of the address is set (CIRAM is enabled)
		return readBank(chrMem, SIZE_8KiB, 0, address);
	}

	@Override
	public void ppuWrite(int address, int data) {
		if(!usesChrRam) {
			System.out.printf("NROM: Write to CHR ROM at %s...\n", Integer.toHexString(address));
            return;
		}

		writeBank(chrMem, SIZE_8KiB, 0, address, data);
	}


}
