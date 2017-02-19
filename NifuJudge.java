package com.example.yokoshogi;

import android.util.Log;

import java.util.Arrays;


class NifuJudge {

    private static NifuJudge nifuJudge;

    private boolean[]
            tateFuArray,
            yokoFuArray,
            enemyTateFuArray,
            enemyYokoFuArray;


    NifuJudge () {
        if (nifuJudge == null) {
            nifuJudge = this;

            tateFuArray = new boolean[9];
            Arrays.fill(tateFuArray,false);

            yokoFuArray = new boolean[9];
            Arrays.fill(yokoFuArray,false);

            enemyTateFuArray = new boolean[9];
            Arrays.fill(enemyTateFuArray,false);

            enemyYokoFuArray = new boolean[9];
            Arrays.fill(enemyYokoFuArray,false);

        }else{
            Log.d("user","Second NifuJudge generated.");
        }
    }

    void fuAppend (Koma koma) {
        this.setArrayElement(koma,true);
    }

    void fuRemove (Koma koma) {
        this.setArrayElement(koma,false);
    }
    private void setArrayElement (Koma koma,boolean flg) {
        Position locate = koma.getPosition ();
        if (koma.getEnemyFlg()) {
            if(koma.getDirection() == Koma.directions.FORWARD) {
                this.enemyTateFuArray[locate.getColumn ()] = flg;
            }else {
                this.enemyYokoFuArray[locate.getRow ()] = flg;
            }
        }else {
            if(koma.getDirection() == Koma.directions.FORWARD) {
                this.tateFuArray[locate.getColumn ()] = flg;
            }else {
                this.yokoFuArray[locate.getRow ()] = flg;
            }
        }
    }
     boolean[] getPuttable(Koma.directions orientation,boolean enemyFlg) {
         boolean[] array;
         if (enemyFlg) {
             if (orientation == Koma.directions.FORWARD) {
                 array = this.enemyTateFuArray;
             } else {
                 array = this.enemyYokoFuArray;
             }
         }else{
             if (orientation == Koma.directions.FORWARD) {
                 array = this.tateFuArray;
             } else {
                 array = this.yokoFuArray;
             }
         }
         return array;
     }

    static NifuJudge getInstance () {
        return nifuJudge;
    }
}
