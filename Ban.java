package com.example.yokoshogi;


import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.yokoshogi.event.BoardTapEvent;
import com.example.yokoshogi.event.DirectionChangeEvent;
import com.example.yokoshogi.event.KomadaiTapEvent;
import com.example.yokoshogi.event.RemoteBendEvent;
import com.example.yokoshogi.event.RemoteMoveEvent;
import com.example.yokoshogi.event.RemotePutEvent;
import com.example.yokoshogi.event.TurnChangeEvent;
import com.example.yokoshogi.logger.Kifu;
import com.example.yokoshogi.logger.Sashite;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static com.example.yokoshogi.Koma.Types.*;
import static com.example.yokoshogi.Koma.directions.FORWARD;
import static com.example.yokoshogi.Koma.directions.LEFT;
import static com.example.yokoshogi.Koma.directions.RIGHT;

public class Ban {
    //将棋盤の行数、列数を表す
    static final int
            ROW_COUNT = 9,
            COLUMN_COUNT = 9;


    //すべての駒の数
    static final int KOMA_NUM = 40;


    //初期位置
    private static final Koma.Types[][] BAN_INITIAL = {
            {KYOUSHA,KEIMA,GIN,KIN,GYOKU,KIN,GIN,KEIMA,KYOUSHA},
            {EMP, HISHA, EMP, EMP, EMP, EMP, EMP,KAKU, EMP},
            {FU,  FU,  FU,  FU,  FU,  FU,  FU,  FU,  FU},
            {EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP},
            {EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP},
            {EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP},
            {FU,  FU,  FU,  FU,  FU,  FU,  FU,  FU,  FU},
            {EMP,KAKU, EMP, EMP, EMP, EMP, EMP, HISHA, EMP},
            {KYOUSHA,KEIMA,GIN,KIN,GYOKU,KIN,GIN,KEIMA,KYOUSHA},
    };

    //タップされた駒
    private Koma tappedKoma;
    //タップされた駒が移動可能な位置のリスト
    private List<Position> paintedLocates;

    private int[] colors;

    private GridLayout banGrid;

    static final int
            SHOWING_NOTHING = 0 ,
            SHOWING_PUTTABLE = 1 ,
            SHOWING_MOVABLE = 2 ;

    private int state = SHOWING_NOTHING;
    //どちらのターンか
    private boolean turn;
    //王手がかかっているか
    private boolean outeFlag = false;

    private boolean earlierFlg = false;

    //MainActivityから生成される
    Ban(GridLayout gridLayout,boolean turn) {

        EventBus.getDefault().register(this);

        this.turn = turn;
        this.earlierFlg = turn;
        paintedLocates = new ArrayList<>();
        colors = new int[]{
                ContextCompat.getColor(MainActivity.getInstance(), R.color.transparentBlack),//半透明の黒
                ContextCompat.getColor(MainActivity.getInstance(), R.color.transparent)//透明
        };

        this.banGrid = gridLayout;
        //ゴミがあると邪魔なので消す
        banGrid.removeAllViews();

        int masuWidth = banGrid.getMeasuredWidth() / COLUMN_COUNT ;
        int masuHeight = banGrid.getMeasuredHeight () / ROW_COUNT ;

        //駒を生成、設置するループ

        for (int y = 0; y < COLUMN_COUNT ; y++) {
            for (int x = 0; x < ROW_COUNT; x++) {
                Position.Builder builder = new Position.Builder ()
                        .setColumn ( x )
                        .setRow ( y )
                        .setReverse ( earlierFlg );
                Position position = builder.build ();
                if (turn) {
                    position = position.getPointSymmetry ();
                }
                LinearLayout ll = new LinearLayout ( MainActivity.getInstance () );

                ll.setBackground (
                        new TransitionDrawable(
                                new Drawable[] {
                                        MainActivity.getInstance ().getDrawable ( R.drawable.empty ),
                                        MainActivity.getInstance ().getDrawable ( R.drawable.waku_1st )
                                }
                        )
                );
                //クリックされた場所からその位置を引数としてbanのメソッドを実行する
                ll.setOnClickListener ( new View.OnClickListener () {

                    Position position;

                    View.OnClickListener setPosition ( Position position ) {
                        this.position = position;
                        return this;
                    }

                    @Override
                    public void onClick ( View view ) {
                        Log.d("tapped",position.toString ());
                        highlightMasu(view);
                        onTapped ( this.position );
                    }
                }.setPosition ( position.copy()) );

                if ( BAN_INITIAL[ y ][ x ] != EMP ) {
                    ImageView komaImage = new ImageView ( MainActivity.getInstance () );

                    ll.addView ( komaImage );

                    //駒のコンストラクタでリストに追加されるので必要
                    new Koma.Builder ()
                            .setPosition ( position )
                            .setEnemyFlg ( y < 3 )
                            .setDirection ( FORWARD )
                            .setKomaImage ( komaImage )
                            .setKomaType ( BAN_INITIAL[ y ][ x ] )
                            .build ();
                }
                //パラミタを適用するためのparamを宣言
                GridLayout.LayoutParams param = new GridLayout.LayoutParams();
                param.width = masuWidth;
                param.height = masuHeight;
                //paramを適用
                banGrid.addView ( ll, param );

            }
        }

    }

    private void changeTurn (){
        EventBus.getDefault ().post ( new TurnChangeEvent ( turn, MainActivity.getInstance ().getKifu ().getLatest () ) );
        turn = !(turn);
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRemoteBend ( RemoteBendEvent event) {
        Koma koma = KomaAndLocateList.getInstance ().find ( event.getPosition () );
        //盤上の選択されている駒の方向転換
        koma.changeDirection(event.getDirection ());
        tsumiJudge ();
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRemotePut ( RemotePutEvent event) {
        Koma putKoma = new Koma.Builder ()
                .setPosition ( event.getPosition () )
                .setEnemyFlg ( turn )
                .setDirection ( event.getDirection () )
                .setKomaImage ( new ImageView ( MainActivity.getInstance () ) )
                .setKomaType ( event.getKomaType () )
                .build ();
        //駒を置く
        //タップされた場所に対応するLinearLayoutに駒のImageViewを追加する
        LinearLayout masu = (LinearLayout) banGrid.getChildAt(event.getPosition ().getIndexOnBoard ());
        masu.addView(putKoma.getKomaImage());

        //駒台の対応する種類の残り駒数を減らす
        Komadai.getKomadai(turn).changeCount(putKoma.KOMA_TYPE, -1);

        //詰み判定
        tsumiJudge();
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRemoteMove( RemoteMoveEvent event) {

        //タップされた位置にある駒を検索
        Koma befKoma = KomaAndLocateList.getInstance().find(event.getBefPos ());
        Koma afKoma = KomaAndLocateList.getInstance().find(event.getAfPos ());
        //EventBus.getDefault().post(new BoardTapEvent (turn, koma, position ,(koma == tappedKoma) ) );

        //移動先のマスに駒が存在したら、それを取る処理
        if (afKoma != null) {
            afKoma.die();
        }
        //駒の移動
        befKoma.move(event.getAfPos (),true,event.isBecomeFlg ());

    }
    private void onTapped(Position position) {

        //成りかどうか尋ねるダイアログが表示されているときは何もしない
        if ( MainActivity.getInstance ().dialogExist () ) {
            return;
        }
        //遠隔のターンであるときは何もしない
//        if (turn) {
//            return;
//        }
        //タップされた位置にある駒を検索
        Koma koma = KomaAndLocateList.getInstance().find(position);
        if (koma != null) {
            Log.d("tappedKoma is ",koma.toString ());
        }
        Koma.Types curSelKomaType = Komadai.getKomadai (turn ).getCurrentSelectedKomaType ();
        EventBus.getDefault().post(new BoardTapEvent (turn, koma, position ,(koma == tappedKoma) ) );

        //光っているマスがタップされたかどうか
        if (isPaintedLocate(position)) {

            if (state == SHOWING_MOVABLE) { //すでに移動する駒を選択していた場合
                //移動先のマスに駒が存在したら、それを取る処理
                if (koma != null) {
                    koma.die();
                }
                //駒の移動
                tappedKoma.move(position,false,false);

            } else if (state == SHOWING_PUTTABLE) { //すでに駒台の駒を選択していた場合
                Log.d("mazu",Komadai.getKomadai (!turn ).getCurrentSelectedKomaType ().toString ());
                Koma putKoma = new Koma.Builder ()
                        .setPosition ( position )
                        .setEnemyFlg ( turn )
                        .setDirection ( OrientationSelect.getInstance ( turn ).getSelectedOrientation () )
                        .setKomaImage ( new ImageView ( MainActivity.getInstance () ) )
                        .setKomaType ( curSelKomaType )
                        .build ();
                //駒を置く
                //タップされた場所に対応するLinearLayoutに駒のImageViewを追加する
                LinearLayout masu = (LinearLayout) banGrid.getChildAt(position.getIndexOnBoard ());
                masu.addView(putKoma.getKomaImage());

                //駒台の対応する種類の残り駒数を減らす
                Komadai.getKomadai( turn).changeCount(putKoma.KOMA_TYPE, -1);


                Position.Builder befPosBuil =
                        new Position.Builder ()
                                .setColumn ( -999 )
                                .setRow ( -999 )
                        .setReverse ( MainActivity.getEarlierFlg () );

                Sashite sashite = new Sashite (
                        putKoma.KOMA_TYPE,
                        befPosBuil.build (),
                        putKoma.getPosition (),
                        putKoma.getDirection (),
                        putKoma.getBecomeFlg ()
                );
                MainActivity.getInstance ().getKifu ().add ( sashite );

                //詰み判定
                tsumiJudge();
            }
            changeState(SHOWING_NOTHING,null);

        } else {

            //盤上の駒が選択された
            if (koma != null && koma.getEnemyFlg() == turn && koma != tappedKoma) {

                changeState(SHOWING_MOVABLE,koma);

                //玉は王手の是非にかかわらず動きを制限されるため
                //そうでない駒は制限されていないとき、王手がかかっていなければ自由、かかっていれば全く動けない
                if ( koma.isConstraint () ) {
                    paintedLocates.addAll ( koma.getMovableLocates () );
                }else {
                    if ( ! (outeFlag) ) {
                        for ( MoveAndIterate moveAndIterate : koma.getMoveAndIterates () ) {
                            ArrayList <Position> movableLocates = getMovableLocates ( koma.getPosition (), moveAndIterate, turn );
                            paintedLocates.addAll ( movableLocates );
                        }
                    }
                }
                paintExceptPaintedLocatesAndCurrentLocate(koma.getPosition());
            } else {
                //タップされた場所に駒がなかったor駒はあったが、敵の駒だったときor前と同じ駒が選択された時
                changeState(SHOWING_NOTHING, null);
            }
        }
    }


    //駒台の駒が選択された場合
    @Subscribe
    public void onKomadaiTapped( KomadaiTapEvent event) {
        //前回と違う種類の駒がタップされたか？
        if (event.getPreType () != event.getNextType ()) {
            changeState(SHOWING_PUTTABLE,null);
            //盤の色塗り
            if ( outeFlag ) {
                paintedLocates = Komadai.getKomadai (turn ).getPuttableLocates ( event.getNextType (), FORWARD );
            }else {
                paintedLocates = KomaAndLocateList.getInstance ().findPuttableLocates ( event.getNextType (), FORWARD, turn );
            }
            paintExceptPaintedLocatesAndCurrentLocate();
            //state更新
        }else{
            
            //同じ駒が選択されたのでstateを戻す
            changeState(SHOWING_NOTHING,null);
        }
    }

    @Subscribe
    public void onKomaDirChanged( DirectionChangeEvent event){
        Koma.directions direction = event.getNextDirection ();
        if (tappedKoma != null){
            
            //盤上の選択されている駒の方向転換
            tappedKoma.changeDirection(direction);

            changeState(SHOWING_NOTHING,null);
            tsumiJudge ();
        }else{
            //駒台の駒を選択中の方向転換
            Koma.Types komaType = Komadai.getKomadai(turn).getCurrentSelectedKomaType();
            //なんか方向によって歩が置ける位置が違うからそれやる
            if (outeFlag) {
                paintedLocates = Komadai.getKomadai (turn ).getPuttableLocates ( komaType,direction );
            }else {
                paintedLocates = KomaAndLocateList.getInstance ().findPuttableLocates ( komaType, direction, turn );
            }
            paintExceptPaintedLocatesAndCurrentLocate();
        }
    }
    
    private void changeState (int state, Koma koma){
        //Log.d("前のstateは",String.valueOf(this.state));
        //Log.d("次のstateは", String.valueOf(state));
        if ( !(paintedLocates.isEmpty ()) || state == SHOWING_NOTHING) {
            paintInitialize();
            paintedLocates.clear ();
        }
        this.state = state;
        this.tappedKoma = koma;
    }


    private class KomaAndLocates {
        Koma koma;
        ArrayList<Position> locates;
        KomaAndLocates(Koma koma,ArrayList<Position> locates) {
            this.koma = koma;
            this.locates = locates;
        }
    }

    //王手、詰みを判定するメソッド
    public void tsumiJudge() {
        long start = System.currentTimeMillis ();
        Koma gyoku = KomaAndLocateList.getGyoku ( !( turn ) );
        List <Position> movableLocates = getGyokuMovableLocates ( gyoku );
        gyoku.addAllMovableLocates ( movableLocates );

        //王手かけられる側の駒
        HashMap<Position,Koma> meKomas = KomaAndLocateList.getInstance ().getKomaList ( !( turn ) );


        //実際に王手をかけた駒とその経路のリスト
        List <KomaAndLocates> outeKomaList = getOuteKomaList ( gyoku );
        outeFlag = outeKomaList.size () > 0;

        boolean tsumiFlag = outeFlag;

        for (KomaAndLocates outeKoma : outeKomaList ) {
            Log.d("王手をかけている駒は", outeKoma.koma.toString());
        }

        //王の壁となっている駒と、間接的王手をかけている駒の組合せのリスト
        ArrayList <Koma[]> wallKomaList = getWallKomaAndOuteKomaList ( gyoku.getEnemyFlg () );

        for ( Koma wallKoma[] : wallKomaList ) {
            Log.d("壁駒は",wallKoma[1].toString());
            ArrayList <Position> tmpList = new ArrayList <> ();
            for ( Position movableLocate : getAllMovableLocates ( wallKoma[ 1 ] ) ) {
                if ( isMovable ( movableLocate, wallKoma[ 1 ], wallKoma[ 0 ] ) ||
                        ( !( outeFlag ) && movableLocate.equals(wallKoma[ 0 ].getPosition ()))) {
                    tmpList.add ( movableLocate );
                }
            }
            wallKoma[ 1 ].addAllMovableLocates ( tmpList );
        }

        //王手をかけている駒が1つしか無い場合は
        if ( outeKomaList.size () == 1 ) {
            Koma outeKoma = outeKomaList.get ( 0 ).koma;
            ArrayList <Position> locates = new ArrayList <> (
                    outeKomaList.get ( 0 ).locates.subList ( 0, outeKomaList.get ( 0 ).locates.size () - 1 )
            );
            //合い駒判定
            //その駒と玉の間に１つ以上のスペースがあるならばそのスペースに駒を入れられるので
            if ( locates.size () >= 1 ) {
                Komadai komadai = Komadai.getKomadai (!( turn ) );
                if ( komadai.hasKomaExceptFu () ) {
                    tsumiFlag = false;
                    komadai.addPutLocExceptFU ( locates );
                    Log.d("歩以外の駒を持っているので","合い駒可能");
                }

                for ( Position enemyMoveLocate : locates ) {
                    //既存の駒を動かす事による合い駒
                    for ( Koma meKoma : meKomas.values () ) {
                        if ( meKoma.KOMA_TYPE == GYOKU ) {
                            continue;
                        }
                        Koma enemyKoma = null;
                        for ( Koma[] wallKoma : wallKomaList ) {
                            if ( meKoma.equals ( wallKoma[ 1 ] ) ) {
                                enemyKoma = wallKoma[ 0 ];
                                break;
                            }
                        }
                        if ( isMovable ( enemyMoveLocate, meKoma, enemyKoma ) ) {
                            meKoma.addMovableLocate ( enemyMoveLocate );
                            //Log.d("合い駒可能な駒は", meKoma.toString());
                            tsumiFlag = false;
                        }
                    }
                    //駒台から駒を打つことによる合い駒
                    /***
                     * 駒台に置ける場所リストを渡すことが必要
                     * それをタップされたとき駒台側からもらう
                     * そこをhighlightする
                     */
                    if ( !( komadai.hasFu () ) ) {
                        continue;
                    }
                    if ( !( NifuJudge.getInstance ().getPuttable ( FORWARD, !( turn ) )[ enemyMoveLocate.getColumn () ] ) ) {
                        komadai.addPutLocOnlyFU ( enemyMoveLocate, FORWARD );
                        tsumiFlag = false;
                        Log.d ( "fu is puttable in col", String.valueOf ( enemyMoveLocate.getColumn ()) );
                        break;
                    }
                    if ( !( NifuJudge.getInstance ().getPuttable ( LEFT, !( turn ) )[ enemyMoveLocate.getRow ()] ) ) {
                        komadai.addPutLocOnlyFU ( enemyMoveLocate, LEFT );
                        tsumiFlag = false;
                        Log.d ( "fu is puttable in row", String.valueOf ( enemyMoveLocate.getRow () ) );
                    }
                }
            }
            //とれるか判定
            for ( Koma meKoma : meKomas.values () ) {

                if ( meKoma.KOMA_TYPE == GYOKU ) {
                    continue;
                }
                Koma enemyKoma = null;
                for ( Koma[] wallKoma : wallKomaList ) {
                    if ( meKoma.equals ( wallKoma[ 1 ] ) ) {
                        enemyKoma = wallKoma[ 0 ];
                        break;
                    }
                }
                if ( isMovable ( outeKoma.getPosition (), meKoma, enemyKoma ) ) {
                    meKoma.addMovableLocate ( outeKoma.getPosition () );
                    Log.d("捕獲可能な駒は", meKoma.toString());
                    tsumiFlag = false;
                }
            }
        }
        for (Position escapable : movableLocates) {
            Log.d("移動可能なマスは", escapable.toString ());
        }

        if ( movableLocates.size () > 0 ) {
            tsumiFlag = false;
        }
        Log.d ( "詰みは", String.valueOf ( tsumiFlag ) );
        Log.d ("spend time is ", String.valueOf (System.currentTimeMillis () - start));
        if ( tsumiFlag ) {
            MainActivity.getInstance ().showResultDialog ( turn );
        } else if ( outeFlag ) {
            MainActivity.getInstance ().showOuteDialog ( );
        }

        changeTurn();
    }


    /*** meKomaがdestinationまで動いた時、
     * enemyKomaが動ける範囲を再探索して
     * もし王手がかかったらfalseを返すメソッド ***/
    /***
     * destinationは王手をかけている駒の位置or合い駒できる位置
     * meKomaはdestinationまで動こうとする駒
     * enemyKomaは王手をかけている駒or間接的王手をかけている駒 ***/
    private boolean isMovable (Position destination, Koma meKoma, Koma enemyKoma) {

        if ( ! (isReachable ( destination, meKoma, 1 ))) {
            return false;
        }

        List <Position> movableLocates = getAllMovableLocates(meKoma);

        for (Position movableLocate : movableLocates) {
            //目的地と同じ位置に移動可能なら
            if (!( destination.equals ( movableLocate ) )) {
                continue;
            }
            //wallKomaListにある場合は動いた後、王手がかからないか判定が必要
            if (enemyKoma == null) {
                return true;
            } else {
                boolean movableFlag = true;

                for (MoveAndIterate enemyMoveAndIterate : enemyKoma.getMoveAndIterates()) {

                    List <Position> afterMovableLocates =
                            getMovableLocatesAddLast(
                                    enemyKoma.getPosition(),
                                    enemyMoveAndIterate,
                                    meKoma.getPosition(),
                                    movableLocate
                            );
                    if (afterMovableLocates.size() <= 0) {
                        continue;
                    }
                    Position lastLocate = afterMovableLocates.get(afterMovableLocates.size() - 1);
                    Koma gyoku = KomaAndLocateList.getGyoku(meKoma.getEnemyFlg());
                    if (gyoku == null ) {
                        break;
                    }
                    if ( lastLocate.equals ( gyoku.getPosition() )  ) {
                        movableFlag = false;
                        break;
                    }
                }
                //敵の駒のどの動きでも王手がかかることはなかったため、動けることを伝える
                if (movableFlag) {
                    return true;
                }
            }
        }

        return false;
    }


    /***玉が移動可能な場所を取得***/
    private List<Position> getGyokuMovableLocates (Koma gyoku) {
        //玉の周囲の動けるマスのリスト
        boolean[][] immobileLocates = {
                {true,true,true},
                {true,true,true},
                {true,true,true}
        };

        //相手玉の動きごとに移動できる(=盤外でない、かつ味方の駒もいない)かどうか判断する
        // もし移動できたら、それを理論的に移動可能として反映する
        for (Position movableLocate: this.getAllMovableLocates(gyoku)) {
            immobileLocates
                    [movableLocate.getRow () - gyoku.getPosition().getRow () + 1]
                    [movableLocate.getColumn () - gyoku.getPosition ().getColumn () + 1] = false;
        }

        HashMap<Position,Koma> enemyKomaList = KomaAndLocateList.getInstance().getKomaList( !(gyoku.getEnemyFlg()) );

        //原点

        Position.Builder builder = new Position.Builder ()
                .setColumn ( 0 )
                .setRow ( 0 )
                .setReverse ( false );
        Position origin = builder.build ();
        for (Koma tmpKoma: enemyKomaList.values ()){

            if ( ! (isReachable(gyoku.getPosition(),tmpKoma, 2) )) {
                continue;
            }


            for (MoveAndIterate moveAndIterate: tmpKoma.getMoveAndIterates()) {
                //反復可能な駒の筋上には逃げられないので、
                //玉がnull位置に動いた = つまり玉がいないものとして移動可能範囲を検索
                ArrayList<Position> tmpLocates = getMovableLocatesAddLast(tmpKoma.getPosition(),moveAndIterate, gyoku.getPosition(), null);
                //もし玉の移動範囲に動くなら、それを移動不可場所リストに反映
                for ( Position movableLocate: tmpLocates) {
                    Position.Builder innerBuilder = new Position.Builder ()
                            .setColumn ( movableLocate.getColumn () - gyoku.getPosition ().getColumn () )
                            .setRow ( movableLocate.getRow () - gyoku.getPosition ().getRow ())
                            .setReverse ( earlierFlg );
                    Position relativeDistance = innerBuilder.build ();
                    //相対距離が、xとy共に、-1から1の範囲なら

                    if ( isRangeOf( relativeDistance, origin, 1 )) {
                        //Log.d("movableLocate is" , String.valueOf(movableLocate[0])+String.valueOf(movableLocate[1]));
                        immobileLocates[ relativeDistance.getRow () + 1][ relativeDistance.getColumn () + 1] = true;
                    }
                }
            }
        }
        List<Position> movableLocates = new ArrayList <> ();
        for (int i = 0;i < 3;i++) {
            for (int j = 0; j< 3; j++) {
                if ( ! (immobileLocates[i][j]) ) {
                    int[] currentLocate = new int[] {
                            gyoku.getPosition ().getRow() + i - 1,
                            gyoku.getPosition ().getColumn() + j - 1
                    };
                    Position.Builder innerBuilder =
                            new Position.Builder ()
                                    .setColumn ( currentLocate[1] )
                                    .setRow ( currentLocate[0] )
                                    .setReverse ( earlierFlg );
                    movableLocates.add (innerBuilder.build ());
                }
            }
        }
        return  movableLocates;
    }

    /***
     *     distance = 1の時、王手が可能な場所にいるかの判定
     *     distance = 2の時、玉の移動範囲に移動することが可能な場所にいるかの判定
     *     ***/
    private boolean isReachable (Position gyokuPosition, Koma enemyKoma, int distance) {

        int enemyX = enemyKoma.getPosition ().getColumn ();
        int enemyY  = enemyKoma.getPosition ().getRow ();
        int gyokuX = gyokuPosition.getColumn ();
        int gyokuY = gyokuPosition.getRow ();

        switch (enemyKoma.KOMA_TYPE) {

            case HISHA:
                if ( distance > Math.abs ( enemyX - gyokuX ) ) {
                    return true;
                } else if (distance > Math.abs ( enemyY - gyokuY )) {
                    return true;
                } else if (enemyKoma.getBecomeFlg() && isRangeOf(gyokuPosition, enemyKoma.getPosition (), distance)) {
                    return true;
                }
                break;

            case KAKU:
                if ( distance >  Math.abs(( gyokuX + gyokuY ) - ( enemyX + enemyY )) ) {
                    return true;
                } else if ( distance > Math.abs (( gyokuX - gyokuY ) - ( enemyX - enemyY)) ) {
                    return true;
                } else if ( enemyKoma.getBecomeFlg() && isRangeOf(gyokuPosition, enemyKoma.getPosition (), distance)) {
                    return true;
                }
                break;
            case KYOUSHA:
                if ( enemyKoma.getBecomeFlg() ) {
                    if ( isRangeOf( gyokuPosition, enemyKoma.getPosition (), distance  ) ) {
                        return true;
                    }
                } else {
                    if ( enemyKoma.getDirection () == FORWARD) {
                        if ( distance > Math.abs(gyokuX - enemyX) ) {
                            return true;
                        }
                    }else{
                        if ( distance > Math.abs(gyokuY - enemyY ) ) {
                            return true;
                        }

                    }
                }
                break;

            case KEIMA:
                if ( isRangeOf( gyokuPosition, enemyKoma.getPosition (), distance + 1) ) {
                    return true;
                }
                break;

            default:
                if ( isRangeOf( gyokuPosition, enemyKoma.getPosition (), distance) ) {
                    return true;
                }

        }
        return false;
    }


    //王手をかけている駒を取得
    private List<KomaAndLocates> getOuteKomaList (Koma gyoku) {
        List<KomaAndLocates> outeKomaList = new ArrayList <>();

        for (Position keiMove: Koma.Types.KEIMA.getMoves ()) {
            //回転、
            for (Koma.directions direction:Koma.directions.values ()) {
                MoveAndIterate moveAndIterate = new MoveAndIterate ( keiMove,false,direction, gyoku.getEnemyFlg ());
                Position position = getNextLocate ( gyoku.getPosition (),moveAndIterate.getMove () );
                if (position == null) {
                    continue;
                }
                Koma koma = KomaAndLocateList.getInstance ().find ( position );
                if (koma == null || gyoku.getEnemyFlg () == koma.getEnemyFlg () || koma.KOMA_TYPE != KEIMA || koma.getBecomeFlg ()) {
                    continue;
                }
                //敵の不成の桂馬が然るべき位置にあることがわかったので
                Position reverse = moveAndIterate.getMove ().getReverse ( false );
                for (MoveAndIterate keimaMai :koma.getMoveAndIterates ()) {
                    if (reverse.equals ( keimaMai.getMove () )) {
                        ArrayList<Position> locates = getMovableLocates(koma.getPosition (),keimaMai,koma.getEnemyFlg ());
                        outeKomaList.add ( new KomaAndLocates ( koma,locates)  );
                        break;
                    }
                }
            }
        }

        for (MoveAndIterate mai:gyoku.getMoveAndIterates ()) {
            //八方向にマスを探査する
            MoveAndIterate iterateMai = new MoveAndIterate (mai.getMove (),true, FORWARD,gyoku.getEnemyFlg ());
            ArrayList<Position> movableLocates = getMovableLocatesAddLast ( gyoku.getPosition (),iterateMai,null,null );
            //盤外に出る場合はその先に敵の駒はいないので除外
            if (movableLocates.size () <= 0 ) {
                continue;
            }
            //最後に当たった駒
            Koma lastKoma = KomaAndLocateList.getInstance ().find ( movableLocates.get ( movableLocates.size () - 1 ) );
            //無い、または味方の駒であるとき
            if (lastKoma == null || (gyoku.getEnemyFlg () == lastKoma.getEnemyFlg ())) {
                continue;
            }
            Position reverse = mai.getMove ().getReverse(gyoku.getEnemyFlg ());
            for (MoveAndIterate lastKomaMai: lastKoma.getMoveAndIterates ()) {
                //もし相手が逆の動きをするならば、王手なので
                if (!(reverse.equals( lastKomaMai.getMove () ))) {
                    continue;
                }
                if ( movableLocates.size () > 1 && lastKomaMai.getIterate () ) {
                    ArrayList <Position> lastMoves = new ArrayList <> ();
                    lastMoves.addAll ( getMovableLocates ( lastKoma.getPosition (), lastKomaMai, lastKoma.getEnemyFlg () ) );
                    outeKomaList.add ( new KomaAndLocates ( lastKoma, lastMoves ) );

                } else if ( movableLocates.size () == 1 ) { //反復移動しない動きで王手をする時、玉のすぐ隣にいる必要があるため
                    ArrayList <Position> lastMoves = new ArrayList <> ();
                    lastMoves.add ( getNextLocate ( lastKoma.getPosition (), lastKomaMai.getMove () ) );
                    outeKomaList.add ( new KomaAndLocates ( lastKoma, lastMoves ) );
                }
                break;
            }
        }
        return outeKomaList;
    }
    private KomaAndLocates  getLastKomaAndMovLocs (Position position ,MoveAndIterate moveAndIterate) {
        ArrayList<Position> movableLocates = getMovableLocatesAddLast ( position , moveAndIterate,null,null );
        Koma lastKoma = null ;
        //盤外に出る場合はその先に敵の駒はいないので除外
        if (movableLocates.size () > 0 ) {
            lastKoma  = KomaAndLocateList.getInstance ().find ( movableLocates.get ( movableLocates.size () - 1 ) );
        }
        return new KomaAndLocates (lastKoma,movableLocates);
    }

    private ArrayList<Koma[]> getWallKomaAndOuteKomaList (boolean turn) {
        Koma gyoku = KomaAndLocateList.getGyoku ( turn );
        ArrayList<Koma[]> wallKomaAndOuteKomaList = new ArrayList <>();

        for (MoveAndIterate mai:gyoku.getMoveAndIterates ()) {
            //八方向にマスを探査する
            MoveAndIterate iterateMai = new MoveAndIterate (mai.getMove (),true, FORWARD,gyoku.getEnemyFlg ());
            KomaAndLocates firstKal = getLastKomaAndMovLocs ( gyoku.getPosition (),iterateMai );
            //無い、または敵の駒であるとき間接的王手は無いので
            if ( firstKal.koma == null || (gyoku.getEnemyFlg () != firstKal.koma.getEnemyFlg ())) {
                continue;
            }
            KomaAndLocates secondKal = getLastKomaAndMovLocs ( firstKal.koma.getPosition (),iterateMai );
            if ( secondKal.koma == null || (gyoku.getEnemyFlg () == secondKal.koma.getEnemyFlg ()) || !(secondKal.koma.isIterate ())) {
                continue;
            }
            Position reverse = mai.getMove ().getReverse(gyoku.getEnemyFlg ());

            for (MoveAndIterate lastKomaMai: secondKal.koma.getMoveAndIterates ()) {
                //反復可能かつ、来たときと逆の動きをする時のみ王手なので
                if (lastKomaMai.getIterate () && !(reverse.equals( lastKomaMai.getMove () ))) {
                    wallKomaAndOuteKomaList.add ( new Koma[] {secondKal.koma,firstKal.koma} );
                    break;
                }
            }
       }
        return wallKomaAndOuteKomaList;
    }

    static private int[] distanceIs(Position a, Position b) {
        return new int[] { Math.abs(a.getColumn () - b.getColumn ()) , Math.abs(a.getRow () - b.getRow ()) };
    }

    private boolean isOnBoard (Position position) {
        Position.Builder builder =
                new Position.Builder ()
                        .setColumn ( 4 )
                        .setRow ( 4 )
                        .setReverse ( false );
        return isRangeOf ( position,builder.build (),4);
    }

    private static boolean isRangeOf(Position a,Position b, int distance) {
        int[] c = distanceIs(a,b);
        return (c[0] <= distance && c[1] <= distance);
    }


    private ArrayList<Position> getAllMovableLocates (Koma koma) {
        ArrayList<Position> movableLocates = new ArrayList <>();
        for (MoveAndIterate moveAndIterate:koma.getMoveAndIterates()) {
            movableLocates.addAll( getMovableLocates(koma.getPosition(), moveAndIterate, koma.getEnemyFlg() ) );
        }
        return movableLocates;
    }
    private ArrayList<Position> getMovableLocates (Position position,MoveAndIterate moveAndIterate,boolean enemyFlg){
        ArrayList<Position> movableLocates = getMovableLocatesAddLast(position,moveAndIterate,null,null);
        if ( movableLocates.size() > 0 ) {
            Koma lastKoma = KomaAndLocateList.getInstance().find(movableLocates.get(movableLocates.size() - 1));
            if (lastKoma != null && lastKoma.getEnemyFlg() == enemyFlg) {
                movableLocates.remove(movableLocates.size() - 1);
            }
        }
        return movableLocates;
    }
    private ArrayList<Position> getMovableLocatesAddLast (Position position,MoveAndIterate moveAndIterate,Position befPos,Position afPos){

        ArrayList<Position> movableLocates = new ArrayList<>();
        //配列のためディープコピー
        Position currentPosition = position.copy ();

        do {

            currentPosition = getNextLocate(currentPosition,moveAndIterate.getMove());

            if (currentPosition == null){
                break;
            }

            Koma inevitableKoma;
            if (afPos != null && currentPosition.equals ( afPos )) {
                inevitableKoma = KomaAndLocateList.getInstance().find(befPos);
            }else if(befPos != null && currentPosition.equals ( befPos )) {
                inevitableKoma = null;
            }else{
                inevitableKoma = KomaAndLocateList.getInstance().find(currentPosition);
            }
            movableLocates.add(currentPosition.copy ());
            if (inevitableKoma != null) {
                break;
            }
        } while (moveAndIterate.getIterate());

        return movableLocates;
    }

    private Position getNextLocate (Position currentPosition,Position move) {
        Position.Builder builder =
                new Position.Builder ()
                        .setColumn ( currentPosition.getColumn () + move.getColumn ()  )
                        .setRow ( currentPosition.getRow () + move.getRow () )
                        .setReverse ( earlierFlg );
        Position nextPos = builder.build ();
        if (isOnBoard ( nextPos )) {
            return nextPos;
        }else {
            return null;
        }
    }

    private void paintExceptPaintedLocatesAndCurrentLocate() {
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 9; y++) {
                Position.Builder builder =
                        new Position.Builder ()
                                .setColumn ( x )
                                .setRow ( y )
                                .setReverse ( this.earlierFlg );
                paintLocate(builder.build () , colors[0]);
            }
        }
        for (Position paintedLocate : paintedLocates) {
            paintLocate(paintedLocate);
        }
    }

    private void paintExceptPaintedLocatesAndCurrentLocate(Position position) {
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 9; y++) {
                Position.Builder builder =
                        new Position.Builder ()
                                .setColumn ( x )
                                .setRow ( y )
                                .setReverse ( earlierFlg );
                paintLocate(builder.build (), colors[0]);
            }
        }
        for (Position paintedLocate : paintedLocates) {
            //Log.d("paint",String.valueOf(paintedLocate[0]) + String.valueOf(paintedLocate[1]));
            paintLocate(paintedLocate);
        }
        paintLocate(position);
    }

    private void paintLocate(Position locate,int color) {
        LinearLayout linearLayout = (LinearLayout) banGrid.getChildAt(locate.getIndexOnBoard ());
        Drawable drawable;
        if (color == colors[0]) {
            drawable = MainActivity.getInstance().getDrawable(R.drawable.deep_gray);
        }else{
            drawable = MainActivity.getInstance().getDrawable(R.drawable.empty);//透明
        }
        linearLayout.setForeground(drawable);
    }
    private void paintLocate(Position position) {
        paintLocate(position,colors[1]);//透明に戻すために透明の色をぬるが、リセットするメソッドが好ましい
    }

    //青く塗られたマス(移動可能なマス)がタップされたかどうか
    private boolean isPaintedLocate(Position locate) {
        if(locate!=null) {
            for (Position paintedLocate:paintedLocates) {
                if (paintedLocate.equals ( locate )) return true;
            }
        }
        return false;
    }
    private void paintInitialize () {
        for (int x = 0;x < 9 ; x++) {
            for (int y = 0 ; y < 9 ; y++) {
                Position.Builder builder =
                        new Position.Builder ()
                                .setColumn ( x )
                                .setRow ( y )
                                .setReverse ( false );
                paintLocate(builder.build ());
            }
        }
    }


    public static Ban getInstance() {
        return MainActivity.getInstance().getBan();
    }


    boolean getTurn() {
        return turn;
    }


    int getState() {
        return state;
    }

    void highlightMasu(View viewGroup) {
        TransitionDrawable animationDrawable = (TransitionDrawable) viewGroup.getBackground();
        animationDrawable.startTransition(0);
        animationDrawable.reverseTransition(1000);
    }


    GridLayout getBanGrid () {
        return banGrid;
    }

    boolean isOuted () {
        return outeFlag;
    }
}