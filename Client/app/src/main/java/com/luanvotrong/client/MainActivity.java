package com.luanvotrong.client;

import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
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

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private Client m_client = null;
    private Button m_listeningButton;
    private LinearLayout m_drawingLayout;
    private DrawingView m_drawingView;

    class DrawingView extends View {
        private Bitmap m_bm;

        public DrawingView(Context context) {
            super(context);
            this.setBackgroundColor(Color.BLACK);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            setVisibility(LinearLayout.INVISIBLE);

            invalidate();
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            canvas.drawBitmap(m_bm, 0, 0, null);
        }

        public void draw(Bitmap bm) {
            m_bm = bm;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        m_drawingView = new DrawingView(this);
        m_drawingLayout = (LinearLayout) findViewById(R.id.drawingView);
        m_drawingLayout.addView(m_drawingView);
        m_drawingLayout.setVisibility(LinearLayout.INVISIBLE);

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
        m_listeningButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                m_client.setState(Client.CONNECTION_STATE.LISTENING);
                //onDraw();
            }
        });

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
    }

    @Override
    public void onStop()
    {
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

    public void onDraw()
    {
        String path = Environment.getExternalStorageDirectory() + "/capture.png";
        File pic = new File(path);
        if(pic.exists()) {
            Bitmap bm = BitmapFactory.decodeFile(path);
            m_drawingView.draw(bm);
            m_drawingLayout.setVisibility(LinearLayout.VISIBLE);
        }
    }

    public void onDraw(Bitmap bm)
    {
        m_drawingView.draw(bm);
        m_drawingLayout.setVisibility(LinearLayout.VISIBLE);
    }

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
}
