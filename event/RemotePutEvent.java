package com.example.yokoshogi.event;

import com.example.yokoshogi.Koma;
import com.example.yokoshogi.Position;

public class RemotePutEvent {
    private final Koma.Types komaType;
    private final Koma.directions direction;
    private final Position position;

    public RemotePutEvent ( Koma.Types komaType, Koma.directions direction, Position position ) {
        this.komaType = komaType;
        this.direction = direction;
        this.position = position;
    }

    public Koma.Types getKomaType () {
        return komaType;
    }

    public Koma.directions getDirection () {
        return direction;
    }

    public Position getPosition () {
        return position;
    }
}
