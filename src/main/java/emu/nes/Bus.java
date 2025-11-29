package emu.nes;

import java.util.ArrayList;


public class Bus {

	private final ArrayList<BusDevice> busDevices;

	//Used to quickly access bus devices
	private BusDevice[] busDevicesArray;

	private int openBus;


	public Bus() {
		busDevices = new ArrayList<>();
		openBus = 0;
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
		// Remember the value written on open bus
		openBus = data & 0xFF;

		for(BusDevice device : busDevicesArray) {
			if(addr >= device.addrStart && addr <= device.addrEnd) {
				device.write(addr, data & 0xFF);
				//return;
			}
		}
	}

	public int read(int addr, boolean bReadOnly) {
		for(BusDevice device : busDevicesArray) {
			if(addr < device.addrStart || addr > device.addrEnd) {
				continue;
			}

			BusValue busValue = device.read(addr);
			if(busValue == null) {
				//This is here so that a device can return null if it cannot be read
				//Specifically the APU returns this when reading anything other than $4015, because the controllers
				//are mapped to the same range.
				//Todo: Figure out something better for overlapping memory locations
				// Maybe the device itself should be solely responsible for determining whether to respond to a
				// read? Like, removing the range logic from bus itself
				continue;
			}

			int value = busValue.value();
			int mask = busValue.mask();

			// This address specifically isn't supposed to update the bus, as it's cpu internal
			// but bit 5 is still open bus
            //Todo: Make this optional somehow (e.g. it breaks tests)
			if(addr == 0x4015) {
				value &= 0b1101_1111;
				value |= openBus & 0b0010_0000;

				return value;
			}


			if(mask == 0 || mask == 0xFF) {
				openBus = value;
			} else {
				//Change only data bits that were set by the device that was read
				openBus &= ~mask;
				openBus |= (value & mask);
			}

			return openBus;
		}

		//Nothing was mapped to that location :(
		return openBus;
	}

}
