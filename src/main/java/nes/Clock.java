package nes;

import nes.apu.APU;

import java.security.interfaces.ECPublicKey;
import java.util.*;

public class Clock {
	private final Jtx6502 cpu;
	private final PPU ppu;
	private final APU apu;
	private final Audio audio;


	double cyclesPerSample = (double) 1_789_773 / 44_100;
	double cycleAccumulator = 0;

	// Every cycle corresponds to one CPU cycle
	private long totalCycles = 0;

	public boolean isPut = false;


	public Clock(Nes nes) {
		this.cpu = nes.cpu;
		this.ppu = nes.ppu;
		this.apu = nes.apu;
		this.audio = nes.audio;
	}


	//Advance the clock by one CPU cycle
	public void tick() {
		cpu.clockCycle();
		ppu.clock();
		ppu.clock();
		ppu.clock();

		//This clocks the pulse, triangle, noise and DMC channels
		//Only the triangle channel clocks at every step, others clock at half the rate
		apu.clock(isPut);


		cycleAccumulator += 1;
		if (cycleAccumulator >= cyclesPerSample) {
			cycleAccumulator -= cyclesPerSample;
			audio.sample(apu.getVolume());
		}


		isPut = !isPut;
		totalCycles++;
	}


	public void reset() {
		totalCycles = 0;
	}
}
