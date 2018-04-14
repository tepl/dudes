package com.tearulez.dudes.server.engine;

import com.badlogic.gdx.math.Vector2;
import com.tearulez.dudes.common.snapshot.Point;

public class LeadCalculator {
    private final float bulletSpeed;

    public LeadCalculator(float bulletSpeed) {
        this.bulletSpeed = bulletSpeed;
    }

    public Vector2 calculateCollisionPoint(Point playerPosition, Point target, Vector2 targetVelocity) {
        Vector2 targetRelativePos = target.asVector().sub(playerPosition.asVector());
        Vector2 targetAngle = targetRelativePos.cpy().nor();

        float targetVelocityProj = targetAngle.dot(targetVelocity);

        float targetVelocityOrtProjSquared = targetVelocity.len2() - targetVelocityProj * targetVelocityProj;

        float bulletVelocityProj = (float) Math.sqrt(bulletSpeed * bulletSpeed - targetVelocityOrtProjSquared);

        float timeToCollision = targetRelativePos.len() / (bulletVelocityProj - targetVelocityProj);

        return target.asVector().add(targetVelocity.cpy().scl(timeToCollision));
    }
}
