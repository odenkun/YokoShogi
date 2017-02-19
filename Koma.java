package com.example.yokoshogi;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.yokoshogi.event.BoardTapEvent;
import com.example.yokoshogi.event.KomadaiTapEvent;
import com.example.yokoshogi.event.TurnChangeEvent;
import com.example.yokoshogi.logger.Sashite;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.yokoshogi.Koma.Types.FU;
import static com.example.yokoshogi.Koma.Types.GIN;
import static com.example.yokoshogi.Koma.Types.GYOKU;
import static com.example.yokoshogi.Koma.Types.HISHA;
import static com.example.yokoshogi.Koma.Types.KAKU;
import static com.example.yokoshogi.Koma.Types.KEIMA;
import static com.example.yokoshogi.Koma.Types.KIN;
import static com.example.yokoshogi.Koma.Types.KYOUSHA;
import static com.example.yokoshogi.Koma.Types.RYU;
import static com.example.yokoshogi.Koma.Types.UMA;


public class Koma {

    public enum directions {
        LEFT("左",270,R.id.arrow_left_me,R.id.arrow_left_enemy),
        FORWARD("直",0,R.id.arrow_forward_me,R.id.arrow_forward_enemy),
        RIGHT("右",90,R.id.arrow_right_me,R.id.arrow_right_enemy);
        private final String name;
        private final int angle;
        private final int
                meIVId,
                enemyIVId;

        directions ( String name, int angle, int meIVId, int enemyIVId ) {
            this.name = name;
            this.angle = angle;
            this.meIVId = meIVId;
            this.enemyIVId = enemyIVId;
        }
        public static directions getDirection (String dirName) {
            for (directions direction : directions.values ()) {
                if (direction.toString ().equals ( dirName )) {
                    return direction;
                }
            }
            return null;
        }
        public int getIVId (boolean enemyFlg) {
            if (enemyFlg) {
                return this.enemyIVId;
            }else{
                return this.meIVId;
            }
        }
        public int getAngle ( boolean enemyFlg) {
            if (enemyFlg) {
                return (angle + 180) % 360;
            } else {
                return angle;
            }
        }
        @Override
        public String toString() {
            return name;
        }

    }

    public enum Types {
        FU (new TypesFactory()
                .setName("歩")
                .setMoves(new int[][] { {0, -1}})
                .setIteratableIndex(MoveAndIterate.NOT_ITERATABLE)
                .setRotatable(true)
                .setBecomable(true)
                .setImageID(R.drawable.fu)
                .setBecomeImageID(R.drawable.fu_nari)
                .setEnemyImageID(R.drawable.ene_fu)
                .setEnemyBecomeImageID(R.drawable.ene_fu_nari)
                .setNumber ( 0 )
        ),
        KYOUSHA (new TypesFactory()
                .setName("香車")
                .setMoves(new int[][] { {0, - 1 }})
                .setIteratableIndex(MoveAndIterate.ALL_ITERATABLE)
                .setRotatable(true)
                .setBecomable(true)
                .setImageID(R.drawable.kyousha)
                .setBecomeImageID(R.drawable.kyousha_nari)
                .setEnemyImageID(R.drawable.ene_kyousha)
                .setEnemyBecomeImageID(R.drawable.ene_kyousha_nari)
                .setNumber ( 1 )
        ),
        KEIMA (new TypesFactory()
                .setName("桂馬")
                .setMoves(new int[][] {
                        {-1,-2},{1,-2}
                })
                .setIteratableIndex(MoveAndIterate.NOT_ITERATABLE)
                .setRotatable(true)
                .setBecomable(true)
                .setImageID(R.drawable.keima)
                .setBecomeImageID(R.drawable.kei_nari)
                .setEnemyImageID(R.drawable.ene_keima)
                .setEnemyBecomeImageID(R.drawable.ene_kei_nari)
                .setNumber ( 2 )
        ),
        GIN (new TypesFactory()
                .setName("銀")
                .setMoves(new int[][] {
                        {-1, -1},{0, -1},{1, -1},{1, 1},{-1, 1}
                })
                .setIteratableIndex(MoveAndIterate.NOT_ITERATABLE)
                .setRotatable(true)
                .setBecomable(true)
                .setImageID(R.drawable.gin)
                .setBecomeImageID(R.drawable.gin_nari)
                .setEnemyImageID(R.drawable.ene_gin)
                .setEnemyBecomeImageID(R.drawable.ene_gin_nari)
                .setNumber ( 3 )
        ),
        KIN (new TypesFactory()
                .setName("金")
                .setMoves(new int[][] {
                        {-1, 0}, {-1, -1}, {0, -1}, {1, -1}, {1, 0}, {0, 1}
                })
                .setIteratableIndex(MoveAndIterate.NOT_ITERATABLE)
                .setRotatable(true)
                .setBecomable(false)
                .setImageID(R.drawable.kin)
                .setEnemyImageID(R.drawable.ene_kin)
                .setNumber ( 4 )
        ),
        KAKU (new TypesFactory()
                .setName("角")
                .setMoves(new int[][] {
                        {1,1},{1,-1},{-1,1},{-1,-1}//すべて反復可能
                })
                .setIteratableIndex(MoveAndIterate.ALL_ITERATABLE)
                .setRotatable(false)
                .setBecomable(true)
                .setImageID(R.drawable.kaku)
                .setEnemyImageID(R.drawable.ene_kaku)
                .setBecomeImageID(R.drawable.kaku_nari)
                .setEnemyBecomeImageID(R.drawable.ene_kaku_nari)
                .setNumber ( 5 )

        ),
        HISHA (new TypesFactory()
                .setName("飛車")
                .setMoves(new int[][] {
                        {1,0},{-1,0},{0,1},{0,-1} //すべて反復可能
                })
                .setIteratableIndex(MoveAndIterate.ALL_ITERATABLE)
                .setRotatable(false)
                .setBecomable(true)
                .setImageID(R.drawable.hisha)
                .setEnemyImageID(R.drawable.ene_hisha)
                .setBecomeImageID(R.drawable.hisha_nari)
                .setEnemyBecomeImageID(R.drawable.ene_hisha_nari)
                .setNumber ( 6 )
        ),

        GYOKU (new TypesFactory()
                .setName("玉")
                .setMoves(new int[][]{
                        {-1, 0}, {-1, -1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}
                })
                .setIteratableIndex(MoveAndIterate.NOT_ITERATABLE)
                .setRotatable(false)
                .setBecomable(false)
                .setImageID(R.drawable.gyoku)
                .setEnemyImageID(R.drawable.ene_gyoku)
                .setNumber ( 999 )

        ),
        UMA (new TypesFactory()
                .setName("馬")
                .setMoves(new int[][] {
                        {-1,0},{0,-1},{1,0},{0,1},
                        {1,1},{1,-1},{-1,1},{-1,-1}//4つ目から反復可能
                })
                .setIteratableIndex( MoveAndIterate.HALF_ITERATABLE )
                .setRotatable(false)
                .setBecomable(false)
                .setImageID(R.drawable.kaku)
                .setEnemyImageID(R.drawable.ene_kaku)
                .setBecomeImageID(R.drawable.kaku_nari)
                .setEnemyBecomeImageID(R.drawable.ene_kaku_nari)
                .setNumber ( 5 )
        ),
        RYU (new TypesFactory()
                .setName("龍")
                .setMoves(new int[][] {
                        {-1,-1},{1,-1},{-1,1},{1,1},//0-3番目まで反復不可
                        {1,0},{-1,0},{0,1},{0,-1} //4つ目から反復可能
                })
                .setIteratableIndex( MoveAndIterate.HALF_ITERATABLE )
                .setRotatable(false)
                .setBecomable(false)
                .setImageID(R.drawable.hisha)
                .setEnemyImageID(R.drawable.ene_hisha)
                .setBecomeImageID(R.drawable.hisha_nari)
                .setEnemyBecomeImageID(R.drawable.ene_hisha_nari)
                .setNumber ( 6 )
        ),
        EMP (new TypesFactory().setName("空"));

        private final String name;
        private final Position[] moves;
        private final int iteratableIndex;
        private final boolean rotatable;
        private final boolean becomable;
        private final int
                imageID,
                enemyImageID,
                becomeImageID,
                enemyBecomeImageID;
        private final int number;

        Types (TypesFactory typesFactory) {
            this.name = typesFactory.name;
            this.moves = typesFactory.moves;
            this.iteratableIndex = typesFactory.iteratableIndex;
            this.rotatable = typesFactory.rotatable;
            this.becomable = typesFactory.becomable;
            this.imageID = typesFactory.imageID;
            this.enemyImageID = typesFactory.enemyImageID;
            this.becomeImageID = typesFactory.becomeImageID;
            this.enemyBecomeImageID = typesFactory.enemyBecomeImageID;
            this.number = typesFactory.number;
        }

        public static Types getType (String typeName) {
            for (Types type : Types.values ()) {
                if (type.toString ().equals ( typeName )) {
                    return type;
                }
            }
            return null;
        }

        static class TypesFactory {
            private String name;
            private Position[] moves;
            private int iteratableIndex;
            private boolean rotatable;
            private boolean becomable;
            private int imageID;
            private int enemyImageID;
            private int becomeImageID;
            private int enemyBecomeImageID;
            private int number;

            private TypesFactory setName (String name) {
                this.name = name;
                return this;
            }

            private TypesFactory setMoves (int[][] moves) {
                Position[] positions = new Position[moves.length];
                for (int i = 0; i < moves.length; i++ ) {
                    Position.Builder builder = new Position.Builder ();
                    builder.setColumn ( moves[i][0] );
                    builder.setRow ( moves[i][1] );
                    builder.setReverse ( false );
                    positions[i]  = builder.build ();
                }
                this.moves = positions;
                return this;

            }

            private TypesFactory setIteratableIndex (int iteratableIndex) {
                this.iteratableIndex = iteratableIndex;
                return this;
            }
            private TypesFactory setRotatable (boolean rotatable) {
                this.rotatable = rotatable;
                return this;
            }
            private TypesFactory setBecomable (boolean becomable) {
                this.becomable = becomable;
                return this;
            }

            private TypesFactory setImageID (int imageID) {
                this.imageID = imageID;
                return this;
            }

            private TypesFactory setEnemyImageID (int enemyImageID) {
                this.enemyImageID = enemyImageID;
                return this;
            }

            private TypesFactory setBecomeImageID (int becomeImageID) {
                this.becomeImageID = becomeImageID;
                return this;
            }

            private TypesFactory setEnemyBecomeImageID (int enemyBecomeImageID) {
                this.enemyBecomeImageID = enemyBecomeImageID;
                return this;
            }
            private TypesFactory setNumber ( int number) {
                this.number = number;
                return this;
            }
        }

        public Position[] getMoves () {
            return Arrays.copyOf(moves,moves.length);
        }

        public int getIteratableIndex () {
            return iteratableIndex;
        }

        public boolean isRotatable () {
            return rotatable;
        }

        public boolean isBecomable () {
            return becomable;
        }

        public int getImageID (boolean enemyFlg, boolean becomeFlg) {
            if (enemyFlg) {
                if (becomeFlg) {
                    return enemyBecomeImageID;
                }else{
                    return enemyImageID;
                }
            }else{
                if (becomeFlg) {
                    return becomeImageID;
                }else{
                    return imageID;
                }
            }
        }
        public static Types[] getPrimitives () {
            return new Types[] { FU,KYOUSHA,KEIMA,GIN,KIN,KAKU,HISHA};
        }
        public int getNumber () {
            return this.number;
        }
        @Override
        public String toString() {
            return name;
        }

    }

    private static final int
            //一度タップされた状態(移動可能なマスを表示している状態)
            STATE_SHOWING_MOVABLE = 0,
    //もう一度タップされた状態(移動可能なマスを非表示にしている状態)
    STATE_NORMAL = 1;

    public Types KOMA_TYPE;

    //現在位置を示す配列
    private Position position;
    //成り駒かどうか
    private boolean becomeFlg = false;
    //敵かどうか
    private boolean enemyFlg;
    //現在の方向
    private Koma.directions direction;
    //駒の画像
    private ImageView komaImage;
    //現在のタップ状態
    private int tapState = STATE_NORMAL;
    //もし壁駒など動きに制約があった場合nullでなくなる
    private List<Position> movableLocates = null;

    private Koma (Builder builder) {

        this.position = builder.position;
        this.enemyFlg = builder.enemyFlg;
        this.direction = builder.direction;
        this.KOMA_TYPE = builder.komaType;
        this.komaImage = builder.komaImage;

        this.tapState = STATE_NORMAL;
        Log.d("flag",String.valueOf ( MainActivity.getEarlierFlg() ) + String.valueOf ( enemyFlg )+ position.toString ());
        this.komaImage.setImageResource(KOMA_TYPE.getImageID(MainActivity.getEarlierFlg() ^ enemyFlg,false));

        this.komaImage = (ImageView) rotationView(this.komaImage,this.direction,enemyFlg);

        if (KOMA_TYPE == FU) {
            NifuJudge.getInstance().fuAppend(this);
        }
        EventBus.getDefault ().register ( this );
        KomaAndLocateList.getInstance().add(this); //リストに追加。移動時の更新と死亡時の削除も必要
    }

    static class Builder {
        Position position;
        boolean enemyFlg;
        directions direction;
        ImageView komaImage;
        Types komaType;

        Koma build () {
            return new Koma(this);
        }

        Builder setPosition ( Position position ) {
            this.position = position;
            return this;
        }

        Builder setEnemyFlg ( boolean enemyFlg ) {
            this.enemyFlg = enemyFlg;
            return this;
        }

        Builder setDirection ( directions direction ) {
            this.direction = direction;
            return this;
        }

        Builder setKomaImage ( ImageView komaImage ) {
            this.komaImage = komaImage;
            return this;
        }

        Builder setKomaType ( Types komaType ) {
            this.komaType = komaType;
            return this;
        }
    }

    public Position getPosition() {
        return position;
    }
    boolean getBecomeFlg() {
        return becomeFlg;
    }
    public boolean getEnemyFlg() {
        return enemyFlg;
    }
    public directions getDirection() {
        return direction;
    }
    @Subscribe
    public void onTurnChangeEvent (TurnChangeEvent event ) {
        if (event.getBeforeTurn () == this.enemyFlg ) {
//            Log.d("koma",toString ());
//            Log.d("beforeTurn",String.valueOf ( event.getBeforeTurn () ));
//            Log.d("earlierFlg",String.valueOf ( MainActivity.getEarlierFlg () ));
//            Log.d("enemyFlg", String.valueOf ( this.enemyFlg ));
            this.resetMovableLocate ();
        }
    }
    @Subscribe
    public void onBoardTapEvent ( BoardTapEvent event ) {
        if (event.getTurn () == this.getEnemyFlg () ) {
            this.onTapped ( event.getPosition () );
        }
    }
    @Subscribe
    public void onKomadaiTapEvent ( KomadaiTapEvent event) {
        onTapped ( null );
    }
    private void onTapped (Position position) {
        if (this.tapState == STATE_NORMAL) {
            if (this.position.equals ( position )) {
                rotate ( this.enemyFlg );
                this.tapState = STATE_SHOWING_MOVABLE;
            }
        }else{
            rotate ( !this.enemyFlg );
            this.tapState = STATE_NORMAL;
        }
    }
    private void rotate (boolean reverse) {
        int incrementalX = -7;
        int incrementalY = -7;
        int incrementalRotation = 2;

        if ( reverse ) {
            incrementalX = -incrementalX;
            incrementalY = -incrementalY;
            incrementalRotation = -incrementalRotation;
        }

        komaImage.setX ( komaImage.getX () + incrementalX );
        komaImage.setY ( komaImage.getY () + incrementalY );
        komaImage.setRotation ( komaImage.getRotation () + incrementalRotation );
    }

    //移動
    void move(Position nextPos, boolean remoteFlg,boolean remoteBecFlg) {

        //画像の位置を変更
        LinearLayout parentll = (LinearLayout) komaImage.getParent();
        parentll.removeView(komaImage);
        LinearLayout newParentll = (LinearLayout) Ban.getInstance().getBanGrid().getChildAt(nextPos.getIndexOnBoard ());
        newParentll.addView(komaImage);

        boolean showDialogFlag = false;
        if ( remoteFlg ) {
            this.changeBecomeFlg ( remoteBecFlg);
        }else{
            //成りの処理
            if ( this.KOMA_TYPE.isBecomable () && !(this.becomeFlg)) {
                switch ( direction ) {
                    case FORWARD:
                        if ( enemyFlg ^ MainActivity.getEarlierFlg() ) {
                            showDialogFlag = ( nextPos.getRow () >= 6 || position.getRow () >= 6 );
                        } else {
                            showDialogFlag = ( nextPos.getRow () <= 2 || position.getRow () <= 2 );
                        }
                        break;
                    case LEFT:
                        if ( enemyFlg ^ MainActivity.getEarlierFlg() ) {
                            showDialogFlag = ( nextPos.getColumn () == 8 || position.getColumn () == 8 );
                        } else {
                            showDialogFlag = ( nextPos.getColumn () == 0 || position.getColumn () == 0 );
                        }
                        break;
                    case RIGHT:
                        if ( enemyFlg ^ MainActivity.getEarlierFlg() ) {
                            showDialogFlag = ( nextPos.getColumn () == 0 || position.getColumn () == 0 );
                        } else {
                            showDialogFlag = ( nextPos.getColumn () == 8 || position.getColumn () == 8 );
                        }
                        break;
                }
            }
        }
        Position befPos = position.copy ();

        KomaAndLocateList.getInstance().remove(this);
        //成りダイアログの後にする必要あり
        this.position = nextPos;

        KomaAndLocateList.getInstance().add(this);

        if (showDialogFlag) {
            MainActivity.getInstance ().showBecomeDialog ( this, befPos );
        }else{
            //ローカルから呼び出されたら指し手を登録
            Sashite sashite = new Sashite ( KOMA_TYPE, befPos, position.copy (), direction, this.becomeFlg );
            MainActivity.getInstance ().getKifu ().add ( sashite );
            Ban.getInstance ().tsumiJudge ();
        }
    }

    public void changeBecomeFlg(boolean becomeFlg) {
        if (becomeFlg) {
            this.becomeFlg = true;
            this.komaImage.setImageResource ( KOMA_TYPE.getImageID ( MainActivity.getEarlierFlg () ^ enemyFlg, this.becomeFlg ) );

            if ( KOMA_TYPE == FU ) {
                NifuJudge.getInstance ().fuRemove ( this );
            }
        }
    }

    ArrayList<MoveAndIterate> getMoveAndIterates () {

        Types komaType = KOMA_TYPE; //変更する可能性もあるため退避
        if (becomeFlg) {
            if (komaType == FU || komaType == KYOUSHA || komaType == KEIMA || komaType == GIN) {
                komaType = KIN;
            }else if (komaType == HISHA) {
                komaType = RYU;
            }else if (komaType == KAKU) {
                komaType = UMA;
            }
        }

        //動き一覧
        Position[] moves = komaType.getMoves();
        if (MainActivity.getEarlierFlg()) {
            for ( int i = 0; i < moves.length; i++ ) {
                moves[i] = moves[i].getReverse (false);
            }
        }
        int iteratableStartIndex = komaType.getIteratableIndex();
        return MoveAndIterate.createArray(moves, iteratableStartIndex, direction, enemyFlg);

    }
    @Override
    public String toString(){
        String output = "";
        if (enemyFlg) {
            output += "敵";
        }else{
            output += "こっち";
        }
        if (becomeFlg) {
            output += "成り";
        }else{
            output += "不成";
        }
        output += KOMA_TYPE.toString();
        output += direction.toString();
        output += position.toString ();
        return output;
    }

    void die() {
        EventBus.getDefault ().unregister ( this );
        //ビューの削除
        LinearLayout parentll = (LinearLayout) komaImage.getParent();
        parentll.removeView(komaImage);

        //位置と駒リストからの削除
        KomaAndLocateList.getInstance().remove(this);

        //相手方の駒台のカウントを増やす
        Komadai.getKomadai(!enemyFlg).changeCount(this.KOMA_TYPE,1);
        //二歩リストからの削除
        if (KOMA_TYPE == FU) {
            NifuJudge.getInstance().fuRemove(this);
        }
    }
    static View rotationView(View view,directions direction,boolean enemyFlg){

        view.setRotation(direction.getAngle(enemyFlg));

        return view;
    }
    void changeDirection (directions direction) {
        this.direction = direction;
        if (this.position != null && this.tapState == STATE_SHOWING_MOVABLE) {
            onTapped(this.position);
        }

        //方向によって画像の向きを変更
        rotationView(this.komaImage,this.direction,this.enemyFlg);
        Sashite sashite = new Sashite(KOMA_TYPE,position,position,direction,becomeFlg);
        MainActivity.getInstance().getKifu().add(sashite);

    }

    public ImageView getKomaImage() {
        return komaImage;
    }

    boolean isIterate(){
        return (
                this.KOMA_TYPE == HISHA ||
                        this.KOMA_TYPE == KAKU ||
                        (this.KOMA_TYPE == KYOUSHA && !(this.becomeFlg) ) ||
                        this.KOMA_TYPE == UMA ||
                        this.KOMA_TYPE == RYU );
    }

    void addMovableLocate(Position position) {
        if (this.KOMA_TYPE == GYOKU) {
            Log.d("added",position.toString ());
        }
        if (movableLocates==null) {
            movableLocates = new ArrayList<>();
        }
        movableLocates.add ( position );
    }
    void addAllMovableLocates(List<Position> locates) {
        if (movableLocates==null) {
            movableLocates = new ArrayList<>();
        }
        movableLocates.addAll ( locates );
    }
    private void resetMovableLocate() {
        this.movableLocates = null;
    }

    boolean isConstraint() {
        return movableLocates!=null;
    }

    public List <Position> getMovableLocates () {
        return movableLocates;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }

        Koma koma = (Koma)obj;

        return this.position.equals ( koma.position );
    }

    @Override
    public int hashCode () {
        int result = 17;
        result = 31 * result + position.getRow ();
        result = 31 * result + position.getColumn ();
        return result;
    }
}
