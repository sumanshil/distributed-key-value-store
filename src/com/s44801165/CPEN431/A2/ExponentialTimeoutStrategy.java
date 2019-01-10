package com.s44801165.CPEN431.A2;

public class ExponentialTimeoutStrategy implements TimeoutStrategy {
    private static final int INITIAL_TIMEOUT = 100; // in milliseconds.
    private int mTimeout = INITIAL_TIMEOUT;
    
    @Override
    public int getTimeout() {
        return mTimeout;
    }

    @Override
    public void onTimedOut() {
        mTimeout *= 2;
    }

    @Override
    public void reset() {
        mTimeout = INITIAL_TIMEOUT;
    }

}