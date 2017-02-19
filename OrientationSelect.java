package com.example.yokoshogi;

import android.media.Image;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;

import com.example.yokoshogi.event.BoardTapEvent;
import com.example.yokoshogi.event.DirectionChangeEvent;
import com.example.yokoshogi.event.KomadaiTapEvent;
import com.example.yokoshogi.event.TurnChangeEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


import static com.example.yokoshogi.Koma.Types.EMP;
import static com.example.yokoshogi.Koma.directions.FORWARD;
import static com.example.yokoshogi.Koma.directions.LEFT;
import static com.example.yokoshogi.Koma.directions.RIGHT;

class OrientationSelect {
    private ImageView currentKomaIV;
    private ImageView arrow = null;
    private Koma.Types komaType = EMP;
    private Koma.directions selectedOrientation = FORWARD;
    private boolean komaIsBecame = false;
    private boolean enemyFlg;


    OrientationSelect(boolean enemyFlg) {
        EventBus.getDefault ().register ( this );
        MainActivity mainActivity =  MainActivity.getInstance();
        this.enemyFlg = enemyFlg;
        if (enemyFlg) {
            currentKomaIV = (ImageView) mainActivity.findViewById(R.id.current_koma_enemy);
        } else {
            currentKomaIV = (ImageView) mainActivity.findViewById(R.id.current_koma_me);
        }
    }

    static OrientationSelect getInstance(Boolean enemyFlg) {
        if (enemyFlg) {
            return MainActivity.getInstance().getOrientationSelectEnemy();
        }else{
            return MainActivity.getInstance().getOrientationSelectMe();
        }
    }
    @Subscribe
    public void onBoardTapEvent ( BoardTapEvent event) {
        if (this.enemyFlg != event.getTurn ()) {
            return;
        }
        this.initialize ();
        Koma koma = event.getKoma ();
        if (koma==null) {
            return;
        }
        if (koma.getEnemyFlg () != event.getTurn ()) {
            return;
        }
        if (event.isEqualToPre ()) {
            return;
        }
        setCurrentKoma ( koma.KOMA_TYPE, koma.getDirection (), koma.getBecomeFlg () );
    }
    @Subscribe
    public void onKomadaiTapped ( KomadaiTapEvent event) {
        if (event.getEnemyFlg () != enemyFlg) {
            return;
        }
        initialize ();
        if (event.getPreType () != event.getNextType ()) {
            setCurrentKoma ( event.getNextType () , FORWARD,false);
        }
    }
    @Subscribe
    public void onTurnChangeEvent( TurnChangeEvent event) {
        if (event.getBeforeTurn () == this.enemyFlg) {
            initialize ();
        }
    }

    //選択されている駒を表示するメソッド
    private void setCurrentKoma (Koma.Types komaType,Koma.directions orientation,boolean becomeFlg) {
        this.komaType = komaType;
        this.selectedOrientation = orientation;
        komaIsBecame = becomeFlg;
        currentKomaIV.setVisibility(View.VISIBLE);
        //サンプルの駒のセット
        currentKomaIV.setImageResource(komaType.getImageID(MainActivity.getEarlierFlg () ^ enemyFlg,becomeFlg));
        //方向に応じて回転
        Koma.rotationView(currentKomaIV,orientation,enemyFlg);

        MainActivity mainActivity = MainActivity.getInstance();
        arrow = (ImageView) mainActivity.findViewById ( orientation.getIVId ( enemyFlg ) );
        arrow.setColorFilter(ContextCompat.getColor(mainActivity,R.color.transparentRed));
    }

    //方向ボタンがタップされたとき
    void onOrientationSelected (View view){
        //王手されていて、かつ盤上の駒を選択している場合、回転では王取りを防げないため
        Ban ban = Ban.getInstance ();
        if ( ban.isOuted () && ban.getState () == Ban.SHOWING_MOVABLE) {
            return;
        }
        if ( komaType == EMP || !(komaType.isRotatable()) ) {
            return;
        }
        Koma.directions direction = null;
        switch (view.getId()) {
            case R.id.arrow_forward_me:
            case R.id.arrow_forward_enemy:
                direction = FORWARD;
                break;
            case R.id.arrow_left_me:
            case R.id.arrow_left_enemy:
                direction = LEFT;
                break;
            case R.id.arrow_right_me:
            case R.id.arrow_right_enemy:
                direction = RIGHT;
                break;
        }
        if (selectedOrientation == direction) {
            return;
        }
        if (arrow != null) {
            arrow.setColorFilter(ContextCompat.getColor(MainActivity.getInstance(), R.color.transparentDeepGray));
        }
        //サンプルの駒の回転と矢印の再描画
        this.setCurrentKoma(komaType, direction, komaIsBecame );


        EventBus.getDefault ().post (new DirectionChangeEvent(this.enemyFlg, direction) );
        //何か選択していたら


        selectedOrientation = direction;

    }

    private void initialize() {
        currentKomaIV.setVisibility(View.INVISIBLE);
        if (arrow != null) {
            arrow.setColorFilter(ContextCompat.getColor(MainActivity.getInstance(),R.color.transparentDeepGray));
        }
        arrow = null;
        komaType = EMP;
    }

    Koma.directions getSelectedOrientation() {
        return selectedOrientation;
    }
}
