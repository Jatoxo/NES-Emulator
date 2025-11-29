package emu.nes.ppu;

import emu.nes.Cartridge;
import emu.nes.ReadWritable;

public class PPUBus {

	private final CIRAM vram;
	public Cartridge cart;

    public ReadWritable watcher;

	public PPUBus(CIRAM vram) {
		this.vram = vram;
	}


	public void write(int addr, int data) { //16-Bit address, 8-Bit Data
		addr &= 0x3FFF; //PPU Bus has 13 address lanes

        if(watcher != null) {
            watcher.write(addr, data);
        }

		if(addr >= vram.getAddrStart() && addr <= vram.getAddrEnd()) {
			if(cart.isCIRAMEnabled(addr)) {
				vram.ppuWrite(addr, data);
				return;
			}
		}
		if(addr >= cart.getAddrStart() && addr <= cart.getAddrEnd()) {
			if(cart.isEnabled(addr)) {
				cart.ppuWrite(addr, data);
			}
		}

	}

	public int read(int addr) {
		addr &= 0x3FFF; //PPU Bus has 13 address lanes

        if(watcher != null) {
            watcher.read(addr);
        }

		if(addr >= vram.getAddrStart() && addr <= vram.getAddrEnd()) {
			if(cart.isCIRAMEnabled(addr)) {
				return vram.ppuRead(addr) & 0xFF;
			}
		}
		if(addr >= cart.getAddrStart() && addr <= cart.getAddrEnd()) {
			if(cart.isEnabled(addr)) {
				return cart.ppuRead(addr) & 0xFF;
			}
		}

		return 0;

	}
}
