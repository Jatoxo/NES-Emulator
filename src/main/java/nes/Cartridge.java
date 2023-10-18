package nes;

import nes.parsing.ROM;
import nes.mappers.Mapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class Cartridge extends BusDevice implements PPUBusDevice{
	//Stores the ROM that the Cartridge originated from
	ROM sourceROM;

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
			ROM source,
			int pgrRomChunks,
			int chrRomChunks,
			Mapper mapper,
			boolean batteryBackedRam,
			boolean trainer
	) {
		//Cartridge space is 0x4020 - 0xFFFF on the CPU bus
		super(0x4020, 0xFFFF);

		this.sourceROM = source;
		this.pgrRomChunks = pgrRomChunks;
		this.chrRomChunks = chrRomChunks;
		this.mapper = mapper;
		this.batteryBackedRam = batteryBackedRam;
		this.trainer = trainer;

		loadPersistentData();
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

	/**
	 * Some cartridges have battery backed memory that will remain after the game is turned off.
	 * This will save that memory to storage, so it can be restored the next time the same game is loaded.
	 */
	public void storePersistentData() throws IOException {
		//The Mapper isn't aware whether the PRG RAM is persistent. So only actually save it
		//if the ROM file indicates that the cartridge contains persistent memory
		if(!hasBatteryBackedRam()) {
			return;
		}

		byte[] programRam = mapper.getProgramRAM();

		//TODO: Get save location and app name from preferences instead

		String dataFolder = System.getenv("LOCALAPPDATA");
		String savePath = dataFolder + "/JatNesEmulator/";
		String saveName = sourceROM.getFileName() + ".cck";
		String fullPath = savePath + "/" + saveName;

		File saveFile = new File(savePath);
		saveFile.mkdirs();

		System.out.println("Storing PRG RAM to persistent storage at " + fullPath);
		Files.write(Path.of(fullPath), programRam);
	}

	public void loadPersistentData() {
		if(!hasBatteryBackedRam()) {
			return;
		}
		//TODO: Get save location and app name from preferences instead

		String dataFolder = System.getenv("LOCALAPPDATA");
		String savePath = dataFolder + "/JatNesEmulator/";
		String saveName = sourceROM.getFileName() + ".cck";
		String fullPath = savePath + "/" + saveName;

		File saveFile = new File(fullPath);
		if(!saveFile.exists()) {
			System.out.println("No PRG RAM data found to restore");
			return;
		}

		byte[] prgRam;
		try {
			prgRam = Files.readAllBytes(Path.of(fullPath));
		} catch (IOException e) {
			System.out.println("Failed to load PRG RAM read data");
			return;
		}

		mapper.setProgramRAM(prgRam);
		System.out.println("Loaded PRG RAM from " + fullPath);
	}


}
