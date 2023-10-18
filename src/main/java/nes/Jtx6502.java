package nes;


import static nes.Instruction.*;

//Todo: Add convenience functions for stack operations
public class Jtx6502 implements Tickable {
	public static final int ADDR_ACCUM = 1;
	public static final int ADDR_IMM = 2;
	public static final int ADDR_IMP = 3;
	public static final int ADDR_ABS = 4;
	public static final int ADDR_ZP0 =  5;
	public static final int ADDR_ZPX = 6;
	public static final int ADDR_ZPY = 7;
	public static final int ADDR_ABX = 8;
	public static final int ADDR_ABY = 9;
	public static final int ADDR_REL = 10;
	public static final int ADDR_IZX = 11;
	public static final int ADDR_IZY = 12;
	public static final int ADDR_IND = 13;



	public Bus bus;

	//Status register mask bits
	final static int C = (1 << 0); //CARRY
	final static int Z = (1 << 1); //ZERO
	final static int I = (1 << 2); //IRQ DISABLE
	final static int D = (1 << 3); //DECIMAL MODE (Unused on the NES)
	final static int B = (1 << 4); //BRK COMMAND
	final static int U = (1 << 5); //Unused (B Flag)
	final static int V = (1 << 6); //OVERFLOW
	final static int N = (1 << 7); //NEGATIVE


	//Registers (8-Bit, pc is 16 bit)
	public Register a = new Register(0x00, Register.BitSize.BITS_8); //Accumulator Register
	public Register x = new Register(0x00, Register.BitSize.BITS_8); //X (Index Register)
	public Register y = new Register(0x00, Register.BitSize.BITS_8); //Y (Index Register)
	public Register s = new Register(0x00, Register.BitSize.BITS_8); //Stack Pointer (Points to location on bus)
	public Register pc = new Register(0x00, Register.BitSize.BITS_16); //Program counter

	public int status = 0x00; //Status register
	//public FlagRegister statusF = new FlagRegister(0x00, Register.BitSize.BITS_8, "Status", "P");



	int opcode = 0x00;
	public int cycles = 0;

	long totalCycles = 0;

	private boolean pageCrossed = false;


	Instruction currentInstruction;

	private boolean raiseNMI = false;
	private boolean raiseIRQ = false;


	public Jtx6502() {
		this.bus = new Bus();

		/*
		statusF.addFlag("Carry", "C", C);
		statusF.addFlag("Zero", "Z", Z);
		statusF.addFlag("IRQ Disable", "I", I);
		statusF.addFlag("Decimal Mode", "D", D);
		statusF.addFlag("B-Flag", "B", B);
		statusF.addFlag("Overflow", "V", V);
		statusF.addFlag("Negative", "N", N);
		*/

	}

	public void raiseNMI() {
		raiseNMI = true;
	}

	public void raiseIRQ() {
		System.out.println("IRQ raised");
		//TODO: This needs work. Interrupt is level sensitive, so someone might call this function but only afterwards
		// the inhibit flag gets cleared. In that case an interrupt still needs to be triggered.
		raiseIRQ = true;


	}

	public void logCpuState() {
		System.out.print(String.format("%x", currentInstruction.address).toUpperCase());
		System.out.print(String.format(" %x", currentInstruction.opcode).toUpperCase()); //First instruction byte (opcode)
		System.out.print("  ");
		System.out.print(currentInstruction.mnemonic); //Mnemonic
		System.out.print("                      ");

		System.out.print(String.format("A:%02x ", a.get()).toUpperCase());
		System.out.print(String.format("X:%02x ", x.get()).toUpperCase());
		System.out.print(String.format("Y:%02x ", y.get()).toUpperCase());
		System.out.print(String.format("P:%02x ", status).toUpperCase());
		System.out.print(String.format("SP:%02x ", s.get()).toUpperCase());


		System.out.println(" CYC: " + totalCycles);
	}

	public void clockCycle() {


		if(cycles == 0 && raiseNMI) {
			raiseNMI = false;
			nmi();
		} else if(cycles == 0 && raiseIRQ) {
			raiseIRQ = false;
			irq();
		} else if(cycles == 0) {


			opcode = read(pc.get());

			currentInstruction = fromOpCode((byte) opcode);
			currentInstruction.address = pc.get();

			pc.increment();


			//logCpuState();





			cycles = currentInstruction.cycles;


			executeInstruction(currentInstruction);
			//index++;

		}

		cycles--;
		totalCycles++;
	}

	private void executeInstruction(Instruction instruction) {
		int address = getAddress(instruction.addressingMode);
		int i = 0;

		switch(instruction.operation) {
			case ADC:
			case AND:
			case CMP:
			case EOR:
			case LDA:
			case LDY:
			case LDX:
			case ORA:
			case SBC:
				if(pageCrossed) {
					cycles++;
				}


		}
		int m;
		switch(instruction.operation) {

			case ADC:
				i = read(address) & 0xFF;

				int result = (a.get() & 0xFF) + i + getFlag(C);

				setFlag(C, result > 0xFF);
				setFlag(Z, (result & 0xFF) == 0);
				setFlag(V, ((~(a.get()^i) & (a.get()^result)) & 0x80) > 0);
				setFlag(N, (result & 0x80) > 0);
				a.set(result);

				break;
			case AND:
				i = read(address) & 0xFF;
				a.set((a.get() & 0xFF) & i);

				setFlag(Z, a.get() == 0);
				setFlag(N, (a.get() & 0x80) > 0);
				break;

			case ASL:
				if(instruction.addressingMode == ADDR_IMP || instruction.addressingMode == ADDR_ACCUM) {
					i = a.get() << 1;
					a.set(i & 0xFF);
				} else {
					i = read(address) << 1;
					write(address, i & 0xFF);
				}
				setFlag(C, i > 0xFF);
				setFlag(Z, (i & 0xFF) == 0);
				setFlag(N, (i & 0x80) > 0);
				break;

			case BCC:
				if(getFlag(C) == 0) {
					cycles++;
					if(pageCrossed) {
						cycles++;
					}
					pc.set(address);
				}
				break;

			case BCS:
				if(getFlag(C) > 0) {
					cycles++;
					if(pageCrossed) {
						cycles++;
					}
					pc.set(address);
				}
				break;

			case BEQ:
				if(getFlag(Z) > 0) {
					cycles++;
					if(pageCrossed) {
						cycles++;
					}
					pc.set(address);
				}
				break;

			case BIT: //TODO: For overflow and negative, the read value is used. I.e. overflow = read_value & 0x40. Only the zero flag uses the AND result (confirm??)
				m = read(address) & 0xFF;
				i = (a.get() & 0xFF) & m;
				setFlag(Z, i == 0);

				setFlag(V, (m & 0x40) > 0);
				setFlag(N, (m & 0x80) > 0);
				break;

			case BMI:
				if(getFlag(N) > 0) {
					cycles++;
					if(pageCrossed) {
						cycles++;
					}
					pc.set(address);
				}
				break;

			case BNE:
				if(getFlag(Z) == 0) {
					cycles++;
					if(pageCrossed) {
						cycles++;
					}
					pc.set(address);
				}
				break;

			case BPL:
				if(getFlag(N) == 0) {
					cycles++;
					if(pageCrossed) {
						cycles++;
					}
					pc.set(address);
				}
				break;

			case BRK:
				pc.increment();

				write(0x0100 + s.get(), (pc.get() >> 8) & 0xFF); //Push pc to stack
				s.decrement();
				write(0x0100 + s.get(), pc.get() & 0xFF);
				s.decrement();


				write(0x0100 + s.get(), status | B | U); //Push status to stack with B Flag set
				s.decrement();

				setFlag(I, true); //Set I flag (After pushing status?)
				pc.set( (read(0xFFFE)) | (read(0xFFFF) << 8));
				break;

			case BVC:
				if(getFlag(V) == 0) {
					cycles++;
					if(pageCrossed) {
						cycles++;
					}
					pc.set(address);
				}
				break;

			case BVS:
				if(getFlag(V) > 0) {
					cycles++;
					if(pageCrossed) {
						cycles++;
					}
					pc.set(address);
				}
				break;

			case CLC:
				setFlag(C, false);
				break;
			case CLD:
				setFlag(D, false);
				break;
			case CLI:
				setFlag(I, false);
				break;
			case CLV:
				setFlag(V, false);
				break;

			case CMP:
				m = read(address) & 0xFF;
				i = a.get() - m;

				setFlag(C, a.get() >= m);
				setFlag(Z, (i & 0xFF) == 0);
				setFlag(N, (i & 0x80) > 0);
				break;
			case CPX:
				m = read(address) & 0xFF;
				i = x.get() - m;

				setFlag(C, x.get() >= m);
				setFlag(Z, (i & 0xFF) == 0);
				setFlag(N, (i & 0x80) > 0);
				break;
			case CPY:
				m = read(address) & 0xFF;
				i = y.get() - m;

				setFlag(C, y.get() >= m);
				setFlag(Z, (i & 0xFF) == 0);
				setFlag(N, (i & 0x80) > 0);
				break;

			case DEC:
				i = read(address);
				i--;
				write(address, i & 0xFF);

				setFlag(Z, (i & 0xFF) == 0);
				setFlag(N, (i & 0x80) > 0);
				break;
			case DEX:
				i = (x.get() - 1) & 0xFF;
				x.set(i);

				setFlag(Z, (i & 0xFF) == 0);
				setFlag(N, (i & 0x80) > 0);
				break;
			case DEY:
				i = (y.get() - 1) & 0xFF;
				y.set(i);

				setFlag(Z, (i & 0xFF) == 0);
				setFlag(N, (i & 0x80) > 0);
				break;

			case EOR:
				i = read(address) & 0xFF;

				a.set(a.get() ^ i);

				setFlag(Z, a.get() == 0);
				setFlag(N, (a.get() & 0x80) > 0);
				break;

			case INC:
				i = read(address) + 1;
				write(address, i & 0xFF);

				setFlag(Z, (i & 0xFF) == 0);
				setFlag(N, (i & 0x80) > 0);
				break;
			case INX:
				i = (x.get() + 1) & 0xFF;
				x.set(i);

				setFlag(Z, (i & 0xFF) == 0);
				setFlag(N, (i & 0x80) > 0);
				break;
			case INY:
				i = (y.get() + 1) & 0xFF;
				y.set(i);

				setFlag(Z, (i & 0xFF) == 0);
				setFlag(N, (i & 0x80) > 0);
				break;
			case JMP:
				pc.set(address);
				break;
			case JSR:
				i = pc.get() - 1; //PC is pushed before second byte of address is read, so it points to the second byte of the instruction
				write(0x0100 + s.get(), (i >> 8) & 0xFF);
				s.decrement();
				write(0x0100 + s.get(), i & 0xFF);
				s.decrement();

				//Stack could have written to the high byte of the absolute address, so read it again (hack)
				int hiPC = read(pc.get() - 1, true) & 0xFF;
				address &= 0x00FF;
				address |= hiPC << 8;

				pc.set(address);
				break;

			case LDA:
				i = read(address);
				a.set(i & 0xFF);
				setFlag(Z, a.get() == 0);
				setFlag(N, (a.get() & 0x80) > 0);
				break;
			case LDX:
				i = read(address);
				x.set(i & 0xFF);
				setFlag(Z, x.get() == 0);
				setFlag(N, (x.get() & 0x80) > 0);
				break;
			case LDY:
				i = read(address);
				y.set(i & 0xFF);
				setFlag(Z, y.get() == 0);
				setFlag(N, (y.get() & 0x80) > 0);
				break;

			case LSR:
				int carry;
				if(instruction.addressingMode == ADDR_IMP || instruction.addressingMode == ADDR_ACCUM) {
					carry = a.get() & 1;
					i = a.get() >> 1;
					a.set(i & 0xFF);
				} else {
					i = read(address);
					carry = i & 1;
					i = i >> 1;
					write(address, i & 0xFF);
				}

				setFlag(C, carry > 0);
				setFlag(Z, (i & 0xFF) == 0);
				setFlag(N, (i & 0x80) > 0);
				break;
			case NOP:
				switch(instruction.opcode) {
					case 0x1C:
					case 0x3C:
					case 0x5C:
					case 0x7C:
					case 0xDC:
					case 0xFC:
						if(pageCrossed) {
							cycles++;
						}
					break;
				}
				break;

			case ORA:
				i = read(address);

				a.set(a.get() | (i & 0xFF));

				setFlag(Z, a.get() == 0);
				setFlag(N, (a.get() & 0x80) > 0);
				break;

			case PHA:
				write(0x0100 + s.get(), a.get());
				s.decrement();
				break;

			case PHP:
				write(0x0100 + s.get(), status | B | U);
				s.decrement();
				break;

			case PLA:
				s.increment();
				i = read(0x100 + s.get());
				a.set(i);

				setFlag(Z, a.get() == 0);
				setFlag(N, (a.get() & 0x80) > 0);

				break;

			case PLP:
				s.increment();
				i = read(0x100 + s.get());
				i &= 0xEF; //Mask out B Flag
				status = i | U;
				break;

			case ROL:
				if(instruction.addressingMode == ADDR_IMP || instruction.addressingMode == ADDR_ACCUM) {
					i = a.get() << 1 | getFlag(C); //Shift bits left filling in carry for bit 0

					a.set(i & 0xFF);
				} else {
					i = read(address) << 1 | getFlag(C);

					write(address, i & 0xFF);
				}

				setFlag(C, (i & 0x100) > 0); //Set carry to the shifted off bit
				setFlag(Z, (i & 0xFF) == 0);
				setFlag(N, (i & 0x80) > 0);
				break;

			case ROR: //According to https://web.archive.org/web/20210724074746/http://obelisk.me.uk/6502/reference.html#ROR this sets the Z flag solely based on A but that's dumb so I'm not doing that
				if(instruction.addressingMode == ADDR_IMP || instruction.addressingMode == ADDR_ACCUM) {
					i = a.get() | (getFlag(C) << 8);

					a.set((i >> 1) & 0xFF);
				} else {
					i = read(address) | (getFlag(C) << 8);

					write(address, (i >> 1) & 0xFF);
				}

				setFlag(C, (i & 1) > 0);
				setFlag(Z, ((i >> 1) & 0xFF) == 0);
				setFlag(N, ((i >> 1) & 0x80) > 0);
				break;

			case RTI:
				s.increment();
				status = (read(0x100 + s.increment()) & 0xFF) & 0xEF; //Mask out B Flag
				status |= U; //Set U Flag

				int pcl = read(0x100 + s.increment()) & 0xFF;
				int pch = read(0x100 + s.get()) & 0xFF;

				int pcfull = pcl | (pch << 8);
				pc.set(pcfull);
				break;

			case RTS:
				s.increment();
				int lo = 0x100 + s.increment();
				int lo2 = read(lo) & 0xFF;
				int hi = 0x100 + s.get();
				int hi2 = read(hi) & 0xFF;

				int full = lo2 | (hi2 << 8);

				i = full + 1;

				pc.set(i);
				break;

			case SBC:
				int n = read(address) & 0xFF;

				n = (~n) & 0xFF; //take one's complement

				int res = (a.get() & 0xFF) + n + getFlag(C);

				setFlag(C, (res & 0xFF00) > 0);
				setFlag(Z, ((res & 0x00FF) == 0));
				setFlag(V, (((a.get()&0xFF)^res)&(n^res)&0x80) > 0);
				setFlag(N, (res & 0x0080) > 0);
				a.set(res & 0x00FF);

				break;

			case SEC:
				setFlag(C, true);
				break;
			case SED:
				setFlag(D, true);
				break;
			case SEI:
				setFlag(I, true);
				break;
			case STA:
				write(address, a.get());
				break;
			case STX:
				write(address, x.get());
				break;
			case STY:
				write(address, y.get());
				break;
			case TAX:
				x.set(a.get());
				setFlag(Z, x.get() == 0);
				setFlag(N, (x.get() & 0x80) > 0);
				break;
			case TAY:
				y.set(a.get());
				setFlag(Z, y.get() == 0);
				setFlag(N, (y.get() & 0x80) > 0);
				break;
			case TSX:
				x.set(s.get());
				setFlag(Z, x.get() == 0);
				setFlag(N, (x.get() & 0x80) > 0);
				break;
			case TXA:
				a.set(x.get());
				setFlag(Z, a.get() == 0);
				setFlag(N, (a.get() & 0x80) > 0);
				break;
			case TXS:
				s.set(x.get());
				break;
			case TYA:
				a.set(y.get());
				setFlag(Z, a.get() == 0);
				setFlag(N, (a.get() & 0x80) > 0);
				break;

			case XXX:
				break;
			default:
				System.out.println("BIIIG FUCKY WUCKY!!");









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
				int low = read(pc.increment()) & 0xFF;
				int hi = (read(pc.increment()) & 0xFF) << 8;
                return low | hi;
			case ADDR_ZP0:
                return read(pc.increment()) & 0xFF;
			case ADDR_ZPX:
				i = read(pc.increment());
				return (x.get() + i) & 0xFF; //I don't know if excluding the higher bits is necessary, but I'll do it anyway
			case ADDR_ZPY:
				i = read(pc.increment());
				return (y.get() + i) & 0xFF;
			case ADDR_ABX:
				i = read(pc.increment()) | (read(pc.increment()) << 8);
				int addr = i + x.get();

				if((i & 0xFF00) != (addr & 0xFF00)) {
					pageCrossed = true;
				}
				return addr;
			case ADDR_ABY:
				i = read(pc.increment()) | (read(pc.increment()) << 8);
				addr = i + y.get();

				if((i & 0xFF00) != (addr & 0xFF00)) {
					pageCrossed = true;
				}
				return addr & 0xFFFF;

			case ADDR_REL:
				int offset = read(pc.increment());

				addr = pc.get() + (byte) offset;

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

				int addre = (i + y.get()) & 0xFFFF; //Add y to the address

				if((i & 0xFF00) != (addre & 0xFF00)) { //If a page cross occurred an additional clock cycle will happen
					pageCrossed = true;
				}

				return addre;

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
		//pc.set(0x0C000); //To get nestest to work without ppu

		// Reset internal registers
		a.set(0);
		x.set(0);
		y.set(0);

		s.set(0xFD); //The stack starts here idk why it just does

		setFlag(U, true);
		setFlag(I, true);

		totalCycles = 0;

		// Reset takes time
		cycles = 7;
	}

	public void irq() { //Interrupt request signal

		// If interrupts are allowed
		if(getFlag(I) == 0) {
			System.out.println("--------------   IRQ      ----------------------");

			write(0x0100 + s.get(), (pc.get() >> 8) & 0x00FF);
			s.decrement();
			write(0x0100 + s.get(), pc.get() & 0x00FF);
			s.decrement();

			//Push status to stack
			setFlag(B, false);
			setFlag(U, true);
			write(0x0100 + s.get(), status);
			s.decrement();

			setFlag(I, true);

			// Read new pc location from fixed address
			pc.set(read(0xFFFE) | (read(0xFFFF) << 8));

			cycles = 7;
		}
	}

	public void nmi() { //Non maskable interrupt request signal
		write(0x0100 + s.get(), (pc.get() >> 8) & 0x00FF);
		s.decrement();
		write(0x0100 + s.get(), pc.get() & 0x00FF);
		s.decrement();

		//Push status to stack
		setFlag(B, false);
		setFlag(U, true);
		write(0x0100 + s.get(), status);
		s.decrement();

		setFlag(I, true);

		// Read new pc location from fixed address
		pc.set(read(0xFFFA) | (read(0xFFFB) << 8));

		cycles = 7;
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


	public void write(int addr, int data) { //16-Bit address, 8-Bit Data
		addr &= 0xFFFF;
		data &= 0xFF;

		//OAM memory DMA transfer.
		//Transfer 256 bytes from 0xDD00 until 0xDDFF to fill the OAM of the PPU
		if(addr == 0x4014) {
			//Odd cpu cycle results in extra idle cycle
			//https://www.nesdev.org/wiki/PPU_OAM#:~:text=(%2B1%20on%20odd%20CPU%20cycles)
			cycles += ((totalCycles & 1) == 0) ? 1 : 2;

			for(int i = 0; i < 256; i++) {
				int readAddr = (data << 8) | i;
				bus.write(PPU.OAMDATA, bus.read(readAddr, false));
				cycles += 2;
			}
			//In total this should have added either 513 or 514 cycles
		}

		bus.write(addr, data);
	}

	public int read(int addr, boolean bReadOnly) {
		return bus.read(addr & 0xFFFF, bReadOnly) & 0xFF;
	}
	public int read(int addr) {
		return read(addr, false) & 0xFF;
	}


	@Override
	public void tick() {
		clockCycle();
	}
}
