package uno;

import uno.UnoPlayer.Color;
import uno.UnoPlayer.Rank;
import static uno.UnoPlayer.Rank.*;

/**
 * <p>
 * A Card in an Uno deck. Each Card knows its particular type, which is
 * comprised of a 3-tuple (color, rank, number). Not all of these values are
 * relevant for every particular type of card, however; for instance, wild cards
 * have no color (getColor() will return Color.NONE) or number (getNumber() will
 * return -1).</p>
 * <p>
 * A Card knows its forfeit cost (<i>i.e.</i>, how many points it counts against
 * a loser who gets stuck with it) and how it should act during game play
 * (whether it permits the player to change the color, what effect it has on the
 * game state, etc.)</p>
 *
 * @since 1.0
 */
public class Card {

    private Color color;
    private Rank rank;
    private int number;

    /**
     * Constructor for non-number cards (skips, wilds, etc.)
     *
     * @param color The color of the card
     * @param rank The type of the card
     */
    public Card(Color color, Rank rank) {
        this.color = color;
        this.rank = rank;
        this.number = -1;
    }

    /**
     * Constructor for number cards.
     *
     * @param color The color of the card
     * @param number The number on the card
     */
    public Card(Color color, int number) {
        this.color = color;
        this.rank = NUMBER;
        this.number = number;
    }

    /**
     * Constructor to explicitly set entire card state.
     *
     * @param color The color of the card
     * @param rank The type of the card
     * @param number The number on the card
     */
    public Card(Color color, Rank rank, int number) {
        this.color = color;
        this.rank = rank;
        this.number = number;
    }

    /**
     * Render this Card object as a string. Whether the string comes out with
     * ANSI color codes is controlled by the PRINT_IN_COLOR static class
     * variable.
     *
     * @return The String representation of the card
     */
    @Override
    public String toString() {
        String retval = "";
        if (UnoSimulation.PRINT_IN_COLOR) {
            switch (color) {
                case RED:
                    retval += "\033[31m";
                    break;
                case YELLOW:
                    retval += "\033[33m";
                    break;
                case GREEN:
                    retval += "\033[32m";
                    break;
                case BLUE:
                    retval += "\033[34m";
                    break;
                case NONE:
                    retval += "\033[1m";
                    break;
            }
        } else {
            switch (color) {
                case RED:
                    retval += "R";
                    break;
                case YELLOW:
                    retval += "Y";
                    break;
                case GREEN:
                    retval += "G";
                    break;
                case BLUE:
                    retval += "B";
                    break;
                case NONE:
                    retval += "";
                    break;
            }
        }
        switch (rank) {
            case NUMBER:
                retval += number;
                break;
            case SKIP:
                retval += "S";
                break;
            case REVERSE:
                retval += "R";
                break;
            case WILD:
                retval += "W";
                break;
            case DRAW_TWO:
                retval += "+2";
                break;
            case WILD_D4:
                retval += "W4";
                break;
        }
        if (UnoSimulation.PRINT_IN_COLOR) {
            retval += "\033[37m\033[0m";
        }
        return retval;
    }

    /**
     * Returns the number of points this card will count against a player who
     * holds it in his/her hand when another player goes out.
     *
     * @return The point value of the card
     */
    public int forfeitCost() {
        if (rank == SKIP || rank == REVERSE
                || rank == DRAW_TWO) {
            return 20;
        }
        if (rank == WILD || rank == WILD_D4) {
            return 50;
        }
        if (rank == NUMBER) {
            return number;
        }
        System.out.println("Error: Tried to find the value of an illegal card: " + toString());
        return 0;
    }

    /**
     * Returns true only if this Card can legally be played on the up card
     * passed as an argument. The second argument is relevant only if the up
     * card is a wild.
     *
     * @param c An "up card" upon which the current object might (or might not)
     * be a legal play.
     * @param calledColor If the up card is a wild card, this parameter contains
     * the color the player of that color called.
     * @return Whether the card is a legal move
     */
    public boolean canPlayOn(Card c, UnoPlayer.Color calledColor) {
        return rank == WILD
                || rank == WILD_D4
                || color == c.color
                || color == calledColor
                || (rank == c.rank && rank != NUMBER)
                || number == c.number && rank == NUMBER && c.rank == NUMBER;
    }

    /**
     * Returns true only if playing this Card object would result in the player
     * being asked for a color to call. (In the standard game, this is true only
     * for wild cards.)
     *
     * @return Whether you need to specify a color
     */
    public boolean followedByCall() {
        return rank == WILD || rank == WILD_D4;
    }

    /**
     * This method should be called immediately after a Card is played, and will
     * trigger the effect peculiar to that card. For most cards, this merely
     * advances play to the next player. Some special cards have other effects
     * that modify the game state. Examples include a Skip, which will advance
     * <i>twice</i> (past the next player), or a Draw Two, which will cause the
     * next player to have to draw cards.
     *
     * @param game The Game being played, whose state may be modified by this
     * card's effect.
     */
    public void performCardEffect(Game game) {
        switch (rank) {
            case SKIP:
                game.advanceToNextPlayer();
                game.advanceToNextPlayer();
                break;
            case REVERSE:
                game.reverseDirection();
                game.advanceToNextPlayer();
                break;
            case DRAW_TWO:
                nextPlayerDraw(game);
                nextPlayerDraw(game);
                game.advanceToNextPlayer();
                game.advanceToNextPlayer();
                break;
            case WILD_D4:
                nextPlayerDraw(game);
                nextPlayerDraw(game);
                nextPlayerDraw(game);
                nextPlayerDraw(game);
                game.advanceToNextPlayer();
                game.advanceToNextPlayer();
                break;
            default:
                game.advanceToNextPlayer();
                break;
        }
    }

    private void nextPlayerDraw(Game game) {
        Hand nextPlayer = game.getPlayerAt(game.getNextPosition());
        nextPlayer.addCard(game.draw());
    }

    /**
     * Returns the color of this card, which is Color.NONE in the case of wild
     * cards.
     *
     * @return The color of this card
     */
    public Color getColor() {
        return color;
    }

    /**
     * Returns the rank of this card, which is Rank.NUMBER in the case of number
     * cards (calling getNumber() will retrieve the specific number.)
     *
     * @return The type of this card
     */
    public Rank getRank() {
        return rank;
    }

    /**
     * Returns the number of this card, which is guaranteed to be -1 for
     * non-number cards (cards of non-Rank.NUMBER rank.)
     *
     * @return The number of this card
     */
    public int getNumber() {
        return number;
    }
}
