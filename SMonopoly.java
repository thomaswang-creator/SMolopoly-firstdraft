import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class SMonopoly {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                GameFrame frame = new GameFrame();
                frame.setVisible(true);
            }
        });
    }
}

class GameFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel screenHolder;
    private final SetupPanel setupPanel;
    private GamePanel gamePanel;

    public GameFrame() {
        super("SMonopoly");
        cardLayout = new CardLayout();
        screenHolder = new JPanel(cardLayout);
        setupPanel = new SetupPanel(this);

        screenHolder.add(setupPanel, "setup");
        add(screenHolder);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 720));
        setSize(1280, 760);
        setLocationRelativeTo(null);
    }

    public void startGame(List<String> names, WinMode winMode) {
        if (gamePanel != null) {
            screenHolder.remove(gamePanel);
        }
        GameModel model = new GameModel(names, winMode);
        gamePanel = new GamePanel(model, this);
        screenHolder.add(gamePanel, "game");
        cardLayout.show(screenHolder, "game");
        gamePanel.refresh();
    }

    public void returnToSetup() {
        cardLayout.show(screenHolder, "setup");
    }
}

class SetupPanel extends JPanel {
    private final GameFrame frame;
    private final JComboBox<Integer> playerCountBox;
    private final JTextField[] nameFields;
    private final JRadioButton roundLimitButton;
    private final JRadioButton bankruptcyButton;

    public SetupPanel(GameFrame frame) {
        this.frame = frame;
        playerCountBox = new JComboBox<Integer>(new Integer[] {2, 3, 4});
        nameFields = new JTextField[4];
        roundLimitButton = new JRadioButton("20 rounds each: most smollars wins", true);
        bankruptcyButton = new JRadioButton("Bankrupt: when one player is broke, most smollars wins");

        setLayout(new GridBagLayout());
        setBackground(new Color(236, 241, 238));
        buildScreen();
    }

    private void buildScreen() {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 12, 8, 12);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("SMonopoly!!!", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 34));
        add(title, c);

        c.gridy++;
        JLabel subtitle = new JLabel("Smonopoly by Thomas & William", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 16));
        add(subtitle, c);

        c.gridy++;
        c.gridwidth = 1;
        add(new JLabel("Number of players:"), c);

        c.gridx = 1;
        add(playerCountBox, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        JLabel namesTitle = new JLabel("Player Names");
        namesTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(namesTitle, c);

        for (int i = 0; i < nameFields.length; i++) {
            c.gridy++;
            c.gridwidth = 1;
            c.gridx = 0;
            add(new JLabel("Player " + (i + 1) + ":"), c);

            c.gridx = 1;
            nameFields[i] = new JTextField("Player " + (i + 1), 16);
            add(nameFields[i], c);
        }

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        JLabel modeTitle = new JLabel("Choose how the game ends");
        modeTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(modeTitle, c);

        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(roundLimitButton);
        modeGroup.add(bankruptcyButton);

        c.gridy++;
        add(roundLimitButton, c);

        c.gridy++;
        add(bankruptcyButton, c);

        c.gridy++;
        JButton startButton = new JButton("START!");
        startButton.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(startButton, c);

        playerCountBox.addActionListener(event -> updateNameFields());
        startButton.addActionListener(event -> startGame());
        updateNameFields();
    }

    private void updateNameFields() {
        int playerCount = (Integer) playerCountBox.getSelectedItem();
        for (int i = 0; i < nameFields.length; i++) {
            nameFields[i].setEnabled(i < playerCount);
        }
    }

    private void startGame() {
        int playerCount = (Integer) playerCountBox.getSelectedItem();
        List<String> names = new ArrayList<String>();

        for (int i = 0; i < playerCount; i++) {
            String name = nameFields[i].getText().trim();
            if (name.length() == 0) {
                name = "Player " + (i + 1);
            }
            names.add(name);
        }

        WinMode mode = roundLimitButton.isSelected() ? WinMode.ROUND_LIMIT : WinMode.BANKRUPTCY;
        frame.startGame(names, mode);
    }
}

class GamePanel extends JPanel {
    private final GameModel model;
    private final GameFrame frame;
    private final BoardPanel boardPanel;
    private final JLabel currentPlayerLabel;
    private final JLabel diceLabel;
    private final JLabel modeLabel;
    private final JLabel turnsLabel;
    private final JPanel debitCardsPanel;
    private final JTextArea logArea;
    private final JButton rollButton;
    private final JButton buyButton;
    private final JButton loanButton;
    private final JButton endTurnButton;
    private final Random animationRandom;
    private boolean diceAnimationRunning;

    public GamePanel(GameModel model, GameFrame frame) {
        this.model = model;
        this.frame = frame;
        boardPanel = new BoardPanel(model);
        currentPlayerLabel = new JLabel();
        diceLabel = new JLabel();
        modeLabel = new JLabel();
        turnsLabel = new JLabel();
        debitCardsPanel = new JPanel();
        logArea = new JTextArea(8, 26);
        rollButton = new JButton("Roll The Dice");
        buyButton = new JButton("Buy it");
        loanButton = new JButton("Take Loan");
        endTurnButton = new JButton("End|skip");
        animationRandom = new Random();

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(new Color(222, 231, 226));
        buildGameScreen();
    }

    private void buildGameScreen() {
        add(boardPanel, BorderLayout.CENTER);

        JPanel moneyPanel = new JPanel(new BorderLayout(8, 8));
        moneyPanel.setPreferredSize(new Dimension(300, 700));
        moneyPanel.setBackground(new Color(247, 248, 245));
        moneyPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(188, 198, 190)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        debitCardsPanel.setLayout(new GridLayout(0, 1, 6, 6));
        debitCardsPanel.setBackground(moneyPanel.getBackground());
        JScrollPane cardsScrollPane = new JScrollPane(debitCardsPanel);
        cardsScrollPane.setBorder(BorderFactory.createTitledBorder("Player Money and Cards"));
        moneyPanel.add(cardsScrollPane, BorderLayout.CENTER);
        add(moneyPanel, BorderLayout.WEST);

        JPanel sidePanel = new JPanel(new BorderLayout(8, 8));
        sidePanel.setPreferredSize(new Dimension(300, 700));
        sidePanel.setBackground(new Color(247, 248, 245));
        sidePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(188, 198, 190)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JPanel infoPanel = new JPanel(new GridLayout(4, 1, 4, 4));
        infoPanel.setBackground(sidePanel.getBackground());
        currentPlayerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        diceLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        modeLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        turnsLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        infoPanel.add(currentPlayerLabel);
        infoPanel.add(diceLabel);
        infoPanel.add(modeLabel);
        infoPanel.add(turnsLabel);
        sidePanel.add(infoPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 6, 6));
        buttonPanel.setBackground(sidePanel.getBackground());
        JButton newGameButton = new JButton("New Game");
        buttonPanel.add(rollButton);
        buttonPanel.add(buyButton);
        buttonPanel.add(loanButton);
        buttonPanel.add(endTurnButton);
        buttonPanel.add(newGameButton);

        rollButton.addActionListener(event -> startDiceAnimation());

        buyButton.addActionListener(event -> {
            model.buyCurrentProperty();
            refresh();
        });

        loanButton.addActionListener(event -> {
            model.takeCurrentPlayerLoan();
            refresh();
        });

        endTurnButton.addActionListener(event -> {
            model.finishTurn();
            refresh();
        });

        newGameButton.addActionListener(event -> frame.returnToSetup());

        JPanel middlePanel = new JPanel(new BorderLayout(8, 8));
        middlePanel.setBackground(sidePanel.getBackground());
        middlePanel.add(buttonPanel, BorderLayout.NORTH);
        sidePanel.add(middlePanel, BorderLayout.CENTER);

        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Game Log"));
        logScrollPane.setPreferredSize(new Dimension(280, 180));
        sidePanel.add(logScrollPane, BorderLayout.SOUTH);

        add(sidePanel, BorderLayout.EAST);
    }

    private void startDiceAnimation() {
        if (diceAnimationRunning || model.isGameOver() || model.hasRolledThisTurn()) {
            return;
        }

        diceAnimationRunning = true;
        refresh();

        final int[] frames = {0};
        Timer timer = new Timer(80, null);
        timer.addActionListener(event -> {
            frames[0]++;
            boardPanel.showDice(animationRandom.nextInt(6) + 1, animationRandom.nextInt(6) + 1, true);

            if (frames[0] >= 14) {
                ((Timer) event.getSource()).stop();
                model.rollDiceAndMove();
                boardPanel.showDice(model.getLastDieOne(), model.getLastDieTwo(), false);
                diceAnimationRunning = false;
                refresh();
            }
        });
        timer.start();
    }

    public void refresh() {
        Player current = model.getCurrentPlayer();
        currentPlayerLabel.setText("Turn: " + current.getName());
        diceLabel.setText("Dice: " + model.getDiceText());
        modeLabel.setText("Mode: " + model.getModeText());
        turnsLabel.setText("Turns: " + model.getTurnSummary());

        rollButton.setEnabled(!diceAnimationRunning && !model.isGameOver() && !model.hasRolledThisTurn());
        buyButton.setEnabled(!diceAnimationRunning && model.canCurrentPlayerBuy());
        loanButton.setEnabled(!diceAnimationRunning && model.canCurrentPlayerBorrow());
        loanButton.setText(model.getCurrentLoanButtonText());
        endTurnButton.setEnabled(!diceAnimationRunning && !model.isGameOver() && model.hasRolledThisTurn());

        rebuildDebitCards();
        logArea.setText(model.getLogText());
        logArea.setCaretPosition(logArea.getDocument().getLength());
        if (!diceAnimationRunning) {
            boardPanel.showDice(model.getLastDieOne(), model.getLastDieTwo(), false);
        } else {
            boardPanel.repaint();
        }

        if (model.isGameOver() && !model.hasShownGameOverPopup()) {
            model.markGameOverPopupShown();
            JOptionPane.showMessageDialog(this, model.getWinnerMessage(), "Game Over", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void rebuildDebitCards() {
        debitCardsPanel.removeAll();
        for (Player player : model.getPlayers()) {
            JPanel cardPanel = new JPanel(new GridLayout(8, 1));
            cardPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(player.getColor().darker(), player == model.getCurrentPlayer() ? 3 : 1),
                    BorderFactory.createEmptyBorder(6, 8, 6, 8)));
            cardPanel.setBackground(player == model.getCurrentPlayer() ? new Color(255, 249, 218) : Color.WHITE);

            JLabel name = new JLabel(player.getName());
            name.setFont(new Font("SansSerif", Font.BOLD, 14));
            JLabel debitCardNumber = new JLabel("Debit card: " + player.getDebitCard().getCardNumber());
            JLabel balance = new JLabel("Balance: " + player.getDebitCard().getBalance() + " smollars");
            JLabel creditCardNumber = new JLabel("Credit card: " + player.getCreditCard().getCardNumber());
            JLabel creditScore = new JLabel("Credit score: " + player.getCreditCard().getCreditScore() + "/90");
            JLabel creditDebt = new JLabel("Credit debt: " + player.getCreditCard().getLoanBalance() + " smollars");
            JLabel loanAmount = new JLabel("Loan available: " + player.getCreditCard().getAvailableLoanAmount() + " smollars");
            JLabel details = new JLabel("Properties: " + player.getPropertiesOwned() + " | Turns: " + player.getTurnsTaken());

            cardPanel.add(name);
            cardPanel.add(debitCardNumber);
            cardPanel.add(balance);
            cardPanel.add(creditCardNumber);
            cardPanel.add(creditScore);
            cardPanel.add(creditDebt);
            cardPanel.add(loanAmount);
            cardPanel.add(details);
            debitCardsPanel.add(cardPanel);
        }
        debitCardsPanel.revalidate();
        debitCardsPanel.repaint();
    }
}

class BoardPanel extends JPanel {
    private static final int GRID_SIZE = 6;
    private final GameModel model;
    private int displayDieOne;
    private int displayDieTwo;
    private boolean diceRolling;

    public BoardPanel(GameModel model) {
        this.model = model;
        setPreferredSize(new Dimension(680, 680));
        setBackground(new Color(226, 239, 231));
    }

    public void showDice(int dieOne, int dieTwo, boolean rolling) {
        displayDieOne = dieOne;
        displayDieTwo = dieTwo;
        diceRolling = rolling;
        repaint();
    }

    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = Math.min(getWidth(), getHeight()) - 24;
        int startX = (getWidth() - size) / 2;
        int startY = (getHeight() - size) / 2;
        int cell = size / GRID_SIZE;

        drawCenter(g, startX + cell, startY + cell, cell * 4, cell * 4);

        for (int i = 0; i < model.getBoard().size(); i++) {
            BoardSpace space = model.getBoard().get(i);
            int[] point = getGridPoint(i);
            int x = startX + point[0] * cell;
            int y = startY + point[1] * cell;
            drawSpace(g, space, i, x, y, cell);
        }

        drawPlayers(g, startX, startY, cell);
    }

    private void drawCenter(Graphics2D g, int x, int y, int width, int height) {
        g.setColor(new Color(248, 250, 245));
        g.fillRect(x, y, width, height);
        g.setColor(new Color(69, 90, 74));
        g.setFont(new Font("SansSerif", Font.BOLD, 30));
        drawCenteredText(g, "SMOL MONOPOLY", x, y + 48, width);

        int diceSize = Math.min(78, width / 5);
        int diceGap = 18;
        int diceY = y + height / 2 - diceSize / 2;
        int firstDiceX = x + width / 2 - diceSize - diceGap / 2;
        int secondDiceX = x + width / 2 + diceGap / 2;
        drawDice(g, firstDiceX, diceY, diceSize, displayDieOne);
        drawDice(g, secondDiceX, diceY, diceSize, displayDieTwo);

        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        drawCenteredText(g, diceRolling ? "Rolling..." : "Dice roll", x, diceY + diceSize + 28, width);

        g.setFont(new Font("SansSerif", Font.PLAIN, 15));
        drawCenteredText(g, "Start with 1000 smollars", x, y + height - 74, width);
        drawCenteredText(g, "Debit cards hold money. Credit cards give loans.", x, y + height - 48, width);
    }

    private void drawDice(Graphics2D g, int x, int y, int size, int value) {
        g.setColor(diceRolling ? new Color(255, 241, 166) : Color.WHITE);
        g.fillRoundRect(x, y, size, size, 14, 14);
        g.setColor(new Color(52, 62, 56));
        g.drawRoundRect(x, y, size, size, 14, 14);

        if (value < 1 || value > 6) {
            g.setFont(new Font("SansSerif", Font.BOLD, 30));
            drawCenteredText(g, "?", x, y + size / 2 + 11, size);
            return;
        }

        int pipSize = Math.max(7, size / 9);
        int left = x + size / 4;
        int center = x + size / 2;
        int right = x + size * 3 / 4;
        int top = y + size / 4;
        int middle = y + size / 2;
        int bottom = y + size * 3 / 4;

        g.setColor(new Color(42, 47, 43));
        if (value == 1 || value == 3 || value == 5) {
            drawPip(g, center, middle, pipSize);
        }
        if (value >= 2) {
            drawPip(g, left, top, pipSize);
            drawPip(g, right, bottom, pipSize);
        }
        if (value >= 4) {
            drawPip(g, right, top, pipSize);
            drawPip(g, left, bottom, pipSize);
        }
        if (value == 6) {
            drawPip(g, left, middle, pipSize);
            drawPip(g, right, middle, pipSize);
        }
    }

    private void drawPip(Graphics2D g, int centerX, int centerY, int size) {
        g.fillOval(centerX - size / 2, centerY - size / 2, size, size);
    }

    private void drawSpace(Graphics2D g, BoardSpace space, int index, int x, int y, int size) {
        g.setColor(space.getDisplayColor());
        g.fillRect(x, y, size, size);
        g.setColor(new Color(64, 70, 65));
        g.drawRect(x, y, size, size);

        if (space.getOwner() != null) {
            g.setColor(space.getOwner().getColor());
            g.fillRect(x + 3, y + 3, size - 6, 9);
        }

        g.setColor(new Color(32, 42, 35));
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        drawWrappedText(g, index + ". " + space.getName(), x + 5, y + 18, size - 10, 3);

        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        String bottom = space.getShortInfo();
        drawCenteredText(g, bottom, x, y + size - 10, size);
    }

    private void drawPlayers(Graphics2D g, int startX, int startY, int cell) {
        for (int i = 0; i < model.getPlayers().size(); i++) {
            Player player = model.getPlayers().get(i);
            int[] point = getGridPoint(player.getPosition());
            int x = startX + point[0] * cell + 12 + (i % 2) * 25;
            int y = startY + point[1] * cell + 46 + (i / 2) * 25;

            g.setColor(player.getColor());
            g.fillOval(x, y, 22, 22);
            g.setColor(Color.WHITE);
            g.drawOval(x, y, 22, 22);
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            drawCenteredText(g, "" + (i + 1), x, y + 16, 22);
        }
    }

    private int[] getGridPoint(int index) {
        if (index < 6) {
            return new int[] {5 - index, 5};
        } else if (index < 11) {
            return new int[] {0, 10 - index};
        } else if (index < 16) {
            return new int[] {index - 10, 0};
        } else {
            return new int[] {5, index - 15};
        }
    }

    private void drawWrappedText(Graphics2D g, String text, int x, int y, int width, int maxLines) {
        FontMetrics metrics = g.getFontMetrics();
        String[] words = text.split(" ");
        String line = "";
        int linesUsed = 0;

        for (int i = 0; i < words.length && linesUsed < maxLines; i++) {
            String testLine = line.length() == 0 ? words[i] : line + " " + words[i];
            if (metrics.stringWidth(testLine) > width && line.length() > 0) {
                g.drawString(line, x, y + linesUsed * 13);
                line = words[i];
                linesUsed++;
            } else {
                line = testLine;
            }
        }

        if (line.length() > 0 && linesUsed < maxLines) {
            g.drawString(line, x, y + linesUsed * 13);
        }
    }

    private void drawCenteredText(Graphics2D g, String text, int x, int baselineY, int width) {
        FontMetrics metrics = g.getFontMetrics();
        int textX = x + (width - metrics.stringWidth(text)) / 2;
        g.drawString(text, textX, baselineY);
    }
}

class GameModel {
    private static final int STARTING_BALANCE = 1000;
    private static final int STARTING_CREDIT_SCORE = 90;
    private static final int PASS_START_BONUS = 100;
    private static final int PASS_START_CREDIT_BONUS = 2;
    private static final int MAX_TURNS = 20;

    private final List<Player> players;
    private final List<BoardSpace> board;
    private final List<String> log;
    private final Random random;
    private final WinMode winMode;
    private int currentPlayerIndex;
    private boolean rolledThisTurn;
    private boolean loanTakenThisTurn;
    private boolean gameOver;
    private boolean shownGameOverPopup;
    private int lastDieOne;
    private int lastDieTwo;
    private String diceText;
    private String winnerMessage;

    public GameModel(List<String> names, WinMode winMode) {
        this.winMode = winMode;
        players = new ArrayList<Player>();
        board = new ArrayList<BoardSpace>();
        log = new ArrayList<String>();
        random = new Random();
        diceText = "not rolled yet";
        winnerMessage = "";

        Color[] colors = {
            new Color(213, 69, 75),
            new Color(49, 116, 196),
            new Color(60, 151, 93),
            new Color(231, 157, 46)
        };

        for (int i = 0; i < names.size(); i++) {
            DebitCard debitCard = new DebitCard("SMOL-" + (1001 + i), STARTING_BALANCE);
            CreditCard creditCard = new CreditCard("CREDIT-" + (5001 + i), STARTING_CREDIT_SCORE);
            players.add(new Player(names.get(i), colors[i], debitCard, creditCard));
        }

        buildBoard();
        addLog("Game started. Every debit card received " + STARTING_BALANCE + " smollars.");
        addLog("Every credit card starts with a " + STARTING_CREDIT_SCORE + "/90 credit score.");
        addLog("Win condition: " + getModeText() + ".");
    }

    private void buildBoard() {
        board.add(BoardSpace.start("Start"));
        board.add(BoardSpace.property("CS classroom", 120, 30, new Color(202, 200, 195)));
        board.add(BoardSpace.bonus("Allowance Deposit", 80));
        board.add(BoardSpace.property("Hockey Lane", 140, 35, new Color(196, 222, 244)));
        board.add(BoardSpace.tax("Phone Bill", 50));
        board.add(BoardSpace.property("Library Street", 160, 40, new Color(202, 231, 195)));
        board.add(BoardSpace.chance("Chance Card"));
        board.add(BoardSpace.property("Poutine Place", 180, 45, new Color(246, 216, 156)));
        board.add(BoardSpace.rest("Free Parking"));
        board.add(BoardSpace.property("Snowy Square", 200, 50, new Color(205, 221, 232)));
        board.add(BoardSpace.tax("Bus Pass Fee", 60));
        board.add(BoardSpace.property("Coding Cafe", 220, 55, new Color(238, 188, 188)));
        board.add(BoardSpace.bonus("Birthday Money", 100));
        board.add(BoardSpace.property("Laptop Lab", 240, 60, new Color(205, 202, 234)));
        board.add(BoardSpace.chance("Chance Card"));
        board.add(BoardSpace.property("Vancouver View", 260, 70, new Color(183, 218, 207)));
        board.add(BoardSpace.tax("Winter Coat Tax", 75));
        board.add(BoardSpace.property("Robotics Road", 280, 75, new Color(238, 199, 226)));
        board.add(BoardSpace.bonus("Part-Time Pay", 120));
        board.add(BoardSpace.property("Final Project Plaza", 320, 90, new Color(240, 224, 151)));
    }

    public void rollDiceAndMove() {
        if (gameOver || rolledThisTurn) {
            return;
        }

        Player player = getCurrentPlayer();
        int dieOne = random.nextInt(6) + 1;
        int dieTwo = random.nextInt(6) + 1;
        int total = dieOne + dieTwo;
        lastDieOne = dieOne;
        lastDieTwo = dieTwo;
        diceText = dieOne + " + " + dieTwo + " = " + total;
        rolledThisTurn = true;

        int oldPosition = player.getPosition();
        int newPosition = (oldPosition + total) % board.size();
        player.setPosition(newPosition);

        addLog(player.getName() + " rolled " + total + " and moved to " + board.get(newPosition).getName() + ".");

        if (oldPosition + total >= board.size()) {
            player.getDebitCard().deposit(PASS_START_BONUS);
            addLog(player.getName() + " passed Start and received " + PASS_START_BONUS + " smollars.");
            int scoreAdded = player.getCreditCard().raiseCreditScore(PASS_START_CREDIT_BONUS);
            if (scoreAdded > 0) {
                addLog(player.getName() + "'s credit score rose by " + scoreAdded + ".");
            }
        }

        handleLanding(player, board.get(newPosition));
    }

    public void buyCurrentProperty() {
        if (!canCurrentPlayerBuy()) {
            return;
        }

        Player player = getCurrentPlayer();
        BoardSpace space = board.get(player.getPosition());

        boolean paid = player.getDebitCard().withdraw(space.getPrice());
        if (paid) {
            space.setOwner(player);
            player.addProperty();
            addLog(player.getName() + " bought " + space.getName() + " for " + space.getPrice() + " smollars.");
            checkBrokeAfterPayment(player);
        }
    }

    public void takeCurrentPlayerLoan() {
        if (!canCurrentPlayerBorrow()) {
            return;
        }

        Player player = getCurrentPlayer();
        int loanAmount = player.getCreditCard().takeLoan();
        player.getDebitCard().deposit(loanAmount);
        loanTakenThisTurn = true;

        addLog(player.getName() + " borrowed " + loanAmount + " smollars using a credit card.");
        addLog(player.getName() + "'s credit score is now " + player.getCreditCard().getCreditScore() + "/90.");
    }

    public void finishTurn() {
        if (gameOver || !rolledThisTurn) {
            return;
        }

        Player player = getCurrentPlayer();
        player.addTurnTaken();

        if (winMode == WinMode.ROUND_LIMIT && everyoneFinishedTwentyTurns()) {
            endByRoundLimit();
            return;
        }

        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        rolledThisTurn = false;
        loanTakenThisTurn = false;
        diceText = "not rolled yet";
        addLog("Next turn: " + getCurrentPlayer().getName() + ".");
    }

    private void handleLanding(Player player, BoardSpace space) {
        if (space.getType() == SpaceType.START) {
            addLog(player.getName() + " landed on Start.");
        } else if (space.getType() == SpaceType.PROPERTY) {
            handleProperty(player, space);
        } else if (space.getType() == SpaceType.TAX) {
            payBank(player, space.getAmount(), space.getName());
        } else if (space.getType() == SpaceType.BONUS) {
            player.getDebitCard().deposit(space.getAmount());
            addLog(player.getName() + " received " + space.getAmount() + " smollars from " + space.getName() + ".");
        } else if (space.getType() == SpaceType.CHANCE) {
            handleChance(player);
        } else {
            addLog(player.getName() + " is resting at " + space.getName() + ".");
        }
    }

    private void handleProperty(Player player, BoardSpace property) {
        Player owner = property.getOwner();

        if (owner == null) {
            addLog(property.getName() + " is for sale: " + property.getPrice() + " smollars. Rent is " + property.getRent() + ".");
            if (player.getDebitCard().getBalance() < property.getPrice()) {
                addLog(player.getName() + " cannot afford this property right now.");
            }
        } else if (owner == player) {
            addLog(player.getName() + " already owns " + property.getName() + ".");
        } else {
            int paid = transferMoney(player, owner, property.getRent());
            addLog(player.getName() + " paid " + paid + " smollars of rent to " + owner.getName() + ".");
            checkBrokeAfterPayment(player);
        }
    }

    private void handleChance(Player player) {
        int eventNumber = random.nextInt(6);

        if (eventNumber == 0) {
            player.getDebitCard().deposit(90);
            addLog(player.getName() + " found a refund cheque and gained 90 smollars.");
        } else if (eventNumber == 1) {
            payBank(player, 70, "a broken phone screen");
        } else if (eventNumber == 2) {
            player.getDebitCard().deposit(110);
            addLog(player.getName() + " finished a school project and gained 110 smollars.");
        } else if (eventNumber == 3) {
            payBank(player, 45, "snacks for the class");
        } else if (eventNumber == 4) {
            player.getDebitCard().deposit(60);
            addLog(player.getName() + " sold an old textbook and gained 60 smollars.");
        } else {
            payBank(player, 100, "a surprise debit card fee");
        }
    }

    private void payBank(Player player, int amount, String reason) {
        int paid = player.getDebitCard().withdrawUpTo(amount);
        addLog(player.getName() + " paid " + paid + " smollars for " + reason + ".");
        checkBrokeAfterPayment(player);
    }

    private int transferMoney(Player payer, Player receiver, int amount) {
        int paid = payer.getDebitCard().withdrawUpTo(amount);
        receiver.getDebitCard().deposit(paid);
        return paid;
    }

    private void checkBrokeAfterPayment(Player player) {
        if (winMode == WinMode.BANKRUPTCY && player.getDebitCard().getBalance() <= 0) {
            endByBankruptcy(player);
        }
    }

    private boolean everyoneFinishedTwentyTurns() {
        for (Player player : players) {
            if (player.getTurnsTaken() < MAX_TURNS) {
                return false;
            }
        }
        return true;
    }

    private void endByRoundLimit() {
        int highestBalance = -1;
        List<String> winnerNames = new ArrayList<String>();

        for (Player player : players) {
            int balance = player.getDebitCard().getBalance();
            if (balance > highestBalance) {
                highestBalance = balance;
                winnerNames.clear();
                winnerNames.add(player.getName());
            } else if (balance == highestBalance) {
                winnerNames.add(player.getName());
            }
        }

        gameOver = true;
        winnerMessage = "20 turns are finished. Winner: " + joinNames(winnerNames)
                + " with " + highestBalance + " smollars.";
        addLog(winnerMessage);
    }

    private void endByBankruptcy(Player brokePlayer) {
        int highestBalance = -1;
        List<String> winnerNames = new ArrayList<String>();

        for (Player player : players) {
            if (player == brokePlayer) {
                continue;
            }

            int balance = player.getDebitCard().getBalance();
            if (balance > highestBalance) {
                highestBalance = balance;
                winnerNames.clear();
                winnerNames.add(player.getName());
            } else if (balance == highestBalance) {
                winnerNames.add(player.getName());
            }
        }

        gameOver = true;
        winnerMessage = brokePlayer.getName() + "Has no money. Winner is: " + joinNames(winnerNames)
                + " with " + highestBalance + " smollars.";
        addLog(winnerMessage);
    }

    public boolean canCurrentPlayerBuy() {
        if (gameOver || !rolledThisTurn) {
            return false;
        }

        Player player = getCurrentPlayer();
        BoardSpace space = board.get(player.getPosition());
        return space.getType() == SpaceType.PROPERTY
                && space.getOwner() == null
                && player.getDebitCard().getBalance() >= space.getPrice();
    }

    public boolean canCurrentPlayerBorrow() {
        return !gameOver && !loanTakenThisTurn && getCurrentPlayer().getCreditCard().getAvailableLoanAmount() > 0;
    }

    public String getCurrentLoanButtonText() {
        int amount = getCurrentPlayer().getCreditCard().getAvailableLoanAmount();
        if (loanTakenThisTurn) {
            return "Loan Taken This Turn";
        }
        if (amount <= 0) {
            return "No Credit Loan Available";
        }
        return "Take Credit Loan (" + amount + ")";
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public List<BoardSpace> getBoard() {
        return Collections.unmodifiableList(board);
    }

    public boolean hasRolledThisTurn() {
        return rolledThisTurn;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public String getDiceText() {
        return diceText;
    }

    public int getLastDieOne() {
        return lastDieOne;
    }

    public int getLastDieTwo() {
        return lastDieTwo;
    }

    public String getModeText() {
        if (winMode == WinMode.ROUND_LIMIT) {
            return "20 rounds, most smollars wins";
        }
        return "bankruptcy, most smollars wins";
    }

    public String getTurnSummary() {
        List<String> summaries = new ArrayList<String>();
        for (Player player : players) {
            summaries.add(player.getName() + " " + player.getTurnsTaken() + "/" + MAX_TURNS);
        }
        return joinNames(summaries);
    }

    public String getLogText() {
        StringBuilder text = new StringBuilder();
        for (String entry : log) {
            text.append(entry).append("\n");
        }
        return text.toString();
    }

    public String getWinnerMessage() {
        return winnerMessage;
    }

    public boolean hasShownGameOverPopup() {
        return shownGameOverPopup;
    }

    public void markGameOverPopupShown() {
        shownGameOverPopup = true;
    }

    private void addLog(String message) {
        log.add(message);
        if (log.size() > 80) {
            log.remove(0);
        }
    }

    private String joinNames(List<String> names) {
        if (names.size() == 0) {
            return "";
        }
        if (names.size() == 1) {
            return names.get(0);
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) {
                result.append(i == names.size() - 1 ? " and " : ", ");
            }
            result.append(names.get(i));
        }
        return result.toString();
    }
}

class BoardSpace {
    private final String name;
    private final SpaceType type;
    private final int price;
    private final int rent;
    private final int amount;
    private final Color displayColor;
    private Player owner;

    private BoardSpace(String name, SpaceType type, int price, int rent, int amount, Color displayColor) {
        this.name = name;
        this.type = type;
        this.price = price;
        this.rent = rent;
        this.amount = amount;
        this.displayColor = displayColor;
    }

    public static BoardSpace start(String name) {
        return new BoardSpace(name, SpaceType.START, 0, 0, 0, new Color(166, 216, 180));
    }

    public static BoardSpace property(String name, int price, int rent, Color color) {
        return new BoardSpace(name, SpaceType.PROPERTY, price, rent, 0, color);
    }

    public static BoardSpace tax(String name, int amount) {
        return new BoardSpace(name, SpaceType.TAX, 0, 0, amount, new Color(246, 183, 163));
    }

    public static BoardSpace bonus(String name, int amount) {
        return new BoardSpace(name, SpaceType.BONUS, 0, 0, amount, new Color(180, 218, 174));
    }

    public static BoardSpace chance(String name) {
        return new BoardSpace(name, SpaceType.CHANCE, 0, 0, 0, new Color(247, 230, 151));
    }

    public static BoardSpace rest(String name) {
        return new BoardSpace(name, SpaceType.REST, 0, 0, 0, new Color(219, 222, 225));
    }

    public String getName() {
        return name;
    }

    public SpaceType getType() {
        return type;
    }

    public int getPrice() {
        return price;
    }

    public int getRent() {
        return rent;
    }

    public int getAmount() {
        return amount;
    }

    public Color getDisplayColor() {
        return displayColor;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public String getShortInfo() {
        if (type == SpaceType.PROPERTY) {
            if (owner == null) {
                return price + " sm / rent " + rent;
            }
            return "Owned by " + owner.getName();
        } else if (type == SpaceType.TAX) {
            return "-" + amount;
        } else if (type == SpaceType.BONUS) {
            return "+" + amount;
        } else if (type == SpaceType.START) {
            return "+100 passing";
        } else if (type == SpaceType.CHANCE) {
            return "random";
        }
        return "rest";
    }
}

class Player {
    private final String name;
    private final Color color;
    private final DebitCard debitCard;
    private final CreditCard creditCard;
    private int position;
    private int turnsTaken;
    private int propertiesOwned;

    public Player(String name, Color color, DebitCard debitCard, CreditCard creditCard) {
        this.name = name;
        this.color = color;
        this.debitCard = debitCard;
        this.creditCard = creditCard;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public DebitCard getDebitCard() {
        return debitCard;
    }

    public CreditCard getCreditCard() {
        return creditCard;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getTurnsTaken() {
        return turnsTaken;
    }

    public void addTurnTaken() {
        turnsTaken++;
    }

    public int getPropertiesOwned() {
        return propertiesOwned;
    }

    public void addProperty() {
        propertiesOwned++;
    }
}

class CreditCard {
    private static final int MAX_CREDIT_SCORE = 90;

    private final String cardNumber;
    private int creditScore;
    private int loanBalance;

    public CreditCard(String cardNumber, int startingCreditScore) {
        this.cardNumber = cardNumber;
        creditScore = Math.min(MAX_CREDIT_SCORE, startingCreditScore);
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public int getCreditScore() {
        return creditScore;
    }

    public int getLoanBalance() {
        return loanBalance;
    }

    public int getAvailableLoanAmount() {
        if (creditScore > 80) {
            return 500;
        } else if (creditScore > 60) {
            return 300;
        } else if (creditScore > 50) {
            return 100;
        }
        return 0;
    }

    public int takeLoan() {
        int amount = getAvailableLoanAmount();
        loanBalance += amount;
        lowerCreditScore(10);
        return amount;
    }

    public int raiseCreditScore(int amount) {
        int oldScore = creditScore;
        creditScore = Math.min(MAX_CREDIT_SCORE, creditScore + amount);
        return creditScore - oldScore;
    }

    private void lowerCreditScore(int amount) {
        creditScore = Math.max(0, creditScore - amount);
    }
}

class DebitCard {
    private final String cardNumber;
    private int balance;

    public DebitCard(String cardNumber, int startingBalance) {
        this.cardNumber = cardNumber;
        balance = startingBalance;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public int getBalance() {
        return balance;
    }

    public void deposit(int amount) {
        balance += amount;
    }

    public boolean withdraw(int amount) {
        if (balance < amount) {
            return false;
        }
        balance -= amount;
        return true;
    }

    public int withdrawUpTo(int amount) {
        int paid = Math.min(balance, amount);
        balance -= paid;
        return paid;
    }
}

enum WinMode {
    ROUND_LIMIT,
    BANKRUPTCY
}

enum SpaceType {
    START,
    PROPERTY,
    TAX,
    BONUS,
    CHANCE,
    REST
}
