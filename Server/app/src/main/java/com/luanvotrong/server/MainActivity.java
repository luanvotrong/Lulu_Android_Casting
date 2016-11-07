package com.luanvotrong.server;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.media.projection.MediaProjectionManager;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.audio.AudioQuality;
import net.majorkernelpanic.streaming.video.VideoQuality;

public class MainActivity extends AppCompatActivity implements Session.Callback {
    private String TAG = "Lulu MainActivity";

    private Button m_connectButton;
    private Server m_server;

    private static final String STATE_RESULT_CODE = "result_code";
    private static final String STATE_RESULT_DATA = "result_data";
    private static final int REQUEST_MEDIA_PROJECTION = 1;
    private int m_resultCode;
    private Intent m_resultData;

    private int m_screenW;
    private int m_screenH;
    private int m_pxDensity;
    private MediaProjectionManager m_mediaProjectMgr;
    private MediaProjection m_mediaProjection;

    private Session m_session;

    //Activity Override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            {

            }
            else
            {
                ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
            }
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE))
            {

            }
            else
            {
                ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE }, 1);
            }
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        m_server = new Server();
        m_server.init(this);

        m_connectButton = (Button) findViewById(R.id.connect);
        m_connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_server.setState(Server.CONNECTION_STATE.CONNECTING);
            }
        });

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        //////////////////////////////////////////////
        if (savedInstanceState != null) {
            m_resultCode = savedInstanceState.getInt(STATE_RESULT_CODE);
            m_resultData = savedInstanceState.getParcelable(STATE_RESULT_DATA);
        }

        m_mediaProjectMgr = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(m_mediaProjectMgr.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != MainActivity.RESULT_OK) {
                Log.d("Lulu", "Request failed");
                return;
            }

            m_resultCode = resultCode;
            m_resultData = data;

            m_screenW = getWindow().getDecorView().getWidth();
            m_screenH = getWindow().getDecorView().getHeight();
            android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
            m_pxDensity = metrics.densityDpi;
            Log.d("Lulu", m_screenW + " " + m_screenH + " " + m_pxDensity);


            m_mediaProjection = m_mediaProjectMgr.getMediaProjection(m_resultCode, m_resultData);
            //Init streaming session after got media projectino
            m_session = SessionBuilder.getInstance()
                    .setCallback(this)
                    .setContext(this)
                    .setAudioEncoder(SessionBuilder.AUDIO_NONE)
                    .setAudioQuality(new AudioQuality(16000, 32000))
                    .setVideoEncoder(SessionBuilder.VIDEO_H264)
                    .setVideoQuality(new VideoQuality(m_screenW/4, m_screenH/4, 20, 500000))
                    .setMediaProjection(m_mediaProjection)
                    .build();
            m_session.configure();
        }
    }

    @Override
    public void onStop() {
        m_server.disconnect();
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
     * Session callbacks
     */
    @Override
    public void onBitrateUpdate(long bitrate) {
        Log.d(TAG,"Bitrate: "+bitrate);
    }

    @Override
    public void onSessionError(int message, int streamType, Exception e) {
        if (e != null) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Override

    public void onPreviewStarted() {
        Log.d(TAG,"Preview started.");
    }

    @Override
    public void onSessionConfigured() {
        Log.d(TAG,"Preview configured.");
        // Once the stream is configured, you can get a SDP formated session description
        // that you can send to the receiver of the stream.
        // For example, to receive the stream in VLC, store the session description in a .sdp file
        // and open it with VLC while streming.
        Log.d(TAG, m_session.getSessionDescription());
        m_session.start();
    }

    @Override
    public void onSessionStarted() {
        Log.d(TAG,"Session started.");
    }

    @Override
    public void onSessionStopped() {
        Log.d(TAG,"Session stopped.");
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