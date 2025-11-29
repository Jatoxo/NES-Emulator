package emu.nes.mappers;

public class UxROM extends Mapper {
    //https://www.nesdev.org/wiki/UxROM

    private final int bankCount;

    private int currentBank = 0;

    public UxROM(byte[] prgRom, byte[] chrRom, MirrorMode mirrorMode) {
        super(prgRom, chrRom, mirrorMode, 2);

        //16kb chunks
        this.bankCount = prgRom.length / Mapper.SIZE_16KiB;
        this.mirrorMode = mirrorMode;
    }

    @Override
    public int cpuRead(int address) {
        //Prg Rom starts at 0x8000
        if(address < 0x8000) {
            return -1;
        }

        int bank = address > 0xBFFF ? bankCount -1 : currentBank;
        return readBank(programROM, SIZE_16KiB, bank, address);
    }



    @Override
    public void cpuWrite(int address, int data) {
        if(address > 0x8000) {
            //Supposedly it's fine to treat this as full range bank select
            currentBank = data % bankCount;
        }
    }

    @Override
    public int ppuRead(int address) {
        //CHR ROM starts at 0x0000, and is 8kb in size (0x0000 - 0x1FFF)
        address &= 0x1FFF;
        //CHR ROM will be disabled when bit 13 of the address is set (CIRAM is enabled)
        return chrMem[address] & 0xFF;
    }

    @Override
    public void ppuWrite(int address, int data) {
        //System.out.printf("NROM: Write to CHR ROM at %s...\n", Integer.toHexString(address));
        if(usesChrRam) {
            address &= 0x1FFF;
            chrMem[address] = (byte) data;
        }
    }
}
