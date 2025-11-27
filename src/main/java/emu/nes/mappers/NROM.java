package emu.nes.mappers;

public class NROM extends Mapper {
	//https://www.nesdev.org/wiki/NROM


	//16kb or 32kb of PRG ROM
	private final byte[] prgRom;

	//8kb of CHR ROM only
	private final byte[] chrRom;

	//On NROM cartridges, the mirroring mode is hardwired to either horizontal or vertical
	private final MirrorMode mirrorMode;

	private final boolean usesChrRam;

	private int bankSize;


	public NROM(byte[] prgRom, byte[] chrRom, MirrorMode mirrorMode) {
		super(NROM);

		//If no CHR ROM is present, use 8kb CHR RAM
		this.usesChrRam = chrRom == null;
		if(usesChrRam) {
			this.chrRom = new byte[8192];
		} else {
			this.chrRom = chrRom;
		}
		this.prgRom = prgRom;

		bankSize = SIZE_16KiB;
		if(prgRom.length > SIZE_16KiB) {
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

		return readBank(prgRom, bankSize, 0, address);
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
		return readBank(chrRom, SIZE_8KiB, 0, address);
	}

	@Override
	public void ppuWrite(int address, int data) {
		if(!usesChrRam) {
			System.out.printf("NROM: Write to CHR ROM at %s...\n", Integer.toHexString(address));
            return;
		}

		writeBank(chrRom, SIZE_8KiB, 0, address, data);
	}

	@Override
	public MirrorMode getMirrorMode(int address) {
		return mirrorMode;
	}

}
