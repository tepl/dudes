package com.tearulez.dudes.com.tearulez.dudes.server.engine;

import com.badlogic.gdx.math.Vector2;
import com.tearulez.dudes.common.snapshot.Point;
import com.tearulez.dudes.server.engine.LeadCalculator;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.fail;

public class LeadCalculatorTest {

    private final float bulletSpeed = 15;
    private LeadCalculator calculator = new LeadCalculator(bulletSpeed);

    @Test
    public void shootAtTheTargetIfItIsStationary() {
        Point playerPos = Point.create(0, 0);
        Point targetPos = Point.create(0, 10);
        Vector2 targetVelocity = new Vector2(0, 0);
        testCalculator(playerPos, targetPos, targetVelocity);
    }

    @Test
    public void ortogonallyMovingTarget() {
        Point playerPos = Point.create(0, 0);
        Point targetPos = Point.create(0, 50);
        Vector2 targetVelocity = new Vector2(5, 0);
        testCalculator(playerPos, targetPos, targetVelocity);
    }

    @Test
    public void randomTest() {
        Random rnd = new Random();
        for (int i = 0; i < 1000; i++) {
            Point playerPos = randomPoint(rnd);
            Point targetPos = randomPoint(rnd);
            Vector2 targetVelocity = new Vector2(
                    (float) (rnd.nextInt(10) - 5),
                    (float) (rnd.nextInt(10) - 5)
            );
            // skip the case where the target is too close
            if (playerPos.asVector().sub(targetPos.asVector()).len() < 1.5) {
                continue;
            }

            testCalculator(playerPos, targetPos, targetVelocity);
        }
    }

    private void testCalculator(Point playerPos, Point targetPos, Vector2 targetVelocity) {
        Vector2 collisionPoint = calculator.calculateCollisionPoint(
                playerPos,
                targetPos,
                targetVelocity
        );

        float distanceToCollision = playerPos.asVector().sub(collisionPoint).len();
        float timeToCollision = distanceToCollision / bulletSpeed;

        Vector2 bulletVelocity = collisionPoint.cpy().sub(playerPos.asVector()).nor().scl(bulletSpeed);

        Vector2 bulletPositionAtCollisionTime = playerPos.asVector().add(
                bulletVelocity.cpy().scl(timeToCollision)
        );
        Vector2 targetPositionAtCollisionTime = targetPos.asVector().add(
                targetVelocity.cpy().scl(timeToCollision)
        );

        boolean success = tolerantEquals(bulletPositionAtCollisionTime, collisionPoint)
                && tolerantEquals(targetPositionAtCollisionTime, collisionPoint);
        if (!success) {
            String message = "playerPos: " + playerPos + ", targetPos: " + targetPos +
                    ", targetVelocity: " + targetVelocity;
            fail(message);
        }
    }

    private Point randomPoint(Random rnd) {
        return Point.create(rnd.nextInt(100) - 50 , rnd.nextInt(100) - 50);
    }

    private boolean tolerantEquals(Vector2 first, Vector2 second) {
        return tolerantEquals(first.x, second.x, 0.01f)
                && tolerantEquals(first.y, second.y, 0.01f);
    }

    private boolean tolerantEquals(float first, float second, float delta) {
        return Math.abs(first - second) < delta;
    }
}
