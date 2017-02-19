package com.example.yokoshogi;

import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.example.yokoshogi.event.BoardTapEvent;
import com.example.yokoshogi.event.TurnChangeEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Locale;

class RestTimer extends CountDownTimer {

    static final long DEFAULT_TIME = 10 * 60 * 1000;

    private TextView textView;
    private boolean enemyFlg;
    private float
            restTime,
            beforeTime;
    RestTimer (float time,TextView textView,boolean enemy) {
        super((long)time, 50);
        EventBus.getDefault().register(this);
        this.restTime = time;
        this.beforeTime = time;
        this.textView = textView;
        this.enemyFlg = enemy;
        if (isEnemy ()) {
            this.textView.setRotation ( 180.0f );
        }
        this.textView.setTextColor(ContextCompat.getColor(MainActivity.getInstance(),R.color.transparentWhite));
    }


    @Subscribe
    public void onTurnChangeEvent(TurnChangeEvent event) {
        if (this.enemyFlg == event.getBeforeTurn ()) {
            //文字を灰色にする
            this.textView.setTextColor(ContextCompat.getColor(MainActivity.getInstance(),R.color.transparentLiteGray));
            //停止する
            this.cancel ();
        }else{
            //Subから抜ける
            EventBus.getDefault().unregister(this);
            //新しく生成する
            MainActivity.getInstance().setRestTimer(new RestTimer(restTime,textView,!(event.getBeforeTurn ())));
        }
    }



    @Override
    public void onFinish() {
        // 時間切れ負けの表示

    }

    @Override
    public void onTick(long millisUntilFinished) {
        //残り時間の保持
        restTime = millisUntilFinished;
        // インターバル(countDownInterval)毎に呼ばれる
        int sec = (int)millisUntilFinished / 1000;
        textView.setText(String.valueOf(sec / 60 + ":" + String.format(Locale.US,"%02d",sec % 60)));
        if (sec == 0) {
            MainActivity.getInstance ().showResultDialog ( isEnemy () );
        }
    }

    static float getOneTime(boolean enemyFlg) {
        RestTimer restTimer;
        if (enemyFlg) {
             restTimer = MainActivity.getInstance().getEnemyRestTimer();
        }else{
            restTimer = MainActivity.getInstance ().getMyRestTimer ();
        }
        return restTimer.beforeTime - restTimer.restTime;

    }

    boolean isEnemy () {
        return enemyFlg;
    }
}
