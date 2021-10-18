package main.nes;

public class Jtx6502 {
	Bus bus;

	//Status register mask bits
	final static int C = (1 << 0); //CARRY
	final static int Z = (1 << 1); //ZERO
	final static int I = (1 << 2); //IRQ DISABLE
	final static int D = (1 << 3); //DECIMAL MODE (Unused on th NES)
	final static int B = (1 << 4); //BRK COMMAND
	final static int U = (1 << 5); //Unused (B Flag)
	final static int V = (1 << 6); //OVERFLOW
	final static int N = (1 << 7); //NEGATIVE


	//Registers (8-Bit, pc is 16 bit)
	Register A = new Register(0x00, Register.BitSize.BITS_8, "Accumulator", "A"); //Accumulator Register
	Register X = new Register(0x00, Register.BitSize.BITS_8, "X index reg", "X"); //X (Index Register)
	Register Y = new Register(0x00, Register.BitSize.BITS_8, "Y index reg", "Y"); //Y (Index Register)
	Register S = new Register(0x00, Register.BitSize.BITS_8, "Stack Pointer", "S"); //Stack Pointer (Points to location on bus)
	Register pc = new Register(0x00, Register.BitSize.BITS_16, "Program Counter", "PC"); //Program counter

	int status = 0x00; //Status register



	int addr_abs = 0x0000;
	int addr_rel = 0x00;
	int opcode = 0x00;
	int cycles = 0;


	public Jtx6502() {
		this.bus = new Bus();
	}

	public void clockCycle() {

		if(cycles == 0) {
			opcode = read(pc.get());
			pc.increment();

		}

		cycles--;
	}

	public void reset() {
	}

	public void irg() { //Interrupt request signal
	}

	public void nmi() { //Non maskable interrupt request signal
	}


	public int fetch() {
		return 0;
	}



	int getFlag(int flagBitMask) {
		return status & flagBitMask;
	}
	void setFlag(int flagBitMask, boolean value) {
		if((status & flagBitMask) > 0) {

		}
	}


	public void write(int addr, int data) { //16 Bit address, 8 Bit Data
		bus.write(addr, data);
	}



	public int read(int addr, boolean bReadOnly) {
		return bus.read(addr, bReadOnly);
	}
	public int read(int addr) {
		return read(addr, false);
	}

}
