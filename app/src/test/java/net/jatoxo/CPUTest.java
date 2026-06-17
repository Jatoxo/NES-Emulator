package net.jatoxo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import net.jatoxo.emu.nes.Instruction;
import net.jatoxo.emu.nes.Jtx6502;
import net.jatoxo.json.CycleTypeAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;


import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;




public class CPUTest {

    static void main() {
        CPUTest test = new CPUTest();

        test.runCpuTests();
    }


    void runCpuTests() {

        try {

            File file = new File(CPUTest.class.getResource("/TomHarteTests/").getFile());

            int totalInstructionsTested = 0;
            int passed = 0;
            for(File testFile : file.listFiles()) {

                if (!testFile.getName().endsWith(".json"))
                    continue;

                String hexop = testFile.getName().substring(0, testFile.getName().length() - 5);

                int opcode = Integer.parseInt(hexop, 16);

                Instruction testedInstruction = Instruction.fromOpCode(opcode);
                if(testedInstruction.mnemonic.equals("???")) {

                    System.out.println("WWWEEE WOOO");
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
            
            if(passed != totalInstructionsTested) {
                System.exit(1);
            }


        } catch (IOException e) {
            System.exit(1);
            throw new RuntimeException(e);
        }
    }

    private static Stream<Arguments> provideHarteTests() {
        File testFolder = new File(CPUTest.class.getResource("/TomHarteTests/").getFile());
        File[] files = testFolder.listFiles();

        if (files == null) return Stream.empty();

        return Arrays.stream(files)
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("provideHarteTests")
    public void testHarteTestFile(File testFile) throws IOException {
        if (!testFile.getName().endsWith(".json")) {
            System.out.println("Not a valid test file: " + testFile.getName());
            return;
        }

        String hexop = testFile.getName().substring(0, testFile.getName().length() - 5);
        int opcode = Integer.parseInt(hexop, 16);

        Instruction testedInstruction = Instruction.fromOpCode(opcode);
        if(testedInstruction.mnemonic.equals("???")) {
            System.out.println("Opcode unknown");
            return;
        }

        System.out.println("Running " + testedInstruction.mnemonic + " tests (" + testFile.getName() + ")");

        String testsJson = Files.readString(testFile.toPath());

        runJsonTests(testsJson);
    }


    // Runs all the tests for one of the instructions
    private static boolean runJsonTests(String testsJson) {
        Gson gson = new GsonBuilder().registerTypeAdapter(
                Cycle.class, new CycleTypeAdapter()
        ).create();

        HarteTest[] tests = gson.fromJson(testsJson, HarteTest[].class);

        for(HarteTest test : tests) {
            boolean success = runTest(test, false);

            if(!success) {
                System.out.println("\u001B[31mTest " + test.name + " failed!\u001B[0m");
                return false;
            }
        }

        return true;
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
        //assertTrue(success, "CPU State does not match");

        //Compare the cycles to the cycles specified by the test
        if(cycles != test.cycles.size()) {
            System.out.println("Cycle count does not match! Expected " + test.cycles.size() + " but got " + cycles);
            success = false;
        }
        //assertTrue(success, "Cycle count does not match");

        //Compare the RAM to the RAM specified by the test
        for(int[] memoryAllocation : test.finalState.memory) {
            byte actualValue = busWatcher.getRamValue(memoryAllocation[0]);
            byte expectedValue = (byte) memoryAllocation[1];

            if(actualValue != expectedValue) {
                System.out.println("RAM does not match! Expected " + expectedValue + " but got " + actualValue);
                success = false;
            }
        }
        //assertTrue(success, "RAM does not match");


        //If we are ignoring the bus history, we can't compare the cycles
        if(ignoreBusHistory)
            return success;



        boolean cyclesMatch = true;
        //Compare the bus history to the cycles specified by the test
        for(int i = 0; i < Math.min(busWatcher.cycleHistory.size(), test.cycles.size()); i++) {
            Cycle actualCycle = busWatcher.cycleHistory.get(i);
            Cycle expectedCycle = test.cycles.get(i);

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



        //assertTrue(success, "Cycles do not match");

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


    public record HarteTest(
        String name,
        @SerializedName("initial") CPUState initialState,
        @SerializedName("final")   CPUState finalState,
        List<Cycle> cycles
    ) {}


    public record CPUState(
        int pc,
        int s, // Stack pointer
        int a,
        int x,
        int y,
        int p, // Status register
        @SerializedName("ram") int[][] memory
    ) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CPUState(int pc1, int s1, int a1, int x1, int y1, int p1, int[][] memory1))) return false;

            return Objects.equals(pc, pc1)
                    && Objects.equals(s, s1)
                    && Objects.equals(a, a1)
                    && Objects.equals(x, x1)
                    && Objects.equals(y, y1)
                    && Objects.equals(p, p1)
                    && Arrays.deepEquals(memory, memory1);
        }


        @Override
        public String toString() {
            return "CPUState [pc=" +
                    pc + " " +
                    String.format("s=%02x ", s).toUpperCase() +
                    String.format("a=%02x ", a).toUpperCase() +
                    String.format("x=%02x ", x).toUpperCase() +
                    String.format("y=%02x ", y).toUpperCase() +
                    String.format("p=%02x, ", p).toUpperCase() +
                    String.format("memory=%s", Arrays.deepToString(memory));
        }
    }

    public record Cycle(
        int address,
        byte value,
        boolean read
    ) {

        @Override
        public String toString() {
            return "$" + Integer.toHexString(address).toUpperCase() + " " + value + " " + (read ? "READ" : "WRITE");
        }
    }
}
