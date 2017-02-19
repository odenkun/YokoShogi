package com.example.yokoshogi.event;

import com.example.yokoshogi.Position;

public class RemoteMoveEvent {
    private final Position befPos;
    private final Position afPos;
    private final boolean becomeFlg;

    public RemoteMoveEvent ( Position befPos, Position afPos, boolean becomeFlg ) {
        this.befPos = befPos;
        this.afPos = afPos;
        this.becomeFlg = becomeFlg;
    }

    public Position getBefPos () {
        return befPos;
    }

    public Position getAfPos () {
        return afPos;
    }

    public boolean isBecomeFlg () {
        return becomeFlg;
    }
}
