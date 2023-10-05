package main.nes.apu;

public class LengthCounter implements Sequencer.SequencerListener {

    private boolean halt = false;

    private boolean enabled = true; //Todo: Startup state?

    private int count = 0;


    public void reloadWithIndex(int index) {
        if(!enabled) {
            return;
        }

        count = getReloadValue(index);

    }

    public int getCount() {
        return count;
    }



    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if(!enabled)
            count = 0;
    }

    public boolean isHalted() {
        return halt;
    }
    public void setHalt(boolean halt) {
        this.halt = halt;
    }

    public static final int[][] reloadLookup = {
            {0x0A,0x14,0x28,0x50,0xA0,0x3C,0x0E,0x1A,0x0C,0x18,0x30,0x60,0xC0,0x48,0x10,0x20},
            {0xFE,0x02,0x04,0x06,0x08,0x0A,0x0C,0x0E,0x10,0x12,0x14,0x16,0x18,0x1A,0x1C,0x1E}
    };


    public static int getReloadValue(int index) {

        //Mask to 5 bits
        index &= 0b11111;

        //Upper 4 of the 5 bits select a byte from the lookup table
        int high = (index & 0b11110) >>> 1;

        //The lowest bit selects which table to use
        return reloadLookup[index & 1][high];
    }

    /**
     * Called when ticked by the frame sequencer
     * @param step The current step in the sequence, starting at 0
     */
    @Override
    public void tick(int step) {

        if(!halt && count > 0) {

            count--;
        }

    }
}
