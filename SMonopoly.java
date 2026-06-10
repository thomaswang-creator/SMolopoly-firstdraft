import javax.swing.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.*;
import java.util.Random;

public class SMonopoly {
    // CHANGE GAME NAME HERE
    static final String GAME_NAME = "SMonopoly";

    static final int STARTING_MONEY = 1500;
    static final int PASS_GO_MONEY = 200;
    static final int MAX_ROUNDS = 30;
    static final int MAX_PLAYERS = 3;
    static final String JAIL_SOUND = "assets/jail-sound-effect.wav";
    static final String[] PLAYER_TOKEN_IMAGES = {
            "assets/player1-token.png",
            "assets/player2-token.png",
            "assets/player3-token.png"
    };
    static final int[] PLAYER_TOKEN_WIDTHS = {28, 58, 30};
    static final int[] PLAYER_TOKEN_HEIGHTS = {35, 52, 30};

    // CHANGE EVENT MESSAGES HERE
    // These show up when a player lands on an Events space.
    static final String[] EVENT_MESSAGES = {
            "You forgot your homework. Pay $50.",
            "You were late to class. Pay $40.",
            "You lost your student card. Pay $30.",
            "You helped clean up after school. Collect $60.",
            "You won a small school prize. Collect $80.",
            "You found a shortcut to Chrothall First Floor. Move there.",
            "A surprise reward sends you back to GO.",
            "A friend invited you to Cookie Station. Move there."
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
    static JTextArea boardInfoArea;
    static JLabel boardImageLabel;
    static JLabel landingBannerLabel;  // banner shown when a player lands on a property
    static int lastLandedTile = -1;    // tracks the most recently landed-on tile
    static Clip bgmClip;
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
        boolean detentionPass;
        boolean inDetention;
        int turnsInDetention;
        boolean isBankrupt;

        public Player(String name) {
            this.name = name;
            this.money = STARTING_MONEY;
            this.position = 0;
            this.detentionPass = false;
            this.inDetention = false;
            this.turnsInDetention = 0;
            this.isBankrupt = false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setupBoard();
                if (!setupPlayers()) {
                    System.exit(0);
                    return;
                }
                playBackgroundMusic();
                createWindow();
                updateScreen();
                
                addLog("Welcome to " + GAME_NAME + "!");
                addLog("The richest player after " + MAX_ROUNDS + " turns wins.");
            }
        });
    }

    static boolean setupPlayers() {
        int playerCount = -1;
        int playerIndex = 0;

        while (true) {
            if (playerCount != 2 && playerCount != 3) {
                String answer = JOptionPane.showInputDialog(null,
                        "How many players? Enter 2 or 3:",
                        GAME_NAME,
                        JOptionPane.QUESTION_MESSAGE);

                if (answer == null) {
                    return false;
                }

                try {
                    playerCount = Integer.parseInt(answer.trim());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(frame,
                            "Please enter a valid number of players: 2 or 3.",
                            GAME_NAME,
                            JOptionPane.WARNING_MESSAGE);
                    continue;
                }

                if (playerCount < 2 || playerCount > MAX_PLAYERS) {
                    JOptionPane.showMessageDialog(frame,
                            "Please enter 2 or 3 players.",
                            GAME_NAME,
                            JOptionPane.WARNING_MESSAGE);
                    playerCount = -1;
                    continue;
                }

                players = new Player[playerCount];
                playerIndex = 0;
            }

            String prompt = "Name for Player " + (playerIndex + 1) + ":";
            String name = JOptionPane.showInputDialog(null,
                    prompt,
                    GAME_NAME,
                    JOptionPane.QUESTION_MESSAGE);

            if (name == null) {
                if (playerIndex == 0) {
                    playerCount = -1;
                    continue;
                }
                playerIndex--;
                continue;
            }

            if (name.trim().equals("")) {
                name = "Player " + (playerIndex + 1);
            }
            players[playerIndex] = new Player(name.trim());
            playerIndex++;

            if (playerIndex >= playerCount) {
                break;
            }
        }

        return true;
    }

    static void setupBoard() {
        // CHANGE PROPERTY NAMES HERE
        // Format: new Property("Name", "Color", "Type", price, rent)
        // Type can be "Estate", "Special", "Event", "Tax", or "Go To Office".
        board[0] = new Property("GO", "White", "Special", 0, 0);
        board[1] = new Property("Howard's Toilet", "Brown", "Estate", 20, 2);
        board[2] = new Property("Lost and Found", "White", "Special", 0, 6);
        board[3] = new Property("Sun Center's Toilet", "Brown", "Estate", 40, 6);
        board[4] = new Property("Donation", "Black", "Tax", 200, 0);
        board[5] = new Property("Sale Station", "Black", "Estate", 180, 15);
        board[6] = new Property("Chrothall First Floor", "Light Blue", "Estate", 200, 16);
        board[7] = new Property("Chrothall Second Floor", "Light Blue", "Estate", 210, 18);
        board[8] = new Property("Events", "Black", "Event", 0, 0);
        board[9] = new Property("Chrothall Third Floor", "Light Blue", "Estate", 220, 20);
        board[10] = new Property("Mr. Primrose's Office", "Orange", "Special", 0, 0);
        board[11] = new Property("Cristine Duke Lecture Theatre", "Cerise", "Estate", 240, 22);
        board[12] = new Property("Cookie Station", "Black", "Estate", 160, 20);
        board[13] = new Property("Lawn", "Cerise", "Estate", 260, 24);
        board[14] = new Property("Flag Pole", "Cerise", "Estate", 270, 25);
        board[15] = new Property("Bench", "White", "Special", 0, 0);
        board[16] = new Property("Snowden Library", "Red", "Estate", 240, 22);
        board[17] = new Property("School House", "Red", "Estate", 280, 26);
        board[18] = new Property("Events", "Black", "Event", 0, 0);
        board[19] = new Property("Math Office", "Red", "Estate", 290, 28);
        board[20] = new Property("Cookie Station 2", "Black", "Estate", 180, 20);
        board[21] = new Property("Monkmon", "Yellow", "Estate", 300, 30);
        board[22] = new Property("Single Gym", "Yellow", "Estate", 310, 30);
        board[23] = new Property("Sale Station 2", "White", "Estate", 200, 15);
        board[24] = new Property("Double Gym", "Yellow", "Estate", 320, 32);
        board[25] = new Property("Go to Mr. Primrose's Office!", "Black", "Go To Office", 0, 0);
        board[26] = new Property("Events", "Black", "Event", 0, 0);
        board[27] = new Property("Sun Center", "Green", "Estate", 360, 40);
        board[28] = new Property("Service Day", "Black", "Tax", 100, 0);
        board[29] = new Property("Howard Cafe", "Blue", "Estate", 400, 50);
    }

    static void createWindow() {
        frame = new JFrame(GAME_NAME);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 750);
        frame.setLayout(new BorderLayout(10, 10));

        // Create tile buttons
        tileButtons = new JButton[board.length];
        for (int i = 0; i < board.length; i++) {
            final int index = i;
            
            tileButtons[i] = new JButton();
            tileButtons[i].setFocusPainted(false);
            tileButtons[i].setOpaque(true);
            tileButtons[i].setContentAreaFilled(true);
            tileButtons[i].setBorderPainted(true);
            tileButtons[i].setBackground(getTileColor(board[i].color));
            
            tileButtons[i].addActionListener(event -> {
                showBlockInfo(index);
            });
        }

        // Layout panels: top (1 x 11), bottom (1 x 11), left (4 x 1), right (4 x 1)
        JPanel mainBoard = new JPanel(new BorderLayout());

        // Top row: top-left corner (15), then 16..24, then top-right corner (25)
        JPanel topRow = new JPanel();
        topRow.setLayout(new BoxLayout(topRow, BoxLayout.X_AXIS));
        topRow.add(tileButtons[15]);
        for (int i = 16; i <= 24; i++) {
            topRow.add(tileButtons[i]);
        }
        topRow.add(tileButtons[25]);

        // Bottom row: bottom-left corner (10), then 9..1 (in reverse), then bottom-right corner (0)
        JPanel bottomRow = new JPanel();
        bottomRow.setLayout(new BoxLayout(bottomRow, BoxLayout.X_AXIS));
        bottomRow.add(tileButtons[10]);
        for (int i = 9; i >= 1; i--) {
            bottomRow.add(tileButtons[i]);
        }
        bottomRow.add(tileButtons[0]);

        // Left side (between top-left and bottom-left): 14..11 (top to bottom)
        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.add(tileButtons[14]);
        leftCol.add(tileButtons[13]);
        leftCol.add(tileButtons[12]);
        leftCol.add(tileButtons[11]);

        // Right side (between top-right and bottom-right): 26..29 (top to bottom)
        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.add(tileButtons[26]);
        rightCol.add(tileButtons[27]);
        rightCol.add(tileButtons[28]);
        rightCol.add(tileButtons[29]);

        // Make corner tiles larger and adjust fonts (smaller to fit screen)
        Dimension cornerSize = new Dimension(100, 100);
        Dimension horizSize = new Dimension(75, 100);
        Dimension vertSize = new Dimension(100, 100);

        int[] corners = {0, 10, 15, 25};
        for (int c : corners) {
            tileButtons[c].setPreferredSize(cornerSize);
            tileButtons[c].setMaximumSize(cornerSize);
            tileButtons[c].setMinimumSize(cornerSize);
            tileButtons[c].setFont(new Font("Arial", Font.BOLD, 12));
        }

        // Adjust sizes for edge tiles
        for (int i = 1; i <= 9; i++) {
            tileButtons[i].setPreferredSize(horizSize);
            tileButtons[i].setMaximumSize(horizSize);
            tileButtons[i].setMinimumSize(horizSize);
            tileButtons[i].setFont(new Font("Arial", Font.BOLD, 12));
        }
        for (int i = 16; i <= 24; i++) {
            tileButtons[i].setPreferredSize(horizSize);
            tileButtons[i].setMaximumSize(horizSize);
            tileButtons[i].setMinimumSize(horizSize);
            tileButtons[i].setFont(new Font("Arial", Font.BOLD, 12));
        }
        for (int i = 11; i <= 14; i++) {
            tileButtons[i].setPreferredSize(vertSize);
            tileButtons[i].setMaximumSize(vertSize);
            tileButtons[i].setMinimumSize(vertSize);
            tileButtons[i].setFont(new Font("Arial", Font.BOLD, 12));
        }
        for (int i = 26; i <= 29; i++) {
            tileButtons[i].setPreferredSize(vertSize);
            tileButtons[i].setMaximumSize(vertSize);
            tileButtons[i].setMinimumSize(vertSize);
            tileButtons[i].setFont(new Font("Arial", Font.BOLD, 12   
            ));
        }

        // Center area with block information display
        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setBackground(new Color(200, 230, 200));

        JLabel centerTitle = new JLabel("Block Information", SwingConstants.CENTER);
        centerTitle.setFont(new Font("Arial", Font.BOLD, 13));
        center.add(centerTitle, BorderLayout.NORTH);

        JPanel centerContent = new JPanel(new BorderLayout(4, 4));
        centerContent.setBackground(new Color(200, 230, 200));

        boardImageLabel = new JLabel("", SwingConstants.CENTER);
        boardImageLabel.setPreferredSize(new Dimension(340, 220));
        boardImageLabel.setOpaque(true);
        boardImageLabel.setBackground(Color.WHITE);
        centerContent.add(boardImageLabel, BorderLayout.NORTH);

        landingBannerLabel = new JLabel("", SwingConstants.CENTER);
        landingBannerLabel.setFont(new Font("Arial", Font.BOLD, 13));
        landingBannerLabel.setOpaque(true);
        landingBannerLabel.setBackground(new Color(255, 243, 176));
        landingBannerLabel.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        landingBannerLabel.setVisible(false);
        centerContent.add(landingBannerLabel, BorderLayout.SOUTH);

        boardInfoArea = new JTextArea();
        boardInfoArea.setEditable(false);
        boardInfoArea.setLineWrap(true);
        boardInfoArea.setWrapStyleWord(true);
        boardInfoArea.setFont(new Font("Arial", Font.PLAIN, 12));
        boardInfoArea.setRows(4);
        boardInfoArea.setText("Click any block to view its details here.");
        JScrollPane boardInfoScroll = new JScrollPane(boardInfoArea);
        boardInfoScroll.setPreferredSize(new Dimension(340, 95));
        centerContent.add(boardInfoScroll, BorderLayout.CENTER);
        center.add(centerContent, BorderLayout.CENTER);

        // Assemble main board
        mainBoard.add(topRow, BorderLayout.NORTH);
        mainBoard.add(bottomRow, BorderLayout.SOUTH);
        mainBoard.add(leftCol, BorderLayout.WEST);
        mainBoard.add(rightCol, BorderLayout.EAST);
        mainBoard.add(center, BorderLayout.CENTER);

        JPanel boardWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        boardWrapper.add(mainBoard);

        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BorderLayout(8, 8));
        sidePanel.setPreferredSize(new Dimension(260, 500));

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

        frame.add(boardWrapper, BorderLayout.CENTER);
        frame.add(sidePanel, BorderLayout.EAST);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    static void takeTurn() {
        if (gameOver) {
            return;
        }

        // Skip to next non-bankrupt player
        while (currentPlayerIndex < players.length && players[currentPlayerIndex].isBankrupt) {
            currentPlayerIndex++;
        }

        if (currentPlayerIndex >= players.length) {
            currentPlayerIndex = 0;
            roundNumber++;
            while (currentPlayerIndex < players.length && players[currentPlayerIndex].isBankrupt) {
                currentPlayerIndex++;
            }
        }

        if (checkGameEndCondition()) {
            return;
        }

        Player currentPlayer = players[currentPlayerIndex];

        // Check if player is in detention
        if (currentPlayer.inDetention) {
            addLog("");
            addLog(currentPlayer.name + " is in Mr. Primrose's Office (turn " + (currentPlayer.turnsInDetention + 1) + " of 2).");

            if (currentPlayer.turnsInDetention >= 1) {
                // Automatically release after 1 turn
                currentPlayer.inDetention = false;
                currentPlayer.turnsInDetention = 0;
                addLog(currentPlayer.name + " is released from the office!");
            } else if (currentPlayer.detentionPass) {
                // Ask if they want to use the detention pass
                int choice = JOptionPane.showConfirmDialog(frame,
                        currentPlayer.name + ", do you want to use your Detention pass?",
                        "Use Detention Pass",
                        JOptionPane.YES_NO_OPTION);

                if (choice == JOptionPane.YES_OPTION) {
                    currentPlayer.detentionPass = false;
                    currentPlayer.inDetention = false;
                    currentPlayer.turnsInDetention = 0;
                    currentPlayer.position = 25;
                    addLog(currentPlayer.name + " used their detention pass and returned to the original spot!");

                    int roll = rollDice();
                    addLog(currentPlayer.name + " rolled a " + roll + ".");
                    movePlayer(currentPlayer, roll);
                    handleTile(currentPlayer);
                } else {
                    currentPlayer.turnsInDetention++;
                    addLog(currentPlayer.name + " chose to stay in the office.");
                }
            } else {
                currentPlayer.turnsInDetention++;
                addLog(currentPlayer.name + " has no pass, must stay in the office.");
            }

            goToNextPlayer();
            updateScreen();

            if (roundNumber > MAX_ROUNDS) {
                endGame();
            }
            return;
        }

        int roll = rollDice();
        addLog("");
        addLog(currentPlayer.name + " rolled a " + roll + ".");

        movePlayer(currentPlayer, roll);
        handleTile(currentPlayer);

        if (currentPlayer.money < 0) {
            currentPlayer.isBankrupt = true;
            addLog(currentPlayer.name + " is bankrupt and is out of the game!");
        }

        goToNextPlayer();
        updateScreen();

        if (checkGameEndCondition()) {
            return;
        }

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

        // Auto-show the property image and highlight the tile whenever a player lands
        showBlockInfo(player.position);
        highlightLandedTile(player.position, player);

        if (property.type.equals("Estate")) {
            handleEstate(player, property);
        } else if (property.type.equals("Tax")) {
            player.money = player.money - property.price;
            addLog(player.name + " paid $" + property.price + " for " + property.name + ".");
        } else if (property.type.equals("Event")) {
            handleEvent(player);
        } else if (property.type.equals("Go To Office")) {
            handleGoToOffice(player);
        } else if (property.name.equals("Mr. Primrose's Office")) {
            playJailSound();
            addLog(player.name + " is visiting Mr. Primrose's Office.");
        } else if (property.name.equals("Lost and Found")) {
            handleLostAndFound(player);
        } else {
            addLog(player.name + " is resting on " + property.name + ".");
        }
    }

    static void handleLostAndFound(Player player) {
        int outcome = random.nextInt(4);

        if (outcome == 0) {
            int amount = 50 + random.nextInt(51);
            player.money += amount;
            addLog(player.name + " found $" + amount + " in the Lost and Found!");
        } else if (outcome == 1) {
            player.detentionPass = true;
            addLog(player.name + " found a Get Out of Detention Free card in the Lost and Found!");
        } else if (outcome == 2) {
            int amount = 30;
            int collectedFromPlayers = 0;
            for (int i = 0; i < players.length; i++) {
                if (players[i] != player) {
                    int payment = Math.min(amount, players[i].money);
                    players[i].money -= payment;
                    player.money += payment;
                    collectedFromPlayers += payment;
                }
            }
            addLog(player.name + " collected $" + collectedFromPlayers + " from other players at the Lost and Found!");
        } else {
            int loss = 20 + random.nextInt(21);
            player.money -= loss;
            addLog(player.name + " lost $" + loss + " at the Lost and Found.");
        }
    }

    static void handleGoToOffice(Player player) {
        player.position = 10;
        player.inDetention = true;
        player.turnsInDetention = 0;
        playJailSound();
        addLog(player.name + " went to Mr. Primrose's Office and is now in detention!");
    }

    static void playJailSound() {
        try {
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(new java.io.File(JAIL_SOUND));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInput);
            audioInput.close();
            clip.addLineListener(event -> {
                if (event.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
                    clip.close();
                }
            });
            clip.start();
        } catch (Exception e) {
            System.out.println("Could not play jail sound.");
        }
    }
    
    static void playBackgroundMusic() {
        try {
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(new java.io.File("assets/02 Bidding War.wav"));
            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioInput);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
        } catch (Exception e) {
            System.out.println("Could not play background music.");
        }
    }
    static boolean ownsAllSaleStations(Player player) {
        return board[5].owner == player && board[23].owner == player;
    }

    static boolean ownsAllCookieStations(Player player) {
        return board[12].owner == player && board[20].owner == player;
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
            int rentToPay = property.rent;
            
            // Check if this is a Cookie Station
            if ((property.name.equals("Cookie Station") || property.name.equals("Cookie Station 2"))) {
                int diceRoll = rollDice();
                addLog(property.owner.name + " rolls the dice for Cookie Station rent: " + diceRoll + "!");
                
                if (ownsAllCookieStations(property.owner)) {
                    // Both Cookie Stations owned: 2 × dice number × rent
                    rentToPay = 2 * diceRoll * property.rent;
                    addLog("Both Cookie Stations are owned by " + property.owner.name + "! (2x multiplier)");
                } else {
                    // One Cookie Station owned: dice number × rent
                    rentToPay = diceRoll * property.rent;
                }
            }
            // Check if this is a Sale Station and owner has both
            else if ((property.name.equals("Sale Station") || property.name.equals("Sale Station 2")) && 
                ownsAllSaleStations(property.owner)) {
                rentToPay = 50;
                addLog("Both Sale Stations are owned by " + property.owner.name + "!");
            }
            
            player.money = player.money - rentToPay;
            property.owner.money = property.owner.money + rentToPay;
            addLog(player.name + " paid $" + rentToPay + " rent to " + property.owner.name + ".");
        }
    }

    static void handleEvent(Player player) {
        int eventNumber = random.nextInt(EVENT_MESSAGES.length);
        String message = EVENT_MESSAGES[eventNumber];
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
        } else if (eventNumber == 5) {
            movePlayerTo(player, 6);
            handleTile(player);
        } else if (eventNumber == 6) {
            movePlayerTo(player, 0);
            handleTile(player);
        } else if (eventNumber == 7) {
            movePlayerTo(player, 12);
            handleTile(player);
        }
    }

    static void movePlayerTo(Player player, int targetIndex) {
        int oldPosition = player.position;
        player.position = targetIndex;
        if (targetIndex < oldPosition) {
            player.money += PASS_GO_MONEY;
            addLog(player.name + " passed GO and collected $" + PASS_GO_MONEY + ".");
        }
        addLog(player.name + " moved from " + board[oldPosition].name + " to " + board[player.position].name + ".");
    }

    static void goToNextPlayer() {
        currentPlayerIndex++;
        if (currentPlayerIndex >= players.length) {
            currentPlayerIndex = 0;
            roundNumber++;
        }
    }

    static boolean checkGameEndCondition() {
        int solventPlayers = 0;
        for (Player p : players) {
            if (!p.isBankrupt) {
                solventPlayers++;
            }
        }

        if (solventPlayers <= 1) {
            endGame();
            return true;
        }
        return false;
    }

    static void endGame() {
        gameOver = true;
        rollButton.setEnabled(false);

        Player winner = null;
        for (int i = 0; i < players.length; i++) {
            if (!players[i].isBankrupt) {
                if (winner == null || players[i].money > winner.money) {
                    winner = players[i];
                }
            }
        }

        if (winner == null) {
            winner = players[0];
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
        lastLandedTile = -1;
        if (landingBannerLabel != null) landingBannerLabel.setVisible(false);
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
                String status = player.isBankrupt ? " [BANKRUPT]" : "";
                playerLabels[i].setText(player.name + ": $" + player.money + " | Space " + player.position + status);
                playerLabels[i].setVisible(true);
            } else {
                playerLabels[i].setVisible(false);
            }
        }

        for (int i = 0; i < tileButtons.length; i++) {
            Property property = board[i];
            String text = "<html><center>" + property.name + getPlayersOnTileImages(i);

            if (property.owner != null) {
                text = text + "<br>Owner: " + property.owner.name;
            } else if (property.type.equals("Estate")) {
                text = text + "<br>$" + property.price + " / Rent $" + property.rent;
            }

            text = text + "</center></html>";
            tileButtons[i].setText(text);
            // Keep the gold landing-highlight border; only reset tiles that aren't the active landed one
            if (i == lastLandedTile) {
                tileButtons[i].setBorder(BorderFactory.createLineBorder(new Color(220, 160, 0), 4));
            } else {
                tileButtons[i].setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
            }
            tileButtons[i].setBackground(getTileColor(property.color));
        }
    }

    static String getPlayersOnTileImages(int tileNumber) {
        String text = "";
        for (int i = 0; i < players.length; i++) {
            if (players[i].position == tileNumber) {
                text = text + "<img src='" + getTokenImagePath(i) + "' width='" + PLAYER_TOKEN_WIDTHS[i] + "' height='" + PLAYER_TOKEN_HEIGHTS[i] + "'>";
            }
        }
        if (!text.equals("")) {
            text = "<br>" + text;
        }
        return text;
    }

    static String getTokenImagePath(int playerIndex) {
        return new java.io.File(PLAYER_TOKEN_IMAGES[playerIndex]).toURI().toString();
    }

    static void highlightLandedTile(int tileIndex, Player player) {
        // Remove highlight from the previous landed tile
        if (lastLandedTile >= 0 && lastLandedTile < tileButtons.length) {
            tileButtons[lastLandedTile].setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        }
        lastLandedTile = tileIndex;

        // Draw a thick gold border around the newly landed tile
        tileButtons[tileIndex].setBorder(BorderFactory.createLineBorder(new Color(220, 160, 0), 4));

        // Show the yellow landing banner with the player's name and property
        Property prop = board[tileIndex];
        landingBannerLabel.setText("\uD83D\uDCCD " + player.name + " landed on: " + prop.name);
        landingBannerLabel.setVisible(true);
    }

    static void showBlockInfo(int tileIndex) {
        Property property = board[tileIndex];
        showBlockImage(tileIndex, property);
        boardInfoArea.setText(getBlockInfoText(property));
    }

    static void showBlockImage(int tileIndex, Property property) {
        String imagePath = getBlockImagePath(tileIndex, property.name);

        if (imagePath == null) {
            boardImageLabel.setIcon(null);
            boardImageLabel.setText("");
            return;
        }

        ImageIcon originalIcon = new ImageIcon(new java.io.File(imagePath).getAbsolutePath());
        int imageWidth = originalIcon.getIconWidth();
        int imageHeight = originalIcon.getIconHeight();

        if (imageWidth <= 0 || imageHeight <= 0) {
            boardImageLabel.setIcon(null);
            boardImageLabel.setText("");
            return;
        }

        int maxWidth = 340;
        int maxHeight = 260;
        double scale = Math.min((double) maxWidth / imageWidth, (double) maxHeight / imageHeight);
        int scaledWidth = (int) (imageWidth * scale);
        int scaledHeight = (int) (imageHeight * scale);

        Image scaledImage = originalIcon.getImage().getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
        boardImageLabel.setText("");
        boardImageLabel.setIcon(new ImageIcon(scaledImage));
    }

    static String getBlockImagePath(int tileIndex, String propertyName) {
        if (propertyName.equals("Howard's Toilet")) {
            return "assets/Howards Washroom.jpg";
        } else if (propertyName.equals("Sale Station")) {
            return "assets/Sale station.jpeg";
        } else if (propertyName.equals("Chrothall First Floor")) {
            return "assets/Crothall 1.jpg";
        } else if (propertyName.equals("Chrothall Second Floor")) {
            return "assets/Crothall 2.jpg";
        } else if (propertyName.equals("Events") && tileIndex == 8) {
            return "assets/Event.jpeg";
        } else if (propertyName.equals("Chrothall Third Floor")) {
            return "assets/Crothall 3.jpg";
        } else if (propertyName.equals("Mr. Primrose's Office") || propertyName.equals("Go to Mr. Primrose's Office!")) {
            return "assets/Primrose Office.jpg";
        } else if (propertyName.equals("Cristine Duke Lecture Theatre")) {
            return "assets/Christine Duke.jpg";
        } else if (propertyName.equals("Cookie Station") || propertyName.equals("Cookie Station 2")) {
            return "assets/Cookie Station.jpeg";
        } else if (propertyName.equals("Lawn")) {
            return "assets/Field.jpg";
        } else if (propertyName.equals("Flag Pole")) {
            return "assets/Flag Pole.jpg";
        } else if (propertyName.equals("Events") && tileIndex == 18) {
            return "assets/Event 2.jpeg";
        } else if (propertyName.equals("Snowden Library")) {
            return "assets/Library.jpg";
        } else if (propertyName.equals("School House")) {
            return "assets/School House.jpg";
        } else if (propertyName.equals("Math Office")) {
            return "assets/Math Office.jpg";
        } else if (propertyName.equals("Monkmon")) {
            return "assets/Monkmen.jpg";
        } else if (propertyName.equals("Single Gym")) {
            return "assets/Single Gym.jpg";
        } else if (propertyName.equals("Sale Station 2")) {
            return "assets/sale station 2.jpeg";
        } else if (propertyName.equals("Double Gym")) {
            return "assets/Double Gym.jpg";
        } else if (propertyName.equals("Events") && tileIndex == 26) {
            return "assets/Event.jpeg";
        } else if (propertyName.equals("Service Day")) {
            return "assets/Service Day.jpeg";
        } else if (propertyName.equals("Howard Cafe")) {
            return "assets/Howards.jpg";
        } else if (propertyName.equals("Sun Center's Toilet")) {
            return "assets/smustoilet.jpg";
        } else if (propertyName.equals("Sun Center")) {
            return "assets/suncenter.jpg";
        }

        return null;
    }

    static String getBlockInfoText(Property property) {
        StringBuilder builder = new StringBuilder();
        builder.append(property.name).append(" | ").append(property.type).append(" | ").append(property.color).append("\n");

        if (property.type.equals("Estate")) {
            builder.append("Price: $").append(property.price).append(" | Rent: $").append(property.rent).append("\n");
            builder.append("All color: $").append(property.rent * 2);
            builder.append(" | House: $").append(property.price / 2);
            builder.append(" | House rent: $").append(property.rent * 3).append("\n");
        } else if (property.type.equals("Tax")) {
            builder.append("Tax: $").append(property.price).append("\n");
        } else if (property.type.equals("Go To Office")) {
            builder.append("Go to Mr. Primrose's Office!\n");
        } else if (property.type.equals("Event")) {
            builder.append("Event space.\n");
        } else {
            builder.append("No property price or rent.\n");
        }

        if (property.owner != null) {
            builder.append("Owned by ").append(property.owner.name).append("\n");
        } else {
            builder.append("Unowned\n");
        }

        return builder.toString();
    }

    static Color getTileColor(String colorName) {
        if (colorName.equals("Brown")) {
            return new Color(176, 122, 74);
        } else if (colorName.equals("Light Blue")) {
            return new Color(150, 210, 255);
        } else if (colorName.equals("Cerise")) {
            return new Color(255, 84, 190);
        } else if (colorName.equals("Red")) {
            return new Color(235, 0, 66);
        } else if (colorName.equals("Yellow")) {
            return new Color(245, 224, 39);
        } else if (colorName.equals("Orange")) {
            return new Color(245, 175, 95);
        } else if (colorName.equals("Black")) {
            return new Color(210, 210, 210);
        } else if (colorName.equals("Blue")) {
            return new Color(36, 60, 255);
        } else if (colorName.equals("Green")) {
            return new Color(0, 199, 56);
        } else {
            return Color.WHITE;
        }
    }

    static void addLog(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
    