package com.luanvotrong.Renderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.Queue;

import com.luanvotrong.Utiliies.FrameQueue;

public class DrawingView extends View {
    private int FPS = 30;
    private float FPS_INTERVAL = 1000 / FPS;
    private float m_timer;
    private long m_lastTime;

    private FrameQueue<Bitmap> m_frameQueue;

    private class FrameUpdater implements Runnable {
        @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted())
            {
                long now = System.currentTimeMillis();
                long deltaTime = now - m_lastTime;
                update(deltaTime);
                m_lastTime = now;
            }
        }

        private void update(long deltaTime)
        {
            m_timer -= deltaTime;
            if(m_timer <= 0)
            {
                //saveData(m_frameQueue.poll());
                m_timer += FPS_INTERVAL;
            }
        }


        private int frameCount = 0;
        public void saveData(Bitmap bm) {
            if(bm == null)
                return;
            frameCount++;
            long begin_time = System.nanoTime();
            java.io.File image = new java.io.File(Environment.getExternalStorageDirectory() + "/capture" + frameCount + ".png");
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
    }

    public DrawingView(Context context) {
        super(context);
        this.setBackgroundColor(Color.BLACK);
        m_timer = System.currentTimeMillis();
    }

    public void init()
    {
        m_lastTime = System.currentTimeMillis();
        m_timer = FPS_INTERVAL;
        m_frameQueue = new FrameQueue();
        new Thread( new FrameUpdater() ).start();
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

        Bitmap bm = m_frameQueue.peek();
        if(bm != null) {
            canvas.drawBitmap(m_frameQueue.peek(), 0, 0, null);
        }
    }

    public void addFrame(Bitmap bm) {
        m_frameQueue.add(bm);
    }
}