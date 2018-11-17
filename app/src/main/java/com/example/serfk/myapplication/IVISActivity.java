package com.example.serfk.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import java.util.Date;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
import java.util.stream.Collectors;

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
    private RecyclerView recyclerViewServiceNames;

    private TextView[] serviceNameTextViews = new TextView[5];


    private PageIndicatorView verticalIndicatorView;
    private PageIndicatorView horizontalIndicatorView;

    private GestureDetector gestureDetector;

    private ImageView animationHelper;
    private int  currentMargin = 480;
    // specifies which level is active: parameter = 0, value = 1
    private int activeMenuLevel = 0;

    private IVIS ivis;

    private int maxInteractionDuration;

    private SocketClient socketClient;
    private int lockingDuration;

    private boolean isInteracting = false;
    private int currentInputs = 0;
    private long prevInteractionTimestamp = 0;
    private int resetInteractionTime = 1000;
    private int interactionsForLock = 3;
    private int lockingAfter = 500;

    public int getLockingAfter() {
        return lockingAfter;
    }

    public int getResetInteractionTime() {
        return resetInteractionTime;
    }

    public int getInteractionsForLock() {
        return interactionsForLock;
    }

    public int getMaxInteractionDuration() {
        return maxInteractionDuration;
    }

    public int getLockingDuration() {
        return lockingDuration;
    }

    public void setMaxInteractionDuration(int maxInteractionDuration) {
        this.maxInteractionDuration = maxInteractionDuration;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ivis);

        serviceNameTextViews[0] = findViewById(R.id.tv_one);
        serviceNameTextViews[1] = findViewById(R.id.tv_two);
        serviceNameTextViews[2] = findViewById(R.id.tv_three);
        serviceNameTextViews[3] = findViewById(R.id.tv_four);
        serviceNameTextViews[4] = findViewById(R.id.tv_five);


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

        this.recyclerViewServiceNames = findViewById(R.id.recyclerViewServiceName);
        this.recyclerViewServiceNames.setLayoutManager(new RecyclerView.LayoutManager() {
            @Override
            public RecyclerView.LayoutParams generateDefaultLayoutParams() {
                return null;
            }
        });

        lockingBorder = findViewById(R.id.lockingBorder);
        lockingBorder.setVisibility(View.INVISIBLE);

        this.initIVIS(getIntent().getIntExtra("lockingMode", 0));
        this.initRecyclerView();
        this.updateServiceView();
        this.updateParameterView();

        maxInteractionDuration = this.getIntent().getIntExtra("interactionTime", 1500);
        resetInteractionTime = this.getIntent().getIntExtra("resetInteractionTime", 1000);
        interactionsForLock = this.getIntent().getIntExtra("interactionsForLock", 3);
        lockingDuration = this.getIntent().getIntExtra("lockingDuration", 1500);
        lockingAfter = this.getIntent().getIntExtra("lockingAfter", 1500);


        String ip = getResources().getString(R.string.ip);
        int port = getResources().getInteger(R.integer.port);

        socketClient = new SocketClient(ip, port, this);
        socketClient.execute();

        gestureDetector = new GestureDetector(this, new OnSwipeListener(){

            @Override
            public boolean onSwipe(Direction direction) {
                if (ivis.isLocked() && ivis.getLockingMode() > 0) return true;

                //if (ivis.getLockingMode() < 4) lockingHandler();

                int activeServiceIndex = ivis.getActiveServiceIndex();
                int activeParameterIndexForService = ivis.getActiveService().getActiveParameterIndex();
                int activeValueIndexForParameter = ivis.getActiveService().getActiveParameter().getActiveValueIndex();

                String msg = "";

                if (direction == Direction.up) {
                    if (activeParameterIndexForService == 0) {
                        if ( !(activeServiceIndex == ivis.nextService())) {
                            msg += "service-up";
                            updateServiceView();
                        } else {
                            msg += "service-up-error";
                        }

                        Log.d(TAG, "onSwipe: up  - activeService: " + ivis.getActiveServiceIndex());

                    } else {
                        if (activeMenuLevel == 0) {
                            msg += "return";
                            doReturn();
                        } else if (activeMenuLevel == 1) {
                            msg += "abort";
                            doAbort();
                        }
                    }
                }

                else if (direction == Direction.down) {

                    if (activeParameterIndexForService == 0) {

                        if ( !(activeServiceIndex == ivis.previousService())) {
                            msg += "service-down";
                            updateServiceView();
                        } else {
                            msg += "service-down-error-";
                        }

                        Log.d(TAG, "onSwipe: down  - activeService: " + ivis.getActiveServiceIndex());

                    } else {
                        if (activeMenuLevel == 0) {
                            msg += "confirm";
                            doConfirm();
                        } else if (activeMenuLevel == 1) {
                            msg += "select";
                            doSelect();
                        }
                    }
                }

                else if (direction == Direction.left) {
                    if(activeMenuLevel == 0) {
                        if (activeParameterIndexForService == 0) {
                            showConfirmAndBackButton();
                            hideVerticalIndicatorView();
                        }

                        if ( !(activeParameterIndexForService == ivis.getActiveService().nextParameter())) {
                            msg += "param-left";
                            updateParameterView();
                        } else {
                            msg += "param-left-error-";
                        }

                        Log.d(TAG, "onSwipe: left - activeParameter: " + ivis.getActiveService().getActiveParameterIndex());

                    } else {
                        if ( !(activeValueIndexForParameter == ivis.getActiveService().getActiveParameter().nextValue())) {
                            msg += "value-left";

                            updateValueView();
                        } else {
                            msg += "value-left-error-";

                        }

                        Log.d(TAG, "onSwipe: left - activeValue: " + ivis.getActiveService().getActiveParameter().getActiveValueIndex());
                    }

                }

                else if (direction == Direction.right) {
                    if(activeMenuLevel == 0) {
                        if (activeParameterIndexForService == 1) {
                            hideConfirmAndBackButton();
                            showVerticalIndicatorView();
                        }

                        if ( !(activeParameterIndexForService == ivis.getActiveService().previousParameter())) {
                            msg += "param-right";
                            updateParameterView();
                        } else {
                            msg += "param-right-error";
                        }

                        Log.d(TAG, "onSwipe: right - activeParameter: " + ivis.getActiveService().getActiveParameterIndex());

                    } else {
                        if ( !(activeValueIndexForParameter == ivis.getActiveService().getActiveParameter().previousValue())) {
                            msg += "value-right";
                            updateValueView();
                        } else {
                            msg += "value-right-error";
                        }

                        Log.d(TAG, "onSwipe: right - activeValue: " + ivis.getActiveService().getActiveParameter().getActiveValueIndex());
                    }

                }

                socketClient.sendDataToNetwork(msg+"-"+ivis.getActiveServiceIndex()
                        +"-"+ivis.getActiveService().getActiveParameterIndex()
                        +"-"+ivis.getActiveService().getActiveParameter().getActiveValueIndex());

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

        final int activeServiceIndex = ivis.getActiveServiceIndex();

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
                recyclerViewServices[activeServiceIndex].setAlpha(0f);

                // hide parameter Name
                parameterName.setVisibility(View.VISIBLE);

                showSelectAndCancelButton();
                resetConfirmButton();

                // hide back button
                backButton.setAlpha(0f);

            }
        }).setDuration(500).start();
    }

    private void doReturn() {

        final int activeServiceIndex = ivis.getActiveServiceIndex();

        ivis.getActiveService().setActiveParameterIndex(0);
        showVerticalIndicatorView();
        updateHorizontalIndicator();

        // hide confirm button
        Log.d(TAG, "HIDE confirmButton");
        confirmButton.setAlpha(0f); //.animate().alpha(0).setDuration(100).start();

        // back button to middle
        backButton.animate().y(getResources().getDimension(R.dimen.middle)).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                Log.d(TAG, "recyclerView Alpha 0 - " + activeServiceIndex);
                recyclerViewServices[activeServiceIndex].setAlpha(0f);

                resetActiveItem(activeServiceIndex);
                resetBackButton();

                confirmButton.setAlpha(0f); //.animate().alpha(0).setDuration(100).start();

            }
        }).setDuration(500).start();

    }

    // Choosing a value of a parameter is selecting
    private void doSelect() {
        activeMenuLevel = 0;

        final int activeServiceIndex = ivis.getActiveServiceIndex();

        updateHorizontalIndicator();
        parameterName.setVisibility(View.INVISIBLE);

        // hide abort button
        Log.d(TAG, "HIDE abortButton");
        abortButton.setAlpha(0f);// .animate().alpha(0).setDuration(100).start();

        // select button to middle
        selectButton.animate().y(getResources().getDimension(R.dimen.middle)).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                recyclerViewValues.setAlpha(0f);
                recyclerViewServices[activeServiceIndex].setAlpha(1f);

                showConfirmAndBackButton();
                resetSelectButton();

                parameterName.setVisibility(View.INVISIBLE);

                // hide abort button
                Log.d(TAG, "HIDE abortButton");
                abortButton.setAlpha(0f);// .animate().alpha(0).setDuration(100).start();

            }
        }).setDuration(500).start();

    }

    // Aborting means to cancel choosing a value of a parameter
    private void doAbort() {
        activeMenuLevel = 0;
        final int activeServiceIndex = ivis.getActiveServiceIndex();

        updateHorizontalIndicator();
        parameterName.setVisibility(View.INVISIBLE);

        // hide select button
        Log.d(TAG, "HIDE selectButton");
        selectButton.setAlpha(0f);

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

                // hide select button
                Log.d(TAG, "HIDE selectButton");
                selectButton.setAlpha(0f);

            }
        }).setDuration(500).start();

    }

    /*
    RESETS
     */

    public void resetActiveItem(int activeServiceIndex) {

        ivis.getActiveService().setActiveParameterIndex(0);

        recyclerViewServices[activeServiceIndex].scrollToPosition(0);
        recyclerViewServices[activeServiceIndex].setAlpha(1f);
        Log.d(TAG,"resetActiveItem: " + activeServiceIndex);
    }

    private void resetConfirmButton() {
        Log.d(TAG, "resetConfirmButton");

        confirmButton.animate().setListener(null);
        confirmButton.setAlpha(0f);
        confirmButton.setY(getResources().getDimension(R.dimen.upperButton));
    }

    public void resetBackButton() {
        Log.d(TAG, "resetBackButton");

        backButton.animate().setListener(null);
        backButton.setAlpha(0f);
        backButton.setY(getResources().getDimension(R.dimen.lowerButton));

    }

    private void resetSelectButton() {
        Log.d(TAG, "resetSelectButton");

        selectButton.animate().setListener(null);
        selectButton.setAlpha(0f);
        selectButton.setY(getResources().getDimension(R.dimen.upperButton));
    }

    private void resetAbortButton() {
        Log.d(TAG, "resetAbortButton");

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
        Log.d(TAG, "showConfirmAndBackButton");

        confirmButton.setAlpha(1f);
        backButton.setAlpha(1f);
    }

    private void hideConfirmAndBackButton() {
        Log.d(TAG, "hideConfirmAndbackButton");
        confirmButton.setAlpha(0f);
        backButton.setAlpha(0f);
    }

    private void showSelectAndCancelButton() {
        Log.d(TAG, "showSelectAndCancelButton");

        selectButton.setAlpha(1f);
        abortButton.setAlpha(1f);
    }

    private void hideSelectAndCancelButton() {
        Log.d(TAG, "hideSelectAndCancelButton");

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

        int activeServiceIndex = ivis.getActiveServiceIndex();

        int selectedItemPos = (int) getResources().getDimension(R.dimen.pos_0_top_margin);

        for(int i = 0; i < serviceNameTextViews.length; i++) {
            int newPos;
            String newColor;
            int newSize;

            if (i < activeServiceIndex) {
                newPos = selectedItemPos - (activeServiceIndex-i) * 100 ;
                serviceNameTextViews[i].setTextColor(Color.parseColor("#707070"));
                serviceNameTextViews[i].setTextSize(30);


            } else if (i == activeServiceIndex) {
                newPos = selectedItemPos;
                serviceNameTextViews[i].setTextColor(Color.parseColor("#FFFFFF"));
                serviceNameTextViews[i].setTextSize(50);

            } else {
                serviceNameTextViews[i].setTextColor(Color.parseColor("#707070"));
                serviceNameTextViews[i].setTextSize(30);

                newPos = selectedItemPos + (i-activeServiceIndex) * 100 + 350 ;
            }

            serviceNameTextViews[i].animate().y(newPos);
        }
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

        recyclerViewServiceNames.scrollToPosition(activeServiceIndex);

        serviceName.setText(ivis.getActiveService().getLabel());
        this.animateServiceRecyclerViews();
    }

    private void updateParameterView() {
        int activeParameterIndex = ivis.getActiveService().getActiveParameterIndex();
        int activeServiceIndex = ivis.getActiveServiceIndex();

        horizontalIndicatorView.setSelection(activeParameterIndex);

        this.parameterName.setText(ivis.getActiveService().getActiveParameter().getLabel());
        recyclerViewServices[activeServiceIndex].smoothScrollToPosition(activeParameterIndex);

        Log.d(TAG, "Scroll to Position : " + activeParameterIndex);
    }

    private void updateValueView() {
        int activeValueIndex = ivis.getActiveService().getActiveParameter().getActiveValueIndex();

        updateHorizontalIndicator();

        recyclerViewValues.smoothScrollToPosition(activeValueIndex);
    }

    /*
    LOCKING METHODS
     */
    public IVIS getIvis() {
        return ivis;
    }

    /*private void lockingHandler() {

        switch(ivis.getLockingMode()) {
            case 1:
                break;
            case 2:
                if (!isInteracting) {
                    isInteracting = true;

                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    lockIvis();
                                }
                            }, maxInteractionDuration);
                }
                break;
            case 3:
                long currentTimestamp = new Date().getTime();
                System.out.println("lockingHandler: CURRENT: " + currentTimestamp + " PREV: " + prevInteractionTimestamp);

                if(currentTimestamp - prevInteractionTimestamp > resetInteractionTime) {
                    currentInputs = 1;
                    System.out.println("lockingHandler: resetInputs = 1");

                    prevInteractionTimestamp = currentTimestamp;
                } else {

                    if (++currentInputs >= interactionsForLock) {
                        System.out.println("lockingHandler: Inputs <3 : " + currentInputs);

                        new android.os.Handler().post(
                                new Runnable() {
                                    public void run() {
                                        lockIvis();
                                    }
                                });
                    }
                }
                break;
            case 4:
                break;
        }
    }*/

    public boolean lockIvis() {

        //dont lock in Baseline
        if(ivis.getLockingMode() == 0) return false;

        Log.d(TAG, "lock IVIS");

        if(ivis.isLocked()) {
            return false;
        } else {
            Log.d(TAG,"send lock");
            //only send when ivis creates locking and not when lockings are send from openDS
            //if (ivis.getLockingMode() < 4) socketClient.sendDataToNetwork("ivis_locked");

            ivis.lock();

            lockingBorder.setVisibility(View.VISIBLE);

            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            unlockIvis();
                        }
                    }, lockingDuration);
            return true;
        }
    }

    public void unlockIvis() {
        Log.d(TAG, "unlock IVIS");

        if(ivis.isLocked()) {
            isInteracting = false;
            Log.d(TAG,"send unlock");

            //socketClient.sendDataToNetwork("ivis_unlocked");
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
        int factor = getResources().getInteger(R.integer.animation_speed);

        //right now only works for same parameter length for every service
        verticalIndicatorView.setCount(ivis.getServiceCount()); // specify total count of indicators
        horizontalIndicatorView.setCount(ivis.getActiveService().getParameterCount());

        ArrayList<String> serviceNames = new ArrayList<>();
        for (int i = 0; i < ivis.getServiceCount() ; i++) {

            serviceNames.add(ivis.getServices().get(i).toString());

            ArrayList<String> params = ivis.getServices().get(i).getParametersAsStringArray();

            CustomLayoutManager layoutManager = new CustomLayoutManager(
                    this, LinearLayoutManager.HORIZONTAL, false, parentWidth, itemWidth, ivis.getServiceCount(), factor);
            RecyclerViewAdapter adapter = new RecyclerViewAdapter(params, this, R.layout.layout_listitem);

            LinearLayout servicesLayout = findViewById(R.id.MainLayout);

            // i+1 because the servicesLayout also hold the animationHelper as child
            recyclerViewServices[i] = (RecyclerView) servicesLayout.getChildAt(i+1);
            recyclerViewServices[i].setLayoutManager(layoutManager);
            recyclerViewServices[i].setAdapter(adapter);
            recyclerViewServices[i].setHorizontalFadingEdgeEnabled(true);
        }

        ArrayList<String> values = ivis.getActiveService().getParameters().get(1).getValues();

        CustomLayoutManager layoutManagerValue = new CustomLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false, parentWidth, itemWidth, values.size(), factor);
        RecyclerViewAdapter adapterValue = new RecyclerViewAdapter(values, this, R.layout.layout_listitem_value);
        recyclerViewValues = findViewById(R.id.recyclerViewValue);
        recyclerViewValues.setLayoutManager(layoutManagerValue);
        recyclerViewValues.setAdapter(adapterValue);
        recyclerViewValues.setHorizontalFadingEdgeEnabled(true);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        socketClient.cancel(true); //In case the task is currently running
    }

}

