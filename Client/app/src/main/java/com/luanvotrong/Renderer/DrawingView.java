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
                m_frameQueue.poll();
                m_timer += FPS_INTERVAL;
                postInvalidate();
            }
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

        Log.d("Lulu", "onDraw");
        Bitmap bm = m_frameQueue.peek();
        if(bm != null) {
            canvas.drawColor(Color.BLACK);
            canvas.drawBitmap(bm, 0, 0, null);
            Log.d("Lulu", "onDraw frame");
            bm.recycle();
        }
    }

    public void addFrame(Bitmap bm) {
        m_frameQueue.add(bm);
    }
}