package uno;

import java.util.ArrayList;
import java.util.List;
import uno.UnoPlayer.Color;

/**
 * <p>
 * A GameState object provides programmatic access to certain (legal) aspects of
 * an Uno game, so that interested players can take advantage of that
 * information. Note that not all aspects of a game's state (<i>e.g.</i>, the
 * direction of play, whose turn it is next, the actual cards in each player's
 * hand (!), etc.) are reflected in the GameState object -- only those for which
 * it makes sense for a player to have access.</p>
 *
 * @since 2.0
 */
public class GameState {

    private Game game;
    private int[] numCardsInHandsOfUpcomingPlayers;
    private Color[] mostRecentColorCalledByUpcomingPlayers;

    /**
     * Blank constructor used for the TestCaseProcessor class
     */
    public GameState() {
        game = null;
        numCardsInHandsOfUpcomingPlayers = new int[4];
        mostRecentColorCalledByUpcomingPlayers = new Color[4];
    }

    /**
     * Instantiate a new GameState object whose job it is to provide safe access
     * to the Game object passed.
     */
    public GameState(Game game) {

        this.game = game;
        int numPlayers = game.getNumPlayers();

        numCardsInHandsOfUpcomingPlayers = new int[numPlayers];
        mostRecentColorCalledByUpcomingPlayers = new UnoPlayer.Color[numPlayers];

        if (game.isMovingForwards()) {
            for (int i = 0; i < numPlayers; i++) {
                numCardsInHandsOfUpcomingPlayers[i] = game.getPlayerAt(game.getCurrPosition() + i + 1).size();
                mostRecentColorCalledByUpcomingPlayers[i] = game.getMostRecentColorCalled(game.getCurrPosition() + i + 1);
            }
        } else {
            for (int i = 0; i < numPlayers; i++) {
                numCardsInHandsOfUpcomingPlayers[i] = game.getPlayerAt(game.getCurrPosition() - i - 1).size();
                mostRecentColorCalledByUpcomingPlayers[i] = game.getMostRecentColorCalled(game.getCurrPosition() - i - 1);
            }
        }
    }

    /**
     * Return an array of ints indicating the number of cards each player has
     * remaining. The array is ordered so that index 0 has the count for the
     * player who (barring action cards that might change it) will play next,
     * index 1 the player who (barring action cards) will play second, etc.
     */
    public int[] getNumCardsInHandsOfUpcomingPlayers() {
        return numCardsInHandsOfUpcomingPlayers;
    }

    /**
     * Return the color most recently "called" (after playing a wild) by each
     * opponent. If a given opponent has not played a wild card this game, the
     * value will be Color.NONE. The array is ordered so that index 0 has the
     * count for the player who (barring action cards that might change it) will
     * play next, index 1 the player who (barring action cards) will play
     * second, etc.
     */
    public Color[] getMostRecentColorCalledByUpcomingPlayers() {
        return mostRecentColorCalledByUpcomingPlayers;
    }

    /**
     * Return a list of <i>all</i> cards that have been played since the last
     * time the deck was remixed. This allows players to "card count" if they
     * choose.
     */
    public List<Card> getPlayedCards() {
        if (game == null) {
            return new ArrayList();
        }
        return game.getDeck().getDiscardedCards();
    }
}
