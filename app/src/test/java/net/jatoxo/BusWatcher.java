package net.jatoxo;

import net.jatoxo.emu.nes.BusDevice;
import net.jatoxo.emu.nes.BusValue;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BusWatcher extends BusDevice {
    private final Map<Integer, Byte> ram = new HashMap<>();

    public BusWatcher() {
        super(0, 0XFFFF);
    }

    List<CPUTest.Cycle> cycleHistory = new LinkedList<>();


    byte getRamValue(int address) {
        if(!ram.containsKey(address)) {
            return 0;
        }

        return ram.get(address);
    }
    void setRamValue(int address, byte value) {
        ram.put(address, value);
    }


    @Override
    public BusValue read(int addr) {
        byte value = getRamValue(addr);

        cycleHistory.add(new CPUTest.Cycle(addr, value, true));

        return new BusValue(Byte.toUnsignedInt(value));
    }

    @Override
    public void write(int addr, int data) {
        setRamValue(addr, (byte) data);
        cycleHistory.add(new CPUTest.Cycle(addr, (byte) data, false));
    }
}
