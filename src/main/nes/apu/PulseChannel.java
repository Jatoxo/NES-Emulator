package main.nes.apu;

/**
 * The pulse channel is a square wave generator with a variable duty cycle.
 * It contains an envelope unit which determines the volume of the channel,
 * a length counter which counts down and silences the channel when it reaches 0,
 * a sweep unit which changes the timer period (pitch) of the channel,
 * and a sequencer which generates the actual square wave by cycling through a sequence of 8 steps (either disabling or enabling the output)
 */
public class PulseChannel {
    //Divides the CPU clock by a number determined through the registers
    Divider timerDivider = new Divider(0);

    //Divides the timer output by 2
    Divider timerOutputDivider = new Divider(1);

    //Determines the volume of the channel
    Envelope envelope = new Envelope();

    //Determines the duty cycle of the channel
    Sequencer dutySequencer = new Sequencer();

    //Counts down and silences the channel when it reaches 0
    LengthCounter lengthCounter = new LengthCounter();

    //Changes the timer period of the channel
    Sweep sweep = new Sweep(timerDivider);

    //Determines whether this is the first or second pulse channel
    //The second channel's sweep unit has slightly different behaviour
    boolean secondPulse;

    public PulseChannel(FrameSequencer frameSequencer, int id) {
        secondPulse = id == 1;
        sweep.enableAltBehaviour(secondPulse);

        //The frame sequencer clocks the Envelope, Length Counter and Sweep
        frameSequencer.addListener(FrameSequencer.ENVELOPES, envelope);
        frameSequencer.addListener(FrameSequencer.LENGTH_SWEEPS, lengthCounter);
        frameSequencer.addListener(FrameSequencer.LENGTH_SWEEPS, sweep);

        //Pulse channel has 4 different sequences
        dutySequencer.addSet(8);
        dutySequencer.addSet(8);
        dutySequencer.addSet(8);
        dutySequencer.addSet(8);

        dutySequencer.getSet(0).addSequence(false, true);
        dutySequencer.getSet(1).addSequence(false, true, true);
        dutySequencer.getSet(2).addSequence(false, true, true, true, true);
        dutySequencer.getSet(3).addSequence(true, false, false, true, true, true, true, true);
    }


    /**
     * Called for every cpu cycle
     */
    public void clockTimer() {
        if(timerDivider.tick()) {

            if(timerOutputDivider.tick()) {
                dutySequencer.advance();
            }


        }
    }

    /**
     * Get the volume value of the channel
     * @return A 4-Bit Volume value
     */
    public int getVolume() {

        if(!dutySequencer.getCurrentValue(0)) {
            return 0;
        }

        if(lengthCounter.getCount() == 0) {
            return 0;
        }

        if(timerDivider.getPeriod() < 8 || sweep.calculateTarget() > 0x7FF) {
            return 0;
        }


        return envelope.getVolume();
    }

}
