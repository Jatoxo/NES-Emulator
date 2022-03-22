package main.nes;

public interface PPUBusDevice {

	//Both values inclusive
	int getAddrStart();
	int getAddrEnd();

	int ppuRead(int addr);
	void ppuWrite(int addr, int data);

}
