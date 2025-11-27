package nes;

import nes.mappers.Mapper;
import nes.parsing.ROM;

public class DummyCartridge extends Cartridge {

    public DummyCartridge() {
        super(null, null, false);
    }

    @Override
    public Mapper.MirrorMode getMirrorMode(int address) {
        return Mapper.MirrorMode.ONE_SCREEN_FIRST;
    }

    @Override
    public boolean isCIRAMEnabled(int address) {
        return true;
    }

    @Override
    public BusValue read(int addr) {
        return null;
    }

    @Override
    public void write(int addr, int data) {}

    @Override
    public int ppuRead(int addr) {
        return 0;
    }

    @Override
    public void ppuWrite(int addr, int data) {}

    @Override
    public boolean isEnabled(int addr) {
        return false;
    }
}
