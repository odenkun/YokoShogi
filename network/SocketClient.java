package com.example.yokoshogi.network;

import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.example.yokoshogi.Position;
import com.example.yokoshogi.dialog.SuperDialog;
import com.example.yokoshogi.event.MatchFinishEvent;
import com.example.yokoshogi.event.RemoteBendEvent;
import com.example.yokoshogi.event.RemoteMoveEvent;
import com.example.yokoshogi.event.RemotePutEvent;
import com.example.yokoshogi.event.TurnChangeEvent;
import com.example.yokoshogi.logger.Sashite;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketClient {
    private Socket mSocket;
    private final String
            IP_ADDRESS = "150.95.137.17",
            PORT_NUM = "80",
            ROOM_NAME ="theRoom";
    private String
            playerName,
            remoteName;
    private Boolean earlierFlg = null;
    public static final String
            STR_PLAYER = "playerName",
            STR_ROOM = "roomName",
            STR_SENTE = "earlier",
            STR_GOTE = "after",
            STR_MOVE = "move_piece",
            STR_PUT = "put_piece",
            STR_BEND = "bend_piece",
            STR_AF_DIR = "afDirection",
            STR_KOMA_TYPE = "komaType",
            STR_BEC_FLG = "becomeFlg",
            STR_TIME = "time",
    STR_ENE_DISCON = "enemy_discon";

    public SocketClient () {
        EventBus.getDefault ().register ( this );
        playerName = Build.MODEL;
        Log.d("local","playerName is " + playerName + " roomName is " + ROOM_NAME);
        connectToServer ();
    }

    public boolean isConnected () {
        return this.mSocket.connected ();
    }

    @Subscribe
    public void onTurnChangeEvent ( TurnChangeEvent event) {
        //falseのときのみこちらのターンであるため
        if ( ! (event.getBeforeTurn ()) ) {
            JSONObject obj = event.getSashite ().getJSON ();
            try {
                obj.put ( STR_PLAYER, playerName );
                obj.put ( STR_ROOM, ROOM_NAME );
            } catch ( JSONException e ) {

            }
            attemptSend ( "move_piece", obj );
        }
    }
    private void attemptSend(String eventName,JSONObject obj) {
        //ソケットから送信
        Log.d("attemptSend" + eventName, obj.toString ());
        mSocket.emit(eventName, obj);
    }
    public void disconnect () {
        mSocket.disconnect ();
    }
    private void connectToServer () {
        //ソケットを定義
        try {
            IO.Options opts = new IO.Options();
            opts.forceNew = true;
            opts.reconnection = false;
            mSocket = IO.socket("http://" + IP_ADDRESS + ":" + PORT_NUM,opts);
        } catch (URISyntaxException e) {
            Log.d("client","URISyntaxException occurred.");
        }
        //各イベントの設定
        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {//接続開始時に呼ばれるイベント

                //接続が成功したら、部屋名とプレイヤー名を送る
                JSONObject object = new JSONObject ();
                try {
                    object.put ( STR_ROOM, ROOM_NAME );
                    object.put ( STR_PLAYER, playerName );
                } catch ( JSONException e ) {
                    Log.d("firstJSON",e.toString ());
                }
                attemptSend ( "enter_room", object );

            }
        }).on("s2c", new Emitter.Listener() { //サーバからのメッセージを受信した時
            @Override
            public void call(Object... args) { //接続受理時とマッチング成功時に実行される

                JSONObject jsonObject = (JSONObject)args[0];
                Log.d("client","received s2c :" + jsonObject.toString ());
                try {
                    //メッセージは「value」をキーとしているため
                    String roomState = jsonObject.getString("state");

                    if( roomState.equals ( "matched" )) {
                        String earlier =jsonObject.getString(STR_SENTE);
                        String after = jsonObject.getString(STR_GOTE);
                        //
                        if (playerName.equals ( earlier )) {
                            earlierFlg = true;
                        }else if (playerName.equals ( after )){
                            earlierFlg = false;
                        }
                        Log.d(STR_SENTE, earlier);
                        Log.d(STR_GOTE, after);

                        if (earlierFlg!= null) {
                            //trueは成功のtrue
                            EventBus.getDefault ().post ( new MatchFinishEvent (true, earlier, after ) );
                        }
                    }else if (roomState.equals ( "wait" )){

                    }
                }catch (JSONException e) {
                    Log.d("client","JSONException occurred.");
                }
            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("client","received disconnect");

            }
        }).on ( STR_MOVE, new ExListener (this.playerName,STR_MOVE) {
            @Override
            void onReceive ( Sashite sashite, int time ) {
                RemoteMoveEvent remoteMoveEvent = new RemoteMoveEvent (
                        sashite.getPrePosition (),
                        sashite.getNextPosition (),
                        sashite.isBecomeFlg ()
                );
                EventBus.getDefault ().post ( remoteMoveEvent );

            }
        }).on ( STR_PUT, new ExListener (this.playerName,STR_PUT) {
            @Override
            void onReceive ( Sashite sashite, int time ) {
                RemotePutEvent remotePutEvent = new RemotePutEvent (
                        sashite.getKomaType (),
                        sashite.getNextDirection (),
                        sashite.getNextPosition ()
                );
                EventBus.getDefault ().post ( remotePutEvent );
            }
        } ).on ( STR_BEND, new ExListener (this.playerName, STR_BEND) {
            @Override
            void onReceive ( Sashite sashite, int time ) {
                RemoteBendEvent remoteBendEvent = new RemoteBendEvent (
                        sashite.getNextDirection (),
                        sashite.getNextPosition ()
                );
                EventBus.getDefault ().post ( remoteBendEvent );
            }
        } ).on ( STR_ENE_DISCON, new ExListener (this.playerName, STR_ENE_DISCON) {
            @Override
            void onReceive ( Sashite sashite, int time ) {
                Log.d("SocketClient","enemy disconnected.");
            }
        } );

        //接続
        mSocket.connect();
        Log.d("conn",String.valueOf ( mSocket.connected ()));
    }

    public Boolean getEarlierFlg () {
        return earlierFlg;
    }

}
