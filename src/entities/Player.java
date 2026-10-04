package entities;

public class Player extends Character {

    // =========================================
    // MONEY / POWDER / SCORE
    // =========================================

    private int money;
    private int powder;
    private int score;

    // =========================================
    // ITEM COUNT
    // =========================================

    private int watchCount;
    private int shoesCount;
    private int ticketCount;
    private int vaccineCount;

    // =========================================
    // EFFECT TIME
    // =========================================

    private long wingShoesEndTime = 0;
    private long powderTicketEndTime = 0;
    private long invincibleEndTime = 0;

    // =========================================
    // CONSTRUCTOR
    // =========================================

    public Player(int x, int y) {

        super("Yusup", x, y, 100, 5);

        money = 0;
        powder = 0;
        score = 0;

        watchCount = 0;
        shoesCount = 0;
        ticketCount = 0;
        vaccineCount = 0;
    }

    // =========================================
    // UPDATE
    // =========================================

    @Override
    public void update() {
        // Player ไม่มี AI
    }

    // =========================================
    // MOVEMENT
    // =========================================

    public void move(int dx, int dy) {

        int currentSpeed = speed;

        // Wing Shoes = ความเร็ว +50%
        if (hasWingShoesEffect()) {

            currentSpeed =
                (int)(speed * 1.5);
        }

        x += dx * currentSpeed;
        y += dy * currentSpeed;
    }

    // =========================================
    // POWDER
    // =========================================

    // ถือผงได้สูงสุด 5 ชิ้น
    public static final int MAX_POWDER = 5;

    public boolean canCarryPowder() {

        return powder < MAX_POWDER;
    }

    // คืนค่า true ถ้าเก็บได้ / false ถ้าถือเต็มแล้ว
    public boolean collectPowder() {

        if (powder >= MAX_POWDER) {

            return false;
        }

        powder++;

        return true;
    }

    public boolean hasPowder() {

        return powder > 0;
    }

    public void deliverPowder() {

        if (powder > 0) {

            powder--;

            int income = 100;

            // Powder Ticket = เงิน +20%
            if (hasPowderTicket()) {

                income =
                    (int)(income * 1.20);
            }

            money += income;
        }
    }

    public int getPowder() {

        return powder;
    }

    // =========================================
    // MONEY
    // =========================================

    public int getMoney() {

        return money;
    }

    public void addMoney(int amount) {

        money += amount;
    }

    public boolean spendMoney(int amount) {

        if (money >= amount) {

            money -= amount;

            return true;
        }

        return false;
    }

    // =========================================
    // HP
    // =========================================

    public void heal(int amount) {

        hp += amount;

        if (hp > maxHp) {

            hp = maxHp;
        }
    }

    // =========================================
    // INVINCIBLE
    // =========================================

    public void setInvincible(long duration) {

        invincibleEndTime =
            System.currentTimeMillis()
            + duration;
    }

    public boolean isInvincible() {

        return System.currentTimeMillis()
            < invincibleEndTime;
    }

    public long getInvincibleRemaining() {

        long remaining =
            invincibleEndTime
            - System.currentTimeMillis();

        if (remaining < 0) {

            return 0;
        }

        return remaining;
    }

    // =========================================
    // SCORE
    // =========================================

    public void addScore(int amount) {

        score += amount;
    }

    public int getScore() {

        return score;
    }

    // =========================================
    // TIME STOP WATCH
    // =========================================

    public void addWatch() {

        watchCount++;
    }

    public boolean hasWatch() {

        return watchCount > 0;
    }

    public void useWatch() {

        if (watchCount > 0) {

            watchCount--;
        }
    }

    public int getWatchCount() {

        return watchCount;
    }

    // =========================================
    // WING SHOES
    // =========================================

    public void addShoes() {

        shoesCount++;
    }

    public boolean hasShoes() {

        return shoesCount > 0;
    }

    public int getShoesCount() {

        return shoesCount;
    }

    public void useShoes() {

        if (shoesCount > 0) {

            shoesCount--;

            activateWingShoes(10000);
        }
    }

    public void activateWingShoes(long duration) {

        wingShoesEndTime =
            System.currentTimeMillis()
            + duration;
    }

    public boolean hasWingShoesEffect() {

        return System.currentTimeMillis()
            < wingShoesEndTime;
    }

    public long getWingShoesRemaining() {

        long remaining =
            wingShoesEndTime
            - System.currentTimeMillis();

        if (remaining < 0) {

            return 0;
        }

        return remaining;
    }

    // =========================================
    // POWDER TICKET
    // =========================================

    public void addTicket() {

        ticketCount++;
    }

    public boolean hasTicket() {

        return ticketCount > 0;
    }

    public int getTicketCount() {

        return ticketCount;
    }

    public void useTicket() {

        if (ticketCount > 0) {

            ticketCount--;

            activatePowderTicket(30000);
        }
    }

    public void activatePowderTicket(long duration) {

        powderTicketEndTime =
            System.currentTimeMillis()
            + duration;
    }

    public boolean hasPowderTicket() {

        return System.currentTimeMillis()
            < powderTicketEndTime;
    }

    public long getPowderTicketRemaining() {

        long remaining =
            powderTicketEndTime
            - System.currentTimeMillis();

        if (remaining < 0) {

            return 0;
        }

        return remaining;
    }

    // =========================================
    // VACCINE
    // =========================================

    public void addVaccine() {

        vaccineCount++;
    }

    public boolean hasVaccine() {

        return vaccineCount > 0;
    }

    public int getVaccineCount() {

        return vaccineCount;
    }

    public void useVaccine() {

        if (vaccineCount > 0) {

            vaccineCount--;

            // เพิ่ม HP 10
            heal(10);
        }
    }
}