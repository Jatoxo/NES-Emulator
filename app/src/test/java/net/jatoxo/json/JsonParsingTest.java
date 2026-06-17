package net.jatoxo.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.jatoxo.CPUTest;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import net.jatoxo.CPUTest.*;



import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonParsingTest {

    @Test
    public void testJsonParsing() throws IOException {
        File file = new File(CPUTest.class.getResource("/dummytest.json").getFile());
        String testsJson = Files.readString(file.toPath());

        Gson gson = new GsonBuilder().registerTypeAdapter(
                Cycle.class, new CycleTypeAdapter()
        ).create();

        HarteTest[] tests = gson.fromJson(testsJson, HarteTest[].class);

        assertEquals(2, tests.length, "Test Count does not match");

        HarteTest harteTest = tests[0];
        HarteTest dummyTest = getDummyTest();

        System.out.println(dummyTest);


        assertEquals(dummyTest, harteTest);
    }


    private HarteTest getDummyTest() {
        int[][] expectedInitialMemory = {{0,0}, {1,1}, {2,2}};
        int[][] expectedFinalMemory = {{3,3}, {4,4}, {5,5}};
        Cycle[] expectedCycles = {
                new Cycle(0, (byte) 0,true),
                new Cycle(1, (byte) 1,false),
        };

        CPUState initialExpectedState = new CPUState(0,1,2,3,4,5, expectedInitialMemory);
        CPUState finalExpectedState = new CPUState(6,7,8,9,10,11, expectedFinalMemory);

        return new HarteTest(
                "00 00 00",
                initialExpectedState,
                finalExpectedState,
                List.of(expectedCycles)
        );
    }
}
