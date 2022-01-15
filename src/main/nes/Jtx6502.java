package main.nes;

public class Jtx6502 implements Tickable {
	public static final int ADDR_ACCUM = 1;
	public static final int ADDR_IMM = 2;
	public static final int ADDR_IMP = 3;
	public static final int ADDR_ABS = 4;
	public static final int ADDR_ZP =  5;
	public static final int ADDR_ZPX = 6;
	public static final int ADDR_ZPY = 7;
	public static final int ADDR_ABSX = 8;
	public static final int ADDR_ABSY = 9;
	public static final int ADDR_REL = 10;
	public static final int ADDR_IZX = 11;
	public static final int ADDR_IZY = 12;
	public static final int ADDR_IND = 13;





	Bus bus;
	Clock clock;

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
	Register a = new Register(0x00, Register.BitSize.BITS_8, "Accumulator", "A"); //Accumulator Register
	Register x = new Register(0x00, Register.BitSize.BITS_8, "X index reg", "X"); //X (Index Register)
	Register y = new Register(0x00, Register.BitSize.BITS_8, "Y index reg", "Y"); //Y (Index Register)
	Register s = new Register(0x00, Register.BitSize.BITS_8, "Stack Pointer", "S"); //Stack Pointer (Points to location on bus)
	Register pc = new Register(0x00, Register.BitSize.BITS_16, "Program Counter", "PC"); //Program counter

	int status = 0x00; //Status register



	int addr_abs = 0x0000;
	int addr_rel = 0x00;
	int opcode = 0x00;
	int cycles = 0;

	long totalCycles = 0;

	private boolean pageCrossed = false;


	Instruction currentInstruction;



	public Jtx6502() {
		this.bus = new Bus();
		this.clock = new Clock(Clock.NTSC_MASTER_CLOCK_SPEED);
		clock.addListener(this, 12);

		clock.start();
	}



	public void clockCycle() {
		totalCycles++;

		if(cycles == 0) {
			opcode = read(pc.get());
			pc.increment();

			currentInstruction = Instruction.fromOpCode((byte) opcode);
			executeInstruction(currentInstruction);

		}

		cycles--;
	}

	private void executeInstruction(Instruction instruction) {
		int address = getAddress(instruction.addressingMode);

		switch(instruction.operation) {
			case Instruction.ADC:

		}


	}

	private int getAddress(int addressingMode) {
		pageCrossed = false;

		int i = 0;
		switch(addressingMode) {
			case ADDR_ACCUM:
			case ADDR_IMP:
				return 0;
			case ADDR_IMM:
				return pc.increment();
			case ADDR_ABS:
				return read(pc.increment()) ^ (read(pc.increment()) << 8);
			case ADDR_ZP:
				return read(pc.increment()) & 0xFF;
			case ADDR_ZPX:
				i = read(pc.increment());
				return (x.get() + i) & 0xFF; //I don't know if excluding the higher bits is necessary, but I'll do it anyway
			case ADDR_ZPY:
				i = read(pc.increment());
				return read((y.get() + i) & 0xFF);
			case ADDR_ABSX:
				i = read(pc.increment()) | (read(pc.increment()) << 8);
				int addr = i += x.get();

				if((i & 0xFF00) != (addr & 0xFF00)) {
					pageCrossed = true;
				}
				return addr;
			case ADDR_ABSY:
				i = read(pc.increment()) | (read(pc.increment()) << 8);
				addr = i += y.get();

				if((i & 0xFF00) != (addr & 0xFF00)) {
					pageCrossed = true;
				}
				return addr;

			case ADDR_REL:
				int offset = read(pc.increment());


				if((offset & (1<<8)) == 0) {
					addr = pc.get() + offset;
				} else {
					addr = pc.get() - (~offset + 1);
				}

				if((addr & 0xFF00) != (pc.get() & 0xFF00)) {
					pageCrossed = true;
				}
				return addr;
			case ADDR_IZX:
				i = (x.get() + read(pc.increment())) & 0xFF;
				i = read(i) | (read((i + 1) & 0xFF) << 8);
				return i;
			case ADDR_IZY:
				i = read(pc.increment()); //Get an address to the first byte of address in Zero Page
				i = read(i) | (read((i + 1) & 0xFF) << 8); //read the full address in zero page

				i = (i + y.get()) & 0xFFFF; //Add y to the address

				if((i & 0xFF00) != 0x0000) { //If a page cross occurred (if we left zero page) an additional clock cycle will happen
					pageCrossed = true;
				}

				return i;

			case ADDR_IND:
				i = read(pc.increment()) | (read(pc.increment()) << 8); //Address (pointer) to another address
				i = read(i) | (read((i & 0xFF00) | ((i + 1) & 0xFF)) << 8); //The full address (The second byte of which can only come from the same page)
				return i;

		}

		return 0;//no
	}


	public void reset() {
		//Set Program counter to FFFC-FFFD
		pc.set(read(0XFFFC) | (read(0xFFFC + 1) << 8));

		// Reset internal registers
		a.set(0);
		x.set(0);
		y.set(0);

		s.set(0xFD);

		setFlag(U, U > 0);
		setFlag(I, true);

		totalCycles = 0;

		// Reset takes time
		cycles = 8;
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
		if(value) {
			status |= flagBitMask;
		} else {
			status &= ~flagBitMask;
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


	@Override
	public void tick() {
		clockCycle();
	}
}
