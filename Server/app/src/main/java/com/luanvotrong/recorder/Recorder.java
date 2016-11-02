package com.luanvotrong.recorder;

import android.app.Activity;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;

import com.luanvotrong.Utiliies.FrameQueue;

import java.io.IOException;
import java.nio.ByteBuffer;


public class Recorder {
    private static final String VIDEO_MIME_TYPE = "video/avc";
    private static final int VIDEO_WIDTH = 720;
    private static final int VIDEO_HEIGHT = 1280;

    private MediaProjection m_mediaProjection;
    private VirtualDisplay m_virtualDisplay;
    private Surface m_inputSurface;

    private int m_screenH;
    private int m_screenW;
    private int m_pxDensity;

    Activity m_context;

    boolean m_isRecording = false;

    //Video handling
    private MediaMuxer m_mediaMuxer;
    private MediaCodec m_videoEncoder;
    private MediaCodec.BufferInfo m_videoBufferInfo;
    private boolean m_muxerStarted = false;
    private int m_trackIdx = -1;
    String m_videoPath = Environment.getExternalStorageDirectory() + "/video.mp4";

    FrameQueue<ByteBuffer> m_buffer;

    private final Handler m_drainHandler = new Handler(Looper.getMainLooper());
    private Runnable m_drainEncoderRunnable = new Runnable() {
        @Override
        public void run() {
            drainEncoder();
        }
    };

    public Recorder(Activity context, MediaProjection mediaProjection) {
        m_context = context;
        m_mediaProjection = mediaProjection;
        m_buffer = new FrameQueue<ByteBuffer>();
    }

    public boolean isRecording() {
        return m_isRecording;
    }

    public void startRecording() {
        m_isRecording = true;
        java.io.File video = new java.io.File(m_videoPath);
        if (video.exists()) {
            video.delete();
        }


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
        prepareVideoEncoder();

        try {
            //m_mediaMuxer = new MediaMuxer("/sdcard/video.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            m_mediaMuxer = new MediaMuxer(m_videoPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException ioe) {
            throw new RuntimeException("MediaMuxer creation failed", ioe);
        }

        // Start the video input.
        m_mediaProjection.createVirtualDisplay("Recording Display", m_screenW,
                m_screenH, m_pxDensity, 0 /* flags */, m_inputSurface,
                null /* callback */, null /* handler */);

        // Start the encoders
        drainEncoder();
    }

    public FrameQueue getFrameQueue() {
        return m_buffer;
    }

    private void prepareVideoEncoder() {
        m_videoBufferInfo = new MediaCodec.BufferInfo();
        MediaFormat format = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, m_screenW, m_screenH);
        int frameRate = 30; // 30 fps

        // Set some required properties. The media codec may fail if these aren't defined.
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 6000000); // 6Mbps
        format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
        format.setInteger(MediaFormat.KEY_CAPTURE_RATE, frameRate);
        format.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1000000 / frameRate);
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1); // 1 seconds between I-frames

        // Create a MediaCodec encoder and configure it. Get a Surface we can use for recording into.
        try {
            m_videoEncoder = MediaCodec.createEncoderByType(VIDEO_MIME_TYPE);
            m_videoEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            m_inputSurface = m_videoEncoder.createInputSurface();
            m_videoEncoder.start();
        } catch (IOException e) {
            releaseEncoders();
        }
    }

    private boolean drainEncoder() {
        m_drainHandler.removeCallbacks(m_drainEncoderRunnable);
        while (true) {
            int bufferIndex = m_videoEncoder.dequeueOutputBuffer(m_videoBufferInfo, 0);

            if (bufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // nothing available yet
                break;
            } else if (bufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // should happen before receiving buffers, and should only happen once
                if (m_trackIdx >= 0) {
                    throw new RuntimeException("format changed twice");
                }
                m_trackIdx = m_mediaMuxer.addTrack(m_videoEncoder.getOutputFormat());
                if (!m_muxerStarted && m_trackIdx >= 0) {
                    m_mediaMuxer.start();
                    m_muxerStarted = true;
                }
            } else if (bufferIndex < 0) {
                // not sure what's going on, ignore it
            } else {
                ByteBuffer encodedData = m_videoEncoder.getOutputBuffer(bufferIndex);
                if (encodedData == null) {
                    throw new RuntimeException("couldn't fetch buffer at index " + bufferIndex);
                }

                if ((m_videoBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    m_videoBufferInfo.size = 0;
                }

                if (m_videoBufferInfo.size != 0) {
                    if (m_muxerStarted) {
                        encodedData.position(m_videoBufferInfo.offset);
                        encodedData.limit(m_videoBufferInfo.offset + m_videoBufferInfo.size);
                        m_buffer.add(encodedData);
                        m_mediaMuxer.writeSampleData(m_trackIdx, encodedData, m_videoBufferInfo);
                    } else {
                        // muxer not started
                    }
                }

                m_videoEncoder.releaseOutputBuffer(bufferIndex, false);

                if ((m_videoBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break;
                }
            }
        }

        m_drainHandler.postDelayed(m_drainEncoderRunnable, 10);
        return false;
    }

    public void releaseEncoders() {
        m_isRecording = false;
        m_drainHandler.removeCallbacks(m_drainEncoderRunnable);
        if (m_mediaMuxer != null) {
            if (m_muxerStarted) {
                m_mediaMuxer.stop();
            }
            m_mediaMuxer.release();
            m_mediaMuxer = null;
            m_muxerStarted = false;
        }
        if (m_videoEncoder != null) {
            m_videoEncoder.stop();
            m_videoEncoder.release();
            m_videoEncoder = null;
        }
        if (m_inputSurface != null) {
            m_inputSurface.release();
            m_inputSurface = null;
        }
        if (m_mediaProjection != null) {
            m_mediaProjection.stop();
            m_mediaProjection = null;
        }
        m_videoBufferInfo = null;
        m_drainEncoderRunnable = null;
        m_trackIdx = -1;
    }
}