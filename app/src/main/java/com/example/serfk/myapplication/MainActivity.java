package com.example.serfk.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import com.example.serfk.myapplication.IVISActivity;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
    }

    public void startBaseline(View view) {
        startActivity(createActivity(0));

    }

    public void startInteractionLockings(View view) {
        startActivity(createActivity(1));

    }

    public void startTrackLockings(View view) {
        startActivity(createActivity(2));

    }

    public void startDriveLockings(View view) {
        startActivity(createActivity(3));

    }

    private Intent createActivity(int lockingMode) {
        int lockingDuration = Integer.parseInt(((EditText) findViewById(R.id.input_locking_duration)).getText().toString());
        int resetInteractionTime = Integer.parseInt(((EditText) findViewById(R.id.input_reset_action)).getText().toString());
        int interactionsForLock = Integer.parseInt(((EditText) findViewById(R.id.input_actions)).getText().toString());
        int lockingAfter = Integer.parseInt(((EditText) findViewById(R.id.input_locking_after)).getText().toString());

        Intent intent = new Intent(this, IVISActivity.class);
        intent.putExtra("lockingMode", lockingMode);
        intent.putExtra("lockingDuration", lockingDuration);
        intent.putExtra("resetInteractionTime", resetInteractionTime);
        intent.putExtra("interactionsForLock", interactionsForLock);
        intent.putExtra("lockingAfter", lockingAfter);

        return intent;
    }
}

