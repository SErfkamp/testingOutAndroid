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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.serfk.myapplication.Models.IVIS;
import com.example.serfk.myapplication.Models.Parameter;
import com.example.serfk.myapplication.Models.Service;
import com.example.serfk.myapplication.Network.SocketClient;
import com.rd.PageIndicatorView;

import java.util.ArrayList;

public class IVISActivity extends AppCompatActivity implements View.OnTouchListener {

    private static final String TAG = "IVISActivity";

    private View lockingBorder;
    private ImageView backButton;
    private ImageView confirmButton;
    private ImageView selectButton;
    private ImageView abortButton;

    private TextView serviceName;
    private TextView parameterName;

    private RecyclerView[] recyclerViewServices = new RecyclerView[5];
    private RecyclerView recyclerViewValues;

    private PageIndicatorView verticalIndicatorView;
    private PageIndicatorView horizontalIndicatorView;

    private GestureDetector gestureDetector;

    private ImageView animationHelper;
    private int  currentMargin = 480;
    // specifies which level is active: parameter = 0, value = 1
    private int activeMenuLevel = 0;

    public static final int SERVERPORT = 9020;
    public static final String SERVER_IP = "10.0.2.2";

    private IVIS ivis;

    private int lockDuration;

    private SocketClient socketClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ivis);

        // Connect to socket
        /*clientThread = new ClientThread();
        thread = new Thread(clientThread);
        thread.start();*/



        this.animationHelper = findViewById(R.id.animationHelper);

        this.backButton = findViewById(R.id.back);
        this.confirmButton = findViewById(R.id.confirm);
        
        this.serviceName = findViewById(R.id.serviceName);
        this.parameterName = findViewById(R.id.paramName);

        this.selectButton = findViewById(R.id.select);
        this.abortButton = findViewById(R.id.cancel);

        this.verticalIndicatorView = findViewById(R.id.verticalIndicatorView);
        verticalIndicatorView.setSelection(0);

        this.horizontalIndicatorView = findViewById(R.id.horizontalIndicatorView);
        horizontalIndicatorView.setSelection(0);

        this.recyclerViewValues = findViewById(R.id.recyclerViewValue);

        lockingBorder = findViewById(R.id.lockingBorder);
        lockingBorder.setVisibility(View.INVISIBLE);

        this.initIVIS(getIntent().getIntExtra("lockingMode", 0));
        this.initRecyclerView();
        this.updateServiceView();
        this.updateParameterView();

        lockDuration = this.getIntent().getIntExtra("lockingDuration", 1000);

        String ip = getResources().getString(R.string.ip);
        int port = getResources().getInteger(R.integer.port);

        socketClient = new SocketClient(ip, port, this);
        socketClient.execute();

        gestureDetector = new GestureDetector(this, new OnSwipeListener(){

            @Override
            public boolean onSwipe(Direction direction) {
                if (ivis.isLocked() && ivis.getLockingMode() > 0) return true;

                int activeServiceIndex = ivis.getActiveServiceIndex();
                int activeParameterIndexForService = ivis.getActiveService().getActiveParameterIndex();
                int activeValueIndexForParameter = ivis.getActiveService().getActiveParameter().getActiveValueIndex();

                if (direction == Direction.up) {
                    if (activeParameterIndexForService == 0) {
                        if ( !(activeServiceIndex == ivis.nextService())) updateServiceView();

                        Log.d(TAG, "onSwipe: up  - activeService: " + ivis.getActiveServiceIndex());

                        //clientThread.sendMessage("Up");
                    } else {
                        if (activeMenuLevel == 0) {
                            doReturn();
                        } else if (activeMenuLevel == 1) {
                            doAbort();
                        }
                    }
                }

                if (direction == Direction.down) {

                    if (activeParameterIndexForService == 0) {

                        if ( !(activeServiceIndex == ivis.previousService())) updateServiceView();

                        Log.d(TAG, "onSwipe: down  - activeService: " + ivis.getActiveServiceIndex());

                        //clientThread.sendMessage("Down");
                    } else {
                        if (activeMenuLevel == 0) {
                            doConfirm();
                        } else if (activeMenuLevel == 1) {
                            doSelect();
                        }
                    }
                }

                if (direction == Direction.left) {
                    if(activeMenuLevel == 0) {
                        if (activeParameterIndexForService == 0) {
                            showConfirmAndBackButton();
                            hideVerticalIndicatorView();
                        }

                        if ( !(activeParameterIndexForService == ivis.getActiveService().nextParameter())) updateParameterView();

                        Log.d(TAG, "onSwipe: left - activeParameter: " + ivis.getActiveService().getActiveParameterIndex());

                        //clientThread.sendMessage("Left");
                    } else {
                        if ( !(activeValueIndexForParameter == ivis.getActiveService().getActiveParameter().nextValue())) updateValueView();

                        Log.d(TAG, "onSwipe: left - activeValue: " + ivis.getActiveService().getActiveParameter().getActiveValueIndex());

                        //clientThread.sendMessage("Left");
                    }

                }

                if (direction == Direction.right) {
                    if(activeMenuLevel == 0) {
                        if (activeParameterIndexForService == 1) {
                            hideConfirmAndBackButton();
                            showVerticalIndicatorView();
                        }

                        if ( !(activeParameterIndexForService == ivis.getActiveService().previousParameter())) updateParameterView();

                        Log.d(TAG, "onSwipe: right - activeParameter: " + ivis.getActiveService().getActiveParameter().getActiveValueIndex());

                        //clientThread.sendMessage("Right");
                    } else {
                        if ( !(activeValueIndexForParameter == ivis.getActiveService().getActiveParameter().previousValue())) updateValueView();

                        Log.d(TAG, "onSwipe: right - activeValue: " + ivis.getActiveService().getActiveParameter().getActiveValueIndex());

                        //clientThread.sendMessage("Right");
                    }

                }

                return true;
            }
        });

        findViewById(R.id.MainLayout).setOnTouchListener(this);

    }


    /*
    ACTIONS
     */

    // Confirming means to chose a parameter in a service
    private void doConfirm() {
        activeMenuLevel = 1;

        // scroll to value
        int activeValueIndex = ivis.getActiveService().getActiveParameter().getActiveValueIndex();
        recyclerViewValues.scrollToPosition(activeValueIndex);

        updateHorizontalIndicator();

        // hide back button
        backButton.setAlpha(0f);

        // confirm button to middle
        confirmButton.animate().y(getResources().getDimension(R.dimen.middle)).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                // show new recyclerView
                recyclerViewValues.setAlpha(1f);
                // hide recyclerView
                recyclerViewServices[ivis.getActiveServiceIndex()].setAlpha(0f);// .animate().alpha(0).setDuration(100).start();

                // hide parameter Name
                parameterName.setVisibility(View.VISIBLE);

                showSelectAndCancelButton();
                resetConfirmButton();

            }
        }).setDuration(500).start();
    }

    private void doReturn() {

        ivis.getActiveService().setActiveParameterIndex(0);
        showVerticalIndicatorView();
        updateHorizontalIndicator();

        // hide confirm button
        confirmButton.setAlpha(0f); //.animate().alpha(0).setDuration(100).start();

        //recyclerViewServices[ivis.getActiveServiceIndex()].animate().alpha(0).setDuration(500).start();// .setAlpha(0f);

        // back button to middle
        backButton.animate().y(getResources().getDimension(R.dimen.middle)).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                recyclerViewServices[ivis.getActiveServiceIndex()].setAlpha(0f);

                resetActiveItem();
                resetBackButton();

            }
        }).setDuration(500).start();

    }

    // Choosing a value of a parameter is selecting
    private void doSelect() {
        activeMenuLevel = 0;

        updateHorizontalIndicator();
        parameterName.setVisibility(View.INVISIBLE);

        // hide abort button
        abortButton.setAlpha(0f);// .animate().alpha(0).setDuration(100).start();

        // select button to middle
        selectButton.animate().y(getResources().getDimension(R.dimen.middle)).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                recyclerViewValues.setAlpha(0f);
                recyclerViewServices[ivis.getActiveServiceIndex()].setAlpha(1f);

                showConfirmAndBackButton();
                resetSelectButton();

            }
        }).setDuration(500).start();

        this.lockIvis();
    }

    // Aborting means to cancel choosing a value of a parameter
    private void doAbort() {
        activeMenuLevel = 0;
        final int activeServiceIndex = ivis.getActiveServiceIndex();

        updateHorizontalIndicator();
        parameterName.setVisibility(View.INVISIBLE);

        // hide select button
        selectButton.setAlpha(0f);

        // hide recyclerView items
        //recyclerViewValues.animate().alpha(0).setDuration(100).start();

        // abort button to middle
        abortButton.animate().y(getResources().getDimension(R.dimen.middle)).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                showConfirmAndBackButton();
                hideSelectAndCancelButton();

                recyclerViewValues.setAlpha(0f);
                recyclerViewServices[activeServiceIndex].setAlpha(1f);

                resetAbortButton();
            }
        }).setDuration(500).start();

    }


    /*
    RESETS
     */

    public void resetActiveItem() {
        int activeServiceIndex = ivis.getActiveServiceIndex();

        ivis.getActiveService().setActiveParameterIndex(0);

        recyclerViewServices[activeServiceIndex].scrollToPosition(0);
        recyclerViewServices[activeServiceIndex].setAlpha(1f);
    }

    private void resetConfirmButton() {
        confirmButton.animate().setListener(null);
        confirmButton.setAlpha(0f);
        confirmButton.setY(getResources().getDimension(R.dimen.upperButton));
    }

    public void resetBackButton() {
        backButton.animate().setListener(null);
        backButton.setAlpha(0f);
        backButton.setY(getResources().getDimension(R.dimen.lowerButton));

    }

    private void resetSelectButton() {
        selectButton.animate().setListener(null);
        selectButton.setAlpha(0f);
        selectButton.setY(getResources().getDimension(R.dimen.upperButton));
    }

    private void resetAbortButton() {
        abortButton.animate().setListener(null);
        abortButton.setAlpha(0f);
        abortButton.setY(getResources().getDimension(R.dimen.lowerButton));
    }



    /*
    ANIMATIONS
     */
    private void showVerticalIndicatorView() {
        verticalIndicatorView.setAlpha(1f);
    }
    private void hideVerticalIndicatorView() {
        verticalIndicatorView.setAlpha(0f);
    }


    private void showConfirmAndBackButton() {
        confirmButton.setAlpha(1f);
        backButton.setAlpha(1f);
    }

    private void hideConfirmAndBackButton() {
        confirmButton.setAlpha(0f);
        backButton.setAlpha(0f);
    }

    private void showSelectAndCancelButton() {
        selectButton.setAlpha(1f);
        abortButton.setAlpha(1f);
    }

    private void hideSelectAndCancelButton() {
        selectButton.setAlpha(0f);
        abortButton.setAlpha(0f);
    }


    private void animateServiceRecyclerViews() {

        // The helpers object Top Margin is used to animate the cycling through the services
        // Margin of the dummy item increases -> recyclerViews will get pushed down
        final int oldTopMargin = currentMargin;
        final int newTopMargin = (int) ((getResources().getDimension(R.dimen.menuHeight) - (ivis.getActiveServiceIndex() * getResources().getDimension(R.dimen.menuHeight))));

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

    /*
    UPDATE VIEW METHODS
     */

    private void updateHorizontalIndicator() {
        int newCount = activeMenuLevel == 0? ivis.getActiveService().getParameterCount() :  ivis.getActiveService().getActiveParameter().getValueCount();
        int newIndex = activeMenuLevel == 0? ivis.getActiveService().getActiveParameterIndex() : ivis.getActiveService().getActiveParameter().getActiveValueIndex();
        horizontalIndicatorView.setCount(newCount);
        horizontalIndicatorView.setSelection(newIndex);
    }

    private void updateServiceView() {
        final TextView tv_one = findViewById(R.id.tv_one);
        TextView tv_two = findViewById(R.id.tv_two);

        int activeServiceIndex = ivis.getActiveServiceIndex();
        final int newMargin = activeServiceIndex * -30;

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tv_one.getLayoutParams();
                // interpolate the proper value
                params.topMargin = (int) (newMargin * interpolatedTime);
                tv_one.setLayoutParams(params);
            }
        };
        a.setDuration(300);
        animationHelper.startAnimation(a);

        updateHorizontalIndicator();
        verticalIndicatorView.setSelection(activeServiceIndex);

        serviceName.setText(ivis.getActiveService().getLabel());
        this.animateServiceRecyclerViews();
    }

    private void updateParameterView() {
        int activeParameterIndex = ivis.getActiveService().getActiveParameterIndex();
        int activeServiceIndex = ivis.getActiveServiceIndex();

        horizontalIndicatorView.setSelection(activeParameterIndex);

        this.parameterName.setText(ivis.getActiveService().getActiveParameter().getLabel());
        recyclerViewServices[activeServiceIndex].smoothScrollToPosition(activeParameterIndex);
    }

    private void updateValueView() {
        int activeValueIndex = ivis.getActiveService().getActiveParameter().getActiveValueIndex();

        updateHorizontalIndicator();

        recyclerViewValues.smoothScrollToPosition(activeValueIndex);
    }

    /*
    LOCKING METHODS
     */

    public boolean lockIvis() {

        Log.d(TAG, "lock IVIS");

        if(ivis.isLocked()) {
            return false;
        } else {
            Log.d(TAG,"send lock");
            socketClient.sendDataToNetwork("ivis_locked");

            ivis.lock();

            lockingBorder.setVisibility(View.VISIBLE);

            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            unlockIvis();
                        }
                    }, lockDuration);
            return true;
        }
    }

    public void unlockIvis() {
        Log.d(TAG, "unlock IVIS");

        if(ivis.isLocked()) {
            Log.d(TAG,"send unlock");

            socketClient.sendDataToNetwork("ivis_unlocked");
            lockingBorder.setVisibility(View.INVISIBLE);
            ivis.unlock();
        }
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
    public boolean dispatchTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    /*
    INIT DATA
     */

    private void initIVIS(int lockingMode) {

        // SERVICES TO CREATE

        int PARAMS_FOR_SERVICES = 5;
        int VALUES_FOR_PARAMS = 5;

        String[] SERVICE_NAMES = {"A","B","C","D","E"};

        ArrayList<Service> newServices = new ArrayList<>();

        for (int i = 0; i < SERVICE_NAMES.length; i++) {
            ArrayList<Parameter> tempParams = new ArrayList<>();

            for (int j = 0; j < PARAMS_FOR_SERVICES; j++) {
                Parameter tempParam = new Parameter(SERVICE_NAMES[i] + (j+1));
                ArrayList<String> values = new ArrayList<>();

                for (int k = 0; k < VALUES_FOR_PARAMS; k++) {
                    values.add("O" + (k+1));
                }
                tempParam.setValues(values);
                tempParams.add(tempParam);
            }

            Service tempService = new Service(tempParams, SERVICE_NAMES[i]);
            newServices.add(tempService);
        }

        this.ivis = new IVIS(newServices,lockingMode);
    }

    /*
    INIT RECYLCER VIEWS
     */

    private void initRecyclerView() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int parentWidth = metrics.widthPixels;
        int itemWidth = (int) getResources().getDimension(R.dimen.itemWidth);

        //right now only works for same parameter length for every service
        verticalIndicatorView.setCount(ivis.getServiceCount()); // specify total count of indicators
        horizontalIndicatorView.setCount(ivis.getActiveService().getParameterCount());

        for (int i = 0; i < ivis.getServiceCount() ; i++) {

            ArrayList<String> params = ivis.getServices().get(i).getParametersAsStringArray();

            CustomLayoutManager layoutManager = new CustomLayoutManager(
                    this, LinearLayoutManager.HORIZONTAL, false, parentWidth, itemWidth, ivis.getServiceCount(), 8);
            RecyclerViewAdapter adapter = new RecyclerViewAdapter(params, this, R.layout.layout_listitem);

            LinearLayout servicesLayout = findViewById(R.id.MainLayout);

            // i+1 because the servicesLayout also hold the animationHelper as child
            recyclerViewServices[i] = (RecyclerView) servicesLayout.getChildAt(i+1);
            recyclerViewServices[i].setLayoutManager(layoutManager);
            recyclerViewServices[i].setAdapter(adapter);
            recyclerViewServices[i].setHorizontalFadingEdgeEnabled(true);
            //SnapHelperOneByOne startSnapHelper = new SnapHelperOneByOne();
            //startSnapHelper.attachToRecyclerView(recyclerViewServices[i]);

        }

        ArrayList<String> values = ivis.getActiveService().getParameters().get(1).getValues();

        CustomLayoutManager layoutManagerValue = new CustomLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false, parentWidth, itemWidth, values.size(), 8);
        RecyclerViewAdapter adapterValue = new RecyclerViewAdapter(values, this, R.layout.layout_listitem_value);
        recyclerViewValues = findViewById(R.id.recyclerViewValue);
        recyclerViewValues.setLayoutManager(layoutManagerValue);
        recyclerViewValues.setAdapter(adapterValue);
        recyclerViewValues.setHorizontalFadingEdgeEnabled(true);
        //SnapHelperOneByOne startSnapHelper = new SnapHelperOneByOne();
        //startSnapHelper.attachToRecyclerView(recyclerViewValues);
    }


    public int getLockDuration() {
        return lockDuration;
    }

    public void setLockDuration(int lockDuration) {
        this.lockDuration = lockDuration;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socketClient.cancel(true); //In case the task is currently running
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

