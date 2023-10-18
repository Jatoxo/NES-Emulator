package nes;

import nes.apu.APU;

public class MasterClock {
    public boolean paused = false;

    Jtx6502 cpu;
    int cpuStep = 12;

    PPU ppu;
    int ppuStep = 4;

    APU apu;


    Audio audio;
    int audioStep = 486;

    public MasterClock(Jtx6502 cpu, PPU ppu, APU apu, Audio audio) {
        this.cpu = cpu;
        this.ppu = ppu;
        this.apu = apu;
        this.audio = audio;
    }

    public void tick() {
        if(paused) {
            return;
        }

        if(cpuStep == 0) {
            cpuStep = 12;
            cpu.tick();
            apu.tick();
        }
        cpuStep--;


        if(ppuStep == 0) {
            ppuStep = 4;
            ppu.tick();
        }
        ppuStep--;


        if(audioStep == 0) {
            audioStep = 486;
            audio.tick();
        }
        audioStep--;

        //Todo: Potentially optimize apu by only running it when requesting the volume or before writing / reading a register
        apu.frameSequencer.tick();

    }
}
