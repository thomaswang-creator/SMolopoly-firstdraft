import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class SMonopoly {
    // CHANGE GAME NAME HERE
    static final String GAME_NAME = "SMonopoly";

    static final int STARTING_MONEY = 1500;
    static final int PASS_GO_MONEY = 200;
    static final int MAX_ROUNDS = 20;
    static final int MAX_PLAYERS = 3;

    // CHANGE PUNISHMENT VERBAL MESSAGES HERE
    // These show up when a player lands on an Events space.
    static final String[] PUNISHMENT_MESSAGES = {
            "You forgot your homework. Pay $50.",
            "You were late to class. Pay $40.",
            "You lost your student card. Pay $30.",
            "You helped clean up after school. Collect $60.",
            "You won a small school prize. Collect $80."
    };

    static Random random = new Random();
    static Property[] board = new Property[30];
    static Player[] players;

    static int currentPlayerIndex = 0;
    static int roundNumber = 1;
    static boolean gameOver = false;

    static JFrame frame;
    static JButton rollButton;
    static JButton newGameButton;
    static JButton[] tileButtons;
    static JLabel turnLabel;
    static JLabel roundLabel;
    static JLabel[] playerLabels;
    static JTextArea logArea;

    static class Property {
        String name;
        String color;
        String type;
        int price;
        int rent;
        Player owner;

        public Property(String name, String color, String type, int price, int rent) {
            this.name = name;
            this.color = color;
            this.type = type;
            this.price = price;
            this.rent = rent;
            this.owner = null;
        }
    }

    static class Player {
        String name;
        int money;
        int position;

        public Player(String name) {
            this.name = name;
            this.money = STARTING_MONEY;
            this.position = 0;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setupBoard();
                setupPlayers();
                createWindow();
                updateScreen();
                addLog("Welcome to " + GAME_NAME + "!");
                addLog("The richest player after " + MAX_ROUNDS + " rounds wins.");
            }
        });
    }

    static void setupPlayers() {
        String answer = JOptionPane.showInputDialog(null,
                "How many players? Enter 2 or 3:",
                GAME_NAME,
                JOptionPane.QUESTION_MESSAGE);

        int playerCount = 2;
        try {
            playerCount = Integer.parseInt(answer);
        } catch (Exception e) {
            playerCount = 2;
        }

        if (playerCount < 2) {
            playerCount = 2;
        }
        if (playerCount > MAX_PLAYERS) {
            playerCount = MAX_PLAYERS;
        }

        players = new Player[playerCount];
        for (int i = 0; i < players.length; i++) {
            String name = JOptionPane.showInputDialog(null,
                    "Name for Player " + (i + 1) + ":",
                    GAME_NAME,
                    JOptionPane.QUESTION_MESSAGE);

            if (name == null || name.trim().equals("")) {
                name = "Player " + (i + 1);
            }
            players[i] = new Player(name.trim());
        }
    }

    static void setupBoard() {
        // CHANGE PROPERTY NAMES HERE
        // Format: new Property("Name", "Color", "Type", price, rent)
        // Type can be "Estate", "Special", "Event", "Tax", or "Go To Office".
        board[0] = new Property("GO", "White", "Special", 0, 0);
        board[1] = new Property("Howard's Toilet", "Brown", "Estate", 20, 2);
        board[2] = new Property("Lost and Found", "Blue", "Special", 0, 6);
        board[3] = new Property("Sun Center's Toilet", "Brown", "Estate", 40, 6);
        board[4] = new Property("Donation", "Black", "Tax", 200, 0);
        board[5] = new Property("Sale Station", "Black", "Estate", 180, 15);
        board[6] = new Property("Chrothall First Floor", "Light Blue", "Estate", 200, 16);
        board[7] = new Property("Chrothall Second Floor", "Light Blue", "Estate", 210, 18);
        board[8] = new Property("Events", "Black", "Event", 0, 0);
        board[9] = new Property("Chrothall Third Floor", "Light Blue", "Estate", 220, 20);
        board[10] = new Property("Mr. Primrose's Office", "White", "Special", 0, 0);
        board[11] = new Property("Cristine Duke Lecture Theatre", "Green", "Estate", 240, 22);
        board[12] = new Property("Cookie Station", "Black", "Estate", 160, 20);
        board[13] = new Property("Lawn", "Green", "Estate", 260, 24);
        board[14] = new Property("Flag Pole", "Green", "Estate", 270, 25);
        board[15] = new Property("Bench", "White", "Special", 0, 0);
        board[16] = new Property("Snowden Library", "Dark Blue", "Estate", 240, 22);
        board[17] = new Property("School House", "Dark Blue", "Estate", 280, 26);
        board[18] = new Property("Events", "Black", "Event", 0, 0);
        board[19] = new Property("Math Office", "Dark Blue", "Estate", 290, 28);
        board[20] = new Property("Cookie Station 2", "Black", "Estate", 180, 20);
        board[21] = new Property("Monkmon", "Yellow", "Estate", 300, 30);
        board[22] = new Property("Single Gym", "Yellow", "Estate", 310, 30);
        board[23] = new Property("Sale Station 2", "White", "Estate", 200, 15);
        board[24] = new Property("Double Gym", "Yellow", "Estate", 320, 32);
        board[25] = new Property("Go to Mr. Primrose's Office!", "Black", "Go To Office", 0, 0);
        board[26] = new Property("Events", "Black", "Event", 0, 0);
        board[27] = new Property("Sun Center", "Orange", "Estate", 360, 40);
        board[28] = new Property("Service Day", "Black", "Tax", 100, 0);
        board[29] = new Property("Howard Cafe", "Orange", "Estate", 400, 50);
    }

    static void createWindow() {
        frame = new JFrame(GAME_NAME);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLayout(new BorderLayout(10, 10));

        JPanel boardPanel = new JPanel(new GridLayout(5, 6, 4, 4));
        tileButtons = new JButton[board.length];

        for (int i = 0; i < board.length; i++) {
            tileButtons[i] = new JButton();
            tileButtons[i].setFocusPainted(false);
            tileButtons[i].setFont(new Font("Arial", Font.PLAIN, 11));
            tileButtons[i].setBackground(getTileColor(board[i].color));
            boardPanel.add(tileButtons[i]);
        }

        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BorderLayout(8, 8));
        sidePanel.setPreferredSize(new Dimension(300, 600));

        JPanel topInfoPanel = new JPanel(new GridLayout(0, 1, 3, 3));
        turnLabel = new JLabel();
        roundLabel = new JLabel();
        topInfoPanel.add(turnLabel);
        topInfoPanel.add(roundLabel);

        playerLabels = new JLabel[MAX_PLAYERS];
        for (int i = 0; i < playerLabels.length; i++) {
            playerLabels[i] = new JLabel();
            topInfoPanel.add(playerLabels[i]);
        }

        rollButton = new JButton("Roll Dice");
        rollButton.addActionListener(e -> takeTurn());

        newGameButton = new JButton("New Game");
        newGameButton.addActionListener(e -> restartGame());

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        buttonPanel.add(rollButton);
        buttonPanel.add(newGameButton);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        sidePanel.add(topInfoPanel, BorderLayout.NORTH);
        sidePanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        sidePanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(boardPanel, BorderLayout.CENTER);
        frame.add(sidePanel, BorderLayout.EAST);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    static void takeTurn() {
        if (gameOver) {
            return;
        }

        Player currentPlayer = players[currentPlayerIndex];
        int roll = rollDice();
        addLog("");
        addLog(currentPlayer.name + " rolled a " + roll + ".");

        movePlayer(currentPlayer, roll);
        handleTile(currentPlayer);

        if (currentPlayer.money < 0) {
            addLog(currentPlayer.name + " is below $0, but the game continues until round " + MAX_ROUNDS + ".");
        }

        goToNextPlayer();
        updateScreen();

        if (roundNumber > MAX_ROUNDS) {
            endGame();
        }
    }

    static int rollDice() {
        return random.nextInt(6) + 1;
    }

    static void movePlayer(Player player, int steps) {
        int oldPosition = player.position;
        player.position = player.position + steps;

        if (player.position >= board.length) {
            player.position = player.position % board.length;
            player.money = player.money + PASS_GO_MONEY;
            addLog(player.name + " passed GO and collected $" + PASS_GO_MONEY + ".");
        }

        addLog(player.name + " moved from " + board[oldPosition].name + " to " + board[player.position].name + ".");
    }

    static void handleTile(Player player) {
        Property property = board[player.position];

        if (property.type.equals("Estate")) {
            handleEstate(player, property);
        } else if (property.type.equals("Tax")) {
            player.money = player.money - property.price;
            addLog(player.name + " paid $" + property.price + " for " + property.name + ".");
        } else if (property.type.equals("Event")) {
            handleEvent(player);
        } else if (property.type.equals("Go To Office")) {
            player.position = 10;
            player.money = player.money - 100;
            addLog(player.name + " went to Mr. Primrose's Office and paid $100.");
        } else {
            addLog(player.name + " is resting on " + property.name + ".");
        }
    }

    static void handleEstate(Player player, Property property) {
        if (property.owner == null) {
            int choice = JOptionPane.showConfirmDialog(frame,
                    player.name + ", buy " + property.name + " for $" + property.price + "?",
                    "Buy Property",
                    JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                if (player.money >= property.price) {
                    player.money = player.money - property.price;
                    property.owner = player;
                    addLog(player.name + " bought " + property.name + ".");
                } else {
                    addLog(player.name + " does not have enough money to buy " + property.name + ".");
                }
            } else {
                addLog(player.name + " did not buy " + property.name + ".");
            }
        } else if (property.owner == player) {
            addLog(player.name + " owns this property already.");
        } else {
            player.money = player.money - property.rent;
            property.owner.money = property.owner.money + property.rent;
            addLog(player.name + " paid $" + property.rent + " rent to " + property.owner.name + ".");
        }
    }

    static void handleEvent(Player player) {
        int eventNumber = random.nextInt(PUNISHMENT_MESSAGES.length);
        String message = PUNISHMENT_MESSAGES[eventNumber];
        addLog(message);

        if (eventNumber == 0) {
            player.money = player.money - 50;
        } else if (eventNumber == 1) {
            player.money = player.money - 40;
        } else if (eventNumber == 2) {
            player.money = player.money - 30;
        } else if (eventNumber == 3) {
            player.money = player.money + 60;
        } else if (eventNumber == 4) {
            player.money = player.money + 80;
        }
    }

    static void goToNextPlayer() {
        currentPlayerIndex++;
        if (currentPlayerIndex >= players.length) {
            currentPlayerIndex = 0;
            roundNumber++;
        }
    }

    static void endGame() {
        gameOver = true;
        rollButton.setEnabled(false);

        Player winner = players[0];
        for (int i = 1; i < players.length; i++) {
            if (players[i].money > winner.money) {
                winner = players[i];
            }
        }

        addLog("");
        addLog("Game over! The richest player is " + winner.name + " with $" + winner.money + ".");
        JOptionPane.showMessageDialog(frame,
                winner.name + " wins " + GAME_NAME + " with $" + winner.money + "!",
                "Game Over",
                JOptionPane.INFORMATION_MESSAGE);
    }

    static void restartGame() {
        setupBoard();
        setupPlayers();
        currentPlayerIndex = 0;
        roundNumber = 1;
        gameOver = false;
        rollButton.setEnabled(true);
        logArea.setText("");
        addLog("New game started.");
        updateScreen();
    }

    static void updateScreen() {
        frame.setTitle(GAME_NAME + " - Round " + Math.min(roundNumber, MAX_ROUNDS) + " of " + MAX_ROUNDS);
        turnLabel.setText("Turn: " + players[currentPlayerIndex].name);
        roundLabel.setText("Round: " + Math.min(roundNumber, MAX_ROUNDS) + " / " + MAX_ROUNDS);

        for (int i = 0; i < playerLabels.length; i++) {
            if (i < players.length) {
                Player player = players[i];
                playerLabels[i].setText(player.name + ": $" + player.money + " | Space " + player.position);
                playerLabels[i].setVisible(true);
            } else {
                playerLabels[i].setVisible(false);
            }
        }

        for (int i = 0; i < tileButtons.length; i++) {
            Property property = board[i];
            String text = "<html><center>" + i + ": " + property.name + getPlayersOnTileText(i);

            if (property.owner != null) {
                text = text + "<br>Owner: " + property.owner.name;
            } else if (property.type.equals("Estate")) {
                text = text + "<br>$" + property.price + " / Rent $" + property.rent;
            }

            text = text + "</center></html>";
            tileButtons[i].setText(text);
            tileButtons[i].setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
            tileButtons[i].setBackground(getTileColor(property.color));
        }
    }

    static String getPlayersOnTileText(int tileNumber) {
        String text = "";
        for (int i = 0; i < players.length; i++) {
            if (players[i].position == tileNumber) {
                text = text + "<br>" + players[i].name;
            }
        }
        return text;
    }

    static Color getTileColor(String colorName) {
        if (colorName.equals("Brown")) {
            return new Color(176, 122, 74);
        } else if (colorName.equals("Light Blue")) {
            return new Color(150, 210, 255);
        } else if (colorName.equals("Green")) {
            return new Color(130, 210, 140);
        } else if (colorName.equals("Dark Blue")) {
            return new Color(100, 150, 230);
        } else if (colorName.equals("Yellow")) {
            return new Color(245, 220, 90);
        } else if (colorName.equals("Orange")) {
            return new Color(245, 175, 95);
        } else if (colorName.equals("Black")) {
            return new Color(210, 210, 210);
        } else if (colorName.equals("Blue")) {
            return new Color(170, 200, 255);
        } else {
            return Color.WHITE;
        }
    }

    static void addLog(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
