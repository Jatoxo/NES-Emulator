package emu.gui;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class FPSThread extends Thread {
    final LinkedList<Integer> fpsBuffer = new LinkedList<>();



    private GUI gui;
    private final int updateIntervalMs;

    private long lastUpdated = 0;
    private long lastFrameTimeNs = 0;
    private final Semaphore semaphore = new Semaphore(0);

    public FPSThread(GUI gui, int updateIntervalMs) {
        this.gui = gui;
        this.updateIntervalMs = updateIntervalMs;
    }

    public void frameCompleted() {
        long elapsed = System.nanoTime() - lastFrameTimeNs;
        elapsed = Math.round(elapsed / 1000000.0);

        if(elapsed != 0) {
            int fps = (int) Math.round(1000.0 / elapsed);
            addFPSValue(fps);
        }

        lastFrameTimeNs = System.nanoTime();
    }


    private void addFPSValue(int fps) {
        synchronized(fpsBuffer) {
            fpsBuffer.add(fps);
        }

        if(System.currentTimeMillis() - lastUpdated > updateIntervalMs) {
            semaphore.release();
        }
    }

    @Override
    public void run() {
        while(true) {
            try {
                semaphore.acquire();
            } catch(InterruptedException e) {
                System.out.println("FPS thread got nuked");
            }

            long cumulativeFPS = 0;
            int size;

            synchronized(fpsBuffer) {
                size = fpsBuffer.size();
                for(long fps : fpsBuffer) {
                    cumulativeFPS += fps;
                }
                fpsBuffer.clear();
            }

            int val = Math.round(cumulativeFPS / (float) size);
            gui.setTitle(gui.EMU_NAME + " - FPS: "+ val);
            lastUpdated = System.currentTimeMillis();
        }
    }
}
