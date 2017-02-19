package com.example.yokoshogi.event;

import com.example.yokoshogi.Koma;
import com.example.yokoshogi.Position;

/**
 * Created by kouj1en on 2017/02/06.
 */

public class RemoteBendEvent {
    private final Koma.directions direction;
    private final Position position;

    public RemoteBendEvent ( Koma.directions direction, Position position ) {
        this.direction = direction;
        this.position = position;
    }

    public Koma.directions getDirection () {
        return direction;
    }

    public Position getPosition () {
        return position;
    }
}
