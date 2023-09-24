package main.nes.parsing;

import main.nes.Cartridge;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;


//Responsible for applying the correct parser for any given ROM file
public class RomParser {

    /**
     * Parses a ROM file and returns a Cartridge object
     * @param romPath The path to the ROM file
     * @return The Cartridge object
     */
    public static Cartridge parseRom(String romPath) throws IOException, UnsupportedRomException {

        //Load the ROM file into a byte array
        //Rom files are usually very small (<1mb) so this is probably fine
        byte[] romFile = Files.readAllBytes(Path.of(romPath));

        //Determine the correct parser to use
        RomFormat rom = getParser(romFile);

        //If no parser is found, throw an exception
        if(rom == null) {
            throw new UnsupportedRomException("No parser found for ROM file");
        }

        //Parse the ROM file
        return rom.parseRom();
    }

    /**
     * Determines the correct parser to use for a given ROM file
     * @param romFile The bytes of the ROM file
     * @return The correct Parser, or null if no parser is found
     */
    static RomFormat getParser(byte[] romFile) {

        //Check for iNES file format (Beginning with "NES" followed by 0x1A)
        if(romFile[0] == 0x4E && romFile[1] == 0x45 && romFile[2] == 0x53 && romFile[3] == 0x1A) {
            return new INESRom(romFile);
        } else {

            return null;
        }
    }

}
