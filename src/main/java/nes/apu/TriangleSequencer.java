package nes.apu;

public class TriangleSequencer {
    //Determine whether the wave is going up or down
    private boolean upward = false;

    private int volume = 0xF;

    public void advance() {
        //The wave repeats 0 and F twice like this:
        //F E D C B A 9 8 7 6 5 4 3 2 1 0 0 1 2 3 4 5 6 7 8 9 A B C D E F
        if(upward) {
            if(volume == 0xF) {
                upward = false;
            } else {
                volume++;
            }

        } else {

            if(volume == 0) {
                upward = true;
            } else {
                volume--;
            }

        }
    }

    public int getVolume() {
        return volume;
    }
}
