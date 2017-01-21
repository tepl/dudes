package com.tearulez.dudes.server;

import com.esotericsoftware.kryonet.Connection;
import com.tearulez.dudes.Network;

import java.util.Optional;

class PlayerConnection extends Connection {
    volatile int playerId;
    private Optional<Network.MovePlayer> bufferedMoveAction = Optional.empty();
    private final int moveActionMaxTTL;
    private int moveActionTTL = 0;
    private Optional<Network.ShootAt> bufferedShootAction = Optional.empty();

    PlayerConnection(int moveActionMaxTTL) {
        if (moveActionMaxTTL <= 0) {
            throw new IllegalArgumentException("action TTL must be greater than zero");
        }
        this.moveActionMaxTTL = moveActionMaxTTL;
    }

    synchronized Optional<Network.MovePlayer> moveAction() {
        if (moveActionTTL > 0) {
            moveActionTTL -= 1;
            return bufferedMoveAction;
        } else {
            return Optional.empty();
        }
    }

    synchronized void acceptMoveAction(Network.MovePlayer moveAction) {
        bufferedMoveAction = Optional.of(moveAction);
        moveActionTTL = moveActionMaxTTL;
    }

    synchronized Optional<Network.ShootAt> shootAction() {
        Optional<Network.ShootAt> action = bufferedShootAction;
        bufferedShootAction = Optional.empty();
        return action;
    }

    synchronized void acceptShootAction(Network.ShootAt shootAction) {
        bufferedShootAction = Optional.of(shootAction);
    }
}
