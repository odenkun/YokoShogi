package com.example.yokoshogi;


import android.util.Log;

import com.example.yokoshogi.event.TurnChangeEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.example.yokoshogi.Ban.COLUMN_COUNT;
import static com.example.yokoshogi.Ban.KOMA_NUM;
import static com.example.yokoshogi.Ban.ROW_COUNT;

class KomaAndLocateList {
    private static KomaAndLocateList komaAndLocateList;

    //駒とその位置のリスト
    private HashMap<Position,Koma> komaList;
    //玉リスト
    private static HashMap<Boolean,Koma> gyokuList = new HashMap<> (2);

    KomaAndLocateList() {
        if (komaAndLocateList == null) {
            komaAndLocateList = this;
            komaList = new HashMap <> ( KOMA_NUM );
        }
    }
    static KomaAndLocateList getInstance () {
        return komaAndLocateList;
    }

    //新しく駒と位置の対応リストに追加
    void add(Koma koma) {
        komaList.put(koma.getPosition (),koma);

        if (koma.KOMA_TYPE == Koma.Types.GYOKU) {
            gyokuList.put(koma.getEnemyFlg (),koma);
        }
    }


    //駒と位置の対応リストから削除
    void remove(Koma koma) {
        Log.d("removed from komaList :",koma.toString ());
        komaList.remove(koma.getPosition ());
    }

    //敵味方を指定した駒リストを返却
    HashMap<Position,Koma> getKomaList(boolean enemyFlg) {
        HashMap<Position,Koma> returnKomaList = new HashMap <> ();
        for (Koma tmpKoma:komaList.values ()) {
            if (tmpKoma.getEnemyFlg() == enemyFlg ){
                returnKomaList.put(tmpKoma.getPosition (),tmpKoma);
            }
        }
        return returnKomaList;
    }

    //駒が存在"しない"位置のリストを返却
    List<Position> findPuttableLocates (Koma.Types komaType,Koma.directions orientation,Boolean enemyFlg) {

        //置けるマスのリスト
        List<Position> puttableLocates = new ArrayList<>();

        //選択されたのが歩の場合、二歩を考慮するので
        if (komaType == Koma.Types.FU) {
            //置ける列番号の一覧が代入される
            boolean[] puttableArray = NifuJudge.getInstance().getPuttable(orientation, enemyFlg);

            for (int i = 0; i < Ban.COLUMN_COUNT; i ++) {
                if ( ! (puttableArray[i])) {

                    for (int j = 0; j < ROW_COUNT; j++) {
                        Position.Builder builder = new Position.Builder ();
                        builder.setReverse ( MainActivity.getEarlierFlg () );
                        if (orientation == Koma.directions.FORWARD) {
                            builder.setColumn ( i );
                            builder.setRow ( j );
                        } else {
                            builder.setColumn ( j );
                            builder.setRow ( i );
                        }
                        if (this.find(builder.build ()) == null) {
                            puttableLocates.add(builder.build ());
                        }
                    }
                }

            }
        } else { //二歩を考慮しない場合
            for (int i = 0; i < COLUMN_COUNT; i++) {
                for (int j = 0; j < ROW_COUNT; j++) {
                    Position.Builder builder = new Position.Builder ();
                    builder.setRow ( i );
                    builder.setColumn ( j );
                    builder.setReverse ( MainActivity.getEarlierFlg () );
                    if (this.find(builder.build ()) == null) {
                        puttableLocates.add(builder.build ());
                    }
                }
            }
        }
        return puttableLocates;
    }

    //指定された場所にある駒を返却
    Koma find(Position position) {
        if (position != null &&
                8 >= position.getRow () && position.getRow () >= 0 &&
                8 >= position.getColumn () && position.getColumn () >= 0)  {
            return komaList.get ( position );
        }else {
            return null;
        }
    }

    static Koma getGyoku(boolean enemyFlg) {
        return gyokuList.get(enemyFlg);
    }
}