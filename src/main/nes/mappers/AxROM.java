package main.nes.mappers;

public class AxROM extends Mapper {
    //16kb or 32kb of PRG ROM
    private final byte[] programRom;

    //AxROM does not have CHR ROM, instead it has CHR RAM
    private final byte[] chrRam = new byte[8192];

    private byte selectedBank = 0;

    MirrorMode mirrorMode = MirrorMode.ONE_SCREEN_FIRST;

    public AxROM(byte[] programRom) {
        super(Mapper.AXROM);

        this.programRom = programRom;
    }

    @Override
    public int cpuRead(int address) {
        if(address >= 0x8000 && address <= 0xFFFF) {
            int bankAddress = address & 0x7FFF;
            address = selectedBank << 15 | bankAddress;

            return programRom[address % programRom.length];
        }

        return 0;
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
        return chrRam[address];
    }

    @Override
    public void ppuWrite(int address, int data) {
        chrRam[address] = (byte) data;
    }

    @Override
    public MirrorMode getMirrorMode(int address) {
        return mirrorMode;
    }


}
