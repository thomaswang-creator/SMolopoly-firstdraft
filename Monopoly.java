import java.util.*;

public class Monopoly{
    static Scanner scanner = new Scanner(System.in);
    static Random random = new Random();

    // Property class
    static class Property {
        String name;
        String color;
        String type;
        int price;
        int rent;
        Player owner;

        public Property(String name, String color, String type,int price, int rent) {
            this.name = name;
            this.color = color;
            this.type = type;
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
        boolean hasJailPass;

        public Player(String name) {
            this.name = name;
            this.money = 1500;
            this.position = 0;
            this.hasJailPass = false;
        }

        public boolean isBankrupt() {
            return money <= 0;
        }
    }

    static Property[] board = new Property[30];

    public static void main(String[] args) {
        setupBoard();

        Player player1 = new Player("Player 1");
        Player player2 = new Player("Player 2");

        int turn = 0;

        System.out.println("=== SIMPLE MONOPOLY ===");

        while (!player1.isBankrupt() && !player2.isBankrupt()) {
            Player current = (turn % 2 == 0) ? player1 : player2;
            Player other = (turn % 2 == 0) ? player2 : player1;

            System.out.println("\n" + current.name + "'s turn");
            System.out.println("Money: $" + current.money);
            System.out.println("Press ENTER to roll dice...");
            scanner.nextLine();

            int roll = rollDice();
            System.out.println("Rolled: " + roll);

            movePlayer(current, roll);
            handleTile(current, other);

            turn++;
        }

        if (player1.isBankrupt()) {
            System.out.println(player2.name + " wins!");
        } else {
            System.out.println(player1.name + " wins!");
        }
    }

    static void setupBoard() {
        board[0] = new Property("GO", "White", "Special", 0, 0);
        board[1] = new Property("Howard's Toilet", "Brown", "Estate", 20, 2);
        board[2] = new Property("Lost and Found", "Blue", "Special", 0, 6);
        board[3] = new Property("Sun Center's Toilet", "Brown", "Estate", 40, 6);
        board[4] = new Property("Donation", "Black", "Special", 200, 0);
        board[5] = new Property("Sale Station", "Black", "Special", 180, 15);
        board[6] = new Property("Chrothall First Floor", "Light Blue", "Estate", 200, 16);
        board[7] = new Property("Chrothall Second Floor", "Light Blue", "Estate", 210, 18);
        board[8] = new Property("Events", "Black", "Special", 0, 0);
        board[9] = new Property("Chrothall Third Floor", "Light Blue", "Estate", 220, 20);
        board[10] = new Property("Passing by Mr.Primrose's office", "White", "Special", 0, 0);
        board[11] = new Property("Cristine Duke Lecture Theatre", "Green", "Estate", 240, 22);
        board[12] = new Property("Cookie Station", "Black", "Special", 160, 20);
        board[13] = new Property("Lawn", "Green", "Estate", 260, 24);
        board[14] = new Property("Flag Pole", "Green", "Estate", 270, 25);
        board[15] = new Property("Bench", "White", "Special", 0, 0);
        board[16] = new Property("Snowden Library", "Dark Blue", "Estate", 240, 22);
        board[17] = new Property("School House", "Dark Blue", "Estate", 280, 26);
        board[18] = new Property("Events", "Black", "Special", 0, 0);
        board[19] = new Property("Math Office", "Dark Blue", "Estate", 290, 28);
        board[20] = new Property("Cookie Station", "Black", "Special", 180, 20);
        board[21] = new Property("Monkmon", "Yellow", "Estate", 300, 30);
        board[22] = new Property("Single Gym", "Yellow", "Estate", 310, 30);
        board[23] = new Property("Sale Station", "White", "Special", 200, 15);
        board[24] = new Property("Double Gym", "Yellow", "Estate", 320, 32);
        board[25] = new Property("Go to Mr.Primrose's office!!!", "Black", "Special", 0, 0);
        board[26] = new Property("Events", "Black", "Special", 0, 0);
        board[27] = new Property("Sun Center", "Orange", "Estate", 360, 40); 
        board[28] = new Property("Service day", "Black", "Special", 100, 0);
        board[29] = new Property("Howard Cafe", "Orange", "Estate", 400, 50);
        }
    
    static void LostAndFound(Player player, Player other) {
        System.out.println(player.name + "You are at the Lost and Found!");

        int event = random.nextInt(4);
        switch (event) {
            case 0:
                int amount0 = 100 + random.nextInt(201);
                player.money += amount0;
                System.out.println("A bank gift: you receive $" + amount0 + "!");
                break;
            case 1:
                int takeAmount = 50;
                int paid = Math.min(takeAmount, other.money);
                other.money -= paid;
                player.money += paid;
                System.out.println("You took $" + paid + " from " + other.name + "!");
                if (paid < takeAmount) {
                    System.out.println(other.name + " only had $" + paid + " to give.");
                }
                break;
            case 2:
                int amount2 = 150;
                player.money += amount2;
                System.out.println("You found a bonus from the bank: +$" + amount2 + "!");
                break;
            case 3:
                player.hasJailPass = true;
                System.out.println("You found a Get Out of Jail Free pass! Keep it until you need it.");
                break;
        }

        System.out.println(player.name + " now has $" + player.money + ".");
    }

    static int rollDice() {
        return random.nextInt(6) + 1;
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

    static void handleTile(Player player, Player other) {
        Property property = board[player.position];

        if (property.name.equals("Lost and Found")) {
            LostAndFound(player, other);
            return;
        }

        if (property.owner == null && property.type.equals("Estate")) {
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
        } else if (property.owner != null && property.owner != player) {
            int rent = property.rent;
            System.out.println("Owned by " + property.owner.name);
            System.out.println("Pay rent: $" + rent);

            player.money -= rent;
            property.owner.money += rent;
        } else if (property.owner == player) {
            System.out.println("You own this property.");
        } else {
            System.out.println("No action on this tile.");
        }

        System.out.println(player.name + " now has $" + player.money);
    }
    static void soundEffect(String effect) {
        System.out.println("Playing sound: " + effect);
    }
}
