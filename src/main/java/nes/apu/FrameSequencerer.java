package nes.apu;

/*
 * Hardcoded version of the frame sequencer
 */
public class FrameSequencerer {

    private Sequencer.SequencerListener interrupts;
    private PulseChannel pulse1;
    private PulseChannel pulse2;
    private TriangleChannel triangleChannel;
    private NoiseChannel noiseChannel;

    private int divider = 89489;

    private int mode = 0;
    private int step;

    private static int INTERRUPTS = 0;
    private static int LENGTH_SWEEPS = 1;
    private static int ENVELOPES = 2;

    private boolean[][] sequence4step = {
        {false, false, false, true }, //Interrupts
        {false, true , false, true }, //Length and Sweep
        {true , true , true , true }, //Envelopes
    };
    private boolean[][] sequence5step = {
        {false, false, false, false, false}, //Interrupts
        {true , false, true , false, false}, //Length and Sweep
        {true , true , true , true , false}, //Envelopes
    };



    public FrameSequencerer(Sequencer.SequencerListener interrupts, PulseChannel pulse1, PulseChannel pulse2, TriangleChannel triangleChannel, NoiseChannel noiseChannel) {
        this.interrupts = interrupts;
        this.pulse1 = pulse1;
        this.pulse2 = pulse2;
        this.triangleChannel = triangleChannel;
        this.noiseChannel = noiseChannel;
    }

    /**
     * Tick the frame sequencer
     * This should be called every master clock cycle
     */
    public void tick() {
        if(divider-- == 0) {
            divider = 89489;

            advance();
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
    }

    /**
     * Advance the frame sequencer to the next step
     * ticking components that need to be ticked
     */
    public void advance() {
        boolean[][] sequence = mode == 0 ? sequence4step : sequence5step;

        //Tick the components that need to be ticked
        if(sequence[INTERRUPTS][step]) {
            interrupts.tick(step);
        }

        if(sequence[LENGTH_SWEEPS][step]) {
            pulse1.lengthCounter.tick(step);
            pulse1.sweep.tick(step);

            pulse2.lengthCounter.tick(step);
            pulse2.sweep.tick(step);

            noiseChannel.lengthCounter.tick(step);

            triangleChannel.lengthCounter.tick(step);
        }

        if(sequence[ENVELOPES][step]) {
            pulse1.envelope.tick(step);
            pulse2.envelope.tick(step);
            noiseChannel.envelope.tick(step);
            triangleChannel.linearCounter.tick(step);
        }

        //Advance the step
        step++;
        step %= mode == 0 ? 4 : 5;
    }

    /**
     * Reset the step and divider but not the mode
     */
    public void reset() {
        step = 0;
        divider = 89489;
    }



}
