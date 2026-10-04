package entities;

import java.util.Random;

public class Police extends Character {

    private int damage;

    private double detectRange = 300;
    private double chaseRange = 150;

    private int normalSpeed = 2;
    private int chaseSpeed = 5;

    private Random random =
        new Random();

    private int targetX;
    private int targetY;

    private long nextWanderTime = 0;

    private long stunEndTime = 0;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public Police(
        String name,
        int x,
        int y
    ) {

        super(
            name,
            x,
            y,
            100,
            3
        );

        damage = 20;

        chooseRandomTarget();
    }

    // =====================================================
    // UPDATE
    // =====================================================

    @Override
    public void update() {
    }

    // =====================================================
    // AI
    // =====================================================

    public void chase(Player player) {

        if (isStunned()) {

            return;
        }

        double distance =
            getDistance(player);

        // ไกล = เดินสุ่ม
        if (distance > detectRange) {

            wander();

            return;
        }

        int currentSpeed;

        // ใกล้มาก = วิ่ง
        if (distance <= chaseRange) {

            currentSpeed =
                chaseSpeed;

        } else {

            currentSpeed =
                normalSpeed;
        }

        int targetPlayerX =
            player.getX();

        int targetPlayerY =
            player.getY();

        if (x < targetPlayerX) {

            x += currentSpeed;
        }

        if (x > targetPlayerX) {

            x -= currentSpeed;
        }

        if (y < targetPlayerY) {

            y += currentSpeed;
        }

        if (y > targetPlayerY) {

            y -= currentSpeed;
        }
    }

    // =====================================================
    // RANDOM WALK
    // =====================================================

    private void wander() {

        long currentTime =
            System.currentTimeMillis();

        if (currentTime >= nextWanderTime) {

            chooseRandomTarget();
        }

        if (x < targetX) {

            x += normalSpeed;
        }

        if (x > targetX) {

            x -= normalSpeed;
        }

        if (y < targetY) {

            y += normalSpeed;
        }

        if (y > targetY) {

            y -= normalSpeed;
        }
    }

    // =====================================================
    // RANDOM TARGET
    // =====================================================

    private void chooseRandomTarget() {

        int randomX =
            random.nextInt(401) - 200;

        int randomY =
            random.nextInt(401) - 200;

        targetX =
            x + randomX;

        targetY =
            y + randomY;

        int randomTime =
            1000
            + random.nextInt(2001);

        nextWanderTime =
            System.currentTimeMillis()
            + randomTime;
    }

    // =====================================================
    // GET DISTANCE
    // =====================================================

    private double getDistance(
        Player player
    ) {

        double dx =
            player.getX() - x;

        double dy =
            player.getY() - y;

        return Math.sqrt(
            dx * dx
            + dy * dy
        );
    }

    // =====================================================
    // ATTACK
    // =====================================================

    public void attack(
        Player player
    ) {

        if (isStunned()) {

            return;
        }

        if (player.isInvincible()) {

            return;
        }

        player.takeDamage(
            damage
        );

        stunEndTime =
            System.currentTimeMillis()
            + 3000;

        player.setInvincible(
            3000
        );
    }

    // =====================================================
    // STUN
    // =====================================================

    public boolean isStunned() {

        return System.currentTimeMillis()
            < stunEndTime;
    }

    public long getStunRemaining() {

        long remaining =
            stunEndTime
            - System.currentTimeMillis();

        if (remaining < 0) {

            return 0;
        }

        return remaining;
    }

    // =====================================================
    // ITEM : TIME STOP
    // =====================================================

    public void stopFor10Seconds() {

        stunEndTime =
            System.currentTimeMillis()
            + 10000;
    }

    // =====================================================
    // COLLISION SUPPORT
    // =====================================================

    public void moveTo(
        int newX,
        int newY
    ) {

        x = newX;
        y = newY;
    }

    public void changeRandomDirection() {

        chooseRandomTarget();
    }

    // =====================================================
    // GETTERS
    // =====================================================

    public double getDetectRange() {

        return detectRange;
    }

    public double getChaseRange() {

        return chaseRange;
    }

    public int getNormalSpeed() {

        return normalSpeed;
    }

    public int getChaseSpeed() {

        return chaseSpeed;
    }

    public int getDamage() {

        return damage;
    }
    public void stun(long duration) {

    stunEndTime =
        System.currentTimeMillis()
        + duration;
}
}