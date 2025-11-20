package nes;

public record BusValue(int value, int mask) {

    public BusValue(int value, int mask) {
        this.value = value & 0xFF;
        this.mask = mask;
    }


    public BusValue(int value) {
        this(value, 0xFF);
    }
}
