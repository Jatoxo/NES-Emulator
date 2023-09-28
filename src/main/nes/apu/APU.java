package main.nes.apu;

import main.nes.BusDevice;
import main.nes.Clock;
import main.nes.Nes;
import main.nes.Tickable;

public class APU extends BusDevice implements Tickable, Sequencer.SequencerListener {

    //Sequencer responsible for ticking other components
    private final FrameSequencer frameSequencer = new FrameSequencer();

    private final PulseChannel pulse1 = new PulseChannel(frameSequencer, 0);
    private final PulseChannel pulse2 = new PulseChannel(frameSequencer, 1);



    private Nes nes;


    //Whether interrupts are generated
    private boolean inhibitInterrupts = true; //Initial value?

    //Whether an interrupt is currently triggered
    private boolean triggerInterrupt = false;

    public volatile double audioValue = 0;


    public APU(Nes nes) {
        super(0x4000, 0x4017);

        this.nes = nes;

        frameSequencer.addListener(FrameSequencer.INTERRUPTS, this);
    }

    /**
     * Calculate the volume value at the current state
     * @return The volume level between 0 and 1
     */
    public double getVolume() {

        double pulse_out = 95.88 / ((8128 / (double) (pulse1.getVolume() + pulse2.getVolume())) + 100);

        return pulse_out;
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
        }

        pulse1.clockTimer();
        pulse2.clockTimer();

        audioValue = getVolume();

    }



    @Override
    public int read(int addr) {

        switch(addr) {
            case 0x4015:
                int result = 0;

                if(triggerInterrupt) {
                    result |= 0b0100_0000;
                }
                triggerInterrupt = false;

                boolean pulse1enabled = pulse1.lengthCounter.getCount() > 0;
                if(pulse1enabled) {
                    result |= 0b0000_0001;
                }
                boolean pulse2enabled = pulse1.lengthCounter.getCount() > 0;
                if(pulse2enabled) {
                    result |= 0b0000_0010;
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
                //Duty, loop envelope/disable length counter, constant volume, envelope period/volume
                case 0:
                    int newDividerPeriod = (data & 0xF) + 1;
                    channel.envelope.divider.setPeriod(newDividerPeriod);

                    //This flagged is both mapped to envelope loop and lengthCounter halt
                    boolean loopFlag = (data & (1 << 5)) > 0;
                    channel.envelope.loop = loopFlag;
                    channel.lengthCounter.setHalt(loopFlag);

                    boolean disableEnvelopeFlag = (data & (1 << 4)) > 0;
                    channel.envelope.disabled = disableEnvelopeFlag;

                    break;

                //Sweep unit: enabled, period, negative, shift count
                case 1:
                    channel.sweep.sweepRegWrite();

                    boolean enabled = (data & 0x80) > 0;
                    channel.sweep.setEnabled(enabled);

                    int sweepPeriod = ((data & 0x70) >>> 4) + 1;
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

            case 0x4015:
                boolean pulse1enable   = (data & 1) > 0;
                boolean pulse2enable   = (data & (1 << 1)) > 0;
                boolean triangleEnable = (data & (1 << 2)) > 0;
                boolean noiseEnable    = (data & (1 << 3)) > 0;

                pulse1.lengthCounter.setEnabled(pulse1enable);
                pulse2.lengthCounter.setEnabled(pulse2enable);



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
                frameSequencer.divider.reset();

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

    //This is just here to make the frameSequencer not public, but it needs
    //to listen to the master clock
    public void setSequencerClock(Clock clock) {
        clock.addListener(frameSequencer);
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
