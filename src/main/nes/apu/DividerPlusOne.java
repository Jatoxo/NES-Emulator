package main.nes.apu;

public class DividerPlusOne extends Divider {


    public DividerPlusOne(int period) {
        super(period);
    }


    public boolean tick() {
        value--;

        if(value <= 0) {
            value = period + 1;
            return true;
        }

        return false;
    }

    public void reset() {
        value = period + 1;
    }

    /**
     * Get the period of the divider
     */
    public int getPeriod() {
        return period + 1;
    }

}
