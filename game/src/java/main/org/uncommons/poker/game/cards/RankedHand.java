package org.uncommons.poker.game.cards;

import java.util.List;
import java.util.Collections;
import java.util.RandomAccess;

/**
 * @author Daniel Dyer
 */
public final class RankedHand implements Comparable<RankedHand>
{
    public static final int HAND_SIZE = 5;

    private final List<PlayingCard> cards;
    private final HandRanking ranking;

    /**
     * @param cards The cards that make up the hand.  Must be ordered in
     * descending order of significance.
     * @param ranking The value of the hand (e.g. TWO_PAIR or FULL_HOUSE).
     */
    public RankedHand(List<PlayingCard> cards, HandRanking ranking)
    {
        this.cards = Collections.unmodifiableList(cards);
        this.ranking = ranking;
    }


    public HandRanking getRanking()
    {
        return ranking;
    }


    public List<PlayingCard> getCards()
    {
        return cards;
    }


    public int compareTo(RankedHand otherHand)
    {
        int compare = this.ranking.compareTo(otherHand.getRanking());
        // If the hands have the same ranking, check the actual cards.  For example,
        // both may be ranked as PAIR, but one may be a pair of threes and the other
        // a pair of kings.
        if (compare == 0)
        {
            List<PlayingCard> otherCards = otherHand.getCards();
            assert otherCards instanceof RandomAccess : "Performance problem.";
            for (int i = 0; i < cards.size(); i++)
            {
                compare = cards.get(i).compareTo(otherCards.get(i));
                if (compare != 0)
                {
                    break;
                }
            }
        }
        return compare;
    }
}
