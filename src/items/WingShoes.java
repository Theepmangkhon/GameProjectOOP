package items;

import entities.Player;

public class WingShoes extends Item {

    public WingShoes() {

        super("Wing Shoes", 300);
    }

    @Override
    public void use(Player player) {

        if (!canUse()) {
            return;
        }

        player.activateWingShoes(10000);

        lastUsedTime =
            System.currentTimeMillis();
    }
}