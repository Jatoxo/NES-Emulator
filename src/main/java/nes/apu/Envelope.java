package nes.apu;

public class Envelope implements Sequencer.SequencerListener {

    //Keeps track of whether the 4th register has been written to since the last clock
    boolean reset = false;

    boolean loop = false;
    boolean disabled = false;

    //Counter value
    int count = 0;

    Divider divider = new Divider(0);


    public int getVolume() {
        return disabled ? divider.getPeriod() : count;
    }


    @Override
    public void tick(int step) {
        if(reset) {
            count = 15;
            divider.reset();
            reset = false;

            return;
        }

        if(divider.tick()) {
            //When the divider outputs a clock, one of two actions occurs: if loop is set and
            //counter is zero, it is set to 15, otherwise if counter is non-zero, it is
            //decremented.
            if(count == 0 && loop) {
                count = 15;
            } else if(count > 0) {
                count--;
            }
        }
    }

    /**
     * Resets this envelope unit to its power-up state:
     * Loop is disabled,
     * Envelope is enabled,
     * count is set to 0,
     * divider period is set to 0
     */
    public void reset() {
        //Also reset the temp value used for detecting writes
        reset = false;

        loop = false;
        disabled = false;
        divider.setPeriod(0);
    }


}
