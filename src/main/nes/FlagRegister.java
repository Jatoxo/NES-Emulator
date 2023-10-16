package main.nes;

import java.util.HashMap;

public class FlagRegister extends Register {

	public FlagRegister(int initialValue, BitSize size) {
		super(initialValue, size);
	}



	public boolean isSet(int bitMask) {
		return (value & bitMask) > 0;
	}

	public int getFlag(int bitMask) {
		return (value & bitMask);
	}

	public void setFlag(int bitMask, boolean val) {
		if(val) {
			value |= bitMask;
		} else {
			value &= ~bitMask;
		}
	}
}
