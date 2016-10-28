package com.luanvotrong.recorder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.luanvotrong.Utiliies.FrameQueue;

import java.io.FileOutputStream;


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

    public void setView(View context) {
        m_context = context;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            long now = System.currentTimeMillis();
            long deltaTime = now - m_lastTime;
            update(deltaTime);
            m_lastTime = now;
        }
    }

    private void update(long deltaTime) {
        m_timer -= deltaTime;
        if (m_timer <= 0) {
            addFrame(captureView());
            m_timer += FPS_INTERVAL;
        }
    }

    private Bitmap captureView() {
        m_context.setDrawingCacheEnabled(true);
        m_context.buildDrawingCache();
        Bitmap bm = m_context.getDrawingCache();
        bm =  Bitmap.createScaledBitmap(bm, bm.getWidth() / 3, bm.getHeight() / 3, true);
        save(bm);
        m_context.setDrawingCacheEnabled(false);

        return bm;
    }

    private int frame = 0;
    public void save(Bitmap bm) {
        frame++;
        java.io.File image = new java.io.File(Environment.getExternalStorageDirectory() + "/capture" + frame + ".png");
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
    }

    private void addFrame(Bitmap bm) {
        if (bm != null) {
            m_frameQueue.add(bm);
            Log.d("Lulu", "Added " + m_frameQueue.size());
        }
    }

    public FrameQueue getFrameQueue() {
        return m_frameQueue;
    }
}
