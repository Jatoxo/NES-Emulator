package main.input;

import main.nes.BusDevice;

public class ControllerPorts extends BusDevice {
	//Whether the last write operation was a 1 or 0. If a write from 1 to 0 happens, the controllers need to poll
	boolean lastWrite = false;

	public Controller player1 = null;
	public Controller player2 = null;


	public ControllerPorts() {
		//0x4016 for controller port 0, 0x4017 for controller port 1
		super(0x4016,0x4017);
	}

	//First port is 0
	public void connectController(Controller controller, int port) {
		switch(port) {
			case 0:
				player1 = controller;
				break;
			case 1:
				player2 = controller;
				break;
		}

	}

	@Override
	public int read(int addr) {
		Controller player = null;

		if(addr == 0x4016) {
			player = player1;
		} else if(addr == 0x4017) {
			player = player2;
		}

		if(player == null) {
			//When no controller is connected, it will always report 0
			return 0;
		}

		//When the latch is set the hardware continually updates the shift register, so every read will only ever return
		//the first bit, since the shift register is immediately replaced by the button states.
		if(lastWrite) {
			player.poll();
		}

		return player.read() & 0x1f; //Mask 4 least sig. bits since only they are used
	}

	@Override
	public void write(int addr, int data) {
		boolean newWrite = (data&1) > 0;

		//When the latch is no longer set the shift registers maintain the controllers' state at that moment
		if(lastWrite && !newWrite) {
			Controller player = null;
			//The controllers should save their state in their shift register (The poll method of the controller handles this)
			switch(addr) {
				case 0x4016:
					player = player1;
					break;
				case 0x4017:
					player = player2;
					break;
			}

			if(player == null) {
				return;
			}

			player.poll();

		}

		lastWrite = newWrite;

	}


}
