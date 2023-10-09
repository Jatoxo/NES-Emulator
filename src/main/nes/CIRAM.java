package main.nes;

import main.nes.mappers.Mapper;

public class CIRAM implements PPUBusDevice {
	private final int CIRAM_SIZE = 1<<11;
	private Nes nes;

	private byte[] fakeCiram = new byte[CIRAM_SIZE];


	//The CIRAM needs access to the active cartridge to determine if it should be enabled or not
	public CIRAM(Nes nes) {
		this.nes = nes;
	}

	public void clear() {
		fakeCiram = new byte[CIRAM_SIZE];
	}



	private int ppuToCIRAM(int addr) {
		Mapper.MirrorMode mirrorMode = nes.cartridge.getMirrorMode(addr);

		switch(mirrorMode) {
			case HORIZONTAL:
				//When this mirror mode is active, the address pin A11 from the ppu actually connects to address bit 10 on the CIRAM

				//Clear address bit 10
				addr &= ~(1<<10);
				//Set address bit 10 to address bit 11
				addr |= (addr & (1<<11)) >> 1;
				break;

			case ONE_SCREEN_FIRST:
				//In this mode only the first nametable is active, so A10 is always 0

				//Clear address bit 10
				addr &= ~(1<<10);
				break;
			case ONE_SCREEN_SECOND:
				//In this mode only the second nametable is active, so A10 is always 0

				//Set address bit 10
				addr |= 1 << 10;
				break;
		}


		return addr & 0x7FF;
	}

	@Override
	public int ppuRead(int addr) {


		return fakeCiram[ppuToCIRAM(addr)];
	}

	@Override
	public void ppuWrite(int addr, int data) {
		fakeCiram[ppuToCIRAM(addr)] = (byte) (data & 0xFF);
	}

	@Override
	public boolean isEnabled(int addr) {
		return nes.cartridge.isCIRAMEnabled(addr);
	}

	@Override
	public int getAddrStart() {
		return 0;
	}

	@Override
	public int getAddrEnd() {
		return 0x3FFF;
	}


}
