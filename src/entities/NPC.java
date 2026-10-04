package entities;

public class NPC extends Character {

    public NPC(String name, int x, int y) {
        super(name, x, y, 100, 0);
    }

    @Override
    public void update() {
        // NPC ไม่เดิน
    }
}