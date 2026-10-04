package com.aivideomaker.app;
import android.app.Activity; import android.os.Bundle; import android.widget.Toast;
public class MainActivity extends Activity { @Override public void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_main);findViewById(R.id.generate).setOnClickListener(v->Toast.makeText(this,"جاري إنشاء الفيديو...",Toast.LENGTH_SHORT).show());}}
