package com.polito.did2017.lampup.utilities;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by matil on 14/03/2018.
 */

public class UDPAsyncTask extends AsyncTask<Object, String, Integer> {

    private Runnable updateUI;
    private boolean keepListening;
    private final int udpPort = 4096;
    private LampManager lm = LampManager.getInstance();

    DatagramSocket socket = null;

    public UDPAsyncTask(Runnable updateUI) {
        this.updateUI = updateUI;
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        this.updateUI.run();
    }

    @Override
    protected Integer doInBackground(Object... nothing) {
        Log.d("doInBackground_udp", "started");

        keepListening = true;

        byte[] recvBuf = new byte[64000];
        DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);

        try {
            if (socket == null || socket.isClosed()) {
                socket = new DatagramSocket(udpPort);
            }
            socket.setSoTimeout(5000);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        while(keepListening) {
            Log.e("UDP", "Waiting for UDP broadcast");

            try {
                socket.receive(packet);

                String senderIP = packet.getAddress().getHostAddress().trim();
                String lamp_name = new String(recvBuf, 0 , packet.getLength()).trim();

                Log.e("UDP", "ricevuto broadcast UDP da: " + senderIP + ", lamp_name: " + lamp_name);

                LampManager.getInstance().addLamp( senderIP, lamp_name );
                this.publishProgress( "updateUI" );

            }
            catch (SocketTimeoutException e) {
                //this.publishProgress( "upDateUI" );
                continue;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(socket != null) {
            socket.close();
        }

        return 0;
    }

    public void stopListening() {
        keepListening = false;
    }

    public void sendUDPdatagram(InetAddress lampIP, boolean lampState) {
        // per inviare pacchetto udp per lo switch della lista lampade

        String messageStr = (lampState) ? "turnOn" : "turnOff";

        /*if(lampState)
            messageStr = "turnOn";
        else
            messageStr = "turnOff";*/

        int msgLength = messageStr.length();
        byte[] message = messageStr.getBytes();

        try {
            if (socket == null || socket.isClosed()) {
                socket = new DatagramSocket(udpPort);
            }
            socket.setSoTimeout(1000);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        DatagramPacket p = new DatagramPacket(message, msgLength, lampIP, udpPort);

        try {
            socket.send(p);//properly able to send data. i receive data to server
            Log.e( "UDP SENDER TASK", "Inviato UDP a: " + lampIP + ", message: " + messageStr );

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
