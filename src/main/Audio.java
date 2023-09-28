package main;

import main.nes.Tickable;
import main.nes.apu.APU;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Audio implements Tickable {


    private APU apu;

    private byte[] buffer = new byte[44100 / 6];

    int bufferIndex = 0;

    SourceDataLine line;

    public Audio(APU apu) {
        this.apu = apu;


        AudioFormat format = new AudioFormat(
                44100,
                16,//bit
                1,//channel
                true,//signed
                false //little endian
        );

        line = null;
        try {
            line = AudioSystem.getSourceDataLine(format);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
        try {
            line.open();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }

        line.start();



    }

    @Override
    public void tick() {
        double audioValue = apu.audioValue;

        int sampleValue = 0;


        sampleValue = (int) (audioValue * 0x7FFF);


        byte low = (byte) (sampleValue & 0xFF);
        byte high = (byte) ((sampleValue & 0x7F00) >>> 8);


        buffer[bufferIndex] = low;
        buffer[bufferIndex + 1] = high;
        bufferIndex += 2;

        if(bufferIndex >= buffer.length) {
            bufferIndex = 0;
            line.write(buffer, 0, buffer.length);
        }


    }
}
