package nes.mappers;

public class NROM extends Mapper {
	//https://www.nesdev.org/wiki/NROM


	//16kb or 32kb of PRG ROM
	private final byte[] prgRom;

	//8kb of CHR ROM only
	private final byte[] chrRom;

	//On NROM cartridges, the mirroring mode is hardwired to either horizontal or vertical
	private final MirrorMode mirrorMode;


	public NROM(byte[] prgRom, byte[] chrRom, MirrorMode mirrorMode) {
		super(NROM);
		this.prgRom = prgRom;
		this.chrRom = chrRom;
		this.mirrorMode = mirrorMode;
	}

	@Override
	public int cpuRead(int address) {
		//Prg Rom starts at 0x8000
		if(address < 0x8000) {
			return 0;
		}

		//Mask out to be in cartridge address
		address &= 0x7FFF;


		if(address >= prgRom.length) {
			//Mask out 16kb chunk selector
			address &= 0x3FFF;
		}

		return prgRom[address] & 0xFF;

	}

	@Override
	public void cpuWrite(int address, int data) {
		//TODO: Tie together logic for reading and writing
		//System.out.println("Trying to write to ROM... Yea...");

		//Prg Rom starts at 0x8000
		if(address < 0x8000) {
			return;
		}

		//Mask out to be in cartridge address
		address &= 0x7FFF;


		if(address >= prgRom.length) {
			//Mask out 16kb chunk selector
			address &= 0x3FFF;
		}

		prgRom[address] = (byte) data;
	}

	@Override
	public int ppuRead(int address) {
		//CHR ROM starts at 0x0000, and is 8kb in size (0x0000 - 0x1FFF)
		//CHR ROM will be disabled when bit 13 of the address is set (CIRAM is enabled)
		return chrRom[address] & 0xFF;
	}

	@Override
	public void ppuWrite(int address, int data) {
		System.out.printf("NROM: Write to CHR ROM at %s...\n", Integer.toHexString(address));

		//Do it anyway lol
		chrRom[address] = (byte) data;
	}

	@Override
	public MirrorMode getMirrorMode(int address) {
		return mirrorMode;
	}

}
