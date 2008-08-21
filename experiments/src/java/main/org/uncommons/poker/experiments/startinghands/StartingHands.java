package org.uncommons.poker.experiments.startinghands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.poker.game.cards.Deck;
import org.uncommons.poker.game.cards.PlayingCard;
import org.uncommons.poker.game.cards.RankedHand;
import org.uncommons.poker.game.rules.PokerRules;
import org.uncommons.poker.game.rules.TexasHoldem;

/**
 * Statistical anaylsis of different starting hands.
 * @author Daniel Dyer
 */
public class StartingHands
{
    public static void main(String[] args)
    {
        int iterations = Integer.parseInt(args[0]);
        
        long start = System.currentTimeMillis();
        StartingHands startingHands = new StartingHands();
        Map<String, StartingHandInfo> info = startingHands.simulate(2,
                                                                    iterations,
                                                                    new TexasHoldem(),
                                                                    new MersenneTwisterRNG());
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("Completed in " + elapsed/1000 + " seconds.");
        tabulate(info);
    }


    public Map<String, StartingHandInfo> simulate(final int seats,
                                                  final int iterations,
                                                  final PokerRules rules,
                                                  final Random rng)
    {
        final Map<String, StartingHandInfo> startingHands = new ConcurrentHashMap<String, StartingHandInfo>();

        // Divide the work across available processors.
        int threadCount = Runtime.getRuntime().availableProcessors();
        final int iterationsPerThread = iterations / threadCount;
        final CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++)
        {
            new Thread(new Runnable()
            {
                public void run()
                {
                    for (int i = 0; i < iterationsPerThread; i++)
                    {
                        playHand(seats, rules, rng, startingHands);
                    }
                    latch.countDown();
                }
            }).start();

        }
        try
        {
            latch.await();
        }
        catch (InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
        return startingHands;
    }


    public static void tabulate(Map<String, StartingHandInfo> startingHands)
    {
        List<StartingHandInfo> info = new ArrayList<StartingHandInfo>(startingHands.values());
        Collections.sort(info, new Comparator<StartingHandInfo>()
        {
            public int compare(StartingHandInfo info1,
                               StartingHandInfo info2)
            {
                return Double.compare(info2.getWinRate(), info1.getWinRate());
            }
        });

        for (StartingHandInfo hand : info)
        {
            System.out.println(hand.getId() + "\t" + hand.getWinRate());
        }
    }


    private void playHand(int seats,
                          PokerRules rules,
                          Random rng,
                          Map<String, StartingHandInfo> startingHands)
    {
        Deck deck = Deck.createFullDeck(rng);

        // Deal the community cards (for the purposes of the experiment, it doesn't
        // matter that we do this before the hole cards).
        List<PlayingCard> communityCards = new ArrayList<PlayingCard>(5);
        for (int i = 0; i < 5; i++)
        {
            communityCards.add(deck.dealCard());
        }


        RankedHand bestHand = null;
        // Maybe more than one winning hand (split pots).
        List<StartingHandInfo> winningStartingHands = new ArrayList<StartingHandInfo>(seats);

        // Deal hole cards and determine the winning hand(s).
        for (int i = 0; i < seats; i++)
        {
            List<PlayingCard> holeCards = new ArrayList<PlayingCard>(2);
            holeCards.add(deck.dealCard());
            holeCards.add(deck.dealCard());

            String startingHand = getStartingHandClassification(holeCards);
            StartingHandInfo info = getStartingHandInfo(startingHand, startingHands);
            info.incrementDealt();

            RankedHand hand = rules.rankHand(holeCards, communityCards);
            if (bestHand == null || hand.compareTo(bestHand) > 0)
            {
                bestHand = hand;
                winningStartingHands.clear();
                winningStartingHands.add(info);
            }
            else if (hand.compareTo(bestHand) == 0)
            {
                // Potential split pot.
                winningStartingHands.add(info);
            }
        }

        for (StartingHandInfo info : winningStartingHands)
        {
            info.incrementWon();
        }
    }

    
    private StartingHandInfo getStartingHandInfo(String startingHand,
                                                 Map<String, StartingHandInfo> startingHands)
    {
        StartingHandInfo info = startingHands.get(startingHand);
        if (info == null)
        {
            info = new StartingHandInfo(startingHand);
            startingHands.put(startingHand, info);
        }
        return info;
    }


    private String getStartingHandClassification(List<PlayingCard> cards)
    {
        // Sort to ensure that card1 is the highest ranked.
        if (cards.get(0).compareTo(cards.get(1)) < 0)
        {
            Collections.swap(cards, 0, 1);
        }        

        StringBuilder buffer = new StringBuilder(3);
        for (PlayingCard card : cards)
        {
            buffer.append(card.getValue().getSymbol());
        }

        // If not a pair, is it suited or off-suit?
        if (!cards.get(0).getValue().equals(cards.get(1).getValue()))
        {
            buffer.append(cards.get(0).getSuit().equals(cards.get(1).getSuit()) ? 's' : 'o');
        }
        
        return buffer.toString();
    }


    private static class StartingHandInfo
    {
        private final String id;
        private int dealtCount = 0;
        private int wonCount = 0;

        public StartingHandInfo(String id)
        {
            this.id = id;
        }

        public synchronized void incrementDealt()
        {
            ++dealtCount;
        }

        public synchronized void incrementWon()
        {
            ++wonCount;
        }

        public String getId()
        {
            return id;
        }

        public synchronized int getDealtCount()
        {
            return dealtCount;
        }

        public synchronized int getWonCount()
        {
            return wonCount;
        }

        public synchronized double getWinRate()
        {
            return ((double) wonCount) / dealtCount;
        }
    }
}
