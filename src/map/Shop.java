package map;

import items.TimeStopWatch;
import items.WingShoes;
import items.PowderTicket;
import items.Vaccine;
import entities.Player;

public class Shop {

    private TimeStopWatch watch;
    private WingShoes shoes;
    private PowderTicket ticket;
    private Vaccine vaccine;

    public Shop() {

        watch = new TimeStopWatch(new entities.Police[0]);
        shoes = new WingShoes();
        ticket = new PowderTicket();
        vaccine = new Vaccine();
    }

    // =========================================
    // ซื้อ Time Stop Watch
    // =========================================

    public boolean buyWatch(Player player) {

        if (watch.buy(player)) {

            player.addWatch();

            return true;
        }

        return false;
    }

    // =========================================
    // ซื้อ Wing Shoes
    // =========================================

    public boolean buyShoes(Player player) {

        if (shoes.buy(player)) {

            player.addShoes();

            return true;
        }

        return false;
    }

    // =========================================
    // ซื้อ Powder Ticket
    // =========================================

    public boolean buyTicket(Player player) {

        if (ticket.buy(player)) {

            player.addTicket();

            return true;
        }

        return false;
    }

    // =========================================
    // ซื้อ Vaccine
    // =========================================

    public boolean buyVaccine(Player player) {

        if (vaccine.buy(player)) {

            player.addVaccine();

            return true;
        }

        return false;
    }
}