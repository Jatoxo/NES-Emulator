package nes;

public abstract class BusDevice {

	//Both values inclusive
	public int addrStart;
	public int addrEnd;

	public BusDevice(int busStart, int busEnd) {
		this.addrStart = busStart;
		this.addrEnd = busEnd;
	}

	public abstract int read(int addr);

	public abstract void write(int addr, int data);

}
