package net.jatoxo.emu.nes.mappers;

public class AxROM extends Mapper {

    private byte selectedBank = 0;

    public AxROM(byte[] programRom) {
        super(programRom, null, MirrorMode.ONE_SCREEN_FIRST, AXROM);
    }

    @Override
    public int cpuRead(int address) {
        if(address >= 0x8000 && address <= 0xFFFF) {
            return readBank(programROM, SIZE_32KiB, selectedBank, address);
        }

        return -1;
    }

    @Override
    public void cpuWrite(int address, int data) {
        if(address >= 0x8000 && address <= 0xFFFF) {
            //Writes to this range configure the bank

            //First 3 bits are the bank number
            selectedBank = (byte) (data & 0b111);

            //Lowest bit of high nybble is the mirroring mode
            mirrorMode = (data & 0b1000) == 0 ? MirrorMode.ONE_SCREEN_FIRST : MirrorMode.ONE_SCREEN_SECOND;
        }
    }

    @Override
    public int ppuRead(int address) {
        return Byte.toUnsignedInt(chrMem[address & (SIZE_8KiB - 1)]);
    }

    @Override
    public void ppuWrite(int address, int data) {
        chrMem[address & (SIZE_8KiB - 1)] = (byte) data;
    }
}
