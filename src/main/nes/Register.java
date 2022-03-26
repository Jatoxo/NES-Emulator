package main.nes;

public class Register {
	enum BitSize {
		BITS_4,
		BITS_8,
		BITS_16
	}

	protected int value;
	private int bits;

	BitSize size;

	String name;
	String shortName;

	int address;

	public Register(int initialValue, BitSize size, String name, String shortName) {
		this(initialValue, size, name, shortName, 0);
	}
	public Register(int initialValue, BitSize size, String name, String shortName, int address) {
		this.value = initialValue;
		this.size = size;

		switch (size) {
			case BITS_4:
				bits = 4;
				break;
			case BITS_8:
				bits = 8;
				break;
			case BITS_16:
				bits = 16;
				break;
		}

		this.name = name;
		this.shortName = shortName;
		this.address = address;
	}


	public void set(int value) {
		this.value = value & (((int) Math.pow(2, bits)) - 1);
	}
	public int get() {
		return value & (((int) Math.pow(2, bits)) - 1);
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
