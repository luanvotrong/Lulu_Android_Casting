package com.luanvotrong.client;

import android.os.AsyncTask;
import android.util.Log;

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

    private int m_udpPort = 63676;
    private int m_tcpPort = 63677;
    private InetAddress m_serverAddress = null;

    private class UDPBroadcastListener extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... params) {
            byte[] message = new byte[1500];

            try {
                DatagramSocket s = new DatagramSocket(m_udpPort);
                s.setBroadcast(true);

                try {
                    DatagramPacket p = new DatagramPacket(message, message.length);
                    s.receive(p);
                    Log.d("Lulu", "Received mess");
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

            return null;
        }
    }

    private class ClientThread implements Runnable {
        @Override
        public void run() {
            try {
                Socket socket = new Socket(m_serverAddress, m_tcpPort);
                setState(CONNECTION_STATE.CONNECTED);
            } catch (Exception e) {
                Log.d("Lulu", e.toString());
            }
        }
    }

    public void init() {
        m_stateConnection = CONNECTION_STATE.LISTENING;
    }

    public void setState(CONNECTION_STATE state) {
        m_stateConnection = state;
        switch (m_stateConnection) {
            case LISTENING:
                Log.d("Lulu", "Listening");
                m_serverAddress = null;
                new UDPBroadcastListener().execute();
                break;
            case REQUESTING:
                Log.d("Lulu", "Requesting");
                new Thread(new ClientThread()).start();
                break;
            case CONNECTED:
                Log.d("Lulu", "Connected");
                break;
        }
    }
}
