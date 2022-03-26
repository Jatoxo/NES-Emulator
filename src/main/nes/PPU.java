package main.nes;

import java.awt.image.BufferedImage;
import java.util.Random;

public class PPU extends BusDevice implements Tickable {
	public static final int SCREEN_WIDTH = 256;
	public static final int SCREEN_HEIGHT = 240;

	Nes nes;

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
	private CIRAM ciram;

	long time = 0;

	//TODO: Implement internal data latch

	FlagRegister ppuCtrl = new FlagRegister(0x00, Register.BitSize.BITS_8, "PPU Control", "PPUCTRL", 0x2000);
	//7  bit  0
	//---- ----
	//VPHB SINN
	//|||| ||||
	//|||| ||++- Base nametable address
	//|||| ||    (0 = $2000; 1 = $2400; 2 = $2800; 3 = $2C00)
	//|||| |+--- VRAM address increment per CPU read/write of PPUDATA
	//|||| |     (0: add 1, going across; 1: add 32, going down)
	//|||| +---- Sprite pattern table address for 8x8 sprites
	//||||       (0: $0000; 1: $1000; ignored in 8x16 mode)
	//|||+------ Background pattern table address (0: $0000; 1: $1000)
	//||+------- Sprite size (0: 8x8 pixels; 1: 8x16 pixels)
	//|+-------- PPU master/slave select
	//|          (0: read backdrop from EXT pins; 1: output color on EXT pins)
	//+--------- Generate an NMI at the start of the vertical blanking interval (0: off; 1: on)


	FlagRegister ppuMask = new FlagRegister(0x00, Register.BitSize.BITS_8, "PPU Mask", "PPUMASK", 0x2001);
	FlagRegister ppuStatus = new FlagRegister(0x00, Register.BitSize.BITS_8, "PPU Status", "PPUSTATUS", 0x2002);
	//7  bit  0
	//---- ----
	//VSO. ....
	//|||| ||||
	//|||+-++++- Least significant bits previously written into a PPU register
	//|||        (due to register not being updated for this address)
	//||+------- Sprite overflow. The intent was for this flag to be set
	//||         whenever more than eight sprites appear on a scanline, but a
	//||         hardware bug causes the actual behavior to be more complicated
	//||         and generate false positives as well as false negatives; see
	//||         PPU sprite evaluation. This flag is set during sprite
	//||         evaluation and cleared at dot 1 (the second dot) of the
	//||         pre-render line.
	//|+-------- Sprite 0 Hit.  Set when a nonzero pixel of sprite 0 overlaps
	//|          a nonzero background pixel; cleared at dot 1 of the pre-render
	//|          line.  Used for raster timing.
	//+--------- Vertical blank has started (0: not in vblank; 1: in vblank).
	//           Set at dot 1 of line 241 (the line *after* the post-render
	//           line); cleared after reading $2002 and at dot 1 of the
	//           pre-render line.


	FlagRegister oamAddr = new FlagRegister(0x00, Register.BitSize.BITS_8, "OAM Address", "OAMADDR", 0x2003);
	FlagRegister oamData = new FlagRegister(0x00, Register.BitSize.BITS_8, "OAM Data", "OAMDATA", 0x2004);
	FlagRegister ppuScroll = new FlagRegister(0x00, Register.BitSize.BITS_8, "PPU Scroll", "PPUSCROLL", 0x2005);
	FlagRegister ppuAddr = new FlagRegister(0x00, Register.BitSize.BITS_16, "PPU Address", "PPUADDR", 0x2006);
	FlagRegister ppuData = new FlagRegister(0x00, Register.BitSize.BITS_8, "PPU Data", "PPUDATA", 0x2007);




	private int addressLatch = 0;

	private int scanline = -1;
	private int scanlineCycle = 0;
	private boolean oddFrame = false;


	public PPU(Nes nes) {
		//CPU can access PPU through memory mapped registers between 0x2000 and 0x2007, which are then mirrored all
		//the way until 0x3FFF
		super(0x2000, 0x3FFF);

		this.nes = nes;

		output = new BufferedImage(SCREEN_WIDTH, SCREEN_HEIGHT, BufferedImage.TYPE_INT_RGB);
		palette = Palette.defaultPalette();

		palleteRam = new byte[1<<5]; //32 bytes
		OAM = new byte[1<<8]; //256 bytes

		ppuBus = new PPUBus();

		ciram = new CIRAM(nes);
		ppuBus.addBusDevice(ciram);


		ppuCtrl.addFlag("Nametable Select", "NN", 0x3); //(0 = $2000; 1 = $2400; 2 = $2800; 3 = $2C00)
		ppuCtrl.addFlag("Increment", "I", 0x4);  //(0: add 1, going across; 1: add 32, going down)
		ppuCtrl.addFlag("Sprite Pattern Table", "S", 0x8); //(0 = $0000; 1 = $1000)
		ppuCtrl.addFlag("Background Pattern Table", "B", 0x10); //(0 = $0000; 1 = $1000)
		ppuCtrl.addFlag("Sprite Size", "H", 0x20); //(0 = 8x8; 1 = 8x16)
		ppuCtrl.addFlag("Master/Slave", "P", 0x40); //(0: read backdrop from EXT pins; 1: output color on EXT pins)
		ppuCtrl.addFlag("Generate NMI", "V", 0x80); //Generate an NMI at the start of the vertical blanking interval (0: off; 1: on)

		ppuStatus.addFlag("Sprite Overflow", "O", 0x20);
		ppuStatus.addFlag("Sprite 0 Hit", "S", 0x40); //Sprite 0 Hit.  Set when a nonzero pixel of sprite 0 overlaps a nonzero background pixel; cleared at dot 1 of the pre-render line.  Used for raster timing.
		ppuStatus.addFlag("Vertical Blank", "V", 0x80); //Vertical blank has started (0: not in vblank; 1: in vblank).
	}





	private void clock() {
		totalCycles++;

		if(scanline == -1) {
			if(scanlineCycle + 1 == (oddFrame ? 340 : 341)) {
				scanlineCycle = 0;
				scanline++;

			} else {
				scanlineCycle++;
			}

		} else if(scanline < 240) { //visible scanlines
			if(scanline == 239 && scanlineCycle == 340) {
				renderBackground();
				nes.gui.renderScreen(output);
			}

			scanlineCycle++;

			if(scanlineCycle == 341) {
				scanlineCycle = 0;
				scanline++;
			}

		} else if(scanline == 240) { //post-render scanline
			scanlineCycle++;
			if(scanlineCycle == 341) {
				scanlineCycle = 0;
				scanline++;
			}
		} else if(scanline > 240) { //vblank

			if(scanline == 241 && scanlineCycle == 1) {
				//set vblank flag
				int val = ppuStatus.get();
				val |= 0x80;

				ppuStatus.set(val);

				if(ppuCtrl.isSet("V")) {
					nes.cpu.nmi();
				}

			}

			if(scanlineCycle == 340) {
				scanlineCycle = 0;
				scanline++;
			} else {
				scanlineCycle++;
			}

			if(scanline == 261) {
				scanline = -1;
			}
		}

	}

	private void renderBackground() {

		//Go through nametable
		for(int nameT = 0; nameT < 960; nameT++) {

			int chrIndex = ppuRead(0x2000 + nameT);
			if(ppuCtrl.getFlag("S") > 0) {
				chrIndex += 0x2000;
				System.out.println("a");
			}
			if(chrIndex == 0x62) {
				System.out.println("hit");
			}

			byte[] chrPlane0 = new byte[8];
			for(int i = 0; i < 8; i++) {
				int addr = (chrIndex << 4) + i;
				chrPlane0[i] = (byte) ppuRead(addr);
			}

			byte[] chrPlane1 = new byte[8*8];
			for(int i = 0; i < 8; i++) {
				int addr = (chrIndex << 4) + i;

				addr |= 0x8; //Select bitplane 1

				chrPlane1[i] = (byte) ppuRead(addr);
			}

			for(int row = 0; row < 8; row++) {

				for(int pixel = 0; pixel < 8; pixel++) {
					int bit0 = (chrPlane0[row] >> 7-pixel) & 1;
					int bit1 = (chrPlane1[row] >> 7-pixel) & 1;
					int color = bit0 | bit1 << 1 ;

					//Todo: Assuming palette 0 for now
					int paletteIndex = color;
					int colorByte = palleteRam[paletteIndex];
					int rgbOut = palette.colors[colorByte].getRGB();

					int x = (nameT&0x1F) * 8 + pixel;
					int y = ((nameT&0x3E0) >> 5) * 8 + row;


					output.setRGB(x, y, rgbOut);

				}

			}






		}

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

		//TODO: Implement PPU Registers & read buffer

		//PPU Registers only take up 8 bytes, so mask only the first 3 bits for mirroring
		addr &= 0x7;
		addr += 0x2000;

		if(addr == ppuStatus.address) {
			addressLatch = 0;
			int val = ppuStatus.get();
			ppuStatus.value &= 0x7f; //Bit 7 is cleared

			return val;


		} else if(addr == ppuData.address) {
			int val = ppuAddr.get();

			ppuAddr.set(ppuAddr.get() + ((ppuCtrl.getFlag("I") > 0) ? 32 : 1));

			return ppuRead(val);


		}

		return 0;
	}

	//CPU Write
	@Override
	public void write(int addr, int data) {
		//PPU Registers only take up 8 bytes, so mask only the first 3 bits for mirroring
		addr &= 0x7;
		addr += 0x2000;

		if(addr == ppuCtrl.address){
			ppuCtrl.set(data);

		} else if(addr == ppuAddr.address) {
			int val = ppuAddr.get();

			if(addressLatch == 0) {
				//Mask high byte to the bottom 6 bits to limit max address to 0x3FFF
				data &= 0x3F;

				//Reset high byte
				val &= ~0xFF00;
				//set high byte
				val |= (data & 0xFF) << 8;
				addressLatch = 1;
			} else {
				//Reset low byte
				val &= ~0x00FF;
				//set low byte
				val |= data & 0xFF;
				addressLatch = 0;
			}

			ppuAddr.set(val);


		} else if(addr == ppuData.address) {
			ppuWrite(ppuAddr.get(), data);
			ppuAddr.set(ppuAddr.get() + ((ppuCtrl.getFlag("I") > 0) ? 32 : 1));
		}

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

