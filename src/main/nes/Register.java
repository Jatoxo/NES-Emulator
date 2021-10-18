package main.nes;

public class Register {
	enum BitSize {
		BITS_4,
		BITS_8,
		BITS_16
	}

	private int value;

	BitSize size;

	String name;
	String shortName;

	public Register(int initialValue, BitSize size, String name, String shortName) {
		this.value = initialValue;
		this.size = size;
	}


	public void set(int value) {
		this.value = value;
	}
	public int get() {
		return value;
	}

	public void increment() {
		value++;
		// TODO: Overflow n stuff
	}
	public void decrement() {
		value--;
		// TODO: Overflow n stuff
	}


}
