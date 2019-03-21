package com.dean.mplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
import java.util.Objects;

public class MusicOnline extends AppCompatActivity {

/*
    OkHttpClient client = new OkHttpClient();
    String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return Objects.requireNonNull(response.body()).string();
        }
    }
*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_online);

/*        String response = null;
        try {
            response = this.run("https://raw.github.com/square/okhttp/master/README.md");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(response);*/
    }
}
