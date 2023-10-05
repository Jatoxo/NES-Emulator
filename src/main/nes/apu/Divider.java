package main.nes.apu;
/*
A divider outputs a clock every n input clocks, where n is the divider's
period. It contains a counter which is decremented on the arrival of each
clock. When it reaches 0, it is reloaded with the period and an output clock is
generated. Resetting a divider reloads its counter without generating an output
clock. Changing a divider's period doesn't affect its current count.
 */
public class Divider {
    protected int value;

    protected int period;

    public Divider(int period) {
        this.period = period;

        value = period;
    }

    /**
     * Tick the divider.
     * If the divider is clocked when the count is 0, an output is generated (function returns true) and the divider
     * is reset
     * @return True if the divider outputs this tick
     */
    public boolean tick() {
        if(value == 0) {
            value = period;
            return true;
        }

        value--;
        return false;
    }

    /**
     * Resets the divider. Does not clock
     */
    public void reset() {
        value = period;
    }

    /**
     * Set the period to the period specified. Does not affect the current count.
     * @param newPeriod The new period
     */
    public void setPeriod(int newPeriod) {
        period = newPeriod;
    }


    /**
     * Get the period of the divider
     */
    public int getPeriod() {
        return period;
    }


}
