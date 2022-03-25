package main.nes;

import java.util.ArrayList;

public class PPUBus {
	ArrayList<PPUBusDevice> busDevices;



	public PPUBus() {
		busDevices = new ArrayList<>();
	}

	public void addBusDevice(PPUBusDevice ppuBusDevice) {
		busDevices.add(ppuBusDevice);
	}
	public void removeBusDevice(PPUBusDevice device) {
		busDevices.remove(device);
	}

	public void write(int addr, int data) { //16-Bit address, 8-Bit Data
		addr &= 0x3FFF; //PPU Bus has 13 address lanes

		for(PPUBusDevice device : busDevices) {

			if(addr >= device.getAddrStart() && addr <= device.getAddrEnd()) {
				if(device.isEnabled(addr)) {
					device.ppuWrite(addr, data);
				}
				return;
			}
		}

	}

	public int read(int addr) {
		addr &= 0x3FFF; //PPU Bus has 13 address lanes

		for(PPUBusDevice device : busDevices) {
			if(addr >= device.getAddrStart() && addr <= device.getAddrEnd()) {

				if(device.isEnabled(addr)) {
					return device.ppuRead(addr) & 0xFF;
				}

			}
		}


		return 0;

	}
}
