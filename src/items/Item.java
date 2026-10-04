package items;

import entities.Player;

public abstract class Item {

    protected String name;
    protected int price;
    protected long cooldown = 60000;

    protected long lastUsedTime = 0;

    public Item(String name, int price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public boolean canUse() {

        long currentTime =
            System.currentTimeMillis();

        return currentTime - lastUsedTime >= cooldown;
    }

    public long getCooldownRemaining() {

        long remaining =
            cooldown -
            (System.currentTimeMillis() - lastUsedTime);

        if (remaining < 0) {
            return 0;
        }

        return remaining;
    }

    public boolean buy(Player player) {

        if (player.spendMoney(price)) {

            return true;
        }

        return false;
    }

    public abstract void use(Player player);
}