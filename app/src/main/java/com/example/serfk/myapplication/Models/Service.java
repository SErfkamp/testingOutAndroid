package com.example.serfk.myapplication.Models;

import java.util.ArrayList;

public class Service {

    ArrayList<Parameter> parameters = new ArrayList<>();
    private String name;
    private String label;

    private int activeParameterIndex;

    public Service(ArrayList<Parameter> parameters, String label) {
        this.parameters = parameters;
        this.name = name;
        this.label = label;
        this.activeParameterIndex = 0;
    }

    public ArrayList<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(ArrayList<Parameter> parameters) {
        this.parameters = parameters;
    }

    public int nextParameter() {
        return activeParameterIndex < parameters.size()-1? activeParameterIndex++: activeParameterIndex;
    }

    public int previousParameter() {
        return activeParameterIndex > 0? activeParameterIndex--: activeParameterIndex;
    }

    public String getName() {
        return name.isEmpty()? label: name;
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
