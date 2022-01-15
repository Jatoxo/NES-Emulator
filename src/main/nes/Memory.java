package main.nes;

import java.util.Arrays;

public class Memory extends BusDevice {

	byte[] fakeRam = new byte[64 * 1024];

	public Memory() {
		super(0x0000, 0xFFFF);

		Arrays.fill(fakeRam, (byte) 0);
	}


	@Override
	public int read(int addr) {
		return fakeRam[addr];
	}

	@Override
	public void write(int addr, int data) {

	}
}
