package main.nes;

import main.nes.mappers.Mapper;


public class Cartridge extends BusDevice implements PPUBusDevice{
	//Count of 16kb chunks of PRG ROM
	int pgrRomChunks;

	//Count of 8kb chunks of CHR ROM
	int chrRomChunks;

	//Mapper used by this cartridge
	private final Mapper mapper;

	//Whether the cartridge contains battery-backed PRG RAM ($6000-7FFF) or other persistent memory
	private final boolean batteryBackedRam;

	//Whether the cartridge contains a 512-byte trainer at $7000-$71FF (stored before PRG data)
	private final boolean trainer;

	/**
	 * Creates a new Cartridge object
	 * @param pgrRomChunks Count of 16kb chunks of PRG ROM
	 * @param chrRomChunks Count of 8kb chunks of CHR ROM
	 * @param mapper Mapper ID used by this cartridge
	 * @param batteryBackedRam Whether the cartridge contains battery-backed PRG RAM ($6000-7FFF) or other persistent memory
	 * @param trainer Whether the cartridge contains a 512-byte trainer at $7000-$71FF (stored before PRG data)
	 */
	public Cartridge(
			int pgrRomChunks,
			int chrRomChunks,
			Mapper mapper,
			boolean batteryBackedRam,
			boolean trainer
	) {
		//Cartridge space is 0x4020 - 0xFFFF on the CPU bus
		super(0x4020, 0xFFFF);

		this.pgrRomChunks = pgrRomChunks;
		this.chrRomChunks = chrRomChunks;
		this.mapper = mapper;
		this.batteryBackedRam = batteryBackedRam;
		this.trainer = trainer;
	}


	/**
	 * Returns whether the cartridge has battery-backed PRG RAM ($6000-7FFF) or other persistent memory
	 */
	public boolean hasBatteryBackedRam() {
		return batteryBackedRam;
	}

	/**
	 * Returns whether the cartridge contains a 512-byte trainer at $7000-$71FF (stored before PRG data)
	 */
	//TODO: This is not part of the cartridge but part of the ROM
	// Remove this from here (past me is lazy so I'll leave it to you)
	public boolean hasTrainer() {
		return trainer;
	}


	public Mapper.MirrorMode getMirrorMode(int address) {
		return mapper.getMirrorMode(address);
	}

	//whether the CIRAM is enabled when accessing this address
	public boolean isCIRAMEnabled(int address) {
		return mapper.isCIRAMEnabled(address);
	}

	@Override
	public int read(int addr) {
        return (byte) mapper.cpuRead(addr & 0xFFFF);
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


}
