package com.tearulez.dudes.server;

import com.tearulez.dudes.common.Messages;

import java.util.Optional;

class PlayerConnection {
    final int playerId;
    private Optional<Messages.MovePlayer> bufferedMoveAction = Optional.empty();
    private final int moveActionMaxTTL;
    private int moveActionTTL = 0;
    private Optional<Messages.RotatePlayer> bufferedRotationAction = Optional.empty();
    private Optional<Messages.ShootAt> bufferedShootAction = Optional.empty();
    private Optional<Messages.Reload> bufferedReloadAction = Optional.empty();

    PlayerConnection(int moveActionMaxTTL, int playerId) {
        this.playerId = playerId;
        if (moveActionMaxTTL <= 0) {
            throw new IllegalArgumentException("action TTL must be greater than zero");
        }
        this.moveActionMaxTTL = moveActionMaxTTL;
    }

    synchronized Optional<Messages.MovePlayer> moveAction() {
        if (moveActionTTL > 0) {
            moveActionTTL -= 1;
            return bufferedMoveAction;
        } else {
            return Optional.empty();
        }
    }

    synchronized void acceptMoveAction(Messages.MovePlayer moveAction) {
        bufferedMoveAction = Optional.of(moveAction);
        moveActionTTL = moveActionMaxTTL;
    }

    synchronized Optional<Messages.RotatePlayer> rotationAction() {
        Optional<Messages.RotatePlayer> action = bufferedRotationAction;
        bufferedRotationAction = Optional.empty();
        return action;
    }

    synchronized void acceptRotationAction(Messages.RotatePlayer rotationAction) {
        bufferedRotationAction = Optional.of(rotationAction);
    }

    synchronized Optional<Messages.ShootAt> shootAction() {
        Optional<Messages.ShootAt> action = bufferedShootAction;
        bufferedShootAction = Optional.empty();
        return action;
    }

    synchronized void acceptShootAction(Messages.ShootAt shootAction) {
        bufferedShootAction = Optional.of(shootAction);
    }

    synchronized void acceptReloadAction(Messages.Reload reloadAction) {
        bufferedReloadAction = Optional.of(reloadAction);
    }

    synchronized Optional<Messages.Reload> reloadAction() {
        Optional<Messages.Reload> action = bufferedReloadAction;
        bufferedReloadAction = Optional.empty();
        return action;
    }
}
