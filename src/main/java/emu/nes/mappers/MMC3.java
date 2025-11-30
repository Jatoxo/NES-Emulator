package emu.nes.mappers;

public class MMC3 extends Mapper {

    //Number of 8KiB banks of program rom
    int prgBankCount;
    //Number of 2KiB banks of chr rom
    int chrBankCount;

    //TODO: Add logic for when carts actually have this or not
    private final byte[] prgRam = new byte[8192];

    // We have 4 8KiB Windows between
    // 0x8000 and 0xFFFF;

    //0x8000-0x9FFF - Either swappable bank or second to last bank
    //0xA000-0xBFFF - Always swappable bank
    //0xC000-0xDFFF - Either swappable bank or second to last bank
    //0xE000-0xFFFF - Always fixed at last bank


    //Stores the 8 bank registers referred to as R0-R7 on nesdev
    //R6 can move between slot 0 and 2
    //R7 is always fixed at slot 1;
    int[] bankRegisters = new int[8];


    // The "PRG ROM bank mode",
    // If true, the bank in slot 2 is the swappable bank.
    // Otherwise, it is the second to last bank
    boolean slot2Swappable = false;

    boolean chr12Inversion = false;

    //TODO: figure out initial values
    boolean prgRamEnabled = true;
    boolean prgRamBlockWrites = false;

    boolean enableIRQ = false;
    int irqReloadValue = 0;
    boolean reloadIrq = false;


    int irqCounter = 0;



    //When writing to even adresses between $8000 - $9FFE,
    //The first three bits determine which slot the odd write
    //is going to be selecting the bank for
    //000: R0: Select 2 KB CHR bank at PPU $0000-$07FF (or $1000-$17FF)
    //001: R1: Select 2 KB CHR bank at PPU $0800-$0FFF (or $1800-$1FFF)
    //010: R2: Select 1 KB CHR bank at PPU $1000-$13FF (or $0000-$03FF)
    //011: R3: Select 1 KB CHR bank at PPU $1400-$17FF (or $0400-$07FF)
    //100: R4: Select 1 KB CHR bank at PPU $1800-$1BFF (or $0800-$0BFF)
    //101: R5: Select 1 KB CHR bank at PPU $1C00-$1FFF (or $0C00-$0FFF)
    //110: R6: Select 8 KB PRG ROM bank at $8000-$9FFF (or $C000-$DFFF)
    //111: R7: Select 8 KB PRG ROM bank at $A000-$BFFF
    int selectBank;



    public MMC3(byte[] programROM, byte[] chrMem) {
        super(programROM, chrMem, MirrorMode.HORIZONTAL, MMC3);

        prgBankCount = programROM.length / SIZE_8KiB;

        chrBankCount = chrMem == null ? 0 : chrMem.length / SIZE_2KiB;

        //Todo: Figure out reset values

    }


    //Returns the bank mapped to the specified 8KiB section between
    //0x8000 and 0xFFFF;
    public int getPrgBankAtSlot(int slot) {
        slot &= 0b11;
        return switch(slot) {
            case 0 -> slot2Swappable ? (prgBankCount - 2) % prgBankCount : bankRegisters[6];
            case 1 -> bankRegisters[7];
            case 2 -> slot2Swappable ? bankRegisters[6] : (prgBankCount - 2) % prgBankCount;
            case 3 -> prgBankCount - 1;
            default -> -1; //Impossible
        };

    }

    @Override
    public int cpuRead(int address) {

        if(address >= 0x6000 && address <= 0x7FFF) {
            if(!prgRamEnabled) {
                return -1;
            }
            return Byte.toUnsignedInt(prgRam[address & 0x1FFF]);
        }
        //CPU Address (16-BIT):
        //   CHHA_AAAA - AAAA_AAAA
        //PRG rom usually starts at C = 1 and everything else 0
        //HH selects and 8KB bank

        //So the current slot is encoded in HH

        int slot = (address >> 13) & 0b11;
        //System.out.println("Slot " + slot + ": " + Integer.toHexString(address));
        //System.out.println("mapped to bank " +getPrgBankAtSlot(slot));

        //Read from the bank currently mapped to that slot
        return readBank(programROM, SIZE_8KiB, getPrgBankAtSlot(slot), address);
    }

    @Override
    public void cpuWrite(int address, int data) {


        if(address >= 0x6000 && address <= 0x7FFF) {
            if(!prgRamEnabled || prgRamBlockWrites) {
                return;
            }

            prgRam[address & 0x1FFF] = (byte) data;
        }

        int bankReg = address & 0xE001;
        switch(bankReg) {
            case 0x8000: // even $8000-$9FFE bank select (writes mirror every 0x2000)
                // bank select
                //Even Write, Bank Slot Select
                selectBank = data & 0b111;

                slot2Swappable = ((data >> 6) & 1) > 0;
                chr12Inversion = ((data >> 7) & 1) > 0;
                break;
            case 0x8001: // odd  $8001 bank data
                // bank data
                if(selectBank == 6 || selectBank == 7) {
                    //R6 and R7 will ignore the top two bits, as the MMC3 has only 6 PRG ROM address lines
                    data &= 0b0011_1111;
                }
                if(selectBank == 0 || selectBank == 1) {
                    //R0 and R1 ignore the bottom bit, as the value written still counts banks in 1KB units
                    //but odd numbered banks can't be selected.
                    data &= ~1;
                }
                //System.out.println("Switching bank reg " + selectBank + " to " + data);

                bankRegisters[selectBank] = data;
                break;
            case 0xA000:
                // mirroring
                //TODO: Get mirror info from cart
                //https://www.nesdev.org/wiki/MMC3#:~:text=This%20bit%20has%20no%20effect%20on%20cartridges%20with%20hardwired%204%2Dscreen%20VRAM.%20In%20the%20iNES%20and%20NES%202.0%20formats%2C%20this%20can%20be%20identified%20through%20bit%203%20of%20byte%20%2406%20of%20the%20header.

                mirrorMode = (data & 1) == 0 ? MirrorMode.HORIZONTAL : MirrorMode.VERTICAL;
                break;
            case 0xA001:
                // prg ram protect
                prgRamBlockWrites = ((data >> 6) & 1) > 0;
                prgRamEnabled = ((data >> 7) & 1) > 0;
                break;
            case 0xC000:
                // irq latch
                irqReloadValue = data;
                break;
            case 0xC001:
                // irq reload
                //Writing any value to this register clears the MMC3 IRQ counter immediately, and then reloads it at the NEXT rising edge of the PPU address,
                //presumably at PPU cycle 260 of the current scanline.
                irqCounter = 0;
                reloadIrq = true;
                break;
            case 0xE000:
                // irq disable
                enableIRQ = false;
                break;
            case 0xE001:
                // irq enable
                enableIRQ = true;
                break;
        }
    }

    @Override
    public void scanlineTick() {
        if(irqCounter == 0 || reloadIrq) {
            nes.cpu.releaseIRQ();
            irqCounter = irqReloadValue;
            reloadIrq = false;
            return;
        }
        irqCounter--;

        if(irqCounter == 0 && enableIRQ) {
            //System.out.println("MMC3: IRQ raised");
            nes.cpu.raiseIRQ();
        }
    }

    // Maps the given ppu address to the currently mapped chrRom address
    private int mapChrRom(int address) {
        int slot = (address >> 10) &  0b111;

        //A12 is the highest bit (after A13 which is 1 when we're in Pattern memory usually)
        boolean A12 = (slot >> 2) > 0;

        if(chr12Inversion) {
            A12 = !A12;
        }

        int register;
        int effectiveAddress;
        if(!A12) {
            //When A12 is not set, the lower registers are selected
            //A11 tells us the register to use
            register = (slot >> 1) & 1;
            int bank = bankRegisters[register] & ~1;
            //bank %= chrBankCount;

            //The final address is Bits A0-A10 from the address with A11-... being the selected bank
            effectiveAddress = (bank << 10) | (address & (SIZE_2KiB -1));
        } else {
            //When A12 is set, the register to use is (A12-A10) + 2

            register = 2 + (slot & 0b11);

            int bank = bankRegisters[register];
            //bank %= chrBankCount;

            effectiveAddress = (bank << 10) | (address & (SIZE_1KiB -1));
        }

        return effectiveAddress;
    }




    @Override
    public int ppuRead(int address) {

        return chrMem[mapChrRom(address)];
    }

    @Override
    public void ppuWrite(int address, int data) {
        if(!usesChrRam) {
            System.out.printf("Trying to write to CHR ROM at %s...\n", Integer.toHexString(address));
            return;
        }

        chrMem[mapChrRom(address)] = (byte) data;
    }

    @Override
    public boolean isChrRomEnabled(int address) {
        //We're counting scanlines here
        //Todo: Count the scanlines here somehow.
        // good luck
        return true;
    }

    public byte[] getProgramRAM() {
        //By default, return empty array since not all boards have prg ram
        return programROM;
    }

    public void setProgramRAM(byte[] newProgramRam) {
        if(prgRam.length != newProgramRam.length) {
            System.out.println("MMC3: Program ram could not be set because length does not match");
            return;
        }

        System.arraycopy(prgRam, 0, prgRam, 0, prgRam.length);
    }
}
