package emu.nes;

public interface ReadWritable {
    int read(int address);
    void write(int address, int data);
}
