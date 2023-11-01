package nes.apu;

public class LinearCounter implements Sequencer.SequencerListener {
    private boolean halt = false;

    private int reloadValue = 0;

    private int count = 0;

    public boolean controlFlag = false;

    public int getCount() {
        return count;
    }

    public void setReloadValue(int reloadValue) {
        this.reloadValue = reloadValue;
    }

    public int getReloadValue() {
        return reloadValue;
    }

    public void setHalt(boolean halt) {
        this.halt = halt;
    }

    public boolean isHalted() {
        return halt;
    }


    @Override
    public void tick(int step) {
        if(halt) {
            count = reloadValue;
        } else if(count > 0) {
            count--;
        }

        //If control flag is clear, clear halt flag
        if(!controlFlag) {
            halt = false;
        }


    }

    /**
     * Resets the linear counter to the power-up state (Hard reset)
     * Count is 0,
     * Control flag is cleared,
     * reload value is 0,
     * halt is false
     */
    public void reset() {
        count = 0;
        controlFlag = false;
        reloadValue = 0;
        halt = false;
    }

}
