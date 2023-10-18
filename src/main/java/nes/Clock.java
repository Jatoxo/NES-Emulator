package nes;

import java.util.*;

public class Clock {

	private static class Listener {
		private final Tickable tickable;
		private final int divisor;
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

	private long cycles;

	public volatile boolean doTicks;

	ArrayList<Listener> listeners;


	Clock(Tickable... listeners) {
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


	public void reset() {
		cycles = 0;
	}
}
