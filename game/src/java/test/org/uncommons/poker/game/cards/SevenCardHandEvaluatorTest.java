package org.uncommons.poker.game.cards;

import org.testng.annotations.Test;
import java.util.List;
import java.util.ArrayList;

/**
 * Unit test for {@link FiveCardHandEvaluator}.
 * @author Daniel Dyer
 */
public class SevenCardHandEvaluatorTest
{
    private final HandEvaluator handEvaluator = new SevenCardHandEvaluator();

    /**
     * Make sure that an ace-low straight flush is correctly recognised.
     */
    @Test
    public void testLowStraightFlush()
    {
        // Cards must be sorted by rank to start with.
        List<PlayingCard> cards = asList(PlayingCard.ACE_OF_HEARTS,
                                         PlayingCard.KING_OF_DIAMONDS,
                                         PlayingCard.NINE_OF_CLUBS,
                                         PlayingCard.FIVE_OF_HEARTS,
                                         PlayingCard.FOUR_OF_HEARTS,
                                         PlayingCard.THREE_OF_HEARTS,
                                         PlayingCard.TWO_OF_HEARTS);

        RankedHand hand = handEvaluator.evaluate(cards);
        assert hand.getRanking() == HandRanking.STRAIGHT_FLUSH : "Wrong hand ranking: " + hand.getRanking();
        // The five is the most significant card, the ace the least.
        assert hand.getCard(0) == PlayingCard.FIVE_OF_HEARTS : "Wrong order.";
        assert hand.getCard(1) == PlayingCard.FOUR_OF_HEARTS : "Wrong order.";
        assert hand.getCard(2) == PlayingCard.THREE_OF_HEARTS : "Wrong order.";
        assert hand.getCard(3) == PlayingCard.TWO_OF_HEARTS : "Wrong order.";
        assert hand.getCard(4) == PlayingCard.ACE_OF_HEARTS : "Wrong order.";
    }


    /**
     * The straight that makes a straight flush may be a lower straight than is possible without
     * also making a flush.  The evaluator should correctly find the lower straight because it is
     * a higher ranked hand.
     */
    @Test
    public void testStraightFlushWithHigherStraight()
    {
        List<PlayingCard> cards = asList(PlayingCard.JACK_OF_DIAMONDS,
                                         PlayingCard.TEN_OF_HEARTS,
                                         PlayingCard.NINE_OF_CLUBS,
                                         PlayingCard.EIGHT_OF_CLUBS,
                                         PlayingCard.SEVEN_OF_CLUBS,
                                         PlayingCard.SIX_OF_CLUBS,
                                         PlayingCard.FIVE_OF_CLUBS);

        RankedHand hand = handEvaluator.evaluate(cards);

        assert hand.getRanking() == HandRanking.STRAIGHT_FLUSH : "Wrong hand ranking: " + hand.getRanking();

        assert hand.getCard(0) == PlayingCard.NINE_OF_CLUBS : "Wrong order.";
        assert hand.getCard(1) == PlayingCard.EIGHT_OF_CLUBS : "Wrong order.";
        assert hand.getCard(2) == PlayingCard.SEVEN_OF_CLUBS : "Wrong order.";
        assert hand.getCard(3) == PlayingCard.SIX_OF_CLUBS : "Wrong order.";
        assert hand.getCard(4) == PlayingCard.FIVE_OF_CLUBS : "Wrong order.";
    }


    /**
     * Test for the scenario where the seven cards can make both a flush and a straight but not
     * a straight flush.  The highest ranked hand should therefore be a flush.
     */
    @Test
    public void testStraightAndFlushButNotStraightFlush()
    {
        List<PlayingCard> cards = asList(PlayingCard.KING_OF_DIAMONDS,
                                         PlayingCard.QUEEN_OF_SPADES,
                                         PlayingCard.JACK_OF_DIAMONDS,
                                         PlayingCard.TEN_OF_DIAMONDS,
                                         PlayingCard.NINE_OF_HEARTS,
                                         PlayingCard.SIX_OF_DIAMONDS,
                                         PlayingCard.TWO_OF_DIAMONDS);

        RankedHand hand = handEvaluator.evaluate(cards);
        assert hand.getRanking() == HandRanking.FLUSH : "Wrong hand ranking: " + hand.getRanking();

        assert hand.getCard(0) == PlayingCard.KING_OF_DIAMONDS : "Wrong order.";
        assert hand.getCard(1) == PlayingCard.JACK_OF_DIAMONDS : "Wrong order.";
        assert hand.getCard(2) == PlayingCard.TEN_OF_DIAMONDS : "Wrong order.";
        assert hand.getCard(3) == PlayingCard.SIX_OF_DIAMONDS : "Wrong order.";
        assert hand.getCard(4) == PlayingCard.TWO_OF_DIAMONDS : "Wrong order.";
    }


    /**
     * Make sure that a hand with more than 5 cards of the same suit is still considered a flush.
     * This is a regression test since the original implementation was checking for exactly 5 of
     * the seven cards being of the same suit.  It should have been checking for 5 or more.
     */
    @Test
    public void testSixCardFlush()
    {
        List<PlayingCard> cards = asList(PlayingCard.QUEEN_OF_CLUBS,
                                         PlayingCard.JACK_OF_SPADES,
                                         PlayingCard.NINE_OF_CLUBS,
                                         PlayingCard.EIGHT_OF_CLUBS,
                                         PlayingCard.SIX_OF_CLUBS,
                                         PlayingCard.FOUR_OF_CLUBS,
                                         PlayingCard.TWO_OF_CLUBS);

        RankedHand hand = handEvaluator.evaluate(cards);
        assert hand.getRanking() == HandRanking.FLUSH : "Wrong hand ranking: " + hand.getRanking();

        assert hand.getCard(0) == PlayingCard.QUEEN_OF_CLUBS : "Wrong order.";
        assert hand.getCard(1) == PlayingCard.NINE_OF_CLUBS : "Wrong order.";
        assert hand.getCard(2) == PlayingCard.EIGHT_OF_CLUBS : "Wrong order.";
        assert hand.getCard(3) == PlayingCard.SIX_OF_CLUBS : "Wrong order.";
        assert hand.getCard(4) == PlayingCard.FOUR_OF_CLUBS : "Wrong order.";
    }


    /**
     * With seven cards sorted in order of rank, if we have a 5-card straight and one of
     * the middle cards is also one of a pair, then the 5 cards that make the straight will
     * not be in sequence.  E.g. 9, 8, 7, 7, 6, 5.
     */
    @Test
    public void testNonContiguousStraight()
    {
        List<PlayingCard> cards = asList(PlayingCard.KING_OF_HEARTS,
                                         PlayingCard.NINE_OF_DIAMONDS,
                                         PlayingCard.EIGHT_OF_CLUBS,
                                         PlayingCard.SEVEN_OF_SPADES,
                                         PlayingCard.SEVEN_OF_DIAMONDS,
                                         PlayingCard.SIX_OF_CLUBS,
                                         PlayingCard.FIVE_OF_HEARTS);

        RankedHand hand = handEvaluator.evaluate(cards);
        assert hand.getRanking() == HandRanking.STRAIGHT : "Wrong hand ranking: " + hand.getRanking();

        assert hand.getCard(0) == PlayingCard.NINE_OF_DIAMONDS : "Wrong order.";
        assert hand.getCard(1) == PlayingCard.EIGHT_OF_CLUBS : "Wrong order.";
        assert hand.getCard(2) == PlayingCard.SEVEN_OF_SPADES : "Wrong order.";
        assert hand.getCard(3) == PlayingCard.SIX_OF_CLUBS : "Wrong order.";
        assert hand.getCard(4) == PlayingCard.FIVE_OF_HEARTS : "Wrong order.";        
    }


    /**
     * Like {@link java.util.Arrays#asList} except that the returned list is not fixed-size.
     */
    private static <T> List<T> asList(T... elements)
    {
        List<T> list = new ArrayList<T>(elements.length);
        for (T element : elements)
        {
            list.add(element);
        }
        return list;
    }

}
