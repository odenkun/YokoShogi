package com.example.yokoshogi;

import android.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.yokoshogi.dialog.InquireRetryDialog;
import com.example.yokoshogi.dialog.SuperDialog;
import com.example.yokoshogi.logger.Kifu;
import com.example.yokoshogi.network.SocketClient;

import io.socket.client.Socket;

public class MainActivity extends AppCompatActivity {

    private static MainActivity mainActivity;

    private Ban ban = null;
    private NifuJudge nifuJudge;
    private Kifu kifu;
    private KomaAndLocateList komaAndLocateList;
    private SuperDialog superDialog;

    private Komadai
            myKomadai,
            enemyKomadai;

    private OrientationSelect
            orientationSelectEnemy,
            orientationSelectMe;

    private RestTimer
            myRestTimer,
            enemyRestTimer;

    private SocketClient socketClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;
        if (superDialog == null) {
            superDialog = new SuperDialog (this);
        }
        if (kifu == null) {
            kifu = new Kifu();
        }
        if (nifuJudge == null) {
            nifuJudge = new NifuJudge();
        }
        if (komaAndLocateList == null) {
            komaAndLocateList = new KomaAndLocateList();
        }
        if (socketClient == null ) {
            socketClient = new SocketClient ();
        }
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
//        boolean turn = socketClient.getEarlierFlg ();
        boolean turn = false;
        if (ban == null) {
            ban = new Ban((GridLayout)findViewById(R.id.board),turn);
        }
        if (orientationSelectEnemy == null) {
            orientationSelectEnemy = new OrientationSelect(true);
        }
        if (orientationSelectMe == null) {
            orientationSelectMe = new OrientationSelect(false);
        }
        if (myKomadai == null) {
            myKomadai = new Komadai(this,turn, false, (LinearLayout) findViewById(R.id.myKomadai));
        }
        if (enemyKomadai == null) {
            enemyKomadai = new Komadai(this,turn, true, (LinearLayout) findViewById(R.id.enemyKomadai));
        }



        if (myRestTimer == null) {
            myRestTimer = new RestTimer(RestTimer.DEFAULT_TIME,(TextView) findViewById(R.id.meTimer),false);
        }
        if (enemyRestTimer == null) {
            enemyRestTimer = new RestTimer(RestTimer.DEFAULT_TIME,(TextView)findViewById(R.id.enemyTimer),true);
        }
        if (turn) {
            enemyRestTimer.start();
        }else{
            myRestTimer.start();
        }

    }

    public void showInquireRetryDialog () {
        DialogFragment newFragment = new InquireRetryDialog ();
        newFragment.show(getFragmentManager(), "InquireRetry");
    }

    boolean dialogExist () {
        return superDialog.exist;
    }

    public void showBecomeDialog (Koma koma,Position befPos) {
        superDialog.showBecomeDialog ( koma,befPos );
    }

    public void showResultDialog (boolean enemyFlg) {
        superDialog.showResultDialog ( enemyFlg );
    }

    public void showOuteDialog () {
        superDialog.showOuteDialog();
    }

   public  void resetDialog () {
        superDialog.dismiss ();
    }

    public void onOrientationSelected (View view) {

        if (view.getParent() == findViewById(R.id.orientation_select_enemy)) {
            orientationSelectEnemy.onOrientationSelected(view);
        }else{
            orientationSelectMe.onOrientationSelected(view);
        }
    }
    public static MainActivity getInstance() {
        return mainActivity;
    }

    public Komadai getMyKomadai () {
        return myKomadai;
    }

    public Komadai getEnemyKomadai () {
        return enemyKomadai;
    }

    public Ban getBan () {
        return ban;
    }

    public OrientationSelect getOrientationSelectEnemy () {
        return orientationSelectEnemy;
    }

    public OrientationSelect getOrientationSelectMe () {
        return orientationSelectMe;
    }

    public RestTimer getMyRestTimer () {
        return myRestTimer;
    }

    public RestTimer getEnemyRestTimer () {
        return enemyRestTimer;
    }

    public void setMyRestTimer (RestTimer myRestTimer) {
        this.myRestTimer = myRestTimer;
    }

    public void setEnemyRestTimer (RestTimer enemyRestTimer) {
        this.enemyRestTimer = enemyRestTimer;
    }
    void setRestTimer (RestTimer restTimer) {
        if (restTimer.isEnemy()) {
            setEnemyRestTimer(restTimer);
        }else{
            setMyRestTimer(restTimer);
        }
        restTimer.start();
    }

    public Kifu getKifu () {
        return kifu;
    }

    public static boolean getEarlierFlg () {
//        SocketClient client = MainActivity.getInstance ().socketClient;
//        if (client != null && client.isConnected ()) {
//            return client.getEarlierFlg ();
//        }
//        Log.e("shogi","failed to getEarlierFlg");
        return false;
    }

    @Override
    protected void onPause () {
        super.onPause ();
        Log.d("MainActivity","onPause called.");
        if (socketClient != null && socketClient.isConnected ()) {
            socketClient.disconnect();
            Log.d("MainActivity","socket disconnect called.");
        }
    }
}
