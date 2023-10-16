package main.nes;

public class Register {
	public enum BitSize {
		BITS_4,
		BITS_8,
		BITS_16
	}

	protected int value;
	private int bits;

	BitSize size;

	//String name;
	//String shortName;


	private int mask = 0;

	public Register(int initialValue, BitSize size) {
		this.value = initialValue;
		this.size = size;

		switch (size) {
			case BITS_4:
				bits = 4;
				mask = 0xF;
				break;
			case BITS_8:
				bits = 8;
				mask = 0xFF;
				break;
			case BITS_16:
				bits = 16;
				mask = 0xFFFF;
				break;
		}

		//this.name = name;
		//this.shortName = shortName;

		if(mask == 0)
			mask = (((int) Math.pow(2, bits)) - 1);
	}


	public void set(int value) {
		this.value = value & mask;
	}
	public int get() {
		return value & mask;
	}

	//Return the current value, then increment the Register
	public int increment() {
		int ret = get();
		value++;

		return ret;
	}

	//Return the current value, then decrement the Register
	public int decrement() {
		int ret = get();
		value--;
		return ret;
	}




}
