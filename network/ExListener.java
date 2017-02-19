package com.example.yokoshogi.network;

import android.util.Log;
import com.example.yokoshogi.logger.Sashite;

import org.json.JSONException;
import org.json.JSONObject;
import io.socket.emitter.Emitter;

import static com.example.yokoshogi.network.SocketClient.STR_PLAYER;
import static com.example.yokoshogi.network.SocketClient.STR_TIME;


public abstract class ExListener implements Emitter.Listener {

    public static final String
            STR_BEF_LOC = "preLocate",
            STR_AF_LOC = "afLocate",
            STR_COL = "column",
            STR_ROW = "row";
    private String
            playerName,
            eventName;

    ExListener (  String playerName ,String eventName) {
        this.playerName = playerName;
        this.eventName = eventName;
    }

    @Override
    public void call ( Object... args ) {
        JSONObject jsonObject = (JSONObject)args[0];
        Log.d("client","received " + eventName + " :" + jsonObject.toString ());

        try {
            //もし自分が送ったものだったら何もしない
            if (jsonObject.getString (STR_PLAYER ).equals ( playerName )) {
                return;
            }

            Sashite sashite = new Sashite ( jsonObject );
            onReceive (sashite, jsonObject.getInt ( STR_TIME ));
        }catch ( JSONException e) {
            Log.d(eventName,e.getMessage ());
        }
    }
    abstract void onReceive (Sashite sashite,int time);
}
