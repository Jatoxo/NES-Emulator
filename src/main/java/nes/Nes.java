package nes;

import gui.GUI;
import input.Controller;
import input.ControllerPorts;
import input.StandardController;
import nes.apu.APU;
import nes.parsing.RomParser;
import nes.parsing.UnsupportedRomException;

import java.io.IOException;

import static java.lang.Thread.sleep;


public class Nes {
	public boolean paused = false;

	public Cartridge cartridge;

	public Jtx6502 cpu;
	public PPU ppu;
	public APU apu;
	public final Clock clock;
	public ControllerPorts controllerPorts;
	public GUI gui;

	public boolean limitSpeed = true;

	public static void main(String[] args) throws InterruptedException {


		//Nes nes = new Nes();

		System.out.println("oof");
	}


	Audio audio;


    EmulationListener listener;

	public Nes(EmulationListener listener) {
	    this.listener = listener;


		cpu = new Jtx6502(this);
		ppu = new PPU(this);
		apu = new APU(this);

		audio = new Audio(apu);

		cpu.bus.addBusDevice(new Memory());
		cpu.bus.addBusDevice(apu); //Fixme: This needs to be before controllers because otherwise controllers catch writes for $4017. Fix this properly

		controllerPorts = new ControllerPorts();
		cpu.bus.addBusDevice(controllerPorts);
		connectController(new StandardController(), 0);

		cpu.bus.addBusDevice(ppu);


		clock = new Clock(this);


	}

    //TODO: maybe separate console logic, emulation logic, and gui logic?s
	public void start() {

		while(!paused) {
			long lastTime = System.nanoTime();

			advanceFrame();

            listener.frameComplete();
			
			//Todo this isn't accurate at all
			while(limitSpeed && System.nanoTime() - lastTime < 16666667);
		}
	}

	public void advanceFrame() {
		//Don't tick things while we're changing cartridge
		synchronized(this) {
			while(!ppu.frameComplete) {
				clock.tick();
			}
		}
		ppu.frameComplete = false;
	}

	public void reset() {
		cpu.reset();
		ppu.reset();
		apu.reset();
	}

	public void insertCartridge(Cartridge cart) {
        if(cart == null) {
            cart = new DummyCartridge();
        }
		//OH lord
		//clock.paused = true;
		synchronized(this) {

			//This is called from the GUI thread so in order to prevent issues
			//a sleep is necessary for some reason
			//TODO: Check if it still is
			try {
				sleep(50);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}

			cpu.bus.removeBusDevice(cartridge);
			cartridge = cart;
			cpu.bus.addBusDevice(cart);


			ppu.connectCartridge(cartridge);

			reset();

			try {
				sleep(50);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void connectController(Controller controller, int port) {
		controllerPorts.connectController(controller, port);
	}
}
