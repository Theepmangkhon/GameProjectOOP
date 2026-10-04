package items;

import entities.Player;

public class Vaccine extends Item {

    public Vaccine() {

        super("Vaccine", 200);
    }

    @Override
    public void use(Player player) {

        if (!canUse()) {
            return;
        }

        player.heal(10);

        lastUsedTime =
            System.currentTimeMillis();
    }
}