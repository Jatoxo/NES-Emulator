package main.nes;

public class Register {
	enum BitSize {
		BITS_4,
		BITS_8,
		BITS_16
	}

	private int value;
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
	}


	public void set(int value) {
		this.value = value % ((int) Math.pow(2, bits));
	}
	public int get() {
		return value & ((int) Math.pow(2, bits));
	}

	public int increment() {
		value++;
		return get();
	}
	public int decrement() {
		value--;
		return get();
	}




}
