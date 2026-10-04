package com.aivideomaker.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.*;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import android.net.Uri;

public class CreateVideoActivity extends Activity {
    private static final String SUPABASE_URL = "https://zwsmdwvyulwidbgluyae.supabase.co";
    private static final String SUPABASE_KEY = "sb_publishable_PVN3er6Xj5EsrMp96N9x1w_yuSmjzup";
    private EditText prompt; private TextView status; private VideoView video;
    private final Handler handler = new Handler();

    @Override public void onCreate(Bundle b) {
        super.onCreate(b); setContentView(R.layout.activity_create_video);
        prompt=findViewById(R.id.prompt); status=findViewById(R.id.status); video=findViewById(R.id.video);
        Spinner duration=findViewById(R.id.duration), style=findViewById(R.id.style);
        duration.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"5 ثواني","10 ثواني","15 ثانية"}));
        style.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"سينمائي","واقعي","كرتوني","إعلاني","سينمائي عمودي"}));
        findViewById(R.id.createNow).setOnClickListener(v -> {
            String p=prompt.getText().toString().trim();
            if(p.isEmpty()){prompt.setError("اكتب وصف الفيديو أولاً"); return;}
            int d=duration.getSelectedItemPosition()==0?5:(duration.getSelectedItemPosition()==1?10:15);
            findViewById(R.id.createNow).setEnabled(false); status.setText("⏳ جاري الاتصال بالمولّد...");
            new Thread(() -> createVideo(p,d,style.getSelectedItem().toString())).start();
        });
    }
    private void createVideo(String p,int d,String s) {
        try {
            JSONObject body=new JSONObject(); body.put("prompt",p); body.put("duration_seconds",d); body.put("style",s); body.put("aspect_ratio","9:16");
            JSONObject result=post(SUPABASE_URL+"/functions/v1/generate-video",body);
            String id=result.optString("generation_id","");
            if(id.isEmpty()) throw new Exception(result.optString("error","لم يتم استلام رقم عملية التوليد"));
            runOnUiThread(()->status.setText("🎬 تم بدء التوليد... انتظر قليلاً")); poll(id,0);
        } catch(Exception e){runOnUiThread(()->{status.setText("❌ "+e.getMessage());findViewById(R.id.createNow).setEnabled(true);});}
    }
    private void poll(String id,int count) {
        if(count>60){runOnUiThread(()->{status.setText("⏳ التوليد ما زال يعمل.");findViewById(R.id.createNow).setEnabled(true);});return;}
        handler.postDelayed(() -> new Thread(() -> {
            try {
                JSONObject b=new JSONObject(); b.put("action","status"); b.put("generation_id",id);
                JSONObject r=post(SUPABASE_URL+"/functions/v1/generate-video",b); String url=findVideoUrl(r);
                String st=r.optString("status",r.optString("state",""));
                if(url!=null) runOnUiThread(()->{status.setText("✅ تم إنشاء الفيديو");video.setVideoURI(Uri.parse(url));video.setVisibility(View.VISIBLE);video.start();findViewById(R.id.createNow).setEnabled(true);});
                else if(st.toLowerCase().contains("failed")||st.toLowerCase().contains("error")) throw new Exception(r.optString("failure_reason",r.optString("error","فشل إنشاء الفيديو")));
                else {runOnUiThread(()->status.setText("⏳ جاري إنشاء الفيديو..."));poll(id,count+1);}
            } catch(Exception e){runOnUiThread(()->{status.setText("❌ "+e.getMessage());findViewById(R.id.createNow).setEnabled(true);});}
        }).start(),5000);
    }
    private String findVideoUrl(JSONObject o) {
        for(String k:new String[]{"content_url","video_url","output_url","signed_url"}) if(o.has(k)&&!o.optString(k).isEmpty()) return o.optString(k);
        for(String k:new String[]{"video","content","result"}) {JSONObject x=o.optJSONObject(k);if(x!=null){String u=findVideoUrl(x);if(u!=null)return u;}}
        return null;
    }
    private JSONObject post(String url,JSONObject body)throws Exception{
        HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection(); c.setRequestMethod("POST");c.setDoOutput(true);c.setConnectTimeout(20000);c.setReadTimeout(60000);
        c.setRequestProperty("apikey",SUPABASE_KEY);c.setRequestProperty("Content-Type","application/json");
        try(OutputStream os=c.getOutputStream()){os.write(body.toString().getBytes(StandardCharsets.UTF_8));}
        int code=c.getResponseCode();InputStream is=code<400?c.getInputStream():c.getErrorStream();StringBuilder sb=new StringBuilder();
        try(BufferedReader br=new BufferedReader(new InputStreamReader(is,StandardCharsets.UTF_8))){String line;while((line=br.readLine())!=null)sb.append(line);}
        JSONObject out=new JSONObject(sb.toString());if(code>=400)throw new Exception(out.optString("error",out.optString("msg","HTTP "+code)));return out;
    }
}
