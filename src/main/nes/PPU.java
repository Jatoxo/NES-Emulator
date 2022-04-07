package main.nes;

import main.jorit.rendererererer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;

public class PPU extends BusDevice implements Tickable {
	public static final int SCREEN_WIDTH = 256;
	public static final int SCREEN_HEIGHT = 240;


	// 98 7654 3210
	// || |||| ||||
	// || |||| ||||
	// || |||+-++++- x-Position in Nametable
	// ++-+++----- y-Position in Nametable





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
	private byte[] secondaryOAM;

	private CIRAM ciram;

	long time = 0;

	//TODO: Implement internal data latch

	public static final int PPUCTRL = 0x2000;
	FlagRegister ppuCtrl = new FlagRegister(0x00, Register.BitSize.BITS_8, "PPU Control", "PPUCTRL");
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


	public static final int PPUMASK = 0x2001;
	FlagRegister ppuMask = new FlagRegister(0x00, Register.BitSize.BITS_8, "PPU Mask", "PPUMASK", 0x2001);
	//7  bit  0
	//---- ----
	//BGRs bMmG
	//|||| ||||
	//|||| |||+- Greyscale (0: normal color, 1: produce a greyscale display)
	//|||| ||+-- 1: Show background in leftmost 8 pixels of screen, 0: Hide
	//|||| |+--- 1: Show sprites in leftmost 8 pixels of screen, 0: Hide
	//|||| +---- 1: Show background
	//|||+------ 1: Show sprites
	//||+------- Emphasize red (green on PAL/Dendy)
	//|+-------- Emphasize green (red on PAL/Dendy)
	//+--------- Emphasize blue



	public static final int PPUSTATUS = 0x2002;
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

	public static final int OAMADDR = 0x2003;
	FlagRegister oamAddr = new FlagRegister(0x00, Register.BitSize.BITS_8, "OAM Address", "OAMADDR", 0x2003);

	public static final int OAMDATA = 0x2004;
	FlagRegister oamData = new FlagRegister(0x00, Register.BitSize.BITS_8, "OAM Data", "OAMDATA", 0x2004);

	public static final int PPUSCROLL = 0x2005;
	FlagRegister ppuScroll = new FlagRegister(0x00, Register.BitSize.BITS_8, "PPU Scroll", "PPUSCROLL", 0x2005);

	public static final int PPUADDR = 0x2006;
	FlagRegister ppuAddr = new FlagRegister(0x00, Register.BitSize.BITS_16, "PPU Address", "PPUADDR", 0x2006);

	public static final int PPUDATA = 0x2007;
	FlagRegister ppuData = new FlagRegister(0x00, Register.BitSize.BITS_8, "PPU Data", "PPUDATA", 0x2007);


	private int readBuffer = 0; //https://www.nesdev.org/wiki/PPU_registers#:~:text=avoid%20wrong%20scrolling.-,The%20PPUDATA%20read%20buffer%20(post%2Dfetch),-When%20reading%20while

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
		secondaryOAM = new byte[1<<5]; //32 bytes

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

		ppuMask.addFlag("Grayscale", "G", 0x1); //(0: normal color; 1: produce a monochrome display)
		ppuMask.addFlag("Show background Left", "m", 0x2); //Show Background in leftmost 8 pixels of screen (0: hide; 1: show)
		ppuMask.addFlag("Show sprites Left", "M", 0x4); //Show Sprites in leftmost 8 pixels of screen (0: hide; 1: show)
		ppuMask.addFlag("Show background", "b", 0x8); //Show background
		ppuMask.addFlag("Show sprites", "s", 0x10); //Show sprites
		ppuMask.addFlag("Emphasize red", "R", 0x20); //Emphasize red (green on PAL/Dendy)
		ppuMask.addFlag("Emphasize green", "G", 0x40); //Emphasize green (red on Pal/Dendy)
		ppuMask.addFlag("Emphasize blue", "B", 0x80); //Emphasize blue

		ppuStatus.addFlag("Sprite Overflow", "O", 0x20);
		ppuStatus.addFlag("Sprite 0 Hit", "S", 0x40); //Sprite 0 Hit.  Set when a nonzero pixel of sprite 0 overlaps a nonzero background pixel; cleared at dot 1 of the pre-render line.  Used for raster timing.
		ppuStatus.addFlag("Vertical Blank", "V", 0x80); //Vertical blank has started (0: not in vblank; 1: in vblank).
	}





	private void clock() {
		totalCycles++;

		if(scanline == -1) { //pre-render scanline
			preRenderScanlineCycle(scanlineCycle);

			if(scanlineCycle + 1 == (oddFrame ? 340 : 341)) {
				scanlineCycle = 0;
				scanline++;

			} else {
				scanlineCycle++;
			}

		} else if(scanline < 240) { //visible scanlines

			visibleScanlineCycle(scanline, scanlineCycle);

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

			vBlankScanlineCycle(scanline, scanlineCycle);

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

	//Called for every cycle on the pre-render scanline
	//Scanline -1
	private void preRenderScanlineCycle(int scanlineCycle) {
		//https://www.nesdev.org/wiki/PPU_registers#:~:text=OAMADDR%20is%20set%20to%200%20during%20each%20of%20ticks%20257%2D320
		if(scanlineCycle == 1) {
			//Clear sprite 0 hit flag
			ppuStatus.setFlag("S", false);
		} else if(scanlineCycle >= 257 && scanlineCycle <= 320) {
			oamAddr.set(0x00);
		}

	}

	//Called for every cycle on the visible scanline
	//Scanline 0 - 239
	main.jorit.rendererererer ren = new rendererererer();
	private void visibleScanlineCycle(int scanline, int scanlineCycle) {
		ren.start();
		if(scanline == 30 && scanlineCycle == 64) {
			//TODO: This is faking a sprite 0 hit for SMB
			ppuStatus.setFlag("S", true);
		}
		if(scanlineCycle == 256) { //Sprite eval is technically cycles 65-256, but I do it all at once

			evaluateSprites(scanline);

		} else if(scanlineCycle >= 257 && scanlineCycle <= 320) {
			oamAddr.set(0x00);

		} else if(scanline == 239 && scanlineCycle == 340) {
			renderBackground();
			renderSprites();
			nes.gui.renderScreen(output);
			ren.renderScreen(output);
			//clear output to distinguish sprites
			//output = new BufferedImage(SCREEN_WIDTH, SCREEN_HEIGHT, BufferedImage.TYPE_INT_RGB);
		}
	}

	//Called for every vblank scanline cycle
	//Scanline 241 - 260
	private void vBlankScanlineCycle(int scanline, int scanlineCycle) {
		if(scanline == 241 && scanlineCycle == 1) {
			//set vblank flag
			int val = ppuStatus.get();
			val |= 0x80;

			ppuStatus.set(val);

			if(ppuCtrl.isSet("V")) {
				//Todo: This shouldn't be allowed to stop the cpu during executing an instruction, it should wait until it is finished
				//Due to the current implementation, this might work fine. Instruction are executed whole and the the cpu idle cycles until the correct amount
				//of cycles for each instruction has passed. The only side effect of doing it this way is that the nmi may be called earlier than intended during the cpu's
				//idle cycles
				nes.cpu.nmi();
			}

		}
	}


	private void evaluateSprites(int scanline) {
		Arrays.fill(secondaryOAM, (byte) 0xFF);

		int sOAMpointer = 0; //Points at next free slot in secondary OAM
		//We start reading from OAMADDR and assume proper alignment to the y coordinate of a sprite (https://www.nesdev.org/wiki/PPU_registers#:~:text=The%20first%20OAM%20entry%20to%20be%20checked%20during%20sprite%20evaluation%20is%20the%20one%20starting%20at%20OAM%5BOAMADDR%5D)
		oamAddr.set(oamAddr.get() & ~0x3); //Mask out two LSB to force alignement for now //todo?

		for(int i = oamAddr.get(); i <= 0xFF; i += 0x4) {
			//If current sprite is on the scanline, copy it to OAM if we can
			if(OAM[i] == scanline) {
				if((sOAMpointer & 0x20) == 0) {
					//Copy into seconday OAM
					System.arraycopy(OAM, i, secondaryOAM, sOAMpointer, 4);
					sOAMpointer += 4;

					continue;
				}
				//If the sprite is on the scanline but secondaryOAM is full, set sprite overflow flag
				ppuStatus.setFlag("O", true);

			} else if((sOAMpointer & 0x20) > 0) {
				//If a sprite is not on the scanline and secondaryOAM is full, do this hardware bug
				i += 1;
			}


		}
	}

	//Get the 8-Byte
	byte[] getPatternEntry(int halfSelect, int index, int bitPlaneSelect) {
		//DCBA98 76543210 //PPU addresses within the pattern tables can be decoded as follows:
		//---------------
		//0HRRRR CCCCPTTT
		//|||||| |||||+++- T: Fine Y offset, the row number within a tile
		//|||||| ||||+---- P: Bit plane (0: "lower"; 1: "upper")
		//|||||| ++++----- C: Tile column
		//||++++---------- R: Tile row
		//|+-------------- H: Half of sprite table (0: "left"; 1: "right")
		//+--------------- 0: Pattern table is at $0000-$1FFF
		byte[] data = new byte[8];

		index &= 0xFF;

		int addr = index << 4;
		addr |= (halfSelect > 0) ? 0x1000 : 0; //Select Pattern table half
		addr |= (bitPlaneSelect > 0) ? 0x8 : 0; //Select bitplane
		for(int i = 0; i < 8; i++) {
			addr &= ~0x7;
			addr += i;

			data[i] = (byte) ppuRead(addr);
		}
		return data;
	}

	private void renderSprites() {
		//Sprite rendering depends on the following things:
		//-Sprites at lower indexes in secondaryOAM are layered above others
		//-If Flag M in PPUMASK is not set, sprites don't render in the 8 leftmost Screen pixels
		//-If Flag s in PPUMASK is not set, sprides don't render at all
		//-The priority of a sprite (byte two in OAM entry, Bit 5): 0 - In front of BG, 1 - Behind BG
		//      Sprites behind BG should still be rendered if the BG is transparent
		//-The sprite flipping (byte two in OAM entry, Bit 6-7)
		//-Sprite Size 8x16 bit mode set in PPUCTRL


		//Todo: ignoring sprite priority for now and just plonking them on the screen
		//Todo: Also ignoring 8x16 sprites fuck those
		//loop over secondaryOAM in reverse order (OAM now because im rendering all sprites at once and the evaluation is for every scanline)
		for(int i = 0xFC; i > -1; i -= 4) {
			int y = OAM[i] & 0xFF;
			int x = OAM[i+3] & 0xFF;

			if(y >= SCREEN_HEIGHT) {
				continue;
			}

			byte chrInd = OAM[i+1];

			byte attr = OAM[i+2];
			byte paletteSelect = (byte) (attr & 0x3);

			boolean flipH = (attr & 0x40) > 0;
			boolean flipV = (attr & 0x40) > 0; //Also swaps tiles in 8x16 mode

			byte[] bitPlane0 = getPatternEntry(ppuCtrl.isSet("S") ? 1 : 0, chrInd, 0);
			byte[] bitPlane1 = getPatternEntry(ppuCtrl.isSet("S") ? 1 : 0, chrInd, 1);

			for(int spriteY = 0; spriteY < 8; spriteY++) {
				for(int spriteX = 0; spriteX < 8; spriteX++) {
					byte row0 = bitPlane0[spriteY];
					byte row1 = bitPlane1[spriteY];

					int val0 = (row0 >> (7-spriteX)) & 0x1;
					int val1 = (row1 >> (7-spriteX)) & 0x1;

					int colorSelect = val0 | (val1 << 1);

					//5th bit is always set because we are in sprite palettes
					int paletteIndex = (paletteSelect << 2) | colorSelect | 0x10;

					//Any value 0bXX00 mirrors the universal background color at 0b0000
					paletteIndex = ((paletteIndex & 0x3) == 0) ? 0 : paletteIndex;

					//Don't draw transparent sprites
					if((paletteIndex & 0x3) > 0) {
						int colorByte = palleteRam[paletteIndex];
						int colorOut = palette.colors[colorByte].getRGB();

						output.setRGB((x + (flipH ? 7 - spriteX: spriteX))%SCREEN_WIDTH, (y + spriteY)%SCREEN_HEIGHT, colorOut);
					}

				}
			}

		}
	}

	private void renderBackground() {

		//Go through nametable
		for(int nameT = 0; nameT < 960; nameT++) {

			int chrIndex = ppuRead((0x2000 | ((ppuCtrl.get() & 0x3))<<10) + nameT);

			int halfSelect = ppuCtrl.isSet("B") ? 0x1000 : 0;


			byte[] chrPlane0 = getPatternEntry(halfSelect, chrIndex, 0);
			byte[] chrPlane1 = getPatternEntry(halfSelect, chrIndex, 1);

			for(int row = 0; row < 8; row++) {

				for(int pixel = 0; pixel < 8; pixel++) {
					int bit0 = (chrPlane0[row] >> 7-pixel) & 1;
					int bit1 = (chrPlane1[row] >> 7-pixel) & 1;
					int colorSelect = bit0 | bit1 << 1 ;


					int nameTX = nameT & 0x1F;
					int nameTY = (nameT & 0x3E0) >> 5;

					int attrTX = nameTX >> 2;
					int attrTY = nameTY >> 2;

					int attrIndex = attrTX | (attrTY << 3);

					int attrByte = ppuRead((0x2000 | ((ppuCtrl.get() & 0x3))<<10) | 0x3C0 | attrIndex);

					int byteX = nameTX & 0x2;
					int byteY = nameTY & 0x2;

					int shift = byteX | (byteY << 1);


					int paletteSelect = (attrByte >> shift) & 0x3;

					//The index into the palette ram (4 Bits, 5th bit is only set for Sprite palette)
					int paletteIndex = colorSelect | (paletteSelect << 2);

					//Any value 0bXX00 mirrors the universal background color at 0b0000
					paletteIndex = ((paletteIndex & 0x3) == 0) ? 0 : paletteIndex;

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

			//When two last bits are 0, disable bit A4 (To force Background Palette) since these are mirrors of those locations
			//Todo: Why arent we doing this in ppuRead?
			if((addr & 0x3) == 0) {
				addr &= 0xF;
			}
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

		int val;
		switch(addr) {
			case PPUSTATUS:
				addressLatch = 0;
				val = ppuStatus.get();
				ppuStatus.value &= 0x7f; //Bit 7 is cleared

				return val;

			case PPUDATA:
				//When reading from a location that isn't pallete RAM or it's mirrors, there is a buffer that gets returned and filled only afterwards
				if(ppuAddr.get() < PALLETE_RAM_INDEX_START) {
					val = readBuffer;

					readBuffer = ppuRead(ppuAddr.get());

				} else {
					val = ppuRead(ppuAddr.get());

					//Internal read buffer "bypasses" the color index ram and reads from the underlying memory instead (probably CIRAM)
					readBuffer = ppuBus.read(ppuAddr.get());
				}

				//Increment PPU address depending on PPUCTRL flag
				ppuAddr.set(ppuAddr.get() + ((ppuCtrl.isSet("I")) ? 32 : 1));

				return val;

			case OAMDATA:
				return OAM[oamAddr.get()] & 0xFF;

		}


		return 0;
	}

	//CPU Write
	@Override
	public void write(int addr, int data) {
		//PPU Registers only take up 8 bytes, so mask only the first 3 bits for mirroring
		addr &= 0x7;
		addr += 0x2000;

		int val;
		switch(addr) {
			case PPUCTRL:
				ppuCtrl.set(data);
				break;

			case PPUADDR:
				val = ppuAddr.get();

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
				break;

			case PPUMASK:
				ppuMask.set(data);
				break;

			case PPUDATA:
				ppuWrite(ppuAddr.get(), data);
				ppuAddr.set(ppuAddr.get() + ((ppuCtrl.getFlag("I") > 0) ? 32 : 1));
				break;

			case OAMADDR:
				oamAddr.set(data);
				break;

			case OAMDATA:
				if(scanline > 239) {
					OAM[oamAddr.get()] = (byte) data;
					oamAddr.increment();
				}

				break;

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

