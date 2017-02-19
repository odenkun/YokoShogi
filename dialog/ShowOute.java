package com.example.yokoshogi.dialog;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.yokoshogi.R;

class ShowOute {
    private ShowOute() {

    }
    static View getView (Context context) {
        ImageView outeIV = new ImageView(context);
        outeIV.setImageResource( R.drawable.oute);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        outeIV.setLayoutParams(layoutParams);
        return outeIV;
    }
}
