package com.tearulez.dudes;

public class Assertions {
    public static void assertState(boolean mustBeTrue, String description) {
        if (!mustBeTrue) {
            throw new IllegalStateException("State invariant violated: " + description);
        }
    }
}
