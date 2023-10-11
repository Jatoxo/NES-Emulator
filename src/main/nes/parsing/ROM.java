package main.nes.parsing;

import main.nes.Cartridge;

public abstract class ROM {

    public abstract Cartridge parseRom() throws UnsupportedRomException;

    public abstract int getMapperId();

    public abstract byte[] getProgramRom();

    public abstract byte[] getCharacterRom();

    public byte[] data;
}
