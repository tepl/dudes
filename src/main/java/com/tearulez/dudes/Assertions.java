package com.tearulez.dudes;

@SuppressWarnings("WeakerAccess")
public class Assertions {
    public static void assertState(boolean mustBeTrue, String description) {
        if (!mustBeTrue) {
            throw new IllegalStateException("State invariant violated: " + description);
        }
    }

    public static void require(boolean mustBeTrue, String description){
        if (!mustBeTrue) {
            throw new RuntimeException("requirement violated: " + description);
        }
    }
}
