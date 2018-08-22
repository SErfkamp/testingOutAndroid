package com.example.serfk.myapplication.Models;

import java.util.ArrayList;

public class IVIS {

    private ArrayList<Service> services;
    private int activeServiceIndex;
    private boolean locked;
    private String name;
    private String label;

    public IVIS(ArrayList<Service> services, String label) {
        this.services = services;
        this.label = label;
    }

    public ArrayList<Service> getServices() {
        return services;
    }

    public void setServices(ArrayList<Service> services) {
        this.services = services;
    }

    public int getActiveServiceIndex() {
        return activeServiceIndex;
    }

    public void setActiveServiceIndex(int activeServiceIndex) {
        this.activeServiceIndex = activeServiceIndex;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean lock(int millisecondsLocked) {
        if(locked) {
            return false;
        } else {
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            locked = false;
                        }
                    }, millisecondsLocked);
            return true;
        }
    }

    public void unlock() {
        locked = false;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
