package org.uncommons.poker.game.cards;

import java.util.List;
import java.util.Arrays;

/**
 * @author Daniel Dyer
 */
public final class RankedHand implements Comparable<RankedHand>
{
    public static final int HAND_SIZE = 5;

    private final PlayingCard[] cards = new PlayingCard[HAND_SIZE];
    private final HandRanking ranking;

    /**
     * @param cards The cards that make up the hand.  Must be ordered in
     * descending order of significance.
     * @param ranking The value of the hand (e.g. TWO_PAIR or FULL_HOUSE).
     */
    public RankedHand(List<PlayingCard> cards, HandRanking ranking)
    {
        cards.toArray(this.cards);
        this.ranking = ranking;
    }


    public HandRanking getRanking()
    {
        return ranking;
    }


    public PlayingCard getCard(int index)
    {
        return cards[index];
    }


    public int compareTo(RankedHand otherHand)
    {
        int compare = this.ranking.ordinal() - otherHand.getRanking().ordinal();
        // If the hands have the same ranking, check the actual cards.  For example,
        // both may be ranked as PAIR, but one may be a pair of threes and the other
        // a pair of kings.
        if (compare == 0)
        {
            PlayingCard[] otherCards = otherHand.cards;
            for (int i = 0; i < cards.length; i++)
            {
                compare = cards[i].ordinal() - otherCards[i].ordinal();
                if (compare != 0)
                {
                    break;
                }
            }
        }
        return compare;
    }


    /**
     * Test whether this hand contains a given card.  Used mostly for validation by unit tests.
     */
    public boolean contains(PlayingCard card)
    {
        return Arrays.binarySearch(cards, card) >= 0;
    }
}
