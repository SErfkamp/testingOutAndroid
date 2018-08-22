package com.example.serfk.myapplication.Models;

import java.util.ArrayList;

public class Parameter {

    private String name;
    private String label;
    private String description;
    private int activeValueIndex;

    private ArrayList<String> values;

    public Parameter(String label) {
        this.label = label;
        this.activeValueIndex = 0;
    }

    public ArrayList<String> getValues() {
        return values;
    }

    public void setValues(ArrayList<String> values) {
        this.values = values;
    }

    public int getActiveValueIndex() {
        return activeValueIndex;
    }

    public void setActiveValueIndex(int activeValueIndex) {
        this.activeValueIndex = activeValueIndex;
    }

    public int getCountOfValues() {
        return values.size();
    }

    public int nextValue() {
        return activeValueIndex < values.size()-1? activeValueIndex++: activeValueIndex;
    }

    public int previousValue() {
        return activeValueIndex > 0? activeValueIndex--: activeValueIndex;
    }

    public boolean addValue(String newValue) {
        return values.contains(newValue)? false : this.values.add(newValue);
    }

    public boolean removeValue(String toBeRemoved) {
        return this.values.remove(toBeRemoved);
    }

    public String getName() {
        return name.isEmpty()? label : name;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
