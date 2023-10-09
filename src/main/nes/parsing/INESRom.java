package main.nes.parsing;

import main.nes.Cartridge;
import main.nes.mappers.MMC1;
import main.nes.mappers.Mapper;
import main.nes.mappers.NROM;

public class INESRom extends ROM {

    public enum iNESVersion {
        ARCHAIC_iNES,
        iNES0_7,
        iNES,
        iNES2_0
    }

    public iNESHeader header;
    public iNESVersion version;



    //Referenced from https://www.nesdev.org/wiki/INES
    public class iNESHeader {
        public byte pgrRomChunks; //Count of 16kb chunks of PRG ROM
        public byte chrRomChunks; //Count of 8kb chunks of CHR ROM
        public byte flags6;  //Mapper, mirroring, battery, trainer
        public byte flags7;  //Mapper, VS/Playchoice, NES 2.0
        public byte flags8;  //PRG-RAM size (rarely used extension)
        public byte flags9;  //TV system (rarely used extension)
        public byte flags10; //TV system, PRG-RAM presence (unofficial, rarely used extension)
        public int  padding;  //Unused padding (should be filled with zero, but some rippers put their name across bytes 7-15)

        public iNESHeader(byte[] romFile) {
            pgrRomChunks = romFile[4];
            chrRomChunks = romFile[5];
            flags6 = romFile[6];
            flags7 = romFile[7];
            flags8 = romFile[8];
            flags9 = romFile[9];
            flags10 = romFile[10];
            padding = romFile[11] << 24 | romFile[12] << 16 | romFile[13] << 8 | romFile[14];

            //If these bytes aren't all empty there might be some garbage spilled into flag 7
            //due to ancient rom files not using that part
            int adDetection = romFile[12] | romFile[13] | romFile[14] | romFile[15];
            if(adDetection > 0 && version != iNESVersion.iNES2_0) {
                //There is probably some garbage spelled across the flags here
                flags7 &= 0x00;
            }

            //Some roms have 0 for the CHR ROM size, in this case one 8kb chunk is assumed
            if(chrRomChunks == 0) {
                chrRomChunks = 1;
            }
        }

        public boolean hasTrainer() {
            return (flags6 & 0x04) > 0;
        }

        public boolean hasPersistentMemory() {
            return (flags6 & 0x02) > 0;
        }

        public Mapper.MirrorMode getMirrorMode() {
            return (flags6 & 1) > 0 ? Mapper.MirrorMode.VERTICAL : Mapper.MirrorMode.HORIZONTAL;
        }
    }

    public INESRom(byte[] romFile) {
        data = romFile;
        header = new iNESHeader(romFile);

        //TODO: Detect the version better as specified at https://www.nesdev.org/wiki/INES#Flags_7:~:text=Recommended%20detection%20procedure,or%20archaic%20iNES.
        //Determine the iNES version
        if((header.flags7 & 0x0C ) == 0x08) {
            version = iNESVersion.iNES2_0;
        } else {
            version = iNESVersion.iNES;
        }


    }

    public Cartridge parseRom() throws UnsupportedRomException {
        int mapperId = getMapperId();

        Mapper mapper;

        switch(mapperId) {
            case Mapper.NROM:
                int prgRomSize = header.pgrRomChunks * 16384;

                byte[] programRom = new byte[prgRomSize];

                int prgRomOffset = 16 + (header.hasTrainer() ? 512 : 0);
                System.arraycopy(data, prgRomOffset, programRom, 0, programRom.length);

                byte[] chrRom = new byte[header.chrRomChunks * 8192];
                int chrRomOffset = prgRomOffset + programRom.length;

                //Some roms don't have CHR ROM (instead they choose to write to it later),
                // so we need to make sure we don't go out of bounds
                //TODO: Some roms have some playchoice data at the end, so probably should just not attempt to read any CHR ROM if it reports 0 chunks
                System.arraycopy(data, chrRomOffset , chrRom, 0, Math.min(chrRom.length, data.length - chrRomOffset));


                mapper = new NROM(programRom, chrRom, header.getMirrorMode());
                break;
            case Mapper.MMC1:
                int prgRomSizeMMC1 = header.pgrRomChunks * 16384;
                byte[] programRomMMC1 = new byte[prgRomSizeMMC1];

                int prgRomOffsetMMC1 = 16 + (header.hasTrainer() ? 512 : 0);
                System.arraycopy(data, prgRomOffsetMMC1, programRomMMC1, 0, programRomMMC1.length);

                byte[] chrRomMMC1 = new byte[header.chrRomChunks * 8192];
                int chrRomOffsetMMC1 = prgRomOffsetMMC1 + programRomMMC1.length;

                System.arraycopy(data, chrRomOffsetMMC1 , chrRomMMC1, 0, Math.min(chrRomMMC1.length, data.length - chrRomOffsetMMC1));

                mapper = new MMC1(programRomMMC1, header.pgrRomChunks, chrRomMMC1, header.chrRomChunks);
                break;

            default:
                throw new UnsupportedRomException("Mapper " + mapperId + " is not supported");
        }

        return new Cartridge(
                header.pgrRomChunks,
                header.chrRomChunks,
                mapper,
                header.hasPersistentMemory(),
                header.hasTrainer()
        );
    }

    public int getMapperId() {
        //Determine the mapper ID
        //The higher nybble of flags 6 is the lower nybble of the mapper ID
        //The higher nybble of flags 7 is the higher nybble of the mapper ID
        return ((header.flags6 & 0xF0) >>> 4) | header.flags7 & 0xF0;

        //int mapperId = (flags6 & mapIdLo) | ((flags7 & mapIdHi) << 4);
    }
}
