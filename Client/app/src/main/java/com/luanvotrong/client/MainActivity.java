package com.luanvotrong.client;

import android.content.Context;
import android.graphics.*;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.VideoView;

import java.io.File;

import com.luanvotrong.Renderer.*;

public class MainActivity extends AppCompatActivity {
    private Client m_client = null;
    private Button m_listeningButton;
    private VideoView m_videoView;
    String m_videoPath = Environment.getExternalStorageDirectory() + "/video";
    int m_videoId = 0;
    String m_extension = ".mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        m_client = new Client();
        m_client.init(this);
        m_listeningButton = (Button) findViewById(R.id.listen);
        m_listeningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_client.setState(Client.CONNECTION_STATE.LISTENING);
                //onDraw();
            }
        });

        m_videoView = (VideoView) findViewById(R.id.videoView);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
    }

    public void onFileReceived()
    {
        try {
            m_videoId++;
            m_videoView.setVideoPath(m_videoPath + m_videoId + m_extension);
            m_videoView.requestFocus();
            m_videoView.start();

            Log.d("Lulu", "set");
        }catch (Exception e)
        {
            Log.d("Lulu", e.toString());
        }
    }

    @Override
    public void onStop() {
        m_client.disconnect();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public void onDraw(Bitmap bm) {
    }

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
}
