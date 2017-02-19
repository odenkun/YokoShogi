package com.example.yokoshogi.dialog;

import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.yokoshogi.MainActivity;
import com.example.yokoshogi.R;


class ShowResult {
    static View getView (boolean enemyFlg) {
        MainActivity mainActivity = MainActivity.getInstance ();
        RelativeLayout overLayout = (RelativeLayout) mainActivity.findViewById( R.id.overLayout);
        overLayout.setBackgroundColor(ContextCompat.getColor( mainActivity,R.color.transparentMoreBlack));
        ImageView resultIV = new ImageView(mainActivity);
        if (enemyFlg) {
            resultIV.setImageResource(R.drawable.lose_animation);
        } else {
            resultIV.setImageResource(R.drawable.win_animation);
        }
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        resultIV.setLayoutParams(layoutParams);
        TransitionDrawable animationDrawable = (TransitionDrawable) resultIV.getDrawable();
        animationDrawable.startTransition(1500);
        final Handler handler = new Handler ();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MainActivity.getInstance ().showInquireRetryDialog();
            }
        }, 2500);
        return resultIV;
    }
}
