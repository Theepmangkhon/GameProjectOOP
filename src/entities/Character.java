package entities;

public abstract class Character {

    protected String name;

    protected int x;
    protected int y;

    protected int hp;
    protected int maxHp;

    protected int speed;

    public Character(String name, int x, int y, int hp, int speed) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.hp = hp;
        this.maxHp = hp;
        this.speed = speed;
    }

    public abstract void update();

    public void takeDamage(int damage) {
        hp -= damage;

        if (hp < 0) {
            hp = 0;
        }
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getSpeed() {
        return speed;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}