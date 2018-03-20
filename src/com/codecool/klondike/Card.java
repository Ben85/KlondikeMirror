package com.codecool.klondike;

import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.util.*;

public class Card extends ImageView {

    private Suit suit;
    private Rank rank;
    private boolean faceDown;

    private Image backFace;
    private Image frontFace;
    private Pile containingPile;
    private DropShadow dropShadow;

    static Image cardBackImage;
    private static final Map<String, Image> cardFaceImages = new HashMap<>();
    public static final int WIDTH = 150;
    public static final int HEIGHT = 215;

    public Card(Suit suit, Rank rank, boolean faceDown) {
        this.suit = suit;
        this.rank = rank;
        this.faceDown = faceDown;
        this.dropShadow = new DropShadow(2, Color.gray(0, 0.75));
        backFace = cardBackImage;
        frontFace = cardFaceImages.get(getShortName());
        setImage(faceDown ? backFace : frontFace);
        setEffect(dropShadow);
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    public boolean isFaceDown() {
        return faceDown;
    }

    public String getShortName() {
        return suit.getName() + rank.getValue();
    }

    public DropShadow getDropShadow() {
        return dropShadow;
    }

    public Pile getContainingPile() {
        return containingPile;
    }

    public void setContainingPile(Pile containingPile) {
        this.containingPile = containingPile;
    }

    public void moveToPile(Pile destPile) {
        this.getContainingPile().getCards().remove(this);
        destPile.addCard(this);
    }

    public void flip() {
        faceDown = !faceDown;
        setImage(faceDown ? backFace : frontFace);
    }

    @Override
    public String toString() {
        return "The " + rank.getName() + " of " + suit.getName();
    }

    public static boolean isOppositeColor(Card card1, Card card2) {
        if (card1.getSuit() != card2.getSuit()) {
            if (card1.getSuit() + card2.getSuit() < 4 || card1.getSuit() + card2.getSuit() >= 7) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public static boolean isSameSuit(Card card1, Card card2) {
        return card1.getSuit() == card2.getSuit();
    }

    public static List<Card> createNewDeck() {
        List<Card> result = new ArrayList<>();
        for (Suit suit: Suit.values()) {
            for (Rank rank: Rank.values()) {
                result.add(new Card(suit, rank, true));
            }
        }
        Collections.shuffle(result);
        return result;
    }

    public static void loadCardImages() {
        cardBackImage = new Image("card_images/card_back.png");
        String suitName = "";
        for (Suit suit: Suit.values()) {
            suitName = suit.getName();

            for (Rank rank: Rank.values()) {
                int rankValue = rank.getValue();
                String cardName = suitName + rankValue;
                String imageFileName = "card_images/" + cardName + ".png";
                cardFaceImages.put(cardName, new Image(imageFileName));
            }
        }
    }

    public enum Suit {
        HEARTS ("hearts", "red"),
        DIAMONDS ("diamonds", "red"),
        SPADES ("spades", "black"),
        CLUBS ("clubs", "black");

        private final String name;
        private final String color;

        Suit(String name, String color) {
            this.name = name;
            this.color = color;
        }

        public String getName() {
            return name;
        }

        public String getColor() {
            return color;
        }
    }

    public enum Rank {
        ACE (1, "Ace"),
        TWO (2, "two"),
        THREE (3, "three"),
        FOUR (4, "four"),
        FIVE (5, "five"),
        SIX (6, "six"),
        SEVEN (7, "seven"),
        EIGHT (8, "eight"),
        NINE (9, "nine"),
        TEN (10, "ten"),
        JACK (11, "Jack"),
        QUEEN (12, "Queen"),
        KING (13, "King");

        private final int value;
        private final String name;

        Rank (int value, String name){
            this.value = value;
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public String getName() {
            return name;
        }
    }

}
