package com.tearulez.dudes.client;

import com.tearulez.dudes.common.snapshot.StateSnapshot;

public interface GameState {
    StateSnapshot snapshot();
}
