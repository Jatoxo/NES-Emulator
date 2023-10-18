package nes.apu;

import java.util.*;

public class Sequencer {

    //Map of sequence ID and listeners to that ID
    private final Map<Integer, List<SequencerListener>> listeners = new HashMap<>();

    //List of sequence sets
    private final List<SequenceSet> sequenceSets = new ArrayList<>();

    //Index into the sequence set list
    private int currentSet = 0;

    //The current position in all sequences. Starting at 0
    private int currentStep = 0;

    public static class SequenceSet {
        //The amount of steps in the sequence set
        private int length;

        public SequenceSet(int length) {
            this.length = length;
        }

        //List of sequences
        List<boolean[]> sequences = new ArrayList<>();

        /**
         * Set the length of all sequences in the set. All sequences must be of the same length.
         * If not enough values exist in a sequence, the remaining steps will not output anything.
         * @param length The length of all sequences
         */
        public void setLength(int length) {
            this.length = length;
        }

        /**
         * Adds a sequence to the sequence set
         * @param sequence One bool corresponds to one step in the sequence. True means an output will be generated
         *                 When that step is reached.
         * @return The index of the sequence in the list
         */
        public int addSequence(boolean... sequence) {
            sequences.add(sequence);

            return sequences.size() - 1;
        }
    }


    public interface SequencerListener {
        /**
         * Called when the sequencer reaches an active step in the sequence
         * @param step The current step in the sequence, starting at 0
         */
        void tick(int step);
    }


    /**
     * Advance the sequence by one step, ticking the respective listeners if the sequence calls for it
     */
    public void advance() {

        //Go through all sequences in the current set
        for(int i = 0; i < getCurrentSet().sequences.size(); i++) {

            boolean[] sequence = getCurrentSet().sequences.get(i);

            //If the sequence is smaller than the current step, no ticks can be executed
            if(sequence.length <= currentStep) {
                continue;
            }

            //Continue if the sequence is not active at this step
            if(!sequence[currentStep]) {
                continue;
            }

            //Otherwise notify all listeners of this sequence
            for(SequencerListener listener : listeners.get(i)) {
                listener.tick(currentStep);
            }
        }

        //Finally, advance the current step
        currentStep++;

        //Loop around once end is reached
        currentStep %= getCurrentSet().length;

    }

    /**
     * Resets the sequencer to step 0. Does not tick listeners
     */
    public void reset() {
        currentStep = 0;
    }

    /// Get the current step in all sequences. Starts at 0
    public int getCurrentStep() {
        return currentStep;
    }

    public boolean getCurrentValue(int sequenceIndex) {
        boolean[] sequence = getCurrentSet().sequences.get(sequenceIndex);

        //If the sequence is smaller than the current step, no ticks can be executed
        if(sequence.length <= currentStep) {
            return false;

        }


        return sequence[currentStep];
    }


    /**
     * Add a new sequence set
     * @param length The length of all sequences in the sequence set
     * @return The ID of the newly created set
     */
    public int addSet(int length) {
        sequenceSets.add(new SequenceSet(length));
        return sequenceSets.size() - 1;
    }


    /**
     * Get the currently active sequence set
     */
    public SequenceSet getCurrentSet() {
        return sequenceSets.get(currentSet);
    }

    /**
     * Get the currently active sequence set ID (index)
     */
    public int getCurrentSetID() {
        return currentSet;
    }

    /**
     * Get the sequence set with the given ID. The first sequence has ID 0, the next one 1 and so on
     * @param ID the ID of the sequence to get
     * @return The sequence with the given ID
     */
    public SequenceSet getSet(int ID) {
        return sequenceSets.get(ID);
    }

    /**
     * Set the active sequence set to the one with the given ID
     * @param newSetID ID of the new set
     */
    public void switchSets(int newSetID) {
        currentSet = newSetID;
    }




    /**
     * Add a listener that will receive the output of all sequences
     * with the given ID from any set
     * @param index The index of the sequence to listen to. Starts at 0
     * @param listener The listener that will be notified when the sequence outputs
     */
    public void addListener(int index, SequencerListener listener) {
        //If the hashmap already contains a listener for that key, add this one to the list
        if(listeners.containsKey(index)) {
            listeners.get(index).add(listener);
            return;
        }

        //Otherwise we need to create the list first
        List<SequencerListener> newListenerList = new ArrayList<>();
        newListenerList.add(listener);

        listeners.put(index, newListenerList);
    }
}
