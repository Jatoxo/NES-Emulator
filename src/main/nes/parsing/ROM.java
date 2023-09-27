package main.nes.parsing;

import main.nes.Cartridge;

public abstract class ROM {

    public abstract Cartridge parseRom() throws UnsupportedRomException;

    public abstract int getMapperId();

    public byte[] data;
}
