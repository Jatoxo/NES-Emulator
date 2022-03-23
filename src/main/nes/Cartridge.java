package main.nes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Cartridge extends BusDevice implements PPUBusDevice{
	public static final int ppuAddrStart = 0;

	ArrayList<Byte> chrRom;
	ArrayList<Byte> pgrRom;

	int pgrRomChunks; //16kb units
	int chrRomChunks; //8kb units


	int Mirroring = 1 << 0; //Mirroring: 0: horizontal (vertical arrangement) (CIRAM A10 = PPU A11)
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



	public Cartridge(String file) {
		super(0x8000, 0xFFFF);

		chrRom = new ArrayList<>();
		pgrRom = new ArrayList<>();

		Path path = Paths.get(file);



		byte[] bytes;
		try {
			bytes = Files.readAllBytes(path);

			if(bytes[0] == 0x4E && bytes[1] == 0x45 && bytes[2] == 0x53 && bytes[3] == 0x1A) {
				System.out.println("iNES file format detected");
			}

			pgrRomChunks = bytes[4];
			chrRomChunks = bytes[5];
			flags6 = bytes[6];
			flags7 = bytes[7];

			mapperId = (flags6 & mapIdLo) | ((flags7 & mapIdHi) << 8);

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

			boolean nes2 = ((flags7 & nes2id) >> 2) == 2;
			System.out.print("NES2.0 Format: ");
			if(nes2) {
				System.out.println("Yes");
			} else {
				System.out.println("No");
			}

			int size = pgrRomChunks * 16384;

			int j = 0;
			for(int i = 16 + (train ? 512 : 0); i < bytes.length; i++) {
				if(j < size) {
					pgrRom.add(bytes[i]);
					//pgrRom.set(j, bytes[i]);
				}
				j++;


			}

			System.out.println("Donee");

		} catch(IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public int read(int addr) {
		byte thing = pgrRom.get(addr & 0x3FFF);
		return thing;
	}

	@Override
	public void write(int addr, int data) {

	}

	@Override
	public int ppuRead(int addr) {
		return 0;
	}
	@Override
	public void ppuWrite(int addr, int data) {

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
