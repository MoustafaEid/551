package com.steganography.F5;

public class Permutation {
    int[] shuffled; // shuffled sequence

    // The constructor of class Permutation creates a shuffled
    // sequence of the integers 0 ... (size-1).
    public Permutation(final int size, final F5Random random) {
        int i, randomIndex, tmp;
        this.shuffled = new int[size];

        // To create the shuffled sequence, we initialise an array
        // with the integers 0 ... (size-1).
        for (i = 0; i < size; i++) {
            // initialise with �size� integers
            this.shuffled[i] = i;
        }
        int maxRandom = size; // set number of entries to shuffle
        for (i = 0; i < size; i++) { // shuffle entries
            randomIndex = random.getNextValue(maxRandom--);
            tmp = this.shuffled[randomIndex];
            this.shuffled[randomIndex] = this.shuffled[maxRandom];
            this.shuffled[maxRandom] = tmp;
        }
    }

    // get value #i from the shuffled sequence
    public int getShuffled(final int i) {
        return this.shuffled[i];
    }
}
