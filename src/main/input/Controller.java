package main.input;

public interface Controller {



	//read request from the console to get the next bit for input data lines D4 D3 D2 D1 D0
	int read();

	//Saves the currently pressed buttons into the buttonState (On the physical controller the button states are captured in the shift register)
	void poll();
}
