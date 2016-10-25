package com.example.luanvotrong.client;

import android.os.AsyncTask;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Client {
    private int m_serverPort = 54018;

    private class BroadcastListener extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... params) {
            String text = new String();
            byte[] message = new byte[1500];

            try {
                DatagramSocket s = new DatagramSocket(m_serverPort);
                s.setBroadcast(true);

                while (text.length() <= 0) {
                    try {
                        DatagramPacket p = new DatagramPacket(message, message.length);
                        s.receive(p);
                        text = new String(message, 0, p.getLength());
                        Log.d("Lulu", "Ip server: " + p.getAddress().getHostAddress());
                        Log.d("Lulu", "Received: " + text);
                        StartRequestConnect(p.getAddress());
                        s.close();
                    } catch (Exception e) {
                        Log.d("Lulu", e.toString());
                    }
                }
            } catch (Exception e) {
                Log.d("Lulu", e.toString());
            }

            return null;
        }
    }

    private class ClientThread implements Runnable {
        public void run()
        {

        }
    }

    private BroadcastListener m_broadcastListener = new BroadcastListener();

    public void StartListening() {
        m_broadcastListener.execute();
    }

    public void StartRequestConnect(InetAddress address) {

    }
}
