import java.util.*;

public class Monopoly{
    static Scanner scanner = new Scanner(System.in);
    static Random random = new Random();

    // Property class
    static class Property {
        String name;
        int price;
        int rent;
        Player owner;

        public Property(String name, int price, int rent) {
            this.name = name;
            this.price = price;
            this.rent = rent;
            this.owner = null;
        }
    }

    // Players 
    static class Player {
        String name;
        int money;
        int position;

        public Player(String name) {
            this.name = name;
            this.money = 1500;
            this.position = 0;
        }

        public boolean isBankrupt() {
            return money <= 0;
        }
    }

    static Property[] board = new Property[20];

    public static void main(String[] args) {
        setupBoard();

        Player player1 = new Player("Player 1");
        Player player2 = new Player("Player 2");

        int turn = 0;

        System.out.println("=== SIMPLE MONOPOLY ===");

        while (!player1.isBankrupt() && !player2.isBankrupt()) {
            Player current = (turn % 2 == 0) ? player1 : player2;

            System.out.println("\n" + current.name + "'s turn");
            System.out.println("Money: $" + current.money);
            System.out.println("Press ENTER to roll dice...");
            scanner.nextLine();

            int roll = rollDice();
            System.out.println("Rolled: " + roll);

            movePlayer(current, roll);
            handleTile(current);

            turn++;
        }

        if (player1.isBankrupt()) {
            System.out.println(player2.name + " wins!");
        } else {
            System.out.println(player1.name + " wins!");
        }
    }

    static void setupBoard() {
        for (int i = 0; i < board.length; i++) {
            board[i] = new Property(
                "Property " + i,
                100 + i * 20,
                20 + i * 5
            );
        }
    }

    static int rollDice() {
        return random.nextInt(6) + 1 + random.nextInt(6) + 1;
    }

    static void movePlayer(Player player, int steps) {
        player.position += steps;

        if (player.position >= board.length) {
            player.position %= board.length;
            player.money += 200;
            System.out.println(player.name + " passed GO and collected $200!");
        }

        System.out.println(player.name + " landed on " +
                board[player.position].name);
    }

    static void handleTile(Player player) {
        Property property = board[player.position];

        if (property.owner == null) {
            System.out.println("Unowned property.");
            System.out.println("Buy for $" + property.price + "? (y/n)");

            String choice = scanner.nextLine();

            if (choice.equalsIgnoreCase("y")) {
                if (player.money >= property.price) {
                    player.money -= property.price;
                    property.owner = player;
                    System.out.println("Purchased!");
                } else {
                    System.out.println("Not enough money.");
                }
            }
        } else if (property.owner != player) {
            int rent = property.rent;
            System.out.println("Owned by " + property.owner.name);
            System.out.println("Pay rent: $" + rent);

            player.money -= rent;
            property.owner.money += rent;
        } else {
            System.out.println("You own this property.");
        }

        System.out.println(player.name + " now has $" + player.money);
    }
}