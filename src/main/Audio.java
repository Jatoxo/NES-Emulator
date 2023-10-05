package main;

import main.nes.Tickable;
import main.nes.apu.APU;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.*;

public class Audio implements Tickable {


    private APU apu;

    public static final int BUFFER_SIZE = 44100 / 6 ;


    private byte[] buffer = new byte[BUFFER_SIZE];

    int bufferIndex = 0;

    SourceDataLine line;

    AudioFormat format = new AudioFormat(
            44100,
            16,//bit
            1,//channel
            true,//signed
            false //little endian
    );
    AudioFormat formatSpeed = new AudioFormat(
            44500,
            16,//bit
            1,//channel
            true,//signed
            false //little endian
    );

    public Audio(APU apu) {
        this.apu = apu;




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

        Thread audioThread = getAudioThread();



    }

    private Thread getAudioThread() {
        return new Thread(() -> {

            while(true) {

                if(line.getBufferSize() - line.available() < 5000) {
                    byte[] out = new byte[BUFFER_SIZE];

                    System.arraycopy(buffer, bufferIndex, out, 0, BUFFER_SIZE - bufferIndex);
                    System.arraycopy(buffer, 0, out, BUFFER_SIZE - bufferIndex, bufferIndex);


                    line.write(out, 0, out.length);
                }

            }
        });
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

        int buffered = line.getBufferSize() - line.available();
        if(bufferIndex >= buffer.length) {
            bufferIndex = 0;

            if(buffered > BUFFER_SIZE) {
                //This is supposed to make the audio catch up if it's too slow by skipping some samples
                line.write(buffer, 0, Math.min(line.available(), buffer.length - 1000));
            } else {
                line.write(buffer, 0, buffer.length);
            }


        }


    }
}
