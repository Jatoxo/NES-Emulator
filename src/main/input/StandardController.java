package main.input;

public class StandardController implements Controller{
	public boolean dpadUp = false;
	public boolean dpadDown = false;
	public boolean dpadLeft = false;
	public boolean dpadRight = false;

	public boolean buttonA = false;
	public boolean buttonB = false;

	public boolean buttonStart = false;
	public boolean buttonSelect = false;



	//"queue" of button states, this is filled with the button states when the nes polls the controller
	//by writing the latch from 1 to 0. After that the Least significant bit corresponding to one button
	//can be read and the whole thing is shifted to the right so the next state is ready for read
	//Start out as 0xFFFF because the official controllers return 1 when you keep reading past their 8 buttons
	private int buttonState = -1;

	public StandardController() {

	}

	@Override
	public int read() {
		int ret = buttonState & 1;
		buttonState = buttonState >> 1;
		return ret;
	}


	@Override
	public void poll() {
		//The button state at the LSB will be read first (The "A" button)
		//0 - A, 1 - B, 2 - Select, 3 - Start, 4 - UP, 5 - Down, 6 - Left, 7 - Right
		buttonState &= ~0xFF;
		buttonState |= (
				  i(buttonA)      << 0
				| i(buttonB)      << 1
				| i(buttonSelect) << 2
				| i(buttonStart)  << 3
				| i(dpadUp)       << 4
				| i(dpadDown)     << 5
				| i(dpadLeft)     << 6
				| i(dpadRight)    << 7
		) & 0xFF;

	}

	//converts bool to int
	private int i(boolean bool) {
		return bool ? 1 : 0;
	}
}
