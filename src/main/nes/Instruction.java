package main.nes;

import static main.nes.Jtx6502.*;

public class Instruction {
	public static final int NOP = 0;
	public static final int ADC = 1;
	public static final int AND = 2;
	public static final int ASL = 3;
	public static final int BCC = 4;
	public static final int BCS = 6;
	public static final int BEQ = 7;
	public static final int BIT = 8;
	public static final int BMI = 9;
	public static final int BNE = 10;
	public static final int BPL = 11;
	public static final int BRK = 12;
	public static final int BVC = 13;
	public static final int BVS = 14;
	public static final int CLC = 15;
	public static final int CLD = 16;
	public static final int CLI = 17;
	public static final int CLV = 18;
	public static final int CMP = 19;
	public static final int CPX = 20;
	public static final int CPY = 21;
	public static final int DEC = 22;
	public static final int DEX = 23;
	public static final int DEY = 24;
	public static final int EOR = 25;
	public static final int INC = 26;
	public static final int INX = 27;
	public static final int INY = 28;
	public static final int JMP = 29;
	public static final int JSR = 30;
	public static final int LDA = 31;
	public static final int LDX = 32;
	public static final int LDY = 33;
	public static final int LSR = 34;
	public static final int ORA = 35;
	public static final int PHA = 36;
	public static final int PHP = 37;
	public static final int PLA = 38;
	public static final int PLP = 39;
	public static final int ROL = 40;
	public static final int ROR = 41;
	public static final int RTI = 42;
	public static final int RTS = 43;
	public static final int SBC = 44;
	public static final int SEC = 45;
	public static final int SED = 46;
	public static final int SEI = 47;
	public static final int STA = 48;
	public static final int STX = 49;
	public static final int STY = 50;
	public static final int TAX = 51;
	public static final int TAY = 52;
	public static final int TSX = 53;
	public static final int TXA = 54;
	public static final int TXS = 55;
	public static final int TYA = 56;
	public static final int XXX = 57;



	public String mnemonic;

	public int opcode;
	public int addressingMode;
	public int operation;
	public int cycles;

	public int address;
	
	public Instruction(String mnemonic, int operation, int addressingMode, int cycles, int opcode) {
		this.mnemonic = mnemonic;
		this.operation = operation;
		this.addressingMode = addressingMode;
		this.cycles = cycles;
		this.opcode = opcode;
	}
	private Instruction(String mnemonic, int operation, int addressingMode, int cycles) {
		this(mnemonic, operation, addressingMode, cycles, -1);
	}
	
	private static final Instruction[] lookup =
	{
		 new Instruction("BRK", BRK, ADDR_IMM, 7), new Instruction("ORA", ORA, ADDR_IZX, 6), new Instruction("???", XXX, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 8), new Instruction("???", NOP, ADDR_IMP, 3), new Instruction("ORA", ORA, ADDR_ZP0, 3), new Instruction("ASL", ASL, ADDR_ZP0, 5), new Instruction("???", XXX, ADDR_IMP, 5), new Instruction("PHP", PHP, ADDR_IMP, 3), new Instruction("ORA", ORA, ADDR_IMM, 2), new Instruction("ASL", ASL, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 2), new Instruction("???", NOP, ADDR_IMP, 4), new Instruction("ORA", ORA, ADDR_ABS, 4), new Instruction("ASL", ASL, ADDR_ABS, 6), new Instruction("???", XXX, ADDR_IMP, 6),
		 new Instruction("BPL", BPL, ADDR_REL, 2), new Instruction("ORA", ORA, ADDR_IZY, 5), new Instruction("???", XXX, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 8), new Instruction("???", NOP, ADDR_IMP, 4), new Instruction("ORA", ORA, ADDR_ZPX, 4), new Instruction("ASL", ASL, ADDR_ZPX, 6), new Instruction("???", XXX, ADDR_IMP, 6), new Instruction("CLC", CLC, ADDR_IMP, 2), new Instruction("ORA", ORA, ADDR_ABY, 4), new Instruction("???", NOP, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 7), new Instruction("???", NOP, ADDR_IMP, 4), new Instruction("ORA", ORA, ADDR_ABX, 4), new Instruction("ASL", ASL, ADDR_ABX, 7), new Instruction("???", XXX, ADDR_IMP, 7),
		 new Instruction("JSR", JSR, ADDR_ABS, 6), new Instruction("AND", AND, ADDR_IZX, 6), new Instruction("???", XXX, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 8), new Instruction("BIT", BIT, ADDR_ZP0, 3), new Instruction("AND", AND, ADDR_ZP0, 3), new Instruction("ROL", ROL, ADDR_ZP0, 5), new Instruction("???", XXX, ADDR_IMP, 5), new Instruction("PLP", PLP, ADDR_IMP, 4), new Instruction("AND", AND, ADDR_IMM, 2), new Instruction("ROL", ROL, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 2), new Instruction("BIT", BIT, ADDR_ABS, 4), new Instruction("AND", AND, ADDR_ABS, 4), new Instruction("ROL", ROL, ADDR_ABS, 6), new Instruction("???", XXX, ADDR_IMP, 6),
		 new Instruction("BMI", BMI, ADDR_REL, 2), new Instruction("AND", AND, ADDR_IZY, 5), new Instruction("???", XXX, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 8), new Instruction("???", NOP, ADDR_IMP, 4), new Instruction("AND", AND, ADDR_ZPX, 4), new Instruction("ROL", ROL, ADDR_ZPX, 6), new Instruction("???", XXX, ADDR_IMP, 6), new Instruction("SEC", SEC, ADDR_IMP, 2), new Instruction("AND", AND, ADDR_ABY, 4), new Instruction("???", NOP, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 7), new Instruction("???", NOP, ADDR_IMP, 4), new Instruction("AND", AND, ADDR_ABX, 4), new Instruction("ROL", ROL, ADDR_ABX, 7), new Instruction("???", XXX, ADDR_IMP, 7),
		 new Instruction("RTI", RTI, ADDR_IMP, 6), new Instruction("EOR", EOR, ADDR_IZX, 6), new Instruction("???", XXX, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 8), new Instruction("???", NOP, ADDR_IMP, 3), new Instruction("EOR", EOR, ADDR_ZP0, 3), new Instruction("LSR", LSR, ADDR_ZP0, 5), new Instruction("???", XXX, ADDR_IMP, 5), new Instruction("PHA", PHA, ADDR_IMP, 3), new Instruction("EOR", EOR, ADDR_IMM, 2), new Instruction("LSR", LSR, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 2), new Instruction("JMP", JMP, ADDR_ABS, 3), new Instruction("EOR", EOR, ADDR_ABS, 4), new Instruction("LSR", LSR, ADDR_ABS, 6), new Instruction("???", XXX, ADDR_IMP, 6),
		 new Instruction("BVC", BVC, ADDR_REL, 2), new Instruction("EOR", EOR, ADDR_IZY, 5), new Instruction("???", XXX, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 8), new Instruction("???", NOP, ADDR_IMP, 4), new Instruction("EOR", EOR, ADDR_ZPX, 4), new Instruction("LSR", LSR, ADDR_ZPX, 6), new Instruction("???", XXX, ADDR_IMP, 6), new Instruction("CLI", CLI, ADDR_IMP, 2), new Instruction("EOR", EOR, ADDR_ABY, 4), new Instruction("???", NOP, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 7), new Instruction("???", NOP, ADDR_IMP, 4), new Instruction("EOR", EOR, ADDR_ABX, 4), new Instruction("LSR", LSR, ADDR_ABX, 7), new Instruction("???", XXX, ADDR_IMP, 7),
		 new Instruction("RTS", RTS, ADDR_IMP, 6), new Instruction("ADC", ADC, ADDR_IZX, 6), new Instruction("???", XXX, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 8), new Instruction("???", NOP, ADDR_IMP, 3), new Instruction("ADC", ADC, ADDR_ZP0, 3), new Instruction("ROR", ROR, ADDR_ZP0, 5), new Instruction("???", XXX, ADDR_IMP, 5), new Instruction("PLA", PLA, ADDR_IMP, 4), new Instruction("ADC", ADC, ADDR_IMM, 2), new Instruction("ROR", ROR, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 2), new Instruction("JMP", JMP, ADDR_IND, 5), new Instruction("ADC", ADC, ADDR_ABS, 4), new Instruction("ROR", ROR, ADDR_ABS, 6), new Instruction("???", XXX, ADDR_IMP, 6),
		 new Instruction("BVS", BVS, ADDR_REL, 2), new Instruction("ADC", ADC, ADDR_IZY, 5), new Instruction("???", XXX, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 8), new Instruction("???", NOP, ADDR_IMP, 4), new Instruction("ADC", ADC, ADDR_ZPX, 4), new Instruction("ROR", ROR, ADDR_ZPX, 6), new Instruction("???", XXX, ADDR_IMP, 6), new Instruction("SEI", SEI, ADDR_IMP, 2), new Instruction("ADC", ADC, ADDR_ABY, 4), new Instruction("???", NOP, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 7), new Instruction("???", NOP, ADDR_IMP, 4), new Instruction("ADC", ADC, ADDR_ABX, 4), new Instruction("ROR", ROR, ADDR_ABX, 7), new Instruction("???", XXX, ADDR_IMP, 7),
		 new Instruction("???", NOP, ADDR_IMP, 2), new Instruction("STA", STA, ADDR_IZX, 6), new Instruction("???", NOP, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 6), new Instruction("STY", STY, ADDR_ZP0, 3), new Instruction("STA", STA, ADDR_ZP0, 3), new Instruction("STX", STX, ADDR_ZP0, 3), new Instruction("???", XXX, ADDR_IMP, 3), new Instruction("DEY", DEY, ADDR_IMP, 2), new Instruction("???", NOP, ADDR_IMP, 2), new Instruction("TXA", TXA, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 2), new Instruction("STY", STY, ADDR_ABS, 4), new Instruction("STA", STA, ADDR_ABS, 4), new Instruction("STX", STX, ADDR_ABS, 4), new Instruction("???", XXX, ADDR_IMP, 4),
		 new Instruction("BCC", BCC, ADDR_REL, 2), new Instruction("STA", STA, ADDR_IZY, 6), new Instruction("???", XXX, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 6), new Instruction("STY", STY, ADDR_ZPX, 4), new Instruction("STA", STA, ADDR_ZPX, 4), new Instruction("STX", STX, ADDR_ZPY, 4), new Instruction("???", XXX, ADDR_IMP, 4), new Instruction("TYA", TYA, ADDR_IMP, 2), new Instruction("STA", STA, ADDR_ABY, 5), new Instruction("TXS", TXS, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 5), new Instruction("???", NOP, ADDR_IMP, 5), new Instruction("STA", STA, ADDR_ABX, 5), new Instruction("???", XXX, ADDR_IMP, 5), new Instruction("???", XXX, ADDR_IMP, 5),
		 new Instruction("LDY", LDY, ADDR_IMM, 2), new Instruction("LDA", LDA, ADDR_IZX, 6), new Instruction("LDX", LDX, ADDR_IMM, 2), new Instruction("???", XXX, ADDR_IMP, 6), new Instruction("LDY", LDY, ADDR_ZP0, 3), new Instruction("LDA", LDA, ADDR_ZP0, 3), new Instruction("LDX", LDX, ADDR_ZP0, 3), new Instruction("???", XXX, ADDR_IMP, 3), new Instruction("TAY", TAY, ADDR_IMP, 2), new Instruction("LDA", LDA, ADDR_IMM, 2), new Instruction("TAX", TAX, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 2), new Instruction("LDY", LDY, ADDR_ABS, 4), new Instruction("LDA", LDA, ADDR_ABS, 4), new Instruction("LDX", LDX, ADDR_ABS, 4), new Instruction("???", XXX, ADDR_IMP, 4),
		 new Instruction("BCS", BCS, ADDR_REL, 2), new Instruction("LDA", LDA, ADDR_IZY, 5), new Instruction("???", XXX, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 5), new Instruction("LDY", LDY, ADDR_ZPX, 4), new Instruction("LDA", LDA, ADDR_ZPX, 4), new Instruction("LDX", LDX, ADDR_ZPY, 4), new Instruction("???", XXX, ADDR_IMP, 4), new Instruction("CLV", CLV, ADDR_IMP, 2), new Instruction("LDA", LDA, ADDR_ABY, 4), new Instruction("TSX", TSX, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 4), new Instruction("LDY", LDY, ADDR_ABX, 4), new Instruction("LDA", LDA, ADDR_ABX, 4), new Instruction("LDX", LDX, ADDR_ABY, 4), new Instruction("???", XXX, ADDR_IMP, 4),
		 new Instruction("CPY", CPY, ADDR_IMM, 2), new Instruction("CMP", CMP, ADDR_IZX, 6), new Instruction("???", NOP, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 8), new Instruction("CPY", CPY, ADDR_ZP0, 3), new Instruction("CMP", CMP, ADDR_ZP0, 3), new Instruction("DEC", DEC, ADDR_ZP0, 5), new Instruction("???", XXX, ADDR_IMP, 5), new Instruction("INY", INY, ADDR_IMP, 2), new Instruction("CMP", CMP, ADDR_IMM, 2), new Instruction("DEX", DEX, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 2), new Instruction("CPY", CPY, ADDR_ABS, 4), new Instruction("CMP", CMP, ADDR_ABS, 4), new Instruction("DEC", DEC, ADDR_ABS, 6), new Instruction("???", XXX, ADDR_IMP, 6),
		 new Instruction("BNE", BNE, ADDR_REL, 2), new Instruction("CMP", CMP, ADDR_IZY, 5), new Instruction("???", XXX, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 8), new Instruction("???", NOP, ADDR_IMP, 4), new Instruction("CMP", CMP, ADDR_ZPX, 4), new Instruction("DEC", DEC, ADDR_ZPX, 6), new Instruction("???", XXX, ADDR_IMP, 6), new Instruction("CLD", CLD, ADDR_IMP, 2), new Instruction("CMP", CMP, ADDR_ABY, 4), new Instruction("NOP", NOP, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 7), new Instruction("???", NOP, ADDR_IMP, 4), new Instruction("CMP", CMP, ADDR_ABX, 4), new Instruction("DEC", DEC, ADDR_ABX, 7), new Instruction("???", XXX, ADDR_IMP, 7),
		 new Instruction("CPX", CPX, ADDR_IMM, 2), new Instruction("SBC", SBC, ADDR_IZX, 6), new Instruction("???", NOP, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 8), new Instruction("CPX", CPX, ADDR_ZP0, 3), new Instruction("SBC", SBC, ADDR_ZP0, 3), new Instruction("INC", INC, ADDR_ZP0, 5), new Instruction("???", XXX, ADDR_IMP, 5), new Instruction("INX", INX, ADDR_IMP, 2), new Instruction("SBC", SBC, ADDR_IMM, 2), new Instruction("NOP", NOP, ADDR_IMP, 2), new Instruction("???", SBC, ADDR_IMP, 2), new Instruction("CPX", CPX, ADDR_ABS, 4), new Instruction("SBC", SBC, ADDR_ABS, 4), new Instruction("INC", INC, ADDR_ABS, 6), new Instruction("???", XXX, ADDR_IMP, 6),
		 new Instruction("BEQ", BEQ, ADDR_REL, 2), new Instruction("SBC", SBC, ADDR_IZY, 5), new Instruction("???", XXX, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 8), new Instruction("???", NOP, ADDR_IMP, 4), new Instruction("SBC", SBC, ADDR_ZPX, 4), new Instruction("INC", INC, ADDR_ZPX, 6), new Instruction("???", XXX, ADDR_IMP, 6), new Instruction("SED", SED, ADDR_IMP, 2), new Instruction("SBC", SBC, ADDR_ABY, 4), new Instruction("NOP", NOP, ADDR_IMP, 2), new Instruction("???", XXX, ADDR_IMP, 7), new Instruction("???", NOP, ADDR_IMP, 4), new Instruction("SBC", SBC, ADDR_ABX, 4), new Instruction("INC", INC, ADDR_ABX, 7), new Instruction("???", XXX, ADDR_IMP, 7),
	};

	public static Instruction fromOpCode(int opCode) {
		//TODO: Add all opcodes to instructions
		//int h = opCode & 0xFF;
		Instruction instruction = lookup[opCode & 0xFF];
		instruction.opcode = opCode & 0xFF;
		return instruction;
	}

}
