package com.example.yokoshogi.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.example.yokoshogi.MainActivity;

public class InquireRetryDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("再戦しますか？")

                .setPositiveButton("はい", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // クリック時の処理
                        MainActivity.getInstance ().resetDialog ();
                        dialog.dismiss();
                    }
                })

                .setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // クリック時の処理

                        dialog.dismiss();
                    }
                });
        this.setCancelable(false);
        return builder.create();
    }
}
