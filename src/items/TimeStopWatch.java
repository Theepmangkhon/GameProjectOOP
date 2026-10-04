package items;

import entities.Player;
import entities.Police;

public class TimeStopWatch extends Item {

    private Police[] police;

    public TimeStopWatch(Police[] police) {

        super("Time Stop Watch", 300);

        this.police = police;
    }

    @Override
    public void use(Player player) {

        if (!canUse()) {
            return;
        }

        for (Police p : police) {

            p.stun(10000);
        }

        lastUsedTime =
            System.currentTimeMillis();
    }
}