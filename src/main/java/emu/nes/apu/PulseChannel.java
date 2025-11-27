package emu.nes.apu;

/**
 * The pulse channel is a square wave generator with a variable duty cycle.
 * It contains an envelope unit which determines the volume of the channel,
 * a length counter which counts down and silences the channel when it reaches 0,
 * a sweep unit which changes the timer period (pitch) of the channel over time,
 * and a sequencer which generates the actual square wave by cycling through a sequence of 8 steps (either disabling or enabling the output)
 */
public class PulseChannel {
    //Divides the CPU clock by a number determined through the registers
    //The output ticks the duty sequencer (After an additional divide by 2), meaning smaller periods result in higher pitch
    Divider timerDivider = new Divider(0);

    //Determines the volume of the channel
    //The pulse channel, like the triangle and noise channel, outputs a 4-bit volume value
    //The source volume (0-15) is determined by the envelope unit. But the channel can be silenced by the length counter or sweep unit.
    Envelope envelope = new Envelope();

    //Determines the duty cycle of the channel
    PulseSequencer dutySequencer = new PulseSequencer();

    //Counts down and silences the channel when it reaches 0
    LengthCounter lengthCounter = new LengthCounter();

    //Changes the timer period of the channel over time
    Sweep sweep = new Sweep(timerDivider);

    //Determines whether this is the first or second pulse channel
    //The second channel's sweep unit has slightly different behaviour
    boolean secondPulse;

    public PulseChannel(int id) {
        secondPulse = id == 1;
        sweep.enableAltBehaviour(secondPulse);
    }


    /**
     * Called for every cpu cycle
     */
    public void clockTimer() {
        if(timerDivider.tick()) {
            dutySequencer.advance();
        }
    }

    /**
     * Get the volume value of the channel
     * @return A 4-Bit Volume value
     */
    public int getVolume() {

        if(!dutySequencer.getCurrentValue()) {
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


    /**
     * Resets the channel to the power-up state (Hard reset)
     */
    public void reset() {
        //Disable length counter (Also resets count)
        lengthCounter.setEnabled(false);
        lengthCounter.setHalt(false);

        //Reset duty cycle sequence and step
        dutySequencer.switchSequence(0);
        dutySequencer.reset();

        //Reset envelope (No loop, not disabled, divider period 0)
        envelope.reset();

        //Disable & Reset Sweep unit
        sweep.reset();

        //Reset timer
        timerDivider.setPeriod(0);
        timerDivider.reset();
    }
}
