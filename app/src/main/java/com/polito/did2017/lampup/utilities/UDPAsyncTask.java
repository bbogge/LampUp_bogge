package com.polito.did2017.lampup.utilities;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Created by matil on 14/03/2018.
 */

public class UDPAsyncTask extends AsyncTask<Object, String, Integer> {

    private Runnable updateUI;
    private boolean keepListening;
    private final int udpPort = 4096;
    private boolean sendUDP;
    // private boolean prevLampState; // vecchia versione
    private InetAddress lampIP;

    // per salvare lo stato precedente delle lampade della lista della home
    private Dictionary<InetAddress, Boolean> lampPreviousState;

    DatagramSocket socket = null;

    public UDPAsyncTask(Runnable updateUI) {
        this.updateUI = updateUI;
        this.sendUDP = false;
        // this.prevLampState = false; // vecchia versione
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        this.updateUI.run();
    }

    @Override
    protected Integer doInBackground(Object... nothing) {
        Log.d("doInBackground_udp", "started");

        keepListening = true;

        // msg to send alla lampada (turnOn/turnOff)
        String messageStr;
        int msgLength;
        byte[] message;

        // msg to receive
        byte[] recvBuf = new byte[64000];
        DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);

        try {
            if (socket == null || socket.isClosed()) {
                socket = new DatagramSocket(udpPort);
            }
            socket.setSoTimeout(4000);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        while(keepListening) {
            if(sendUDP) {

                // è avvenuto uno switch di accensione/spegnimento -> mando il pacchetto
                messageStr = (!lampPreviousState.get( lampIP )) ? "turnOn" : "turnOff";
                Log.d("doInBackground_udp", "new state: " + !lampPreviousState.get( lampIP ));

                msgLength = messageStr.length();
                message = messageStr.getBytes();

                DatagramPacket p = new DatagramPacket(message, msgLength, lampIP, udpPort);

                try {
                    socket.send(p);//properly able to send data. i receive data to server
                    Log.e( "UDP SENDER TASK", "Inviato UDP a: " + lampIP + ", message: " + messageStr );

                } catch (IOException e) {
                    e.printStackTrace();
                }

                // aggiorna lo stato della lampada nel dizionario apposito
                lampPreviousState.put( lampIP, !lampPreviousState.get( lampIP ));
                sendUDP = false;
            }
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
                this.publishProgress( "upDateUI" );
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

        this.lampIP = lampIP;

        Log.d("sendUDPdatagram TASK", "lampState: " + lampState);
        Log.d("sendUDPdatagram TASK", "prevState: " + lampPreviousState.get( lampIP ));



        if(lampPreviousState.get( lampIP ) != lampState) {
            sendUDP = true;
        }

        // vecchia versione
        /*if(prevLampState != lampState)
            sendUDP = true;*/
        Log.d("sendUDPdatagram TASK", "sendUDP" + sendUDP);
    }

    public Dictionary<InetAddress, Boolean> getLampPreviousState() {
        return lampPreviousState;
    }

    public void setLampPreviousState(Dictionary<InetAddress, Boolean> lampPreviousState) {
        this.lampPreviousState = lampPreviousState;
    }
}
