package nes.apu;

public class NoiseChannel {
    private final Divider timerDivider = new Divider(0);

    LengthCounter lengthCounter = new LengthCounter();

    Envelope envelope = new Envelope();


    //15-Bit shifter used to generate "random" on/off values
    private int randomShifter = 1 << 14; //One bit is shifted in at startup

    //Determines the random shifter mode
    private boolean mode = false;

    private static final int[] periodTable = {
            0x004,0x008,0x010,0x020,0x040,0x060,0x080,0x0A0,0x0CA,0x0FE,0x17C,0x1FC,0x2FA,0x3F8,0x7F2,0xFE4
    };

    public NoiseChannel() {

        //frameSequencer.addListener(FrameSequencer.ENVELOPES, envelope);
        //frameSequencer.addListener(FrameSequencer.LENGTH_SWEEPS, lengthCounter);

    }

    public int getVolume() {
        //When bit 0 of the shifter is set, the volume is 0
        if((randomShifter & 1)  == 1) {
            return 0;
        }
        //System.out.println(lengthCounter.getCount());
        if(lengthCounter.getCount() == 0) {
            return 0;
        }

        return envelope.getVolume();
    }

    /**
     * Sets the mode of the random shifter
     * @param mode 1 or 0
     */
    public void setMode(int mode) {
        this.mode = mode > 0;
    }

    /**
     * Sets the period of the random shifter by the index into the lookup period table
     * @param index 0-15
     */
    public void setPeriodByIndex(int index) {
        timerDivider.setPeriod(periodTable[index & 0xF]);
    }

    /**
     * Ticks the noise channel's timer
     * <p>
     * should be called every CPU cycle
     */
    public void clockTimer() {
        if(timerDivider.tick()) {
            //The random shifter is clocked by the timer


            //Determine the value of the bit that will fill the empty space after the right shift
            boolean newBit;
            if(!mode) {
                //Mode 0
                //In this mode the new bit is the XOR of bits 0 and 1
                newBit = ((randomShifter & 1) ^ ((randomShifter >> 1) & 1)) == 1;
            } else {
                //Mode 1
                //In this mode the new bit is the XOR of bits 0 and 6
                newBit = ((randomShifter & 1) ^ ((randomShifter >> 6) & 1)) == 1;
            }

            //Shift the random shifter
            randomShifter >>>= 1;

            //Set bit 14 to the new bit
            randomShifter |= (newBit ? 1 : 0) << 14;
        }
    }

    /**
     * Resets the channel to the power-up state (Hard reset)
     */
    public void reset() {
        //Disable length counter (Also resets count)
        lengthCounter.setEnabled(false);
        lengthCounter.setHalt(false);

        //Reset noise mode & shifter
        mode = false;
        randomShifter = 1 << 14;

        //Reset envelope (No loop, not disabled, divider period 0)
        envelope.reset();

        //Reset timer
        setPeriodByIndex(0);
        timerDivider.reset();
    }



}
