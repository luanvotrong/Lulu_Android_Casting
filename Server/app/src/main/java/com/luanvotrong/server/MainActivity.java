package com.luanvotrong.server;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.io.FileOutputStream;

import com.luanvotrong.recorder.Recorder;

public class MainActivity extends AppCompatActivity {

    private Button m_connectButton;
    private Button m_castButton;
    private Recorder m_recorder;
    private Server m_server;
    private View m_screenView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        m_screenView = (View) findViewById(android.R.id.content).getRootView();
        m_screenView.setDrawingCacheEnabled(true);

        m_recorder = new Recorder();
        m_recorder.setView(m_screenView);
        new Thread(m_recorder).start();

        m_server = new Server();
        m_server.init(this);
        m_server.setRecorder(m_recorder);

        m_connectButton = (Button) findViewById(R.id.connect);
        m_connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_server.setState(Server.CONNECTION_STATE.CONNECTING);
            }
        });

        m_castButton = (Button) findViewById(R.id.cast);
        m_castButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_server.sendCapture();
            }
        });

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
    }

    @Override
    public void onStop() {
        m_server.disconnect();
        super.onStop();
    }

    /*
    public void onCapture() {
        Bitmap bm = m_screenView.getDrawingCache();

        String path = Environment.getExternalStorageDirectory() + "/capture.png";
        Log.v("Lulu", "path: " + path);

        long begin_time = System.nanoTime();
        java.io.File image = new java.io.File(Environment.getExternalStorageDirectory() + "/capture.png");
        if (image.exists()) {
            image.delete();
        }

        try {
            FileOutputStream out = new FileOutputStream(image);
            bm.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long end_time = System.nanoTime();
        long deltaTime = (end_time - begin_time) / 1000000;

        Log.v("Lulu", "deltatime: " + deltaTime);
    }

    public Bitmap getCapture() {
        return m_screenView.getDrawingCache();
    }
    */

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

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
}
