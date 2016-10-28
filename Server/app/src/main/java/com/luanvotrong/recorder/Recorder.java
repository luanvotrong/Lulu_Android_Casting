package com.luanvotrong.recorder;

import android.graphics.Bitmap;
import android.view.View;

import java.util.LinkedList;
import java.util.Queue;

import com.luanvotrong.Utiliies.FrameQueue;


public class Recorder implements Runnable {
    private int FPS = 30;
    private float FPS_INTERVAL = 1000 / FPS;
    private float m_timer;
    private long m_lastTime;

    private View m_context;
    private FrameQueue<Bitmap> m_frameQueue;

    public Recorder() {
        m_context = null;
        m_lastTime = System.currentTimeMillis();
        m_timer = FPS_INTERVAL;
        m_frameQueue = new FrameQueue();
    }

    public void setView(View context)
    {
        m_context = context;
    }

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
            Bitmap original = m_context.getDrawingCache();
            addFrame(Bitmap.createScaledBitmap(original, original.getWidth() / 3, original.getHeight() / 3, false));
            m_timer += FPS_INTERVAL;
        }
    }

    private void addFrame(Bitmap bm)
    {
        m_frameQueue.add(bm);
    }

    public FrameQueue getFrameQueue()
    {
        return m_frameQueue;
    }
}
