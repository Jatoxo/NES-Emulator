package main.nes.parsing;

import main.nes.Cartridge;

public abstract class RomFormat {

    public abstract Cartridge parseRom() throws UnsupportedRomException;

    public abstract int getMapperId();
}
