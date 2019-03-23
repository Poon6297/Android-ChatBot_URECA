package com.example.poon6.ureca_shared_v1.Model;

import java.util.List;

public class QuickReply {
    private String button1;
    private String button2;
    private String button3;

    public QuickReply(List<String> quickReply) {
        this.button1 = quickReply.get(0);
        this.button2 = quickReply.get(1);
        this.button3 = quickReply.get(2);
    }

    public String getButton1() {
        return this.button1;
    }

    public String getButton2() {
        return this.button2;
    }

    public String getButton3() {
        return this.button3;
    }
}
