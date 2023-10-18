package nes;

import java.util.Arrays;

public class Memory extends BusDevice {

	byte[] fakeRam = new byte[2048];

	public Memory() {
		super(0x0000, 0x1FFF); //Mirrored three times

		Arrays.fill(fakeRam, (byte) 0);
	}


	@Override
	public int read(int addr) {
		int ret = fakeRam[addr & (2048 - 1)];
		return ret & 0xFF;
	}

	@Override
	public void write(int addr, int data) {
		fakeRam[addr & (2048 - 1)] = (byte) (data & 0xFF);
	}
}
