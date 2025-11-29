package emu.nes.mappers;


public class MMC1 extends Mapper {
    //https://www.nesdev.org/wiki/MMC1

    //TODO: The CHR rom bank select shenanigans
    //https://www.nesdev.org/wiki/MMC1#iNES_Mapper_001:~:text=use%20this%20region.-,iNES%20Mapper%20001,-iNES%20Mapper%20001


    //8kb of PRG RAM
    //Todo: Different MMC1 boards have different amounts of PRG RAM
    // Some are battery backed, some aren't. And some don't have any at all
    // If the iNES header doesn't specify any PRG RAM, there is no way to know
    // (Just provide 8kb of writable memory for now)
    private final byte[] prgRam = new byte[8192];

    //MMC1 uses a shift register as an interface to set the internal registers
    //It starts out with bit 4 set, and the other bits cleared
    private byte shiftRegister = 0b10000;


    //0,1: switch 32 KB at $8000, ignoring low bit of bank number
    //  2: fix first bank at $8000 and switch 16 KB bank at $C000
    //  3: fix last bank at $C000 and switch 16 KB bank at $8000
    private byte programRomBankMode = 3;

    //CHR ROM bank mode
    // 0 : switch 8 KB at a time;
    // 1 : switch two separate 4 KB banks
    private boolean characterRom4kbMode = false;

    private final int prgBankCount;
    private final int chrBankCount;


    //Select 4 KB or 8 KB CHR bank at PPU $0000 (low bit ignored in 8 KB mode)
    private byte characterRomBank0 = 0;

    //Select 4 KB CHR bank at PPU $1000 (ignored in 8 KB mode)
    private byte characterRomBank1 = 0;

    //Select 16 KB PRG ROM bank (low bit ignored in 32 KB mode)
    private byte programRomBank = 0;

    MirrorMode[] mirrorModesById = {MirrorMode.ONE_SCREEN_FIRST, MirrorMode.ONE_SCREEN_SECOND, MirrorMode.VERTICAL, MirrorMode.HORIZONTAL};


    public MMC1(byte[] prgRom, byte[] chrRom) {
        super(prgRom, chrRom, MirrorMode.ONE_SCREEN_FIRST, MMC1);

        //16kb chunks
        prgBankCount = prgRom.length / Mapper.SIZE_16KiB;

        chrBankCount = chrRom == null ? 0 : chrRom.length / SIZE_4KiB;
    }

    /**
     * Reset the mapper
     * Writing to any ROM address with bit 7 set will reset the mapper
     */
    public void reset() {
        shiftRegister = 0b10000;
        programRomBankMode = 3;
    }

    @Override
    public int cpuRead(int address) {
        if(address >= 0x6000 && address <= 0x7FFF) {
            //System.out.println("Read from Cartridge");

            return Byte.toUnsignedInt(prgRam[address & 0x1FFF]);

        } else if(address >= 0x8000 && address <= 0xBFFF) {
            //First bank of program rom

            //Mask to 16kb address
            int bankAddress = address & 0x3FFF;

            switch(programRomBankMode) {
                case 0:
                case 1:
                    //32kb chunk
                    //Bank is 4-Bit value. Lower bit is ignored in this 32kb chunk mode
                    byte bankSelect = (byte) (getCurrentProgramBank() & 0b1110);

                    return readBank(programROM, SIZE_16KiB, bankSelect, bankAddress);
                    //return Byte.toUnsignedInt(prgRom[bankSelect][bankAddress]);
                case 2:
                    //This bank is fixed to the first bank in this mode
                    return readBank(programROM, SIZE_16KiB, 0, bankAddress);
                    //return Byte.toUnsignedInt(prgRom[0][bankAddress]);
                case 3:
                    //Bank is switched to the one selected by control register in this mode
                    return readBank(programROM, SIZE_16KiB, getCurrentProgramBank(), bankAddress);
            }


        } else if(address >= 0xC000 && address <= 0xFFFF) {
            //Second bank of program rom

            //Mask to 16kb address
            int bankAddress = address & 0x3FFF;

            switch (programRomBankMode) {
                case 0:
                case 1:
                    //32kb chunk
                    //Bank is 4-Bit value. Lower bit is ignored in this 32kb chunk mode
                    //Adding one since this is the second bank of the two 16kb chunks
                    byte bankSelect = (byte) ((getCurrentProgramBank() & 0b1110) + 1);

                    return readBank(programROM, SIZE_16KiB, bankSelect, bankAddress);
                    //return Byte.toUnsignedInt(prgRom[bankSelect][bankAddress]);
                case 2:
                    //Bank is switched to the one selected by control register in this mode
                    //return Byte.toUnsignedInt(prgRom[getCurrentProgramBank()][bankAddress]);
                    return readBank(programROM, SIZE_16KiB, getCurrentProgramBank(), bankAddress);
                case 3:
                    //This bank is fixed to the last bank in this mode
                    return readBank(programROM, SIZE_16KiB, prgBankCount - 1, bankAddress);
                    //return Byte.toUnsignedInt(prgRom[prgRom.length -1][bankAddress]);
            }

        }


        return -1;
    }

    @Override
    public void cpuWrite(int address, int data) {
        if(address >= 0x6000 && address <= 0x7FFF) {
            //TODO: Implement PRG RAM sizes
            //System.out.println("MMC1: Write to PRG RAM");
            prgRam[address & 0x1FFF] = (byte) data;

        } else
        //Writes to this address range interface (PRG ROM) with the shift register
        if(address >= 0x8000 && address <= 0xFFFF) {

            //Writing any value with a set bit 7 sets the shiftRegister back to its initial state
            if((data >>> 7) > 0) {
                reset();

                return;
            }

            //If bit 0 of the shift register is a 1, it's full (It was shifted four times, as bit 5 is always set)
            //Another write will copy combine d0 with the bits from the shift register (excluding the 1) into a 5 bit value
            if((shiftRegister & 1) > 0) {

                //Shift away the 1, leaving a 4-Bit value
                byte value = (byte) (shiftRegister >>> 1);

                //Put bit 0 from data into the 5th spot (bit 4)
                value |= (byte) ((data & 1) << 4);

                //The value is written into a mapper register selected by the address the write was done to
                //Bit 13 and 14 select which register is to be written to
                //Mask out bits 13 and 14
                byte registerSelect = (byte) ((address >>> 13) & 0b11);

                registerWrite(registerSelect, value);

                shiftRegister = 0b10000;

                return;
            }

            //Right shift the register by one to make space for the new bit
            shiftRegister >>>= 1;

            //Insert bit 0 from the written byte into the vacant spot (bit 4)
            shiftRegister |= (byte) ((data & 1) << 4);

            //ROMs will usually write the desired value into a CPU register, then repeatedly write to the cartridge
            //and right shift the CPU register.

            //TODO: Implement consecutive-cycle write behavior
            //https://www.nesdev.org/wiki/MMC1#Banks:~:text=inc%20resetMapper%0A%20%20rts-,Consecutive%2Dcycle%20writes,-When%20the%20serial

        }

    }

    private byte getCurrentProgramBank() {
        return (byte) (programRomBank % prgBankCount);
    }



    /**
     * Write to a mapper register
     * @param register 2-bit register select
     * @param data 5-bit data to write to the register
     */
    public void registerWrite(byte register, byte data) {
        if(register == 0b00) {
            //Write to control register

            //First two bits are the mirroring mode
            byte mirror = (byte) (data & 0b11);
            MirrorMode newMode = mirrorModesById[mirror % MirrorMode.values().length];

            if(newMode != mirrorMode) {
                System.out.println("> MMC1: Mirror mode changed to " + newMode);
            }

            mirrorMode = newMode;

            data >>>= 2;
            //Bit 2-3 are the PRG ROM bank mode
            programRomBankMode = (byte) (data & 0b11);

            data >>>= 2;
            //Bit 4 selects 4kb or 8kb CHR ROM banks
            characterRom4kbMode = (data & 1) > 0;

        } else if(register == 0b01) {
            //Select Character ROM bank 0
            characterRomBank0 = (byte) (data & 0b11111);

        } else if(register == 0b10) {
            //Select Character ROM bank 1 (only used in 4kb mode)
            characterRomBank1 = (byte) (data & 0b11111);

        } else if(register == 0b11) {
            //Select PRG ROM bank
            programRomBank = (byte) (data & 0b1111);

            //TODO: Implement bit 4
            //https://www.nesdev.org/wiki/MMC1#iNES_Mapper_001:~:text=8%20KB%20mode)-,PRG%20bank,-(internal%2C%20%24E000%2D%24FFFF
        }

    }


    @Override
    public int ppuRead(int address) {
        //CHR ROM starts at 0x0000, and is 8kb in size (0x0000 - 0x1FFF)
        //CHR ROM will be disabled when bit 13 of the address is set (CIRAM is enabled)

        if(!characterRom4kbMode) {
            //Bit 0 is ignored in 8KB banking mode
            int selectedBank = characterRomBank0 & ~1;

            return readBank(chrMem, SIZE_8KiB, selectedBank, address);

        } else {
            boolean secondBank = (address & 0x1FFF) > 0xFFF;

            int selectedBank = secondBank ? characterRomBank1 : characterRomBank0;

            return readBank(chrMem, SIZE_4KiB, selectedBank, address);
        }
    }

    @Override
    public void ppuWrite(int address, int data) {
        if(!usesChrRam) {
            System.out.printf("Trying to write to CHR ROM at %s...\n", Integer.toHexString(address));
            return;
        }

        if(!characterRom4kbMode) {
            //Bit 0 is ignored in 8KB banking mode
            int selectedBank = characterRomBank0 & ~1;

            writeBank(chrMem, SIZE_8KiB, selectedBank, address, data);
        } else {
            boolean secondBank = (address & 0x1FFF) > 0xFFF;

            int selectedBank = secondBank ? characterRomBank1 : characterRomBank0;

            writeBank(chrMem, SIZE_4KiB, selectedBank, address, data);
        }
    }


    @Override
    public byte[] getProgramRAM() {
        return prgRam;
    }

    @Override
    public void setProgramRAM(byte[] programRAM) {
        if(programRAM.length != prgRam.length) {
            System.out.println("MMC1: Program ram could not be set because length does not match");
            return;
        }

        System.arraycopy(programRAM, 0, prgRam, 0, prgRam.length);
    }
}
