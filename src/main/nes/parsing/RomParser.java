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

        File romFile = new File(romPath);

        //Load the ROM file into a byte array
        //Rom files are usually very small (<1mb) so this is probably fine
        byte[] romData = Files.readAllBytes(romFile.toPath());

        //Determine the file format
        ROM rom = getParser(romData, romFile.getName());

        //If the file format is unknown, throw an exception
        if(rom == null) {
            throw new UnsupportedRomException("Unknown file format or invalid header");
        }

        //Parse the ROM file
        return rom.parseRom();
    }

    /**
     * Determines the correct parser to use for a given ROM file
     * @param romFile The bytes of the ROM file
     * @return The correct Parser, or null if no parser is found
     */
    static ROM getParser(byte[] romFile, String fileName) {

        //Check for iNES file format (Beginning with "NES" followed by 0x1A)
        if(romFile[0] == 0x4E && romFile[1] == 0x45 && romFile[2] == 0x53 && romFile[3] == 0x1A) {
            return new INESRom(romFile, fileName);
        } else {
            return null;
        }
    }

}
