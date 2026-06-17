package net.jatoxo.emu.nes.apu;

/*
 * Hardcoded version of the frame sequencer
 */
public class FrameSequencererer {

    private APU apu;
    private PulseChannel pulse1;
    private PulseChannel pulse2;
    private TriangleChannel triangleChannel;
    private NoiseChannel noiseChannel;

    private long currentApuCycle;
    private int resetCounter = -1;
    private boolean doReset = false;


    private int mode = 0;






    public FrameSequencererer(APU apu, PulseChannel pulse1, PulseChannel pulse2, TriangleChannel triangleChannel, NoiseChannel noiseChannel) {
        this.apu = apu;
        this.pulse1 = pulse1;
        this.pulse2 = pulse2;
        this.triangleChannel = triangleChannel;
        this.noiseChannel = noiseChannel;
    }

    /**
     * Tick the frame sequencer
     * This should be called every CPU cycle (1 APU cycle according to NESDEV)
     */
    public void tick(boolean isPut) {
        //Reset counter was set during this CPU cycle
        //* If the write occurs during an APU cycle, the effect occurs 3 CPU cycles after the $4017 write cycle,
        // and if the write occurs between APU cycles, the effect occurs 4 CPU cycles after the write cycle.
        if(doReset) {
            resetCounter = isPut ? 3 : 4;
        }
        if(resetCounter == 0) {
            currentApuCycle = 0;
            //TODO: Assuming we don't want the IRQ to trigger now?
            return;
        }
        if(resetCounter > 0) {
            resetCounter--;
        }


        if(mode == 0 && currentApuCycle == 0 && !isPut) {
            apu.setInterruptFlag(true);
        }

        //TODO: Pal has different timing
        //https://www.nesdev.org/wiki/APU_Frame_Counter
        if(currentApuCycle == 3728 && isPut) {
            tickEnvelopes();
        }
        if(currentApuCycle == 7456 && isPut) {
            tickEnvelopes();
            tickLengthsSweeps();
        }
        if(currentApuCycle == 11185 && isPut) {
            tickEnvelopes();
        }
        if(currentApuCycle == 14914) {
            if(mode == 0) {
                apu.setInterruptFlag(true);
                if(isPut) {
                    tickEnvelopes();
                    tickLengthsSweeps();
                    if(mode == 0) {
                        currentApuCycle = 0;
                        return;
                    }
                }
            }
        }

        if(currentApuCycle == 18640 && isPut) {
            tickEnvelopes();
            tickLengthsSweeps();
            currentApuCycle = 0;
            return;
        }

        if(isPut) {
            currentApuCycle += 1;
        }
    }

    /**
     * Set the mode of the frame sequencer
     * 0: 4-step sequence
     * 1: 5-step sequence
     * @param mode The mode to set (0 or 1)
     */
    public void setMode(int mode) {
        this.mode = mode;

        //If the mode flag is set, then both "quarter frame" and "half frame" signals are also generated
        if(mode == 1) {
            tickEnvelopes();
            tickLengthsSweeps();
        }
    }

    private void tickEnvelopes() {
        pulse1.envelope.tick(0);
        pulse2.envelope.tick(0);
        noiseChannel.envelope.tick(0);
        triangleChannel.linearCounter.tick(0);
    }

    private void tickLengthsSweeps() {
        pulse1.lengthCounter.tick(0);
        pulse1.sweep.tick(0);

        pulse2.lengthCounter.tick(0);
        pulse2.sweep.tick(0);

        noiseChannel.lengthCounter.tick(0);

        triangleChannel.lengthCounter.tick(0);
    }



    /**
     * Reset the step and divider but not the mode
     */
    public void reset() {
        currentApuCycle = 0;
    }


    /**
     *  If the write occurs during an APU cycle, the effect occurs 3 CPU cycles after the $4017 write cycle,
     *  and if the write occurs between APU cycles, the effect occurs 4 CPU cycles after the write cycle.
     */
    public void resetDelayed() {
        doReset = true;
    }



}
