package main.nes.apu;


public class PulseChannel {
    //Divides the CPU clock by a number determined through the registers
    Divider timerDivider = new Divider(0);

    //Divides the timer output by 2
    Divider timerOutputDivider = new Divider(1);

    Envelope envelope = new Envelope();

    Sequencer dutySequencer = new Sequencer();

    LengthCounter lengthCounter = new LengthCounter();

    Sweep sweep = new Sweep(timerDivider);


    boolean div2 = false;


    boolean secondPulse;

    public PulseChannel(FrameSequencer frameSequencer, int id) {
        secondPulse = id == 1;
        sweep.enableAltBehaviour(secondPulse);

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
