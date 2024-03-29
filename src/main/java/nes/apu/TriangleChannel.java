package nes.apu;

public class TriangleChannel {
    //The timer for the triangle channel
    //Configured by 0x400A and 0x400B
    Divider timerDivider = new Divider(0);

    //The linear counter
    LinearCounter linearCounter = new LinearCounter();

    //The length counter
    LengthCounter lengthCounter = new LengthCounter();

    TriangleSequencer sequencer = new TriangleSequencer();






    public TriangleChannel(FrameSequencer frameSequencer) {
        //The APU frame sequencer

        frameSequencer.addListener(FrameSequencer.LENGTH_SWEEPS, lengthCounter);
        //Linear counter gets clocked the same as envelopes
        frameSequencer.addListener(FrameSequencer.ENVELOPES, linearCounter);


    }


    public int getVolume() {

        //At the lowest two periods ($400B = 0 and $400A = 0 or 1), the resulting
        //frequency is so high that the DAC effectively outputs a value half-way between
        //7 and 8.
        if(timerDivider.getPeriod() <= 1) {
            return 7;
        }

        //The triangle channel always outputs the value of the sequencer
        //Even if the length/linear counters are zero
        return sequencer.getVolume();
    }

    /**
     * Ticks the triangle channel's timer.
     * should be called every CPU cycle
     */
    public void clockTimer() {
        if(timerDivider.tick()) {

            //Advance the sequencer if both the linear counter and length counter are non-zero
            if(linearCounter.getCount() > 0 && lengthCounter.getCount() > 0) {
                sequencer.advance();
            }
        }
    }


    /**
     * Resets the channel to the power-up state (Hard reset)
     */
    public void reset() {
        //Disable length counter (Also resets count)
        lengthCounter.setEnabled(false);
        lengthCounter.setHalt(false);

        linearCounter.reset();

        //Reset timer
        timerDivider.setPeriod(0);
        timerDivider.reset();
    }
}
