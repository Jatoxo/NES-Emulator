package main.nes.mappers;

public class NROM extends Mapper {
	//https://www.nesdev.org/wiki/NROM


	//16kb or 32kb of PRG ROM
	private byte[] prgRom;

	//8kb of CHR ROM only
	private byte[] chrRom;

	private MirrorMode mirrorMode;
	private int prgBanks;


	public NROM(byte[] prgRom, int prgBanks, byte[] chrRom, MirrorMode mirrorMode) {
		super(0);
		this.prgRom = prgRom;
		this.chrRom = chrRom;
		this.mirrorMode = mirrorMode;
		this.prgBanks = prgBanks;
	}

	@Override
	public int cpuRead(int address) {
		//Prg Rom starts at 0x8000
		if(address < 0x8000) {
			return 0;
		}

		//Mask out to be in cartridge address
		address &= 0x7FFF;


		if(prgBanks == 1) {
			//Mask out 16kb chunk selector
			address &= 0x3FFF;
		}

		return prgRom[address] & 0xFF;

	}

	@Override
	public void cpuWrite(int address, int data) {
		System.out.println("Trying to write to ROM... Yea...");
	}

	@Override
	public int ppuRead(int address) {
		//Todo mask out like i did with cpu read idk maybe not necessary bc starts at 0
		return chrRom[address] & 0xFF;
	}

	@Override
	public void ppuWrite(int address, int data) {
		System.out.println("Trying to write to ROM... Yea...");
	}

	@Override
	public MirrorMode getMirrorMode(int address) {
		return mirrorMode;
	}

	@Override
	public boolean isCIRAMEnabled(int address) {
		//When bit 13 of the address is set, the CIRAM should be enabled
		return (address & (1 << 13)) > 0;
	}

	@Override
	public boolean isChrRomEnabled(int address) {
		//The Character Rom should only be used when the CIRAM isn't
		return !isCIRAMEnabled(address);
	}
}
