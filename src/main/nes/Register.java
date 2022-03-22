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

	public Register(int initialValue, BitSize size, String name, String shortName) {
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
	}


	public void set(int value) {
		this.value = value & (((int) Math.pow(2, bits)) - 1);
	}
	public int get() {
		return value & (((int) Math.pow(2, bits)) - 1);
	}

	public int increment() {
		value++;
		int ret = get();
		return ret;
	}
	public int decrement() {
		value--;
		int ret = get();
		return ret;
	}




}
