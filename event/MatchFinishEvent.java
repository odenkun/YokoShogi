package com.example.yokoshogi.event;


public class MatchFinishEvent {
    final private boolean
            succeeded;
    private final String
            earlier,
            later;

    public MatchFinishEvent ( boolean succeeded, String earlier, String later ) {
        this.succeeded = succeeded;
        this.earlier = earlier;
        this.later = later;
    }

    public boolean isSucceeded () {
        return succeeded;
    }

}
