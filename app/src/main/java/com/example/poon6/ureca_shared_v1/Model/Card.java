package com.example.poon6.ureca_shared_v1.Model;

public class Card {

    private String uri;
    private String title;

    public Card(String uri, String title) {
        this.uri = uri;
        this.title = title;
    }

    public String getUri() {
        return uri;
    }

    public String getTitle() {
        return title;
    }
}
