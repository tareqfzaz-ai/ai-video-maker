package com.aivideomaker.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class CreateVideoActivity extends Activity {
    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_create_video);

        Spinner duration = findViewById(R.id.duration);
        Spinner style = findViewById(R.id.style);

        duration.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"5 ثواني", "10 ثواني", "15 ثانية"}));

        style.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"سينمائي", "واقعي", "كرتوني", "إعلاني", "سينمائي عمودي"}));

        findViewById(R.id.createNow).setOnClickListener(v ->
            Toast.makeText(this, "تم اختيار إعدادات الفيديو. الربط مع مولّد الفيديو قيد التجهيز.", Toast.LENGTH_LONG).show()
        );
    }
}
