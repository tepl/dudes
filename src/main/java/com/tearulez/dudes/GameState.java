package com.tearulez.dudes;

import com.tearulez.dudes.model.StateSnapshot;

public interface GameState {
    StateSnapshot snapshot();
}
