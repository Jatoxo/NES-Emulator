package main.nes;

import main.nes.mappers.Mapper;
import main.nes.mappers.NROM;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static main.nes.mappers.Mapper.*;

public class Cartridge extends BusDevice implements PPUBusDevice{

	int pgrRomChunks; //16kb units
	int chrRomChunks; //8kb units


	int mirroring = 1 << 0; //Mirroring: 0: horizontal (vertical arrangement) (CIRAM A10 = PPU A11)
							//1: vertical (horizontal arrangement) (CIRAM A10 = PPU A10)
	int prgRam = 1 << 1;    //1: Cartridge contains battery-backed PRG RAM ($6000-7FFF) or other persistent memory
	int trainer = 1 << 2;   //1: 512-byte trainer at $7000-$71FF (stored before PRG data)
	int v = 1 << 3;         //1: Ignore mirroring control or above mirroring bit; instead provide four-screen VRAM
	int mapIdLo = 0xF0;     //Lower nybble of mapper number
	int flags6;

	int consoleType = 0b11; //Console type
							//0: Nintendo Entertainment System/Family Computer
                            //1: Nintendo Vs. System
                            //2: Nintendo Playchoice 10
							//3: Extended Console Type
	int nes2id = 0b1100;    //2.0 has bit 2 clear and bit 3 set:
	int mapIdHi = 0xF0;     //Higher nybble of mapper number
	int flags7;

	int mapperId;
	private Mapper mapper;



	public Cartridge(String file) {
		super(0x4020, 0xFFFF);


		Path path = Paths.get(file);



		byte[] romFile;
		try {
			romFile = Files.readAllBytes(path);

			if(romFile[0] == 0x4E && romFile[1] == 0x45 && romFile[2] == 0x53 && romFile[3] == 0x1A) {
				System.out.println("iNES file format detected");
			}

			pgrRomChunks = romFile[4];
			chrRomChunks = romFile[5];
			flags6 = romFile[6];
			flags7 = romFile[7];


			boolean nes2 = ((flags7 & nes2id) >> 2) == 2;
			System.out.print("NES2.0 Format: ");
			if(nes2) {
				System.out.println("Yes");
			} else {
				System.out.println("No");
			}


			int adDetection = romFile[12] | romFile[13] | romFile[14] | romFile[15];
			if(adDetection > 0 && !nes2) {
				//There is probably some garbage spelled across the flags here
				flags7 &= 0x0F;
			}
			mapperId = (flags6 & mapIdLo) | ((flags7 & mapIdHi) << 4);

			System.out.println("Mapper ID: " + mapperId);
			System.out.print("Console Type: ");

			switch(flags7 & consoleType) {
				case 0:
					System.out.println("Nintendo Entertainment System/Family Computer");
					break;
				case 1:
					System.out.println("Nintendo Vs. System");
					break;
				case 2:
					System.out.println("Nintendo Playchoice 10");
					break;
				case 3:
					System.out.println("Extended Console Type");
					break;
				default:
					System.out.println("Unknown");
			}

			System.out.print("Trainer: ");

			boolean train = (flags6 & trainer) > 0;
			if(train) {
				System.out.println("No");
			} else {
				System.out.println("Yes");
			}



			int prgRomSize = pgrRomChunks * 16384;

			byte[] programRom = new byte[prgRomSize];
			System.arraycopy(romFile, 16 + (train ? 512 : 0), programRom, 0, programRom.length);

			byte[] chrRom = new byte[chrRomChunks * 8192];
			System.arraycopy(romFile, 16 + prgRomSize + (train ? 512 : 0), chrRom, 0, chrRom.length);


			switch(mapperId) {
				case NROM:
					mapper = new NROM(programRom, pgrRomChunks, chrRom, mirrorMode());
					break;
				default:
					System.out.println("Unknown Mapper");
			}


		} catch(IOException e) {
			e.printStackTrace();
		}

	}


	private MirrorMode mirrorMode() {

		return (flags6 & mirroring) > 0 ? MirrorMode.VERTICAL : MirrorMode.HORIZONTAL;
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
		byte thing = (byte) mapper.cpuRead(addr & 0xFFFF);
		return thing;
	}

	@Override
	public void write(int addr, int data) {

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
