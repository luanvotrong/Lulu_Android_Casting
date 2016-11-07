package com.luanvotrong.server;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.session.MediaController;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.VideoView;

import com.luanvotrong.recorder.Recorder;

import static com.luanvotrong.server.R.id.videoView;

public class MainActivity extends AppCompatActivity {

    private Button m_connectButton;
    private Button m_castButton;
    private Server m_server;

    private static final String STATE_RESULT_CODE = "result_code";
    private static final String STATE_RESULT_DATA = "result_data";
    private static final int REQUEST_MEDIA_PROJECTION = 1;
    private int m_resultCode;
    private Intent m_resultData;

    private MediaProjectionManager m_mediaProjectMgr;
    private MediaProjection m_mediaProjection;

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

        m_castButton = (Button) findViewById(R.id.cast);
        m_castButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

            m_mediaProjection = m_mediaProjectMgr.getMediaProjection(m_resultCode, m_resultData);
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
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
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