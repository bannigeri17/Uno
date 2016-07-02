package uno;

import java.util.ArrayList;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * <p>
 * An entire terminal-based simulation of a multi-game Uno match. Command-line
 * switches can control certain aspects of the game. Output is provided to the
 * screen about game flow and final playerScores.</p>
 */
public class UnoSimulation {

    //How many games to be played
    public static final int NUM_GAMES = 100000;
    //Whether text appears when running matches (turn OFF when running large a large trial)
    public static boolean PRINT_VERBOSE = false;
    //Whether to print the color of the card as a letter or as a color on the screen
    public static final boolean PRINT_IN_COLOR = true;
    //The number of cards each player will be dealt at start of game.
    public static final int INIT_HAND_SIZE = 7;
    //Whether to count score or just rounds won
    public static final boolean USE_SCORE = true;
    /**
     * <p>
     * The name of a file (relative to working directory) containing
     * comma-separated lines, each of which contains a player name (unrestricted
     * text) and the <i>prefix</i> of the (package-less) class name (implementer
     * of UnoPlayer) that player will use as a playing strategy.</p>
     *
     * For example, if the file contained these lines:
     * <pre>
     * Fred,fsmith
     * Jane,jdoe
     * Billy,bbob
     * Thelma,tlou
     * </pre> then the code would pit Fred (whose classname was
     * "uno.fsmith_UnoPlayer") against Jane (whose classname was
     * "uno.jdoe_Unoplayer") against, Billy,... etc.
     */
    public static final String PLAYER_FILENAME = "players.txt";
    private ArrayList<String> playerNames = new ArrayList();
    private ArrayList<String> playerClasses = new ArrayList();
    private ArrayList<Integer> playerWins = new ArrayList();
    private ArrayList<Integer> playerScores = new ArrayList();

    /**
     * Run an Uno simulation of some number of games pitting some set of
     * opponents against each other. The mandatory command-line argument
     * (numberOfGames) should contain an integer specifying how many games to
     * play in the match. The optional second command-line argument should be
     * either the word "verbose" or "quiet" and controls the magnitude of
     * output.
     */
    public static void main(String args[]) {
        UnoSimulation sim = new UnoSimulation();
        sim.run();
        sim.display();
    }

    public void run() {
        try {
            loadPlayerData();
            for (int i = 0; i < NUM_GAMES; i++) {
                Game g = new Game(playerNames, playerClasses);
                Victory v = g.play();
                playerWins.set(v.winningPlayer, playerWins.get(v.winningPlayer) + 1);
                playerScores.set(v.winningPlayer, playerScores.get(v.winningPlayer) + v.score);
            }
            display();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPlayerData() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(PLAYER_FILENAME));
        String playerLine = br.readLine();
        while (playerLine != null) {
            Scanner line = new Scanner(playerLine).useDelimiter(",");
            playerNames.add(line.next());
            playerClasses.add("uno." + line.next() + "_UnoPlayer");
            playerWins.add(0);
            playerScores.add(0);
            playerLine = br.readLine();
        }
    }

    public void display() {
        int maxNameLength = 0;
        for (String s : playerNames) {
            if (s.length() > maxNameLength) {
                maxNameLength = s.length();
            }
        }
        int totalWins = 0;
        for (int i : playerWins) {
            totalWins += i;
        }
        int totalScore = 0;
        for (int i : playerScores) {
            totalScore += i;
        }
        String toPrint = "";
        for (int i = 0; i < playerNames.size(); i++) {
            toPrint += "\n" + playerNames.get(i) + ":  ";
            for (int j = playerNames.get(i).length(); j < maxNameLength; j++) {
                toPrint += " ";
            }
            toPrint += playerWins.get(i) + " wins (" + Math.round(playerWins.get(i) * 100. / totalWins) + "%)";
            if (USE_SCORE) {
                toPrint += "  " + playerScores.get(i) + " points (" + Math.round(playerScores.get(i) * 100. / totalScore) + "%)";
            }
        }
        System.out.println(toPrint);
    }
}
