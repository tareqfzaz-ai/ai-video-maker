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

public class CreateVideoActivity extends Activity {
    private static final String SUPABASE_URL = "https://zwsmdwvyulwidbgluyae.supabase.co";
    private static final String SUPABASE_KEY = "sb_publishable_PVN3er6Xj5EsrMp96N9x1w_yuSmjzup";
    private EditText prompt;
    private TextView status;
    private VideoView video;
    private String accessToken;
    private final Handler handler = new Handler();

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_create_video);
        prompt=findViewById(R.id.prompt);
        status=findViewById(R.id.status);
        video=findViewById(R.id.video);
        Spinner duration=findViewById(R.id.duration);
        Spinner style=findViewById(R.id.style);

        duration.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"5 ثواني","10 ثواني","15 ثانية"}));
        style.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"سينمائي","واقعي","كرتوني","إعلاني","سينمائي عمودي"}));

        findViewById(R.id.createNow).setOnClickListener(v -> {
            String p=prompt.getText().toString().trim();
            if(p.isEmpty()){prompt.setError("اكتب وصف الفيديو أولاً"); return;}
            int d=duration.getSelectedItemPosition()==0?5:(duration.getSelectedItemPosition()==1?10:15);
            String s=style.getSelectedItem().toString();
            findViewById(R.id.createNow).setEnabled(false);
            status.setText("⏳ جاري الاتصال بالمولّد...");
            new Thread(() -> createVideo(p,d,s)).start();
        });
    }

    private void createVideo(String p,int d,String s) {
        try {
            if(accessToken==null) accessToken=anonymousSignIn();
            JSONObject body=new JSONObject();
            body.put("prompt",p); body.put("duration_seconds",d); body.put("style",s); body.put("aspect_ratio","9:16");
            JSONObject result=post(SUPABASE_URL+"/functions/v1/generate-video",body,accessToken);
            if(result.has("error")) throw new Exception(result.optString("error"));
            String generationId=result.optString("generation_id","");
            if(generationId.isEmpty()) throw new Exception("لم يتم استلام رقم عملية التوليد");
            runOnUiThread(()->status.setText("🎬 تم بدء التوليد... انتظر قليلاً"));
            poll(generationId,0);
        } catch(Exception e) {
            runOnUiThread(()->{status.setText("❌ "+e.getMessage()); findViewById(R.id.createNow).setEnabled(true);});
        }
    }

    private void poll(String id,int count) {
        if(count>60){runOnUiThread(()->{status.setText("⏳ التوليد ما زال يعمل، جرّب الانتظار ثم افتح النتيجة.");findViewById(R.id.createNow).setEnabled(true);});return;}
        handler.postDelayed(() -> new Thread(() -> {
            try {
                JSONObject b=new JSONObject(); b.put("action","status"); b.put("generation_id",id);
                JSONObject r=post(SUPABASE_URL+"/functions/v1/generate-video",b,accessToken);
                String url=findVideoUrl(r);
                String st=r.optString("status",r.optString("state",""));
                if(url!=null){
                    runOnUiThread(()->{status.setText("✅ تم إنشاء الفيديو");video.setVideoURI(Uri.parse(url));video.setVisibility(View.VISIBLE);video.start();findViewById(R.id.createNow).setEnabled(true);});
                } else if(st.toLowerCase().contains("failed")||st.toLowerCase().contains("error")){
                    throw new Exception(r.optString("failure_reason",r.optString("error","فشل إنشاء الفيديو")));
                } else {
                    runOnUiThread(()->status.setText("⏳ جاري إنشاء الفيديو..."));
                    poll(id,count+1);
                }
            } catch(Exception e){runOnUiThread(()->{status.setText("❌ "+e.getMessage());findViewById(R.id.createNow).setEnabled(true);});}
        }).start(),5000);
    }

    private String findVideoUrl(JSONObject o) {
        String[] keys={"content_url","video_url","output_url","signed_url"};
        for(String k:keys) if(o.has(k)&&!o.optString(k).isEmpty()) return o.optString(k);
        for(String k:new String[]{"video","content","result"}) {
            JSONObject x=o.optJSONObject(k); if(x!=null){String u=findVideoUrl(x);if(u!=null)return u;}
        }
        return null;
    }

    private String anonymousSignIn() throws Exception {
        JSONObject r=post(SUPABASE_URL+"/auth/v1/signup",new JSONObject(),null);
        String token=r.optString("access_token","");
        if(token.isEmpty()) throw new Exception("فعّل تسجيل الدخول المجهول في Supabase أولاً");
        return token;
    }

    private JSONObject post(String url,JSONObject body,String token) throws Exception {
        HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();
        c.setRequestMethod("POST"); c.setDoOutput(true); c.setConnectTimeout(20000); c.setReadTimeout(60000);
        c.setRequestProperty("apikey",SUPABASE_KEY); c.setRequestProperty("Content-Type","application/json");
        if(token!=null)c.setRequestProperty("Authorization","Bearer "+token);
        try(OutputStream os=c.getOutputStream()){os.write(body.toString().getBytes(StandardCharsets.UTF_8));}
        InputStream is=c.getResponseCode()<400?c.getInputStream():c.getErrorStream();
        String text; try(BufferedReader br=new BufferedReader(new InputStreamReader(is,StandardCharsets.UTF_8))){StringBuilder sb=new StringBuilder();String line;while((line=br.readLine())!=null)sb.append(line);text=sb.toString();}
        JSONObject out=new JSONObject(text);
        if(c.getResponseCode()>=400) throw new Exception(out.optString("msg",out.optString("error_description",out.optString("error","HTTP "+c.getResponseCode()))));
        return out;
    }
}
