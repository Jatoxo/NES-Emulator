package main.nes;

import java.util.*;

public class Clock {

	public static final long NTSC_MASTER_CLOCK_SPEED = 21477272;

	private static class Listener {
		private Tickable tickable;
		private int divisor;
		private int step;
		private long cycles;

		public Listener(Tickable listener) {
			this(listener, 1);
		}
		public Listener(Tickable listener, int divisor) {
			this.tickable = listener;
			this.divisor = divisor;
			this.step = 0;

			cycles = 0;
		}

		public void tick() {
			tickable.tick();
			cycles++;
		}
	}


	private boolean active;
	private long cycles;

	private long tickSpeed;

	public volatile boolean doTicks;

	ArrayList<Listener> listeners;


	Clock(long frequency, Tickable... listeners) {
		active = false;
		this.tickSpeed = frequency;
		this.listeners = new ArrayList<>();

		for(Tickable listener : listeners) {
			this.listeners.add(new Listener(listener));
		}
	}



	public void addListener(Tickable listener) {
		addListener(listener, 1);

	}
	public void addListener(Tickable listener, int divisor) {
		listeners.add(new Listener(listener, divisor));
	}


	public void tick() {

		for(Listener listener : listeners) {
			if(listener.step <= 0) {
				if(doTicks) {
					listener.step = listener.divisor;
					listener.tick();

				}
			}
			listener.step--;
		}


		cycles++;
	}

	public void start() {
		active = true;

		while(active) {
			tick();
			cycles++;
		}

	}

	public void stop() {
		active = false;
	}

	public void reset() {
		active = false;
		cycles = 0;
	}
}
