package emu.nes.mappers;

public class CNROM extends Mapper {
    //https://www.nesdev.org/wiki/CNROM

    //Always 32kb

    private byte[] prgRom;
    private byte[] chrRom;

    private MirrorMode mirrorMode;

    //CNROM has fixed PRGROM but switched CHROM in 8kb windows
    private int currentBank = 0;
    private int bankCount;

    private boolean usesChrRam;


    public CNROM(byte[] prgRom, byte[] chrRom, MirrorMode mirrorMode) {
        super(2);

        this.prgRom = prgRom;

        //If no CHR ROM is present, use 8kb CHR RAM
        this.usesChrRam = chrRom == null;
        if(usesChrRam) {
            this.chrRom = new byte[8192];
            this.bankCount = 1;
        } else {
            this.chrRom = chrRom;
            this.bankCount = chrRom.length / Mapper.SIZE_8KiB;
        }

        this.mirrorMode = mirrorMode;


    }

    @Override
    public int cpuRead(int address) {
        //Prg Rom starts at 0x8000
        if(address < 0x8000) {
            return -1;
        }

        return readBank(prgRom, SIZE_32KiB, 0, address);
    }



    @Override
    public void cpuWrite(int address, int data) {
        if(address < 0x8000) {
            return;
        }

        int bank;
        //Apparently oversize mapper 3s exist
        if(chrRom.length > SIZE_32KiB) {
            bank = data & 0b1111;
        } else {
            //Normally lower two bits select bank
            bank = data & 0b11;
        }

        currentBank = bank % bankCount;
    }

    @Override
    public int ppuRead(int address) {
        //CHR ROM starts at 0x0000, and is 8kb in size (0x0000 - 0x1FFF)
        //CHR ROM will be disabled when bit 13 of the address is set (CIRAM is enabled)
        return readBank(chrRom, SIZE_8KiB, currentBank, address);
    }

    @Override
    public void ppuWrite(int address, int data) {
        //System.out.printf("NROM: Write to CHR ROM at %s...\n", Integer.toHexString(address));
        if(usesChrRam) {
            writeBank(chrRom, SIZE_8KiB, currentBank, address, data);
        }
    }

    @Override
    public MirrorMode getMirrorMode(int address) {
        return mirrorMode;
    }
}
