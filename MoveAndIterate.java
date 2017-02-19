package com.example.yokoshogi;

import android.util.Log;

import java.util.ArrayList;

class MoveAndIterate {
    private Position move;
    private Boolean iterate;

    static final int NOT_ITERATABLE = - 1;

    static final int ALL_ITERATABLE = 0;
    static final int HALF_ITERATABLE = 4;

    MoveAndIterate (Position move,Boolean iterate,Koma.directions direction,Boolean enemyFlg) {
        Position tmpMove = move.copy ();

        if ( direction == Koma.directions.FORWARD ) {
            if (enemyFlg) {
                tmpMove.setRow( - tmpMove.getRow ());
                tmpMove.setColumn ( -tmpMove.getColumn () );
            }
        } else if ( direction == Koma.directions.LEFT ^ enemyFlg){
            int tmpCol = tmpMove.getColumn ();
            tmpMove.setColumn ( tmpMove.getRow () );
            tmpMove.setRow ( tmpCol );
        }else{ //(こっち側の右向きor敵側の左向き
            int tmpCol = tmpMove.getColumn ();
            tmpMove.setColumn ( - tmpMove.getRow () );
            tmpMove.setRow ( - tmpCol );
        }

        this.move = tmpMove;
        this.iterate = iterate;
    }

    Position getMove () {
        return move;
    }
    Boolean getIterate() {
        return iterate;
    }
    public String toString() {
        return move.toString () + "の" + iterate.toString()+"な動き";
    }
    //iterateIndexには、movesの中の何番目からiteratableな動きが始まるか入れる
    static ArrayList<MoveAndIterate> createArray(Position[] moves,int iterateStartIndex,Koma.directions direction,boolean enemyFlg) {
        ArrayList<MoveAndIterate> moveAndIterates = new ArrayList <> ( moves.length );
        for (int i = 0; i < moves.length;i++) {

            boolean iterate = (iterateStartIndex != NOT_ITERATABLE) && (i >= iterateStartIndex);
            moveAndIterates.add( new MoveAndIterate(moves[i],iterate,direction,enemyFlg));
        }
        return moveAndIterates;
    }
}
