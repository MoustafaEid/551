package com.steganography.F5;

import java.security.SecureRandom;

public class F5Random {
    private SecureRandom random = null;

    private byte[] b = null;

    public F5Random(final byte[] password) {
        this.random = new SecureRandom();
        this.random.setSeed(password);
        this.b = new byte[1];
    }

    // get a random byte
    public int getNextByte() {
        this.random.nextBytes(this.b);
        return this.b[0];
    }

    // get a random integer 0 ... (maxValue-1)
    public int getNextValue(final int maxValue) {
        int retVal = getNextByte() | getNextByte() << 8 | getNextByte() << 16 | getNextByte() << 24;
        retVal %= maxValue;
        if (retVal < 0) {
            retVal += maxValue;
        }
        return retVal;
    }
}
