package com.luanvotrong.client;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

public class Client {
    public enum CONNECTION_STATE {
        LISTENING,
        REQUESTING,
        CONNECTED
    }

    private CONNECTION_STATE m_stateConnection;

    private int m_udpPort = 63678;
    private int m_tcpPort = 63677;
    private InetAddress m_serverAddress = null;
    private MainActivity m_context;

    private class UDPBroadcastListener implements Runnable {
        @Override
        public void run() {
            byte[] message = new byte[1500];

            try {
                DatagramSocket s = new DatagramSocket(m_udpPort);
                s.setBroadcast(true);

                try {
                    Log.d("Lulu", "Receivings mess");
                    DatagramPacket p = new DatagramPacket(message, message.length);
                    s.receive(p);
                    String mess = new String(message, 0, p.getLength());
                    m_serverAddress = p.getAddress();
                    Log.d("Lulu", "Ip server: " + m_serverAddress.getHostAddress());
                    Log.d("Lulu", "Received: " + mess);
                    s.close();

                    if (mess.length() > 0) {
                        setState(CONNECTION_STATE.REQUESTING);
                    }
                } catch (Exception e) {
                    Log.d("Lulu", e.toString());
                }
            } catch (Exception e) {
                Log.d("Lulu", e.toString());
            }
        }
    }

    Socket m_socket = null;

    private class ClientThread implements Runnable {
        @Override
        public void run() {
            try {
                m_socket = new Socket(m_serverAddress, m_tcpPort);
                setState(CONNECTION_STATE.CONNECTED);
            } catch (Exception e) {
                Log.d("Lulu", e.toString());
            }
        }
    }

    private class ClientReceiveThread implements Runnable {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    InputStream in = m_socket.getInputStream();
                    DataInputStream dis = new DataInputStream(in);

                    int len = dis.readInt();
                    byte[] data = new byte[len];
                    if (len > 0) {
                        dis.readFully(data);
                    }
                    //saveData(BitmapFactory.decodeByteArray(data, 0, len));
                    m_context.onDraw(BitmapFactory.decodeByteArray(data, 0, len));
                } catch (Exception e) {

                }
            }
        }
    }

    public void saveData(Bitmap bm) {
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

    public void init(MainActivity context) {
        m_context = context;
        m_stateConnection = CONNECTION_STATE.LISTENING;
    }

    public void setState(CONNECTION_STATE state) {
        m_stateConnection = state;
        switch (m_stateConnection) {
            case LISTENING:
                Log.d("Lulu", "Listening");
                m_serverAddress = null;
                //new UDPBroadcastListener().execute();
                new Thread(new UDPBroadcastListener()).start();
                break;
            case REQUESTING:
                Log.d("Lulu", "Requesting");
                new Thread(new ClientThread()).start();
                break;
            case CONNECTED:
                Log.d("Lulu", "Connected");
                new Thread(new ClientReceiveThread()).start();
                break;
        }
    }

    public void disconnect() {
        try {
            m_socket.close();
        } catch (Exception e) {
        }
    }
}
