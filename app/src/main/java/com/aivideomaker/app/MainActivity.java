package com.aivideomaker.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        findViewById(R.id.generate).setOnClickListener(v ->
            startActivity(new Intent(this, CreateVideoActivity.class))
        );
    }
}
