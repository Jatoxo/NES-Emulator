package main.nes.apu;

public class Sweep implements Sequencer.SequencerListener {

    Divider divider = new Divider(0);

    Divider pulseTimerDivider;

    private boolean sweepRegWrite = false;

    private int shiftCount = 0;

    private boolean enabled = false;

    private boolean negate = false;

    //Second pulse channel sweep behaves slightly differently
    private boolean altBehavior = false;

    public Sweep(Divider pulseTimerDivider) {
        this.pulseTimerDivider = pulseTimerDivider;
    }



    //Returns the target period
    public int calculateTarget() {
        int value = pulseTimerDivider.getPeriod();

        int temp = value;
        temp >>>= shiftCount;

        if(negate) {
            temp = -temp;

            //Pulse 1 takes 1s complement
            //Pulse 2 takes 2s complement
            if(!altBehavior) {
                //If this is Pulse 1, subtract 1 to get 1s complement
                temp -= 1;
            }
        }

        int target = value + temp;

        return Math.max(target, 0);
    }

    public void sweepRegWrite() {
        sweepRegWrite = true;
    }

    public void setNegate(boolean negate) {
        this.negate = negate;
    }

    public void enableAltBehaviour(boolean enabled) {
        altBehavior = enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setShiftCount(int shiftCount) {
        this.shiftCount = shiftCount & 0x7;
    }

    @Override
    public void tick(int step) {
        if(divider.tick()) {

            if(enabled && shiftCount > 0) {
                int targetPeriod = calculateTarget();

                if(!(pulseTimerDivider.period < 8 || targetPeriod > 0x7FF)) {

                    pulseTimerDivider.setPeriod(targetPeriod);
                }
            }

        }

        if(sweepRegWrite) {
            divider.reset();
            sweepRegWrite = false;
        }

    }
}
