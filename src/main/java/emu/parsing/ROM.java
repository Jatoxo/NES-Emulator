package emu.parsing;

import emu.nes.Cartridge;
import emu.FilePersistence;
import emu.nes.mappers.*;

public abstract class ROM {
    public enum MirrorMode {
        MAPPER_CONTROLLED,
        HORIZONTAL,
        VERTICAL
    }

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


    /**
     * Return the PRG ROM or null if none is present
     */
    public abstract byte[] getProgramRom();

    /**
     * Return the CHR ROM or null if none is present
     */
    public abstract byte[] getCharacterRom();


    /**
     * Get the iNES Mapper Id;
     */
    public abstract int getMapperId();
    //Todo: This is specific to iNES? maybe do some thonks if this should be separate or somethin
    // Turns out there ARE other formats. TNES is nintendo's file format

    /**
     * Get the MirrorMode that is hardwired by the cartridge. Not meaningful if the cartridge changes
     * the mode on the fly
     */
    public abstract MirrorMode getMirrorMode();

    public abstract boolean hasPersistentMemory();

    public byte[] data;


    public Cartridge parseRom() throws UnsupportedRomException {
        int mapperId = getMapperId();

        Mapper.MirrorMode mapperMirrorMode;
        if(getMirrorMode() == MirrorMode.VERTICAL) {
            mapperMirrorMode = Mapper.MirrorMode.VERTICAL;
        } else {
            mapperMirrorMode = Mapper.MirrorMode.HORIZONTAL;
        }

        Mapper mapper = switch (mapperId) {
            case Mapper.NROM  -> new NROM(getProgramRom(), getCharacterRom(), mapperMirrorMode);
            case Mapper.MMC1  -> new MMC1(getProgramRom(), getCharacterRom());
            case Mapper.AXROM -> new AxROM(getProgramRom());
            case Mapper.UXROM -> new UxROM(getProgramRom(), getCharacterRom(), mapperMirrorMode);
            case Mapper.CNROM -> new CNROM(getProgramRom(), getCharacterRom(), mapperMirrorMode);
            case Mapper.MMC3  -> new MMC3(getProgramRom(), getCharacterRom());
            default -> throw new UnsupportedRomException("Mapper " + mapperId + " is not supported");
        };

        return new Cartridge(
                new FilePersistence(getFileName()),
                mapper,
                hasPersistentMemory()
        );
    }
}
