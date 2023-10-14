package main.nes.parsing;

import main.nes.Cartridge;

public abstract class ROM {

    private final String fileName;

    public ROM(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Get the name of the ROM file as it would appear in a file explorer
     * @return The name of the ROM file
     */
    public String getFileName() {
        return fileName;
    }

    public abstract Cartridge parseRom() throws UnsupportedRomException;

    public abstract int getMapperId();

    public abstract byte[] getProgramRom();

    public abstract byte[] getCharacterRom();

    public byte[] data;
}
