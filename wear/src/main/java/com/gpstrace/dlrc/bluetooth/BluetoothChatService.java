package com.gpstrace.dlrc.bluetooth;

/*

 * Copyright (C) 2014 The Android Open Source Project

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * you may not use this file except in compliance with the License.

 * You may obtain a copy of the License at

 *

 *      http://www.apache.org/licenses/LICENSE-2.0

 *

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 * See the License for the specific language governing permissions and

 * limitations under the License.

 */


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;



/**

 * This class does all the work for setting up and managing Bluetooth

 * connections with other devices. It has a thread that listens for

 * incoming connections, a thread for connecting with a device, and a

 * thread for performing data transmissions when connected.

 */

public class BluetoothChatService {

    // Debugging

    private static final String TAG = "BluetoothChatService";



    // Name for the SDP record when creating server socket

    private static final String NAME_SECURE = "shit";

 //   private static final String NAME_INSECURE = "BluetoothChatInsecure";



    // Unique UUID for this application

   // private static final UUID MY_UUID_SECURE =

   //         UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

   // private static final UUID MY_UUID_INSECURE =

            //UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

     private static final UUID MY_UUID_SECURE =

            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

   //  private static final UUID MY_UUID_INSECURE =

    //         UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    
    //"00001101-0000-1000-8000-00805F9B34FB"

    // Member fields

    private final BluetoothAdapter mAdapter;

    private final Handler mHandler;

    private AcceptThread mSecureAcceptThread;

   // private AcceptThread mInsecureAcceptThread;

    private ConnectThread mConnectThread;

    private ConnectedThread mConnectedThread;

    private int mState;



    // Constants that indicate the current connection state

    public static final int STATE_NONE = 0;       // we're doing nothing

    public static final int STATE_LISTEN = 1;     // now listening for incoming connections

    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection

    public static final int STATE_CONNECTED = 3;  // now connected to a remote device


    public static final int ROLE_CLIENT = 0;
    public static final int ROLE_SERVER = 1;
    
    private int _role = ROLE_SERVER;
    BluetoothDevice _destDeivce = null;
    private Context mContext= null;

    /**

     * Constructor. Prepares a new BluetoothChat session.

     *

     * @param context The UI Activity Context

     * @param handler A Handler to send messages back to the UI Activity

     */

    public BluetoothChatService(Context context, Handler handler, int role) {

        mAdapter = BluetoothAdapter.getDefaultAdapter();

        mState = STATE_NONE;

        mHandler = handler;
        mContext= context;
        _role = role;

    }



    /**

     * Set the current state of the chat connection

     *

     * @param state An integer defining the current connection state

     */

    private synchronized void setState(int state) {

        Log.d(TAG, "setState() " + mState + " -> " + state);

        mState = state;



        // Give the new state to the Handler so the UI Activity can update

        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();

    }



    /**

     * Return the current connection state.

     */

    public synchronized int getState() {

        return mState;

    }



    /**

     * Start the chat service. Specifically start AcceptThread to begin a

     * session in listening (server) mode. Called by the Activity onResume()

     */

    // 0: connect
    // 1: accept
    public synchronized void start(int role) {

        Log.d(TAG, "start");


        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        // Cancel any thread attempting to make a connection

        if (mConnectThread != null) {

            mConnectThread.cancel();

            mConnectThread = null;

        }



        // Cancel any thread currently running a connection

        if (mConnectedThread != null) {

            mConnectedThread.cancel();

            mConnectedThread = null;

        }

        if(_role==ROLE_CLIENT){
        	  setState(STATE_NONE);
        	  
        	
        }else if(_role==ROLE_SERVER){
        	
        	  setState(STATE_LISTEN);
        	  if (mSecureAcceptThread == null) {

                  mSecureAcceptThread = new AcceptThread(true);

                  mSecureAcceptThread.start();

              }
        }

      



        // Start the thread to listen on a BluetoothServerSocket

        
        

        /*
        if (mInsecureAcceptThread == null) {

            mInsecureAcceptThread = new AcceptThread(false);

            mInsecureAcceptThread.start();

        }
        */

    }



    /**

     * Start the ConnectThread to initiate a connection to a remote device.

     *

     * @param device The BluetoothDevice to connect

     * @param secure Socket Security type - Secure (true) , Insecure (false)

     */

    public synchronized void connect(BluetoothDevice device, boolean secure) {

        Log.d(TAG, "connect to: " + device);


        _destDeivce = device;

        // Cancel any thread attempting to make a connection

        if (mState == STATE_CONNECTING) {

            if (mConnectThread != null) {

                mConnectThread.cancel();

                mConnectThread = null;

            }

        }



        // Cancel any thread currently running a connection

        if (mConnectedThread != null) {

            mConnectedThread.cancel();

            mConnectedThread = null;

        }



        // Start the thread to connect with the given device

        mConnectThread = new ConnectThread(device, secure);

        mConnectThread.start();

        setState(STATE_CONNECTING);

    }



    /**

     * Start the ConnectedThread to begin managing a Bluetooth connection

     *

     * @param socket The BluetoothSocket on which the connection was made

     * @param device The BluetoothDevice that has been connected

     */

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice

            device, final String socketType) {

        Log.d(TAG, "connected, Socket Type:" + socketType);



        // Cancel the thread that completed the connection

        if (mConnectThread != null) {

            mConnectThread.cancel();

            mConnectThread = null;

        }



        // Cancel any thread currently running a connection

        if (mConnectedThread != null) {

            mConnectedThread.cancel();

            mConnectedThread = null;

        }



        // Cancel the accept thread because we only want to connect to one device

        
        if (mSecureAcceptThread != null) {

            mSecureAcceptThread.cancel();

            mSecureAcceptThread = null;

        }

        /*
        if (mInsecureAcceptThread != null) {

            mInsecureAcceptThread.cancel();

            mInsecureAcceptThread = null;

        }
*/


        // Start the thread to manage the connection and perform transmissions

       

       



        // Send the name of the connected device back to the UI Activity

        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);

        Bundle bundle = new Bundle();

        bundle.putString(Constants.DEVICE_NAME, device.getName());

        msg.setData(bundle);

        mHandler.sendMessage(msg);



        setState(STATE_CONNECTED);
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

    }



    /**

     * Stop all threads

     */

    public synchronized void stop() {

        Log.d(TAG, "stop");



        if (mConnectThread != null) {

            mConnectThread.cancel();

            mConnectThread = null;

        }



        if (mConnectedThread != null) {

            mConnectedThread.cancel();

            mConnectedThread = null;

        }



        
        if (mSecureAcceptThread != null) {

            mSecureAcceptThread.cancel();

            mSecureAcceptThread = null;

        }


/*
        if (mInsecureAcceptThread != null) {

            mInsecureAcceptThread.cancel();

            mInsecureAcceptThread = null;

        }
*/
        setState(STATE_NONE);

    }



    /**

     * Write to the ConnectedThread in an unsynchronized manner

     *

     * @param out The bytes to write

     * @see ConnectedThread#write(byte[])

     */

    public int write(byte[] out) {

        // Create temporary object

        ConnectedThread r;

        // Synchronize a copy of the ConnectedThread

        synchronized (this) {

            if (mState != STATE_CONNECTED)
                return -1;

            r = mConnectedThread;

        }

        // Perform the write unsynchronized

        int res =  r.write(out);
        return res;

    }



    /**

     * Indicate that the connection attempt failed and notify the UI Activity.

     */

    private void connectionFailed() {

        // Send a failure message back to the Activity

    	/*
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);

        Bundle bundle = new Bundle();

        bundle.putString(Constants.TOAST, "Unable to connect device");

        msg.setData(bundle);

        mHandler.sendMessage(msg);
*/


        // Start the service over to restart listening mode

        if(_role==ROLE_CLIENT){
            mHandler.postDelayed( _restart, 5000);

        }
    }

    Runnable _restart = new Runnable() {
        @Override
        public void run() {
            Log.d("luk","try to reconnect...");
            SharedPreferences macPreferences = mContext.getSharedPreferences(Constants.SAVE_MAC, Context.MODE_PRIVATE);
            String macString = macPreferences.getString("mac","");
            BluetoothDevice destDevice = null;
            if(macString.length()!=0) {
                destDevice=BluetoothAdapter.getDefaultAdapter().getRemoteDevice(macString);
            }
            _destDeivce = destDevice;
            if(_destDeivce!= null) {
                BluetoothChatService.this.start(0);
                BluetoothChatService.this.connect(_destDeivce, false);
            }else{
                mHandler.postDelayed( _restart, 5000);
            }
        }
    };

    /**
     * get handler
     * @return the thread to handle reconnect
     * @author Luk
     */
    protected Handler getHandler() {
        return mHandler;
    }
    /**

     * Indicate that the connection was lost and notify the UI Activity.

     */

    private void connectionLost() {

        // Send a failure message back to the Activity

        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);

        Bundle bundle = new Bundle();

        bundle.putString(Constants.TOAST, "Disconnect");

        msg.setData(bundle);

        mHandler.sendMessage(msg);



        // Start the service over to restart listening mode

        if(_role==ROLE_SERVER)
        {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    BluetoothChatService.this.start(1);
                }
            },5000);
        }else if(_role==ROLE_CLIENT){
        	BluetoothChatService.this.start(0);
        	BluetoothChatService.this.connect(_destDeivce, false);
        	
        }
        

    }



    /**

     * This thread runs while listening for incoming connections. It behaves

     * like a server-side client. It runs until a connection is accepted

     * (or until cancelled).

     */

    private class AcceptThread extends Thread {

        // The local server socket

        private final BluetoothServerSocket mmServerSocket;

        private String mSocketType;



        public AcceptThread(boolean secure) {

            BluetoothServerSocket tmp = null;
            

            mSocketType = secure ? "Secure" : "Insecure";



            // Create a new listening server socket

            try {
            	

              //  if (secure) {
            		
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,

                            MY_UUID_SECURE);
                    

                    /*
                } else {

                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(

                            NAME_INSECURE, MY_UUID_INSECURE);

  //              }
*/
            } catch (IOException e) {

                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);


            }

            mmServerSocket = tmp;

        }



        public void run() {

            Log.d(TAG, "Socket Type: " + mSocketType +

                    "BEGIN mAcceptThread" + this);

            setName("AcceptThread" + mSocketType);



            BluetoothSocket socket = null;



            // Listen to the server socket if we're not connected

            while (mState != STATE_CONNECTED) {

                try {

                    // This is a blocking call and will only return on a

                    // successful connection or an exception

                    socket = mmServerSocket.accept();

                } catch (IOException e) {

                    Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e);

                    break;

                }



                // If a connection was accepted

                if (socket != null) {

                    synchronized (BluetoothChatService.this) {

                        switch (mState) {

                            case STATE_LISTEN:

                            case STATE_CONNECTING:

                                // Situation normal. Start the connected thread.

                                connected(socket, socket.getRemoteDevice(),

                                        mSocketType);

                                break;

                            case STATE_NONE:

                            case STATE_CONNECTED:

                                // Either not ready or already connected. Terminate new socket.

                                try {

                                    socket.close();

                                } catch (IOException e) {

                                    Log.e(TAG, "Could not close unwanted socket", e);

                                }

                                break;

                        }

                    }

                }

            }

            Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);



        }



        public void cancel() {

            Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);

            try {

                mmServerSocket.close();

            } catch (IOException e) {

                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);

            }

        }

    }





    /**

     * This thread runs while attempting to make an outgoing connection

     * with a device. It runs straight through; the connection either

     * succeeds or fails.

     */

    private class ConnectThread extends Thread {

        private final BluetoothSocket mmSocket;

        private final BluetoothDevice mmDevice;

        private String mSocketType;
        
        private boolean _bCanceled  =false;


        public ConnectThread(BluetoothDevice device, boolean secure) {

            mmDevice = device;

            BluetoothSocket tmp = null;

            mSocketType = secure ? "Secure" : "Insecure";



            // Get a BluetoothSocket for a connection with the

            // given BluetoothDevice

            try {

            	
  //              if (secure) {

                    tmp = device.createRfcommSocketToServiceRecord(

                            MY_UUID_SECURE);
/*
                } else {

                    tmp = device.createInsecureRfcommSocketToServiceRecord(

                            MY_UUID_INSECURE);

//                }
*/
            } catch (IOException e) {

                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);

            }

            mmSocket = tmp;

        }



        public void run() {

            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);

            setName("ConnectThread" + mSocketType);



            // Always cancel discovery because it will slow down a connection

            mAdapter.cancelDiscovery();



            // Make a connection to the BluetoothSocket

            try {

                // This is a blocking call and will only return on a

                // successful connection or an exception

                mmSocket.connect();

            } catch (IOException e) {

                // Close the socket

                try {

                    mmSocket.close();

                } catch (IOException e2) {

                    Log.e(TAG, "unable to close() " + mSocketType +

                            " socket during connection failure", e2);
                   

                }

                if(!_bCanceled) {
                    Log.d("luk","connectionFailed()");
                    connectionFailed();
                }


                return;

            }



            // Reset the ConnectThread because we're done

            synchronized (BluetoothChatService.this) {

                mConnectThread = null;

            }



            // Start the connected thread
           // Toast.makeText(mContext, "connect", Toast.LENGTH_LONG).show();

            connected(mmSocket, mmDevice, mSocketType);

        }



        public void cancel() {

            try {

            	_bCanceled = true;
                mmSocket.close();
                

            } catch (IOException e) {

                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);

            }

        }

    }



    /**

     * This thread runs during a connection with a remote device.

     * It handles all incoming and outgoing transmissions.

     */

    private class ConnectedThread extends Thread {

        private final BluetoothSocket mmSocket;

        private final InputStream mmInStream;

        private final OutputStream mmOutStream;

        private boolean _bCanceled = false;

        public ConnectedThread(BluetoothSocket socket, String socketType) {

            Log.d(TAG, "create ConnectedThread: " + socketType);

            mmSocket = socket;

            InputStream tmpIn = null;

            OutputStream tmpOut = null;



            // Get the BluetoothSocket input and output streams

            try {

                tmpIn = socket.getInputStream();

                tmpOut = socket.getOutputStream();

            } catch (IOException e) {

                Log.e(TAG, "temp sockets not created", e);

            }



            mmInStream = tmpIn;

            mmOutStream = tmpOut;

        }



        public void run() {

            Log.i(TAG, "BEGIN mConnectedThread");

            byte[] buffer = new byte[1024];

            int bytes;



            // Keep listening to the InputStream while connected

            while (mState == STATE_CONNECTED) {

                try {

                    // Read from the InputStream

                	bytes = mmInStream.read(buffer);


                    Log.e("shit", "read data + "+bytes);
                    // Send the obtained bytes to the UI Activity

                    byte[] data = new byte[bytes];
                    for( int i=0; i<bytes; i++)
                    {
                    	data[i] = buffer[i];
                    }
                    mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, 0, data) // Luk modify arg2 is transfer status,0 for ok,other is error mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, 0, data) //


                            .sendToTarget();

                } catch (IOException e) {
                    mHandler.obtainMessage(Constants.MESSAGE_READ, 0, -1, null) // Luk modify arg2 is transfer status,0 for ok,other is error mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, 0, data) //


                            .sendToTarget();
                    Log.e(TAG, "disconnected", e);

                    if(!_bCanceled)
                    	connectionLost();

                    // Start the service over to restart listening mode

                    //BluetoothChatService.this.start(1);

                    break;

                }

            }

        }



        /**

         * Write to the connected OutStream.

         *

         * @param buffer The bytes to write

         */

        public int write(byte[] buffer) {

            try {

                mmOutStream.write(buffer);



                // Share the sent message back to the UI Activity

                mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)

                        .sendToTarget();

            } catch (IOException e) {

                Log.e(TAG, "Exception during write", e);

                return -1;
            }
            return 0;
        }



        public void cancel() {

            try {

            	_bCanceled = true;
                mmSocket.close();


            } catch (IOException e) {

                Log.e(TAG, "close() of connect socket failed", e);

            }

        }

    }
}