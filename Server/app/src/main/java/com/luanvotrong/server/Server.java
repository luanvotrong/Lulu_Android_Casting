package com.luanvotrong.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.net.DhcpInfo;

import com.luanvotrong.recorder.Recorder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;


public class Server {
    public enum CONNECTION_STATE {
        CONNECTING,
        CONNECTED
    }

    private CONNECTION_STATE m_stateConnection;

    private int m_udpPort = 63678;
    private int m_tcpPort = 63677;

    private class Broadcaster extends AsyncTask<Void, Void, Void> {
        private Exception exception;

        private InetAddress getBroadcastAddress() throws IOException {
            WifiManager wifi = (WifiManager) m_context.getSystemService(Context.WIFI_SERVICE);
            DhcpInfo dhcp = wifi.getDhcpInfo();
            if (dhcp == null) {
                Log.d("Lulu", "fuck no DHCP");
            }

            int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
            byte[] quads = new byte[4];
            for (int k = 0; k < 4; k++) {
                quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
            }

            return InetAddress.getByAddress(quads);
        }

        protected Void doInBackground(Void... params) {
            String mess = "yeah";
            try {
                DatagramSocket s = new DatagramSocket();
                InetAddress local = getBroadcastAddress();

                int msg_length = mess.length();
                byte[] message = mess.getBytes();
                DatagramPacket p = new DatagramPacket(message, msg_length, local, m_udpPort);

                s.send(p);
                Log.d("Lulu", "sent");
            } catch (Exception e) {
                Log.d("Lulu", e.toString());
            }

            return null;
        }
    }

    ServerSocket m_serverSocket = null;
    Socket m_socket = null;

    public Socket getSocket() {
        return m_socket;
    }

    public class ServerThread implements Runnable {
        @Override
        public void run() {
            try {
                Log.d("Lulu", "Binded socket!");
                m_serverSocket = new ServerSocket(m_tcpPort);
                m_socket = m_serverSocket.accept();

                //ONCONNECTED
                setState(CONNECTION_STATE.CONNECTED);
            } catch (Exception e) {
            }
        }
    }

    private MainActivity m_context = null;
    private Recorder m_recorder;
    private Broadcaster m_broadReceiver;
    private Thread m_serverThread;

    Server(MainActivity context) {
        m_context = context;
        m_broadReceiver = null;
        m_serverThread = null;
    }

    public void init() {
        m_recorder = m_context.getRecorder();
    }

    public void setState(CONNECTION_STATE state) {
        m_stateConnection = state;
        switch (m_stateConnection) {
            case CONNECTING:
                Log.d("Lulu", "Connecting!");
                m_broadReceiver = new Broadcaster();
                m_broadReceiver.execute();

                m_serverThread = new Thread(new ServerThread());
                m_serverThread.start();
                break;
            case CONNECTED:
                Log.d("Lulu", "Connected!");
                m_broadReceiver.cancel(true);
                m_broadReceiver = null;
                break;
        }
    }

    private class Sender implements Runnable {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                sendCapture();
            }
        }
    }

    private Thread m_senderThread;

    public void startCasting() {
        m_senderThread = new Thread(
                new Sender()
        );
        m_senderThread.start();
    }

    String m_videoPath = Environment.getExternalStorageDirectory() + "/video";
    int m_videoId = 0;
    String m_extension = ".mp4";

    public void sendCapture() {
        m_videoId++;
        if(m_recorder.getCurrentFileId()<=m_videoId) {
            m_videoId--;
            return;
        }
        try {
            File file = new File(m_videoPath+m_videoId+m_extension);
            if(file.exists()) {
                byte[] array = new byte[(int) file.length()];
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                bis.read(array, 0, array.length);

                OutputStream os = m_socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(os);
                dos.writeInt(array.length);
                dos.write(array, 0, array.length);

                file.delete();
                Log.d("Lulu", "id " + m_videoId);
            }
        } catch (Exception e) {
        }
    }

    public void disconnect() {
        try {
            m_socket.close();
            m_serverSocket.close();
        } catch (Exception e) {
        }
    }
}
