package com.example.yokoshogi.dialog;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.yokoshogi.Ban;
import com.example.yokoshogi.Koma;
import com.example.yokoshogi.MainActivity;
import com.example.yokoshogi.Position;
import com.example.yokoshogi.R;
import com.example.yokoshogi.logger.Sashite;

class InquireBecomeDialog {

    static LinearLayout getView( MainActivity mainActivity, Koma koma, Position befPosition ) {
        LinearLayout linearLayout = new LinearLayout( mainActivity );
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setPadding(5,5,3,5);
        linearLayout.setBackgroundResource( R.drawable.become_back);


        final int komaWidth = koma.getKomaImage().getMeasuredWidth();
        final int komaHeight = koma.getKomaImage().getMeasuredHeight();

        int[] locationArray = new int[2];
        koma.getKomaImage().getLocationInWindow(locationArray);
        int x;
        if (koma.getEnemyFlg()) {
            x = locationArray[0] - (int)(komaWidth * 1.9);
        }else{
            x = locationArray[0] - (int)(komaWidth * 0.9);
        }

        RelativeLayout relativeLayout = (RelativeLayout) mainActivity.findViewById(R.id.overLayout);
        if (x < 0) {
            x = 0;
        }else if (x > relativeLayout.getMeasuredWidth() - (int)(komaWidth * 2.6) - 10){
            x = relativeLayout.getMeasuredWidth() - (int)(komaWidth * 2.6) - 10;
        }
        linearLayout.setX(x);

        if (koma.getEnemyFlg()) {
            linearLayout.setY(locationArray[1]  - komaHeight / 2 - (int)(komaHeight * 2.6));
            linearLayout.setRotation(180.0f);
        }else{
            linearLayout.setY(locationArray[1] + komaHeight / 2);
        }
        int[][] drawableIDArray = {
                {
                        R.drawable.become_back,
                        koma.KOMA_TYPE.getImageID(MainActivity.getEarlierFlg () ^ koma.getEnemyFlg(),true)
                },
                {
                        R.drawable.not_become_back,
                        koma.KOMA_TYPE.getImageID(MainActivity.getEarlierFlg () ^  koma.getEnemyFlg(),false)
                }
        };

        for (int[] drawableIDs:drawableIDArray) {

            LinearLayout selectableKomall = new LinearLayout ( mainActivity );
            selectableKomall.setBackgroundResource ( drawableIDs[ 0 ] );

            selectableKomall.setOnClickListener ( new View.OnClickListener () {
                Koma koma = null;
                Boolean become = null;
                Position befPosition = null;

                View.OnClickListener setKoma ( Koma koma, Boolean become ,Position befPosition) {
                    this.koma = koma;
                    this.become = become;
                    this.befPosition = befPosition;
                    return this;
                }

                @Override
                public void onClick ( View v ) {
                    MainActivity.getInstance ().resetDialog ();
                    koma.changeBecomeFlg ( become);
                    Sashite sashite = new Sashite ( koma.KOMA_TYPE,befPosition, koma.getPosition ().copy (), koma.getDirection (), become );
                    MainActivity.getInstance ().getKifu ().add ( sashite );
                    Ban.getInstance().tsumiJudge();

                }
            }.setKoma ( koma, drawableIDs[ 0 ] == R.drawable.become_back, befPosition ));

            ImageView selectableKoma = new ImageView ( mainActivity );
            selectableKoma.setImageResource ( drawableIDs[ 1 ] );
            selectableKomall.addView ( selectableKoma );

            linearLayout.addView ( selectableKomall );

            LinearLayout.LayoutParams selectableKomalllp = new LinearLayout.LayoutParams ( (int) ( komaWidth * 1.3 ), (int) ( komaHeight * 1.3 ) );
            selectableKomalllp.rightMargin = 2;
            selectableKomall.setLayoutParams ( selectableKomalllp );
        }
        return linearLayout;
    }
}
