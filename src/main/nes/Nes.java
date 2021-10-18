package main.nes;

public class Nes {

	public static void main(String[] args) throws InterruptedException {


		Thread.sleep(8000);
	}


	public Nes() {
		Jtx6502 cpu = new Jtx6502();

	}
}
