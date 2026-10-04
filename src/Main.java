import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        JFrame frame =
            new JFrame();

        frame.setTitle(
            "Escape The D.E.A"
        );

        frame.setDefaultCloseOperation(
            JFrame.EXIT_ON_CLOSE
        );

        frame.add(
            new Game()
        );

        frame.pack();

        frame.setLocationRelativeTo(
            null
        );

        frame.setVisible(true);
    }
}