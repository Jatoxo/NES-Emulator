package main.nes;

import java.util.Dictionary;
import java.util.HashMap;

public class FlagRegister extends Register {

	public FlagRegister(int initialValue, BitSize size, String name, String shortName) {
		super(initialValue, size, name, shortName);

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
}
