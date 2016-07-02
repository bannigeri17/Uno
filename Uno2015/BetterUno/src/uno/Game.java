package uno;

import java.util.ArrayList;
import uno.UnoPlayer.Color;

/**
 * <p>
 * A Game object represents a single game of Uno in an overall match (of
 * possibly many games). Games are instantiated by providing them with a roster
 * of players, including a Scoreboard object through which scores can be
 * accumulated. The play() method then kicks off the game, which will proceed
 * from start to finish and update the Scoreboard. Various aspects of the game's
 * state (<i>e.g.</i>, whether the movingForwards of play is currently clockwise
 * or counterclockwise, whose player's turn is next) can be accessed and
 * controlled through methods on this class.</p>
 * <p>
 * A GameState object can be obtained through the getGameState() method, which
 * allows UnoPlayers to selectively and legally examine certain aspects of the
 * game's state.</p>
 *
 * @since 1.0
 */
public class Game {

    private int numPlayers;
    private Deck deck;
    private Hand[] playerHands;
    private Card upCard;
    private boolean movingForwards;
    private int currPosition;
    private Color calledColor;
    private Color mostRecentColorCalled[];

    /**
     * Main constructor to instantiate a Game of Uno. Provided must be two
     * objects indicating the player roster: a Scoreboard, and a class list.
     * This constructor will deal hands to all players, select a non-action up
     * card, and reset all game settings so that play() can be safely called.
     *
     * @param playerNames A list of the names of the players
     * @param playerClasses A list of the class names of the players, minus the
     * _UnoPlayer bit
     */
    public Game(ArrayList<String> playerNames, ArrayList<String> playerClasses) {
        numPlayers = playerNames.size();
        deck = new Deck();
        playerHands = new Hand[numPlayers];
        mostRecentColorCalled = new Color[numPlayers];
        try {
            for (int i = 0; i < numPlayers; i++) {
                playerHands[i] = new Hand(playerClasses.get(i), playerNames.get(i));
                for (int j = 0; j < UnoSimulation.INIT_HAND_SIZE; j++) {
                    playerHands[i].addCard(deck.draw());
                }
            }
            upCard = deck.draw();
            while (upCard.followedByCall()) {
                deck.discard(upCard);
                upCard = deck.draw();
            }
        } catch (EmptyDeckException e) {
            System.out.println("ERROR: Not enough cards in deck to draw initial hands");
            System.exit(1);
        }
        movingForwards = Math.random() < .5;
        currPosition = (int) (Math.random() * numPlayers);
        calledColor = Color.NONE;
    }

    public void printState() {
        for (int i = 0; i < numPlayers; i++) {
            System.out.println("Hand #" + i + ": " + playerHands[i]);
        }
    }

    /**
     * Return the number of the <i>next</i> player to play, provided the current
     * player doesn't jack that up by playing an action card.
     *
     * @return An integer from 0 to numPlayers-1.
     */
    public int getNextPosition() {
        if (movingForwards) {
            return (currPosition + 1) % numPlayers;
        } else {
            if (currPosition == 0) {
                return numPlayers - 1;
            } else {
                return currPosition - 1;
            }
        }
    }

    /**
     * Go ahead and advance to the next player.
     */
    public void advanceToNextPlayer() {
        currPosition = getNextPosition();
    }

    /**
     * Change the movingForwards of the game from clockwise to counterclockwise
     * (or vice versa.)
     */
    public void reverseDirection() {
        movingForwards = !movingForwards;
    }

    public Card draw() {
        try {
            if (deck.isEmpty()) {
                print("...deck exhausted, remixing...");
                deck.remix();
            }
            return deck.draw();
        } catch (EmptyDeckException e) {
            System.out.println("Error: Cannot draw card");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Play an entire Game of Uno from start to finish. Hands should have
     * already been dealt before this method is called, and a valid up card
     * turned up. When the method is completed, the Game's scoreboard object
     * will have been updated with new scoring favoring the winner.
     *
     * @return Data on the winning player
     */
    public Victory play() {
        //Print the initial card
        println("Initial upcard is " + upCard + ".");
        Hand currPlayer = playerHands[currPosition];
        //While the game isn't over
        while (true) {
            currPlayer = playerHands[currPosition];
            //Prints the current player and their hand
            print(currPlayer.getPlayerName() + " (" + currPlayer + ")");
            //Gets the card to be played
            Card playedCard = currPlayer.play(this);
            //If they cannot play a card at first, draw
            if (playedCard == null) {
                //Get the drawn card
                Card drawnCard = draw();
                //Add it to the player's hand
                currPlayer.addCard(drawnCard);
                //Say so
                print(" has to draw (" + drawnCard + ").");
                //See if they can play now
                playedCard = currPlayer.play(this);
            }
            //If they don't pass
            if (playedCard != null) {
                //Say what they play
                print(" plays " + playedCard + " on " + upCard + ".");
                //Add it to the discard
                deck.discard(upCard);
                //Set it as the upCard
                upCard = playedCard;
                //If they need to call
                if (upCard.followedByCall()) {
                    //Find the call
                    calledColor = currPlayer.callColor(this);
                    //Set the call
                    mostRecentColorCalled[currPosition] = calledColor;
                    //Print the call
                    print(" (and calls " + calledColor + ").");
                } else {
                    //Set the call to nothing
                    calledColor = Color.NONE;
                }
            }
            //Check if game is over
            if (currPlayer.isEmpty()) {
                break;
            }
            //Print UNO if relevant
            if (currPlayer.size() == 1) {
                print(" UNO!");
            }
            println("");
            //Affect the game with the card
            if (playedCard != null) {
                playedCard.performCardEffect(this);
            } else {
                advanceToNextPlayer();
            }
        }
        //Calculate the score
        int roundPoints = 0;
        for (Hand p : playerHands) {
            roundPoints += p.countCards();
        }
        //Say stuff
        if (UnoSimulation.USE_SCORE) {
            print("\n" + currPlayer.getPlayerName() + " wins! (and collects " + roundPoints + " points.)");
        } else {
            println("\n" + currPlayer.getPlayerName() + " wins!");
        }
        return new Victory(currPosition, roundPoints);
    }

    public void print(String s) {
        if (UnoSimulation.PRINT_VERBOSE) {
            System.out.print(s);
        }
    }

    public void println(String s) {
        if (UnoSimulation.PRINT_VERBOSE) {
            System.out.println(s);
        }
    }

    /**
     * Return the GameState object, through which the state of the game can be
     * accessed and safely manipulated.
     */
    public GameState getGameState() {
        return new GameState(this);
    }

    /**
     * Return the Card that is currently the "up card" in the game.
     */
    public Card getUpCard() {
        return upCard;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public boolean isMovingForwards() {
        return movingForwards;
    }

    public Color getCalledColor() {
        return calledColor;
    }

    public Deck getDeck() {
        return deck;
    }

    public Hand getPlayerAt(int pos) {
        return playerHands[(pos + playerHands.length) % playerHands.length];
    }

    public int getCurrPosition() {
        return currPosition;
    }

    public Color getMostRecentColorCalled(int pos) {
        return mostRecentColorCalled[(pos + playerHands.length) % playerHands.length];
    }

}
