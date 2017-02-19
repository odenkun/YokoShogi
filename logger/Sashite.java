package com.example.yokoshogi.logger;

import android.util.Log;

import com.example.yokoshogi.Koma;
import com.example.yokoshogi.MainActivity;
import com.example.yokoshogi.Position;
import com.example.yokoshogi.network.SocketClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import static com.example.yokoshogi.network.ExListener.STR_AF_LOC;
import static com.example.yokoshogi.network.ExListener.STR_BEF_LOC;
import static com.example.yokoshogi.network.ExListener.STR_COL;
import static com.example.yokoshogi.network.ExListener.STR_ROW;
import static com.example.yokoshogi.network.SocketClient.STR_AF_DIR;
import static com.example.yokoshogi.network.SocketClient.STR_BEC_FLG;
import static com.example.yokoshogi.network.SocketClient.STR_KOMA_TYPE;

public class Sashite {

    private Koma.Types komaType;
    private Position
            prePosition,
            nextPosition;
    private Koma.directions
            nextDirection;
    private boolean becomeFlg;

    public Sashite ( Koma.Types komaType, Position prePos, Position nextPos, Koma.directions nextDir, boolean becomeFlg ) {
        this.komaType = komaType;
        this.prePosition = prePos;
        this.nextPosition = nextPos;
        this.nextDirection = nextDir;
        this.becomeFlg = becomeFlg;
        Log.d("sashite",this.toString());
    }
    public Sashite (JSONObject json) throws JSONException {

        //駒の種類
        komaType = Koma.Types.getType ( json.getString ( STR_KOMA_TYPE ));

        //前の場所
        Position.Builder befPosBuil =new Position.Builder ();

        JSONObject preLocate = json.getJSONObject ( STR_BEF_LOC );
        befPosBuil.setColumn ( preLocate.getInt ( STR_COL ) );
        befPosBuil.setRow ( preLocate.getInt ( STR_ROW ) );
        befPosBuil.setReverse ( MainActivity.getEarlierFlg () );

        prePosition = befPosBuil.build ();

        //次の場所
        Position.Builder afPosBuil = new Position.Builder ();

        JSONObject afLocate = json.getJSONObject ( STR_AF_LOC );
        afPosBuil.setColumn ( afLocate.getInt ( STR_COL ) );
        afPosBuil.setRow ( afLocate.getInt ( STR_ROW ) );
        afPosBuil.setReverse ( MainActivity.getEarlierFlg () );

        nextPosition = afPosBuil.build ();

        //次の方向
        nextDirection = Koma.directions.getDirection ( json.getString ( STR_AF_DIR ) );

        //成っているかどうか
        becomeFlg = json.getBoolean ( STR_BEC_FLG );

        Log.d("sashite from JSON",this.toString());
    }

    public Koma.Types getKomaType() {
        return komaType;
    }

    public Position getPrePosition () {
        return prePosition;
    }

    public Position getNextPosition () {
        return nextPosition;
    }


    public Koma.directions getNextDirection() {
        return nextDirection;
    }

    public boolean isBecomeFlg() {
        return becomeFlg;
    }

    @Override
    public String toString () {
        String string =
                String.valueOf(prePosition.getLocation ()[0]) + String.valueOf(prePosition.getLocation ()[1]) + "の" +
                komaType.toString() + "が";
        if (Arrays.equals(prePosition.getLocation (),nextPosition.getLocation ())) {
            string = string +
                    nextDirection.toString() + "向きになりました";
        }else {
            string = string +
                    String.valueOf(nextPosition.getLocation ()[0]) + String.valueOf(nextPosition.getLocation ()[1]) + "に";
            if (becomeFlg) {
                string = string + "成って移動しました";
            }else{
                string = string + "成らずに移動しました";
            }
        }

        return string;
    }

    public JSONObject getJSON () {
        JSONObject preLocate = new JSONObject ();
        JSONObject afLocate = new JSONObject ();
        JSONObject obj = new JSONObject ();

        try {
            preLocate.put ( STR_ROW, prePosition.getRow () );
            preLocate.put ( STR_COL, prePosition.getColumn () );
            afLocate.put ( STR_ROW, nextPosition.getRow () );
            afLocate.put ( STR_COL, nextPosition.getColumn () );

            obj.put ( "number", MainActivity.getInstance ().getKifu ().getCount ());
            obj.put ( STR_BEF_LOC, preLocate );
            obj.put ( STR_AF_LOC, afLocate );
            obj.put ( STR_AF_DIR, nextDirection.toString () );
            obj.put ( STR_KOMA_TYPE,komaType.toString () );
            obj.put ( STR_BEC_FLG,becomeFlg );
        } catch ( JSONException e ) {
            Log.e("JSON Error",e.toString ());
        }

        return obj;
    }
}
