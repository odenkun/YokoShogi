package com.example.yokoshogi.event;

import com.example.yokoshogi.logger.Sashite;

public class TurnChangeEvent {
    private final boolean beforeTurn;
    private final Sashite sashite;

    public TurnChangeEvent ( boolean beforeTurn, Sashite sashite ) {
        this.beforeTurn = beforeTurn;
        this.sashite = sashite;
    }

    public Sashite getSashite () {
        return sashite;
    }

    public boolean getBeforeTurn () {
        return beforeTurn;
    }
}
