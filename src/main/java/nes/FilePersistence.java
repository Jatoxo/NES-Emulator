package nes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FilePersistence implements  RetainedStorage {

    private String fullPath;

    public FilePersistence(String romName) {
        //TODO: Get save location and app name from preferences instead
        String dataFolder = System.getenv("LOCALAPPDATA");
        String savePath = dataFolder + "/JatNesEmulator/";
        String saveName = romName + ".cck";
        this.fullPath = savePath + "/" + saveName;
    }


    @Override
    public void persist(byte[] data) {
        File saveFile = new File(fullPath);
        saveFile.mkdirs();

        try {
            Files.write(Path.of(fullPath), data);
            System.out.println("Wrote PRG RAM to persistent storage at " + fullPath);
        } catch (IOException e) {
            System.out.println("Error writing to persistent storage at " + fullPath);
        }

    }

    @Override
    public byte[] restore() {
        File saveFile = new File(fullPath);
        if(!saveFile.exists()) {
            System.out.println("No PRG RAM data found to restore");
            return null;
        }

        byte[] data;
        try {
            data = Files.readAllBytes(Path.of(fullPath));
        } catch (IOException e) {
            System.out.println("Failed to load PRG RAM read data");
            return null;
        }

        return data;
    }
}
