package items;

import entities.Player;

public class PowderTicket extends Item {

    public PowderTicket() {

        super("Powder Price Ticket", 400);
    }

    @Override
    public void use(Player player) {

        if (!canUse()) {
            return;
        }

        player.activatePowderTicket(30000);

        lastUsedTime =
            System.currentTimeMillis();
    }
}