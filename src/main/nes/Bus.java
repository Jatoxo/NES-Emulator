package main.nes;

import main.nes.Jtx6502;

import java.util.ArrayList;
import java.util.Arrays;

public class Bus {
	Jtx6502 cpu;

	ArrayList<BusDevice> busDevices;



	public Bus() {
		busDevices = new ArrayList<>();

		busDevices.add(new Memory());
	}

	public void addBusDevice(BusDevice busDevice) {
		busDevices.add(busDevice);
	}
	public void removeBusDevice(BusDevice busDevice) {
		busDevices.remove(busDevice);
	}

	public void write(int addr, int data) { //16-Bit address, 8-Bit Data

		for(BusDevice device : busDevices) {

			if(addr >= device.addrStart && addr <= device.addrEnd) {
				device.write(addr, data);
				return;
			}
		}

	}

	public int read(int addr, boolean bReadOnly) {
		for(BusDevice device : busDevices) {
			if(addr >= device.addrStart && addr <= device.addrEnd) {
				return device.read(addr) & 0xFF;
			}
		}


		return 0;

	}

}
