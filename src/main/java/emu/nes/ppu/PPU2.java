package nes;

import emu.nes.BusDevice;
import emu.nes.BusValue;
import emu.nes.Nes;
import emu.nes.Palette;
import emu.nes.ppu.CIRAM;
import emu.nes.ppu.PPUBus;

public class PPU2 extends BusDevice {
    public static final int PALETTE_RAM_INDEX_START = 0x3F00; //Inclusive

    private final Nes nes;

    // Rendering shift registers store tile and attribute data

    // 2 16-Bit registers contain pattern data for the background
    //                                    [BBBBBBBB] - Next tile's pattern data,
    //                                    [BBBBBBBB] - 2 bits per pixel
    //                                     ||||||||<----[Transfers every inc hori(v)]
    //                                     vvvvvvvv
    // Serial-to-parallel - [AAAAAAAA] <- [BBBBBBBB] <- [1...] - Parallel-to-serial  (low plane)
    //    shift registers - [AAAAAAAA] <- [BBBBBBBB] <- [0...] - shift registers     (high plane)
    int patternRegisterLow;
    int patternRegisterHigh;


    // 8-Bit attribute register
    int attributeRegisterLow;
    int attributeRegisterHigh;
    // Serial-to-parallel - [PPPPPPPP] <- [P] - 1-bit latch
    //    shift registers - [PPPPPPPP] <- [P] - 1-bit latch
    //                                     ^
    //                                     |<--------[Transfers every inc hori(v)]
    //                                [  Mux   ]<----[coarse_x bit 1 and coarse_y bit 1 select 2 bits]
    //                                 ||||||||
    //                                 ^^^^^^^^
    //                                [PPPPPPPP] - Next tile's attributes data




    // The two rendering registers known as the "loopy registers"

    // "Temp" register that receives and stores scrolling positions for them to copied into vReg at the right
    // moment by the PPU
    private int tRegister = 0;

    // Used for addressing on the PPU
    private int vRegister = 0;

    // This flag is toggled after a write to PPUSCROLL or PPUADDR
    // as they are set in two parts
    // (w register on nesdev)
    private boolean writeToggle = false;

    //------------ PPUCTRL Flags -------------------
    private boolean vramAddrIncreaseMode  ; // False: Increase vram address by 1 after write to PPUDATA / // True: Increase by 32
    private int     spritePatternTable    ;
    private int     backgroundPatternTable;
    private boolean useLargeSprites       ;   //Whether to use 8x16 sprites
    private boolean ppuMasterMode         ;
    private boolean enableVBLANKnmi       ;
    // ---------------------------------------------

    //------------ PPUMASK Flags ---------------
    private boolean useGreyscale      ;
    private boolean disableLeftMargin ; //Enables rendering the 8 leftmost pixels of the screen
    private boolean disableRightMargin; //Enables rendering the 8 rightmost pixels of the screen
    private boolean renderBackground  ; //Whether to use 8x16 sprites
    private boolean renderSprites     ;
    private boolean emphasizeRed      ;
    private boolean emphasizeGreen    ;
    private boolean emphasizeBlue     ;
    // -----------------------------------------

    //---------- PPUSTATUS Flags -----------
    private boolean spriteOverflow;
    private boolean sprite0Hit    ;
    private boolean inVBlank      ;
    // -------------------------------------

    private int oamAddress;


    private byte[] palleteRam;
    private byte[] oam;
    private byte[] secondaryOAM;


    //Lowest 3 bits of the x scroll position
    private int fineX;

    //PPUDATA read buffer
    private byte readBuffer;

    private PPUBus ppuBus;
    public Palette palette;


    //Register (base) addresses
    public static final int PPUCTRL   = 0x2000;
    public static final int PPUMASK   = 0x2001;
    public static final int PPUSTATUS = 0x2002;
    public static final int OAMADDR   = 0x2003;
    public static final int OAMDATA   = 0x2004;
    public static final int PPUSCROLL = 0x2005;
    public static final int PPUADDR   = 0x2006;
    public static final int PPUDATA   = 0x2007;
    public static final int OAMDMA    = 0x4014;



    public PPU2(Nes nes) {
        //CPU can access PPU through memory mapped registers between 0x2000 and 0x2007,
        //which are then mirrored all the way until 0x3FFF
        super(0x2000, 0x3FFF);

        this.nes = nes;

        this.ppuBus = new PPUBus(new CIRAM(nes));
        this.palette = Palette.defaultPalette();

        reset(true);
    }

    // Gets the 4-Bit Index into palette RAM currently
    // selected by the rendering shift registers and scroll position
    private int getCurrentBackgroundPixel() {
        int pixel = 0;

        pixel |= patternRegisterHigh     >> (7 - fineX);
        pixel |= ((patternRegisterLow    >> (7 - fineX)) & 1) << 1;

        pixel |= ((attributeRegisterLow  >> (7 - fineX)) & 1) << 2;
        pixel |= ((attributeRegisterHigh >> (7 - fineX)) & 1) << 3;

        return pixel;
    }

    public void clock() {

    }



    /**
     * Reset the PPU
     * @param hard Whether to do a full reset. Some values remain
     *             unchanged during soft resets.
     */
    public void reset(boolean hard) {
        writePPUCTRL(0);
        writePPUMASK(0);

        // --- PPUSTATUS Flags --
        spriteOverflow = true; //TODO: This is "often" set (Randomize?)
        sprite0Hit     = hard ? false : sprite0Hit; //Remains unchanged at soft reset
        inVBlank       = true;
        // ----------------------


        writeToggle = false;

        tRegister  = hard ? 0 : tRegister;
        fineX = 0;
        oamAddress = hard ? 0 : oamAddress;

        readBuffer = 0;

        //oam can be random
        //Todo: make this more random? It should be "Unspecified"
        oam = new byte[64];
        vRegister = hard ? 0 : vRegister; //Unspecified if off

        //TODO: Determine startup values
        secondaryOAM = new byte[32];
        palleteRam = new byte[32];
    }







    public int ppuRead(int address) {
        address &= 0x3FFF;

        //Palette RAM does not use the bus
        if(address >= PALETTE_RAM_INDEX_START) {
            return Byte.toUnsignedInt(paletteRead(address));
        }

        return ppuBus.read(address);
    }


    public void ppuWrite(int address, int data) {
        address &= 0x3FFF;
        data &= 0xFF;

        //Palette RAM does not use the bus
        if(address >= PALETTE_RAM_INDEX_START) {
            paletteWrite(address, (byte) data);
            return;
        }

        ppuBus.write(address, data);
    }


    public byte paletteRead(int address) {
        //Mask out 5 bits for mirroring
        address &= 0x1F;

        //When two last bits are 0, disable bit A4 (To force Background Palette) since these are mirrors of those locations
        if((address & 0x3) == 0) {
            address &= 0xF;
        }

        //Todo: Upper two bits should be open bus
        return (byte) (palleteRam[address] & 0b0011_1111);
    }

    public void paletteWrite(int address, byte data) {
        address &= 0x1F;
        data &= 0b0011_1111; //Values are 6-Bit only

        //When two last bits are 0, disable bit A4 (To force Background Palette) since these are mirrors of those locations
        if((address & 0x3) == 0) {
            address &= 0xF;
        }
        palleteRam[address] = data;
    }




    @Override
    public BusValue read(int address) {
        address = address & 0b111;
        address += 0x2000;

        return switch(address) {
            case PPUSTATUS -> new BusValue( readPPUSTATUS() );
            case OAMDATA   -> new BusValue( readOAMDATA()   );
            case PPUDATA   -> new BusValue( readPPUDATA()   );
            case PPUCTRL, PPUMASK, OAMADDR, PPUSCROLL, PPUADDR -> new BusValue(readBuffer);
            default -> null;
        };
    }



    @Override
    public void write(int address, int data) {
        address = address & 0b111;
        address += 0x2000;

        switch(address) {
            case PPUCTRL:
                writePPUCTRL(data);
                break;
            case PPUMASK:
                writePPUMASK(data);
                break;
            case OAMADDR:
                writeOAMADDR(data);
                break;
            case OAMDATA:
                writeOAMDATA(data);
                break;
            case PPUSCROLL:
                writePPUSCROLL(data);
                break;
            case PPUADDR:
                writePPUADDR(data);
                break;
            case PPUDATA:
                writePPUDATA(data);
                break;
        }
    }


    private int readPPUSTATUS() {
        //TODO: Lower 5 bits should be "open bus"

        int v = inVBlank       ? 1:0;
        int s = sprite0Hit     ? 1:0;
        int o = spriteOverflow ? 1:0;

        return ((v << 2) | (s << 1) | o) << 5;
    }


    private void writePPUCTRL(int value) {
        // First two bits set the nametable address bits in t
        //t: ...GH.. ........ <- d: ......GH
        tRegister &= ~(0b11 << 10); // Clear the bits
        tRegister |=  (value & 0b11) << 10; // Set new value

        vramAddrIncreaseMode   = (value & (1 << 2)) > 0;
        spritePatternTable     = (value >> 3) & 1;
        backgroundPatternTable = (value >> 4) & 1;
        useLargeSprites        = (value & (1 << 5)) > 0;
        ppuMasterMode          = (value & (1 << 6)) > 0;
        enableVBLANKnmi        = (value & (1 << 7)) > 0;

        // TODO: "Changing NMI enable from 0 to 1 while the vblank flag in PPUSTATUS is 1 will
        // immediately trigger an NMI."
    }

    private void writePPUMASK(int value) {
        //TODO: Implement emphasis
        //https://www.nesdev.org/wiki/PPU_registers#PPUMASK:~:text=of%20writes.-,Color%20control,-Greyscale%20mode%20forces
        //TODO: After power/reset, writes to this register are ignored until the first pre-render scanline.
        useGreyscale       = (value & 1) > 0;
        disableLeftMargin  = (value & (1 << 1)) > 0;
        disableRightMargin = (value & (1 << 2)) > 0;
        renderBackground   = (value & (1 << 3)) > 0;
        renderSprites      = (value & (1 << 4)) > 0;
        emphasizeRed       = (value & (1 << 5)) > 0; //TODO SWAPPED ON PAL / DENDY
        emphasizeGreen     = (value & (1 << 6)) > 0;
        emphasizeBlue      = (value & (1 << 7)) > 0;
    }

    private void writeOAMADDR(int value) {
        oamAddress = value & 0xFF;
    }


    private int readOAMDATA() {
        //Todo: Implement these
        //Reading OAMDATA while the PPU is rendering will expose internal OAM accesses during sprite evaluation and loading; Micro Machines does this.
        //https://www.nesdev.org/wiki/PPU_registers#OAMDATA:~:text=of%20the%202C02.-,OAMDATA,-%2D%20Sprite%20RAM%20data

        return oam[oamAddress] & 0xFF;
    }
    private void writeOAMDATA(int value) {
        //Todo: Nicer OAM abstraction?
        oam[oamAddress] = (byte) value;

        //Writes incremend oamAddress;
        oamAddress += 1;
        oamAddress &= 0xFF;

        // TODO: Writes to OAMDATA during rendering (on the pre-render line and the visible lines 0–239, provided either sprite or
        // background rendering is enabled) do not modify values in OAM, but do perform a glitchy increment of OAMADDR, bumping
        // only the high 6 bits (i.e., it bumps the [n] value in PPU sprite evaluation – it's plausible that it could bump the
        // low bits instead depending on the current status of sprite evaluation). This extends to DMA transfers via OAMDMA,
        // since that uses writes to $2004. For emulation purposes, it is probably best to completely ignore writes during rendering.
    }

    private void writePPUSCROLL(int value) {
        if(!writeToggle) {
            setXScroll(value);
        } else {
            setYScroll(value);
        }

        writeToggle = !writeToggle;
    }

    private void writePPUADDR(int value) {
        if(!writeToggle) {
            value &= 0b111111;
            tRegister &= ~((0b111111) << 8);
            tRegister |= value << 8;

        } else {
            value &= 0xFF;
            tRegister &= ~0xFF;
            tRegister |= value;

            //Todo: Technically this is supposed to wait "1 to 1.5 dots" before copying the value
            vRegister = tRegister;
        }

        writeToggle = !writeToggle;
    }


    private int readPPUDATA() {
        return ppuRead(vRegister);
    }
    private void writePPUDATA(int data) {
        //TODO: implement this weirdness
        //https://www.nesdev.org/wiki/PPU_scrolling#$2000_(PPUCTRL)_write:~:text=t%3A%20%3C...all%20bits...%3E-,%242007%20(PPUDATA)%20reads%20and%20writes,-Outside%20of%20rendering

        ppuWrite(vRegister, data);
        vRegister += vramAddrIncreaseMode ? 32 : 1;
    }


    /**
     * Set the scroll value in t and fine X for the x-axis
     * @param scrollValue 8-Bit scroll value
     */
    void setXScroll(int scrollValue) {
        scrollValue &= 0xFF;

        //First five bits in t are x scroll (tile accurate)
        tRegister &= ~0b11111;
        tRegister |= scrollValue >> 3;
        //The last three bits (position in tile) is saved in an extra register
        fineX = scrollValue & 0b111;
    }

    /**
     * Set the scroll value in t for the y-axis
     * @param scrollValue 8-Bit scroll value
     */
    void setYScroll(int scrollValue) {
        scrollValue &= 0xFF;
        // t = yyyNNYYYYYXXXXX
        tRegister &= ~(0b1110011111 << 5);
        tRegister |= (scrollValue >> 3) << 5;
        tRegister |= (scrollValue & 0b111) << 12;
    }

}

