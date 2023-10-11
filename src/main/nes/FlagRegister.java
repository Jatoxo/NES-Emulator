package main.nes;

import java.util.HashMap;

public class FlagRegister extends Register {

	public FlagRegister(int initialValue, BitSize size, String name, String shortName) {
		this(initialValue, size, name, shortName, 0);
	}
	public FlagRegister(int initialValue, BitSize size, String name, String shortName, int address) {
		super(initialValue, size, name, shortName, address);

		flagsSymbol = new HashMap<>();
		flagsName = new HashMap<>();
	}

	private final HashMap<String, Integer> flagsSymbol;
	private final HashMap<String, Integer> flagsName;

	public void addFlag(String name, String symbol, int bitMask) {
		flagsSymbol.put(symbol, bitMask);
		flagsName.put(name, bitMask);
	}


	public int getFlagN(String flagName) {
		return value & flagsSymbol.get(flagName);
	}
	public int getFlag(String symbol) {
		return value & flagsSymbol.get(symbol);
	}
	public int getFlag(int bitMask) {
		return value & bitMask;
	}

	public boolean isSet(String symbol) {
		int mask = flagsSymbol.get(symbol);
		return (value & mask) > 0;
	}
	public void setFlag(String symbol, boolean val) {
		int mask = flagsSymbol.get(symbol);
		if(val) {
			value |= mask;
		} else {
			value &= ~mask;
		}
	}



	public void copyFlags(FlagRegister reg) {
		flagsName.clear();
		flagsSymbol.clear();

		flagsSymbol.putAll(reg.flagsSymbol);
		flagsName.putAll(reg.flagsName);
	}
}
