package main.nes;

public class Instruction {
	public static final int NOOP = 0;
	public static final int ADC = 1;
	public static final int AND = 2;
	public static final int ASL = 3;


	String mnemonic;

	int addressingMode;
	int operation;
	int cycles;

	public static Instruction fromOpCode(byte opCode) {
		//TODO: Add all opcodes to instructions

		return new Instruction();
	}

}
