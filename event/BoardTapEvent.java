package com.example.yokoshogi.event;

import com.example.yokoshogi.Koma;
import com.example.yokoshogi.Position;


public class BoardTapEvent {
    private final boolean turn;
    private final Koma tappedKoma;
    private final Position position;
    private final boolean equalToPreFlg;

    public BoardTapEvent ( boolean turn, Koma tappedKoma, Position position, boolean equalToPreFlg ) {
        this.turn = turn;
        this.tappedKoma = tappedKoma;
        this.position = position;
        this.equalToPreFlg = equalToPreFlg;
    }

    public boolean getTurn () {
        return turn;
    }

    public Koma getKoma () {
        return tappedKoma;
    }

    public Position getPosition () {
        return position;
    }

    public boolean isEqualToPre () {
        return equalToPreFlg;
    }
}
