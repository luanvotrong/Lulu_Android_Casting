package com.luanvotrong.recorder;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;

import com.luanvotrong.Utiliies.FrameQueue;
import com.luanvotrong.server.Server;


public class Recorder  implements  MediaRecorder.OnInfoListener{
    private MediaProjection m_mediaProjection;
    private VirtualDisplay m_virtualDisplay;
    private Surface m_inputSurface;

    private int m_screenH;
    private int m_screenW;
    private int m_pxDensity;

    Activity m_context;

    private MediaRecorder m_mediaRecorder;
    boolean m_isRecording = false;

    String m_videoPath = Environment.getExternalStorageDirectory() + "/video";
    int m_videoId = 0;
    String m_extension = ".mp4";

    //Video handling
    Server m_server;

    public Recorder(Activity context, MediaProjection mediaProjection) {
        m_context = context;
        m_mediaProjection = mediaProjection;
    }

    public void setServer(Server server) {
        m_server = server;
    }

    public boolean isRecording() {
        return m_isRecording;
    }

    public void startRecording() {
        m_isRecording = true;

        //Prepare File
        m_videoId++;

        // Get the display size and density.
        m_screenW = m_context.getWindow().getDecorView().getWidth();
        m_screenH = m_context.getWindow().getDecorView().getHeight();
        android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
        Log.d("Lulu", m_screenW + " " + m_screenH + " ");
        m_context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        m_pxDensity = metrics.densityDpi;

        DisplayManager dm = (DisplayManager) m_context.getSystemService(Context.DISPLAY_SERVICE);
        Display defaultDisplay = dm.getDisplay(Display.DEFAULT_DISPLAY);
        if (defaultDisplay == null) {
            throw new RuntimeException("No display found.");
        }

        m_mediaRecorder = new MediaRecorder();
        m_mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        m_mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        m_mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        m_mediaRecorder.setVideoEncodingBitRate(512 * 1000);
        m_mediaRecorder.setVideoFrameRate(30);
        m_mediaRecorder.setVideoSize(m_screenW, m_screenH);
        m_mediaRecorder.setMaxDuration(1000);
        m_mediaRecorder.setOutputFile(m_videoPath+m_videoId+m_extension);
        m_mediaRecorder.setOnInfoListener(this);

        try
        {
            m_mediaRecorder.prepare();
        }
        catch (Exception e)
        {

        }


        m_inputSurface = m_mediaRecorder.getSurface();
        // Start the video input.
        m_virtualDisplay = m_mediaProjection.createVirtualDisplay("Recording Display", m_screenW,
                m_screenH, m_pxDensity, 0 /* flags */, m_inputSurface,
                null /* callback */, null /* handler */);

        m_mediaRecorder.start();

    }

    public void releaseEncoders() {
        m_isRecording = false;

        m_mediaRecorder.stop();
        m_mediaRecorder.reset();
    }

    public int getCurrentFileId()
    {
        return m_videoId;
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra)
    {
        Log.d("Lulu", "on");
        if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED)
        {

            Log.d("Lulu", "on1");
            releaseEncoders();
            startRecording();
        }
    }
}