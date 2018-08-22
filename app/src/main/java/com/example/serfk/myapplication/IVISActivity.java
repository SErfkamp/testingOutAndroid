package com.example.serfk.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.serfk.myapplication.Models.IVIS;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class IVISActivity extends AppCompatActivity implements View.OnTouchListener {

    private static final String TAG = "IVISActivity";

    private ArrayList<String> mServices = new ArrayList<>();

    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mNamesTwo = new ArrayList<>();
    private ArrayList<String> mNamesThree = new ArrayList<>();
    private ArrayList<String> mNamesFour = new ArrayList<>();
    private ArrayList<String> mNamesFive = new ArrayList<>();
    private ArrayList<String> mNamesSub = new ArrayList<>();

    private int activeService = 0;
    private int[] activeParameter = {0,0,0,0,0};
    private int[][] activeValue = {{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0}};
    private int lockingMode;
    private boolean systemLocked = false;
    private View lockingBorder;
    private ImageView backButton;
    private ImageView confirmButton;
    private ImageView selectButton;
    private ImageView abortButton;

    private TextView serviceName;
    private TextView parameterName;

    private RecyclerView[] recyclerViews = new RecyclerView[5];
    private RecyclerView recyclerViewValue;

    private IVIS ivis;

    private GestureDetector gestureDetector;

    private ImageView animationHelper;
    private int  currentMargin = 480;

    // specifies which level is active: parameter = 0, value = 1
    private int activeMenuLevel = 0;

    public static final int SERVERPORT = 9020;

    public static final String SERVER_IP = "10.0.2.2";
    private ArrayList<ArrayList<String>> mParams = new ArrayList<>();


    /*ClientThread clientThread;
    Thread thread;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ivis);

        // Connect to socket
        /*clientThread = new ClientThread();
        thread = new Thread(clientThread);
        thread.start();*/

        this.lockingMode = getIntent().getIntExtra("lockingMode", 0);
        this.animationHelper = findViewById(R.id.animationHelper);
        this.initItems();

        this.backButton = findViewById(R.id.back);
        this.confirmButton = findViewById(R.id.confirm);
        
        this.serviceName = findViewById(R.id.serviceName);
        this.parameterName = findViewById(R.id.paramName);

        this.selectButton = findViewById(R.id.select);
        this.abortButton = findViewById(R.id.cancel);

        this.recyclerViewValue = findViewById(R.id.recyclerViewValue);

        this.updateServiceView();
        this.updateParameterView();

        gestureDetector = new GestureDetector(this, new OnSwipeListener(){

            @Override
            public boolean onSwipe(Direction direction) {
                if (systemLocked && lockingMode > 0) return true;

                if (direction == Direction.up) {
                    //Check if allowed to change service (first item is selected)
                    if (activeParameter[activeService] == 0) {
                        //hard coded for 5 parameters in a service
                        if (activeService < 4) {
                            activeService++;
                            updateServiceView();
                        }
                        Log.d(TAG, "onSwipe: up  - activeService " + activeService);
                        //clientThread.sendMessage("Up");
                    } else {
                        if(activeMenuLevel == 0) {
                            doReturn();
                        } else if (activeMenuLevel == 1) {
                            doAbort();
                        }
                    }
                }

                if (direction == Direction.down) {
                    if (activeParameter[activeService] == 0) {
                        if (activeService > 0) {
                            activeService--;
                            updateServiceView();
                        }
                        Log.d(TAG, "onSwipe: down  - activeService " + activeService);
                        //clientThread.sendMessage("Up");
                    } else {
                        if(activeMenuLevel == 0) {
                            doConfirm();
                        } else if (activeMenuLevel == 1) {
                            doSelect();
                        }
                    }
                }

                if (direction == Direction.left) {
                    if(activeMenuLevel == 1) {
                        int activeItemInValueList = activeValue[activeService][activeParameter[activeService]];

                        if (activeItemInValueList < mNamesSub.size()-1) {
                            activeValue[activeService][activeParameter[activeService]]++;
                            updateValueView();
                            Log.d(TAG, "onSwipe: left - activeValue: " + activeValue[activeService][activeParameter[activeService]]);
                        }
                    } else {
                        if (activeParameter[activeService] < mNames.size() - 1) {
                            if (activeParameter[activeService] == 0) fadeInConfirmAndBackButton();

                            activeParameter[activeService]++;
                            updateParameterView();
                            Log.d(TAG, "onSwipe: left - activeParameter: " + activeParameter[activeService]);
                        }
                    }

                }

                if (direction == Direction.right) {
                    if (activeMenuLevel == 1) {
                        int activeItemInValueList = activeValue[activeService][activeParameter[activeService]];

                        if (activeItemInValueList > 0) {
                            activeValue[activeService][activeParameter[activeService]]--;
                            updateValueView();
                            Log.d(TAG, "onSwipe: right - activeValue: " + activeValue[activeService][activeParameter[activeService]]);
                        }
                    } else {
                        if (activeParameter[activeService] > 0) {
                            if (activeParameter[activeService] == 1) fadeOutConfirmAndBackButton();

                            activeParameter[activeService]--;
                            updateParameterView();
                            Log.d(TAG, "onSwipe: right - activeParameter: " + activeParameter[activeService]);
                        }
                    }

                }

                return true;
            }
        });

        findViewById(R.id.MainLayout).setOnTouchListener(this);

        lockingBorder = findViewById(R.id.lockingBorder);
        lockingBorder.setVisibility(View.INVISIBLE);
    }



    /*
    ACTIONS
     */

    private void doSelect() {
        activeMenuLevel = 0;

        parameterName.setVisibility(View.INVISIBLE);

        // select button to middle
        selectButton.animate().y(625).setDuration(500).start();

        // hide abort button
        abortButton.animate().alpha(0).setDuration(500).start();

        // hide recyclerViewValue items
        recyclerViewValue.animate().alpha(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fadeInConfirmAndBackButton();
                recyclerViews[activeService].animate().alpha(100).setListener(null).setDuration(1000).start();// setVisibility(View.VISIBLE);
                resetSelectButton();
            }
        }).setDuration(500).start();
    }

    private void doAbort() {
        activeMenuLevel = 0;

        parameterName.setVisibility(View.INVISIBLE);



        // hide select button
        selectButton.animate().alpha(0).setDuration(500).start();

        // hide recyclerView items
        recyclerViewValue.animate().alpha(0).setDuration(500).start();

        // abort button to middle
        abortButton.animate().y(625).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                fadeInConfirmAndBackButton();
                recyclerViews[activeService].animate().alpha(100).setListener(null).setDuration(1000).start();// setVisibility(View.VISIBLE);
                resetAbortButton();
            }
        }).setDuration(500).start();

    }

    private void doConfirm() {

        activeMenuLevel = 1;
        recyclerViewValue.scrollToPosition(activeValue[activeService][activeParameter[activeService]]);

        // hide back button
        backButton.animate().alpha(0).setDuration(500).start();

        // hide recyclerView
        recyclerViews[activeService].animate().alpha(0).setDuration(500).start();

        // confirm button to middle
        confirmButton.animate().y(625).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                // show new recyclerView
                recyclerViewValue.animate().alpha(100).setListener(null).setDuration(500).start();


                parameterName.setVisibility(View.VISIBLE);

                fadeInSelectAndCancelButton();
                resetConfirmButton();
            }
        }).setDuration(500).start();
    }

    private void doReturn() {

        // back button to middle
        backButton.animate().y(625).setDuration(500).start();

        // hide confirm button
        confirmButton.animate().alpha(0).setDuration(500).start();

        // hide recyclerView items
        recyclerViews[activeService].animate().alpha(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                resetActiveItem();
                resetBackButton();
            }
        }).setDuration(500).start();

    }

    /*
    RESETS
     */

    public void resetActiveItem() {

        Log.d(TAG, "resetActiveItem: currentItem " + activeParameter[activeService]);
        Log.d(TAG, "resetActiveItem: currentMenu " + activeService);

        activeParameter[activeService] = 0;
        recyclerViews[activeService].scrollToPosition(0);
        recyclerViews[activeService].animate().setListener(null).alpha(100).setDuration(700).start();
    }

    private void resetConfirmButton() {
        confirmButton.animate().setListener(null).alpha(0).setDuration(20).start();
        confirmButton.animate().setListener(null).y(250).setDuration(200).start();
    }

    public void resetBackButton() {
        backButton.animate().setListener(null).alpha(0).setDuration(20).start();
        backButton.animate().setListener(null).y(950).setDuration(200).start();
    }

    private void resetSelectButton() {
        selectButton.animate().setListener(null).alpha(0).setDuration(20).start();
        selectButton.animate().setListener(null).y(250).setDuration(200).start();
    }

    private void resetAbortButton() {
        abortButton.animate().setListener(null).alpha(0).setDuration(20).start();
        abortButton.animate().setListener(null).y(950).setDuration(200).start();
    }



    /*
    ANIMATIONS
     */

    private void fadeInSelectAndCancelButton() {
        selectButton.animate().alpha(100).setDuration(300).start();
        abortButton.animate().alpha(100).setDuration(300).start();
    }

    private void fadeInConfirmAndBackButton() {
        confirmButton.animate().alpha(100).setDuration(300).start();
        backButton.animate().alpha(100).setDuration(300).start();
    }

    private void fadeOutConfirmAndBackButton() {
        confirmButton.animate().alpha(0).setDuration(300).start();
        backButton.animate().alpha(0).setDuration(300).start();
    }

    /*
    UPDATE VIEW METHODS
     */

    private void updateServiceView() {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) animationHelper.getLayoutParams();
        final int oldTopMargin = currentMargin;
        final int newTopMargin = (int) ((getResources().getDimension(R.dimen.menuHeight) - (activeService * getResources().getDimension(R.dimen.menuHeight))));

        serviceName.setText(mServices.get(activeService));

        Log.d(TAG, "updateServiceView: curentMargin: " + currentMargin + " newMargin: " + newTopMargin);


        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) animationHelper.getLayoutParams();
                // interpolate the proper value
                params.topMargin = oldTopMargin + (int) ((newTopMargin - oldTopMargin) * interpolatedTime);
                animationHelper.setLayoutParams(params);
            }
        };
        a.setDuration(300);
        animationHelper.startAnimation(a);

        currentMargin = newTopMargin;
    }

    private void updateParameterView() {
        this.parameterName.setText(mParams.get(activeService).get(activeParameter[activeService]));
        recyclerViews[activeService].smoothScrollToPosition(activeParameter[activeService]);
    }

    private void updateValueView() {
        recyclerViewValue.smoothScrollToPosition(activeValue[activeService][activeParameter[activeService]]);
    }

    /*
    LOCKING METHODS
     */
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

    /*
    TOUCH EVENTS
     */

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


    /*
    INIT DATA
     */

    private void initItems() {
        
        mServices.add("A");
        mServices.add("B");
        mServices.add("C");
        mServices.add("D");
        mServices.add("E");  
        
        mParams.add(mNames);
        mParams.add(mNamesTwo);
        mParams.add(mNamesThree);
        mParams.add(mNamesFour);
        mParams.add(mNamesFive);


        mNames.add("");
        mNames.add("A1");
        mNames.add("A2");
        mNames.add("A3");
        mNames.add("A4");
        mNames.add("A5");

        mNamesTwo.add("");
        mNamesTwo.add("B1");
        mNamesTwo.add("B2");
        mNamesTwo.add("B3");
        mNamesTwo.add("B4");
        mNamesTwo.add("B5");

        mNamesThree.add("");
        mNamesThree.add("C1");
        mNamesThree.add("C2");
        mNamesThree.add("C3");
        mNamesThree.add("C4");
        mNamesThree.add("C5");

        mNamesFour.add("");
        mNamesFour.add("D1");
        mNamesFour.add("D2");
        mNamesFour.add("D3");
        mNamesFour.add("D4");
        mNamesFour.add("D5");

        mNamesFive.add("");
        mNamesFive.add("E1");
        mNamesFive.add("E2");
        mNamesFive.add("E3");
        mNamesFive.add("E4");
        mNamesFive.add("E5");

        mNamesSub.add("O1");
        mNamesSub.add("O2");
        mNamesSub.add("O3");
        mNamesSub.add("O4");
        mNamesSub.add("O5");

        this.initRecyclerView();
    }

    private void initRecyclerView() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int parentWidth = metrics.widthPixels;
        int itemWidth = 35;

        CustomLayoutManager layoutManager = new CustomLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false, parentWidth, itemWidth, mNames.size(), 8);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(mNames, this, R.layout.layout_listitem);
        recyclerViews[0] = findViewById(R.id.recyclerView);
        recyclerViews[0].setLayoutManager(layoutManager);
        recyclerViews[0].setAdapter(adapter);
        recyclerViews[0].addItemDecoration(new CirclePagerIndicatorDecoration());
        recyclerViews[0].setHorizontalFadingEdgeEnabled(true);

        CustomLayoutManager layoutManagerSecond = new CustomLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false, parentWidth, itemWidth, mNamesTwo.size(), 8);
        RecyclerViewAdapter adapterSecond = new RecyclerViewAdapter(mNamesTwo, this, R.layout.layout_listitem);
        recyclerViews[1] = findViewById(R.id.recyclerViewSecond);
        recyclerViews[1].setLayoutManager(layoutManagerSecond);
        recyclerViews[1].setAdapter(adapterSecond);
        recyclerViews[1].addItemDecoration(new CirclePagerIndicatorDecoration());
        recyclerViews[1].setHorizontalFadingEdgeEnabled(true);

        CustomLayoutManager layoutManagerThird = new CustomLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false, parentWidth, itemWidth, mNamesThree.size(), 8);
        RecyclerViewAdapter adapterThird = new RecyclerViewAdapter(mNamesThree, this, R.layout.layout_listitem);
        recyclerViews[2] = findViewById(R.id.recyclerViewThird);
        recyclerViews[2].setLayoutManager(layoutManagerThird);
        recyclerViews[2].setAdapter(adapterThird);
        recyclerViews[2].addItemDecoration(new CirclePagerIndicatorDecoration());
        recyclerViews[2].setHorizontalFadingEdgeEnabled(true);

        CustomLayoutManager layoutManagerFour = new CustomLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false, parentWidth, itemWidth, mNamesFour.size(), 8);
        RecyclerViewAdapter adapterFour = new RecyclerViewAdapter(mNamesFour, this, R.layout.layout_listitem);
        recyclerViews[3] = findViewById(R.id.recyclerViewFour);
        recyclerViews[3].setLayoutManager(layoutManagerFour);
        recyclerViews[3].setAdapter(adapterFour);
        recyclerViews[3].addItemDecoration(new CirclePagerIndicatorDecoration());
        recyclerViews[3].setHorizontalFadingEdgeEnabled(true);

        CustomLayoutManager layoutManagerFive = new CustomLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false, parentWidth, itemWidth, mNamesFive.size(), 8);
        RecyclerViewAdapter adapterFive = new RecyclerViewAdapter(mNamesFive, this, R.layout.layout_listitem);
        recyclerViews[4] = findViewById(R.id.recyclerViewFive);
        recyclerViews[4].setLayoutManager(layoutManagerFive);
        recyclerViews[4].setAdapter(adapterFive);
        recyclerViews[4].addItemDecoration(new CirclePagerIndicatorDecoration());
        recyclerViews[4].setHorizontalFadingEdgeEnabled(true);

        CustomLayoutManager layoutManagerValue = new CustomLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false, parentWidth, itemWidth, mNamesFive.size(), 8);
        RecyclerViewAdapter adapterValue = new RecyclerViewAdapter(mNamesSub, this, R.layout.layout_listitem_value);
        recyclerViewValue = findViewById(R.id.recyclerViewValue);
        recyclerViewValue.setLayoutManager(layoutManagerValue);
        recyclerViewValue.setAdapter(adapterValue);
        recyclerViewValue.addItemDecoration(new CirclePagerIndicatorDecoration());
        recyclerViewValue.setHorizontalFadingEdgeEnabled(true);

        /*
        CustomLayoutManager layoutManagerServiceName = new CustomLayoutManager(
                this, LinearLayoutManager.VERTICAL, false, parentWidth, itemWidth, mNames.size(), 8);
        RecyclerViewAdapter adapterServiceName = new RecyclerViewAdapter(mNamesSub, this, R.layout.layout_listitem_servicename);
        recyclerViewValue = findViewById(R.id.recyclerViewServiceName);
        recyclerViewValue.setLayoutManager(layoutManagerServiceName);
        recyclerViewValue.setAdapter(adapterServiceName);
        recyclerViewValue.addItemDecoration(new CirclePagerIndicatorDecoration());
        recyclerViewValue.setHorizontalFadingEdgeEnabled(true);
*/

        /*SnapHelperOneByOne startSnapHelper = new SnapHelperOneByOne();
        startSnapHelper.attachToRecyclerView(recyclerViews[0]);*/
    }


    /*class ClientThread implements Runnable {

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
    }*/

}

