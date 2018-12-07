package com.example.serfk.myapplication.Network;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Switch;

import com.example.serfk.myapplication.IVISActivity;
import com.example.serfk.myapplication.Models.IVIS;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;


//https://stackoverflow.com/q/5135438/4311829
public class SocketClient extends AsyncTask<Void, byte[], Boolean> {
    private static final String TAG = "SocketClient";
    Socket nsocket; //Network Socket
    InputStream nis; //Network Input Stream
    //OutputStream nos; //Network Output Stream
    PrintWriter nos; //Network Output Stream
    IVISActivity ivisActivity;
    String ip;
    int port;

    public SocketClient(String ip, int port, IVISActivity ivisActivity) {
        this.ivisActivity = ivisActivity;
        this.ip = ip;
        this.port = port;
    }

    @Override
    protected void onPreExecute() {
        Log.i("AsyncTask", "onPreExecute");
    }

    @Override
    protected Boolean doInBackground(Void... params) { //This runs on a different thread
        boolean result = false;
        try {
            Log.i("AsyncTask", "doInBackground: Creating socket");
            SocketAddress sockaddr = new InetSocketAddress( ip, port);
            nsocket = new Socket();
            nsocket.connect(sockaddr, 5000); //5 second connection timeout
            if (nsocket.isConnected()) {
                nis = nsocket.getInputStream();
                nos = new PrintWriter(nsocket.getOutputStream());

                this.sendDataToNetwork("lockingMode_"+ivisActivity.getIvis().getLockingMode());
                this.sendDataToNetwork("lockingDuration_"+ivisActivity.getLockingDuration());
                this.sendDataToNetwork("maxInteractionDuration_"+ivisActivity.getMaxInteractionDuration());
                this.sendDataToNetwork("actionsForLocking_"+ivisActivity.getInteractionsForLock());
                this.sendDataToNetwork("resetInputAfter_"+ivisActivity.getResetInteractionTime());

                Log.i(TAG, "doInBackground: Socket created, streams assigned");
                Log.i(TAG, "doInBackground: Waiting for inital data...");
                byte[] buffer = new byte[4096];
                int read = nis.read(buffer, 0, 4096); //This is blocking
                while(read != -1){
                    byte[] tempdata = new byte[read];
                    System.arraycopy(buffer, 0, tempdata, 0, read);
                    publishProgress(tempdata);
                    Log.i(TAG, "doInBackground: Got some data");
                    read = nis.read(buffer, 0, 4096); //This is blocking
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "doInBackground: IOException");
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "doInBackground: Exception");
            result = true;
        } finally {
            try {
                nis.close();
                nos.close();
                nsocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i(TAG, "doInBackground: Finished");
        }
        return result;
    }

    public boolean sendDataToNetwork(final String msg)
    {
        if (nsocket.isConnected())
        {
            Log.i(TAG, "SendDataToNetwork: Writing received message to socket");
            new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        nos.println(msg);
                        //nos.write(msg.getBytes());
                        nos.flush();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "SendDataToNetwork: Message send failed. Caught an exception");
                    }
                }
            }).start();

            return true;
        }

        Log.i(TAG, "SendDataToNetwork: Cannot send message. Socket is closed");
        return false;
    }
    //-65957
    //65801
    //66091

    @Override
    protected void onProgressUpdate(byte[]... values) {
        if (values.length > 0) {
            String msg = new String(values[0]);
            Log.i("AsyncTask", "onProgressUpdate: " + values[0].length + " bytes received.");
            Log.d("AsyncTask", msg);
            switch(msg) {
                case "lock_ivis":
                    ivisActivity.lockIvis();
                    break;
                case "unlock_ivis":
                    ivisActivity.unlockIvis();
                    break;
            }
        }
    }
    @Override
    protected void onCancelled() {
        Log.i("AsyncTask", "Cancelled.");
    }
    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Log.i("AsyncTask", "onPostExecute: Completed with an Error.");
        } else {
            Log.i("AsyncTask", "onPostExecute: Completed.");
        }
    }

}
