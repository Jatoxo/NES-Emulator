package nes;

public interface PPUBusDevice {

	//Both values inclusive
	int getAddrStart();
	int getAddrEnd();

	int ppuRead(int addr);
	void ppuWrite(int addr, int data);
	boolean isEnabled(int addr);

}
