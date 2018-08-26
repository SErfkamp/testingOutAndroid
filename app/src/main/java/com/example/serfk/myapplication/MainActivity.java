package com.example.serfk.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import com.example.serfk.myapplication.IVISActivity;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
    }

    public void startBaseline(View view) {
        Intent intent = new Intent(this, IVISActivity.class);
        intent.putExtra("lockingMode",0);
        intent.putExtra("lockingDuration",0);
        startActivity(intent);
    }

    public void startShortLockings(View view) {
        Intent intent = new Intent(this, IVISActivity.class);
        intent.putExtra("lockingMode",1);
        intent.putExtra("lockingDuration",1500);
        startActivity(intent);
    }

    public void startLongLockings(View view) {
        Intent intent = new Intent(this, IVISActivity.class);
        intent.putExtra("lockingMode",2);
        intent.putExtra("lockingDuration",2500);
        startActivity(intent);
    }

}

