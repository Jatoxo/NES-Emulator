package emu.nes.mappers;

public class CNROM extends Mapper {
    //https://www.nesdev.org/wiki/CNROM

    //Program ROM is always 32kb

    //CNROM has fixed PRG ROM but switched CHR ROM in 8kb windows
    private int currentBank = 0;

    private final int chrBankCount;

    public CNROM(byte[] prgRom, byte[] chrRom, MirrorMode mirrorMode) {
        super(prgRom, chrRom, mirrorMode, 2);

        chrBankCount = chrRom.length / SIZE_8KiB;
    }

    @Override
    public int cpuRead(int address) {
        //Prg Rom starts at 0x8000
        if(address < 0x8000) {
            return -1;
        }

        return readBank(programROM, SIZE_32KiB, 0, address);
    }


    @Override
    public void cpuWrite(int address, int data) {
        if(address < 0x8000) {
            return;
        }

        int bank;

        //Apparently oversize mapper 3s exist
        if(chrMem.length > SIZE_32KiB) {
            bank = data & 0b1111;
        } else {
            //Normally lower two bits select bank
            bank = data & 0b11;
        }

        currentBank = bank % chrBankCount;
    }

    @Override
    public int ppuRead(int address) {
        //CHR ROM starts at 0x0000, and is 8kb in size (0x0000 - 0x1FFF)
        //CHR ROM will be disabled when bit 13 of the address is set (CIRAM is enabled)
        return readBank(chrMem, SIZE_8KiB, currentBank, address);
    }

    @Override
    public void ppuWrite(int address, int data) {
        //System.out.printf("NROM: Write to CHR ROM at %s...\n", Integer.toHexString(address));
        if(usesChrRam) {
            writeBank(chrMem, SIZE_8KiB, currentBank, address, data);
        }
    }
}
