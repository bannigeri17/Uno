//TEAM NUMERO UNO


package uno;
import java.util.ArrayList;
import java.util.List;

public class TeamNumeroUno_UnoPlayer implements UnoPlayer {

    //The weights serve to establish a priority for special (skip, draw 2 or reverse) cards, when determining 
    //whether or not to switch colors or what color to switch to. More specifically, the ratio between the 
    //weights determines how much more we want to switch to a certain color based on how many specials it    
    //has. These variables come into play in the generateScore() method.

    //Weights determined by extensive testing, used in generating scores later
    private final double normalWeight = 1; //Weight given to normal, number cards
    private double specialWeight = 0; //Weight given to specials: skip, draw 2s and reverses, determined in play method
    
    /**
     * play - This method is called when it's your turn and you need to
     * choose what card to play.
     *
     * The hand parameter tells you what's in your hand. You can call
     * getColor(), getRank(), and getNumber() on each of the cards it
     * contains to see what it is. The color will be the color of the card,
     * or "Color.NONE" if the card is a wild card. The rank will be
     * "Rank.NUMBER" for all numbered cards, and another value (e.g.,
     * "Rank.SKIP," "Rank.REVERSE," etc.) for special cards. The value of
     * a card's "number" only has meaning if it is a number card. 
     * (Otherwise, it will be -1.)
     *
     * The upCard parameter works the same way, and tells you what the 
     * up card (in the middle of the table) is.
     *
     * The calledColor parameter only has meaning if the up card is a wild,
     * and tells you what color the player who played that wild card called.
     *
     * Finally, the state parameter is a GameState object on which you can 
     * invoke methods if you choose to access certain detailed information
     * about the game (like who is currently ahead, what colors each player
     * has recently called, etc.)
     *
     * You must return a value from this method indicating which card you
     * wish to play. If you return a number 0 or greater, that means you
     * want to play the card at that index. If you return -1, that means
     * that you cannot play any of your cards (none of them are legal plays)
     * in which case you will be forced to draw a card (this will happen
     * automatically for you.)
     */
    public int play(List<Card> hand, Card upCard, UnoPlayer.Color calledColor,
        GameState state) {
        
            if(specialWeight==0) specialWeight=state.getNumCardsInHandsOfUpcomingPlayers().length; //if the special weight is not initialized to a non-zero value, 
                                                                                                   //set it to the number of players in the game

            //TEST IF CAN'T PLAY A CARD
            if(handNotLegal(hand, upCard, calledColor)){ 
              return -1; //Return -1 if no card is playable
            }
            
             if(state.getNumCardsInHandsOfUpcomingPlayers()[0]==1){
                if(canUseASpecial(hand, upCard, calledColor)){
                    if(getBestSpecial(hand,calledColor)!=-1) return getBestSpecial(hand,calledColor);
                }
            }
            
            //PLAY ON A WILD
            if(upCard.getRank().equals(UnoPlayer.Rank.WILD)||upCard.getRank().equals(UnoPlayer.Rank.WILD_D4)){ //If playing on a wild...
                if(hasCardOfColor(hand,calledColor)){ //If the hand contains a card of the called color...
                    return getBestCard(hand,calledColor); //Return the hand’s best card of that color
                } 
                return getBestWildCard(hand); //Otherwise return the best wild card
            }
            
            
            //PLAY A WILD
            int count=0;
            for(Card c : hand){
                if(c.getColor().equals(upCard.getColor()))count++; //Count cards of the upCard color
            }
            if(count==0&&!canSwitch(hand,upCard)){ //If hand has no cards of upCard’s color and can’t switch colors...
                return getBestWildCard(hand); //Return the best wild card in the hand
            }
            
            //SWITCHING
            if(canSwitch(hand, upCard)){ //if you can switch colors...
                double switchingScore=getSwitchingScore(hand,upCard); //Generate the score representing the value of switching
                double stayingScore=getStayingScore(hand,upCard); //Generate a score representing the value of staying
                double newSScore = switchingScore-2; //Subtracting 2 generates a more accurate switching score as determined by testing
                if (newSScore<0) newSScore=0; //Make newSScore 0 in case it’s negative
                if(newSScore > stayingScore || stayingScore==0){ //If staying score is less than the switching score or staying score is 0...
                    return getSwitchCard(hand,upCard); //Return the best switch card
                } else {
                    return getBestCard(hand,upCard.getColor()); //Else, return best card of upCard’s color
                }
            } else {
            //DEFAULT
                return getBestCard(hand,upCard.getColor());//if you can’t switch, return best card of upCard’s color
            }
               
    }

    
    //This method checks if the hand has no playable cards
    public boolean handNotLegal(List<Card> hand, Card upCard,UnoPlayer.Color calledColor){
        for(Card c : hand){
            if(c.canPlayOn(upCard, upCard.getColor())||c.getColor().equals(calledColor)) return false; //Through checking each card in the hand, if any cards are playable, return false
        }
        return true; //Return true if no cards are playable
    }


    //Checks if the passed List of Cards contains any cards of the color parameter
    public boolean hasCardOfColor(List<Card> hand, UnoPlayer.Color color){
        for(Card c: hand){ //For each card in the hand...
            if(c.getColor().equals(color)) return true; //Return true once a card matching the color parameter is found
        }
        return false; //If no cards of the color parameter were found, return false
    }
    

    //Return true if it’s possible for the player to switch colors, given parameters of hand and upCard
    public boolean canSwitch(List<Card> hand, Card upCard){
        if (upCard.getRank().equals(UnoPlayer.Rank.WILD))return false; //Return false if the upCard is a Wild, and the player can’t change the color
        for(Card c : hand){        
            if(!c.getColor().equals(upCard.getColor())&&c.getRank().equals(upCard.getRank())&&c.getNumber()==upCard.getNumber())return true; //Return true if any card in the hand matches rank and number of the upCard, but not color.
        }
        return false; //Return false if no potential ‘switch’ cards are found
    }
    
    //Generate a score representing how viable it is to switch colors
    public double getSwitchingScore(List<Card> hand, Card upCard){
        
        //GENERATE SWITCHING COLOR, RANK, AND NUMBER 
        //AS WELL AS A SCORE
            double sScore; //Returned switch score, determined later
            double redScore,yellowScore,greenScore,blueScore; //A score representing each color’s ‘switch viability’

            List<UnoPlayer.Color> potentialColors = new ArrayList<>(); //List of potential colors to switch to
            for(Card c : hand){ //For each Card in hand...
                if(!c.getColor().equals(upCard.getColor())&&!potentialColors.contains(c.getColor())&&c.getRank().equals(upCard.getRank())&&c.getNumber()==upCard.getNumber()){ //if the card’s color is not already in potentialColors, and the card’s color is not the upCard’s color, and the card matches upCard’s rank and number...
                    potentialColors.add(c.getColor()); //Add the color to potential colors
                }
            }
            
            //Create 4 lists of cards representing individual colors
            List<Card> redCards = new ArrayList<>();
            List<Card> yellowCards = new ArrayList<>();
            List<Card> greenCards = new ArrayList<>();
            List<Card> blueCards = new ArrayList<>();

            //Sort the cards in hand into respective lists representing their color
            for(Card c : hand){
                if(c.getColor().equals(UnoPlayer.Color.RED)){
                    redCards.add(c);
                }
                if(c.getColor().equals(UnoPlayer.Color.YELLOW)){
                    yellowCards.add(c);
                }
                if(c.getColor().equals(UnoPlayer.Color.GREEN)){
                    greenCards.add(c);
                }
                if(c.getColor().equals(UnoPlayer.Color.BLUE)){
                    blueCards.add(c);
                }
            }

            //Generate a score for each of these lists.
            redScore = generateScore(redCards);
            yellowScore = generateScore(yellowCards);
            greenScore = generateScore(greenCards);
            blueScore = generateScore(blueCards);

            //Disqualify the color of the upCard, which is not in potentialColors.
            if(!potentialColors.contains(UnoPlayer.Color.RED))redScore=-1;
            if(!potentialColors.contains(UnoPlayer.Color.YELLOW))yellowScore=-1;
            if(!potentialColors.contains(UnoPlayer.Color.GREEN))greenScore=-1;
            if(!potentialColors.contains(UnoPlayer.Color.BLUE))blueScore=-1;

            //The overall switching score is set to the maximum of the generated color scores and returned
            sScore = Math.max(Math.max(redScore,yellowScore),Math.max(greenScore,blueScore));
            
        return sScore;
    }
    
    
    //Returns a double representing the viability of preserving upCard's color in your turn
    public double getStayingScore(List<Card> hand, Card upCard){
        //GENERATE STAYING COLOR, RANK, AND NUMBER
        //AS WELL AS A SCORE
            double Score;
            
            List<Card> sameColorCards = new ArrayList<>(); //Cards in hand that have the same color as the upCard
                    
            for(Card c: hand){
                if(c.getColor().equals(upCard.getColor())) sameColorCards.add(c);//Add the card to the sameColorCards list if they have the same color.
            }
            
            Score = generateScore(sameColorCards);//Generate the score of this list, representing the viability of maintaining color
            
        return Score;
    }
    
    
    /**
     * callColor - This method will be called when you have just played a
     * wild card, and is your way of specifying which color you want to 
     * change it to.
     *
     * You must return a valid Color value from this method. You must not
     * return the value Color.NONE under any circumstances.
     */
    public UnoPlayer.Color callColor(List<Card> hand) {

        //Count of 'normal' number cards in respective colors
        double yellowCount=0;
        double greenCount=0;
        double blueCount=0;
        double redCount=0;
        double yellowSCount=0;
        double greenSCount=0;
        double blueSCount=0;
        double redSCount=0;

        //Count the cards in hand, in terms of color and rank
        for(Card c : hand){
            if(c.getColor().equals(Color.RED)&&c.getRank().equals(Rank.NUMBER)) redCount++;
            if(c.getColor().equals(Color.BLUE)&&c.getRank().equals(Rank.NUMBER)) blueCount++;
            if(c.getColor().equals(Color.YELLOW)&&c.getRank().equals(Rank.NUMBER)) yellowCount++;
            if(c.getColor().equals(Color.GREEN)&&c.getRank().equals(Rank.NUMBER)) greenCount++;
            
            if(c.getColor().equals(Color.RED)&&!c.getRank().equals(Rank.NUMBER)) redSCount++;
            if(c.getColor().equals(Color.BLUE)&&!c.getRank().equals(Rank.NUMBER)) blueSCount++;
            if(c.getColor().equals(Color.YELLOW)&&!c.getRank().equals(Rank.NUMBER)) yellowSCount++;
            if(c.getColor().equals(Color.GREEN)&&!c.getRank().equals(Rank.NUMBER)) greenSCount++;
        }
        
        yellowCount=yellowSCount+yellowCount;
        greenCount=greenSCount+greenCount;
        blueCount=blueSCount+blueCount;
        redCount=redSCount+redCount;
        
        //Return the color with the highest count found.
        if(redCount>blueCount&&redCount>yellowCount&&redCount>greenCount) return Color.RED;
        if(blueCount>yellowCount&&blueCount>greenCount) return Color.BLUE;
        if(yellowCount>greenCount) return Color.YELLOW;
        else return Color.GREEN;
    }
 
    //A way to determine which color to switch to, using previously determined respective weights for normal 
    //and special cards. For example, if we are determining which color to switch to, and we have 3 normal 
    //red cards, and 2 special red cards, the red score generated is normalWeight*3+specialWeight*2. If we 
    //additionally had 2 normal yellow cards, and 4 special yellow cards, the yellow score would be 
    //normalWeight*2+specialWeight*4. In the case of our normalWeight and specialWeight values of 1 and 3 
    //respectively, this makes the red score 9 and the yellow score 14. It should be noted that the method 
    //does not account for color, it simply computes a score representative of how valuable the parameter     
    //List of Cards it is passed is to the player. Thus, to compute a score related to color, say, red, then all 
    //of the red cards must be sorted into a list and then passed into the method to compute a red score 
    //within a hand.
    public double generateScore(List<Card> cards){
        int normalCount=0; //A count of the 'normal' (number) cards within the list
        int specialCount=0; //A count of the 'special' (Skip, Reverse, Draw-2 and Wilds) cards within the list
    
        for(Card c : cards){ //For each card in the list parameter
            if(!c.getRank().equals(UnoPlayer.Rank.NUMBER)){
                normalCount++; //If rank is Rank.NUMBER, increase normal count
            } else {
                specialCount++; //Otherwise increase special count
            }
        }    
        
        return normalCount*normalWeight+specialCount*specialWeight; //Use predetermined weights as coefficients to respective counts and sum them to generate the score
    }

    //Returns the index of the best card with a different color from that of upCard. 
    private int getSwitchCard(List<Card> hand, Card upCard) {
        //GENERATE SWITCHING COLOR, RANK, AND NUMBER
            Color sColor=null; //optimal color to switch to
            double sScore; //score of the cards of this color
            double redScore,yellowScore,greenScore,blueScore; //Respective scores representing cards of the colors in the titles



            List<Color> potentialColors = new ArrayList<>(); //List of potential colors to switch to
            for(Card c : hand){ //For each card in hand
                if(!c.getColor().equals(upCard.getColor()) //If card's color doesn't match upCard's color
                        &&!potentialColors.contains(c.getColor()) //...And the color isn't already in potentialColors
                        &&c.getRank().equals(upCard.getRank()) //And the card matches upCard's rank...
                        &&c.getNumber()==upCard.getNumber()){ //And the card matches upCard's number...
                    potentialColors.add(c.getColor()); //Add the card's color to potentialColors
                }
            }

            //Lists representing cards in hand of respective colors
            List<Card> redCards = new ArrayList<>();
            List<Card> yellowCards = new ArrayList<>();
            List<Card> greenCards = new ArrayList<>();
            List<Card> blueCards = new ArrayList<>();

            //Sort the cards in hand into the lists by adding them to theit respective lists by color
            for(Card c : hand){
                if(c.getColor().equals(UnoPlayer.Color.RED)){
                    redCards.add(c);
                }
                if(c.getColor().equals(UnoPlayer.Color.YELLOW)){
                    yellowCards.add(c);
                }
                if(c.getColor().equals(UnoPlayer.Color.GREEN)){
                    greenCards.add(c);
                }
                if(c.getColor().equals(UnoPlayer.Color.BLUE)){
                    blueCards.add(c);
                }
            }

            //Generate a score for each color via the lists
            redScore = generateScore(redCards);
            yellowScore = generateScore(yellowCards);
            greenScore = generateScore(greenCards);
            blueScore = generateScore(blueCards);

            //Discount the score based on the color of upCard
            if(!potentialColors.contains(UnoPlayer.Color.RED))redScore=-1;
            if(!potentialColors.contains(UnoPlayer.Color.YELLOW))yellowScore=-1;
            if(!potentialColors.contains(UnoPlayer.Color.GREEN))greenScore=-1;
            if(!potentialColors.contains(UnoPlayer.Color.BLUE))blueScore=-1;

            //Find the maximum of these four scores
            sScore = Math.max(Math.max(redScore,yellowScore),Math.max(greenScore,blueScore));

            //Determine the color of this score; Even if two colors' scores are equal to the max score, switching to either one would be tactically the same based on our own hand
            if(sScore==redScore){
                sColor = UnoPlayer.Color.RED;
            } else if(sScore==yellowScore){
                sColor = UnoPlayer.Color.YELLOW;
            } else if(sScore==greenScore){
                sColor = UnoPlayer.Color.GREEN;
            } else if(sScore==blueScore){
                sColor = UnoPlayer.Color.BLUE;
            }
            
        //Return the index of the card in hand of the switch color, and upCard's rank and number, via the getCard method
        return getCard(hand, sColor ,upCard.getRank(),upCard.getNumber());
    }

    //Returns the index within hand of the first card in the hand matching color, rank and number parameters
    private int getCard(List<Card> hand, Color color, Rank rank, int number) {
        for(int i = 0; i<hand.size(); i++){
            Card c = hand.get(i);
            if(c.getColor().equals(color)&&c.getRank().equals(rank)&&c.getNumber()==number) return i; //if the card at position i matches color rank and number parameters, return i, 
        }
        return -1;//If no card found, return -1 (draw)
    }
    
    //Returns the index within hand of the first card in the hand to match the parameter
    private int getCard(List<Card> hand, Card c) {
        for(int i = 0; i<hand.size(); i++){
            if(c == hand.get(i))return i; //If the card matches, return the index
        }
        return -1;//If no card found, return -1 (draw)
    }
    
    //Returns the best card in the hand of the given color
    private int getBestCard(List<Card> hand, UnoPlayer.Color color) {
        Card card=null; //Initialize the card
        for(Card c : hand){ //For each card in the hand
            if(c.getColor().equals(color)){ //if the card matches the color
                if(card==null){
                    card=c; //if the card is not yet initialized to an actual object, set it equal to this card
                }else {
                    for(int i=0;i<hand.size();i++){//For each card in hand
                        if(hand.get(i).getColor().equals(color)){//If hand matches color
                            int num=c.getNumber();//Compare the value of the cards based on number
                            int onum=card.getNumber();//onum represents the number of the best card found so far, num represents the number of the current card
                            //Account for specials, placing draw-2s at the most value, then skips, and then reverses, with respective values of 12, then 11, then 10.
                            if(c.getRank().equals(Rank.REVERSE))num=10; 
                            if(c.getRank().equals(Rank.SKIP))num=11;
                            if(c.getRank().equals(Rank.DRAW_TWO))num=12;
                            if(card.getRank().equals(Rank.REVERSE))onum=10;
                            if(card.getRank().equals(Rank.SKIP))onum=11;
                            if(card.getRank().equals(Rank.DRAW_TWO))onum=12;
                            
                            if(num>onum){
                                card=c;//If the numerical value of the current card is greater than that of the supposedly best card so far, then set card equal to the current card, thus deeming it the best card in the hand so far.
                            }
                        }
                    }
                }
            }
        }
        if(card==null)return -1; //If no card is found of this color, return -1 (draw)
        return getCard(hand,card); //Return the card
    }

    
    //Returns the best wild (if possible, WILD_D4; otherwise, WILD)
    private int getBestWildCard(List<Card> hand) {
        
        Card card=hand.get(0); //Initialize a Card object as the first Card in hand
        for(Card c : hand){ //For each Card in hand...
            if(!card.getRank().equals(UnoPlayer.Rank.WILD_D4)
                    &&(c.getRank().equals(UnoPlayer.Rank.WILD)||c.getRank().equals(UnoPlayer.Rank.WILD_D4))) 
                card=c; //If the card object isn't already a WILD_D4 (best Wild card), and it is a WILD or a WILD_D4, set card equal to c
        }
        if (card.getRank().equals(UnoPlayer.Rank.WILD_D4)||card.getRank().equals(UnoPlayer.Rank.WILD)){ //If card is actually a WILD card...
            return getCard(hand,card); //Return the index of the card within hand via the getCard method
        }else{
            return -1; //Otherwise, return -1 (draw)
        }
    }
    
    //Returns whether or not you can play a special card
     public boolean canUseASpecial(List<Card> hand, Card upCard,Color calledColor){
        for(Card c : hand){//For each card in hand,
            if((c.canPlayOn(upCard, upCard.getColor())||c.getColor().equals(calledColor))&&(c.getRank().equals(Rank.WILD_D4)||c.getRank().equals(Rank.DRAW_TWO)||c.getRank().equals(Rank.SKIP)||c.getRank().equals(Rank.REVERSE))) return true; //If the card can play on the upCard or on calledcolor AND is a special card, return true
        }
        return false;//if no such card is found, return false
    }

    //Returns the index within hand of the best special card
    private int getBestSpecial(List<Card> hand, Color calledColor) {
       Card card=null; //Initialize the card
        for(Card c : hand){ //For each card in the hand
            if(c.getColor().equals(calledColor)){ //if the card matches the color
                if(card==null){
                    card=c; //if the card is not yet initialized to an actual object, set it equal to this card
                }else {
                    for(int i=0;i<hand.size();i++){ //For each card in hand
                        if(hand.get(i).getColor().equals(calledColor)){
                            if(!c.getRank().equals(Rank.NUMBER)){ //As long as card is not numerical
                                int num=c.getNumber(); //Create numbers representing value of both the originally chosen card and the current card
                                int onum=card.getNumber();
                                //Account for special cards, creating values for reverses (10), skips (11), and draw-2s (12).
                                if(c.getRank().equals(Rank.REVERSE))num=10;
                                if(c.getRank().equals(Rank.SKIP))num=11;
                                if(c.getRank().equals(Rank.DRAW_TWO))num=12;
                                if(card.getRank().equals(Rank.REVERSE))onum=10;
                                if(card.getRank().equals(Rank.SKIP))onum=11;
                                if(card.getRank().equals(Rank.DRAW_TWO))onum=12;
                                
                                if(num>onum){
                                    card=c;//If the current card is more valuable than the originally chosen card, make this the best card
                                }
                            }
                        }
                    }
                }
            }
        }
        
        int i=getBestWildCard(hand); 
        if(i!=-1&&hand.get(i).getRank().equals(Rank.WILD_D4))card = hand.get(i); //If there is a draw 4, choose the draw 4
        
        if(card==null)return -1; //If card has not yet been initialized, return -1 (draw)
        
        return getCard(hand,card); //Return the chosen card
    }
    
}
