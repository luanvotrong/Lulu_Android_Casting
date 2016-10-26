package com.luanvotrong.server;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.net.DhcpInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
    private int m_serverPort = 54018;

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
                DatagramPacket p = new DatagramPacket(message, msg_length, local, m_serverPort);

                s.send(p);
                Log.d("Lulu", "sent");
            } catch (Exception e) {
                Log.d("Lulu", e.toString());
            }

            return null;
        }
    }


    public class ServerThread implements Runnable {
        public void run()
        {
            try {
                ServerSocket serverSocket = new ServerSocket(m_serverPort);
                while(true)
                {
                    Socket client = serverSocket.accept();

                    //ONCONNECTED

                    try
                    {
                        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        String line = null;

                        while((line = in.readLine()) != null)
                        {
                            Log.d("Lulu", "Message received: " + line);
                        }
                    }
                    catch(Exception e)
                    {
                        //ON-DISCONNECTED
                    }
                }
            }
            catch(Exception e)
            {
            }
        }
    }

    private Boolean m_isConnected = false;
    private Context m_context = null;

    public void SetContext(Context context) {
        m_context = context;
    }

    public Boolean IsConnected() {
        return m_isConnected;
    }

    public void FindConnect() {
        new Broadcaster().execute();
    }
}
