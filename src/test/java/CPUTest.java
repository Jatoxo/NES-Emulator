import emu.gui.GUI;
import emu.nes.Instruction;
import emu.nes.Jtx6502;
import emu.nes.Nes;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Files;


public class CPUTest {

    public static void main(String[] args) {

        try {
            File file = new File(CPUTest.class.getResource("TomHarteTests/").getFile());

            int totalInstructionsTested = 0;
            int passed = 0;
            for(File testFile : file.listFiles()) {

                if (!testFile.getName().endsWith(".json"))
                    continue;

                String hexop = testFile.getName().substring(0, testFile.getName().length() - 5);

                int opcode = Integer.parseInt(hexop, 16);

                Instruction testedInstruction = Instruction.fromOpCode(opcode);
                if(testedInstruction.mnemonic.equals("???")) {
                    continue;
                }

                System.out.println("Running " + testedInstruction.mnemonic + " tests (" + testFile.getName() + ")");

                String testsJson = Files.readString(testFile.toPath());
                boolean result = runJsonTests(testsJson);

                totalInstructionsTested++;
                if(result) {
                    passed++;
                    System.out.println("\u001B[32m> Passed\u001B[0m");
                }
            }

            System.out.println("Tests passed: " + passed + "/" + totalInstructionsTested);


        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }


    // Runs all the tests for one of the instructions
    private static boolean runJsonTests(String testsJson) throws ParseException {
        JSONParser parser = new JSONParser();

        Object obj = parser.parse(testsJson);

        JSONArray tests = (JSONArray) obj;

        for(Object test : tests) {
            JSONObject testObj = (JSONObject) test;

            HarteTest harteTest = getTest(testObj);

            boolean success = runTest(harteTest, false);

            if(!success) {
                System.out.println("\u001B[31mTest " + harteTest.name + " failed!\u001B[0m");
                return false;
            }
        }

        return true;
    }

    private static HarteTest getTest(JSONObject testObj) {
        String name = (String) testObj.get("name");


        CPUState initialState = getState((JSONObject) testObj.get("initial"));
        CPUState finalState = getState((JSONObject) testObj.get("final"));

        Cycle[] cycles = getCycles((JSONArray) testObj.get("cycles"));

        return new HarteTest(name, initialState, finalState, cycles);
    }

    private static CPUState getState(JSONObject stateObj) {
        JSONArray initialStateMemoryObj = (JSONArray) stateObj.get("ram");

        int[][] initialMemory = new int[initialStateMemoryObj.size()][];

        int i = 0;
        for(Object memoryAllocationObj : initialStateMemoryObj) {
            JSONArray memoryAllocation = (JSONArray) memoryAllocationObj;

            initialMemory[i] = new int[2];
            initialMemory[i][0] = ((Long) memoryAllocation.get(0)).intValue();
            initialMemory[i][1] = ((Long) memoryAllocation.get(1)).intValue();

            i++;
        }

        return new CPUState(
                (int) (long) stateObj.get("pc"),
                (int) (long) stateObj.get("s"),
                (int) (long) stateObj.get("a"),
                (int) (long) stateObj.get("x"),
                (int) (long) stateObj.get("y"),
                (int) (long) stateObj.get("p"),
                initialMemory
        );
    }

    private static Cycle[] getCycles(JSONArray cyclesObj) {
        Cycle[] cycles = new Cycle[cyclesObj.size()];

        int i = 0;
        for(Object cycleObj : cyclesObj) {
            JSONArray cycle = (JSONArray) cycleObj;

            cycles[i] = new Cycle(
                    (int) (long) cycle.get(0),
                    (byte) (long) cycle.get(1),
                    cycle.get(2).equals("read")
            );

            i++;
        }

        return cycles;
    }

    // Runs a single test for one of the instructions
    public static boolean runTest(HarteTest test, boolean ignoreBusHistory) {
        Jtx6502 cpu = new Jtx6502(null);


        for(Cycle cycle : test.cycles) {
            if(cycle.address == 0x4015 && cycle.read) {
                System.out.println("\u001B[90mSkipping test due to 0x4015 read\u001B[0m");
                return true;
            }
            if(cycle.address == 0x4014 && !cycle.read) {
                System.out.println("\u001B[90mSkipping test due to 0x4014 write\u001B[0m");
                return true;
            }
        }

        if(test.name.equals("2a 07 56")) {
            System.out.println("This one");
        }

        BusWatcher busWatcher = new BusWatcher();

        cpu.bus.addBusDevice(busWatcher);

        //Set the CPU state to the initial state specified by the test
        applyState(cpu, test.initialState);

        //Set RAM values
        for(int[] memoryAllocation : test.initialState.memory) {
            busWatcher.setRamValue(memoryAllocation[0], (byte) memoryAllocation[1]);
        }

        //Run cycles until CPU has executed one instruction
        int cycles = 0;
        do {
            cpu.clockCycle();
            cycles++;
        } while(cpu.cycles != 0);


        //Compare the state to the final state specified by the test
        boolean success = compareState(cpu, test.finalState);

        //Compare the cycles to the cycles specified by the test
        if(cycles != test.cycles.length) {
            System.out.println("Cycle count does not match! Expected " + test.cycles.length + " but got " + cycles);
            success = false;
        }

        //Compare the RAM to the RAM specified by the test
        for(int[] memoryAllocation : test.finalState.memory) {
            byte actualValue = busWatcher.getRamValue(memoryAllocation[0]);
            byte expectedValue = (byte) memoryAllocation[1];

            if(actualValue != expectedValue) {
                System.out.println("RAM does not match! Expected " + expectedValue + " but got " + actualValue);
                success = false;
            }
        }

        //If we are ignoring the bus history, we can't compare the cycles
        if(ignoreBusHistory)
            return success;



        boolean cyclesMatch = true;
        //Compare the bus history to the cycles specified by the test
        for(int i = 0; i < Math.min(busWatcher.cycleHistory.size(), test.cycles.length); i++) {
            Cycle actualCycle = busWatcher.cycleHistory.get(i);
            Cycle expectedCycle = test.cycles[i];

            if(actualCycle.address != expectedCycle.address) {
                cyclesMatch = false;
            }
            if(actualCycle.value != expectedCycle.value) {
                cyclesMatch = false;
            }
            if(actualCycle.read != expectedCycle.read) {
                cyclesMatch = false;
            }

            if(cyclesMatch)
                continue;

            System.out.println("Cycles do not match!");
            //Print expected cycles
            System.out.println("Expected cycles:");
            System.out.println("------------------");
            for(Cycle cycle : test.cycles) {
                System.out.println(cycle);
            }
            System.out.println("------------------");
            System.out.println("Actual cycles:");
            System.out.println("------------------");
            for(Cycle cycle : busWatcher.cycleHistory) {
                System.out.println(cycle);
            }
            System.out.println("------------------");

        }





        return success;
    }

    private static void applyState(Jtx6502 cpu, CPUState state) {
        cpu.pc.set(state.pc);
        cpu.s.set(state.s);
        cpu.a.set(state.a);
        cpu.x.set(state.x);
        cpu.y.set(state.y);
        cpu.status = state.p;
    }

    //Returns true if the state matches
    private static boolean compareState(Jtx6502 cpu, CPUState state) {
        boolean match = true;

        if(cpu.pc.get() != state.pc) {
            System.out.println("PC does not match! Expected " + state.pc + " but got " + cpu.pc.get());
            match = false;
        }
        if(cpu.s.get() != state.s) {
            System.out.println("S does not match! Expected " + state.s + " but got " + cpu.s.get());
            match = false;
        }
        if(cpu.a.get() != state.a) {
            System.out.println("A does not match! Expected " + state.a + " but got " + cpu.a.get());
            match = false;
        }

        if(cpu.x.get() != state.x) {
            System.out.println("X does not match! Expected " + state.x + " but got " + cpu.x.get());
            match = false;
        }
        if(cpu.y.get() != state.y) {
            System.out.println("Y does not match! Expected " + state.y + " but got " + cpu.y.get());
            match = false;
        }
        if(cpu.status != state.p) {
            System.out.println("P does not match! Expected " + state.p + " but got " + cpu.status);
            match = false;
        }

        return match;
    }


    public static class HarteTest {
        String name;
        CPUState initialState;
        CPUState finalState;
        Cycle[] cycles;

        public HarteTest(String name, CPUState initialState, CPUState finalState, Cycle[] cycles) {
            this.name = name;
            this.initialState = initialState;
            this.finalState = finalState;
            this.cycles = cycles;
        }
    }


    public static class CPUState {
        int pc;
        int s;
        int a;
        int x;
        int y;
        int p;
        int[][] memory;

        public CPUState(int pc, int s, int a, int x, int y, int p, int[][] memory) {
            this.pc = pc;
            this.s = s;
            this.a = a;
            this.x = x;
            this.y = y;
            this.p = p;
            this.memory = memory;
        }
    }

    public static class Cycle {
        int address;
        byte value;
        boolean read;

        public Cycle(int address, byte value, boolean read) {
            this.address = address;
            this.value = value;
            this.read = read;
        }

        @Override
        public String toString() {
            return "$" + Integer.toHexString(address).toUpperCase() + " " + value + " " + (read ? "READ" : "WRITE");
        }
    }
}
