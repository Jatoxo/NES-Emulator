package main.nes.apu;


import main.nes.Tickable;

//Just a sequencer preconfigured to act as the APU frame sequencer and implementing the interface
public class FrameSequencer extends Sequencer implements Tickable {

    public static final int INTERRUPTS = 0;
    public static final int LENGTH_SWEEPS = 1;
    public static final int ENVELOPES = 2;

    public final Divider divider = new Divider(89490);
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


    /**
     * Ticked every master clock cycle
     */
    @Override
    public void tick() {
        if(divider.tick())
            advance();
    }

}
