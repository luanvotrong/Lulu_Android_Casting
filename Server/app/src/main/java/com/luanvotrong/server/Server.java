package com.luanvotrong.server;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.net.DhcpInfo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
    public enum CONNECTION_STATE {
        CONNECTING,
        CONNECTED
    }

    private CONNECTION_STATE m_stateConnection;

    private int m_udpPort = 63678;
    private int m_tcpPort = 63677;

    private String TAG = "Lulu Server";

    private class Broadcaster extends AsyncTask<Void, Void, Void> {
        private Exception exception;

        private InetAddress getBroadcastAddress() throws IOException {
            WifiManager wifi = (WifiManager) m_context.getSystemService(Context.WIFI_SERVICE);
            DhcpInfo dhcp = wifi.getDhcpInfo();
            if (dhcp == null) {
                Log.d(TAG, "fuck no DHCP");
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
                Log.d(TAG, "sent");
            } catch (Exception e) {
                Log.d(TAG, e.toString());
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
                Log.d(TAG, "Binded socket!");
                m_serverSocket = new ServerSocket(m_tcpPort);
                m_socket = m_serverSocket.accept();

                //ONCONNECTED
                setState(CONNECTION_STATE.CONNECTED);
            } catch (Exception e) {
            }
        }
    }

    private MainActivity m_context = null;
    private Broadcaster m_broadReceiver;
    private Thread m_serverThread;

    public void init(MainActivity context) {
        m_broadReceiver = null;
        m_serverThread = null;
        m_context = context;
    }

    public void setState(CONNECTION_STATE state) {
        m_stateConnection = state;
        switch (m_stateConnection) {
            case CONNECTING:
                Log.d(TAG, "Connecting!");
                m_broadReceiver = new Broadcaster();
                m_broadReceiver.execute();

                m_serverThread = new Thread(new ServerThread());
                m_serverThread.start();
                break;
            case CONNECTED:
                Log.d(TAG, "Connected!");
                m_broadReceiver.cancel(true);
                m_broadReceiver = null;
                break;
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
