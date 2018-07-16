package com.polito.did2017.lampup.utilities;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import static java.lang.Thread.sleep;

import static android.content.ContentValues.TAG;

/**
 * Created by matil on 20/04/2018.
 */

public class TCPClient {
    //public static final String SERVER_IP = "192.168.1.8";
    private String server_ip; // server IP address (della lampada)
    public static final int SERVER_PORT = 2048;
    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;
    //socket
    private Socket socket;
    //message
    private static final String DEFAULT= "?";
    private String message = DEFAULT;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPClient(OnMessageReceived listener, String lamp_ip) {
        mMessageListener = listener;
        this.server_ip = lamp_ip;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    private String sendMessage(final String message) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mBufferOut != null) {
                    Log.d(TAG, "Sending: " + message);
                    //mBufferOut.println(message + "\r\n");
                    mBufferOut.print(message + "\n");
                    mBufferOut.flush();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        return message;
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {

        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }

    public void run() throws IOException, InterruptedException{

        mRun = true;

        //here you must put your computer's IP address.
        InetAddress serverAddr = InetAddress.getByName(server_ip);

        //in this while the client listens for the messages sent by the server
        while (mRun) {

            Log.e("TCP Client", "C: Connected");

            //create a socket to make the connection with the server
            socket = new Socket(serverAddr, SERVER_PORT);
            socket.setSoTimeout(5000);
            //sends the message to the server
            mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

            //receives the message which the server sends back
            mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String sent = sendMessage(message);

            try {
                mServerMessage = mBufferIn.readLine();
            }
            catch(SocketTimeoutException e) {
                socket.close();
                if(sent.equals(message))
                    this.setMessage(DEFAULT);
            }

            if (mServerMessage != null && mMessageListener != null) {
                //call the method messageReceived from MyActivity class
                mMessageListener.messageReceived(mServerMessage);
                //Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");
            }

            socket.close();
            if(sent.equals(message))
                this.setMessage(DEFAULT);

            Log.e("TCP Client", "C: Disconnected");

            sleep(1000);
        }

    }

    public void setMessage(String message) {
        this.message = message;
    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}

