package com.example.yokoshogi;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.yokoshogi.event.BoardTapEvent;
import com.example.yokoshogi.event.KomadaiTapEvent;
import com.example.yokoshogi.event.TurnChangeEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.EnumMap;

import static com.example.yokoshogi.Koma.Types.EMP;
import static com.example.yokoshogi.Koma.Types.FU;

class Komadai {

    private LinearLayout llKomadai;
    private FrameLayout[] eachKomall = new FrameLayout[Koma.Types.values().length];
    private TextView[] eachKomaCount = new TextView[Koma.Types.values().length];
    private Koma.Types currentSelectedKomaType = EMP;
    private boolean enemyFlg;
    private boolean earlierFlg;
    private ArrayList<Position> puttableLocatesExceptFU = new ArrayList <> (  ) ;
    private ArrayList<Position> puttableLocatesFUForward = new ArrayList <> (  );
    private ArrayList<Position> puttableLocatesFUHorizontal = new ArrayList <> (  );
    private EnumMap<Koma.Types, Integer> komaCount = new EnumMap <> (Koma.Types.class) ;

    Komadai( final Context context, final boolean earlierFlg, boolean enemyFlg, LinearLayout linearLayout) {

        EventBus.getDefault().register(this);

        llKomadai = linearLayout;
        this.enemyFlg = enemyFlg;
        this.earlierFlg = earlierFlg;
        if ( enemyFlg) {
            llKomadai.setRotation ( 180.0f );
        }
        //横と縦の長さを適用するためのparamを宣言
        LinearLayout.LayoutParams param =new LinearLayout.LayoutParams(
                Ban.getInstance().getBanGrid().getWidth() / Ban.COLUMN_COUNT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        for (Koma.Types type : Koma.Types.getPrimitives ()) {
            komaCount.put ( type, 0 );
            FrameLayout frameLayout = new FrameLayout(context);
            //駒の画像
            final ImageView komaImage = new ImageView(context);
            komaImage.setImageResource(type.getImageID(earlierFlg ^ enemyFlg,false));
            komaImage.setPadding(0,0,0,7);
            //Koma.rotationView(komaImage,orientation,enemyFlg);
            frameLayout.addView(komaImage);

            //駒の個数
            TextView textView = new TextView(context);
            textView.setText("0");
            textView.setTextColor(Color.WHITE);
            textView.setTextSize(23.0f);
            textView.setAlpha(0.9f);
            textView.setGravity(Gravity.END | Gravity.BOTTOM);
            textView.setPadding(0,0,5,0);
            textView.setVisibility(View.INVISIBLE);

            frameLayout.addView(textView);
            frameLayout.setLayoutParams(param);
            frameLayout.setBackground(MainActivity.getInstance().getDrawable(R.drawable.waku_masu));
            frameLayout.setVisibility(View.GONE);

            frameLayout.setOnClickListener(new View.OnClickListener() {
                Koma.Types komaType;
                boolean enemyFlg;
                View.OnClickListener setLocation (Koma.Types komaType,boolean enemyFlg) {
                    this.komaType = komaType;
                    this.enemyFlg = enemyFlg;
                    return this;
                }
                public void onClick(View view) {
                    //ダイアログを表示している場合、何もしない
                    if ((MainActivity.getInstance ().dialogExist ())) {
                        return;
                    }
                    Log.d("駒台でタップされたのは",komaType.toString ());
                    Log.d("aaa",String.valueOf ( earlierFlg ) + String.valueOf ( enemyFlg ));
                    onTapped ( komaType );
                    Ban.getInstance().highlightMasu(view);
                }
            }.setLocation(type , enemyFlg));
            eachKomall[type.getNumber()] = frameLayout;
            eachKomaCount[type.getNumber()] = textView;
            if (enemyFlg) {
                llKomadai.addView(frameLayout,0);
            }else{
                llKomadai.addView(frameLayout);
            }
        }
    }
    static Komadai getKomadai (boolean enemyFlg) {
//        Log.d("a",String.valueOf (earlierFlg) + String.valueOf ( enemyFlg ));
        if (!enemyFlg) {
            return MainActivity.getInstance().getMyKomadai();
        }else{
            return MainActivity.getInstance().getEnemyKomadai();
        }
    }
    void changeCount (Koma.Types komaType,int difference) {
        Log.d("b",komaType.toString ());
        int changedCount = getKomaCount(komaType) + difference;

        komaCount.remove ( komaType );
        komaCount.put ( komaType, changedCount );
        eachKomaCount[komaType.getNumber()].setText(String.valueOf(changedCount));
        switch (changedCount) {
            case 0:
                eachKomall[komaType.getNumber()].setVisibility(View.GONE);
                eachKomaCount[komaType.getNumber()].setVisibility(View.INVISIBLE);
                break;
            case 1:
                eachKomall[komaType.getNumber()].setVisibility(View.VISIBLE);
                eachKomaCount[komaType.getNumber()].setVisibility(View.INVISIBLE);
                break;
            default:
                eachKomall[komaType.getNumber()].setVisibility(View.VISIBLE);
                eachKomaCount[komaType.getNumber()].setVisibility(View.VISIBLE);
        }
    }
    private int getKomaCount (Koma.Types komaType) {
        return komaCount.get ( komaType );
    }
    private void onTapped (Koma.Types nextKomaType){

        if ( Ban.getInstance().getTurn() != this.getEnemyFlg()) {
            return;
        }

        EventBus.getDefault ().post ( new KomadaiTapEvent ( currentSelectedKomaType,nextKomaType, enemyFlg ) );
        suit(currentSelectedKomaType, false);
        if (currentSelectedKomaType == nextKomaType) {
            currentSelectedKomaType = EMP;
        }else{
            suit(nextKomaType,true);
            currentSelectedKomaType = nextKomaType;
        }
        Log.d("agrabea",String.valueOf (earlierFlg) + String.valueOf ( enemyFlg ));
        Log.d("currentKomaType",currentSelectedKomaType.toString ());
    }
    private void suit (Koma.Types komaType, boolean selectedFlg) {
        if (komaType == EMP ) {
            return;
        }
        int incrementalX = -7;
        int incrementalY = -7;
        int incrementalRotation = 2;

        if (getEnemyFlg()) {
            incrementalRotation = -incrementalRotation;
        }

        if (!(selectedFlg)) {
            incrementalX = -incrementalX;
            incrementalY = -incrementalY;
            incrementalRotation = -incrementalRotation;
        }

        ImageView imageView = (ImageView) this.eachKomall[komaType.getNumber()].getChildAt(0);
        imageView.setX(imageView.getX() + incrementalX);
        imageView.setY(imageView.getY() + incrementalY);
        imageView.setRotation(imageView.getRotation() + incrementalRotation);
    }

    Koma.Types getCurrentSelectedKomaType() {
        return currentSelectedKomaType;
    }

    boolean getEnemyFlg(){
        return enemyFlg;
    }

    boolean hasKomaExceptFu () {
        for (Koma.Types type: Koma.Types.getPrimitives ()) {
            if ( type == FU ) {
                continue;
            }
            if (getKomaCount ( type ) > 0) {
                return true;
            }
        }
        return false;
    }
    @Subscribe
    public void onBoardTapEvent ( BoardTapEvent event) {

        //選択されていた駒があれば
        if (currentSelectedKomaType != EMP) {
            //回転を戻す
            suit(currentSelectedKomaType,false);
            currentSelectedKomaType = EMP;
        }
    }
    @Subscribe
    public void onTurnChangeEvent ( TurnChangeEvent event ) {
        if (event.getBeforeTurn () == this.enemyFlg) {
            puttableLocatesExceptFU.clear ();
            puttableLocatesFUForward.clear ();
            puttableLocatesFUHorizontal.clear ();
        }
    }

    public void addPutLocExceptFU ( ArrayList<Position> positions) {
        puttableLocatesExceptFU.addAll ( positions );
    }

    public void addPutLocOnlyFU ( Position position, Koma.directions direction) {
        if (direction == Koma.directions.FORWARD) {
            puttableLocatesFUForward.add ( position );
        }else {
            puttableLocatesFUHorizontal.add ( position );
        }
    }

    ArrayList<Position> getPuttableLocates(Koma.Types type, Koma.directions direction) {

        if ( type == FU ) {
            if ( direction == Koma.directions.FORWARD ) {
                return new ArrayList <> ( puttableLocatesFUForward );
            }else {
                return new ArrayList <> ( puttableLocatesFUHorizontal );
            }
        } else {
            return new ArrayList <> ( puttableLocatesExceptFU );
        }
    }
    boolean hasFu () {
        return  getKomaCount ( FU ) > 0 ;
    }
}
