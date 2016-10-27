package com.luanvotrong.recorder;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;


public class Recorder implements Runnable {
    private float FPS_INTERVAL = 1000 / 60;
    private float m_timer;
    private long m_lastTime;

    private View m_context;
    private Queue<Bitmap> m_frameQueue;

    public Recorder() {
        m_context = null;
        m_lastTime = System.currentTimeMillis();
        m_timer = FPS_INTERVAL;
        m_frameQueue = new LinkedList();
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
            Log.d("Lulu", "Recorded");
            Bitmap original = m_context.getDrawingCache();
            m_frameQueue.add(Bitmap.createScaledBitmap(original, original.getWidth() / 5, original.getHeight() / 5, true));
            m_timer += FPS_INTERVAL;
        }
        Log.d("Lulu", "timer: " + m_timer + "deltatime " + deltaTime);
    }

    public int numberOfFrame()
    {
        return m_frameQueue.size();
    }

    public Bitmap getFrame()
    {
        return m_frameQueue.poll();
    }
}
