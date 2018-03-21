package com.codecool.klondike;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;

import java.util.*;

public class Game extends Pane {

    private List<Card> deck = new ArrayList<>();

    private Pile stockPile;
    private Pile discardPile;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();

    private double dragStartX, dragStartY;
    private List<Card> draggedCards = FXCollections.observableArrayList();

    private static double STOCK_GAP = 1;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;


    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card card = (Card) e.getSource();
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK) {
            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
            System.out.println("Placed " + card + " to the waste.");
        }
    };

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        refillStockFromDiscard();
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
        dragStartX = e.getSceneX();
        dragStartY = e.getSceneY();
    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
        Card card = (Card) e.getSource();
        Pile activePile = card.getContainingPile();
        if (activePile.getPileType() == Pile.PileType.STOCK)
            return;
        double offsetX = e.getSceneX() - dragStartX;
        double offsetY = e.getSceneY() - dragStartY;

        draggedCards.clear();
        draggedCards.add(card);
        card.toFront();
        int counter = 1;
        for (int i = card.getContainingPile().getCards().indexOf(card); i < card.getContainingPile().getCards().size(); i++){
            Card cards = card.getContainingPile().getCards().get(i);
            if (card.getContainingPile().getPileType() == Pile.PileType.TABLEAU && card != cards ) {
                draggedCards.add(cards);
                cards.getDropShadow().setRadius(20);
                cards.getDropShadow().setOffsetX(10);
                cards.getDropShadow().setOffsetY(10);
                cards.toFront();
                cards.setTranslateX(offsetX);
                cards.setTranslateY(offsetY);
                counter++;
            }
        }
        card.getDropShadow().setRadius(20);
        card.getDropShadow().setOffsetX(10);
        card.getDropShadow().setOffsetY(10);
        if (draggedCards.size() == 1) {
            card.toFront();
        }
        card.setTranslateX(offsetX);
        card.setTranslateY(offsetY);
    };

    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        if (draggedCards.isEmpty())
            return;
        Card card = (Card) e.getSource();
        Pile pile = getValidIntersectingPile(card, tableauPiles);
        Pile foundationPile = getValidIntersectingPile(card, foundationPiles);
        if (pile != null) {
            handleValidMove(card, pile);
            Card card2 = card.getContainingPile().getCardUnderTopCard(card);
            if(card2.isFaceDown()){
                card2.flip();
            }

        } else if (foundationPile != null) {
            handleValidMove(card, foundationPile);
            if(card.getContainingPile().getCardUnderTopCard(card).isFaceDown()){
                card.getContainingPile().getCardUnderTopCard(card).flip();
                System.out.println("Wurking");
            }

        } else {
            System.out.println("Invalid Move!");
            for (Card cards : draggedCards){
                MouseUtil.slideBack(cards);
            }
            draggedCards.clear();
        }
    };

    public boolean isGameWon() {
        //TODO
        return false;
    }

    public Game() {
        deck = Card.createNewDeck();
        initPiles();
        dealCards();
    }

    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);
    }

    public void refillStockFromDiscard() {
        Collections.reverse(discardPile.getCards());
        for (Card currentCard : discardPile.getCards()){
            stockPile.addCard(currentCard);
            currentCard.flip();
        }
        discardPile.clear();
        System.out.println("Stock refilled from discard pile.");
    }

    private boolean isMoveValid(Card card, Pile destPile) {
        if (destPile.getPileType() == Pile.PileType.TABLEAU) {
            if (isRightRank(card, destPile)) {
                if (destPile.isEmpty() && card.getRank().getName().equals("King")) {
                    return true;
                } else if (!destPile.isEmpty() && Card.isOppositeColor(card, destPile.getTopCard())) {
                    return true;
                }
                return false;
            }
        }
        if (destPile.getPileType() == Pile.PileType.FOUNDATION){
            if (isRightRank(card, destPile)) {
                if (destPile.isEmpty() && card.getRank().getName().equals("Ace")) {
                    return true;
                } else if (!destPile.isEmpty() && (card.getSuit() == destPile.getTopCard().getSuit())) {
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    private Pile getValidIntersectingPile(Card card, List<Pile> piles) {
        Pile result = null;
        for (Pile pile : piles) {
            if (!pile.equals(card.getContainingPile()) &&
                    isOverPile(card, pile) &&
                    isMoveValid(card, pile))
                result = pile;
        }
        return result;
    }

    private boolean isOverPile(Card card, Pile pile) {
        if (pile.isEmpty())
            return card.getBoundsInParent().intersects(pile.getBoundsInParent());
        else
            return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());
    }

    private void handleValidMove(Card card, Pile destPile) {
        String msg = null;
        if (destPile.getPileType() == Pile.PileType.TABLEAU) {
            if (destPile.isEmpty()) {
                    msg = String.format("Placed %s to a new pile.", card);
            } else {
                msg = String.format("Placed %s to %s.", card, destPile.getTopCard());
            }
        } else if (destPile.getPileType() == Pile.PileType.FOUNDATION){
            if (destPile.isEmpty()) {
                msg = String.format("Placed %s to the foundation.", card);
            } else {
                msg = String.format("Placed %s to %s.", card, destPile.getTopCard());
            }
        }

        System.out.println(msg);
        MouseUtil.slideToDest(draggedCards, destPile);
        draggedCards.clear();

    }


    private boolean isRightRank(Card card, Pile destpile) {
        if (destpile.getPileType() == Pile.PileType.TABLEAU) {

            Card card2 = destpile.getTopCard();
            if (destpile.isEmpty()) {

                return true;
            }
            return ((card2.getRank().getValue() - card.getRank().getValue()) == 1);

        } else if (destpile.getPileType() == Pile.PileType.FOUNDATION){

            Card card2 = destpile.getTopCard();
            if (destpile.isEmpty()){
                return true;
            }
            return ((card.getRank().getValue() - card2.getRank().getValue() == 1));
        }
        return false;
    }

    private void initPiles() {
        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
        stockPile.setBlurredBackground();
        stockPile.setLayoutX(95);
        stockPile.setLayoutY(20);
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);

        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", STOCK_GAP);
        discardPile.setBlurredBackground();
        discardPile.setLayoutX(285);
        discardPile.setLayoutY(20);
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            foundationPile.setBlurredBackground();
            foundationPile.setLayoutX(610 + i * 180);
            foundationPile.setLayoutY(20);
            foundationPiles.add(foundationPile);
            getChildren().add(foundationPile);
        }
        for (int i = 0; i < 7; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            tableauPile.setBlurredBackground();
            tableauPile.setLayoutX(95 + i * 180);
            tableauPile.setLayoutY(275);
            tableauPiles.add(tableauPile);
            getChildren().add(tableauPile);
        }
    }

    public void dealCards() {
        Iterator<Card> deckIterator = deck.iterator();
        //TODO
        /** Dealing facedown cards to each tableau */
        for (int i = 0; i < tableauPiles.size(); i++) {
            for (int j = 0; j <= i; j++) {
                Card card = deckIterator.next();
                tableauPiles.get(i).addCard(card);
                addMouseEventHandlers(card);
                getChildren().add(card);
            }
        }
        /** Dealing faceup cards */
        for (int i = 0; i < tableauPiles.size(); i++) {
            Card topCard = tableauPiles.get(i).getTopCard();
            topCard.flip();
        }

        deckIterator.forEachRemaining(card -> {
            stockPile.addCard(card);
            addMouseEventHandlers(card);
            getChildren().add(card);
        });

    }

    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

}
