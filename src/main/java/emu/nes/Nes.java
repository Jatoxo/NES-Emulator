package emu.nes;

import emu.Audio;
import emu.EmulationListener;
import emu.nes.input.Controller;
import emu.nes.input.ControllerPorts;
import emu.nes.input.StandardController;
import emu.nes.apu.APU;
import emu.nes.ppu.PPU;


public class Nes {
	public boolean paused = false;

	public Cartridge cartridge;

	public Jtx6502 cpu;
	public PPU ppu;
	public APU apu;
	public final Clock clock;
	public ControllerPorts controllerPorts;

	public static double REFRESH_RATE = 60.0988;
	public boolean limitSpeed = true;


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

    public Nes() {
        this(null);
    }

    //TODO: maybe separate console logic, emulation logic, and gui logic?s
	public void start() {

		while(!paused) {
			long lastTime = System.nanoTime();

			advanceFrame();

            if(listener != null) {
                listener.frameComplete();
            }
			
			//Todo this isn't accurate at all
			while(limitSpeed && System.nanoTime() - lastTime < ((1 / REFRESH_RATE) * 1_000_000_000));
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
			cpu.bus.removeBusDevice(cartridge);
			cartridge = cart;
			cpu.bus.addBusDevice(cart);


			ppu.connectCartridge(cartridge);

            cartridge.nesConnected(this);
			reset();
		}
	}

	public void connectController(Controller controller, int port) {
		controllerPorts.connectController(controller, port);
	}
}
