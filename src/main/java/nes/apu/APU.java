package nes.apu;

import nes.BusDevice;
import nes.Nes;
import nes.Tickable;

public class APU extends BusDevice implements Tickable, Sequencer.SequencerListener {

    //Sequencer responsible for ticking other components
    public final FrameSequencer frameSequencer = new FrameSequencer();

    private final PulseChannel pulse1 = new PulseChannel(frameSequencer, 0);
    private final PulseChannel pulse2 = new PulseChannel(frameSequencer, 1);
    private final TriangleChannel triangle = new TriangleChannel(frameSequencer);
    private final NoiseChannel noise = new NoiseChannel(frameSequencer);



    private final Nes nes;


    //Whether interrupts are generated
    private boolean inhibitInterrupts = true; //Initial value?

    //Whether an interrupt is currently triggered
    private boolean triggerInterrupt = false;

    public volatile double audioValue = 0;


    public APU(Nes nes) {
        super(0x4000, 0x4017);

        this.nes = nes;

        //Listen to the IRQ sequence to trigger IRQs with the CPU
        frameSequencer.addListener(FrameSequencer.INTERRUPTS, this);
    }

    /**
     * Calculate the volume value at the current state
     * @return The volume level between 0 and 1
     */
    public double getVolume() {

        double pulse_out = 95.88 / ((8128 / (double) (pulse1.getVolume() + pulse2.getVolume())) + 100);

        double tnd_out =
                159.79 / (1 / ((triangle.getVolume() / 8227.0) + (noise.getVolume()/12241.0) + (0)) + 100);


        return pulse_out + tnd_out;
    }

    /**
     * Called for every cpu cycle
     */
    @Override
    public void tick() {
        //This is really hacky but probably should work?
        //I need a proper way to trigger IRQs whenever the line is high
        //For now just call the function as much as possible when needed lol
        if(triggerInterrupt) {
            nes.cpu.raiseIRQ();
            triggerInterrupt = false;
        }

        pulse1.clockTimer();
        pulse2.clockTimer();
        triangle.clockTimer();
        noise.clockTimer();


        //audioValue = getVolume();

    }



    @Override
    public int read(int addr) {

        if(addr == 0x4015) {
            int result = 0;

            if (triggerInterrupt) {
                result |= 0b0100_0000;
            }
            triggerInterrupt = false;

            boolean pulse1enabled = pulse1.lengthCounter.getCount() > 0;
            boolean pulse2enabled = pulse1.lengthCounter.getCount() > 0;
            boolean triangleEnabled = triangle.lengthCounter.getCount() > 0;
            boolean noiseEnabled = noise.lengthCounter.getCount() > 0;

            if (pulse1enabled) {
                result |= 0b0000_0001;
            }
            if (pulse2enabled) {
                result |= 0b0000_0010;
            }
            if (triangleEnabled) {
                result |= 0b0000_0100;
            }
            if (noiseEnabled) {
                result |= 0b0000_1000;
            }

            return result;
        }
        return 0;
    }

    @Override
    public void write(int addr, int data) {
        //System.out.println(Integer.toHexString(addr));

        //0x4000 - 0x4007 Pulse 1 & Pulse 2
        if(addr <= 0x4007) {
            //The registers for the two pulse channels are the same, so select the right one here
            PulseChannel channel = addr <= 0x4003 ? pulse1 : pulse2;
            //Then mask out the bits
            addr &= 0x3;

            switch(addr) {
                //Duty, loop envelope/halt length counter, constant volume, envelope period/volume
                case 0:
                    int duty = (data & 0xC0) >>> 6;
                    channel.dutySequencer.switchSequence(duty);

                    int newDividerPeriod = (data & 0xF);
                    channel.envelope.divider.setPeriod(newDividerPeriod);

                    //This flag is both mapped to envelope loop and lengthCounter halt
                    boolean loopFlag = (data & (1 << 5)) > 0;
                    channel.envelope.loop = loopFlag;
                    channel.lengthCounter.setHalt(loopFlag);

                    boolean disableEnvelopeFlag = (data & (1 << 4)) > 0;
                    channel.envelope.disabled = disableEnvelopeFlag;

                    break;

                //Sweep unit:
                // eppp nsss - enabled, period, negative, shift count
                case 1:
                    channel.sweep.sweepRegWrite();

                    boolean enabled = (data & 0x80) > 0;
                    channel.sweep.setEnabled(enabled);

                    int sweepPeriod = ((data & 0x70) >>> 4);
                    channel.sweep.divider.setPeriod(sweepPeriod);

                    boolean negate = (data & 0x8) > 0;
                    channel.sweep.setNegate(negate);

                    int shiftCount = (data & 0x7);
                    channel.sweep.setShiftCount(shiftCount);


                    break;

                //Timer low
                case 2:
                    //Clear low byte
                    int period = channel.timerDivider.getPeriod();
                    period &= 0xFF00;
                    //Set low byte
                    period |= data & 0xFF;
                    channel.timerDivider.setPeriod(period);

                    break;

                //Length counter load, timer high (also resets duty and starts envelope)
                case 3:
                    channel.envelope.reset = true;
                    channel.dutySequencer.reset();

                    //Clear high byte
                    int period2 = channel.timerDivider.getPeriod();
                    period2 &= 0xFF;
                    //Set high 3 bits
                    period2 |= (data & 0x7) << 8;
                    channel.timerDivider.setPeriod(period2);

                    //Load into length counter
                    if(channel.lengthCounter.isEnabled()) {
                        //Upper 5 bits contain index into table
                        int index = data & 0xF8;
                        channel.lengthCounter.reloadWithIndex(index >>> 3);

                    }

                    break;
            }

            return;
        }

        switch(addr) {
            //Triangle channel

            //crrr rrrr   control flag/length counter halt, reload value
            case 0x4008:
                boolean halt = (data & (1 << 7)) > 0;
                triangle.lengthCounter.setHalt(halt);
                triangle.linearCounter.controlFlag = halt;

                int reloadValue = data & 0b0111_1111;
                triangle.linearCounter.setReloadValue(reloadValue);
                break;

            //Timer low 8 bits
            case 0x400A:
                //Clear low byte
                int period = triangle.timerDivider.getPeriod();
                period &= 0xFF00;
                //Set low byte
                period |= data & 0xFF;
                triangle.timerDivider.setPeriod(period);

                break;

            //Length counter load, timer high
            case 0x400B:
                //Writing to this register sets the linear counter's halt flag
                triangle.linearCounter.setHalt(true);

                //Set timer period
                //Clear high byte
                int period2 = triangle.timerDivider.getPeriod();
                period2 &= 0xFF;
                //Set high 3 bits
                period2 |= (data & 0x7) << 8;
                triangle.timerDivider.setPeriod(period2);

                //Load into length counter
                if(triangle.lengthCounter.isEnabled()) {
                    //Upper 5 bits contain index into table
                    int index = data & 0b1111_1000;
                    triangle.lengthCounter.reloadWithIndex(index >>> 3);
                }

                break;

            //Noise channel
            // --lc vvvv   loop, constant volume, volume/envelope period
            case 0x400C:
                //Lower 4 bits are the envelope period
                int newDividerPeriod = (data & 0xF);
                noise.envelope.divider.setPeriod(newDividerPeriod);

                //This flag is both mapped to envelope loop and lengthCounter halt
                boolean loopFlag = (data & 0b0010_0000) > 0;
                noise.envelope.loop = loopFlag;
                noise.lengthCounter.setHalt(loopFlag);

                boolean disableEnvelopeFlag = (data & (1 << 4)) > 0;
                noise.envelope.disabled = disableEnvelopeFlag;

                break;

            //Random mode select, timer period index
            case 0x400E:
                int mode = (data & 0b1000_0000) >>> 7;
                noise.setMode(mode);

                int periodIndex = data & 0b1111;
                noise.setPeriodByIndex(periodIndex);

                break;
            case 0x400F:
                //Write to channels 4th register resets the envelope
                noise.envelope.reset = true;

                //Length counter load
                if(noise.lengthCounter.isEnabled()) {
                    //Upper 5 bits contain index into table
                    int index = (data & 0b1111_1000) >>> 3;
                    noise.lengthCounter.reloadWithIndex(index);
                }

                break;

            case 0x4015:
                boolean pulse1enable   = (data & 1) > 0;
                boolean pulse2enable   = (data & (1 << 1)) > 0;
                boolean triangleEnable = (data & (1 << 2)) > 0;
                boolean noiseEnable    = (data & (1 << 3)) > 0;

                pulse1.lengthCounter.setEnabled(pulse1enable);
                pulse2.lengthCounter.setEnabled(pulse2enable);
                triangle.lengthCounter.setEnabled(triangleEnable);
                noise.lengthCounter.setEnabled(noiseEnable);



                break;



            //Frame Counter settings
            //SD-- ----
            //5-frame sequence, disable frame interrupt
            case 0x4017:
                //On a write to $4017, the divider and sequencer are reset, then the sequencer is
                //configured. Two sequences are available, and frame IRQ generation can be
                //disabled.

                //Reset divider and sequencer
                frameSequencer.reset();

                inhibitInterrupts = (data & (1<<6)) > 0;

                if(inhibitInterrupts) {
                    triggerInterrupt = false;
                }

                int sequencerMode = data >>> 7;
                frameSequencer.switchSets(sequencerMode);

                //If the mode flag is clear, the 4-step sequence is selected, otherwise the
                //5-step sequence is selected and the sequencer is immediately clocked once.
                if(sequencerMode == 1) {
                    frameSequencer.advance();
                }


                break;

        }

    }


    //Hooked up to the IRQ signal
    @Override
    public void tick(int step) {
        if(inhibitInterrupts) {
            return;
        }

        triggerInterrupt = true;
        nes.cpu.raiseIRQ();
    }
}
