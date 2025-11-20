package nes;

public abstract class BusDevice {

	//Both values inclusive
	public int addrStart;
	public int addrEnd;

	public BusDevice(int busStart, int busEnd) {
		this.addrStart = busStart;
		this.addrEnd = busEnd;
	}



	/**
	 * Read a byte from this bus device
	 * @param addr The address to read from
	 * @return Lower 8 bits: The byte read
	 *         Next higher 8 bits: Mask of which data bus lines were set (or 0 for all 8)
	 *         -1 if not responding to read
	 */
	public abstract BusValue read(int addr);

	public abstract void write(int addr, int data);

}
