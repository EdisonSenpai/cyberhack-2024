package com.example.mywalletproject;

public class StoreCard {
    private String name;
    private String cardId;

    public StoreCard(String name, String cardId) {
        this.name = name;
        this.cardId = cardId;
    }

    // Getters și setters (opțional)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }
}
