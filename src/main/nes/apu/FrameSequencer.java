package main.nes.apu;


import main.nes.Tickable;

//Just a sequencer preconfigured to act as the APU frame sequencer and implementing the interface
public class FrameSequencer extends Sequencer {

    public static final int INTERRUPTS = 0;
    public static final int LENGTH_SWEEPS = 1;
    public static final int ENVELOPES = 2;

    private int divider = 89489;

    //public final Divider divider = new Divider(89489);

    /**
     * Just a Sequencer configured with sequences for the APU frame sequencer and a divider
     * Add listeners using the enums for the index
     */
    public FrameSequencer() {
        addSet(4);
        addSet(5);

        SequenceSet mode0 = getCurrentSet();
        mode0.addSequence(false, false, false, true );
        mode0.addSequence(false, true , false, true );
        mode0.addSequence(true , true , true , true );

        SequenceSet mode1 = getSet(1);
        mode1.addSequence(false);
        mode1.addSequence(true , false, true , false);
        mode1.addSequence(true , true , true , true);
    }

    public void reset() {
        super.reset();
        divider = 89489;
    }


    /**
     * Ticked every master clock cycle
     */
    public void tick() {
        if(divider-- == 0) {
            divider = 89489;

            //TODO: This is too slow. Replace with hardcoded sequencer
            advance();
        }
    }

}
