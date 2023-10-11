package main.nes;

import java.util.ArrayList;


public class Bus {

	private final ArrayList<BusDevice> busDevices;

	//Used to quickly access bus devices
	private BusDevice[] busDevicesArray;

	public Bus() {
		busDevices = new ArrayList<>();
	}

	public void addBusDevice(BusDevice busDevice) {
		busDevices.add(busDevice);
		busDevicesArray = busDevices.toArray(new BusDevice[0]);
	}
	public void removeBusDevice(BusDevice busDevice) {
		busDevices.remove(busDevice);
		busDevicesArray = busDevices.toArray(new BusDevice[0]);
	}

	public void write(int addr, int data) { //16-Bit address, 8-Bit Data
		for(BusDevice device : busDevicesArray) {
			if(addr >= device.addrStart && addr <= device.addrEnd) {
				device.write(addr, data & 0xFF);
				return;
			}
		}

	}

	public int read(int addr, boolean bReadOnly) {
		for(BusDevice device : busDevicesArray) {
			if(addr >= device.addrStart && addr <= device.addrEnd) {
				return device.read(addr) & 0xFF;
			}
		}


		return 0;

	}

}
