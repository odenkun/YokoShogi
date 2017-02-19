package com.example.yokoshogi.dialog;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.RelativeLayout;

import com.example.yokoshogi.InitialActivity;
import com.example.yokoshogi.Koma;
import com.example.yokoshogi.MainActivity;
import com.example.yokoshogi.Position;
import com.example.yokoshogi.R;
import com.example.yokoshogi.event.MatchFinishEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class SuperDialog {
    public enum TYPE {
        BECOME,
        RESULT,
        OUTE
    }
    public boolean exist = false;
    ProgressDialog dialog;

    public SuperDialog (MainActivity mainActivity) {
        showProgressDialog ( mainActivity );
    }

    public void showProgressDialog ( MainActivity mainActivity) {
        EventBus.getDefault ().register ( this );
        dialog = new ProgressDialog(mainActivity);
        dialog.setTitle("マッチング中");
        dialog.setMessage("しばらくお待ち下さい...");
        dialog.setProgressStyle( ProgressDialog.STYLE_SPINNER);
        dialog.setMax(10);
        dialog.setCancelable(true);
        dialog.show();
    }
    @Subscribe
    public void dismissProgressDialog( MatchFinishEvent event) {
        if (dialog != null ) {
            dialog.dismiss ();
            dialog =null;
        }
    }

    public void showBecomeDialog ( Koma koma, Position befPos ) {
        if ( ! (exist) ) {
            exist = true;
            View view = InquireBecomeDialog.getView ( MainActivity.getInstance (), koma,befPos );
            addView ( view );
        }
    }

    public void showResultDialog (boolean enemyFlg) {
        if ( ! (exist) ) {
            exist = true;
            View view = ShowResult.getView ( enemyFlg );
            addView ( view );
        }
    }
    public void showOuteDialog ( ) {

        final View view = ShowOute.getView ( MainActivity.getInstance () );
        addView ( view );
        if (view == null) {
           Log.d("okac","yo");
        }
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat( view, "alpha", 1f, 0f );
        objectAnimator.setDuration( 800 );
        objectAnimator.setInterpolator ( new AccelerateInterpolator() );

        objectAnimator.addListener ( new Animator.AnimatorListener () {
            @Override
            public void onAnimationEnd ( Animator animation ) {
                RelativeLayout parent = (RelativeLayout) view.getParent ();
                    parent.removeView ( view );

            }
            @Override
            public void onAnimationStart ( Animator animation ) {}
            @Override
            public void onAnimationCancel ( Animator animation ) {}
            @Override
            public void onAnimationRepeat ( Animator animation ) {}
        } );
        objectAnimator.start();
    }
    private void addView (View view ) {
        RelativeLayout relativeLayout  = (RelativeLayout) MainActivity.getInstance ().findViewById ( R.id.overLayout );
        relativeLayout.addView ( view );
    }
   public  void dismiss () {
        exist = false;
        RelativeLayout relativeLayout = (RelativeLayout) MainActivity.getInstance ().findViewById ( R.id.overLayout );
        relativeLayout.removeAllViews ();
    }
}
