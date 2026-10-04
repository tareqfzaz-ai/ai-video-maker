package com.aivideomaker.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class CreateVideoActivity extends Activity {
    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_create_video);

        findViewById(R.id.createNow).setOnClickListener(v ->
            Toast.makeText(this, "تم اختيار إعدادات الفيديو. الربط مع مولّد الفيديو قيد التجهيز.", Toast.LENGTH_LONG).show()
        );
    }
}
