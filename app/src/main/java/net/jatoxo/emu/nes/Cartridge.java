package net.jatoxo.emu.nes;

import emu.RetainedStorage;
import net.jatoxo.emu.nes.mappers.Mapper;
import net.jatoxo.emu.nes.ppu.PPUBusDevice;

import java.io.IOException;


public class Cartridge extends BusDevice implements PPUBusDevice {
	RetainedStorage retainedStorage;

	//Mapper used by this cartridge
	public final Mapper mapper;

	//Whether the cartridge contains battery-backed PRG RAM ($6000-7FFF) or other persistent memory
	private final boolean batteryBackedRam;


	/**
	 * Creates a new Cartridge object
	 * @param mapper Mapper ID used by this cartridge
	 * @param batteryBackedRam Whether the cartridge contains battery-backed PRG RAM ($6000-7FFF) or other persistent memory
	 */
	public Cartridge(
			RetainedStorage retainedStorage,
			Mapper mapper,
			boolean batteryBackedRam
	) {
		//Cartridge space is 0x4020 - 0xFFFF on the CPU bus
		super(0x4020, 0xFFFF);
		//Todo: Maybe Cartridge shouldn't be a bus device, rather just the individual memory chips / mappers?
		this.retainedStorage = retainedStorage;
		this.mapper = mapper;
		this.batteryBackedRam = batteryBackedRam;

		loadPersistentData();
	}

    public void nesConnected(Nes nes) {
        if(mapper != null) {
            mapper.nesConnected(nes);
        }

    }

	/**
	 * Returns whether the cartridge has battery-backed PRG RAM ($6000-7FFF) or other persistent memory
	 */
	public boolean hasBatteryBackedRam() {
		return batteryBackedRam;
	}


	public Mapper.MirrorMode getMirrorMode() {
		return mapper.getMirrorMode();
	}

	//whether the CIRAM is enabled when accessing this address
	public boolean isCIRAMEnabled(int address) {
		return mapper.isCIRAMEnabled(address);
	}

	@Override
	public BusValue read(int addr) {
		int value = mapper.cpuRead(addr & 0xFFFF);
		return value == -1 ? null : new BusValue(value);
	}

	@Override
	public void write(int addr, int data) {
		mapper.cpuWrite(addr & 0xFFFF, data);
	}

	@Override
	public int ppuRead(int addr) {
		addr &= 0x3FFF;
		return mapper.ppuRead(addr);
	}
	@Override
	public void ppuWrite(int addr, int data) {
		addr &= 0x3FFF;
		data &= 0xFF;
		mapper.ppuWrite(addr, data);
	}

	@Override
	public boolean isEnabled(int addr) {
		return mapper.isChrRomEnabled(addr);
	}


	@Override
	public int getAddrStart() {
		return 0x0;
	}
	@Override
	public int getAddrEnd() {
		return 0x3FFF;
	}

	/**
	 * Some cartridges have battery backed memory that will remain after the game is turned off.
	 * This will save that memory to storage, so it can be restored the next time the same game is loaded.
	 */
	public void storePersistentData() throws IOException {
		//The Mapper isn't aware whether the PRG RAM is persistent. So only actually save it
		//if the ROM file indicates that the cartridge contains persistent memory
		if(!hasBatteryBackedRam() || retainedStorage == null) {
			return;
		}

		byte[] programRam = mapper.getProgramRAM();

        retainedStorage.persist(programRam);
	}

	public void loadPersistentData() {
		if(!hasBatteryBackedRam() || retainedStorage == null) {
			return;
		}
        byte[] data = retainedStorage.restore();
        if(data == null) {
            return;
        }

        mapper.setProgramRAM(retainedStorage.restore());
	}


}
