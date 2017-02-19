package com.example.yokoshogi.event;

import com.example.yokoshogi.Koma;

public class DirectionChangeEvent {
    private final boolean enemyFlg;
    private final Koma.directions nextDirection;

    public boolean isEnemyFlg () {
        return enemyFlg;
    }

    public Koma.directions getNextDirection () {
        return nextDirection;
    }

    public DirectionChangeEvent ( boolean enemyFlg, Koma.directions nextDirection ) {

        this.enemyFlg = enemyFlg;
        this.nextDirection = nextDirection;
    }
}
