package nes.apu;

/**
 * The pulse sequencer has 4 different sequences
 */
public class PulseSequencer {

    public PulseSequencer() {
        switchSequence(0);
    }

    //The 4 sequences
    //True means the audio signal can pass, false means it is blocked
    private final boolean[][] sequences = {
            {false, true , false, false, false, false, false, false},
            {false, true , true , false, false, false, false, false},
            {false, true , true , true , true , false, false, false},
            {true , false, false, true , true , true , true , true },
    };

    //Index into the current sequence
    private int sequenceIndex = 0;

    //The current sequence
    private boolean[] currentSequence;

    /**
     * Switches the current sequence
     * @param index The index of the sequence to switch to
     */
    public void switchSequence(int index) {
        currentSequence = sequences[index % sequences.length];
    }

    /**
     * Advance the sequencer to the next step
     * Get the current value using getCurrentValue()
     */
    public void advance() {
        //Advance the current step
        sequenceIndex++;

        //Loop around once end is reached
        sequenceIndex %= currentSequence.length;
    }

    /**
     * Resets the sequencer to step 0
     */
    public void reset() {
        sequenceIndex = 0;
    }

    /**
     * Get the value at the current step in the current sequence
     */
    public boolean getCurrentValue() {
        return currentSequence[sequenceIndex];
    }
}
