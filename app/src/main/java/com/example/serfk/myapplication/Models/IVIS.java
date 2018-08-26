package com.example.serfk.myapplication.Models;

import java.io.Serializable;
import java.util.ArrayList;

public class IVIS {

    private ArrayList<Service> services;
    private int activeServiceIndex;
    private boolean locked;
    private int lockingMode;

    public IVIS(ArrayList<Service> services, int lockingMode) {
        this.services = services;
        this.lockingMode = lockingMode;
        this.activeServiceIndex = 0;
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

    public int nextService() {
        return activeServiceIndex < services.size()-1? ++activeServiceIndex : activeServiceIndex;
    }

    public int previousService() {
        return activeServiceIndex > 0? --activeServiceIndex : activeServiceIndex;
    }

    public Service getActiveService() {
        return services.get(activeServiceIndex);
    }

    public int getActiveParameterIndex() {
        return services.get(activeServiceIndex).getActiveParameterIndex();
    }

    public boolean isLocked() {
        return locked;
    }

    public void lock() {
        locked = true;
    }

    public void unlock() {
        locked = false;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public int getLockingMode() {
        return lockingMode;
    }

    public void setLockingMode(int lockingMode) {
        this.lockingMode = lockingMode;
    }

    public int getServiceCount() {
        return this.services.size();
    }

}
