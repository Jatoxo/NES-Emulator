import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BusWatcher extends nes.BusDevice {
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
    public int read(int addr) {
        byte value = getRamValue(addr);

        cycleHistory.add(new CPUTest.Cycle(addr, value, true));

        return value;
    }

    @Override
    public void write(int addr, int data) {
        setRamValue(addr, (byte) data);
        cycleHistory.add(new CPUTest.Cycle(addr, (byte) data, true));
    }
}
