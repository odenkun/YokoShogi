package com.example.yokoshogi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class InitialActivity extends AppCompatActivity {

    @Override
    protected void onCreate ( Bundle savedInstanceState ) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_initial );
        View view = findViewById ( R.id.button );
        view.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick ( View v ) {
                intent ();
            }
        } );
    }
    void intent () {
        Intent intent = new Intent (InitialActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
