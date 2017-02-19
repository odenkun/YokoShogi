package com.example.yokoshogi.event;

import com.example.yokoshogi.Koma;

public class KomadaiTapEvent {
    private final Koma.Types
            preType,
            nextType;
    private final boolean enemyFlg;

    public KomadaiTapEvent ( Koma.Types preType, Koma.Types nextType, boolean enemyFlg ) {
        this.preType = preType;
        this.nextType = nextType;
        this.enemyFlg = enemyFlg;
    }

    public boolean getEnemyFlg () {
        return enemyFlg;
    }

    public Koma.Types getPreType () {
        return preType;
    }

    public Koma.Types getNextType () {
        return nextType;
    }
}
