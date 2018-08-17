package com.example.serfk.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class IVISActivity extends AppCompatActivity implements View.OnTouchListener {

    private static final String TAG = "IVISActivity";

    private ArrayList<String> mNames = new ArrayList<>();

    private int activeMenu = 0;
    private int[] activeItem = {0,0,0};
    private int  currentMargin = 10;
    private int lockingMode;
    private boolean systemLocked = false;
    private View lockingBorder;

    private RecyclerView[] recyclerViews = new RecyclerView[3];

    private GestureDetector gestureDetector;

    public static final int SERVERPORT = 9020;

    public static final String SERVER_IP = "10.0.2.2";
    ClientThread clientThread;
    Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ivis);

        // Connect to socket
        clientThread = new ClientThread();
        thread = new Thread(clientThread);
        thread.start();

        this.lockingMode = getIntent().getIntExtra("lockingMode", 0);

        this.initItems();
        this.updateActiveMenu();

        gestureDetector = new GestureDetector(this, new OnSwipeListener(){

            @Override
            public boolean onSwipe(Direction direction) {
                if (systemLocked && lockingMode > 0) return true;

                if (direction == Direction.up) {
                    if (activeMenu < 2) activeMenu++;
                    updateActiveMenu();
                    Log.d(TAG, "onSwipe: up  - activeMenu " + activeMenu);
                    clientThread.sendMessage("Up");
                }

                if (direction == Direction.down) {
                    if (activeMenu > 0) activeMenu--;
                    updateActiveMenu();
                    Log.d(TAG, "onSwipe: down - activeMenu " + activeMenu);
                    clientThread.sendMessage("Down");
                }

                if (direction == Direction.left) {
                    if (activeItem[activeMenu] < mNames.size()-1) activeItem[activeMenu]++;
                    updateMenuItem();
                    Log.d(TAG, "onSwipe: left - activeMenuItem: " +activeItem[activeMenu]);

                }

                if (direction == Direction.right) {
                    if (activeItem[activeMenu] > 0) activeItem[activeMenu]--;
                    updateMenuItem();
                    Log.d(TAG, "onSwipe: right - activeMenuItem: " +activeItem[activeMenu]);
                }
                return true;
            }
        });

        findViewById(R.id.MainLayout).setOnTouchListener(this);

        lockingBorder = findViewById(R.id.lockingBorder);
        lockingBorder.setVisibility(View.INVISIBLE);
    }

    public void lockSystem() {
        lockingBorder.setVisibility(View.VISIBLE);
        systemLocked = true;
    }

    public void unlockSystem() {
        lockingBorder.setVisibility(View.INVISIBLE);
        systemLocked = false;
    }

    public boolean isLocked() {
        return systemLocked;
    }

    public void updateMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Message: " + message);
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return true;
    }


    private void updateMenuItem() {
        recyclerViews[activeMenu].smoothScrollToPosition(activeItem[activeMenu]);
    }

    private void updateActiveMenu() {
        //animate both shapes to new line
        final int newTopMargin = (int) (activeMenu == 0 ? getResources().getDimension(R.dimen.first)
                : activeMenu == 1 ? getResources().getDimension(R.dimen.second)
                : getResources().getDimension(R.dimen.third));

        final ImageView activeItemView = findViewById(R.id.activeItem);
        final ImageView activeMenuView = findViewById(R.id.activeMenu);

        activeMenuView.animate().y(newTopMargin-20).setDuration(500).start();
        activeItemView.animate().y(newTopMargin).setDuration(500).start();

        currentMargin = newTopMargin;
    }

    private void initItems() {
        mNames.add("A");
        mNames.add("B");
        mNames.add("C");
        mNames.add("D");
        mNames.add("E");
        mNames.add("F");
        mNames.add("G");
        mNames.add("H");
        mNames.add("I");
        mNames.add("J");
        mNames.add("K");
        mNames.add("L");
        mNames.add("M");

        this.initRecyclerView();
    }

    private void initRecyclerView() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int parentWidth = metrics.widthPixels;
        int itemWidth = 25;

        CustomLayoutManager layoutManager = new CustomLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false, parentWidth, itemWidth, mNames.size(), 8);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(mNames, this);
        recyclerViews[0] = findViewById(R.id.recyclerView);
        recyclerViews[0].setLayoutManager(layoutManager);
        recyclerViews[0].setAdapter(adapter);
        recyclerViews[0].addItemDecoration(new CirclePagerIndicatorDecoration());
        recyclerViews[0].setHorizontalFadingEdgeEnabled(true);

        CustomLayoutManager layoutManagerSecond = new CustomLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false, parentWidth, itemWidth, mNames.size(), 8);
        RecyclerViewAdapter adapterSecond = new RecyclerViewAdapter(mNames, this);
        recyclerViews[1] = findViewById(R.id.recyclerViewSecond);
        recyclerViews[1].setLayoutManager(layoutManagerSecond);
        recyclerViews[1].setAdapter(adapterSecond);
        recyclerViews[1].addItemDecoration(new CirclePagerIndicatorDecoration());
        recyclerViews[1].setHorizontalFadingEdgeEnabled(true);


        CustomLayoutManager layoutManagerThird = new CustomLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false, parentWidth, itemWidth, mNames.size(), 8);
        RecyclerViewAdapter adapterThird = new RecyclerViewAdapter(mNames, this);
        recyclerViews[2] = findViewById(R.id.recyclerViewThird);
        recyclerViews[2].setLayoutManager(layoutManagerThird);
        recyclerViews[2].setAdapter(adapterThird);
        recyclerViews[2].addItemDecoration(new CirclePagerIndicatorDecoration());
        recyclerViews[2].setHorizontalFadingEdgeEnabled(true);


        SnapHelperOneByOne startSnapHelper = new SnapHelperOneByOne();
        startSnapHelper.attachToRecyclerView(recyclerViews[0]);
    }


    class ClientThread implements Runnable {

        private Socket socket;
        private BufferedReader input;

        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVERPORT);

                while (!Thread.currentThread().isInterrupted()) {

                    Log.i(TAG, "Waiting for message from server...");

                    this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String message = input.readLine();
                    Log.i(TAG, "Message received from the server : " + message);

                    if (null == message || "Disconnect".contentEquals(message)) {
                        Thread.interrupted();
                        message = "Server Disconnected.";
                        updateMessage(getTime() + " | Server : " + message);
                        break;
                    }

                    updateMessage(getTime() + " | Server : " + message);

                }

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

        void sendMessage(String message) {
            try {
                if (null != socket) {
                    PrintWriter out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())),
                            true);
                    out.println(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }

    }

    String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != clientThread) {
            clientThread.sendMessage("Disconnect");
            clientThread = null;
        }
    }

}

